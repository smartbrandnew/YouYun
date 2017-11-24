# coding: utf-8

import paramiko
from concurrent.futures import ThreadPoolExecutor

from errors import MessageError

_executor = ThreadPoolExecutor(max_workers=20)

SSHRunError = SCPRunError = paramiko.SSHException


class SSHClient(object):

    def __init__(self, user, passwd, ip, port=22, timeout=10):
        self.user = user
        self.passwd = passwd
        self.ip = ip
        self.port = port or 22
        self.timeout = timeout
        self.ssh_client = paramiko.SSHClient()
        self.ssh_client.load_system_host_keys()
        self.ssh_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    def _connect(self):
        self.ssh_client.connect(
            self.ip,
            port=self.port,
            username=self.user,
            password=self.passwd,
            timeout=self.timeout)

    def ssh(self, cmd):
        future = _executor.submit(self._ssh, cmd)
        return future

    def _ssh(self, cmd):
        self._connect()
        chan = self.ssh_client.get_transport().open_session()
        chan.settimeout(None)
        chan.get_pty()
        chan.exec_command(cmd)
        stdout = chan.makefile('r', -1)
        stderr = chan.makefile_stderr('r', -1)
        output = stdout.read()
        err = stderr.read()
        result = err if err else output
        if chan.recv_exit_status() != 0:
            self.ssh_client.close()
            raise MessageError('SSH running cmd error:\n {}'.format(result))
        self.ssh_client.close()
        return result

    def scp(self, src, dst):
        future = _executor.submit(self._scp, src, dst)
        return future

    def _scp(self, src, dst):
        self._connect()
        sftp = self.ssh_client.open_sftp()
        sftp.put(src, dst)
        sftp.close()
        self.ssh_client.close()
