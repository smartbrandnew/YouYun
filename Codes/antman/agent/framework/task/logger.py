# coding: utf-8
import requests
from urlparse import urljoin

from tornado import gen
from tornado.queues import Queue
from tornado.escape import json_encode
from tornado.httpclient import AsyncHTTPClient
from tornado.ioloop import IOLoop
from framework.config import config
from framework.utils import update_query_params
from framework.message.models import (TaskLogMessage, TaskResultMessage)

TASK_URL = 'http://127.0.0.1:{}/task/'.format(
    config.get('framework_port', '16600'))


class EngineType(object):
    __slots__ = ('REQUESTS', 'TORNADO')
    REQUESTS = 1
    TORNADO = 2

    @classmethod
    def types_str(cls):
        return ', '.join('{}.{}'.format(cls.__name__, t) for t in cls.__slots__)


class LogType(object):
    __slots__ = ('LOG', 'RESULT')
    LOG = 1
    RESULT = 2

    @classmethod
    def types_str(cls):
        return ', '.join('{}.{}'.format(cls.__name__, t) for t in cls.__slots__)


class TaskLoggerError(Exception):

    def __init__(self, data, reason):
        super(TaskLoggerError,
              self).__init__('When logging {}, occurs an error. Reason:\n {}'
                             .format(data, reason))


class TaskLogger(object):

    def __init__(self,
                 task_id,
                 engine=EngineType.REQUESTS,
                 io_loop=None,
                 task_url=TASK_URL,
                 wrap=False,
                 tenant=None):
        self.task_id = task_id
        self.task_url = task_url
        self._seq = 0
        self._partial_log_url = self._get_partial_url('log')
        self._partial_result_url = self._get_partial_url('result')

        self.wrap = wrap
        if wrap and tenant:
            self._partial_log_url = update_query_params(self._partial_log_url,
                                                        {'tenant': tenant})
            self._partial_result_url = update_query_params(
                self._partial_result_url, {'tenant': tenant})

        if engine == EngineType.REQUESTS:
            self.log = self._log_by_requests
            self.result = self._result_by_requests
        elif engine == EngineType.TORNADO:
            io_loop = io_loop if io_loop else IOLoop.current()
            self._http_client = AsyncHTTPClient(io_loop=io_loop)
            self._queue = Queue()
            self.log = self._log_by_tornado
            self.result = self._result_by_tornado
        else:
            raise TaskLoggerError(
                '',
                reason='engine only supports {}'.format(EngineType.types_str()))

    def _get_partial_url(self, partial_name):
        url = urljoin(self.task_url, partial_name)
        url = update_query_params(url, {'task_id': self.task_id})
        return url

    def _get_log_url(self, seq):
        url = update_query_params(self._partial_log_url, {'seq': seq})
        return url

    def _get_result_url(self, seq, exit_code=0):
        url = update_query_params(self._partial_result_url,
                                  {'seq': seq,
                                   'exit_code': exit_code})
        return url

    def _log_by_requests(self, log):
        self._seq += 1
        log_url = self._get_log_url(self._seq)
        data = self._create_log(log, self._seq)
        self._send_by_requests(log_url, data)

    def _result_by_requests(self, result, exit_code=0):
        self._seq += 1
        result_url = self._get_result_url(self._seq, exit_code)
        data = self._create_result(result, self._seq, exit_code=exit_code)
        self._send_by_requests(result_url, data)

    @staticmethod
    def _send_by_requests(url, data):
        res = requests.post(url, data=data, verify=False)
        if res.status_code != 200:
            raise TaskLoggerError(data, reason=res.reason)

    @gen.coroutine
    def _log_by_tornado(self, log):
        yield self._queue.put(1)
        self._seq += 1
        log_url = self._get_log_url(self._seq)
        data = self._create_log(log, self._seq)
        try:
            yield self._send_by_tornado(log_url, data)
        finally:
            yield self._queue.get()
            self._queue.task_done()

    @gen.coroutine
    def _result_by_tornado(self, result, exit_code=0):
        yield self._queue.join()
        self._seq += 1
        result_url = self._get_result_url(self._seq, exit_code)
        data = self._create_result(result, self._seq, exit_code=exit_code)
        yield self._send_by_tornado(result_url, data)

    @gen.coroutine
    def _send_by_tornado(self, url, data):
        try:
            response = yield self._http_client.fetch(
                url,
                method='POST',
                headers={'Content-Type': 'application/json'},
                validate_cert=False,
                body=data)
        except Exception as exc:
            if hasattr(exc, 'response') and exc.response:
                exc = 'url:{}, exc:{}, body:{}'.format(url, exc,
                                                       exc.response.body)
            raise TaskLoggerError(data, str(exc))
        else:
            if response.code != 200:
                raise TaskLoggerError(data, reason=response.body)

    def _create_log(self, log, seq):
        assert isinstance(log, basestring)
        log = log + '\n'
        if self.wrap:
            log_msg = TaskLogMessage(task_id=self.task_id, log=log, seq=seq)
            data = json_encode({'messages': log_msg})
        else:
            data = log
        return data

    def _create_result(self, result, seq, exit_code):
        assert isinstance(result, basestring)
        result = result + '\n'
        if self.wrap:
            result_msg = TaskResultMessage(
                task_id=self.task_id,
                result=result,
                seq=seq,
                exit_code=exit_code)
            data = json_encode({'messages': result_msg})
        else:
            data = result
        return data
