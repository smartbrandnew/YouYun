# coding=utf-8
import os

PACKAGE_NAME = {
    'agent': 'agent-{system_platform}-{system_bit}-{version}.{postfix}',
    'python': 'platform-ant-python-{version}.tar.gz',
    'node': 'platform-ant-node-{version}.tar.gz',
    'nginx': 'platform-ant-nginx-{version}.tar.gz',
    'manager': 'platform-ant-manager-{version}.tar.gz',
    'dispatcher': 'platform-ant-dispatcher-{version}.tar.gz',
    'discovery': ''
}

ANT_BUILD = '/home/project/ant-build'
PROJECT_ROOT = '/home/project/ant-build/projects'
DIST_ROOT = '/home/project/ant-build/dist'
TEMP_INSTALL_PATH = '/home/project/omp_temp'
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
WITH_MODULE = 'agent,discovery,jre,nmap'

BUILD_CMD = '{}  bin/build --build-project={} ' \
            '--branch={} --system={} --with-module={}'

INSTALL_CMD = "sh {} " \
              "--install-role=single " \
              "--local-ip=10.1.100.220 " \
              "--disconf-user uyun " \
              "--disconf-passwd  '>>>13a09adexsCdd3IfwWwFgIjobvZR7A==<<<' " \
              "--disconf-host  '10.1.100.110' " \
              "--running-user  'uyun' " \
              "--disconf-port  '8081'"

DEPLOY_MECHINE = {
    'ip': '10.1.100.220',
    'user': 'root',
    'passwd': 'ant123456'
}

SAVE_PACKAGE_MECHINE = {
    'ip': '10.1.100.104',
    'user': 'root',
    'passwd': 'ant123456'
}

DOCKER_MACHINE = {
    'ip': '10.1.100.109',
    'user': 'root',
    'passwd': 'ant123456'
}

VERSION = {
    'python': '2.7.13',
    'node': '7.8.0',
    'nginx': '1.8.1',
    'dispatcher': 'V2.0.{}',
    'manager': 'V2.0.{}'
}

INSTALL_DIR = {
    'nginx':'/opt/uyun/platform/nginx',
    'python':'/opt/uyun/platform/dispatcher/embedded',
    'node':'/opt/uyun/platform/dispatcher/node',
    'dispatcher':'/opt/uyun/platform/dispatcher',
    'manager':'/opt/uyun/platform/manager'
}

DOCKER_URL = 'http://dockerhub.uyuntest.cn:5000/v2/_catalog?n=10000'
TENANT_LOGIN_URL = "http://{}/tenant/api/v1/user/login"
GET_NETWORK_DOMAIN_URL = 'http://{}/ant/manager/api/v1/net-zone/query'
GET_AGENT_BY_IP_URL = 'http://{}/ant/manager/api/v1/store/get/agent'

DEFAULT_API_KEY = 'e10adc3949ba59abbe56e057f2gg88dd'

INSTALL_AGENT_API_FORMAT = 'http://{}/ant/manager/api/v1/agent/install'
INSTALL_AGENT_SUCCESS_MSG = u'Job下发成功'
DEFAULT_MANAGER_HOST = '10.1.100.110'

DEFAULT_AGENT_INSTALL_PATH = '/home/ant'

UNINSTALL_AGENT_API_FORMAT = 'http://{}/manager/api/v1/agent/uninstall'
UNINSTALL_AGENT_SUCCESS_MSG = u'Job下发成功'

UPGRADE_AGENT_API_FORMAT = 'http://{}/manager/api/v1/agent/upgrade'
UPGRADE_AGENT_SUCCESS_MSG = u'Job下发成功'

NETWORKDOMAIN = '59db94f725e84023bf02c397'

AGENT_INFO_API_FORMAT = 'http://{}/ant/manager/api/v1/agent/detail'

COMPUTE_OBJECT_INFO_API_FORMAT = 'http://{}/ant/manager/api/v1/store/get/compute-object'

