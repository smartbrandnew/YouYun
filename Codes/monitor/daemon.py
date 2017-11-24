import atexit
import errno
import logging
import os
import signal
import sys
import time

from utils.process import is_my_process

log = logging.getLogger(__name__)


class AgentSupervisor(object):
    RESTART_EXIT_STATUS = 5

    @classmethod
    def start(cls, parent_func, child_func=None):
        signal.signal(signal.SIGTERM, cls._handle_sigterm)

        cls.need_stop = False

        while True:
            try:
                if hasattr(cls, 'child_pid'):
                    delattr(cls, 'child_pid')
                pid = os.fork()
                if pid > 0:
                    cls.child_pid = pid
                    while not cls.need_stop:
                        cpid, status = os.waitpid(pid, os.WNOHANG)
                        if (cpid, status) != (0, 0):
                            break
                        time.sleep(1)
                    if parent_func is not None:
                        parent_func()

                    if cls.need_stop:
                        break
                else:
                    if child_func is not None:
                        child_func()
                    else:
                        break
            except OSError, e:
                msg = "Agent fork failed: %d (%s)" % (e.errno, e.strerror)
                logging.error(msg)
                sys.stderr.write(msg + "\n")
                sys.exit(1)

        if pid > 0:
            sys.exit(0)

    @classmethod
    def _handle_sigterm(cls, signum, frame):
        if hasattr(cls, 'child_pid'):
            os.kill(cls.child_pid, signal.SIGTERM)
            cls.need_stop = True
        else:
            sys.exit(0)


class Daemon(object):
    def __init__(self, pidfile, stdin=os.devnull, stdout=os.devnull, stderr=os.devnull, autorestart=False):
        self.autorestart = autorestart
        self.stdin = stdin
        self.stdout = stdout
        self.stderr = stderr
        self.pidfile = pidfile

    def daemonize(self):
        try:
            pid = os.fork()
            if pid > 0:
                sys.exit(0)
        except OSError, e:
            msg = "fork #1 failed: %d (%s)" % (e.errno, e.strerror)
            log.error(msg)
            sys.stderr.write(msg + "\n")
            sys.exit(1)

        log.debug("Fork 1 ok")

        os.chdir("/")
        os.setsid()

        if self.autorestart:
            logging.info('Running with auto-restart ON')
            AgentSupervisor.start(parent_func=None, child_func=None)
        else:
            try:
                pid = os.fork()
                if pid > 0:
                    sys.exit(0)
            except OSError, e:
                msg = "fork #2 failed: %d (%s)" % (e.errno, e.strerror)
                logging.error(msg)
                sys.stderr.write(msg + "\n")
                sys.exit(1)

        if sys.platform != 'darwin':
            sys.stdout.flush()
            sys.stderr.flush()
            si = file(self.stdin, 'r')
            so = file(self.stdout, 'a+')
            se = file(self.stderr, 'a+', 0)
            os.dup2(si.fileno(), sys.stdin.fileno())
            os.dup2(so.fileno(), sys.stdout.fileno())
            os.dup2(se.fileno(), sys.stderr.fileno())

        log.info("Daemon started")

    def start(self, foreground=False):
        log.info("Starting")
        pid = self.pid()

        if pid:
            if is_my_process(pid):
                log.error("Not starting, another instance is already running"
                          " (using pidfile {0})".format(self.pidfile))
                sys.exit(1)
            else:
                log.warn("pidfile doesn't contain the pid of an agent process."
                         ' Starting normally')

        log.info("Pidfile: %s" % self.pidfile)
        if not foreground:
            self.daemonize()
        self.write_pidfile()
        self.run()

    def stop(self):
        log.info("Stopping daemon")
        pid = self.pid()

        if os.path.exists(self.pidfile):
            os.remove(self.pidfile)

        if pid > 1:
            try:
                if self.autorestart:
                    try:
                        os.kill(os.getpgid(pid), signal.SIGTERM)
                    except OSError:
                        log.warn("Couldn't not kill parent pid %s. Killing pid." % os.getpgid(pid))
                        os.kill(pid, signal.SIGTERM)
                else:
                    os.kill(pid, signal.SIGTERM)
                log.info("Daemon is stopped")
            except OSError, err:
                if str(err).find("No such process") <= 0:
                    log.exception("Cannot kill Agent daemon at pid %s" % pid)
                    sys.stderr.write(str(err) + "\n")
        else:
            message = "Pidfile %s does not exist. Not running?\n" % self.pidfile
            log.info(message)
            sys.stderr.write(message)

            if os.path.exists(self.pidfile):
                os.remove(self.pidfile)

            return

    def restart(self):
        self.stop()
        self.start()

    def run(self):
        raise NotImplementedError

    @classmethod
    def info(cls):
        raise NotImplementedError

    def status(self):
        pid = self.pid()

        if pid < 0:
            message = '%s is not running' % self.__class__.__name__
            exit_code = 1
        else:
            try:
                os.kill(pid, 0)
            except OSError, e:
                if e.errno != errno.EPERM:
                    message = '%s pidfile contains pid %s, but no running process could be found' % (
                    self.__class__.__name__, pid)
                else:
                    message = 'You do not have sufficient permissions'
                exit_code = 1

            else:
                message = '%s is running with pid %s' % (self.__class__.__name__, pid)
                exit_code = 0

        log.info(message)
        sys.stdout.write(message + "\n")
        sys.exit(exit_code)

    def pid(self):
        try:
            pf = file(self.pidfile, 'r')
            pid = int(pf.read().strip())
            pf.close()
            return pid
        except IOError:
            return None
        except ValueError:
            return None

    def write_pidfile(self):
        atexit.register(self.delpid)
        pid = str(os.getpid())
        try:
            fp = open(self.pidfile, 'w+')
            fp.write(str(pid))
            fp.close()
            os.chmod(self.pidfile, 0644)
        except Exception:
            msg = "Unable to write pidfile: %s" % self.pidfile
            log.exception(msg)
            sys.stderr.write(msg + "\n")
            sys.exit(1)

    def delpid(self):
        try:
            os.remove(self.pidfile)
        except OSError:
            pass
