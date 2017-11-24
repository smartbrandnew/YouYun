from mock import patch, MagicMock
from tornado.testing import AsyncTestCase, gen_test
from tornado.concurrent import Future
from framework.message.models import Message
from framework.message.type import MessageType
from framework.message.handlers.task import NewTaskHandler, running_tasks
from framework.message.transfers import mock_transfer
from framework.task.type import TaskType


class NewTaskHandlerTest(AsyncTestCase):

    @gen_test
    def test_handle_action_task(self):
        with patch('framework.message.handlers.task.ActionTask') as mock:
            mocked_transfer = MagicMock()
            future = Future()
            future.set_result(True)
            instance = mock.return_value
            mocked_transfer.send.return_value = future
            test_new_task_message = Message.create({
                'id': 'test_msg_id',
                'type': MessageType.NEW_TASK,
                'body': {
                    'task_id': 1,
                    'type': TaskType.ACTION,
                    'action': 'test_action',
                    'args': {
                        'a': 1,
                        'b': 2
                    }
                }
            })
            with mock_transfer(mocked_transfer):
                handler = NewTaskHandler()
                yield handler.handle(test_new_task_message)
            mock.assert_called_once_with(
                task_id=1, action='test_action', args={'a': 1,
                                                       'b': 2})
            self.assertEqual(mocked_transfer.send.call_count, 1)
            test_new_task_message = mocked_transfer.send.call_args[0][0]
            self.assertEqual(test_new_task_message.type, MessageType.TASK_START)
            self.assertEqual(test_new_task_message.body['task_id'], 1)
            mocked_transfer.send.assert_called_once_with(test_new_task_message)
            self.assertEqual(instance.start.call_count, 1)
            self.assertEqual(len(running_tasks), 1)

    @gen_test
    def test_handle_python_task(self):
        with patch('framework.message.handlers.task.PythonTask') as mock:
            mocked_transfer = MagicMock()
            future = Future()
            future.set_result(True)
            instance = mock.return_value
            mocked_transfer.send.return_value = future
            test_new_task_message = Message.create({
                'id': 'test_msg_id',
                'type': MessageType.NEW_TASK,
                'body': {
                    'task_id': 1,
                    'type': TaskType.PYTHON,
                    'script': 'test_script'
                }
            })
            with mock_transfer(mocked_transfer):
                handler = NewTaskHandler()
                yield handler.handle(test_new_task_message)
            mock.assert_called_once_with(task_id=1, stream='test_script')
            self.assertEqual(mocked_transfer.send.call_count, 1)
            test_new_task_message = mocked_transfer.send.call_args[0][0]
            self.assertEqual(test_new_task_message.type, MessageType.TASK_START)
            self.assertEqual(test_new_task_message.body['task_id'], 1)
            mocked_transfer.send.assert_called_once_with(test_new_task_message)
            self.assertEqual(instance.start.call_count, 1)
            self.assertEqual(len(running_tasks), 1)

    @gen_test
    def test_handle_command_task(self):
        with patch('framework.message.handlers.task.CommandTask') as mock:
            mocked_transfer = MagicMock()
            future = Future()
            future.set_result(True)
            instance = mock.return_value
            mocked_transfer.send.return_value = future
            test_new_task_message = Message.create({
                'id': 'test_msg_id',
                'type': MessageType.NEW_TASK,
                'body': {
                    'task_id': 1,
                    'type': TaskType.COMMAND,
                    'shell_args': ['python', '-m', 'this'],
                    'shell': True
                }
            })
            with mock_transfer(mocked_transfer):
                handler = NewTaskHandler()
                yield handler.handle(test_new_task_message)
            mock.assert_called_once_with(
                task_id=1, args=['python', '-m', 'this'], shell=True)
            self.assertEqual(mocked_transfer.send.call_count, 1)
            test_new_task_message = mocked_transfer.send.call_args[0][0]
            self.assertEqual(test_new_task_message.type, MessageType.TASK_START)
            self.assertEqual(test_new_task_message.body['task_id'], 1)
            mocked_transfer.send.assert_called_once_with(test_new_task_message)
            self.assertEqual(instance.start.call_count, 1)
            self.assertEqual(len(running_tasks), 1)
