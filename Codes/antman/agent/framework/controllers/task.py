# coding: utf-8
import logging
from tornado import web, gen, escape
from framework.message import cache
from framework.message.models.task import TaskLogMessage, TaskResultMessage
from framework.message.transfers import get_current_transfer
from framework.message.handlers import task

logger = logging.getLogger('default')


class TaskLogHandler(web.RequestHandler):

    def initialize(self, **kwargs):
        self._transfer = kwargs.get('transfer') or get_current_transfer()

    @gen.coroutine
    def post(self):
        task_id = int(self.get_argument('task_id'))
        seq = int(self.get_argument('seq', 0))
        log = self.request.body or ''
        logger.debug('Task id: {}, seq: {}, log: {}'.format(task_id, seq,
                                                            log.rstrip('\n')))
        message = TaskLogMessage(task_id=task_id, log=log, seq=seq)
        result = yield self._transfer.send(message)
        if not result:
            yield cache.put_message(message)
        self.write(escape.json_encode({'ok': True}))


class TaskResultHandler(web.RequestHandler):

    def initialize(self, **kwargs):
        self._transfer = kwargs.get('transfer') or get_current_transfer()

    @gen.coroutine
    def post(self):
        task_id = int(self.get_argument('task_id'))
        seq = int(self.get_argument('seq', 0))
        exit_code = int(self.get_argument('exit_code', 0))
        result = self.request.body or ''
        logger.debug('Task id: {}, seq: {}, exit_code: {}, result: {}'.format(
            task_id, seq, exit_code, result.rstrip('\n')))
        message = TaskResultMessage(
            task_id=task_id, result=result, seq=seq, exit_code=exit_code)
        result = yield self._transfer.send(message)
        if not result:
            yield cache.put_message(message)
        task.running_tasks.pop(task_id, None)  # 从正在运行任务队列中移出
        self.write(escape.json_encode({'ok': True}))
