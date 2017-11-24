# coding: utf-8
import os
import re
import yaml
import nfs
import locale
from subprocess import Popen, PIPE, STDOUT

ip_regex = re.compile(
    '^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$')

upstream_regex = re.compile(
    r'^(?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9]'
    r'[-a-zA-Z0-9]{0,62})+$|(?:\d{1,3}\.){3}\d{1,3}(?![\.\d])(:\d+)?')

upstream_ip_regex = re.compile(
    r"(?<=SSH_CLIENT=)"
    r"((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)",)

_agents_info = {'names_map': {}, 'versions': {}}

IS_WINDOWS = os.name == 'nt'
DEFAULT_CODING = locale.getpreferredencoding()


def get_agent_version():
    from constants import ROOT_DIR
    version = ''
    module_yaml = nfs.join(ROOT_DIR, 'manifest.yaml')
    with open(module_yaml) as temp:
        module_mes = yaml.load(temp.read())
        if 'version' in module_mes:
            version = module_mes['version']
    return version


def get_agent_filename(agent_platform, version='latest'):
    from constants import COMMON_POSTFIX, WIN_POSTFIX, AGENT_NAME
    postfix = WIN_POSTFIX if 'windows' in agent_platform else COMMON_POSTFIX
    compress_name = '{agent_name}-{agent_platform}-{version}.{POSTFIX}' \
        .format(agent_name=AGENT_NAME,
                agent_platform=agent_platform,
                version=version,
                POSTFIX=postfix)
    return compress_name


def get_agent_ip(des_ip, port):
    try:
        import socket
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect((des_ip, port))
        ip = s.getsockname()[0]
    finally:
        s.close()
    return ip


def ip_validate(ip):
    if ip_regex.match(ip):
        return True
    else:
        return False


def get_upstream_ip(ssh_str):
    if not ssh_str:
        return None
    result = upstream_ip_regex.search(ssh_str)
    if result:
        result = upstream_regex.search((result.group(0)))
        if result:
            return result.group(0)
    return None


def upstream_validate(upstream):
    upstream_mes = upstream_regex.search(upstream)
    if upstream_mes:
        upstream_mes = upstream_mes.group(0)
        if ':' in upstream_mes:
            return upstream_mes.split(':')
        return [upstream_mes, 80]
    else:
        return None


def to_int(s, default):
    return int(s) if s else default


def get_agent_config():
    from constants import CONF_PATH
    if not nfs.exists(CONF_PATH):
        return {}

    with open(CONF_PATH) as temp:
        agent_mes = yaml.load(temp.read())
        return agent_mes


def get_env():
    key_dict = {
        'id': 'ANT_AGENT_ID',
        'ip': 'ANT_AGENT_IP',
        'network_domain': 'ANT_NETWORK_DOMAIN',
        'upstream': 'ANT_UPSTREAM'
    }
    conf_dict = get_agent_config()
    for key, value in key_dict.items():
        if key in conf_dict and conf_dict[key]:
            os.environ.setdefault(value, conf_dict[key])


def execute(cmd, cwd=None, wait=True, env=None):
    env = os.environ if not env else env
    p = Popen(cmd, shell=True, stdout=PIPE, stderr=STDOUT, cwd=cwd, env=env)
    if not wait:
        return

    out, _ = p.communicate()
    return p.returncode, out


def to_string(s):
    if isinstance(s, unicode):
        return s.encode('utf-8')
    return s


def de_duplication(s):
    if not s:
        return s
    s = s.split('\n')
    return '\n'.join(set(s))
