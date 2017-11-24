# coding: utf-8

import sys
import socket
import json
import os
import paramiko

from concurrent.futures import ThreadPoolExecutor

from tornado import gen, ioloop
from framework.actions import logger
from framework.actions.constants import IS_VIRTUAL_CMD
from framework.actions.errors import MessageError
from framework.actions.reporter import Reporter


class Scan(object):
    """
        Scan ips
        [{
            "host": "host",
            "port": port,
            "network_domain": 'network_domain'
        }]

    """

    def __init__(self, task_message):
        self.io_loop = ioloop.IOLoop()
        self.task_message = task_message
        self.result = []

    def _validate(self):
        if not isinstance(self.task_message, list):
            raise MessageError('Infos should be list')
        for info in self.task_message:
            if not isinstance(info, dict):
                raise MessageError('Info should be dict')
            for key in ('host', 'port', 'user', 'passwd', 'network_domain'):
                if key not in info:
                    raise MessageError('{!r} is missing in info'.format(key))

        task_length = len(self.task_message)
        if task_length > 20:
            self._executor = ThreadPoolExecutor(max_workers=20)
        else:
            self._executor = ThreadPoolExecutor(max_workers=task_length)

    def _prepare(self):
        self.task_id = os.environ.get('ANT_TASK_ID')
        if not self.task_id:
            logger.error('The task_id is None, '
                         'please input the right ANT_TASK_ID with env!!!')
            sys.exit(1)

        if os.environ.get('ANT_TASK_URL'):
            tenant = os.environ.get('ANT_TENANT')
            if not tenant:
                logger.error('The tenant is None, '
                             'please input the right ANT_TASK_ID with env!!!')
                sys.exit(1)
        else:
            tenant = None
        self.reporter = Reporter(self.task_id, logger, tenant)

    @staticmethod
    def do_ssh_cmd(ssh_client, cmd):
        chan = ssh_client.get_transport().open_session()
        chan.get_pty()
        chan.exec_command(cmd)
        stdout = chan.makefile('r', -1)
        stderr = chan.makefile_stderr('r', -1)
        output = stdout.read()
        err = stderr.read()
        if chan.recv_exit_status() != 0:
            raise MessageError(err)
        else:
            return output

    def check_server(self, info):
        ssh_client = paramiko.SSHClient()
        ssh_client.load_system_host_keys()
        ssh_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        try:
            ssh_client.connect(
                info['host'],
                port=info['port'],
                username=info['user'],
                password=info['passwd'],
                timeout=1)
            info.pop('passwd')

            system = self.do_ssh_cmd(ssh_client, 'uname')
            system = system.lower()
            if 'aix' in system:
                info['node_type'] = "MiniServer"
            else:
                result = self.do_ssh_cmd(ssh_client, IS_VIRTUAL_CMD)
                info['node_type'] = 'VM' if result else 'PCServer'

            self.result.append({
                'host': info['host'],
                'network_domain': info['network_domain'],
                'node_type': info['node_type']
            })
        except socket.error as e:
            info.pop('passwd')
            logger.error('The info: {}, connect error: {}'.format(info, str(e)))
        except Exception as e:
            info.pop('passwd')
            logger.error('The info: {}, get system info error: {}'.format(
                info, str(e)))

    def do_check(self):
        running = []
        for info in self.task_message:
            running.append(self._executor.submit(self.check_server, info))
        return running

    @gen.coroutine
    def execute(self):
        try:
            self._prepare()
            self._validate()
            running = yield self.do_check()
            while running:
                for future in running[:]:
                    if not future or future.done():
                        running.remove(future)
            yield self.reporter.log_ok(json.dumps(self.result), done=True)
        except Exception as e:
            logger.error(str(e))
            yield self.reporter.log_error(str(e), done=True)
        self.io_loop.stop()

    def run(self):
        self.io_loop.spawn_callback(self.execute)
        self.io_loop.start()


if __name__ == '__main__':
    message = sys.stdin.read()
    if not message:
        logger.error('The stdin is None, '
                     'please input the right args with stdin !!!')
        sys.exit(1)

    scan = Scan(json.loads(message))
    scan.run()
