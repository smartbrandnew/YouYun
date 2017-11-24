import ConfigParser
import glob
import imp
import inspect
import itertools
import logging
import logging.config
import logging.handlers
import os
import platform
import re
import stat
import string
import sys
from cStringIO import StringIO
from optparse import OptionParser, Values
from socket import gaierror, gethostbyname
from urlparse import urlparse

import yaml

from util import get_os, yLoader
from utils.platform import Platform
from utils.proxy import get_proxy
from utils.subprocess_output import (
    get_subprocess_output,
    SubprocessOutputEmptyError,
)

AGENT_VERSION = "2.6.2"
DATAMONITOR_CONF = "datamonitor.conf"
MAC_CONFIG_PATH = '/opt/datadog-agent/etc'
DEFAULT_CHECK_FREQUENCY = 15
LOGGING_MAX_BYTES = 5 * 1024 * 1024

log = logging.getLogger(__name__)

OLD_STYLE_PARAMETERS = [
    ('apache_status_url', "apache"),
    ('cacti_mysql_server', "cacti"),
    ('couchdb_server', "couchdb"),
    ('elasticsearch', "elasticsearch"),
    ('haproxy_url', "haproxy"),
    ('hudson_home', "Jenkins"),
    ('memcache_', "memcached"),
    ('mongodb_server', "mongodb"),
    ('mysql_server', "mysql"),
    ('nginx_status_url', "nginx"),
    ('postgresql_server', "postgres"),
    ('redis_urls', "redis"),
    ('varnishstat', "varnish"),
    ('WMI', "WMI"),
]

NAGIOS_OLD_CONF_KEYS = [
    'nagios_log',
    'nagios_perf_cfg'
]

LEGACY_DATAMONITOR_URLS = [
    "app.datamonitorhq.com",
    "app.datad0g.com",
]


class PathNotFound(Exception):
    pass


def get_parsed_args():
    parser = OptionParser()
    parser.add_option('-A', '--autorestart', action='store_true', default=False,
                      dest='autorestart')
    parser.add_option('-d', '--m_url', action='store', default=None,
                      dest='m_url')
    parser.add_option('-u', '--use-local-forwarder', action='store_true',
                      default=False, dest='use_forwarder')
    parser.add_option('-n', '--disable-dd', action='store_true', default=False,
                      dest="disable_dd")
    parser.add_option('-v', '--verbose', action='store_true', default=False,
                      dest='verbose',
                      help='Print out stacktraces for errors in checks')
    parser.add_option('-p', '--profile', action='store_true', default=False,
                      dest='profile', help='Enable Developer Mode')

    try:
        options, args = parser.parse_args()
    except SystemExit:
        options, args = Values({'autorestart': False,
                                'm_url': None,
                                'disable_dd': False,
                                'use_forwarder': False,
                                'verbose': False,
                                'profile': False}), []
    return options, args


def get_version():
    return AGENT_VERSION


def get_url_endpoint(default_url, endpoint_type='app'):
    parsed_url = urlparse(default_url)
    if parsed_url.netloc not in LEGACY_DATAMONITOR_URLS:
        return default_url

    subdomain = parsed_url.netloc.split(".")[0]

    return default_url.replace(subdomain,
                               "{0}-{1}.agent".format(
                                   get_version().replace(".", "-"),
                                   endpoint_type))


def skip_leading_wsp(f):
    return StringIO("\n".join(map(string.strip, f.readlines())))


def _windows_commondata_path():
    path = os.path.dirname(os.path.realpath(__file__))
    return path


def _windows_config_path():
    common_data = _windows_commondata_path()
    return _config_path(os.path.join(common_data))


def _windows_confd_path():
    common_data = _windows_commondata_path()
    return _confd_path(os.path.join(common_data))


def _windows_checksd_path():
    if hasattr(sys, 'frozen'):
        prog_path = os.path.dirname(sys.executable)
        return _checksd_path(os.path.join(prog_path, '..'))
    else:
        cur_path = os.path.dirname(__file__)
        return _checksd_path(cur_path)


def _mac_config_path():
    return _config_path(MAC_CONFIG_PATH)


def _mac_confd_path():
    return _confd_path(MAC_CONFIG_PATH)


def _mac_checksd_path():
    return _unix_checksd_path()


def _unix_config_path():
    root_path = _unix_root_path()
    config_path = os.path.join(root_path, "conf")
    if not os.path.exists(config_path):
        try:
            st = os.stat(__file__)
            os.chmod(__file__, st.st_mode | stat.S_IEXEC)
            os.makedirs(config_path)
        except Exception:
            raise PathNotFound(config_path)
    return _config_path(config_path)


def _unix_confd_path():
    root_path = _unix_root_path()
    return _confd_path(root_path)


def _unix_checksd_path():
    cur_path = os.path.dirname(os.path.realpath(__file__))
    return _checksd_path(cur_path)


def _unix_root_path():
    root_path = os.path.dirname(os.path.dirname(os.path.realpath(__file__)))
    if os.path.exists(root_path):
        return root_path
    raise PathNotFound(root_path)


def _unix_log_path():
    path = _unix_root_path()
    log_path = os.path.join(path, 'logs')
    if not os.path.exists(log_path):
        try:
            st = os.stat(__file__)
            os.chmod(__file__, st.st_mode | stat.S_IEXEC)
            os.makedirs(log_path)
        except Exception:
            raise PathNotFound(log_path)
    return log_path


def _config_path(directory):
    path = os.path.join(directory, DATAMONITOR_CONF)
    if os.path.exists(path):
        return path
    raise PathNotFound(path)


def _confd_path(directory):
    path = os.path.join(directory, 'conf.d')
    if os.path.exists(path):
        return path
    raise PathNotFound(path)


def _checksd_path(directory):
    path = os.path.join(directory, 'checks.d')
    if os.path.exists(path):
        return path
    raise PathNotFound(path)


def _is_affirmative(s):
    if isinstance(s, int):
        return bool(s)
    return s.lower() in ('yes', 'true', '1')


def get_config_path(cfg_path=None, os_name=None):
    if cfg_path is not None and os.path.exists(cfg_path):
        return cfg_path

    try:
        path = os.path.realpath(__file__)
        path = os.path.dirname(path)
        return _config_path(path)
    except PathNotFound as e:
        pass

    if os_name is None:
        os_name = get_os()

    bad_path = ''
    try:
        if os_name == 'windows':
            return _windows_config_path()
        elif os_name == 'mac':
            return _mac_config_path()
        else:
            return _unix_config_path()
    except PathNotFound as e:
        if len(e.args) > 0:
            bad_path = e.args[0]

    sys.stderr.write(
        "Please supply a configuration file at %s or in the directory where the Agent is currently deployed.\n" % bad_path)
    sys.exit(3)


def get_default_bind_host():
    try:
        gethostbyname('localhost')
    except gaierror:
        log.warning("localhost seems undefined in your hosts file, using 127.0.0.1 instead")
        return '127.0.0.1'
    return 'localhost'


def get_histogram_aggregates(configstr=None):
    if configstr is None:
        return None

    try:
        vals = configstr.split(',')
        valid_values = ['min', 'max', 'median', 'avg', 'count']
        result = []

        for val in vals:
            val = val.strip()
            if val not in valid_values:
                log.warning("Ignored histogram aggregate {0}, invalid".format(val))
                continue
            else:
                result.append(val)
    except Exception:
        log.exception("Error when parsing histogram aggregates, skipping")
        return None

    return result


def get_histogram_percentiles(configstr=None):
    if configstr is None:
        return None

    result = []
    try:
        vals = configstr.split(',')
        for val in vals:
            try:
                val = val.strip()
                floatval = float(val)
                if floatval <= 0 or floatval >= 1:
                    raise ValueError
                if len(val) > 4:
                    log.warning("Histogram percentiles are rounded to 2 digits: {0} rounded"
                                .format(floatval))
                result.append(float(val[0:4]))
            except ValueError:
                log.warning("Bad histogram percentile value {0}, must be float in ]0;1[, skipping"
                            .format(val))
    except Exception:
        log.exception("Error when parsing histogram percentiles, skipping")
        return None

    return result


def get_config(parse_args=True, cfg_path=None, options=None):
    if parse_args:
        options, _ = get_parsed_args()

    agentConfig = {
        'check_freq': DEFAULT_CHECK_FREQUENCY,
        'monitorstatsd_port': 8125,
        'monitorstatsd_target': 'http://localhost:17123',
        'graphite_listen_port': None,
        'hostname': None,
        'listen_port': None,
        'tags': None,
        'use_ec2_instance_id': False,
        'version': get_version(),
        'watchmonitor': True,
        'additional_checksd': '/etc/monitor-agent/checks.d/',
        'bind_host': get_default_bind_host(),
        'statsd_metric_namespace': None,
        'utf8_decoding': False
    }

    if Platform.is_mac():
        agentConfig['additional_checksd'] = '/opt/datadog-agent/etc/checks.d'

    try:
        path = os.path.realpath(__file__)
        path = os.path.dirname(path)

        config_path = get_config_path(cfg_path, os_name=get_os())
        config = ConfigParser.ConfigParser()
        config.readfp(skip_leading_wsp(open(config_path)))

        for option in config.options('Main'):
            agentConfig[option] = config.get('Main', option)

        if config.has_option('Main', 'developer_mode'):
            agentConfig['developer_mode'] = _is_affirmative(config.get('Main', 'developer_mode'))

        if options is not None and options.profile:
            agentConfig['developer_mode'] = True

        if config.has_option("Main", "frequency"):
            agentConfig['check_freq'] = config.get("Main", "frequency")

        agentConfig['use_forwarder'] = False
        if options is not None and options.use_forwarder:
            listen_port = 17123
            if config.has_option('Main', 'listen_port'):
                listen_port = int(config.get('Main', 'listen_port'))
            agentConfig['m_url'] = "http://" + agentConfig['bind_host'] + ":" + str(listen_port)
            agentConfig['use_forwarder'] = True
        elif options is not None and not options.disable_dd and options.m_url:
            agentConfig['m_url'] = options.m_url
        else:
            agentConfig['m_url'] = config.get('Main', 'm_url')
        if agentConfig['m_url'].endswith('/'):
            agentConfig['m_url'] = agentConfig['m_url'][:-1]

        if config.has_option('Main', 'additional_checksd'):
            agentConfig['additional_checksd'] = config.get('Main', 'additional_checksd')
        elif get_os() == 'windows':
            common_path = _windows_commondata_path()
            agentConfig['additional_checksd'] = os.path.join(common_path, 'Datamonitor', 'checks.d')

        if config.has_option('Main', 'use_monitorstatsd'):
            agentConfig['use_monitorstatsd'] = config.get('Main', 'use_monitorstatsd').lower() in ("yes", "true")
        else:
            agentConfig['use_monitorstatsd'] = True

        if config.has_option('Main', 'use_web_info_page'):
            agentConfig['use_web_info_page'] = config.get('Main', 'use_web_info_page').lower() in ("yes", "true")
        else:
            agentConfig['use_web_info_page'] = True

        agentConfig['api_key'] = config.get('Main', 'api_key')

        agentConfig['non_local_traffic'] = False
        if config.has_option('Main', 'non_local_traffic'):
            agentConfig['non_local_traffic'] = config.get('Main', 'non_local_traffic').lower() in ("yes", "true")

        if config.has_option('Main', 'use_ec2_instance_id'):
            use_ec2_instance_id = config.get('Main', 'use_ec2_instance_id')
            agentConfig['use_ec2_instance_id'] = (use_ec2_instance_id.lower() == 'yes')

        if config.has_option('Main', 'check_freq'):
            try:
                agentConfig['check_freq'] = int(config.get('Main', 'check_freq'))
            except Exception:
                pass

        if config.has_option('Main', 'histogram_aggregates'):
            agentConfig['histogram_aggregates'] = get_histogram_aggregates(config.get('Main', 'histogram_aggregates'))

        if config.has_option('Main', 'histogram_percentiles'):
            agentConfig['histogram_percentiles'] = get_histogram_percentiles(
                config.get('Main', 'histogram_percentiles'))

        if config.has_option('Main', 'watchmonitor'):
            if config.get('Main', 'watchmonitor').lower() in ('no', 'false'):
                agentConfig['watchmonitor'] = False

        if config.has_option('Main', 'graphite_listen_port'):
            agentConfig['graphite_listen_port'] = \
                int(config.get('Main', 'graphite_listen_port'))
        else:
            agentConfig['graphite_listen_port'] = None

        monitorstatsd_defaults = {
            'monitorstatsd_port': 8125,
            'monitorstatsd_target': 'http://' + agentConfig['bind_host'] + ':17123',
        }
        for key, value in monitorstatsd_defaults.iteritems():
            if config.has_option('Main', key):
                agentConfig[key] = config.get('Main', key)
            else:
                agentConfig[key] = value

        agentConfig['create_dd_check_tags'] = config.has_option('Main', 'create_dd_check_tags') and \
                                              _is_affirmative(config.get('Main', 'create_dd_check_tags'))

        if config.has_option('Main', 'statsd_forward_host'):
            agentConfig['statsd_forward_host'] = config.get('Main', 'statsd_forward_host')
            if config.has_option('Main', 'statsd_forward_port'):
                agentConfig['statsd_forward_port'] = int(config.get('Main', 'statsd_forward_port'))

        if config.has_option('Main', 'monitorstatsd_use_murl'):
            if _is_affirmative(config.get('Main', 'monitorstatsd_use_murl')):
                agentConfig['monitorstatsd_target'] = agentConfig['m_url']

        if config.has_option('Main', 'use_mount'):
            agentConfig['use_mount'] = _is_affirmative(config.get('Main', 'use_mount'))

        if options is not None and options.autorestart:
            agentConfig['autorestart'] = True
        elif config.has_option('Main', 'autorestart'):
            agentConfig['autorestart'] = _is_affirmative(config.get('Main', 'autorestart'))

        if config.has_option('Main', 'check_timings'):
            agentConfig['check_timings'] = _is_affirmative(config.get('Main', 'check_timings'))

        if config.has_option('Main', 'exclude_process_args'):
            agentConfig['exclude_process_args'] = _is_affirmative(config.get('Main', 'exclude_process_args'))

        try:
            filter_device_re = config.get('Main', 'device_blacklist_re')
            agentConfig['device_blacklist_re'] = re.compile(filter_device_re)
        except ConfigParser.NoOptionError:
            pass

        if config.has_option('datamonitor', 'ddforwarder_log'):
            agentConfig['has_datamonitor'] = True

        if config.has_option("Main", "monitorstream_log"):

            log_path = config.get("Main", "monitorstream_log")
            if config.has_option("Main", "monitorstream_line_parser"):
                agentConfig["monitorstreams"] = ':'.join([log_path, config.get("Main", "monitorstream_line_parser")])
            else:
                agentConfig["monitorstreams"] = log_path

        elif config.has_option("Main", "monitorstreams"):
            agentConfig["monitorstreams"] = config.get("Main", "monitorstreams")

        if config.has_option("Main", "nagios_perf_cfg"):
            agentConfig["nagios_perf_cfg"] = config.get("Main", "nagios_perf_cfg")

        if config.has_option("Main", "use_curl_http_client"):
            agentConfig["use_curl_http_client"] = _is_affirmative(config.get("Main", "use_curl_http_client"))
        else:

            agentConfig["use_curl_http_client"] = False

        if config.has_section('WMI'):
            agentConfig['WMI'] = {}
            for key, value in config.items('WMI'):
                agentConfig['WMI'][key] = value

        if (config.has_option("Main", "limit_memory_consumption") and
                    config.get("Main", "limit_memory_consumption") is not None):
            agentConfig["limit_memory_consumption"] = int(config.get("Main", "limit_memory_consumption"))
        else:
            agentConfig["limit_memory_consumption"] = None

        if config.has_option("Main", "skip_ssl_validation"):
            agentConfig["skip_ssl_validation"] = _is_affirmative(config.get("Main", "skip_ssl_validation"))

        agentConfig["collect_instance_metadata"] = True
        if config.has_option("Main", "collect_instance_metadata"):
            agentConfig["collect_instance_metadata"] = _is_affirmative(config.get("Main", "collect_instance_metadata"))

        agentConfig["proxy_forbid_method_switch"] = False
        if config.has_option("Main", "proxy_forbid_method_switch"):
            agentConfig["proxy_forbid_method_switch"] = _is_affirmative(
                config.get("Main", "proxy_forbid_method_switch"))

        agentConfig["collect_ec2_tags"] = False
        if config.has_option("Main", "collect_ec2_tags"):
            agentConfig["collect_ec2_tags"] = _is_affirmative(config.get("Main", "collect_ec2_tags"))

        agentConfig["utf8_decoding"] = False
        if config.has_option("Main", "utf8_decoding"):
            agentConfig["utf8_decoding"] = _is_affirmative(config.get("Main", "utf8_decoding"))

        agentConfig["gce_updated_hostname"] = False
        if config.has_option("Main", "gce_updated_hostname"):
            agentConfig["gce_updated_hostname"] = _is_affirmative(config.get("Main", "gce_updated_hostname"))

    except ConfigParser.NoSectionError as e:
        sys.stderr.write('Config file not found or incorrectly formatted.\n')
        sys.exit(2)

    except ConfigParser.ParsingError as e:
        sys.stderr.write('Config file not found or incorrectly formatted.\n')
        sys.exit(2)

    except ConfigParser.NoOptionError as e:
        sys.stderr.write('There are some items missing from your config file, but nothing fatal [%s]' % e)

    agentConfig['proxy_settings'] = get_proxy(agentConfig)
    if agentConfig.get('ca_certs', None) is None:
        agentConfig['ssl_certificate'] = get_ssl_certificate(get_os(), 'datamonitor-cert.pem')
    else:
        agentConfig['ssl_certificate'] = agentConfig['ca_certs']

    agentConfig['interval'] = config.get('Main', 'updater_interval')

    return agentConfig


def get_system_stats():
    systemStats = {
        'machine': platform.machine(),
        'platform': sys.platform,
        'processor': platform.processor(),
        'pythonV': platform.python_version(),
    }

    platf = sys.platform

    try:
        if Platform.is_linux(platf):
            output, _, _ = get_subprocess_output(['grep', 'model name', '/proc/cpuinfo'], log)
            systemStats['cpuCores'] = len(output.splitlines())

        if Platform.is_darwin(platf) or Platform.is_freebsd(platf):
            output, _, _ = get_subprocess_output(['sysctl', 'hw.ncpu'], log)
            systemStats['cpuCores'] = int(output.split(': ')[1])
    except SubprocessOutputEmptyError as e:
        log.warning("unable to retrieve number of cpuCores. Failed with error %s", e)

    if Platform.is_linux(platf):
        systemStats['nixV'] = platform.dist()

    elif Platform.is_darwin(platf):
        systemStats['macV'] = platform.mac_ver()

    elif Platform.is_freebsd(platf):
        version = platform.uname()[2]
        systemStats['fbsdV'] = ('freebsd', version, '')

    elif Platform.is_win32(platf):
        systemStats['winV'] = platform.win32_ver()

    return systemStats


def set_win32_cert_path():
    if hasattr(sys, 'frozen'):
        prog_path = os.path.dirname(sys.executable)
        crt_path = os.path.join(prog_path, 'ca-certificates.crt')
    else:
        cur_path = os.path.dirname(__file__)
        crt_path = os.path.join(cur_path, 'packaging', 'datamonitor-agent', 'win32',
                                'install_files', 'ca-certificates.crt')
    import tornado.simple_httpclient
    log.info("Windows certificate path: %s" % crt_path)
    tornado.simple_httpclient._DEFAULT_CA_CERTS = crt_path


def set_win32_requests_ca_bundle_path():
    import requests.adapters
    if hasattr(sys, 'frozen'):
        prog_path = os.path.dirname(sys.executable)
        ca_bundle_path = os.path.join(prog_path, 'cacert.pem')
        requests.adapters.DEFAULT_CA_BUNDLE_PATH = ca_bundle_path

    log.info("Default CA bundle path of the requests library: {0}"
             .format(requests.adapters.DEFAULT_CA_BUNDLE_PATH))


def get_confd_path(osname=None):
    try:
        cur_path = os.path.dirname(os.path.realpath(__file__))
        return _confd_path(cur_path)
    except PathNotFound as e:
        pass

    if not osname:
        osname = get_os()
    bad_path = ''
    try:
        if osname == 'windows':
            return _windows_confd_path()
        elif osname == 'mac':
            return _mac_confd_path()
        else:
            return _unix_confd_path()
    except PathNotFound as e:
        if len(e.args) > 0:
            bad_path = e.args[0]

    raise PathNotFound(bad_path)


def get_checksd_path(osname=None):
    if not osname:
        osname = get_os()
    if osname == 'windows':
        return _windows_checksd_path()
    elif osname == 'mac':
        return _mac_checksd_path()
    else:
        return _unix_checksd_path()


def get_win32service_file(osname, filename):
    if osname == 'windows':
        if hasattr(sys, 'frozen'):
            prog_path = os.path.dirname(sys.executable)
            path = os.path.join(prog_path, filename)
        else:
            cur_path = os.path.dirname(__file__)
            path = os.path.join(cur_path, filename)
        if os.path.exists(path):
            log.debug("Certificate file found at %s" % str(path))
            return path

    else:
        cur_path = os.path.dirname(os.path.realpath(__file__))
        path = os.path.join(cur_path, filename)
        if os.path.exists(path):
            return path

    return None


def get_ssl_certificate(osname, filename):
    if osname == 'windows':
        if hasattr(sys, 'frozen'):
            prog_path = os.path.dirname(sys.executable)
            path = os.path.join(prog_path, filename)
        else:
            cur_path = os.path.dirname(__file__)
            path = os.path.join(cur_path, filename)
        if os.path.exists(path):
            log.debug("Certificate file found at %s" % str(path))
            return path
    else:
        cur_path = os.path.dirname(os.path.realpath(__file__))
        path = os.path.join(cur_path, filename)
        if os.path.exists(path):
            return path

    log.info("Certificate file NOT found at %s" % str(path))
    return None


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


def load_check_directory(agentConfig, hostname):
    from checks import AgentCheck, AGENT_METRICS_CHECK_NAME

    initialized_checks = {}
    init_failed_checks = {}
    deprecated_checks = {}
    agentConfig['checksd_hostname'] = hostname

    deprecated_configs_enabled = [v for k, v in OLD_STYLE_PARAMETERS if
                                  len([l for l in agentConfig if l.startswith(k)]) > 0]
    for deprecated_config in deprecated_configs_enabled:
        msg = "Configuring %s in datamonitor.conf is not supported anymore. Please use conf.d" % deprecated_config
        deprecated_checks[deprecated_config] = {'error': msg, 'detail': None}
        log.error(msg)

    osname = get_os()
    checks_paths = [glob.glob(os.path.join(agentConfig['additional_checksd'], '*.py'))]

    try:
        checksd_path = get_checksd_path(osname)
        checks_paths.append(glob.glob(os.path.join(checksd_path, '*.py')))
    except PathNotFound as e:
        log.error(e.args[0])
        sys.exit(3)

    try:
        confd_path = get_confd_path(osname)
    except PathNotFound as e:
        log.error(
            "No conf.d folder found at '%s' or in the directory where the Agent is currently deployed.\n" % e.args[0])
        sys.exit(3)

    for check in itertools.chain(*checks_paths):
        check_name = os.path.basename(check).split('.')[0]
        check_config = None
        if check_name in initialized_checks or check_name in init_failed_checks:
            log.debug('Skipping check %s because it has already been loaded from another location', check)
            continue

        conf_path = os.path.join(confd_path, '%s.yaml' % check_name)
        conf_exists = False

        if os.path.exists(conf_path):
            conf_exists = True
        else:
            log.debug("No configuration file for %s. Looking for defaults" % check_name)

            default_conf_path = os.path.join(confd_path, '%s.yaml.default' % check_name)
            if not os.path.exists(default_conf_path):
                log.debug("Default configuration file {0} is missing. Skipping check".format(default_conf_path))
                continue
            conf_path = default_conf_path
            conf_exists = True

        if conf_exists:
            try:
                check_config = check_yaml(conf_path)
            except Exception as e:
                log.exception("Unable to parse yaml config in %s" % conf_path)
                init_failed_checks[check_name] = {'error': str(e)}
                continue
        else:
            if check_name == 'nagios':
                if any([nagios_key in agentConfig for nagios_key in NAGIOS_OLD_CONF_KEYS]):
                    log.warning("Configuring Nagios in datamonitor.conf is deprecated "
                                "and will be removed in a future version. "
                                "Please use conf.d")
                    check_config = {'instances': [
                        dict((key, agentConfig[key]) for key in agentConfig if key in NAGIOS_OLD_CONF_KEYS)]}
                else:
                    continue
            else:
                log.debug("No configuration file for %s" % check_name)
                continue

        try:
            check_module = imp.load_source('checksd_%s' % check_name, check)
        except Exception as e:
            init_failed_checks[check_name] = {'error': e}
            log.exception('Unable to import check module %s.py from checks.d' % check_name)
            continue

        check_class = None
        classes = inspect.getmembers(check_module, inspect.isclass)
        for _, clsmember in classes:
            if clsmember == AgentCheck:
                continue
            if issubclass(clsmember, AgentCheck):
                check_class = clsmember
                if AgentCheck in clsmember.__bases__:
                    continue
                else:
                    break

        if not check_class:
            log.error('No check class (inheriting from AgentCheck) found in %s.py' % check_name)
            continue

        if not check_config.get('instances'):
            log.error("Config %s is missing 'instances'" % conf_path)
            continue

        init_config = check_config.get('init_config', {})

        if init_config is None:
            init_config = {}

        instances = check_config['instances']
        try:
            try:
                c = check_class(check_name, init_config=init_config,
                                agentConfig=agentConfig, instances=instances)
            except TypeError as e:

                c = check_class(check_name, init_config=init_config,
                                agentConfig=agentConfig)
                c.instances = instances
        except Exception as e:
            log.exception('Unable to initialize check %s' % check_name)
            init_failed_checks[check_name] = {'error': e}
        else:
            initialized_checks[check_name] = c

        if 'pythonpath' in check_config:
            pythonpath = check_config['pythonpath']
            if not isinstance(pythonpath, list):
                pythonpath = [pythonpath]
            sys.path.extend(pythonpath)

        log.debug('Loaded check.d/%s.py' % check_name)

    init_failed_checks.update(deprecated_checks)
    log.info(
        'initialized checks.d checks: %s' % [k for k in initialized_checks.keys() if k != AGENT_METRICS_CHECK_NAME])
    log.info('initialization failed checks.d checks: %s' % init_failed_checks.keys())
    return {'initialized_checks': initialized_checks.values(),
            'init_failed_checks': init_failed_checks,
            }


def get_log_date_format():
    return "%Y-%m-%d %H:%M:%S"


def get_log_format(logger_name):
    if get_os() != 'windows':
        return '%%(asctime)s | %%(levelname)s | dd.%s | %%(name)s(%%(filename)s:%%(lineno)s) | %%(message)s' % logger_name
    return '%(asctime)s | %(levelname)s | %(name)s(%(filename)s:%(lineno)s) | %(message)s'


def get_syslog_format(logger_name):
    return 'dd.%s[%%(process)d]: %%(levelname)s (%%(filename)s:%%(lineno)s): %%(message)s' % logger_name


def get_logging_config(cfg_path=None):
    system_os = get_os()
    logging_config = {
        'log_level': None,
        'log_to_event_viewer': False,
        'log_to_syslog': False,
        'syslog_host': None,
        'syslog_port': None,
    }
    if system_os == 'windows':
        logging_config['windows_collector_log_file'] = os.path.join(_windows_commondata_path(), 'logs', 'collector.log')
        logging_config['windows_net_collector_log_file'] = os.path.join(_windows_commondata_path(), 'logs',
                                                                        'net_collector.log')
        logging_config['windows_script_caller_log_file'] = os.path.join(_windows_commondata_path(), 'logs',
                                                                        'script_caller.log')
        logging_config['windows_updater_log_file'] = os.path.join(_windows_commondata_path(), 'logs', 'updater.log')
        logging_config['windows_custom_script_log_file'] = os.path.join(_windows_commondata_path(), 'logs',
                                                                        'custom_script.log')
        logging_config['windows_forwarder_log_file'] = os.path.join(_windows_commondata_path(), 'logs', 'forwarder.log')
        logging_config['windows_monitorstatsd_log_file'] = os.path.join(_windows_commondata_path(), 'logs',
                                                                        'monitorstatsd.log')
        logging_config['jmxfetch_log_file'] = os.path.join(_windows_commondata_path(), 'logs', 'jmxfetch.log')

    else:
        log_path = _unix_log_path()
        logging_config['collector_log_file'] = os.path.join(log_path, 'collector.log')
        logging_config['net_collector_log_file'] = os.path.join(log_path, 'net_collector.log')
        logging_config['script_caller_log_file'] = os.path.join(log_path, 'script_caller.log')
        logging_config['updater_log_file'] = os.path.join(log_path, 'updater.log')
        logging_config['forwarder_log_file'] = os.path.join(log_path, 'forwarder.log')
        logging_config['monitorstatsd_log_file'] = os.path.join(log_path, 'monitorstatsd.log')
        logging_config['jmxfetch_log_file'] = os.path.join(log_path, 'jmxfetch.log')
        logging_config['go-metro_log_file'] = os.path.join(log_path, 'go-metro.log')
        logging_config['custom_script_log_file'] = os.path.join(log_path, 'custom_script.log')

    config_path = get_config_path(cfg_path, os_name=system_os)
    config = ConfigParser.ConfigParser()
    config.readfp(skip_leading_wsp(open(config_path)))

    for option in logging_config:
        if config.has_option('Main', option):
            logging_config[option] = config.get('Main', option)

    levels = {
        'CRITICAL': logging.CRITICAL,
        'DEBUG': logging.DEBUG,
        'ERROR': logging.ERROR,
        'FATAL': logging.FATAL,
        'INFO': logging.INFO,
        'WARN': logging.WARN,
        'WARNING': logging.WARNING,
    }
    if config.has_option('Main', 'log_level'):
        logging_config['log_level'] = levels.get(config.get('Main', 'log_level'))

    if config.has_option('Main', 'log_to_syslog'):
        logging_config['log_to_syslog'] = config.get('Main', 'log_to_syslog').strip().lower() in ['yes', 'true', 1]

    if config.has_option('Main', 'log_to_event_viewer'):
        logging_config['log_to_event_viewer'] = config.get('Main', 'log_to_event_viewer').strip().lower() in ['yes',
                                                                                                              'true', 1]

    if config.has_option('Main', 'syslog_host'):
        host = config.get('Main', 'syslog_host').strip()
        if host:
            logging_config['syslog_host'] = host
        else:
            logging_config['syslog_host'] = None

    if config.has_option('Main', 'syslog_port'):
        port = config.get('Main', 'syslog_port').strip()
        try:
            logging_config['syslog_port'] = int(port)
        except Exception:
            logging_config['syslog_port'] = None

    if config.has_option('Main', 'disable_file_logging'):
        logging_config['disable_file_logging'] = config.get('Main', 'disable_file_logging').strip().lower() in ['yes',
                                                                                                                'true',
                                                                                                                1]
    else:
        logging_config['disable_file_logging'] = False

    return logging_config


def initialize_logging(logger_name):
    try:
        logging_config = get_logging_config()

        logging.basicConfig(
            format=get_log_format(logger_name),
            level=logging_config['log_level'] or logging.INFO,
        )

        log_file = logging_config.get('%s_log_file' % logger_name)
        if log_file is not None and not logging_config['disable_file_logging']:
            if os.access(os.path.dirname(log_file), os.R_OK | os.W_OK):
                file_handler = logging.handlers.RotatingFileHandler(log_file, maxBytes=LOGGING_MAX_BYTES, backupCount=1)
                formatter = logging.Formatter(get_log_format(logger_name), get_log_date_format())
                file_handler.setFormatter(formatter)

                root_log = logging.getLogger()
                root_log.addHandler(file_handler)
            else:
                sys.stderr.write("Log file is unwritable: '%s'\n" % log_file)

        if logging_config['log_to_syslog']:
            try:
                from logging.handlers import SysLogHandler

                if logging_config['syslog_host'] is not None and logging_config['syslog_port'] is not None:
                    sys_log_addr = (logging_config['syslog_host'], logging_config['syslog_port'])
                else:
                    sys_log_addr = "/dev/log"

                    if Platform.is_darwin():
                        sys_log_addr = "/var/run/syslog"
                    elif Platform.is_freebsd():
                        sys_log_addr = "/var/run/log"

                handler = SysLogHandler(address=sys_log_addr, facility=SysLogHandler.LOG_DAEMON)
                handler.setFormatter(logging.Formatter(get_syslog_format(logger_name), get_log_date_format()))
                root_log = logging.getLogger()
                root_log.addHandler(handler)
            except Exception as e:
                sys.stderr.write("Error setting up syslog: '%s'\n" % str(e))

        if get_os() == 'windows' and logging_config['log_to_event_viewer']:
            try:
                from logging.handlers import NTEventLogHandler
                nt_event_handler = NTEventLogHandler(logger_name, get_win32service_file('windows', 'win32service.pyd'),
                                                     'Application')
                nt_event_handler.setFormatter(logging.Formatter(get_syslog_format(logger_name), get_log_date_format()))
                nt_event_handler.setLevel(logging.ERROR)
                app_log = logging.getLogger(logger_name)
                app_log.addHandler(nt_event_handler)
            except Exception as e:
                sys.stderr.write("Error setting up Event viewer logging: '%s'\n" % str(e))

    except Exception as e:
        sys.stderr.write("Couldn't initialize logging: %s\n" % str(e))

        logging.basicConfig(
            format=get_log_format(logger_name),
            level=logging.INFO,
        )

    global log
    log = logging.getLogger(__name__)
