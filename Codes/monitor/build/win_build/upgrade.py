# coding: utf-8

import json
import logging
import os
import re
import shutil
import sys
import tempfile
import time
import traceback
import uuid
import zipfile
from subprocess import PIPE, STDOUT, Popen
from urlparse import urljoin

import psutil
import requests

from config import (
    _windows_config_path,
    _windows_confd_path,
    get_config
)

LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'verbose': {
            'format':
                '[%(levelname)s][%(asctime)s][%(module)s][%(process)d] %(message)s'
        },
    },
    'handlers': {
        'console': {
            'level': 'INFO',
            'class': 'logging.StreamHandler',
            'formatter': 'verbose'
        },
        'rotating': {
            'level': 'INFO',
            'class': 'logging.handlers.RotatingFileHandler',
            'filename': os.path.join(os.getcwd(), 'upgrade_to_ant.log'),
            'maxBytes': 1024 * 1024 * 5,
            'backupCount': 5,
            'formatter': 'verbose',
        },
    },
    'loggers': {
        'upgrade': {
            'handlers': ['console', 'rotating'],
            'level': 'INFO',
            'propagate': False,
        },
    }
}

upstream_regex = re.compile(
    r'^(?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9]'
    r'[-a-zA-Z0-9]{0,62})+$|(?:\d{1,3}\.){3}\d{1,3}(?![\.\d])(:\d+)?')

logging.config.dictConfig(LOGGING)
logger = logging.getLogger('upgrade')


class Reporter(object):
    def __init__(self, upstream):
        self.upstream = upstream
        self.upgrade_url = urljoin(upstream, '/ant/dispatcher/update')
        self.task_id = uuid.uuid4().hex
        self._get_network_info()
        self._get_ip()

    def _get_network_info(self):
        info = []
        for name, snics in psutil.net_if_addrs().iteritems():
            name = name.lower()
            if 'loopback' in name or 'vmware' in name or 'virtual' in name or 'tunneling' in name:
                continue
            ipv4 = None
            for snic in snics:
                if snic.family == 2:
                    ipv4 = snic.address
            if not ipv4 or (ipv4 and ipv4.startswith(('169', '127.0.0.1'))):
                continue
            info.append(ipv4)
        self.ips = info

    @staticmethod
    def upstream_validate(upstream):
        upstream_mes = upstream_regex.search(upstream)
        if upstream_mes:
            upstream_mes = upstream_mes.group(0)
            if ':' in upstream_mes:
                return upstream_mes.split(':')
            return [upstream_mes, 80]
        else:
            return None

    def _get_ip(self):
        upstream_mes = self.upstream_validate(self.upstream)
        if not upstream_mes:
            logger.info('Upstream is worng')
            sys.exit(1)
        try:
            import socket
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.connect((upstream_mes[0], int(upstream_mes[1])))
            ip = s.getsockname()[0]
        finally:
            s.close()
        if not ip:
            logger.info('Get ip failed!')
            sys.exit(1)
        self.ip = ip

    def post_message(self, msg, state=0):
        body = {
            'id': self.task_id,
            'ip': self.ip,
            'ips': self.ips,
            'module': 'local-monitor',
            'message': msg,
            'state': state
        }
        try:
            logging.info(msg)
            r = requests.post(self.upgrade_url, json=body, verify=False)
            if r.status_code != 200:
                logger.error('Post task log:{} failed, code {} !!!'
                             .format(body, r.status_code))
        except Exception as e:
            logger.error('Post task log:{} failed, exception: {}!!!'
                         .format(body, e))


def unpack_tar(filename, destpath):
    if not os.path.exists(filename):
        return False

    if not os.path.exists(destpath):
        os.mkdir(destpath, 0777)

    zfobj = zipfile.ZipFile(filename)
    if destpath.endswith(':'):
        destpath = destpath + os.sep

    zfobj.extractall(path=destpath)
    zfobj.close()
    return os.path.join(destpath, 'agent')


class BaseModule(object):
    def __init__(self, tenant, upstream):
        self.tenant = tenant
        self.upstream = upstream
        self.module_cmd = ''
        self.exec_environ = os.environ
        self.reporter = Reporter(self.upstream)
        self._get_install_conf()

    def _get_install_conf(self):
        self.config_dict = get_config(parse_args=False)
        m_url = self.config_dict.get('m_url', None)
        if not self.upstream:
            m_url = re.sub('/monitor/api/v2/gateway/dd-agent', '', m_url)
            if not m_url:
                self.reporter.post_message('Get upstream failed!!', 2)
                self.finish_install()
            self.upstream = m_url

    def _get_path(self):
        source_path = os.path.dirname(os.path.abspath(__file__))
        agent_path = re.sub('agent/checks.d', '', source_path)
        if not agent_path:
            self.reporter.post_message('Get path error!!!', 2)
            self.finish_install()
        self.agent_dir = os.path.abspath(
            os.path.join(agent_path, os.path.pardir, 'uyun-ant'))
        if not os.path.exists(self.agent_dir):
            os.makedirs(self.agent_dir)

    def get_agent_name(self, install_path):
        pkg_name = 'agent-windows-32-latest.zip'
        python_path = os.path.normpath(
            os.path.join(install_path, 'agent', 'embedded', 'python'))

        install_cmd = r'"{}" -m framework.actions.bootstrap --tenant {} ' \
                      '--upstream {}'.format(python_path, self.tenant,
                                             self.upstream)
        return pkg_name, install_cmd

    def get_module_cmd(self, uncompress_path):
        cmd = r'"{}" -m framework.actions.module.install'
        return cmd.format(os.path.normpath(
            os.path.join(uncompress_path, 'embedded', 'python')))

    @staticmethod
    def finish_install(status=1):
        sys.exit(status)

    def check_agent_service(self, flag=True):
        cmd = 'sc query ant-agent status |find /c "RUNNING"'

        output, status = self.do_cmd(cmd)
        if status != 0 or output.strip() != '1':
            status = 0 if flag else 2
            self.reporter.post_message('ant-agent is not running', status)
            return False
        self.reporter.post_message('ant-agent is running')
        return True

    def get_running_cwd(self):
        try:
            import wmi
            s = wmi.WMI().Win32_Service(name='agent')[0]
            path = '\\'.join(s.Pathname.split('\\')[:-2])
            return path
        except Exception:
            self.reporter.post_message('Get ant path failed', 2)
            self.finish_install()

    def install_ant_agent(self, install_path):
        pkg_name, install_cmd = self.get_agent_name(install_path)
        agent_pkg = self.down_pkg(pkg_name, install_path)
        uncompress_path = self.uncompress_file(agent_pkg, install_path)
        output, status = self.do_cmd(install_cmd, uncompress_path)
        if status != 0:
            self.reporter.post_message(
                'Install ant-agent failed:{}'.format(output, 2))
            self.finish_install()

        os.remove(os.path.join(install_path, pkg_name))
        if not self.check_agent_service(False):
            self.finish_install()

        self.reporter.post_message('Install ant-agent success', 2)
        return uncompress_path

    def down_pkg(self, pkg_name, file_path):
        self.reporter.post_message('Begin to download {}'.format(pkg_name))
        dst_name = os.path.normpath(os.path.join(file_path, pkg_name))
        file_url = urljoin(urljoin(self.upstream, 'ant/file/'), pkg_name)
        logger.info('download pkg url: {}'.format(file_url))
        time.sleep(15)
        r = requests.get(file_url, stream=True, verify=False)
        with open(dst_name, 'wb') as code:
            for chunk in r.iter_content(chunk_size=10240000):
                if chunk:
                    code.write(chunk)
        if r.status_code != 200:
            self.reporter.post_message('Download failed!')
            self.finish_install()

        self.reporter.post_message('Finish download!')
        return dst_name

    def do_cmd(self, cmd, cwd=None, std_in=None, wait=True):
        from subprocess import CREATE_NEW_CONSOLE
        if wait:
            proc = Popen(cmd, stdout=PIPE, stderr=STDOUT, stdin=PIPE,
                         shell=True, cwd=cwd, env=self.exec_environ,
                         creationflags=CREATE_NEW_CONSOLE)
        else:
            cmd = r'echo {} | {}'.format(json.dumps(std_in), cmd)
            proc = Popen(cmd, shell=True, cwd=cwd, close_fds=True,
                         env=self.exec_environ,
                         creationflags=CREATE_NEW_CONSOLE)

        logger.info('Cmd: {}, Cwd: {}'.format(cmd, cwd))
        if std_in and proc.stdin:
            proc.stdin.write(json.dumps(std_in))

        if not wait:
            return [], 0

        result, _ = proc.communicate()
        if proc.wait() != 0:
            logger.error(result)
        return result, proc.wait()

    def uncompress_file(self, agent_pkg, uncompress_dir):
        try:
            self.reporter.post_message(
                'Begin to uncompress {}'.format(agent_pkg))
            uncompress_path = unpack_tar(agent_pkg, uncompress_dir)
            if not isinstance(uncompress_path, basestring):
                if not uncompress_path:
                    self.reporter.post_message(
                        'uncompress {} failed'.format(agent_pkg), 2)
                    self.finish_install()
            uncompress_path = os.path.join(uncompress_dir, uncompress_path)

            if not os.path.exists(uncompress_path):
                self.reporter.post_message(
                    'Uncompress agent pkg failed, {}, {}'
                        .format(uncompress_dir, uncompress_path), 2)
                self.finish_install()
            self.reporter.post_message(
                'Uncompress {} success!'.format(agent_pkg))
            return uncompress_path
        except Exception as e:
            self.reporter.post_message(
                'Uncompress agent pkg failed, {}'.format(e), 2)
            self.finish_install()

    def install_ant(self, install_path):
        try:
            if self.check_agent_service():
                uncompress_path = self.get_running_cwd()
                if not os.path.exists(uncompress_path):
                    self.reporter.post_message('The path of agent is empty', 2)
                    self.finish_install()
                self.reporter.post_message(
                    'The path of agent is {}'.format(uncompress_path))
            else:
                uncompress_path = \
                    self.install_ant_agent(install_path)
            return uncompress_path
        except Exception as e:
            self.reporter.post_message(
                'Install agent failed: {}'.format(e), 2)
            return None

    def copy_conf(self):
        temp_dir = os.path.join(tempfile.gettempdir(), 'monitor_update_temp')
        if os.path.exists(temp_dir):
            shutil.rmtree(temp_dir)

        confd_path = _windows_confd_path()
        temp_confd_path = os.path.join(temp_dir, 'conf.d')
        shutil.copytree(confd_path, temp_confd_path)

        conf_path = _windows_config_path()
        if os.path.isfile(conf_path):
            shutil.copy2(conf_path,
                         os.path.join(temp_dir, os.path.basename(conf_path)))
        else:
            shutil.copytree(conf_path, temp_dir)

    def install(self):
        try:
            self.copy_conf()
            uncompress_path = self.install_ant(self.agent_dir)
            if not uncompress_path:
                shutil.rmtree(self.agent_dir)
                self.finish_install()

            std_in = [{"module_name": "local-monitor",
                       "filename": self.module_pkg
                       }]
            self.exec_environ.update({'ANT_TASK_ID': "-1"})
            output, status = \
                self.do_cmd(self.get_module_cmd(uncompress_path),
                            uncompress_path,
                            std_in=std_in)
            if status != 0:
                self.reporter.post_message(
                    'Install local-monitor failed!', 2)
                self.finish_install(status)
            else:
                self.reporter.post_message(
                    'Install local-monitor success!', 1)
                self.finish_install(0)
        except Exception as e:
            logger.error(traceback.format_exc())
            self.reporter.post_message(
                'Install local-monitor failed: {}'.format(e), 2)
            self.finish_install()


def do_install():
    agent_module = BaseModule()
    agent_module.install()


if __name__ == '__main__':
    do_install()
