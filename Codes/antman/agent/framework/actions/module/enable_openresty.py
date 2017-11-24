# coding: utf-8

from tornado import gen

import nfs
from circle.client import AsyncCircleClient
from framework.actions.errors import MessageError
from framework.actions.constants import CIRCLE_CONF_DIR


class Openresty(object):
    enable_conf = nfs.join(CIRCLE_CONF_DIR, 'openresty.ini')
    disable_conf = nfs.join(CIRCLE_CONF_DIR, 'openresty.ini.disable')
    client = AsyncCircleClient()

    @classmethod
    @gen.coroutine
    def reload(cls):
        props = {'waiting': True, 'path': cls.enable_conf}
        ret = yield cls.client.send_message('reloadconfig', **props)
        if isinstance(ret, dict):
            if ret.get('status') == 'error':
                raise MessageError(ret.get('reason'))
        raise gen.Return('Finish reload openresty!')

    @classmethod
    @gen.coroutine
    def rm_openresty(cls):
        props = {"name": "openresty", "nostop": False, "waiting": False}
        ret = yield cls.client.send_message('rm', **props)
        if isinstance(ret, dict):
            if ret.get('status') == 'error':
                raise MessageError(ret.get('reason'))
        raise gen.Return('Finish rm openresty!')

    @classmethod
    @gen.coroutine
    def enable(cls):
        if nfs.exists(cls.disable_conf):
            nfs.rename(cls.disable_conf, cls.enable_conf)
            if not nfs.exists(cls.enable_conf):
                raise MessageError('Enable openresty failed !')
            yield cls.reload()

    @classmethod
    @gen.coroutine
    def disable(cls):
        if nfs.exists(cls.enable_conf):
            nfs.rename(cls.enable_conf, cls.disable_conf)
            yield cls.rm_openresty()

        if not nfs.exists(cls.disable_conf):
            raise MessageError('Disable openresty failed !')
