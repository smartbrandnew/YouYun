# coding: utf-8
import os
from os import path

IS_WINDOWS = os.name == 'nt'
IS_VIRTUAL_CMD = 'dmidecode -s system-product-name || dmesg | ' \
                 'grep -iE "KVM|VMware|Virtual Machine|hvm"'

KNOWN_DISTRIBUTION = \
    "Debian|Ubuntu|Red Hat|CentOS|openSUSE|Amazon|Arista|SUSE|Turbo|AIX|RedHat"

SPECIAL_PLATFORM_LIST = ['suse10']

INSTALL_PARALLEL_NUM = 10
START_AGENT_TIMEOUT = 20
STOP_AGENT_TIMEOUT = 20
TIME_TICK = 5
UPSTREAM_SUFFIX = 'ant/'
COMMON_POSTFIX = 'tar.gz'
WIN_POSTFIX = 'zip'
NGINX_PORT = '16603'
REPO_ANT_SPACENAME = 'ant'

# Directory or Path
ROOT_DIR = os.getcwd()
REPO_DIR = os.path.join(ROOT_DIR, 'repo')
BIN_DIR = path.join(ROOT_DIR, 'bin')
CIRCLE_CONF_DIR = path.join(ROOT_DIR, 'proc')
LOG_DIR = path.join(ROOT_DIR, 'logs')

CONF_PATH = path.join(ROOT_DIR, 'config.yaml')
CIRCLED_PATH = path.join(BIN_DIR, 'circled')
CIRCLECTL_PATH = path.join(BIN_DIR, 'circlectl')
CIRCLE_LOG_PATH = path.join(ROOT_DIR, 'logs', 'circle.log')
PYTHON_DIR = path.join(ROOT_DIR, 'embedded')
PYTHON = path.join(PYTHON_DIR, '' if IS_WINDOWS else 'bin', 'python')
UPGRADE_PYTHON_DIR = path.join(ROOT_DIR, '.embedded')
UPGRADE_PYTHON = path.join(UPGRADE_PYTHON_DIR, ''
                           if IS_WINDOWS else 'bin', 'python')

AGENT_NAME = 'agent'
SERVICE_NAME = 'ant-agent'
UPGRADE_SERVICE_NAME = 'ant-upgrade'
SERVICE_FILE = path.join(ROOT_DIR, 'templates', SERVICE_NAME)
SERVICE_TEMPLATE = path.join(ROOT_DIR, 'templates', 'agent-template')
UPGRADE_SERVICE_FILE = path.join(ROOT_DIR, 'templates', UPGRADE_SERVICE_NAME)
UPGRADE_SERVICE_TEMPLATE = path.join(ROOT_DIR, 'templates',
                                     'agent-upgrade-template')
SERVICE_PATH = path.join('/etc', 'init.d')
NGINX_CONF = path.join(ROOT_DIR, 'openresty', 'conf', 'nginx.conf') \
    if IS_WINDOWS else path.join(ROOT_DIR, 'openresty', 'nginx', 'conf', 'nginx.conf')
BOOT_SCRIPT = path.join('/etc', 'rc.local')
DESCRIPTION = {
    'like_debian': '### BEGIN INIT INFO\n'
    '# Provides:          {service_name}\n'
    '# Required-Start:    $remote_fs\n'
    '# Required-Stop:     $remote_fs\n'
    '# Default-Start:     2 3 4 5\n'
    '# Default-Stop:      0 1 6\n'
    '# Short-Description: {service_name} manager\n'
    '# Description:       Manager the {service_name} '
    'process\n'
    '### END INIT INFO\n',
    'like_redhat': '#\n'
    '# {service_name}       '
    'Bring up/down {service_name}\n'
    '#\n'
    '# chkconfig: 2345 10 90\n'
    '# description: Activates/Deactivates {service_name}'
    ' Process\n'
    '#\n'
    '### BEGIN INIT INFO\n'
    '# Description: Bring up/down {service_name}\n'
    '### END INIT INFO'
}

SYSTEM_SCRIPT_TYPE = '.bat' if IS_WINDOWS else '.sh'
BIN_START = path.join(ROOT_DIR, 'bin', 'start' + SYSTEM_SCRIPT_TYPE)
BIN_STOP = path.join(ROOT_DIR, 'bin', 'stop' + SYSTEM_SCRIPT_TYPE)
UPGRADE_BIN_START = path.join(ROOT_DIR, 'bin',
                              'start_upgrade' + SYSTEM_SCRIPT_TYPE)

VERSION_FILE_NAME = 'version.txt'
VERSION_FILE_PATH = path.join(ROOT_DIR, VERSION_FILE_NAME)
