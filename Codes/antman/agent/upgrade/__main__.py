# coding: utf-8
import json
from tornado import gen, queues, httpserver, ioloop
from tornado.web import RequestHandler, Application

import reloader
from constants import logger, UPGRADE_PORT


class UpgradeServer(RequestHandler):

    def initialize(self, task_queue):
        self.task_queue = task_queue

    @gen.coroutine
    def post(self):
        if self.request.body:
            try:
                data = json.loads(self.request.body)
                self.task_queue.put(data)
                self.write('Valid request !!!')
            except:
                self.write('Deal request body error!!!')
        else:
            self.write('Invalid Request')


class Status(RequestHandler):

    @gen.coroutine
    def get(self):
        self.write('ok')


class Upgrade(object):

    def __init__(self):
        self.io_loop = ioloop.IOLoop()
        self.task_queue = queues.Queue()
        self.modify_times = {}

    def run(self):
        try:
            upgrade_app = self.create_upgrade_app()
            upgrade_server = httpserver.HTTPServer(upgrade_app)
            upgrade_server.bind(UPGRADE_PORT, '127.0.0.1')
            upgrade_server.start()
            self.io_loop.spawn_callback(self.waiting_for_upgrade)
            self.io_loop.start()
        except Exception as e:
            logger.error(str(e))

    def create_upgrade_app(self):
        upgrade_app = Application([
            (r'/upgrade', UpgradeServer, dict(task_queue=self.task_queue)),
            (r'/status', Status),
        ])
        return upgrade_app

    @gen.coroutine
    def waiting_for_upgrade(self):
        while 1:
            try:
                task_message = yield self.task_queue.get()
                from preupgrade import PreUpgrade
                pre_upgrade = PreUpgrade(task_message)
                pre_upgrade.execute()
                reloader.reload_module(self.modify_times)
            except Exception as e:
                logger.error(e)


if __name__ == '__main__':
    upgrade = Upgrade()
    upgrade.run()
