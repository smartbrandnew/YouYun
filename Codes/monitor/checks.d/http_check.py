import os.path
import re
import socket
import ssl
import time
import warnings
from datetime import datetime
from urlparse import urlparse

import requests
import tornado
from requests.adapters import HTTPAdapter
from requests.packages import urllib3
from requests.packages.urllib3.exceptions import (
    SecurityWarning,
)
from requests.packages.urllib3.packages.ssl_match_hostname import \
    match_hostname
from requests.packages.urllib3.util import ssl_

from checks.network_checks import EventType, NetworkCheck, Status
from config import _is_affirmative
from util import headers as agent_headers
from utils.proxy import get_proxy

DEFAULT_EXPECTED_CODE = "(1|2|3)\d\d"


class WeakCiphersHTTPSConnection(urllib3.connection.VerifiedHTTPSConnection):
    SUPPORTED_CIPHERS = (
        'ECDH+AESGCM:DH+AESGCM:ECDH+AES256:DH+AES256:ECDH+AES128:DH+AES:'
        'ECDH+HIGH:DH+HIGH:ECDH+3DES:DH+3DES:RSA+AESGCM:RSA+AES:RSA+HIGH:'
        'RSA+3DES:ECDH+RC4:DH+RC4:RSA+RC4:!aNULL:!eNULL:!EXP:-MD5:RSA+RC4+MD5'
    )

    def __init__(self, host, port, ciphers=None, **kwargs):
        self.ciphers = ciphers if ciphers is not None else self.SUPPORTED_CIPHERS
        super(WeakCiphersHTTPSConnection, self).__init__(host, port, **kwargs)

    def connect(self):
        conn = self._new_conn()

        resolved_cert_reqs = ssl_.resolve_cert_reqs(self.cert_reqs)
        resolved_ssl_version = ssl_.resolve_ssl_version(self.ssl_version)

        hostname = self.host
        if getattr(self, '_tunnel_host', None):
            self.sock = conn
            self._tunnel()
            self.auto_open = 0

            hostname = self._tunnel_host

        self.sock = ssl_.ssl_wrap_socket(conn, self.key_file, self.cert_file,
                                         cert_reqs=resolved_cert_reqs,
                                         ca_certs=self.ca_certs,
                                         server_hostname=hostname,
                                         ssl_version=resolved_ssl_version,
                                         ciphers=self.ciphers)

        if self.assert_fingerprint:
            ssl_.assert_fingerprint(self.sock.getpeercert(binary_form=True), self.assert_fingerprint)
        elif resolved_cert_reqs != ssl.CERT_NONE \
                and self.assert_hostname is not False:
            cert = self.sock.getpeercert()
            if not cert.get('subjectAltName', ()):
                warnings.warn((
                    'Certificate has no `subjectAltName`, falling back to check for a `commonName` for now. '
                    'This feature is being removed by major browsers and deprecated by RFC 2818. '
                    '(See https://github.com/shazow/urllib3/issues/497 for details.)'),
                    SecurityWarning
                )
            match_hostname(cert, self.assert_hostname or hostname)

        self.is_verified = (resolved_cert_reqs == ssl.CERT_REQUIRED
                            or self.assert_fingerprint is not None)


class WeakCiphersHTTPSConnectionPool(urllib3.connectionpool.HTTPSConnectionPool):
    ConnectionCls = WeakCiphersHTTPSConnection


class WeakCiphersPoolManager(urllib3.poolmanager.PoolManager):
    def _new_pool(self, scheme, host, port):
        if scheme == 'https':
            return WeakCiphersHTTPSConnectionPool(host, port, **(self.connection_pool_kw))
        return super(WeakCiphersPoolManager, self)._new_pool(scheme, host, port)


class WeakCiphersAdapter(HTTPAdapter):
    def init_poolmanager(self, connections, maxsize, block=False, **pool_kwargs):
        self._pool_connections = connections
        self._pool_maxsize = maxsize
        self._pool_block = block

        self.poolmanager = WeakCiphersPoolManager(num_pools=connections,
                                                  maxsize=maxsize, block=block, strict=True, **pool_kwargs)


def get_ca_certs_path():
    CA_CERTS = [
        '/opt/datamonitor-agent/embedded/ssl/certs/cacert.pem',
        os.path.join(os.path.dirname(tornado.__file__), 'ca-certificates.crt'),
        '/etc/ssl/certs/ca-certificates.crt',
    ]

    for f in CA_CERTS:
        if os.path.exists(f):
            return f
    return None


class HTTPCheck(NetworkCheck):
    SOURCE_TYPE_NAME = 'system'
    SC_STATUS = 'http.can_connect'
    SC_SSL_CERT = 'http.ssl_cert'

    def __init__(self, name, init_config, agentConfig, instances):
        self.ca_certs = init_config.get('ca_certs', get_ca_certs_path())
        proxy_settings = get_proxy(agentConfig)
        if not proxy_settings:
            self.proxies = None
        else:
            uri = "{host}:{port}".format(
                host=proxy_settings['host'],
                port=proxy_settings['port'])
            if proxy_settings['user'] and proxy_settings['password']:
                uri = "{user}:{password}@{uri}".format(
                    user=proxy_settings['user'],
                    password=proxy_settings['password'],
                    uri=uri)
            self.proxies = {
                'http': "http://{uri}".format(uri=uri),
                'https': "https://{uri}".format(uri=uri)
            }

        NetworkCheck.__init__(self, name, init_config, agentConfig, instances)

    def _load_conf(self, instance):
        tags = instance.get('tags', [])
        username = instance.get('username')
        password = instance.get('password')
        http_response_status_code = str(instance.get('http_response_status_code', DEFAULT_EXPECTED_CODE))
        timeout = int(instance.get('timeout', 10))
        config_headers = instance.get('headers', {})
        headers = agent_headers(self.agentConfig)
        headers.update(config_headers)
        url = instance.get('url')
        content_match = instance.get('content_match')
        response_time = _is_affirmative(instance.get('collect_response_time', True))
        if not url:
            raise Exception("Bad configuration. You must specify a url")
        include_content = _is_affirmative(instance.get('include_content', False))
        ssl = _is_affirmative(instance.get('disable_ssl_validation', True))
        ssl_expire = _is_affirmative(instance.get('check_certificate_expiration', True))
        instance_ca_certs = instance.get('ca_certs', self.ca_certs)
        weakcipher = _is_affirmative(instance.get('weakciphers', False))
        ignore_ssl_warning = _is_affirmative(instance.get('ignore_ssl_warning', False))

        return url, username, password, http_response_status_code, timeout, include_content, \
               headers, response_time, content_match, tags, ssl, ssl_expire, instance_ca_certs, \
               weakcipher, ignore_ssl_warning

    def _check(self, instance):
        addr, username, password, http_response_status_code, timeout, include_content, headers, \
        response_time, content_match, tags, disable_ssl_validation, \
        ssl_expire, instance_ca_certs, weakcipher, ignore_ssl_warning = self._load_conf(instance)
        start = time.time()

        service_checks = []
        try:
            parsed_uri = urlparse(addr)
            self.log.debug("Connecting to %s" % addr)
            if disable_ssl_validation and parsed_uri.scheme == "https" and not ignore_ssl_warning:
                self.warning("Skipping SSL certificate validation for %s based on configuration"
                             % addr)

            auth = None
            if username is not None and password is not None:
                auth = (username, password)

            sess = requests.Session()
            if weakcipher:
                base_addr = '{uri.scheme}://{uri.netloc}/'.format(uri=parsed_uri)
                sess.mount(base_addr, WeakCiphersAdapter())
                self.log.debug("Weak Ciphers will be used for {0}. Suppoted Cipherlist: {1}".format(
                    base_addr, WeakCiphersHTTPSConnection.SUPPORTED_CIPHERS))

            r = sess.request('GET', addr, auth=auth, timeout=timeout, headers=headers, proxies=self.proxies,
                             verify=False if disable_ssl_validation else instance_ca_certs)

        except (socket.timeout, requests.exceptions.ConnectionError, requests.exceptions.Timeout) as e:
            length = int((time.time() - start) * 1000)
            self.log.info("%s is DOWN, error: %s. Connection failed after %s ms"
                          % (addr, str(e), length))
            service_checks.append((
                self.SC_STATUS,
                Status.DOWN,
                "%s. Connection failed after %s ms" % (str(e), length)
            ))

        except socket.error, e:
            length = int((time.time() - start) * 1000)
            self.log.info("%s is DOWN, error: %s. Connection failed after %s ms"
                          % (addr, repr(e), length))
            service_checks.append((
                self.SC_STATUS,
                Status.DOWN,
                "Socket error: %s. Connection failed after %s ms" % (repr(e), length)
            ))

        except Exception, e:
            length = int((time.time() - start) * 1000)
            self.log.error("Unhandled exception %s. Connection failed after %s ms"
                           % (str(e), length))
            raise

        if response_time and not service_checks:
            running_time = time.time() - start
            tags_list = list(tags)
            tags_list.append('url:%s' % addr)
            self.gauge('network.http.response_time', running_time, tags=tags_list)

        if not (service_checks or re.match(http_response_status_code, str(r.status_code))):
            if http_response_status_code == DEFAULT_EXPECTED_CODE:
                expected_code = "1xx or 2xx or 3xx"
            else:
                expected_code = http_response_status_code

            message = "Incorrect HTTP return code for url %s. Expected %s, got %s" % (
                addr, expected_code, str(r.status_code))

            self.log.info(message)

            service_checks.append((
                self.SC_STATUS,
                Status.DOWN,
                message
            ))

        if not service_checks:
            if content_match:
                content = r.content
                if re.search(content_match, content, re.UNICODE):
                    self.log.debug("%s is found in return content" % content_match)
                    service_checks.append((
                        self.SC_STATUS, Status.UP, "UP"
                    ))
                else:
                    self.log.info("%s not found in content" % content_match)
                    self.log.debug("Content returned:\n%s" % content)
                    service_checks.append((
                        self.SC_STATUS,
                        Status.DOWN,
                        'Content "%s" not found in response' % content_match
                    ))
            else:
                self.log.debug("%s is UP" % addr)
                service_checks.append((
                    self.SC_STATUS, Status.UP, "UP"
                ))

        if ssl_expire and parsed_uri.scheme == "https":
            status, msg = self.check_cert_expiration(instance, timeout, instance_ca_certs)
            service_checks.append((
                self.SC_SSL_CERT, status, msg
            ))

        return service_checks

    def _create_status_event(self, sc_name, status, msg, instance):
        if sc_name != self.SC_STATUS:
            return
        url = instance.get('url', None)
        name = instance.get('name', None)
        nb_failures = self.statuses[name][sc_name].count(Status.DOWN)
        nb_tries = len(self.statuses[name][sc_name])
        tags = instance.get('tags', [])
        tags_list = []
        tags_list.extend(tags)
        tags_list.append('url:%s' % url)

        custom_message = instance.get('message', "")
        if custom_message:
            custom_message += " \n"

        instance_source_type_name = instance.get('source_type', None)
        if instance_source_type_name is None:
            source_type = "%s.%s" % (NetworkCheck.SOURCE_TYPE_NAME, name)
        else:
            source_type = "%s.%s" % (NetworkCheck.SOURCE_TYPE_NAME, instance_source_type_name)

        notify = instance.get('notify', self.init_config.get('notify', []))
        notify_message = ""
        if notify:
            notify_list = []
            for handle in notify:
                notify_list.append("@%s" % handle.strip())
            notify_message = " ".join(notify_list) + " \n"

        if status == Status.DOWN:
            if isinstance(msg, tuple):
                code, reason, content = msg
                if len(content) > 200:
                    content = content[:197] + '...'

                msg = u"%d %s\n\n%s" % (code, reason, content)
                msg = msg.rstrip()

            title = "[Alert] %s reported that %s is down" % (self.hostname, name)
            alert_type = "error"
            msg = u"%s %s %s reported that %s (%s) failed %s time(s) within %s last attempt(s)." \
                  " Last error: %s" % (notify_message, custom_message, self.hostname,
                                       name, url, nb_failures, nb_tries, msg)
            event_type = EventType.DOWN

        else:
            title = "[Recovered] %s reported that %s is up" % (self.hostname, name)
            alert_type = "success"
            msg = u"%s %s %s reported that %s (%s) recovered" \
                  % (notify_message, custom_message, self.hostname, name, url)
            event_type = EventType.UP

        return {
            'timestamp': int(time.time()),
            'event_type': event_type,
            'host': self.hostname,
            'msg_text': msg,
            'msg_title': title,
            'alert_type': alert_type,
            "source_type_name": source_type,
            "event_object": name,
            "tags": tags_list
        }

    def report_as_service_check(self, sc_name, status, instance, msg=None):
        instance_name = self.normalize(instance['name'])
        url = instance.get('url', None)
        sc_tags = ['url:{0}'.format(url), "instance:{0}".format(instance_name)]
        custom_tags = instance.get('tags', [])
        tags = sc_tags + custom_tags

        if sc_name == self.SC_STATUS:
            if isinstance(msg, tuple):
                code, reason, content = msg

                if len(content) > 200:
                    content = content[:197] + '...'

                msg = u"%d %s\n\n%s" % (code, reason, content)
                msg = msg.rstrip()

        self.service_check(sc_name,
                           NetworkCheck.STATUS_TO_SERVICE_CHECK[status],
                           tags=tags,
                           message=msg
                           )

    def check_cert_expiration(self, instance, timeout, instance_ca_certs):
        warning_days = int(instance.get('days_warning', 14))
        critical_days = int(instance.get('days_critical', 7))
        url = instance.get('url')

        o = urlparse(url)
        host = o.hostname

        port = o.port or 443

        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.settimeout(float(timeout))
            sock.connect((host, port))
            ssl_sock = ssl.wrap_socket(sock, cert_reqs=ssl.CERT_REQUIRED,
                                       ca_certs=instance_ca_certs)
            cert = ssl_sock.getpeercert()

        except Exception as e:
            return Status.DOWN, "%s" % (str(e))

        exp_date = datetime.strptime(cert['notAfter'], "%b %d %H:%M:%S %Y %Z")
        days_left = exp_date - datetime.utcnow()

        if days_left.days < 0:
            return Status.DOWN, "Expired by {0} days".format(days_left.days)

        elif days_left.days < critical_days:
            return Status.CRITICAL, "This cert TTL is critical: only {0} days before it expires" \
                .format(days_left.days)

        elif days_left.days < warning_days:
            return Status.WARNING, "This cert is almost expired, only {0} days left" \
                .format(days_left.days)

        else:
            return Status.UP, "Days left: {0}".format(days_left.days)