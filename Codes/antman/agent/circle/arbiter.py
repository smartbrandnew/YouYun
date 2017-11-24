import errno
import gc
import logging
import os
from time import sleep

from tornado import gen
from tornado import ioloop

from circle import logger
from circle.config import get_config
from circle.controller import Controller
from circle.errors import AlreadyExist
from circle.util import DictDiffer, synchronized
from circle.util import IS_WINDOWS
from circle.util import debuglog, _setproctitle, parse_env_dict
from circle.watcher import Watcher

_ENV_EXCEPTIONS = ('__CF_USER_TEXT_ENCODING', 'PS1', 'COMP_WORDBREAKS',
                   'PROMPT_COMMAND')


class Arbiter(object):
    """Class used to control a list of watchers.

    Options:

    - **watchers** -- a list of Watcher objects
    - **check_delay** -- the delay between two controller points
      (default: 1 s)
    - **loop**: if provided, a :class:`tornado.ioloop.IOLoop` instance
       to reuse. (default: None)
    - **debug** -- if True, adds a lot of debug info in the stdout (default:
      False)
    - **proc_name** -- the arbiter process name
    """

    def __init__(self,
                 watchers,
                 endpoint,
                 endpoint_owner=None,
                 check_delay=1.0,
                 prereload_fn=None,
                 warmup_delay=0,
                 loop=None,
                 debug=False,
                 debug_gc=False,
                 proc_name='circled',
                 pidfile_path=None,
                 loglevel=None,
                 logoutput=None,
                 loggerconfig=None,
                 umask=None):
        self.watchers = watchers
        self.endpoint = endpoint
        self.endpoint_owner = endpoint_owner
        self.check_delay = check_delay
        self.prereload_fn = prereload_fn
        self.warmup_delay = warmup_delay
        self.proc_name = proc_name
        self.pidfile_path = pidfile_path
        self.loglevel = loglevel
        self.logoutput = logoutput
        self.loggerconfig = loggerconfig
        self.umask = umask
        self.config_path = None
        self._cfg = None
        self._running = False
        self._provided_loop = False

        if loop is None:
            self.loop = ioloop.IOLoop.instance()
        else:
            self._provided_loop = True
            self.loop = loop

        self.ctrl = Controller(self.endpoint, self.loop, self, self.check_delay,
                               self.endpoint_owner)
        self.pid = os.getpid()
        self._watchers_names = {}
        self._stopping = False
        self._restarting = False
        self.debug = debug
        self._exclusive_running_command = None

        if self.debug:
            self.stdout_stream = self.stderr_stream = {'class': 'StdoutStream'}
        else:
            self.stdout_stream = self.stderr_stream = None

        if debug_gc:
            gc.set_debug(gc.DEBUG_LEAK)

    @classmethod
    def load_from_config(cls, config_path):
        cfg = get_config(config_path)
        watchers = []
        for watcher_name in cfg.get('watchers', []):
            watchers.append(Watcher.load_from_config(watcher_name))

        # creating arbiter
        arbiter = cls(
            watchers,
            cfg['endpoint'],
            endpoint_owner=cfg.get('endpoint_owner', None),
            check_delay=cfg.get('check_delay', 1.),
            prereload_fn=cfg.get('prereload_fn'),
            debug=cfg.get('debug', False),
            debug_gc=cfg.get('debug_gc', False),
            pidfile_path=cfg.get('pidfile', None),
            loglevel=cfg.get('loglevel'),
            logoutput=cfg.get('logoutput'),
            loggerconfig=cfg.get('loggerconfig'),
            umask=cfg['umask'],)

        # store the cfg which will be used, so it can be used later
        # for checking if the cfg has been changed
        arbiter._cfg = cls.get_arbiter_config(cfg)
        arbiter.config_path = config_path

        return arbiter

    @classmethod
    def get_arbiter_config(cls, config):
        cfg = config.copy()
        del cfg['watchers']

        return cfg

    @property
    def running(self):
        return self._running

    def get_watcher_config(self, config, name):
        for i in config.get('watchers', []):
            if i['name'] == name:
                return i.copy()
        return None

    @gen.coroutine
    def add_new_watchers(self, new_cfg, watchers):
        if not watchers:
            raise gen.Return()

        for name in watchers:
            new_watcher_cfg = self.get_watcher_config(new_cfg, name)

            watcher = Watcher.load_from_config(new_watcher_cfg)
            watcher.initialize(self)
            yield self.start_watcher(watcher)
            self.watchers.append(watcher)
            self._watchers_names[watcher.name.lower()] = watcher

    @gen.coroutine
    def remove_watchers(self, watchers):
        if not watchers:
            raise gen.Return()

        for name in watchers:
            watcher = self.get_watcher(name)
            yield watcher._stop()
            del self._watchers_names[watcher.name.lower()]
            self.watchers.remove(watcher)

    @gen.coroutine
    def reload_config_env(self, new_cfg, watchers):
        deleted_wn = set()
        added_wn = set()
        if not watchers:
            raise gen.Return((deleted_wn, added_wn))

        for name in watchers:
            watcher = self.get_watcher(name)
            new_watcher_cfg = self.get_watcher_config(new_cfg, name)
            old_watcher_cfg = watcher._cfg.copy()

            if 'env' in new_watcher_cfg:
                new_watcher_cfg['env'] = parse_env_dict(new_watcher_cfg['env'])

            # discarding env exceptions
            for key in _ENV_EXCEPTIONS:
                if 'env' in new_watcher_cfg and key in new_watcher_cfg['env']:
                    del new_watcher_cfg['env'][key]

                if 'env' in new_watcher_cfg and key in old_watcher_cfg['env']:
                    del old_watcher_cfg['env'][key]

            diff = DictDiffer(new_watcher_cfg, old_watcher_cfg).changed()

            if len(diff) > 0:
                # Others things are changed. Just delete and add the watcher.
                deleted_wn.add(name)
                added_wn.add(name)
        raise gen.Return((deleted_wn, added_wn))

    @gen.coroutine
    def reload_from_config_path(self, config_path):
        new_cfg = get_config(config_path)
        if self.get_arbiter_config(new_cfg) != self._cfg:
            yield self._restart(inside_circled=True)
            raise gen.Return()

        current_wn = set([i.name for i in self.iter_watchers()])
        new_wn = set([i['name'] for i in new_cfg.get('watchers', [])])
        added_wn = (new_wn - current_wn)
        common_wn = current_wn & new_wn

        if common_wn:
            deleted, added = yield self.reload_config_env(new_cfg, common_wn)
            # delete watchers
            yield self.remove_watchers(deleted)
            added_wn = added_wn | added
        yield self.add_new_watchers(new_cfg, added_wn)

    @synchronized("arbiter_reload_config")
    @gen.coroutine
    def reload_from_config(self, config_path=None):
        if config_path:
            yield self.reload_from_config_path(config_path)
            return

        new_cfg = get_config(config_path if config_path else self.config_path)
        # if arbiter is changed, reload everything
        if self.get_arbiter_config(new_cfg) != self._cfg:
            yield self._restart(inside_circled=True)
            return

        # Gather watcher names.
        current_wn = set([i.name for i in self.iter_watchers()])
        new_wn = set([i['name'] for i in new_cfg.get('watchers', [])])
        added_wn = (new_wn - current_wn)
        deleted_wn = current_wn - new_wn
        common_wn = current_wn & new_wn

        # get changed watchers
        deleted, added = yield self.reload_config_env(new_cfg, common_wn)
        # delete watchers
        yield self.remove_watchers(deleted_wn | deleted)

        # add watchersadded_wn
        yield self.add_new_watchers(new_cfg, added_wn | added)

    @gen.coroutine
    @debuglog
    def start(self, callback=None):
        """Starts all the watchers.

        If the ioloop has been provided during __init__() call,
        starts all watchers as a standard coroutine

        If the ioloop hasn't been provided during __init__() call (default),
        starts all watchers and the eventloop (and blocks here). In this mode
        the method MUST NOT yield anything because it's called as a standard
        method.

        :param callback: Callback called after all the watchers have been
                   started, when the loop hasn't been provided.
        :type function:
        """
        logger.info('Starting circle arbiter on pid {}'.format(self.pid))
        self.initialize()

        # start controller
        self.ctrl.start()
        self._restarting = False
        try:
            # initialize processes
            logger.info('Starting watchers')
            if self._provided_loop:
                yield self.start_watchers()
            else:
                # start_watchers will be called just after the start_io_loop()
                if not callback:

                    def callback(x):
                        pass

                self.loop.add_future(self.start_watchers(), callback)
            logger.info('Arbiter now waiting for commands')
            self._running = True
            if not self._provided_loop:
                # If an event loop is not provided, block at this line
                self.start_io_loop()
        finally:
            if not self._provided_loop:
                # If an event loop is not provided, do some cleaning
                self.stop_controller_and_close_sockets()
        raise gen.Return(self._restarting)

    @debuglog
    def initialize(self):
        # set process title
        _setproctitle(self.proc_name)

        # set umask even though we may have already set it early in circled.py
        if self.umask is not None:
            os.umask(self.umask)

        # initialize watchers
        for watcher in self.iter_watchers():
            self._watchers_names[watcher.name.lower()] = watcher
            watcher.initialize(self)

    def iter_watchers(self, reverse=True):
        return sorted(self.watchers, key=lambda a: a.priority, reverse=reverse)

    @gen.coroutine
    def start_watcher(self, watcher):
        """Aska a specific watcher to start and wait for the specified
        warmup delay."""
        if watcher.autostart:
            yield watcher._start()
            yield gen.sleep(self.warmup_delay)

    def stop_controller_and_close_sockets(self):
        self.ctrl.stop()
        self._running = False

    def start_io_loop(self):
        """Starts the ioloop and wait inside it
        """
        self.loop.start()

    @synchronized("arbiter_stop")
    @gen.coroutine
    def stop(self):
        yield self._stop()

    @gen.coroutine
    def _emergency_stop(self):
        """Emergency and fast stop, to use only in circled
        """
        for watcher in self.iter_watchers():
            watcher.graceful_timeout = 0
        yield self._stop_watchers()
        self.stop_controller_and_close_sockets()

    @gen.coroutine
    def _stop(self):
        logger.info('Arbiter exiting')
        self._stopping = True
        yield self._stop_watchers(close_output_streams=True)
        if self._provided_loop:
            cb = self.stop_controller_and_close_sockets
            self.loop.add_callback(cb)
        else:
            # stop_controller_and_close_sockets will be
            # called in the end of start() method
            self.loop.add_callback(self.loop.stop)

    def reap_processes(self):
        # map watcher to pids
        watchers_pids = {}
        for watcher in self.iter_watchers():
            if not watcher.is_stopped():
                for process in watcher.processes.values():
                    watchers_pids[process.pid] = watcher

        # detect dead children
        if not IS_WINDOWS:
            while True:
                try:
                    # wait for our child (so it's not a zombie)
                    pid, status = os.waitpid(-1, os.WNOHANG)
                    if not pid:
                        break

                    if pid in watchers_pids:
                        watcher = watchers_pids[pid]
                        watcher.reap_process(pid, status)
                except OSError as e:
                    if e.errno == errno.EAGAIN:
                        sleep(0)
                        continue
                    elif e.errno == errno.ECHILD:
                        # process already reaped
                        return
                    else:
                        raise

    @synchronized("manage_watchers")
    @gen.coroutine
    def manage_watchers(self):
        if self._stopping:
            return

        # manage and reap processes
        self.reap_processes()
        list_to_yield = []
        for watcher in self.iter_watchers():
            list_to_yield.append(watcher.manage_processes())
        if len(list_to_yield) > 0:
            yield list_to_yield

    @synchronized("arbiter_reload")
    @gen.coroutine
    @debuglog
    def reload(self, graceful=True, sequential=False):
        """Reloads everything.

        Run the :func:`prereload_fn` callable if any, then gracefuly
        reload all watchers.
        """
        if self._stopping:
            return
        if self.prereload_fn is not None:
            self.prereload_fn(self)

        # reopen log files
        for handler in logger.handlers:
            if isinstance(handler, logging.FileHandler):
                handler.acquire()
                handler.stream.close()
                handler.stream = open(handler.baseFilename, handler.mode)
                handler.release()

        # gracefully reload watchers
        for watcher in self.iter_watchers():
            yield watcher._reload(graceful=graceful, sequential=sequential)
            yield gen.sleep(self.warmup_delay)

    def numprocesses(self):
        """Return the number of processes running across all watchers."""
        return sum([len(watcher) for watcher in self.watchers])

    def numwatchers(self):
        """Return the number of watchers."""
        return len(self.watchers)

    def get_watcher(self, name):
        """Return the watcher *name*."""
        return self._watchers_names[name.lower()]

    def statuses(self):
        return dict([(watcher.name, watcher.status())
                     for watcher in self.watchers])

    @synchronized("arbiter_start_watchers")
    @gen.coroutine
    def start_watchers(self, watcher_iter_func=None):
        yield self._start_watchers(watcher_iter_func=watcher_iter_func)

    @gen.coroutine
    def _start_watchers(self, watcher_iter_func=None):
        watchers = (watcher_iter_func or self.iter_watchers)()
        for watcher in watchers:
            if watcher.autostart:
                self.loop.spawn_callback(watcher._start)
                # yield watcher._start()
                yield gen.sleep(self.warmup_delay)

    @gen.coroutine
    @debuglog
    def _stop_watchers(self, close_output_streams=False,
                       watcher_iter_func=None):
        if watcher_iter_func is None:
            watchers = self.iter_watchers(reverse=False)
        else:
            watchers = watcher_iter_func(reverse=False)
        yield [w._stop(close_output_streams) for w in watchers]

    @synchronized("arbiter_stop_watchers")
    @gen.coroutine
    def stop_watchers(self, watcher_iter_func=None):
        yield self._stop_watchers(watcher_iter_func=watcher_iter_func)

    @synchronized("arbiter_add_watcher")
    def add_watcher(self, name, cmd, **kw):
        """Adds a watcher.

        Options:

        - **name**: name of the watcher to add
        - **cmd**: command to run.
        - all other options defined in the Watcher constructor.
        """
        if name in self._watchers_names:
            raise AlreadyExist("%r already exist" % name)

        if not name:
            return ValueError("command name shouldn't be empty")

        watcher = Watcher(name, cmd, **kw)
        self.watchers.append(watcher)
        self._watchers_names[watcher.name.lower()] = watcher
        return watcher

    @synchronized("arbiter_rm_watcher")
    @gen.coroutine
    def rm_watcher(self, name, nostop=False):
        """Deletes a watcher.

        Options:

        - **name**: name of the watcher to delete
        """
        logger.debug('Deleting %r watcher', name)

        # remove the watcher from the list
        watcher = self._watchers_names.pop(name.lower())
        del self.watchers[self.watchers.index(watcher)]

        if not nostop:
            # stop the watcher
            yield watcher._stop()

    @gen.coroutine
    def _restart(self, inside_circled=False, watcher_iter_func=None):
        if inside_circled:
            self._restarting = True
            yield self._stop()
        else:
            yield self._stop_watchers(watcher_iter_func=watcher_iter_func)
            yield self._start_watchers(watcher_iter_func=watcher_iter_func)

    @synchronized("arbiter_restart")
    @gen.coroutine
    def restart(self, inside_circled=False, watcher_iter_func=None):
        yield self._restart(
            inside_circled=inside_circled, watcher_iter_func=watcher_iter_func)
