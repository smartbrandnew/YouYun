# coding: utf-8
import os
import logging
import psutil

from framework.settings import ROOT_DIR
from framework.config import config

logger = logging.getLogger('default')


class BaseTask(object):

    def __init__(self, task_id):
        self.task_id = task_id
        self.cwd = os.getcwd()
        self.module_name = None
        self._is_aborted = False
        self._proc = None
        self._exc_info = None
        self.env = {}
        self.env.update(os.environ)
        self.env.update({
            'ANT_TASK_ID': str(task_id),
            'ANT_AGENT_ID': config['id'],
            'PYTHONPATH': ROOT_DIR
        })

    def start(self):
        raise NotImplementedError()

    def stop(self):
        if not self._proc:
            return

        logger.info('Killing task <{}>'.format(self.task_id))
        try:
            proc = psutil.Process(self._proc.pid)
            for child in reversed(proc.children(recursive=True)):
                try:
                    child.kill()
                except Exception:
                    logger.error(
                        'Kill child process <{}> of task <{}, {}>  error'
                        .format(child.pid, self.task_id, self.module_name),
                        exc_info=True)
            proc.kill()
            self._is_aborted = True
        except Exception:
            logger.error(
                'Kill process <{}> of task <{}, {}>  error'
                .format(self._proc.pid, self.task_id, self.module_name),
                exc_info=True)
        else:
            logger.info('Kill task <{}, {}> done'.format(self.task_id,
                                                         self.module_name))

    def is_stopped(self):
        return self._proc and self._proc.poll() is not None

    @property
    def exit_code(self):
        if self._proc:
            return self._proc.poll()
        return int(bool(self.exc_info))

    @property
    def exc_info(self):
        if self._exc_info is None and self.is_stopped() and self.exit_code != 0:
            self._exc_info = self._proc.stdout.read()  # FIXME: Windows可能会卡住

        return self._exc_info

    @exc_info.setter
    def exc_info(self, value):
        self._exc_info = value

    @property
    def is_aborted(self):
        return self._is_aborted
