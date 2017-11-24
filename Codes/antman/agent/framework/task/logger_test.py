import json
import httpretty
import traceback

from unittest import TestCase
from urlparse import urljoin
from tornado import web
from tornado.locks import Condition
from tornado.testing import AsyncHTTPTestCase, gen_test
from framework.task.logger import TaskLogger, EngineType


class TaskLoggerRequestsTest(TestCase):

    def setUp(self):
        super(TaskLoggerRequestsTest, self).setUp()

    @httpretty.activate
    def _test_requests(self):
        task_url = 'http://127.0.0.1:16600/task/'

        def request_callback(request, uri, headers):
            payload = json.loads(request.body)
            if uri == urljoin(task_url, 'log?seq=1&task_id=1'):
                assert payload['messages']['body'] == {
                    'log': 'a\n',
                    'task_id': 1,
                    'seq': 1
                }
            elif uri == urljoin(task_url, 'result?seq=2&task_id=1&exit_code=0'):
                assert payload['messages']['body'] == {
                    'exit_code': 0,
                    'is_aborted': False,
                    'is_timeout': False,
                    'result': 'b\n',
                    'task_id': 1,
                    'seq': 2,
                }
            else:
                assert False

            return (200, headers, '')

        httpretty.register_uri(
            httpretty.POST, urljoin(task_url, 'log'), body=request_callback)
        httpretty.register_uri(
            httpretty.POST, urljoin(task_url, 'result'), body=request_callback)

        task_logger = TaskLogger(
            task_id=1, engine=EngineType.REQUESTS, task_url=task_url, wrap=True)
        task_logger.log(log='a')
        task_logger.result(result='b')
        task_logger.log(log='a')


class TaskLoggerTornadoTest(AsyncHTTPTestCase):

    def setUp(self):
        self.condition = Condition()
        self.log_str = 'aaaaaa' * 100000
        self.exc = None
        super(TaskLoggerTornadoTest, self).setUp()

    def get_app(self):
        receives = []

        class LogTestHandler(web.RequestHandler):

            def post(hdlr):
                try:
                    self.assertEqual(hdlr.request.query_arguments['task_id'],
                                     ['1'])
                    self.assertIn(hdlr.request.query_arguments['seq'],
                                  [['1'], ['2']])
                    payload = json.loads(hdlr.request.body)
                    seq = payload['messages']['body'].pop('seq')
                    self.assertIn(seq, [1, 2])
                    self.assertEqual(payload['messages']['body'], {
                        'log': self.log_str + '\n',
                        'task_id': 1,
                    })
                    hdlr.write('ok')
                    receives.append('a')
                except Exception as exc:
                    self.exc = exc

        class ResultTestHandler(web.RequestHandler):

            def post(hdlr):
                try:
                    self.assertEqual(hdlr.request.headers['Content-Type'],
                                     'application/json')
                    self.assertEqual(hdlr.request.query_arguments, {
                        'task_id': ['1'],
                        'seq': ['3'],
                        'exit_code': ['0']
                    })
                    payload = json.loads(hdlr.request.body)
                    self.assertEqual(payload['messages']['body'], {
                        'exit_code': 0,
                        'is_aborted': False,
                        'is_timeout': False,
                        'result': 'b\n',
                        'task_id': 1,
                        'seq': 3
                    })
                    hdlr.write('ok')
                    receives.append('b')
                    self.assertEqual(receives, ['a', 'a', 'b'])
                except Exception as exc:
                    self.exc = exc
                    print(traceback.format_exc())
                finally:
                    self.condition.notify()

        return web.Application([(r'/log', LogTestHandler),
                                (r'/result', ResultTestHandler)])

    @gen_test
    def test_tornado(self):
        task_logger = TaskLogger(
            task_id=1,
            engine=EngineType.TORNADO,
            task_url=self.get_url('/'),
            wrap=True)
        self.io_loop.spawn_callback(task_logger.log, self.log_str)
        self.io_loop.spawn_callback(task_logger.log, self.log_str)
        self.io_loop.spawn_callback(task_logger.result, 'b')
        yield self.condition.wait()
        if self.exc:
            raise self.exc
