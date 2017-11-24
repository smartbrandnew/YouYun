from framework.message.type import MessageType
from framework.message.models.base import Message


class TaskLogMessage(Message):

    def __init__(self, task_id, log, seq=0):
        # type: (int, str, int) -> None
        msg = Message.create(MessageType.TASK_LOG,
                             {'task_id': task_id,
                              'log': log,
                              'seq': seq})
        super(TaskLogMessage, self).__init__(msg.id, msg.type, msg.body)


class TaskResultMessage(Message):

    def __init__(self,
                 task_id,
                 result,
                 seq=0,
                 exit_code=0,
                 is_timeout=False,
                 is_aborted=False):
        # type: (int, str, int, int, bool, bool) -> None
        msg = Message.create(MessageType.TASK_RESULT, {
            'task_id': task_id,
            'result': result,
            'seq': seq,
            'exit_code': exit_code,
            'is_timeout': is_timeout,
            'is_aborted': is_aborted
        })
        super(TaskResultMessage, self).__init__(msg.id, msg.type, msg.body)
