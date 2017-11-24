# coding: utf-8

import sys
import os
import subprocess

import nfs as fs
from tornado import gen, ioloop

from framework.actions import logger
from framework.actions.reporter import Reporter
from framework.actions.constants import IS_WINDOWS, ROOT_DIR


class Remove(object):

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
        if IS_WINDOWS:
            remove_script = fs.join(ROOT_DIR, 'bin', 'remove.bat')
            subprocess.Popen(
                [remove_script, '-f'],
                close_fds=True,
                shell=True,
                creationflags=subprocess.CREATE_NEW_CONSOLE)
        else:
            remove_script = fs.join(ROOT_DIR, 'bin', 'remove.sh')
            subprocess.Popen(
                '/usr/bin/env bash {} -f &'.format(remove_script), shell=True)
        yield self.reporter.log_ok('Remove agent success!', done=True)
        self.io_loop.stop()

    def run(self):
        self._prepare()
        self.io_loop.spawn_callback(self.execute)
        self.io_loop.start()


if __name__ == '__main__':
    remove = Remove()
    remove.run()
