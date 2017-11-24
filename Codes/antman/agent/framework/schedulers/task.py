from tornado import gen

from framework.config import config
from framework.message.models import TaskResultMessage
from framework.message import cache
from framework.message.transfers import get_current_transfer
from framework.message.handlers import task
from framework.schedulers.base import BaseScheduler

import logging

logger = logging.getLogger('default')


class TaskSyncScheduler(BaseScheduler):
    DO_NOT_RESULT = ('core.upgrade',)

    def __init__(self):
        self._transfer = get_current_transfer()

    @gen.coroutine
    def eventloop(self):
        while True:
            task_id = None
            task_ins = None
            try:
                for task_id, task_ins in task.running_tasks.items():
                    if task_ins.is_stopped():
                        result = ''
                        if task_ins.exit_code != 0 and task_ins.exc_info:
                            result = task_ins.exc_info
                            logger.error('Task <%d> exited by code %d: %s',
                                         task_id, task_ins.exit_code, result)
                        if (task_ins.action not in
                                TaskSyncScheduler.DO_NOT_RESULT):
                            message = TaskResultMessage(
                                task_id=task_id,
                                result=result,
                                exit_code=task_ins.exit_code,
                                seq=-1,
                                is_aborted=task_ins.is_aborted)
                            result = yield self._transfer.send(message)
                            if not result:
                                yield cache.put_message(message)
                        task.running_tasks.pop(task_id, None)
            except Exception as exc:
                logger.error('Error while syncing tasks: %s', exc)
                logger.info('Current task id: %s', task_id)
                logger.info('Current task instance: %s', task_ins)
            yield gen.sleep(config.get('task_sync_interval', 5))
