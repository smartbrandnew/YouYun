import time
import datetime

from playhouse.test_utils import test_database
from peewee import SqliteDatabase
from framework.message.cache import (Message, put_message, get_messages_before,
                                     delete_messages_before)


def test_put_message():
    test_db = SqliteDatabase(':memory:')
    with test_database(test_db, [Message]):
        put_message(dict(id='xxx', type=100, body='zzz'), async=False)
        assert Message.select().count() == 1
        put_message(dict(id='yyy', type=100), async=False)
        assert Message.select().count() == 2
        msg = Message.select().first()
        assert msg.id == 'xxx'
        assert msg.type == 100
        assert msg.body == '"zzz"'


def test_get_message_with_deserialized_body():
    test_db = SqliteDatabase(':memory:')
    with test_database(test_db, [Message]):
        put_message(dict(id='xxx', type=100, body='zzz'), async=False)
        msg = Message.select().first()
        assert msg.id == 'xxx'
        assert msg.type == 100
        assert msg.body == '"zzz"'

    test_db = SqliteDatabase(':memory:')
    with test_database(test_db, [Message]):
        put_message(
            dict(
                id='xxx', type=100, body={'a': 1,
                                          'b': 2}), async=False)
        time.sleep(0.001)
        now = datetime.datetime.now()
        messages = get_messages_before(now, async=False)
        msg = messages[0]
        assert msg.id == 'xxx'
        assert msg.type == 100
        assert msg.body == {'a': 1, 'b': 2}


def test_get_messages_before():
    test_db = SqliteDatabase(':memory:')
    with test_database(test_db, [Message]):
        for i in range(100):
            put_message(dict(id='msg{0}'.format(i), type=100), async=False)
        import time
        time.sleep(0.001)
        now = datetime.datetime.now()
        put_message(dict(id='msg100', type=100), async=False)
        messages = get_messages_before(now, async=False)
        assert len(messages) == 100


def test_delete_messages_before():
    test_db = SqliteDatabase(':memory:')
    with test_database(test_db, [Message]):
        for i in range(100):
            put_message(dict(id='msg{0}'.format(i), type=100), async=False)
        import time
        time.sleep(0.001)
        now = datetime.datetime.now()
        put_message(dict(id='msg100', type=100), async=False)
        result = delete_messages_before(now, async=False)
        assert result == 100
        assert Message.select().count() == 1
        assert Message.select().first().id == 'msg100'
