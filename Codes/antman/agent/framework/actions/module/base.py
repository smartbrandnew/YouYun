# coding: utf-8
import os
import sys
import nfs

from tornado import ioloop, gen
from tornado.httpclient import AsyncHTTPClient, HTTPError
from circle.client import AsyncCircleClient
from framework.config import config
from framework.actions.errors import MessageError
from framework.actions import module_logger
from framework.actions.utils import get_env, to_string
from framework.actions.reporter import Reporter

from constants import PKG_CACHE_DIR


class Base(object):

    def __init__(self, modules, io_loop=None, reload_config=True):
        self.io_loop = io_loop if io_loop else ioloop.IOLoop.current()
        self.client = AsyncHTTPClient(io_loop=self.io_loop)
        self.circle_client = AsyncCircleClient()
        self.modules = modules
        self.reload_config = reload_config
        self.download_url = 'http://127.0.0.1:{}/file' \
                            ''.format(config.get('framework_port', '16600'))

    def _prepare(self):
        self.task_id = os.environ.get('ANT_TASK_ID')
        if not self.task_id:
            print('The task_id is None, '
                  'please input the right ANT_TASK_ID with env!')
            sys.exit(1)
        if self.task_id != '-1':
            self.modules = self.modules.get(config['id'])
        if not self.modules:
            print('There is no message for this agent!')
            sys.exit(1)
        get_env()
        self.reporter = Reporter(self.task_id, module_logger)

    @staticmethod
    def deal_env(envs):
        module_env = os.environ
        envs = {
            to_string(key): to_string(value)
            for key, value in envs.items()
        } if envs else {}
        module_env.update(envs)
        return module_env

    @gen.coroutine
    def execute(self):
        raise NotImplementedError('Execute function is not implemented')

    @gen.coroutine
    def download_pkg(self, filename):
        yield self.reporter.log_ok('Begin to download package '
                                   '{} ...'.format(filename))
        down_url = self.download_url + '?filename=' + filename
        try:
            response = yield self.client.fetch(
                down_url,
                connect_timeout=config.get('file_service_connect_timeout',
                                           3600.0),
                request_timeout=config.get('file_service_request_timeout',
                                           3600.0),
                validate_cert=False)
            if response.code == 200:
                if not nfs.exists(PKG_CACHE_DIR):
                    os.makedirs(PKG_CACHE_DIR)
                nfs.copy(response.body, nfs.join(PKG_CACHE_DIR, filename))
                yield self.reporter.log_ok('Download package {} success'.format(
                    filename))
            else:
                raise MessageError('Download package {} failed, reason: {}!'
                                   .format(filename, response.body))
        except HTTPError as e:
            raise MessageError('Download package {} failed, reason: {}, {}!'
                               .format(filename, e, e.message))

    @gen.coroutine
    def circle_cmd(self, cmd, module_name=None):
        props = {'waiting': True, 'name': module_name, 'match': 'regex'} \
            if module_name else {}
        ret = yield self.circle_client.send_message(cmd, **props)
        if isinstance(ret, dict):
            if ret.get('status') == 'error':
                raise MessageError(ret.get('reason'))
        if module_name:
            raise gen.Return('Finish {} {}!'.format(cmd, module_name))
        raise gen.Return('Finish {}!'.format(cmd))

    def run(self):
        self._prepare()
        self.io_loop.run_sync(self.execute)
