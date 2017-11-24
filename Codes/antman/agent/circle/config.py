import os
import signal
from fnmatch import fnmatch
from glob import glob

from configparser import (ConfigParser, MissingSectionHeaderError, ParsingError,
                          DEFAULTSECT)

from circle.constants import DEFAULT_ENDPOINT_DEALER
from circle.util import (sort_by_field, replace_gnu_args, to_signum, to_bool)


def watcher_defaults():
    return {
        'name': '',
        'cmd': '',
        'args': '',
        'numprocesses': 1,
        'warmup_delay': 0,
        'executable': None,
        'working_dir': None,
        'shell': False,
        'uid': None,
        'gid': None,
        'send_hup': False,
        'stop_signal': signal.SIGTERM,
        'stop_children': False,
        'max_retry': 5,
        'graceful_timeout': 3,
        'rlimits': dict(),
        'stderr_stream': dict(),
        'stdout_stream': dict(),
        'priority': 0,
        'use_sockets': False,
        'singleton': False,
        'copy_env': False,
        'copy_path': False,
        'respawn': True,
        'autostart': True
    }


class StrictConfigParser(ConfigParser):

    def _read(self, fp, fpname):
        cursect = None  # None, or a dictionary
        optname = None
        lineno = 0
        e = None  # None, or an exception
        while True:
            line = fp.readline()
            if not line:
                break
            lineno += 1
            # comment or blank line?
            if line.strip() == '' or line[0] in '#;':
                continue
            if line.split(None, 1)[0].lower() == 'rem' and line[0] in "rR":
                # no leading whitespace
                continue
            # continuation line?
            if line[0].isspace() and cursect is not None and optname:
                value = line.strip()
                if value:
                    cursect[optname].append(value)
            # a section header or option header?
            else:
                # is it a section header?
                mo = self.SECTCRE.match(line)
                if mo:
                    sectname = mo.group('header')
                    if sectname in self._sections:
                        # we're extending/overriding, we're good
                        cursect = self._sections[sectname]
                    elif sectname == DEFAULTSECT:
                        cursect = self._defaults
                    else:
                        cursect = self._dict()
                        cursect['__name__'] = sectname
                        self._sections[sectname] = cursect
                    # So sections can't start with a continuation line
                    optname = None
                # no section header in the file?
                elif cursect is None:
                    raise MissingSectionHeaderError(fpname, lineno, line)
                # an option line?
                else:
                    mo = self._optcre.match(line)
                    if mo:
                        optname, vi, optval = mo.group('option', 'vi', 'value')
                        self.optionxform = unicode
                        optname = self.optionxform(optname.rstrip())
                        # We don't want to override.
                        if optname in cursect:
                            continue
                        # This check is fine because the OPTCRE cannot
                        # match if it would set optval to None
                        if optval is not None:
                            if vi in ('=', ':') and ';' in optval:
                                # ';' is a comment delimiter only if it follows
                                # a spacing character
                                pos = optval.find(';')
                                if pos != -1 and optval[pos - 1].isspace():
                                    optval = optval[:pos]
                            optval = optval.strip()
                            # allow empty values
                            if optval == '""':
                                optval = ''
                            cursect[optname] = [optval]
                        else:
                            # valueless option handling
                            cursect[optname] = optval
                    else:
                        # a non-fatal parsing error occurred.  set up the
                        # exception but keep going. the exception will be
                        # raised at the end of the file and will contain a
                        # list of all bogus lines
                        if not e:
                            e = ParsingError(fpname)
                        e.append(lineno, repr(line))
        # if any parsing errors occurred, raise an exception
        if e:
            raise e

        # join the multi-line values collected while reading
        all_sections = [self._defaults]
        all_sections.extend(self._sections.values())
        for options in all_sections:
            for name, val in options.items():
                if isinstance(val, list):
                    options[name] = '\n'.join(val)


class DefaultConfigParser(StrictConfigParser):

    def __init__(self, *args, **kw):
        super(DefaultConfigParser, self).__init__(*args, **kw)
        self._env = dict(os.environ)

    def set_env(self, env):
        self._env = dict(env)

    def get(self, section, option, **kwargs):
        res = StrictConfigParser.get(self, section, option, **kwargs)
        return replace_gnu_args(res, env=self._env)

    def items(self, section, noreplace=False, nounicode=False):
        items = StrictConfigParser.items(self, section)

        if not noreplace:
            items = [(key, replace_gnu_args(
                value, env=self._env)) for key, value in items]

        if nounicode:
            for i, (key, value) in enumerate(items):
                if isinstance(key, unicode):
                    key = key.encode('utf-8')
                if isinstance(value, unicode):
                    value = value.encode('utf-8')
                items[i] = (key, value)
        return items

    def dget(self, section, option, default=None, type=str):
        if not self.has_option(section, option):
            return default

        value = self.get(section, option)

        if type is int:
            value = int(value)
        elif type is bool:
            value = to_bool(value)
        elif type is float:
            value = float(value)
        elif type is not str:
            raise NotImplementedError()

        return value


def rlimit_value(val):
    return int(val)


def read_config(config_paths):
    cfg = DefaultConfigParser()
    cfg.read(config_paths)
    return cfg


def get_config(config_path=None):
    if config_path:
        config_path = os.path.realpath(config_path)
        if not os.path.exists(config_path):
            raise IOError("the configuration file %r does not exist\n" %
                          config_path)
        # if given config is dir, we will look for *.ini
        if os.path.isdir(config_path):
            config_paths = glob(os.path.join(config_path, '*.ini'))
        else:
            config_paths = [config_path]
    else:
        config_paths = []

    cfg = read_config(config_paths)
    dget = cfg.dget
    config = {}

    # reading the global environ first
    global_env = dict(os.environ.items())
    local_env = dict()

    # update environments with [env] section
    # if 'env' in cfg.sections():
    #     local_env.update(dict(cfg.items('env', nounicode=True)))
    #     global_env.update(local_env)
    #
    # # always set the cfg environment
    # cfg.set_env(global_env)

    # main circle options
    config['check_delay'] = dget('circle', 'check_delay', 10.0, float)
    config['endpoint'] = dget('circle', 'endpoint', DEFAULT_ENDPOINT_DEALER)
    config['endpoint_owner'] = dget('circle', 'endpoint_owner', None, str)
    config['umask'] = dget('circle', 'umask', None)
    if config['umask']:
        config['umask'] = int(config['umask'], 8)

    config['warmup_delay'] = dget('circle', 'warmup_delay', 0, int)
    config['debug'] = dget('circle', 'debug', False, bool)
    config['pidfile'] = dget('circle', 'pidfile')
    config['loglevel'] = dget('circle', 'loglevel')
    config['logoutput'] = dget('circle', 'logoutput')
    config['loggerconfig'] = dget('circle', 'loggerconfig', None)

    # Initialize watchers to manage
    watchers = []

    for section in cfg.sections():
        section_items = dict(cfg.items(section))
        if list(section_items.keys()) in [[], ['__name__']]:
            # Skip empty sections
            continue
        if section.startswith("watcher:"):
            watcher = watcher_defaults()
            watcher['name'] = section.split("watcher:", 1)[1]

            # create watcher options
            for opt, val in cfg.items(section, noreplace=True):
                if opt in ('cmd', 'args', 'working_dir', 'uid', 'gid'):
                    watcher[opt] = val
                elif opt == 'numprocesses':
                    watcher['numprocesses'] = dget(section, 'numprocesses', 1,
                                                   int)
                elif opt == 'warmup_delay':
                    watcher['warmup_delay'] = dget(section, 'warmup_delay', 0,
                                                   int)
                elif opt == 'executable':
                    watcher['executable'] = dget(section, 'executable', None,
                                                 str)
                # default bool to False
                elif opt in ('shell', 'send_hup', 'stop_children',
                             'close_child_stderr', 'use_sockets', 'singleton',
                             'copy_env', 'copy_path', 'close_child_stdout'):
                    watcher[opt] = dget(section, opt, False, bool)
                elif opt == 'stop_signal':
                    watcher['stop_signal'] = to_signum(val)
                elif opt == 'max_retry':
                    watcher['max_retry'] = dget(section, "max_retry", 5, int)
                elif opt == 'graceful_timeout':
                    watcher['graceful_timeout'] = dget(
                        section, "graceful_timeout", 3, int)
                elif opt.startswith('stderr_stream') or \
                        opt.startswith('stdout_stream'):
                    stream_name, stream_opt = opt.split(".", 1)
                    watcher[stream_name][stream_opt] = val
                elif opt.startswith('rlimit_'):
                    limit = opt[7:]
                    watcher['rlimits'][limit] = rlimit_value(val)
                elif opt == 'priority':
                    watcher['priority'] = dget(section, "priority", 0, int)
                # default bool to True
                elif opt in ('check_flapping', 'respawn', 'autostart',
                             'close_child_stdin'):
                    watcher[opt] = dget(section, opt, True, bool)
                else:
                    # freeform
                    watcher[opt] = val

            env_tag = 'env:{}'.format(watcher['name'])
            if env_tag in cfg.sections():
                local_env = dict(cfg.items(env_tag, nounicode=True))

            if watcher['copy_env']:
                local_env.update(global_env)
                watcher['env'] = dict(local_env)
            else:
                watcher['env'] = dict(local_env)

            hooks_tag = 'hooks:{}'.format(watcher['name'])
            if hooks_tag in cfg.sections():
                local_hooks = dict(cfg.items(hooks_tag, nounicode=True))
                watcher['hooks'] = dict(local_hooks)

            watchers.append(watcher)

    # making sure we return consistent lists
    sort_by_field(watchers)

    # Second pass to make sure env sections apply to all watchers.
    def _extend(target, source):
        for name, value in source:
            if name in target:
                continue
            target[name] = value

    def _expand_vars(target, key, env):
        if isinstance(target[key], str) or isinstance(target[key], basestring):
            target[key] = replace_gnu_args(target[key], env=env)
        elif isinstance(target[key], dict):
            for k in target[key].keys():
                _expand_vars(target[key], k, env)

    def _expand_section(section, env, exclude=None):
        if exclude is None:
            exclude = ('name', 'env')

        for option in section.keys():
            if option in exclude:
                continue
            _expand_vars(section, option, env)

    # build environment for watcher sections
    for section in cfg.sections():
        if section.startswith('env:'):
            section_elements = section.split("env:", 1)[1]
            watcher_patterns = [s.strip() for s in section_elements.split(',')]
            env_items = dict(cfg.items(section, noreplace=True, nounicode=True))

            for pattern in watcher_patterns:
                match = [w for w in watchers if fnmatch(w['name'], pattern)]

                for watcher in match:
                    watcher['env'].update(env_items)

    # expand environment for watcher sections
    for watcher in watchers:
        env = dict(global_env)
        env.update(watcher['env'])
        _expand_section(watcher, env)
    config['watchers'] = watchers
    return config
