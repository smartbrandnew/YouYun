import functools
import json
import sys
import traceback

from tornado import ioloop
from tornado.concurrent import Future
from tornado.tcpserver import TCPServer

from circle import logger
from circle.commands import get_commands, ok, error, errors
from circle.constants import MSG_END
from circle.errors import MessageError, ConflictError
from circle.sighandler import SysHandler
from circle.util import rstrip, check_future_exception_and_log, IS_WINDOWS
from circle.winservice import CircleWinService


class Controller(object):

    def __init__(self,
                 endpoint,
                 loop,
                 arbiter,
                 check_delay=1.0,
                 endpoint_owner=None):
        self.arbiter = arbiter
        self.caller = None
        self.endpoint = endpoint
        self.loop = loop
        self.check_delay = check_delay * 1000
        self.endpoint_owner = endpoint_owner
        self.started = False
        self.managing_watchers_future = None

        # initialize the sys handler
        self.sys_handler = SysHandler(self)

        # get registered commands
        self.commands = get_commands()

    def initialize(self):
        address, port = self.endpoint.split(':')
        self.ctrl_server = ControllerServer(self)
        self.ctrl_server.listen(port, address)

    def start(self):
        logger.debug('Starting controller')
        self.initialize()
        if self.check_delay > 0:
            # The specific case (check_delay < 0)
            # so with no period callback to manage_watchers
            # is probably "unit tests only"
            self.caller = ioloop.PeriodicCallback(self.manage_watchers,
                                                  self.check_delay, self.loop)
            self.caller.start()
        self.started = True

    def stop(self):
        if self.started:
            if self.caller is not None:
                self.caller.stop()
            self.ctrl_server.stop()
        self.sys_handler.stop()

    def quit(self):
        self.arbiter.stop()

    def manage_watchers(self):
        if self.managing_watchers_future is not None:
            logger.debug('manage_watchers is already running...')
            return
        try:
            self.managing_watchers_future = self.arbiter.manage_watchers()
            self.loop.add_future(self.managing_watchers_future,
                                 self._manage_watchers_cb)
        except ConflictError:
            logger.debug('manage_watchers is conflicting with another command')

    def _manage_watchers_cb(self, future):
        self.managing_watchers_future = None


class ControllerConnection(object):

    def __init__(self, stream, address, controller):
        self.stream = stream
        self.address = address
        self.controller = controller
        self.read_message()

    def read_message(self):
        self.stream.read_until(MSG_END, self.handle_message)

    def handle_message(self, msg):
        msg = rstrip(msg, MSG_END)

        if not msg:
            self.send_error(None, msg, 'error: empty command')
        else:
            self.dispatch(msg)

        self.read_message()

    def send_error(self,
                   mid,
                   msg,
                   reason='unknown',
                   tb=None,
                   errno=errors.NOT_SPECIFIED):
        resp = error(reason=reason, tb=tb, errno=errno)
        self.send_response(mid, msg, resp)

    def send_ok(self, mid, msg, props=None):
        resp = ok(props)
        self.send_response(mid, msg, resp)

    def send_response(self, mid, msg, resp):
        if isinstance(resp, basestring):
            raise DeprecationWarning('Takes only a mapping')

        resp['id'] = mid
        resp = json.dumps(resp)
        self.stream.write(resp + MSG_END)

    def dispatch(self, msg):
        try:
            json_msg = json.loads(msg)
        except ValueError:
            return self.send_error(
                None, msg, 'json invalid', errno=errors.INVALID_JSON)

        mid = json_msg.get('id')
        cmd_name = json_msg.get('command')
        properties = json_msg.get('properties', {})

        try:
            cmd = self.controller.commands[cmd_name.lower()]
        except KeyError:
            error_ = 'unknown command: {!r}'.format(cmd_name)
            return self.send_error(
                mid, msg, error_, errno=errors.UNKNOWN_COMMAND)

        try:
            cmd.validate(properties)
            resp = cmd.execute(self.controller.arbiter, properties)
            if isinstance(resp, Future):
                if properties.get('waiting', False):
                    cb = functools.partial(self._dispatch_callback_future, msg,
                                           mid, cmd_name, True)
                    resp.add_done_callback(cb)
                else:
                    cb = functools.partial(self._dispatch_callback_future, msg,
                                           mid, cmd_name, False)
                    resp.add_done_callback(cb)
                    self._dispatch_callback(msg, mid, cmd_name, None)
            else:
                self._dispatch_callback(msg, mid, cmd_name, resp)
        except MessageError as e:
            return self.send_error(mid, msg, str(e), errno=errors.MESSAGE_ERROR)
        except ConflictError as e:
            if self.controller.managing_watchers_future is not None:
                logger.debug('The command conflicts with running '
                             'manage_watchers, re-executing it at '
                             'the end')
                cb = functools.partial(self.dispatch, msg)
                self.controller.loop.add_future(
                    self.controller.managing_watchers_future, cb)
                return
            # conflicts between two commands, sending error...
            return self.send_error(mid, msg, str(e), errno=errors.COMMAND_ERROR)
        except OSError as e:
            return self.send_error(mid, msg, str(e), errno=errors.OS_ERROR)
        except:
            exctype, value = sys.exc_info()[:2]
            tb = traceback.format_exc()
            reason = 'Command {!r}: {}'.format(msg, value)
            logger.debug('Error: command {!r}: {}\n\n{}'.format(msg, value, tb))
            return self.send_error(
                mid, msg, reason, tb, errno=errors.COMMAND_ERROR)

    def _dispatch_callback_future(self, msg, mid, cmd_name, send_resp, future):
        exception = check_future_exception_and_log(future)
        if exception is not None:
            if send_resp:
                self.send_error(
                    mid,
                    msg,
                    error('Server error'),
                    errno=errors.BAD_MSG_DATA_ERROR)
        else:
            resp = future.result()
            if send_resp:
                self._dispatch_callback(msg, mid, cmd_name, resp)

    def _dispatch_callback(self, msg, mid, cmd_name, resp=None):
        if resp is None:
            resp = ok()

        if not isinstance(resp, (dict, list)):
            msg = 'Message {!r} tried to send a non-dict: {}'\
                .format(msg, str(resp))
            logger.error(msg)
            return self.send_error(
                mid,
                msg,
                error('server error'),
                errno=errors.BAD_MSG_DATA_ERROR)

        if isinstance(resp, list):
            resp = {'results': resp}

        self.send_ok(mid, msg, resp)

        if cmd_name.lower() == 'quit':
            self.controller.arbiter.stop()
            if IS_WINDOWS and CircleWinService.status() == 'running':
                CircleWinService.stop()


class ControllerServer(TCPServer):

    def __init__(self, controller):
        self.controller = controller
        super(ControllerServer, self).__init__()

    def handle_stream(self, stream, address):
        ControllerConnection(stream, address, self.controller)
