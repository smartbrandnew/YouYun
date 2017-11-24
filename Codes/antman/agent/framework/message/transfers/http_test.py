from tornado import web
from tornado.escape import json_decode
from tornado.testing import AsyncHTTPTestCase, gen_test
from framework.ioloop import get_io_loop
from framework.config import config
from framework.message.transfers.http import HttpTransfer


class HttpTransferTest(AsyncHTTPTestCase):

    def setUp(self):
        super(HttpTransferTest, self).setUp()
        with config.mock({
                'upstream': self.get_url('/'),
                'tenant': 'mock_tenant'
        }):
            self.transfer = HttpTransfer()

    def get_new_ioloop(self):
        return get_io_loop()

    def get_app(self):

        class TestHandler(web.RequestHandler):

            def post(hdlr):
                assert hdlr.get_argument('tenant') == 'mock_tenant'
                self.assertEqual(hdlr.request.headers['Content-Type'],
                                 'application/json')
                payload = json_decode(hdlr.request.body)
                messages = payload['messages']
                self.assertEqual(len(messages), 2)
                msg1, msg2 = messages
                self.assertEqual(msg1['id'], 'xxx')
                self.assertEqual(msg1['type'], 'yyy')
                self.assertEqual(msg1['body'], 'zzz')
                self.assertEqual(msg2['id'], 'aaa')
                self.assertEqual(msg2['type'], 'bbb')
                self.assertEqual(msg2['body'], 'ccc')
                hdlr.write('ok')

        return web.Application([(r'/dispatcher/message', TestHandler)])

    @gen_test
    def test_send(self):
        result = yield self.transfer.send([{
            'id': 'xxx',
            'type': 'yyy',
            'body': 'zzz'
        }, {
            'id': 'aaa',
            'type': 'bbb',
            'body': 'ccc'
        }])
        self.assertTrue(result)
