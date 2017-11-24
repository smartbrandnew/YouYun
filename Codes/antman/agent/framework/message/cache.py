import datetime

from peewee import (SqliteDatabase, Model, CharField, IntegerField, TextField,
                    DateTimeField)
from concurrent.futures import ProcessPoolExecutor
from tornado.escape import json_encode, json_decode
from framework.config import config
from framework.message import models

executor = ProcessPoolExecutor(max_workers=1)
database = SqliteDatabase(config['message_cache_file'])


class Message(Model):
    id = CharField(primary_key=True, max_length=32)
    type = IntegerField()
    body = TextField(null=True)
    created_time = DateTimeField(default=datetime.datetime.now)

    class Meta:
        database = database


def _put_message(d):
    with database.execution_context() as ctx:  # flake8: noqa
        msg = models.Message(d['id'], d['type'], d.get('body'))
        Message.create(
            id=msg.id,
            type=msg.type,
            body=json_encode(msg.body) if msg.body else None)
    return msg


def put_message(msg, async=True):
    if async:
        return executor.submit(_put_message, msg)
    else:
        return _put_message(msg)


def _get_messages_before(datetime):
    with database.execution_context() as ctx:  # flake8: noqa
        messages = [
            models.Message(msg.id, msg.type,
                           json_decode(msg.body) if msg.body else None)
            for msg in Message.select().where(Message.created_time < datetime)
        ]
    return messages


def get_messages_before(datetime, async=True):
    if async:
        return executor.submit(_get_messages_before, datetime)
    else:
        return _get_messages_before(datetime)


def _delete_messages_before(datetime):
    with database.execution_context() as ctx:  # flake8: noqa
        query = Message.delete().where(Message.created_time < datetime)
        result = query.execute()
    return result


def delete_messages_before(datetime, async=True):
    if async:
        return executor.submit(_delete_messages_before, datetime)
    else:
        return _delete_messages_before(datetime)
