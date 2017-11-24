"""
Usage:
  circled [--config=<Config>] [--pidfile=<PID-File>] [--daemon]
          [--log-level=<Log-Level>] [--log-output=<Log-Output>]
          [--logger-config=<Logger-Config>]
  circled -h | --help
  circled --version

Options:
  -h, --help                   Show help.
  -c, --config Config          Set config file or dir for Circled.
                               If given config directory, files which match
                               patten '*.ini' will be regarded as config file.
  -l, --log-level Log-Level    Set log level, can be one of info, debug,
                               critical, warning, error, INFO, DEBUG, CRITICAL,
                               WARNING, ERROR.
  -o, --log-output Log-Output  The location where the logs will be written. The
                               default behavior is to write to stdout (you can
                               force it by passing '-' to this option). Takes a
                               filename otherwise.
  -g, --logger-config Logger-Config
                               The location where a standard Python logger
                               configuration INI, JSON or YAML file can be
                               found. This can be used to override the default
                               logging configuration for the arbiter.
  --daemon                     Start circled in the background.
  -p, --pidfile PID-File       Set pid file.
  -v, --version                Show Circled version and exits.
"""
import os
import sys

from docopt import docopt, DocoptExit

from circle import __version__
from circle import logger
from circle.arbiter import Arbiter
from circle.errors import CallError, AlreadyExist, NotExistError
from circle.pidfile import Pidfile
from circle.util import MAXFD, REDIRECT_TO, IS_WINDOWS
from circle.util import check_future_exception_and_log, configure_logger
from circle.winservice import CircleWinService

VERSION = 'Circled Version: {}'.format(__version__)
os.umask(0027)


def get_maxfd():
    return MAXFD


def closerange(fd_low, fd_high):  # NOQA
    # Iterate through and close all file descriptors.
    for fd in range(fd_low, fd_high):
        try:
            os.close(fd)
        except OSError:  # ERROR, fd wasn't open to begin with (ignored)
            pass


# http://www.svbug.com/documentation/comp.unix.programmer-FAQ/faq_2.html#SEC16
def unix_daemonize():
    """
    Standard daemonization of a process.
    """
    # guard to prevent daemonization with gevent loaded
    for module in sys.modules.keys():
        if module.startswith('gevent'):
            raise ValueError('Cannot daemonize if gevent is loaded')

    if hasattr(os, 'fork'):
        child_pid = os.fork()
    else:
        raise ValueError('Daemonizing is not available on this platform.')

    if child_pid != 0:
        # we're in the parent
        os._exit(0)

    # child process
    os.setsid()

    subchild = os.fork()
    if subchild:
        os._exit(0)

    # subchild
    maxfd = get_maxfd()
    closerange(0, maxfd)

    os.open(REDIRECT_TO, os.O_RDWR)
    os.dup2(0, 1)
    os.dup2(0, 2)


def win_daemonize():
    logger.info('Starting deamon mode. '
                'The AntCircled service will be started.')
    args = sys.argv[1:]
    if '--daemon' in args:
        args.remove('--daemon')
    try:
        if not CircleWinService.exists():
            CircleWinService.install(*args)
            CircleWinService.start(*args)
            sys.exit(0)
    except (AlreadyExist, NotExistError, CallError) as e:
        logger.error(e)
        sys.exit(1)


def main(args):
    if args['--daemon'] and not IS_WINDOWS:
        unix_daemonize()

    if IS_WINDOWS:
        win_daemonize()

    arbiter = Arbiter.load_from_config(args['--config'])

    # go ahead and set umask early if it is in the config
    if arbiter.umask is not None:
        os.umask(arbiter.umask)

    # pidfile
    pidfile = None
    pidfile_path = args['--pidfile'] or arbiter.pidfile_path or None
    if pidfile_path:
        pidfile = Pidfile(pidfile_path)

        try:
            pidfile.create(os.getpid())
        except RuntimeError as e:
            print(str(e))
            sys.exit(1)

    # configure the logger
    loglevel = args['--log-level'] or arbiter.loglevel or 'info'
    logoutput = args['--log-output'] or arbiter.logoutput or '-'
    loggerconfig = args['--logger-config'] or arbiter.loggerconfig or None
    configure_logger(loglevel, logoutput, loggerconfig)

    # Main loop
    restart = True
    while restart:
        try:
            arbiter = arbiter or Arbiter.load_from_config(args['--config'])
            future = arbiter.start()
            restart = False
            if check_future_exception_and_log(future) is None:
                restart = arbiter._restarting
        except Exception as e:
            # emergency stop
            if arbiter:
                arbiter.loop.run_sync(arbiter._emergency_stop)
            raise e
        except KeyboardInterrupt:
            break
        finally:
            arbiter = None
            if pidfile is not None:
                pidfile.unlink()
    sys.exit(0)


def parse_args(argv=None):
    cli_args = docopt(__doc__, argv, version=VERSION, options_first=True)
    if cli_args['--log-level'] not in \
            (None, 'info', 'debug', 'critical', 'warning', 'error',
             'INFO', 'DEBUG', 'CRITICAL', 'WARNING', 'ERROR'):
        raise DocoptExit()
    return cli_args


def handle_cli():
    cli_args = parse_args()
    main(cli_args)
