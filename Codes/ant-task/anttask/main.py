# coding: utf-8
import os

from ng import gen
from ng.ioc import Injector
from ng.services import IOLoopService, LoggerService
from antsdk.antcom_client import AntcomClient
from anttask.constants import DIS_PLUGIN_ID, REDIS_LIKE_URL, ANTCOM_CO_PORT

class TaskPlugin(object):
    def __init__(self):
        injector = Injector()
        self.io_loop = injector.provide(IOLoopService)
        self.logger = injector.provide(LoggerService)

        redis_like_url = os.environ.get('REDIS_LIKE_URL', REDIS_LIKE_URL)
        antcom_co_port = os.environ.get('ANTCOM_CO_PORT', ANTCOM_CO_PORT)
        self.client = AntcomClient(
            ctrl_id=os.environ.get('DIS_PLUGIN_ID', DIS_PLUGIN_ID),
            host=redis_like_url,
            co_port=antcom_co_port,
            injector=injector)

        # Update constants, in order let scripts/entry
        # get right REDIS_LIKE_URL and ANTCOM_CO_PORT
        constants.REDIS_LIKE_URL = redis_like_url
        constants.ANTCOM_CO_PORT = antcom_co_port

        self.io_loop.spawn_callback(self._run)

    def run(self):
        self.io_loop.start()

    @gen.coroutine
    def _run(self):
        yield self.client.try_connect()
        # for commands
        commands = get_commands()
        for event_name, cmd_cls in commands.iteritems():
            cmd_obj = cmd_cls(self.client)
            self.client.listen(event_name, cmd_obj.listen_callback)
        self.logger.info('Waiting for commands...')


if __name__ == '__main__':
    task_plugin = TaskPlugin()
    task_plugin.run()