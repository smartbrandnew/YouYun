# coding: utf-8
import sys
import json
import subprocess
import logging

from framework.task.base import BaseTask
from framework.ioloop import get_io_loop

logger = logging.getLogger('default')


class PythonTask(BaseTask):

    def __init__(self, task_id, stream):
        super(PythonTask, self).__init__(task_id)
        if isinstance(stream, unicode):
            self.content = stream.encode('utf-8')
        elif isinstance(stream, str):
            self.content = stream
        else:
            self.content = stream.read()

        self._io_loop = get_io_loop()

    def start(self):
        logger.info('Executing python task <{}>'.format(self.task_id))
        logger.debug('Python task content:\n {}'.format(self.content))

        self._proc = subprocess.Popen(
            args=sys.executable,
            shell=True,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            env=self.env,
            cwd=self.cwd)

        if self.content:
            self._proc.stdin.write(json.dumps(self.content))
            self._proc.stdin.close()
