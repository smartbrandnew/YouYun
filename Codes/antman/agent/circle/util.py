import functools
import json
import logging
import logging.config
import os
import re
import shlex
import sys
import time
import traceback
import locale
from subprocess import Popen, PIPE, STDOUT

try:
    import yaml
except ImportError:
    yaml = None  # NOQA
try:
    import papa
except ImportError:
    papa = None  # NOQA
try:
    import pwd
    import grp
    import fcntl
except ImportError:
    fcntl = None
    grp = None
    pwd = None
from tornado import concurrent
from concurrent.futures import ThreadPoolExecutor

try:
    from urllib.parse import urlparse
except ImportError:
    from urlparse import urlparse  # NOQA

from datetime import timedelta
from functools import wraps
import signal
from pipes import quote as shell_escape_arg

try:
    import importlib
    reload_module = importlib.reload
except (ImportError, AttributeError):
    from imp import reload as reload_module

from psutil import AccessDenied, NoSuchProcess, Process

from circle.errors import ConflictError

try:
    from setproctitle import setproctitle

    def _setproctitle(title):  # NOQA
        setproctitle(title)
except ImportError:

    def _setproctitle(title):  # NOQA
        return


MAXFD = 1024
if hasattr(os, 'devnull'):
    REDIRECT_TO = os.devnull  # PRAGMA: NOCOVER
else:
    REDIRECT_TO = '/dev/null'  # PRAGMA: NOCOVER

LOG_LEVELS = {
    'critical': logging.CRITICAL,
    'error': logging.ERROR,
    'warning': logging.WARNING,
    'info': logging.INFO,
    'debug': logging.DEBUG
}

LOG_FMT = r'%(asctime)s %(name)s[%(process)d] [%(levelname)s] %(message)s'
LOG_DATE_FMT = r'%Y-%m-%d %H:%M:%S'
LOG_DATE_SYSLOG_FMT = r'%b %d %H:%M:%S'
_SYMBOLS = ('K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y')
_all_signals = {}

IS_WINDOWS = os.name == 'nt'
ENV_DICT = None
DEFAULT_CODING = locale.getpreferredencoding()


def rstrip(string, pattern):
    return re.sub(pattern + '$', '', string)


def get_status_output(args, cwd=None):
    proc = Popen(args, stdout=PIPE, stderr=STDOUT, shell=True, cwd=cwd)
    result, _ = proc.communicate()
    return proc.returncode, result


def async_get_status_output(args, cwd=None):
    _executor = ThreadPoolExecutor(max_workers=1)
    future = _executor.submit(get_status_output, args, cwd)
    return future


def sort_by_field(obj, field='name'):  # NOQA

    def _by_field(item1, item2):
        return cmp(item1[field], item2[field])  # NOQA

    obj.sort(_by_field)


def get_working_dir():
    """Returns current path, try to use PWD env first.

    Since os.getcwd() resolves symlinks, we want to use
    PWD first if present.
    """
    pwd_ = os.environ.get('PWD')
    cwd = os.getcwd()

    if pwd_ is None:
        return cwd

    # if pwd is the same physical file than the one
    # pointed by os.getcwd(), we use it.
    try:
        pwd_stat = os.stat(pwd_)
        cwd_stat = os.stat(cwd)

        if pwd_stat.st_ino == cwd_stat.st_ino and \
                pwd_stat.st_dev == cwd_stat.st_dev:
            return pwd_
    except Exception:
        pass

    # otherwise, just use os.getcwd()
    return cwd


def bytes2human(n):
    """Translates bytes into a human repr.
    """
    if not isinstance(n, (int, long)):
        raise TypeError(n)

    prefix = {}
    for i, s in enumerate(_SYMBOLS):
        prefix[s] = 1 << (i + 1) * 10

    for s in reversed(_SYMBOLS):
        if n >= prefix[s]:
            value = int(float(n) / prefix[s])
            return '%s%s' % (value, s)
    return '%sB' % n


_HSYMBOLS = {
    'customary': ('B', 'K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'),
    'customary_ext': ('byte', 'kilo', 'mega', 'giga', 'tera', 'peta', 'exa',
                      'zetta', 'iotta'),
    'iec': ('Bi', 'Ki', 'Mi', 'Gi', 'Ti', 'Pi', 'Ei', 'Zi', 'Yi'),
    'iec_ext': ('byte', 'kibi', 'mebi', 'gibi', 'tebi', 'pebi', 'exbi', 'zebi',
                'yobi'),
}

_HSYMBOLS_VALUES = _HSYMBOLS.values()


def human2bytes(s):
    init = s
    num = ''
    while s and s[0:1].isdigit() or s[0:1] == '.':
        num += s[0]
        s = s[1:]

    num = float(num)
    letter = s.strip()

    for sset in _HSYMBOLS_VALUES:
        if letter in sset:
            break

    else:
        if letter == 'k':
            # treat 'k' as an alias for 'K' as per: http://goo.gl/kTQMs
            sset = _HSYMBOLS['customary']
            letter = letter.upper()
        else:
            raise ValueError("can't interpret %r" % init)

    prefix = {sset[0]: 1}
    for i, s in enumerate(sset[1:]):
        prefix[s] = 1 << (i + 1) * 10

    return int(num * prefix[letter])


# XXX weak dict ?
_PROCS = {}


def to_string(s):
    if isinstance(s, unicode):
        return s.encode(DEFAULT_CODING)
    return s


def cast_bytes(s, encoding='utf8'):
    """cast unicode or bytes to bytes"""
    if isinstance(s, unicode):
        return s.encode(encoding)
    return str(s)


def cast_unicode(s, encoding='utf8', errors='replace'):
    """cast bytes or unicode to unicode.
      errors options are strict, ignore or replace"""
    if isinstance(s, unicode):
        return s
    return str(s).decode(encoding)


def cast_string(s, errors='replace'):
    return s if isinstance(s, basestring) else str(s)  # NOQA


def get_info(process=None, interval=0, with_childs=False):
    """Return information about a process. (can be an pid or a Process object)
    If process is None, will return the information about the current process.
    """
    # XXX moce get_info to circle.process ?
    from circle.process import (get_children, get_memory_info, get_cpu_percent,
                                get_memory_percent, get_cpu_times, get_nice,
                                get_cmdline, get_create_time, get_username)

    if process is None or isinstance(process, int):
        if process is None:
            pid = os.getpid()
        else:
            pid = process

        if pid in _PROCS:
            process = _PROCS[pid]
        else:
            _PROCS[pid] = process = Process(pid)

    info = {}
    try:
        mem_info = get_memory_info(process)
        info['mem_info1'] = bytes2human(mem_info[0])
        info['mem_info2'] = bytes2human(mem_info[1])
    except AccessDenied:
        info['mem_info1'] = info['mem_info2'] = 'N/A'

    try:
        info['cpu'] = get_cpu_percent(process, interval=interval)
    except AccessDenied:
        info['cpu'] = 'N/A'

    try:
        info['mem'] = round(get_memory_percent(process), 3)
    except AccessDenied:
        info['mem'] = 'N/A'

    try:
        cpu_times = get_cpu_times(process)
        ctime = timedelta(seconds=sum(cpu_times))
        ctime = '%s:%s.%s' % (ctime.seconds // 60 % 60, str(
            (ctime.seconds % 60)).zfill(2), str(ctime.microseconds)[:2])
    except AccessDenied:
        ctime = 'N/A'

    info['ctime'] = ctime

    try:
        info['pid'] = process.pid
    except AccessDenied:
        info['pid'] = 'N/A'

    try:
        info['username'] = get_username(process)
    except AccessDenied:
        info['username'] = 'N/A'

    try:
        info['nice'] = get_nice(process)
    except AccessDenied:
        info['nice'] = 'N/A'
    except NoSuchProcess:
        info['nice'] = 'Zombie'

    try:
        raw_cmdline = get_cmdline(process)

        cmdline = os.path.basename(
            shlex.split(
                raw_cmdline[0], posix=not IS_WINDOWS)[0])
    except (AccessDenied, IndexError):
        cmdline = 'N/A'

    try:
        info['create_time'] = get_create_time(process)
    except AccessDenied:
        info['create_time'] = 'N/A'

    try:
        info['age'] = time.time() - get_create_time(process)
    except TypeError:
        info['create_time'] = get_create_time(process)
    except AccessDenied:
        info['age'] = 'N/A'

    info['cmdline'] = cmdline

    info['children'] = []
    if with_childs:
        for child in get_children(process):
            info['children'].append(get_info(child, interval=interval))

    return info


TRUTHY_STRINGS = ('yes', 'true', 'on', '1')
FALSY_STRINGS = ('no', 'false', 'off', '0')


def to_bool(s):
    if isinstance(s, bool):
        return s
    if s is None:
        return False

    if s.lower().strip() in TRUTHY_STRINGS:
        return True
    elif s.lower().strip() in FALSY_STRINGS:
        return False
    else:
        raise ValueError('%r is not a boolean' % s)


def to_signum(signum):
    """Resolves the signal number from arbitrary signal representation.

     Supported formats:
        10 - plain integers
        '10' - integers as a strings
        'KILL' - signal names
        'SIGKILL' - signal names with SIG prefix
        'SIGRTMIN+1' - signal names with offsets
    """
    if isinstance(signum, int):
        return signum

    m = re.match(r'(\w+)(\+(\d+))?', signum)
    if m:
        name = m.group(1).upper()
        if not name.startswith('SIG'):
            name = 'SIG' + name

        offset = int(m.group(3)) if m.group(3) else 0

        try:
            return getattr(signal, name) + offset
        except KeyError:
            pass

    raise ValueError('signal invalid: {}'.format(signum))


if pwd is None:

    def to_uid(name):
        raise RuntimeError('"to_uid" not available on this operating system')

else:

    def to_uid(name):  # NOQA
        """Return an uid, given a user name.
        If the name is an integer, make sure it's an existing uid.

        If the user name is unknown, raises a ValueError.
        """
        try:
            name = int(name)
        except ValueError:
            pass

        if isinstance(name, int):
            try:
                pwd.getpwuid(name)
                return name
            except KeyError:
                raise ValueError("%r isn't a valid user id" % name)

        if not isinstance(name, to_string):
            raise TypeError(name)

        try:
            return pwd.getpwnam(name).pw_uid
        except KeyError:
            raise ValueError("%r isn't a valid user name" % name)


if grp is None:

    def to_gid(name):
        raise RuntimeError('"to_gid" not available on this operating system')

else:

    def to_gid(name):  # NOQA
        """Return a gid, given a group name

        If the group name is unknown, raises a ValueError.
        """
        try:
            name = int(name)
        except ValueError:
            pass

        if isinstance(name, int):
            try:
                grp.getgrgid(name)
                return name
            # getgrid may raises overflow error on mac/os x,
            # fixed in python2.7.5
            # see http://bugs.python.org/issue17531
            except (KeyError, OverflowError):
                raise ValueError('No such group: %r' % name)

        if not isinstance(name, to_string):
            raise TypeError(name)

        try:
            return grp.getgrnam(name).gr_gid
        except KeyError:
            raise ValueError('No such group: %r' % name)


def get_username_from_uid(uid):
    """Return the username of a given uid."""
    if isinstance(uid, int):
        return pwd.getpwuid(uid).pw_name
    return uid


def get_default_gid(uid):
    """Return the default group of a specific user."""
    if isinstance(uid, int):
        return pwd.getpwuid(uid).pw_gid
    return pwd.getpwnam(uid).pw_gid


def parse_env_str(env_str):
    env = dict()
    for kvs in env_str.split(','):
        k, v = kvs.split('=')
        env[k.strip()] = v.strip()
    return parse_env_dict(env)


def parse_env_dict(env):
    search_env = functools.partial(replace_env, env)
    ret = dict()
    for k, v in env.items():
        v = re.sub(r'\$([A-Z]+[A-Z0-9_]*)', search_env, v)
        ret[k.strip()] = v.strip()
    return ret


def replace_env(env, var):
    if os.getenv(var.group(1)):
        return os.getenv(var.group(1))
    return env.get(var.group(1))


def env_to_str(env):
    if not env:
        return ''
    return ','.join([
        '{}={}'.format(k, v) for k, v in sorted(
            env.items(), key=lambda i: i[0])
    ])


if fcntl is None:

    def close_on_exec(fd):
        raise RuntimeError(
            '"close_on_exec" not available on this operating system')

else:

    def close_on_exec(fd):  # NOQA
        flags = fcntl.fcntl(fd, fcntl.F_GETFD)
        flags |= fcntl.FD_CLOEXEC
        fcntl.fcntl(fd, fcntl.F_SETFD, flags)


INDENTATION_LEVEL = 0


def debuglog(func):

    @wraps(func)
    def _log(self, *args, **kw):
        if os.environ.get('DEBUG') is None:
            return func(self, *args, **kw)

        from circle import logger
        cls = self.__class__.__name__
        global INDENTATION_LEVEL
        func_name = func.func_name if hasattr(func, 'func_name')\
            else func.__name__
        logger.debug('    ' * INDENTATION_LEVEL + '"{}.{}" starts'.format(
            cls, func_name))
        INDENTATION_LEVEL += 1
        try:
            return func(self, *args, **kw)
        finally:
            INDENTATION_LEVEL -= 1
            logger.debug('    ' * INDENTATION_LEVEL + '"{}.{}" ends'.format(
                cls, func_name))

    return _log


def convert_opt(key, val):
    """ get opt
    """
    if key == 'env':
        val = env_to_str(val)
    else:
        if val is None:
            val = ''
        else:
            val = str(val)
    return val


# taken from werkzeug
class ImportStringError(ImportError):
    """Provides information about a failed :func:`import_string` attempt."""

    #: String in dotted notation that failed to be imported.
    import_name = None
    #: Wrapped exception.
    exception = None

    def __init__(self, import_name, exception):
        self.import_name = import_name
        self.exception = exception

        msg = ('import_string() failed for %r. Possible reasons are:\n\n'
               '- missing __init__.py in a package;\n'
               '- package or module path not included in sys.path;\n'
               '- duplicated package or module name taking precedence in '
               'sys.path;\n'
               '- missing module, class, function or variable;\n\n'
               'Debugged import:\n\n%s\n\n'
               'Original exception:\n\n%s: %s')

        name = ''
        tracked = []
        for part in import_name.replace(':', '.').split('.'):
            name += (name and '.') + part
            imported = resolve_name(name, silent=True)
            if imported:
                tracked.append((name, getattr(imported, '__file__', None)))
            else:
                track = ['- %r found in %r.' % (n, i) for n, i in tracked]
                track.append('- %r not found.' % name)
                msg = msg % (import_name, '\n'.join(track),
                             exception.__class__.__name__, str(exception))
                break

        ImportError.__init__(self, msg)

    def __repr__(self):
        return '<%s(%r, %r)>' % (self.__class__.__name__, self.import_name,
                                 self.exception)


def resolve_name(import_name, silent=False, reload=False):
    """Imports an object based on a string.  This is useful if you want to
    use import paths as endpoints or something similar.  An import path can
    be specified either in dotted notation (``xml.sax.saxutils.escape``)
    or with a colon as object delimiter (``xml.sax.saxutils:escape``).

    If `silent` is True the return value will be `None` if the import fails.

    :param import_name: the dotted name for the object to import.
    :param silent: if set to `True` import errors are ignored and
                   `None` is returned instead.
    :param reload: if set to `True` modules that are already loaded will be
                   reloaded
    :return: imported object
    """
    # force the import name to automatically convert to strings
    import_name = to_string(import_name)
    try:
        if ':' in import_name:
            module, obj = import_name.split(':', 1)
        elif '.' in import_name and import_name not in sys.modules:
            module, obj = import_name.rsplit('.', 1)
        else:
            module, obj = import_name, None
            # __import__ is not able to handle basestring strings in the
            # fromlist

        mod = None
        # if the module is a package
        if reload and module in sys.modules:
            try:
                importlib.invalidate_caches()
            except Exception:
                pass
            try:
                mod = reload_module(sys.modules[module])
            except Exception:
                pass
        if not mod:
            if not obj:
                return __import__(module)
            try:
                mod = __import__(module, None, None, [obj])
            except ImportError:
                if ':' in import_name:
                    raise
                return __import__(import_name)
        if not obj:
            return mod
        try:
            return getattr(mod, obj)
        except AttributeError:
            # support importing modules not yet set up by the parent module
            # (or package for that matter)
            if ':' in import_name:
                raise
            return __import__(import_name)
    except ImportError as e:
        if not silent:
            raise ImportStringError(import_name, e), None, sys.exc_info()[2]


_SECTION_NAME = '\w\.\-'
_PATTERN1 = r'\$\(%%s\.([%s]+)\)' % _SECTION_NAME
_PATTERN2 = r'\(\(%%s\.([%s]+)\)\)' % _SECTION_NAME
_CIRCLE_VAR = re.compile(_PATTERN1 % 'circle' + '|' + _PATTERN2 % 'circle',
                         re.I)


def replace_gnu_args(data, prefix='circle', **options):
    fmt_options = {}
    for key, value in options.items():
        key = key.lower()

        if prefix is not None:
            key = '%s.%s' % (prefix, key)

        if isinstance(value, dict):
            for subkey, subvalue in value.items():
                subkey = subkey.lower()
                subkey = '%s.%s' % (key, subkey)
                fmt_options[subkey] = subvalue
        else:
            fmt_options[key] = value

    if prefix is None:
        pattern = r'\$\(([%s]+)\)|\(\(([%s]+)\)\)' % (_SECTION_NAME,
                                                      _SECTION_NAME)
        match = re.compile(pattern, re.I)
    elif prefix == 'circle':
        match = _CIRCLE_VAR
    else:
        match = re.compile(_PATTERN1 % prefix + '|' + _PATTERN2 % prefix, re.I)

    def _repl(matchobj):
        option = None

        for result in matchobj.groups():
            if result is not None:
                option = result.lower()
                break

        if prefix is not None and not option.startswith(prefix):
            option = '%s.%s' % (prefix, option)

        if option in fmt_options:
            return u'"{}"'.format(fmt_options[option].decode(DEFAULT_CODING)) \
                if ' ' in fmt_options[option] else fmt_options[option]

        return matchobj.group()

    return match.sub(_repl, data)


class ObjectDict(dict):

    def __getattr__(self, item):
        return self[item]


def configure_logger(level='INFO', output='-', loggerconfig=None):
    if loggerconfig is None or loggerconfig.lower().strip() == 'default':
        logger = logging.getLogger()
        loglevel = LOG_LEVELS.get(level.lower(), logging.INFO)
        logger.setLevel(loglevel)
        datefmt = LOG_DATE_FMT
        if output in ('-', 'stdout'):
            handler = logging.StreamHandler()
        elif output.startswith('syslog://'):
            # URLs are syslog://host[:port]?facility or syslog:///path?facility
            info = urlparse(output)
            facility = 'user'
            if info.query in logging.handlers.SysLogHandler.facility_names:
                facility = info.query
            if info.netloc:
                address = (info.netloc, info.port or 514)
            else:
                address = info.path
            datefmt = LOG_DATE_SYSLOG_FMT
            handler = logging.handlers.SysLogHandler(
                address=address, facility=facility)
        else:
            # if not IS_WINDOWS:
            #    handler = logging.handlers.WatchedFileHandler(output)
            #    close_on_exec(handler.stream.fileno())
            # else:
            # WatchedFileHandler is not supported on Windows,
            # but a FileHandler should be a good drop-in replacement
            # as log files are locked
            handler = logging.handlers.RotatingFileHandler(
                output, maxBytes=10 * 1024 * 1024, backupCount=5)
        formatter = logging.Formatter(fmt=LOG_FMT, datefmt=datefmt)
        handler.setFormatter(formatter)
        logger.handlers = [handler]
    else:
        loggerconfig = os.path.realpath(loggerconfig)
        if loggerconfig.lower().endswith('.ini'):
            logging.config.fileConfig(
                loggerconfig, disable_existing_loggers=True)
        elif loggerconfig.lower().endswith('.json'):
            if not hasattr(logging.config, 'dictConfig'):
                raise Exception('Logger configuration file %s appears to be '
                                'a JSON file but this version of Python '
                                'does not support the '
                                'logging.config.dictConfig function. Try '
                                'Python 2.7.')
            with open(loggerconfig, 'r') as fh:
                logging.config.dictConfig(json.loads(fh.read()))
        elif loggerconfig.lower().endswith('.yaml'):
            if not hasattr(logging.config, 'dictConfig'):
                raise Exception('Logger configuration file %s appears to be '
                                'a YAML file but this version of Python '
                                'does not support the '
                                'logging.config.dictConfig function. Try '
                                'Python 2.7.')
            if yaml is None:
                raise Exception('Logger configuration file %s appears to be '
                                'a YAML file but PyYAML is not available. '
                                'Try: pip install PyYAML' %
                                (shell_escape_arg(loggerconfig),))
            with open(loggerconfig, 'r') as fh:
                logging.config.dictConfig(yaml.load(fh.read()))
        else:
            raise Exception('Logger configuration file %s is not in one '
                            'of the recognized formats.  The file name '
                            'should be: *.ini, *.json or *.yaml.' %
                            (shell_escape_arg(loggerconfig),))


# taken from http://stackoverflow.com/questions/1165352


class DictDiffer(object):
    """
    Calculate the difference between two dictionaries as:
    (1) items added
    (2) items removed
    (3) keys same in both but changed values
    (4) keys same in both and unchanged values
    """

    def __init__(self, current_dict, past_dict):
        self.current_dict, self.past_dict = current_dict, past_dict
        self.set_current, self.set_past = (set(current_dict.keys()),
                                           set(past_dict.keys()))
        self.intersect = self.set_current.intersection(self.set_past)

    def added(self):
        return self.set_current - self.intersect

    def removed(self):
        return self.set_past - self.intersect

    def changed(self):
        return set(o for o in self.intersect
                   if self.past_dict[o] != self.current_dict[o])

    def unchanged(self):
        return set(o for o in self.intersect
                   if self.past_dict[o] == self.current_dict[o])


def dict_differ(dict1, dict2):
    return len(DictDiffer(dict1, dict2).changed()) > 0


def _synchronized_cb(arbiter, future):
    if arbiter is not None:
        arbiter._exclusive_running_command = None


def synchronized(name):

    def real_decorator(f):

        @wraps(f)
        def wrapper(self, *args, **kwargs):
            arbiter = None
            if hasattr(self, 'arbiter'):
                arbiter = self.arbiter
            elif hasattr(self, '_exclusive_running_command'):
                arbiter = self
            if arbiter is not None:
                if arbiter._restarting:
                    raise ConflictError('Arbiter is restarting...')
                if arbiter._exclusive_running_command is not None:
                    raise ConflictError('Arbiter is already running {} command'
                                        .format(
                                            arbiter._exclusive_running_command))
                arbiter._exclusive_running_command = name
            resp = None
            try:
                resp = f(self, *args, **kwargs)
            finally:
                if isinstance(resp, concurrent.Future):
                    cb = functools.partial(_synchronized_cb, arbiter)
                    resp.add_done_callback(cb)
                else:
                    if arbiter is not None:
                        arbiter._exclusive_running_command = None
            return resp

        return wrapper

    return real_decorator


class TransformableFuture(concurrent.Future):

    _upstream_future = None
    _upstream_callback = None
    _result = None
    _exception = None

    def _transform_function(x):
        return x

    def set_transform_function(self, fn):
        self._transform_function = fn

    def set_upstream_future(self, upstream_future):
        self._upstream_future = upstream_future

    def result(self, timeout=None):
        if self._upstream_future is None:
            raise Exception('upstream_future is not set')
        return self._transform_function(self._result)

    def _internal_callback(self, future):
        self._result = future.result()
        self._exception = future.exception()
        if self._upstream_callback is not None:
            self._upstream_callback(self)

    def add_done_callback(self, fn):
        if self._upstream_future is None:
            raise Exception('upstream_future is not set')
        self._upstream_callback = fn
        self._upstream_future.add_done_callback(self._internal_callback)

    def exception(self, timeout=None):
        if self._exception:
            return self._exception
        else:
            return None


def check_future_exception_and_log(future):
    from circle import logger
    if isinstance(future, concurrent.Future):
        exception = future.exception()
        if exception is not None:
            logger.error('exception {} caught'.format(exception))
            if hasattr(future, 'exc_info'):
                exc_info = future.exc_info()
                traceback.print_tb(exc_info[2])
            return exception


def check_env(keys):
    for key in keys:
        if not os.environ.get(key):
            return False
    return True


def get_run_env():
    key_dict = {
        'id': 'ANT_AGENT_ID',
        'ip': 'ANT_AGENT_IP',
        'network_domain': 'ANT_NETWORK_DOMAIN',
        'upstream': 'ANT_UPSTREAM'
    }
    if check_env(key_dict.values()):
        return

    conf_path = os.path.join(os.environ.get('ANT_ROOT_DIR'), 'config.yaml')
    if os.path.exists(conf_path):
        with open(conf_path) as f:
            import yaml
            conf_dict = yaml.load(f.read()) or {}

            for key, value in key_dict.items():
                if key in conf_dict and conf_dict[key]:
                    os.environ.setdefault(value, conf_dict[key])
