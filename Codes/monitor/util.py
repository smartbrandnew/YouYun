import logging
import math
import os
import platform
import re
import signal
import socket
import sys
import time
import types
import urllib2
from hashlib import md5
from netifaces import interfaces, ifaddresses, AF_INET, AF_LINK, AF_INET6

import simplejson as json
import sysutil
import yaml
from tornado import ioloop

try:
    from yaml import CLoader as yLoader
    from yaml import CDumper as yDumper
except ImportError:
    from yaml import Loader as yLoader
    from yaml import Dumper as yDumper

from utils.dockerutil import DockerUtil
from utils.platform import Platform
from utils.proxy import get_proxy
from utils.subprocess_output import get_subprocess_output

VALID_HOSTNAME_RFC_1123_PATTERN = re.compile(
    r"^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$")
MAX_HOSTNAME_LEN = 255
COLON_NON_WIN_PATH = re.compile(':(?!\\\\)')

log = logging.getLogger(__name__)

NumericTypes = (float, int, long)

distribution_names = ['centos', 'ubuntu', 'redhat', 'debian', 'suse', 'fedora', 'freebsd']
class_names = {
    'centos': 'CentOS',
    'ubuntu': 'Ubuntu',
    'redhat': 'RedHat',
    'debian': 'Debian',
    'suse': 'OpenSUSE',
    'fedora': 'Fedora',
    'freebsd': 'FreeBSD'
}


def get_os_version():
    from pf import get_platform
    return get_platform().dist


def get_win_system():
    import wmi
    c = wmi.WMI()
    raw_dist = c.Win32_OperatingSystem()[0].Caption
    dist = eval(re.sub(r"(\\x\w{2,4})", '', repr(raw_dist))).strip()
    if isinstance(dist, unicode):
        dist = dist.encode('utf-8')
    dist = dist.replace('Microsoft', '').strip()
    return dist


def plural(count):
    if count == 1:
        return ""
    return "s"


def get_tornado_ioloop():
    return ioloop.IOLoop.current()


def get_os():
    if sys.platform == 'darwin':
        return 'mac'
    elif sys.platform.find('freebsd') != -1:
        return 'freebsd'
    elif sys.platform.find('linux') != -1:
        return 'linux'
    elif sys.platform.find('win32') != -1:
        return 'windows'
    elif sys.platform.find('sunos') != -1:
        return 'solaris'
    else:
        return sys.platform


def headers(agentConfig):
    return {
        'User-Agent': 'Monitor Agent/%s' % agentConfig['version'],
        'Content-Type': 'application/x-www-form-urlencoded',
        'Accept': 'text/html, */*',
    }


def windows_friendly_colon_split(config_string):
    if Platform.is_win32():
        return COLON_NON_WIN_PATH.split(config_string)
    else:
        return config_string.split(':')


def getTopIndex():
    macV = None
    if sys.platform == 'darwin':
        macV = platform.mac_ver()

    if macV and macV[0].startswith('10.6.'):
        return 6
    else:
        return 5


def isnan(val):
    if hasattr(math, 'isnan'):
        return math.isnan(val)

    return str(val) == str(1e400 * 0)


def cast_metric_val(val):
    if not isinstance(val, NumericTypes):
        for cast in [int, float]:
            try:
                val = cast(val)
                return val
            except ValueError:
                continue
        raise ValueError
    return val


_IDS = {}


def get_next_id(name):
    global _IDS
    current_id = _IDS.get(name, 0)
    current_id += 1
    _IDS[name] = current_id
    return current_id


def is_valid_hostname(hostname):
    if len(hostname) > MAX_HOSTNAME_LEN:
        log.warning("Hostname: %s is too long (max length is  %s characters)" % (hostname, MAX_HOSTNAME_LEN))
        return False
    return True


def check_yaml(conf_path):
    with open(conf_path) as f:
        check_config = yaml.load(f.read(), Loader=yLoader)
        assert 'init_config' in check_config, "No 'init_config' section found"
        assert 'instances' in check_config, "No 'instances' section found"

        valid_instances = True
        if check_config['instances'] is None or not isinstance(check_config['instances'], list):
            valid_instances = False
        else:
            for i in check_config['instances']:
                if not isinstance(i, dict):
                    valid_instances = False
                    break
        if not valid_instances:
            raise Exception('You need to have at least one instance defined in the YAML file for this check')
        else:
            return check_config


def get_utf8(hostname, max_size=64):
    chars = []
    length = 0

    if not isinstance(hostname, unicode):
        hostname = hostname.decode('utf-8')

    for char in hostname:
        utf_char = char.encode('utf-8')
        length += len(utf_char)
        if length < max_size:
            chars.append(utf_char)
        else:
            break
    return "".join(chars)


def get_hostname(config=None):
    hostname = None

    if config is None:
        from config import get_config
        config = get_config(parse_args=True)
    config_hostname = config.get('hostname')
    if config_hostname and is_valid_hostname(config_hostname):
        print("type, {}".format(get_utf8(config_hostname)))
        return get_utf8(config_hostname)

    if hostname is None:
        gce_hostname = GCE.get_hostname(config)
        if gce_hostname is not None:
            if is_valid_hostname(gce_hostname):
                return gce_hostname[:64]

    docker_util = DockerUtil()
    if hostname is None and docker_util.is_dockerized():
        docker_hostname = docker_util.get_hostname()
        if docker_hostname is not None and is_valid_hostname(docker_hostname):
            hostname = docker_hostname[:64]

    if hostname is None:
        def _get_hostname_unix():
            try:
                out, _, rtcode = get_subprocess_output(['/bin/hostname', '-f'], log)
                if rtcode == 0:
                    return out.strip()
            except Exception:
                return None

        os_name = get_os()
        if os_name in ['mac', 'freebsd', 'linux', 'solaris']:
            unix_hostname = _get_hostname_unix()
            if unix_hostname and is_valid_hostname(unix_hostname):
                hostname = unix_hostname

    if (Platform.is_ecs_instance()) or (hostname is not None and EC2.is_default(hostname)):
        instanceid = EC2.get_instance_id(config)
        if instanceid:
            hostname = instanceid

    if hostname is None:
        try:
            socket_hostname = socket.gethostname()
        except socket.error:
            socket_hostname = None
        if socket_hostname and is_valid_hostname(socket_hostname):
            hostname = socket_hostname

    if hostname is None:
        log.critical('Unable to reliably determine host name. You can define one in datadog.conf or in your hosts file')
        raise Exception(
            'Unable to reliably determine host name. You can define one in datadog.conf or in your hosts file')
    else:
        return hostname


class GCE(object):
    URL = "http://169.254.169.254/computeMetadata/v1/?recursive=true"
    TIMEOUT = 0.1
    SOURCE_TYPE_NAME = 'google cloud platform'
    metadata = None
    EXCLUDED_ATTRIBUTES = ["kube-env", "startup-script", "sshKeys", "user-data",
                           "cli-cert", "ipsec-cert", "ssl-cert"]

    @staticmethod
    def _get_metadata(agentConfig):
        if GCE.metadata is not None:
            return GCE.metadata

        if not agentConfig['collect_instance_metadata']:
            log.info("Instance metadata collection is disabled. Not collecting it.")
            GCE.metadata = {}
            return GCE.metadata

        socket_to = None
        try:
            socket_to = socket.getdefaulttimeout()
            socket.setdefaulttimeout(GCE.TIMEOUT)
        except Exception:
            pass

        try:
            opener = urllib2.build_opener()
            opener.addheaders = [('X-Google-Metadata-Request', 'True')]
            GCE.metadata = json.loads(opener.open(GCE.URL).read().strip())

        except Exception:
            GCE.metadata = {}

        try:
            if socket_to is None:
                socket_to = 3
            socket.setdefaulttimeout(socket_to)
        except Exception:
            pass
        return GCE.metadata

    @staticmethod
    def get_tags(agentConfig):
        if not agentConfig['collect_instance_metadata']:
            return None

        try:
            host_metadata = GCE._get_metadata(agentConfig)
            tags = []

            for key, value in host_metadata['instance'].get('attributes', {}).iteritems():
                if key in GCE.EXCLUDED_ATTRIBUTES:
                    continue
                tags.append("%s:%s" % (key, value))

            tags.extend(host_metadata['instance'].get('tags', []))
            tags.append('zone:%s' % host_metadata['instance']['zone'].split('/')[-1])
            tags.append('instance-type:%s' % host_metadata['instance']['machineType'].split('/')[-1])
            tags.append('internal-hostname:%s' % host_metadata['instance']['hostname'])
            tags.append('instance-id:%s' % host_metadata['instance']['id'])
            tags.append('project:%s' % host_metadata['project']['projectId'])
            tags.append('numeric_project_id:%s' % host_metadata['project']['numericProjectId'])

            GCE.metadata['hostname'] = host_metadata['instance']['hostname'].split('.')[0]

            return tags
        except Exception:
            return None

    @staticmethod
    def get_hostname(agentConfig):
        try:
            host_metadata = GCE._get_metadata(agentConfig)
            hostname = host_metadata['instance']['hostname']
            if agentConfig.get('gce_updated_hostname'):
                return hostname
            else:
                return hostname.split('.')[0]
        except Exception:
            return None

    @staticmethod
    def get_host_aliases(agentConfig):
        try:
            host_metadata = GCE._get_metadata(agentConfig)
            project_id = host_metadata['project']['projectId']
            instance_name = host_metadata['instance']['hostname'].split('.')[0]
            return ['%s.%s' % (instance_name, project_id)]
        except Exception:
            return None


class EC2(object):
    EC2_METADATA_HOST = "http://169.254.169.254"
    METADATA_URL_BASE = EC2_METADATA_HOST + "/latest/meta-data"
    INSTANCE_IDENTITY_URL = EC2_METADATA_HOST + "/latest/dynamic/instance-identity/document"
    TIMEOUT = 0.1  # second
    DEFAULT_PREFIXES = [u'ip-', u'domu']
    metadata = {}

    class NoIAMRole(Exception):
        pass

    @staticmethod
    def get_iam_role():
        try:
            return urllib2.urlopen(EC2.METADATA_URL_BASE + "/iam/security-credentials/").read().strip()
        except urllib2.HTTPError as err:
            if err.code == 404:
                raise EC2.NoIAMRole()
            raise

    @staticmethod
    def is_default(hostname):
        hostname = hostname.lower()
        for prefix in EC2.DEFAULT_PREFIXES:
            if hostname.startswith(prefix):
                return True
        return False

    @staticmethod
    def get_tags(agentConfig):
        if not agentConfig['collect_instance_metadata']:
            log.info("Instance metadata collection is disabled. Not collecting it.")
            return []

        EC2_tags = []
        socket_to = None
        try:
            socket_to = socket.getdefaulttimeout()
            socket.setdefaulttimeout(EC2.TIMEOUT)
        except Exception:
            pass

        try:
            iam_role = EC2.get_iam_role()
            iam_params = json.loads(urllib2.urlopen(
                EC2.METADATA_URL_BASE + "/iam/security-credentials/" + unicode(iam_role)).read().strip())
            instance_identity = json.loads(urllib2.urlopen(EC2.INSTANCE_IDENTITY_URL).read().strip())
            region = instance_identity['region']

            import boto.ec2
            proxy_settings = get_proxy(agentConfig) or {}
            connection = boto.ec2.connect_to_region(
                region,
                aws_access_key_id=iam_params['AccessKeyId'],
                aws_secret_access_key=iam_params['SecretAccessKey'],
                security_token=iam_params['Token'],
                proxy=proxy_settings.get('host'), proxy_port=proxy_settings.get('port'),
                proxy_user=proxy_settings.get('user'), proxy_pass=proxy_settings.get('password')
            )

            tag_object = connection.get_all_tags({'resource-id': EC2.metadata['instance-id']})

            EC2_tags = [u"%s:%s" % (tag.name, tag.value) for tag in tag_object]
            if agentConfig.get('collect_security_groups') and EC2.metadata.get('security-groups'):
                EC2_tags.append(u"security-group-name:{0}".format(EC2.metadata.get('security-groups')))

        except EC2.NoIAMRole:
            log.warning(
                u"Unable to retrieve AWS EC2 custom tags: "
                u"an IAM role associated with the instance is required"
            )
        except Exception:
            log.exception("Problem retrieving custom EC2 tags")

        try:
            if socket_to is None:
                socket_to = 3
            socket.setdefaulttimeout(socket_to)
        except Exception:
            pass

        return EC2_tags

    @staticmethod
    def get_metadata(agentConfig):
        if not agentConfig['collect_instance_metadata']:
            log.info("Instance metadata collection is disabled. Not collecting it.")
            return {}

        socket_to = None
        try:
            socket_to = socket.getdefaulttimeout()
            socket.setdefaulttimeout(EC2.TIMEOUT)
        except Exception:
            pass

        for k in (
                'instance-id', 'hostname', 'local-hostname', 'public-hostname', 'ami-id', 'local-ipv4', 'public-keys/',
                'public-ipv4', 'reservation-id', 'security-groups'):
            try:
                v = urllib2.urlopen(EC2.METADATA_URL_BASE + "/" + unicode(k)).read().strip()
                assert type(v) in (types.StringType, types.UnicodeType) and len(v) > 0, "%s is not a string" % v
                EC2.metadata[k.rstrip('/')] = v
            except Exception:
                pass

        try:
            if socket_to is None:
                socket_to = 3
            socket.setdefaulttimeout(socket_to)
        except Exception:
            pass

        return EC2.metadata

    @staticmethod
    def get_instance_id(agentConfig):
        try:
            return EC2.get_metadata(agentConfig).get("instance-id", None)
        except Exception:
            return None


class Watchmonitor(object):
    def __init__(self, duration, max_mem_mb=None):
        import resource

        self._duration = int(duration)
        signal.signal(signal.SIGALRM, Watchmonitor.self_destruct)

        if max_mem_mb is not None:
            self._max_mem_kb = 1024 * max_mem_mb
            max_mem_bytes = 1024 * self._max_mem_kb
            resource.setrlimit(resource.RLIMIT_AS, (max_mem_bytes, max_mem_bytes))
            self.memory_limit_enabled = True
        else:
            self.memory_limit_enabled = False

    @staticmethod
    def self_destruct(signum, frame):
        try:
            log.error("Self-destructing...")
        finally:
            os.kill(os.getpid(), signal.SIGKILL)

    def reset(self):
        if self.memory_limit_enabled:
            mem_usage_kb = int(os.popen('ps -p %d -o %s | tail -1' % (os.getpid(), 'rss')).read())
            if mem_usage_kb > (0.95 * self._max_mem_kb):
                Watchmonitor.self_destruct(signal.SIGKILL, sys._getframe(0))

        log.debug("Resetting watchmonitor for %d" % self._duration)
        signal.alarm(self._duration)


class LaconicFilter(logging.Filter):
    LACONIC_MEM_LIMIT = 1024

    def __init__(self, name=""):
        logging.Filter.__init__(self, name)
        self.hashed_messages = {}

    def hash(self, msg):
        return md5(msg).hexdigest()

    def filter(self, record):
        try:
            h = self.hash(record.getMessage())
            if h in self.hashed_messages:
                return 0
            else:
                if len(self.hashed_messages) >= LaconicFilter.LACONIC_MEM_LIMIT:
                    self.hashed_messages.clear()
                self.hashed_messages[h] = True
                return 1
        except Exception:
            return 1


class Timer(object):
    def __init__(self):
        self.start()

    def _now(self):
        return time.time()

    def start(self):
        self.started = self._now()
        self.last = self.started
        return self

    def step(self):
        now = self._now()
        step = now - self.last
        self.last = now
        return step

    def total(self, as_sec=True):
        return self._now() - self.started


def chunks(iterable, chunk_size):
    iterable = iter(iterable)
    while True:
        chunk = [None] * chunk_size
        count = 0
        try:
            for _ in range(chunk_size):
                chunk[count] = iterable.next()
                count += 1
            yield chunk[:count]
        except StopIteration:
            if count:
                yield chunk[:count]
            break


def get_network_info():
    network_info = dict()
    for ifaceName in interfaces():
        macaddress = ''
        ipaddress = ''
        ipaddressv6 = ''
        netinfo = ifaddresses(ifaceName)
        if netinfo.has_key(AF_LINK):
            macaddress = netinfo[AF_LINK][0]['addr'].replace(":", "-")
        if netinfo.has_key(AF_INET):
            ipaddress = netinfo[AF_INET][0]['addr']
        if netinfo.has_key(AF_INET6):
            ipaddressv6 = netinfo[AF_INET6][0]['addr']
        if ipaddress and ipaddress != "127.0.0.1":
            network_info[ipaddress] = {"ipaddress": ipaddress, "ipaddressv6": ipaddressv6, "macddress": macaddress}
            network_info[ipaddressv6] = \
                {"ipaddress": ipaddress, "ipaddressv6": ipaddressv6, "macddress": macaddress}
    return network_info


def get_network_metadata(
        config,
        gohai_metadata,
        network_info,
        log,
        is_json=False):
    gohai_data = json.loads(gohai_metadata)
    try:
        gohai_data['platform']['machine_type'] = get_machine_type()
        gohai_data['platform']['os'] = get_os_version()
    except:
        gohai_data['platform'] = {}
        gohai_data['platform']['machine_type'] = get_machine_type()
        gohai_data['platform']['os'] = get_os_version()
        gohai_data['platform']['kernel_name'] = platform.uname()[0]
        gohai_data['platform']['kernel_release'] = platform.uname()[3]
        gohai_data['platform']['hostname'] = platform.node()

    ip = config.get('ip', '')
    if ip and ip in network_info.keys():
        gohai_data['network'].update(network_info[ip])
        log.info("had used the ip that User config")
    elif not ip:
        log.info("User not config the ip")
    else:
        log.info("the ip that User config wrong format or computer hasn't this ip ")
    if is_json:
        return json.dumps(gohai_data)
    else:
        return gohai_data


def get_machine_type():
    if sysutil.is_virtual_machine():
        return 'VM'
    return 'PM'


def get_ip(config):
    config_ip = config.get('ip', None)
    if config_ip:
        log.debug('get ip from datamonitor.conf')
        return config_ip

    env_ip = os.environ.get('ANT_AGENT_IP')
    if env_ip:
        log.debug('get ip from env ANT_AGENT_IP')
        return env_ip

    log.debug('get ip from env sysutil.main_ip()')
    return sysutil.main_ip()


def get_uuid():
    from config import get_config
    agentconfig = get_config()
    agentuuid = agentconfig.get("uuid", '')
    if agentuuid:
        return agentuuid
    return os.environ.get('ANT_AGENT_ID')


def decrypted(encrypted_passwd):
    import requests
    from config import get_config
    config = get_config(parse_args=False)
    url = config.get('m_url').replace('api/v2/gateway/dd-agent',
                                      'api/v2/agent/config/encryption/decrypt?apikey=') + config.get('api_key')
    param = {'encryptPwd': encrypted_passwd}
    r = requests.post(url, json=param)
    decrypted_passwd = str(r.text)
    return decrypted_passwd


def convert_to_str(input):
    if isinstance(input, dict):
        return {convert_to_str(key): convert_to_str(value) for key, value in input.iteritems()}
    elif isinstance(input, list):
        return [convert_to_str(element) for element in input]
    elif isinstance(input, unicode):
        return input.encode('utf-8')
    else:
        return input
