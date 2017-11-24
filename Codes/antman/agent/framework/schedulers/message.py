import logging
import time
import datetime
import urlparse

from tornado import gen, escape, httpclient
from framework.config import config
from framework.message import cache
from framework.message.models import Message
from framework.message.reactor import reactor
from framework.message.transfers import get_current_transfer
from framework.ioloop import get_io_loop
from framework import settings
from framework import utils
from framework.schedulers.base import BaseScheduler

logger = logging.getLogger('default')


class MessageFetchScheduler(BaseScheduler):
    TIMEOUT_DELAY = 5.0

    def __init__(self):
        self._cursor = self.get_cursor()
        url = urlparse.urljoin(config['upstream'], 'dispatcher/message')
        self._base_url = utils.update_query_params(
            url, {'tenant': config['tenant'],
                  'id': config['id']})
        self._retry_times = 0
        self._retry_interval = config.get('message_fetch_retry_interval', 5)
        self._http_client = httpclient.AsyncHTTPClient(io_loop=get_io_loop())

    def get_cursor(self):
        cursor = None
        raw_data = None
        json_data = None
        try:
            with open(settings.MESSAGE_CURSOR_FILE) as f:
                raw_data = f.read()
                json_data = escape.json_decode(raw_data)
                cursor = json_data['cursor']
        except Exception as exc:
            logger.error('Error while reading message cursor: %s', exc)
            logger.info('The content of message cursor file: %s', raw_data)
            logger.info('Reset message cursor to 0')
            cursor = 0
        return cursor

    def set_cursor(self, cursor):
        json_data = {'cursor': cursor, 'updated_time': time.time()}
        raw_data = escape.json_encode(json_data)
        try:
            with open(settings.MESSAGE_CURSOR_FILE, 'w') as f:
                f.write(raw_data)
        except Exception as exc:
            logger.error('Error while writing message cursor: %s', exc)
            logger.info('The content of writing data: %s', raw_data)

    @gen.coroutine
    def eventloop(self):
        while True:
            try:
                params = {'cursor': self._cursor}
                url = utils.update_query_params(self._base_url, params)
                timeout = (config['message_fetch_timeout'] +
                           MessageFetchScheduler.TIMEOUT_DELAY)
                response = yield self._http_client.fetch(
                    url,
                    method='GET',
                    headers={'Accept': 'application/json'},
                    connect_timeout=timeout,
                    request_timeout=timeout,
                    validate_cert=False,
                    raise_error=True,)
                self._retry_times = 0
                json_data = escape.json_decode(response.body)
                self._cursor = json_data['cursor']
                self.set_cursor(self._cursor)
                messages = json_data['messages']
                logger.info('Received %s messages', len(messages))
                for message in messages:
                    msg = Message.create(message)
                    reactor.feed(msg)
            except Exception as exc:
                logger.error(
                    'Error while fetching message: %s', exc, exc_info=True)
                if hasattr(exc, 'response') and exc.response:
                    logger.error(exc.response.body)
                self._retry_times += 1
                logger.info('Current message cursor: %s', self._cursor)
                if self._retry_times == 1:
                    wait_unit = 0
                else:
                    wait_unit = (self._retry_times % 12 + 1)
                wait_time = wait_unit * self._retry_interval
                logger.info('Wait %s seconds to re-fetch', wait_time)
                yield gen.sleep(wait_time)


class MessageCacheScheduler(BaseScheduler):

    @gen.coroutine
    def eventloop(self):
        while True:
            try:
                yield gen.sleep(config['message_resend_interval'])
                now = datetime.datetime.now()
                logger.info('Check message cache before %s', now)
                messages = yield cache.get_messages_before(now)
                if messages:
                    logger.info('Found %d messages in cache', len(messages))
                else:
                    logger.info('No message in cache')
                    continue
                transfer = get_current_transfer()
                logger.info('Start to resend messages')
                result = yield transfer.send(messages)
                if result:
                    logger.info('Resend successfully')
                    yield cache.delete_messages_before(now)
                    logger.info('Cache cleared')
            except Exception as exc:
                logger.error('Error while resending messages: %s', exc)
