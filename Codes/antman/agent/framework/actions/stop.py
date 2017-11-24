# coding: utf-8
import os
import sys
from tornado import gen, ioloop
from subprocess import check_call

from circle.winservice import CircleWinService
from framework.actions import logger
from framework.actions.constants import IS_WINDOWS, BIN_STOP
from framework.actions.reporter import Reporter


class Stop(object):

    def __init__(self):
        self.io_loop = ioloop.IOLoop()

    def _prepare(self):
        self.task_id = os.environ.get('ANT_TASK_ID')
        if not self.task_id:
            print('The task_id is None, '
                  'please input the right ANT_TASK_ID with env!')
            sys.exit(1)
        self.reporter = Reporter(self.task_id, logger)

    @gen.coroutine
    def execute(self):
        try:
            yield self.reporter.log_ok('Stoping Agent ...')
            yield self.reporter.log_ok('Finish stop agent!', True)
            self.stop_agent()
        except Exception as e:
            yield self.reporter.log_error(str(e), True)
        self.io_loop.stop()

    def run(self):
        self._prepare()
        self.io_loop.spawn_callback(self.execute)
        self.io_loop.start()

    @staticmethod
    def stop_agent():
        if IS_WINDOWS:
            if CircleWinService.status() != 'stopped':
                CircleWinService.stop()
        else:
            check_call([BIN_STOP], shell=True)


if __name__ == '__main__':
    stop = Stop()
    stop.run()
