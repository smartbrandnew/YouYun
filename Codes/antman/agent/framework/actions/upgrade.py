# coding: utf-8

import sys
import json
import os

from tornado import ioloop, gen, httpclient
from framework.actions import logger
from framework.actions.errors import MessageError
from framework.actions.reporter import Reporter


class Upgrade(object):

    def __init__(self, task_message):
        self.task_message = task_message
        self.ioloop = ioloop.IOLoop()
        self._http_client = httpclient.AsyncHTTPClient(io_loop=self.ioloop)
        self._url = 'http://127.0.0.1:16601/upgrade'

    def _get_env(self):
        try:
            self.task_id = os.environ.get('ANT_TASK_ID')
            self.id = os.environ.get('ANT_AGENT_ID')
            self.reporter = Reporter(self.task_id, logger)
        except Exception as e:
            logger.error(e)
            sys.exit(1)

    @gen.coroutine
    def post_upgrade(self, body):
        response = yield self._http_client.fetch(
            self._url,
            method='POST',
            headers={'Accept': 'application/json'},
            body=body,
            validate_cert=False,
            raise_error=True)

        if response.code != 200:
            raise MessageError('The response code：{}，body：{}'.format(
                response.code, response.body))

    @gen.coroutine
    def deal_message(self):
        upgrade_message = self.task_message.get(self.id)
        if not upgrade_message:
            yield self.reporter.log_error(
                'There is no upgrade information '
                'needed by the agent!',
                done=True)
            sys.exit(1)
        upgrade_message.update({'task_id': self.task_id})
        yield self.post_upgrade(json.dumps(upgrade_message))
        self.reporter.log_ok('The upgrade information has '
                             'been sent to the upgrade process')

    def run(self):
        self._get_env()
        try:
            self.ioloop.run_sync(self.deal_message)
        except Exception as e:
            self.reporter.log_error(
                'Exec upgrade error: {}'.format(e), done=True)
            sys.exit(1)


if __name__ == '__main__':
    message = sys.stdin.read()
    if not message:
        logger.error('The stdin is None, please input the '
                     'right args with stdin !!!')
        sys.exit(1)
    upgrade = Upgrade(json.loads(message))
    upgrade.run()
