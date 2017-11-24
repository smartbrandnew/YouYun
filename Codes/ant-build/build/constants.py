import os
from os.path import dirname, join
from pf import get_platform


IS_WINDOWS = os.name == 'nt'

PLATFORM = get_platform()
SYSTEM = PLATFORM.system.lower()
DIST = PLATFORM.dist.lower()

ROOT_DIR = dirname(dirname(__file__))
PROJECT_ROOT = join(ROOT_DIR, 'projects')
DIST_ROOT = join(ROOT_DIR, 'dist')
BIN_ROOT = join(ROOT_DIR, 'bin')
SAVE_DIR = '/home/repo/develop/ant'

EXCLUDE_DIRS = ('tests', 'test', 'requirements')
EXCLUDE_FILES = ('setup.py', 'README.md', 'CHANGELOG.md', 'MANIFEST.in',
                 'tox.ini')
                 
MODULE_PROJECT = ['node', 'python', 'nginx', 'dispatcher', 'manager', 'manager-web']
AGENT_PROJECT = ['agent', 'openresty']
PLUGIN_PROJECT = ['discovery', 'monitor', 'automation']

AGENT_NAME = 'agent'
DISPATCHER_NAME = 'dispatcher'
MANAGER_NAME = 'manager'
MANAGERWEB_NAME = 'manager-web'
PKG_NAME = 'platform-ant'
GLOBAL_VERSION = 'V2.0'

REPO_URL = {
    'agent':'http://10.1.100.100/develop/ant/agent/',
    'jre':'http://10.1.100.100/modules/',
    'nmap':'http://10.1.100.100/modules/',
    'discovery':'http://10.1.100.100/modules/',
    'jdk':'http://10.1.100.100/modules/',
}

REPO_NAME = {
    'agent':[
    'agent-windows-64-{}.zip',
    'agent-windows-32-{}.zip',
    'agent-linux-64-{}.tar.gz',
    ],
    'jre':[
    'jre@{}@Windows64.tar.gz',
    'jre@{}@Linux64.tar.gz'
    ],
    'discovery':[
    'remote-discovery@{}.tar.gz'
    ],
    'nmap':[
    'nmap@{}@Linux.tar.gz',
    'nmap@{}@Windows.tar.gz'
], 
}

PYTHON_URL='http://10.1.100.100/interpreter/python/{}.{}'

