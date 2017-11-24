import uuid
from typing import Union, Optional  # flake8: noqa


class Message(dict):
    __slots__ = ()

    def __init__(self, id, type, body):
        self['id'] = id
        self['type'] = type
        self['body'] = body

    @property
    def id(self):
        return self['id']

    @property
    def type(self):
        return self['type']

    @property
    def body(self):
        return self['body']

    @staticmethod
    def create(dict_or_type, body=None):  # Body can be emtpy.
        # type: (Union[dict, int], Optional[dict]) -> Message
        if isinstance(dict_or_type, dict):
            id = dict_or_type['id']
            type = dict_or_type['type']
            body = dict_or_type.get('body')
            return Message(id, type, body)
        id = uuid.uuid4().hex
        return Message(id, dict_or_type, body)
