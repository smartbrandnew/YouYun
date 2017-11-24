from paramiko import SSHClient
from paramiko import AutoAddPolicy
import yaml
from contextlib import contextmanager


@contextmanager
def create_ssh(host, username, password):
    ssh = SSHCMD()
    ssh.set_missing_host_key_policy(AutoAddPolicy())
    try:
       print "creating connection"
       ssh.connect(host, username=username, password=password)
       print "connected"
       yield ssh
    finally:
       print "closing connection"
       ssh.close()
       print "closed"

def parase_yaml_file(yaml_file):
    with open(yaml_file,'r') as f:
        content = yaml.load(f)
    return content


def excutor_cmd(ssh, cmd, debug=False):
    stdin, stdout, stderr,exit_code = ssh.exec_command(cmd)
    out = stdout.read()
    err = stderr.read()
    print exit_code,cmd,'-------------'
    if exit_code != 0:
        raise Exception('{} excutor fiald;ERROR:{}'.format(cmd, err))
    if debug:
        print out,err
    return out, err

class SSHCMD(SSHClient):
    def exec_command(
        self,
        command,
        bufsize=-1,
        timeout=None,
        get_pty=False,
        environment=None,
    ):
        chan = self._transport.open_session(timeout=timeout)
        if get_pty:
            chan.get_pty()
        chan.settimeout(timeout)
        if environment:
            chan.update_environment(environment)
        chan.exec_command(command)
        exit_code = chan.recv_exit_status()
        stdin = chan.makefile('wb', bufsize)
        stdout = chan.makefile('r', bufsize)
        stderr = chan.makefile_stderr('r', bufsize)
        return stdin, stdout, stderr, exit_code

