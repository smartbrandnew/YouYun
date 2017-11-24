import json
import urlparse
import logging

from tornado import gen
from tornado.httpclient import AsyncHTTPClient
from framework.message.models import Message
from framework.message.transfers.base import BaseTransfer
from framework.config import config
from framework.ioloop import get_io_loop
from framework import utils
from framework.settings import ENCODING

logger = logging.getLogger('default')


class HttpTransfer(BaseTransfer):

    def __init__(self):
        url = urlparse.urljoin(config['upstream'], 'dispatcher/message')
        self._url = utils.update_query_params(url, {'tenant': config['tenant']})
        self._http_client = AsyncHTTPClient(io_loop=get_io_loop())

    @gen.coroutine
    def send(self, messages):
        if isinstance(messages, Message):
            messages = [messages]
        try:
            response = yield self._http_client.fetch(
                self._url,
                method='POST',
                headers={'Content-Type': 'application/json'},
                validate_cert=False,
                body=json.dumps(
                    {
                        'messages': messages
                    }, encoding=ENCODING))
        except Exception as exc:
            logger.error('Error in http transfer: %s', exc)
            if hasattr(exc, 'response') and exc.response:
                logger.error(exc.response.body)
            raise gen.Return(False)
        else:
            raise gen.Return(response.code == 200)
