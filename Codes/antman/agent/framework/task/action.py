# coding: utf-8
import nfs
import yaml
import json
import subprocess
import logging

from framework import settings
from framework.task.base import BaseTask
from framework import utils
from framework.settings import IS_WINDOWS

logger = logging.getLogger('default')


class ActionTask(BaseTask):

    def __init__(self, task_id, action, args):
        super(ActionTask, self).__init__(task_id)
        self.action = action
        self.args = args

    def _load_command_and_env(self, action):
        if action.startswith('core.'):
            self.module_name = 'core'
            command = settings.CORE_ACTIONS.get(action)
        else:
            self.module_name, _ = action.split('.')
            module_path = nfs.join(settings.MODULES_DIR, self.module_name)
            self.cwd = module_path
            yaml_file = nfs.join(module_path, 'manifest.yaml')

            if not nfs.exists(yaml_file):
                return None

            with open(yaml_file) as f:
                data = yaml.load(f)

            command = data['actions'].get(action)
            env = utils.normalize_env(
                data.get('env', {}), relpath_prefix=module_path)

            logger.debug('Action task {!r} env: {}'.format(self.action, env))

            # 处理PATH开头的环境变量，加入到PATH环境变量中
            paths = [self.env.get('PATH', '')]
            for name, value in env.iteritems():
                if isinstance(value, int):
                    value = str(value)
                else:
                    value = value.encode('utf-8')

                if name.startswith('PATH'):
                    paths.append(value)
                else:
                    self.env[name] = value
            self.env['PATH'] = ';'.join(paths) if IS_WINDOWS else \
                ':'.join(paths)
            logger.debug('Action task {!r} env PATH: {}'.format(
                self.action, self.env['PATH']))

        command = utils.normalize_cmdline(command)

        return command

    def start(self):
        logger.info('Executing action task <{}>: {}'.format(self.task_id,
                                                            self.action))
        logger.debug('Action task: {}, args: {}'.format(self.action, self.args))

        cmd = self._load_command_and_env(self.action)
        logger.debug('Action task: {}, command: {}'.format(self.action, cmd))

        if not cmd:
            self.exc_info = 'Action: {} not found'.format(self.action)
            return

        if isinstance(cmd, str):
            shell = True
        elif isinstance(cmd, (list, tuple)):
            shell = False
        else:
            raise TypeError('Invalid args type: ' + str(cmd))

        self._proc = subprocess.Popen(
            args=cmd,
            shell=shell,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            env=self.env,
            cwd=self.cwd)

        if self.args:
            self._proc.stdin.write(json.dumps(self.args))
            self._proc.stdin.close()
