# coding: utf-8

import os
import sys
import socket
import urlparse
import json
import re
import traceback

import nfs
from tornado import gen, ioloop
from tornado.httpclient import AsyncHTTPClient
from framework.actions import logger
from framework.actions.utils import (to_int, get_agent_filename,
                                     get_upstream_ip, de_duplication)
from framework.actions.constants import (
    AGENT_NAME, UPSTREAM_SUFFIX, INSTALL_PARALLEL_NUM, NGINX_PORT,
    KNOWN_DISTRIBUTION, SPECIAL_PLATFORM_LIST, REPO_DIR, REPO_ANT_SPACENAME)
from framework.actions.errors import (MessageError, NotMatchError,
                                      AlreadyExistsError)
from framework.actions.reporter import Reporter
from framework.actions.ssh import SSHClient, SSHRunError


class Install(object):
    """
        To install ant agent(s)::

        [{
            "network_domain": "network_domain",
            "host": "host",
            "port": port,
            "user": "user name",
            "passwd": "password",
            "dst": "dst",
            "upstream": "upstream",
            "task_id": "task_id",
            "tenant": "tenant"
        }]
    """

    def __init__(self, task_message):
        self.io_loop = ioloop.IOLoop()
        self.task_message = task_message
        self.require = ('host', 'port', 'user', 'passwd', 'upstream', 'task_id',
                        'tenant')

    def validate(self):
        if not isinstance(self.task_message, list):
            raise MessageError('Infos should be list')
        for info in self.task_message:
            if not isinstance(info, dict):
                raise MessageError('Info should be dict')
            for key in self.require:
                if key not in info:
                    raise MessageError('{!r} is missing in info'.format(key))

    @gen.coroutine
    def init_openresty(self):
        if os.environ.get('ANT_TASK_URL'):
            raise gen.Return('')

        from framework.actions.module.enable_openresty import Openresty
        yield Openresty.enable()

    @gen.coroutine
    def execute(self):
        runs = []
        try:
            self.validate()
            yield self.init_openresty()
            for info in self.task_message:
                single_install = SingleInstall(info)
                runs.append(single_install.run())

            for i in range(len(runs) / INSTALL_PARALLEL_NUM + 1):
                try:
                    yield runs[i:i + INSTALL_PARALLEL_NUM]
                except Exception as e:
                    logger.error(e, exc_info=True)
        except Exception as e:
            logger.error(str(e))
        self.io_loop.stop()

    def run(self):
        os.umask(0027)
        self.io_loop.spawn_callback(self.execute)
        self.io_loop.start()


class SingleInstall(object):

    def __init__(self, info):
        self.host = info['host']
        self.port = to_int(info['port'], 22)
        self.user = info['user']
        self.passwd = info['passwd']
        self.dst = info.get('dst') or '/home'
        self.dst = '{}/uyun-ant'.format(self.dst)
        self.project_dir = '{}/{}'.format(self.dst, AGENT_NAME)
        self.upstream = info.get('upstream')
        self.tenant = info.get('tenant')
        self.network_domain = info.get('network_domain')
        self.reporter = Reporter(info['task_id'], logger, self.tenant)
        self.ssh_client = SSHClient(self.user, self.passwd, self.host,
                                    self.port)

    @gen.coroutine
    def run(self):
        try:
            yield self._check_sudo()
            yield self._check_installed()
            yield self._check_started()
            yield self._get_upstream()
            yield self.reporter.log_ok('Install agent to {}'.format(self.host))
            agent_platform = yield self._get_agent_platform()
            compress_name = self._get_pkg_name(agent_platform)
            if 'aix' in compress_name:
                yield self._deliver_to_aix(compress_name)
            else:
                yield self._deliver_to_agent(compress_name)
            yield self._install(agent_platform, compress_name)
            yield self._do_bootstrap()
            yield self.reporter.log_ok('Install agent to {} done'.format(
                self.host))
            yield self.get_agent_platform_detail()
        except socket.timeout:
            yield self.reporter.log_error(
                'Connect to {} time out. Please check host IP'
                .format(self.host))
        except Exception as e:
            yield self.reporter.log_error(str(e), True)

    def _get_pkg_name(self, agent_platform):
        compress_name = get_agent_filename(agent_platform)
        self.dst_name = '{}/{}'.format(self.dst, compress_name)
        return compress_name

    @gen.coroutine
    def do_ssh_cmd(self, cmd, cmd_prefix=None):
        cmd_prefix = cmd_prefix if cmd_prefix else self.cmd_prefix
        logger.info('cmd: {!r}'.format(cmd))
        cmd = '{} \'{}\''.format(cmd_prefix, cmd) if cmd_prefix else cmd
        result = yield self.ssh_client.ssh(cmd)
        raise gen.Return(result.strip())

    @gen.coroutine
    def _check_sudo(self, runas=None):
        if self.user == 'root':
            self.runner = 'root'
            self.cmd_prefix = ''
            raise gen.Return(True)

        self.runner = runas if runas else self.user
        self.cmd_prefix = 'echo \'{}\' | sudo -p "" -S  su - {} -c'\
            .format(self.passwd, self.runner)

        try:
            yield self.do_ssh_cmd('LANG=C && env > /dev/null')
        except Exception as e:
            raise MessageError('Need sudo to install agent, {}'.format(e))
        raise gen.Return(True)

    @gen.coroutine
    def _get_upstream(self):
        if self.upstream:
            raise gen.Return(True)

        result = yield self.ssh_client.ssh('env |grep SSH_CLIENT')
        upstream_ip = get_upstream_ip(result.strip())
        if not upstream_ip:
            raise MessageError('Get upstream error!')

        self.upstream = 'http://{}:{}'.format(upstream_ip, NGINX_PORT)

    @gen.coroutine
    def _check_installed(self):
        try:
            cmd = 'ls {} > /dev/null 2>&1 && echo 0 || echo 1'\
                .format(self.project_dir)
            result = yield self.do_ssh_cmd(cmd)
            if result and result.strip() == '0':
                raise AlreadyExistsError('Ant agent on {}'.format(self.host))
        except MessageError:
            pass

    @gen.coroutine
    def _check_started(self):
        cmd = 'ps -ef| grep -Ew "circled|upgrade" | ' \
              'grep "{}/embedded/bin/python"| grep -v grep| wc -l'\
              .format(AGENT_NAME)
        result = yield self.do_ssh_cmd(cmd)
        if result and result.strip() != '0':
            raise MessageError('Ant agent on {} has been installed and '
                               'is running, {}'.format(self.host, result))

    @staticmethod
    def deal_agent_platform(system, version, arch):
        agent_platform = (system + version).lower()
        if agent_platform in SPECIAL_PLATFORM_LIST:
            return '{}-{}'.format(agent_platform, arch)
        version = version.split('.')[0]
        agent_platform = (system + version).lower()
        if agent_platform in SPECIAL_PLATFORM_LIST:
            return '{}-{}'.format(agent_platform, arch)
        if 'aix' in system:
            return 'aix-{}'.format(arch)
        return 'linux-{}'.format(arch)

    @gen.coroutine
    def get_system_version(self, distribution):
        version_cmd = \
            "cat /etc/*-release 2>/dev/null |sed 's/^#//g' |" \
            "grep -E \"^VERSION|^{distribution}\"|" \
            "grep -v \"VERSION_ID\" || " \
            "grep -E \"{distribution}\" /etc/issue 2>/dev/null" \
            .format(distribution=distribution)
        version = yield self.do_ssh_cmd(version_cmd)
        search = re.search('\d+(\.\d+)?', version)
        if search:
            version = search.group(0)
            raise gen.Return(version)
        else:
            raise MessageError('Get system version error!')

    @gen.coroutine
    def _get_agent_platform(self):
        """Detect agent platform (AIX, Linux) and cpu arch (x86, x64)"""
        yield self.reporter.log_ok('Detecting agent platform')
        try:
            system_uname = yield self.do_ssh_cmd('uname')
            system_uname = system_uname.lower()
            if 'aix' in system_uname:
                system = 'aix'
                output = yield self.do_ssh_cmd(
                    '[[ `getconf HARDWARE_BITMODE | grep 64|wc -l` -gt 0 ]] '
                    '&& echo 64 || echo 32')
                output = output.strip()
                version = yield self.do_ssh_cmd('oslevel')
                version = '.'.join(version.split('.')[0:2])
            else:
                find_str = 'grep -Eo "{}" '.format(KNOWN_DISTRIBUTION)
                cmd = 'lsb_release -d 2>/dev/null | {find_str} ' \
                      '|| cat /etc/*-release  2>/dev/null | {find_str}' \
                      '|| {find_str} /etc/issue 2>/dev/null' \
                      '|| uname -s'.format(find_str=find_str)
                distribution = yield self.do_ssh_cmd(cmd)
                distribution = de_duplication(distribution)
                system = distribution.lower()

                if not system:
                    raise MessageError('Get system distribution error!')

                output = yield self.do_ssh_cmd(
                    '[[ `uname -m|grep 64|wc -l` -gt 0 ]] '
                    '&& echo 64 || echo 32')
                output = output.strip()
                version = yield self.get_system_version(distribution)

            logger.info('Get system version: {}'.format(version))
            self.system = system
            agent_platform = self.deal_agent_platform(system, version, output)
            yield self.reporter.log_ok('Agent platform: {}'
                                       .format(agent_platform))
            raise gen.Return(agent_platform)
        except SSHRunError:
            raise NotMatchError('Unsupport system')

    @gen.coroutine
    def _deliver_to_agent(self, compress_name):
        try:
            file_url = urlparse.urljoin(self.upstream, UPSTREAM_SUFFIX)
            file_url = urlparse.urljoin(file_url, 'file/')
            file_url = urlparse.urljoin(file_url, compress_name)
            if self.system == 'debian':
                curl_cmd = \
                    'wget "{}" -c -P "{}" --no-check-certificate'\
                    .format(file_url, self.dst)
                yield self.do_ssh_cmd(curl_cmd)
            else:
                curl_cmd = 'curl -s -w "%{{http_code}}" "{}" -s -m 3600 -o ' \
                           '"{}" -k --create-dirs'\
                            .format(file_url, self.dst_name)
                http_code = yield self.do_ssh_cmd(curl_cmd)
                logger.info('Download pkg {}, the http_code is {}'
                            .format(compress_name, http_code))
                if http_code == '404':
                    raise MessageError('The package {} is not exists'.format(
                        compress_name))

                if http_code != '200':
                    raise MessageError('Http code is {}!'.format(http_code))
            yield self.reporter.log_ok('Download agent pkg successfully')
        except Exception as e:
            raise MessageError('Download agent pkg failed. {}'.format(e))

    @gen.coroutine
    def _deliver_to_aix(self, compress_name):
        try:
            file_path = nfs.join(REPO_DIR, compress_name)

            if not nfs.exists(file_path):
                file_path = nfs.join(REPO_DIR, REPO_ANT_SPACENAME,
                                     compress_name)
                if not nfs.exists(file_path):
                    down_url = 'http://127.0.0.1:16600/file?filename={}'\
                        .format(compress_name)
                    client = AsyncHTTPClient(io_loop=ioloop.IOLoop.current())
                    response = yield client.fetch(
                        down_url,
                        connect_timeout=3600.0,
                        request_timeout=3600.0,
                        validate_cert=False)
                    if response.code != 200:
                        raise MessageError("Can't download pkg by http")
            yield self.do_ssh_cmd('umask 0027 && mkdir -p "{}"'.format(
                self.dst))
            yield self.ssh_client.scp(
                os.path.realpath(file_path), self.dst_name)
        except Exception as e:
            raise MessageError('Download agent pkg failed. {}'.format(e))

    @gen.coroutine
    def _install(self, agent_platform, compress_name):
        if not compress_name:
            raise MessageError("compress_name can't be empty")

        yield self.reporter.log_ok('Prepare environment for agent')

        if 'aix' in agent_platform:
            new_dst_name = re.sub('.gz$', '', compress_name)
            uncompress_cmd = 'gunzip {compress_name} && ' \
                             'tar xf {new_dst_name} && rm -rf {new_dst_name}'\
                .format(compress_name=compress_name, new_dst_name=new_dst_name)
        else:
            uncompress_cmd = \
                'tar mzxf {compress_name} && rm -f {compress_name}'\
                .format(compress_name=compress_name)
        cmd = 'umask 0027 && cd {dst} && {uncompress_cmd} '\
            .format(dst=self.dst, uncompress_cmd=uncompress_cmd)

        try:
            yield self.reporter.log_ok('Execute install cmd.')
            result = yield self.do_ssh_cmd(cmd)
            if result:
                yield self.reporter.log_ok(result)
        except Exception as e:
            logger.error(traceback.format_exc())
            yield self._rollback()
            raise MessageError('Execute install error on agent. {}'.format(e))

    @gen.coroutine
    def _do_bootstrap(self):
        cmd = 'cd {project_dir} && ./embedded/bin/python -m ' \
              'framework.actions.bootstrap --tenant {tenant} ' \
              '--network-domain {network_domain} --upstream {upstream} ' \
              '--ip {ip} --user {user}'.format(
                    project_dir=self.project_dir,
                    tenant=self.tenant,
                    network_domain=self.network_domain,
                    upstream=self.upstream,
                    ip=self.host,
                    user=self.runner)
        try:
            yield self.reporter.log_ok('Execute bootstrap cmd!')
            if self.user == 'root':
                result = yield self.do_ssh_cmd(cmd)
            else:
                result = yield self.do_ssh_cmd(
                    cmd, 'echo \'{}\' | sudo -p "" -S su -c '
                    .format(self.passwd))
            yield self.reporter.log_ok(result)
        except Exception as e:
            yield self._rollback()
            raise MessageError('Execute bootstrap error on agent. {}'.format(e))

    @gen.coroutine
    def _rollback(self):
        cmd = '[[ `ls {project_dir} |wc -l ` -gt 0 ]] && cd {project_dir} ' \
              '&& ./bin/remove.sh -f || rm -rf {project_dir}'\
              .format(project_dir=self.project_dir)
        try:
            yield self.reporter.log_ok('Roll back')
            yield self.do_ssh_cmd(cmd)
        except Exception as e:
            yield self.reporter.log_error('Roll back error. {}'.format(e))

    @gen.coroutine
    def get_agent_platform_detail(self):
        cmd = 'cd {} && ./embedded/bin/python -m framework.actions.info' \
              ''.format(self.project_dir)
        try:
            yield self.reporter.log_ok('Execute info cmd')
            result = yield self.do_ssh_cmd(cmd)
            yield self.reporter.log_ok(result, True)
        except Exception as e:
            yield self._rollback()
            raise MessageError('Execute info error on agent. {}'.format(e))


if __name__ == '__main__':
    message = sys.stdin.read()
    if not message:
        logger.error('The stdin is None, '
                     'please input the right args with stdin !!!')
        sys.exit(1)

    install = Install(json.loads(message))
    install.run()
