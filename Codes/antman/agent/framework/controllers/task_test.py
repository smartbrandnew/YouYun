from tornado.web import Application
from tornado import escape, gen
from tornado.testing import AsyncHTTPTestCase
from framework.ioloop import get_io_loop
from framework.controllers.task import TaskLogHandler, TaskResultHandler
from framework.message.type import MessageType
from framework.message.handlers import task


class TaskTest(AsyncHTTPTestCase):

    def setUp(self):

        class MockedTransfer(object):

            @gen.coroutine
            def send(_, message):
                self._message = message
                raise gen.Return(True)

        self._transfer = MockedTransfer()

        super(TaskTest, self).setUp()

    def get_new_ioloop(self):
        return get_io_loop()

    def get_app(self):
        return Application([(r'/task/log', TaskLogHandler, {
            'transfer': self._transfer
        }), (r'/task/result', TaskResultHandler, {
            'transfer': self._transfer
        })])

    def test_task_log(self):
        path = '/task/log?task_id=1&seq=1'
        task.running_tasks[1] = 'mock_task_ins'
        response = self.fetch(path, method='POST', body='test_task_log_content')
        self.assertEqual(self._message.type, MessageType.TASK_LOG)
        self.assertEqual(self._message.body, {
            'task_id': 1,
            'log': 'test_task_log_content',
            'seq': 1
        })
        data = escape.json_decode(response.body)
        self.assertTrue(data['ok'])

    def test_task_result(self):
        path = '/task/result?task_id=1'
        task.running_tasks[1] = 'mock_task_ins'
        response = self.fetch(path, method='POST', body='Hello, world!')
        data = escape.json_decode(response.body)
        self.assertTrue(data['ok'])
        self.assertEqual(self._message.type, MessageType.TASK_RESULT)
        self.assertEqual(self._message.body, {
            'task_id': 1,
            'result': 'Hello, world!',
            'seq': 0,
            'exit_code': 0,
            'is_timeout': False,
            'is_aborted': False
        })
        self.assertFalse(1 in task.running_tasks)
