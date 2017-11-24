# coding: utf-8

import os
from tornado import gen
from framework.task.logger import TaskLogger, EngineType


class Reporter(object):

    def __init__(self, task_id, logger, tenant=None, asynchronous=True):
        self.logger = logger
        self.task_id = task_id
        if asynchronous:
            self.log_ok = self._async_log_ok
            self.log_error = self._async_log_error
            self.engine = EngineType.TORNADO
        else:
            self.log_ok = self._log_ok
            self.log_error = self._log_error
            self.engine = EngineType.REQUESTS
        self._init_task_logger(task_id, tenant)

    def _init_task_logger(self, task_id, tenant):
        if os.environ.get('ANT_TASK_URL'):
            self.task_logger = TaskLogger(
                task_id=task_id,
                engine=self.engine,
                task_url=os.environ.get('ANT_TASK_URL'),
                wrap=True,
                tenant=tenant)
        else:
            self.task_logger = TaskLogger(
                task_id=task_id, engine=self.engine, tenant=tenant)

    def emit_log(self, msg, done, exit_code):
        if self.task_id == -1:
            return
        try:
            if done:
                self.task_logger.result(msg, exit_code)
            else:
                self.task_logger.log(msg)
        except Exception as e:
            self.logger.error('Send task log error: {}'.format(e))

    def _log_ok(self, msg, done=False, exit_code=0):
        self.logger.info(msg)
        self.emit_log(msg, done, exit_code)

    def _log_error(self, msg, done=False, exit_code=-1):
        self.logger.error(msg)
        self.emit_log(msg, done, exit_code)

    @gen.coroutine
    def emit_msg(self, msg, done, exit_code):
        if self.task_id == -1:
            raise gen.Return()
        try:
            if done:
                yield self.task_logger.result(msg, exit_code)
            else:
                yield self.task_logger.log(msg)
        except Exception as e:
            self.logger.error('Send task log error: {}'.format(e))

    @gen.coroutine
    def _async_log_ok(self, msg, done=False, exit_code=0):
        self.logger.info(msg)
        yield self.emit_msg(msg, done, exit_code)

    @gen.coroutine
    def _async_log_error(self, msg, done=False, exit_code=-1):
        self.logger.error(msg)
        yield self.emit_msg(msg, done, exit_code)
