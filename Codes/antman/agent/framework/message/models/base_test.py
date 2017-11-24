from tornado.escape import json_encode, json_decode
from framework.message.models.base import Message


def test_new_message():
    msg = Message('xxx', 100, 'zzz')
    assert isinstance(msg, dict)
    assert msg.id == 'xxx'
    assert msg.type == 100
    assert msg.body == 'zzz'


def test_create_message():
    msg = Message.create(100, 'yyy')
    assert len(msg.id) == 32
    assert msg.type == 100
    assert msg.body == 'yyy'


def test_encode_message():
    msg = Message.create(100)
    serialized = json_encode(msg)
    msg1 = json_decode(serialized)
    assert msg1['id'] == msg.id
    assert msg1['type'] == msg.type
    assert msg1['body'] == msg.body is None


def test_create_from_dict():
    msg = Message.create({'id': 'xxx', 'type': 100, 'body': 'zzz'})
    assert msg.id == 'xxx'
    assert msg.type == 100
    assert msg.body == 'zzz'
