from tornado import gen
from tornado.concurrent import Future
from tornado.ioloop import IOLoop
from playhouse.test_utils import test_database
from peewee import SqliteDatabase
from framework.config import config
from framework.message.models import Message
from framework.message.cache import Message as MessageModel
from framework.message.type import MessageType
from framework.message import cache
from framework.message.transfers import mock_transfer
from framework.schedulers.message import MessageCacheScheduler  # flake8: noqa


def test_message_cache_resend():
    io_loop = IOLoop()
    test_db = SqliteDatabase(':memory:')

    class MockedTransfer(object):

        @gen.coroutine
        def send(self, messages):
            assert len(messages) == 2
            assert messages[0]['type'] == MessageType.HEARTBEAT
            assert messages[0]['body'] == {'a': 1}
            assert messages[1]['type'] == MessageType.HEARTBEAT
            assert messages[1]['body'] == {'b': 2}
            io_loop.stop()

    origin_get_messages_before = cache.get_messages_before
    origin_delete_messages_before = cache.delete_messages_before

    future = Future()
    future.set_result([
        Message.create(MessageType.HEARTBEAT, {'a': 1}),
        Message.create(MessageType.HEARTBEAT, {'b': 2})
    ])

    def get_messages_before(datetime):
        return future

    cache.get_messages_before = get_messages_before

    def delete_messages_before(datetime):
        pass

    cache.delete_messages_before = delete_messages_before

    transfer = MockedTransfer()
    scheduler = MessageCacheScheduler()

    try:
        with test_database(test_db, [MessageModel]):
            with config.mock({'message_resend_interval': 0.01}):
                with mock_transfer(transfer):
                    io_loop.spawn_callback(scheduler.eventloop)
                    io_loop.start()
    except Exception:
        raise
    finally:
        cache.get_messages_before = origin_get_messages_before
        cache.delete_messages_before = origin_delete_messages_before
