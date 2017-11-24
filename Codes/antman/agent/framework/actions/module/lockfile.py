# coding: utf-8

import os

import nfs
import yaml

from tornado import gen
from circle.client import AsyncCircleClient
from framework.actions.module.constants import LOCK_FILE, PKG_DIR, FULLNAME_SEP
from framework.actions.module.config import deal_cmd


class LockFile(object):
    client = AsyncCircleClient()

    @classmethod
    def get_dict(cls):
        if os.path.exists(LOCK_FILE):
            with open(LOCK_FILE) as lock_file:
                info = yaml.load(lock_file.read()) or {}
            return info
        return {}

    @classmethod
    def create_lock_file(cls, pkg_dict):
        if not nfs.exists(PKG_DIR):
            nfs.makedirs(PKG_DIR)
        with open(LOCK_FILE, 'w') as lock_file:
            yaml.dump(pkg_dict, lock_file, default_flow_style=False)

    @classmethod
    def add_pkg(cls, pkg_name, config):
        lock_dict = cls.get_dict()
        pkg_detail = {"version": config.content.get('version')}
        if config.content.get('run'):
            pkg_detail["daemon"] = True
            cmds = deal_cmd(config.content.get('run'))

            if isinstance(cmds, basestring):
                pkg_detail["watchers"] = [
                    "{}{}1".format(pkg_name, FULLNAME_SEP)
                ]
            else:
                pkg_detail["watchers"] = [
                    "{}{}{}".format(pkg_name, FULLNAME_SEP, i + 1)
                    for i in range(len(cmds))
                ]
        else:
            pkg_detail["daemon"] = False
            pkg_detail["watchers"] = None

        lock_dict[pkg_name] = pkg_detail
        cls.create_lock_file(lock_dict)

    @classmethod
    def check_pkg(cls, pkg_name):
        lock_dict = cls.get_dict()
        if pkg_name in lock_dict:
            return True
        return False

    @classmethod
    def remove_pkg(cls, pkg_name):
        lock_dict = cls.get_dict()
        if pkg_name in lock_dict:
            del lock_dict[pkg_name]
        cls.create_lock_file(lock_dict)

    @classmethod
    @gen.coroutine
    def get_module_status(cls):
        ret = yield cls.client.send_message('status')
        raise gen.Return(ret['statuses'])

    @classmethod
    @gen.coroutine
    def get_pkg_status(cls):
        lock_dict = cls.get_dict()
        results = []
        module_status = yield cls.get_module_status()

        for module_name, detail in lock_dict.items():
            result = {
                'name': module_name,
                'version': detail['version'],
                'code': module_name,
                'status': 'started'
            }
            if detail['daemon']:
                for watcher in detail['watchers']:
                    if module_status[watcher] != 'active':
                        result['status'] = 'stopped'
                        break
            results.append(result)

        raise gen.Return(results)
