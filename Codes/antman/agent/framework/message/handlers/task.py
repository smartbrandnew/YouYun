from tornado import gen
from framework.message.models import Message
from framework.message import cache
from framework.message.type import MessageType
from framework.message.handlers.base import BaseMessageHandler
from framework.message.transfers import get_current_transfer
from framework.task.type import TaskType
from framework.task.action import ActionTask
from framework.task.python import PythonTask
from framework.task.command import CommandTask

running_tasks = {}


class NewTaskHandler(BaseMessageHandler):

    def __init__(self):
        self._transfer = get_current_transfer()

    @gen.coroutine
    def handle(self, msg):
        body = msg.body
        task_id = body.get('task_id')
        type = body.get('type', TaskType.ACTION)
        if type == TaskType.ACTION:
            task = ActionTask(
                task_id=task_id, action=body['action'], args=body.get('args'))
        elif type == TaskType.PYTHON:
            task = PythonTask(task_id=task_id, stream=body['script'])
        elif type == TaskType.COMMAND:
            task = CommandTask(
                task_id=task_id,
                args=body['shell_args'],
                shell=body.get('shell', True))
        else:
            raise TypeError('Invalid task type: ' + str(type))
        task.start()
        running_tasks[task_id] = task
        message = Message.create(MessageType.TASK_START, {'task_id': task_id})
        result = yield self._transfer.send(message)
        if not result:
            yield cache.put_message(message)


class KillTaskHandler(BaseMessageHandler):

    def __init__(self):
        self._transfer = get_current_transfer()

    @gen.coroutine
    def handle(self, msg):
        body = msg.body
        task_id = body.get('task_id')

        task = running_tasks.get(task_id, None)
        if task:
            task.stop()
