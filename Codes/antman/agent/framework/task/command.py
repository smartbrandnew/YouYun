# coding: utf-8
import subprocess
import logging

from framework.task.base import BaseTask

logger = logging.getLogger('default')


class CommandTask(BaseTask):

    def __init__(self, task_id, args, shell):
        super(CommandTask, self).__init__(task_id)
        self.args = args
        self.shell = shell

    def start(self):
        logger.info('Executing command task <{}>, shell is {}'.format(
            self.task_id, self.shell))
        logger.debug('Command task command: {}'.format(self.args))

        self._proc = subprocess.Popen(
            args=self.args,
            shell=self.shell,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            env=self.env,
            cwd=self.cwd)
