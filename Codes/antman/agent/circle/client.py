import datetime
import json
import uuid

from tornado import gen
from tornado.ioloop import IOLoop
from tornado.tcpclient import TCPClient
from tornado.websocket import StreamClosedError

from circle.constants import DEFAULT_ENDPOINT_DEALER, MSG_END
from circle.errors import CallError
from circle.util import rstrip, cast_bytes


def make_message(command, **props):
    return {"command": command, "properties": props or {}}


def cast_message(command, **props):
    return {"command": command, "msg_type": "cast", "properties": props or {}}


def make_json(command, **props):
    return json.dumps(make_message(command, **props))


def gen_timeout(timeout, future):
    return gen.with_timeout(
        timeout=datetime.timedelta(seconds=timeout), future=future)


class AsyncCircleClient(object):

    def __init__(self, endpoint=DEFAULT_ENDPOINT_DEALER, timeout=10.0):
        self.endpoint = endpoint
        self._id = cast_bytes(uuid.uuid4().hex)
        self.timeout = timeout
        self.stream = None
        self.endpoint = endpoint
        self.client = TCPClient()

    def stop(self):
        self.client.close()

    @gen.coroutine
    def send_message(self, command, **props):
        res = yield self.call(make_message(command, **props))
        raise gen.Return(res)

    @gen.coroutine
    def call(self, cmd):
        if isinstance(cmd, basestring):
            raise DeprecationWarning('call() takes a mapping')

        call_id = uuid.uuid4().hex
        cmd['id'] = call_id
        host, port = self.endpoint.split(':')

        try:
            cmd = json.dumps(cmd)
            if not self.stream or self.stream.closed():
                self.stream = yield gen_timeout(self.timeout,
                                                self.client.connect(host, port))

            yield gen_timeout(self.timeout, self.stream.write(cmd + MSG_END))
        except gen.TimeoutError:
            raise CallError('Timed out ({} seconds).'.format(self.timeout))
        except ValueError as e:
            raise CallError(str(e))

        while True:
            try:
                msg = yield gen_timeout(self.timeout,
                                        self.stream.read_until(MSG_END))
                msg = rstrip(msg, MSG_END)
                res = json.loads(msg)
                if 'id' in res and res['id'] not in (call_id, None):
                    # we got the wrong message
                    continue
                raise gen.Return(res)
            except gen.TimeoutError:
                raise CallError('Timed out ({} seconds).'.format(self.timeout))
            except ValueError as e:
                raise CallError(str(e))


class CircleClient(object):

    def __init__(self, endpoint=DEFAULT_ENDPOINT_DEALER, timeout=5.0):
        self.endpoint = endpoint
        self._id = cast_bytes(uuid.uuid4().hex)
        self.timeout = timeout
        self.stream = None
        self.endpoint = endpoint
        self.client = TCPClient()

    def stop(self):
        self.client.close()

    def send_message(self, command, **props):
        return self.call(make_message(command, **props))

    def call(self, cmd):
        result = IOLoop.instance().run_sync(lambda: self._call(cmd))
        return result

    @gen.coroutine
    def _call(self, cmd):
        if isinstance(cmd, basestring):
            raise DeprecationWarning('call() takes a mapping')

        call_id = uuid.uuid4().hex
        cmd['id'] = call_id
        host, port = self.endpoint.split(':')

        try:
            cmd = json.dumps(cmd)
            self.stream = yield gen_timeout(self.timeout,
                                            self.client.connect(host, port))

            yield gen_timeout(self.timeout, self.stream.write(cmd + MSG_END))
        except StreamClosedError:
            raise CallError("Can't connect circled. Maybe it is closed.")
        except gen.TimeoutError:
            raise CallError('Connect timed out ({} seconds).'.format(
                self.timeout))
        except ValueError as e:
            raise CallError(str(e))

        while True:
            try:
                msg = yield gen_timeout(self.timeout,
                                        self.stream.read_until(MSG_END))
                msg = rstrip(msg, MSG_END)
                res = json.loads(msg)
                if 'id' in res and res['id'] not in (call_id, None):
                    # we got the wrong message
                    continue
                raise gen.Return(res)
            except gen.TimeoutError:
                raise CallError('Run timed out ({} seconds).'.format(
                    self.timeout))
            except ValueError as e:
                raise CallError(str(e))
