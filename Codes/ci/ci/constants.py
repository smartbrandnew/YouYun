# coding: utf-8
import os
import pf


#######################
#  System
#######################
platform = pf.get_platform()
system = platform.system.lower()

WINDOWS = 'win' in system
LINUX = 'Linux' in system
AIX = 'aix' in system
SUSE = 'suse' in platform.dist.lower()


#######################
#  PATH, DIR
#######################
ROOT = os.path.dirname(os.path.abspath(__file__))
CONFIG_PATH = os.path.join(ROOT, 'config.yml')

INSTALL_DIR = {
    'manager': '/opt/uyun/platform/manager',
    'manager-web': '/opt/uyun/platform/manager/front',
    'dispatcher': '/opt/uyun/platform/dispatcher'
}


#######################
#  Web Settings
#######################
PORT = 8000
TEST_TEMPLATE_URL = 'http://{}:{}/test'.format('{}', PORT)
DEPLOY_TEMPLATE_URL = 'http://{}:{}/deploy'.format('{}', PORT)
PYTHON_TEMPLATE_URL = 'http://10.1.100.100/interpreter/python/{}'


#######################
#  Others
#######################
AGENT_BUILD_TEMPLATE_CMD = '{python} bin/build --build-project=agent ' \
                           '--branch={branch} --system={system}'
PROGRESS_TEMPLATE_SIGN = '{:=^50}'
POSTFIX = {
    'windows': 'zip',
    'linux': 'tgz',
    'aix': 'tgz'
}
REPO_DIR = '{}/{}'.format(INSTALL_DIR['dispatcher'], 'repo')


#######################
#  Dingding
#######################
TOKEN = 'c19301d7d5c2f351db9a1c054d89e1ed75b31575c4ef5269917e51fbbd32213f'
URL = 'https://oapi.dingtalk.com/robot/send?access_token={}'.format(TOKEN)
CORP_ID = 'ding1aeb3554483ce275'
CORP_SECRET = 'Yu7PKg0Lhy8XVrgpys4Kq5_91bi6K6V-d3JnvgDD2PfqQ8lan3BXNGm4bXDX83FY'


MSG = {
    'msgtype': 'text',
    'text': {
        'content': u'{title}\n{text}'
    },
    'at': {
        'atMobiles': '{at_mobiles}',
        'isAtAll': False
    }
}

# centos5_64 '10.1.100.101'
# centos5_32
# suse10 '10.1.100.105'
# windows64 10.1.100.103
# windows32 10.1.100.102
# aix = 10.1.11.228

SSHHOST = '10.1.100.126'
SSHUSER = 'root'
