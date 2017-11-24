# coding: utf-8
import json

from tornado import ioloop, gen
from circle.client import AsyncCircleClient


@gen.coroutine
def get_module_status():
    client = AsyncCircleClient()
    ret = yield client.send_message('status')
    print(json.dumps(ret))
    raise gen.Return(ret)


ioloop.IOLoop.instance().run_sync(get_module_status)
