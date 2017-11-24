from tornado import gen
from tornado.ioloop import IOLoop
from framework.config import config
from framework.message.type import MessageType
from framework.message.transfers import mock_transfer
from framework.schedulers.heartbeat import HeartbeatScheduler  # flake8: noqa


def test_heartbeat():
    io_loop = IOLoop()

    class MockedTransfer(object):

        @gen.coroutine
        def send(self, msg):
            assert len(msg['id']) == 32
            assert msg['type'] == MessageType.HEARTBEAT
            assert 'id' in msg['body']
            assert 'rss' in msg['body']
            assert 'vms' in msg['body']
            assert 'cpu' in msg['body']
            io_loop.stop()

    transfer = MockedTransfer()
    scheduler = HeartbeatScheduler()

    with config.mock({'heartbeat': 0.01}):
        with mock_transfer(transfer):
            io_loop.spawn_callback(scheduler.eventloop)
            io_loop.start()
