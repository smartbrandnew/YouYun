# coding: utf-8
import os
import re
import sys
from subprocess import CalledProcessError

import nfs
import yaml
import pf
from constants import FULLNAME_SEP
from framework.actions import module_logger
from framework.actions.constants import CIRCLE_CONF_DIR
from framework.actions.module.decorator import LazyProperty
from framework.actions.errors import MessageError, NotExistsError
from framework.actions.utils import execute


def deal_cmd(cmd):
    if isinstance(cmd, dict):
        platform = pf.get_platform()
        if platform.dist in cmd:
            cmd = cmd.get(platform.dist)
        elif platform.system in cmd:
            cmd = cmd.get(platform.system)
        else:
            return None
    return cmd


class Config(object):
    """Manage module.yml config"""

    def __init__(self, config_path):
        self.config_path = config_path

    @LazyProperty
    def content(self):
        if not os.path.exists(self.config_path):
            raise NotExistsError(self.config_path)

        with open(self.config_path) as f:
            content = yaml.load(f)

        env = content.get('env')
        if env:
            current_dir = os.path.dirname(self.config_path)
            local_env = dict(os.environ.items())
            local_env.update(env)
            for key, value in env.iteritems():
                if value == '.':
                    value = current_dir
                if value.startswith('./') or \
                        value.startswith('.\\'):
                    value = os.path.normpath(os.path.join(current_dir, value))
                env[key] = value
        return content

    def exec_lifecycle_script(self, cmd_name, cwd=None, wait=True, env=None):
        if not cwd:
            cwd = os.path.dirname(self.config_path)

        scripts = self.content.get('scripts')
        if not scripts\
                or cmd_name not in scripts \
                or not scripts.get(cmd_name):
            return

        cmds = scripts[cmd_name]

        cmds = deal_cmd(cmds)
        if not cmds:
            raise MessageError('{} is not support for this platform !'.format(
                cmd_name))

        if isinstance(cmds, str) and cmds.strip():
            cmds = [cmds.strip()]
        elif not isinstance(cmds, list):
            raise MessageError('scirpts:{} must be str or list'.format(
                cmd_name))
        cmd = ''
        try:
            for cmd in cmds:
                executable = cmd.split()[0]
                sys_executable = '"{}"'.format(sys.executable) \
                    if ' ' in sys.executable else sys.executable
                if executable.lower().strip() == 'python':
                    cmd = cmd.replace(executable, sys_executable, 1)
                module_logger.info('Executing lifecycle {}'.format(cmd_name))
                module_logger.info('Lifecycle cmd: {}, cwd: {}'.format(cmd,
                                                                       cwd))
                status, output = execute(cmd, cwd, wait=wait, env=env)
                module_logger.info(
                    "Command '%s' returned exit status %d, the output is: %s" %
                    (cmd, status, output))
                if status != 0:
                    raise MessageError(
                        "Command '%s' returned non-zero exit status %d, "
                        "the output is: %s" % (cmd, status, output))
        except CalledProcessError as e:
            raise MessageError(e)
        except Exception as e:
            raise MessageError('When executing "{0}": "{1}", meet an error:\n'
                               '{2}'.format(cmd_name, cmd, str(e)))


class CircledConfig(object):

    def __init__(self, fullname):
        self.fullname = fullname
        self._INI_NAME = 'Ini of {}'.format(self.fullname)
        self.able_path = os.path.join(CIRCLE_CONF_DIR, fullname + '.ini')
        self.disable_path = os.path.join(CIRCLE_CONF_DIR,
                                         fullname + '.ini.disable')

    @LazyProperty
    def path(self):
        if os.path.exists(self.able_path):
            return self.able_path
        if os.path.exists(self.disable_path):
            return self.disable_path
        raise NotExistsError(self._INI_NAME)

    def get_hooks(self, scripts):
        if not scripts:
            return None

        cmds_names = ['post_stop', 'post_start']
        result = {}
        for cmd_name in cmds_names:
            hooks = scripts.get(cmd_name)
            if not hooks:
                continue
            hooks = deal_cmd(hooks)
            if not hooks:
                raise MessageError(
                    '{} is not support for this platform !'.format(cmd_name))

            if isinstance(hooks, list):
                raise MessageError('scirpts:{} must be str'.format(cmd_name))
            result[cmd_name] = hooks
        return result

    def generate_file(self, main_cmds, env, scripts):
        if not main_cmds:
            return

        if not os.path.exists(CIRCLE_CONF_DIR):
            os.makedirs(CIRCLE_CONF_DIR)

        main_cmds = deal_cmd(main_cmds)
        if not main_cmds:
            raise MessageError('Run cmd is not support for this platform !')

        main_cmds = [cmd for cmd in main_cmds if cmd.strip()] \
            if isinstance(main_cmds, list) else [main_cmds]
        watcher_names = [
            '{}{}{}'.format(self.fullname, FULLNAME_SEP, i + 1)
            for i in range(len(main_cmds))
        ]

        module_logger.debug('Write config to {}'.format(self.able_path))
        hooks = self.get_hooks(scripts)
        with open(self.able_path, 'w') as config_file:
            for i, cmd in enumerate(main_cmds):
                if re.match(r'^python', cmd):
                    cmd = re.sub(r'^python',
                                 sys.executable.replace('\\', '\\\\'), cmd)

                config_file.write('[watcher:{}]\n'
                                  'cmd={}\n'
                                  'numprocess=1\n'
                                  'stop_children=True\n\n'
                                  .format(watcher_names[i], cmd))
                config_file.write('[env:{}]\n'.format(watcher_names[i]))

                if not env:
                    return

                for key, value in env.iteritems():
                    config_file.write('{}={}\n'.format(key, value))
                config_file.write('\n')

                if hooks:
                    config_file.write('[hooks:{}]\n'.format(watcher_names[i]))
                    for key, value in hooks.iteritems():
                        config_file.write('{}={}\n'.format(key, value))
                    config_file.write('\n')

    def remove_file(self):
        if os.path.exists(self.able_path):
            nfs.remove(self.able_path)
