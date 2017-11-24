import re
import socket
import struct
from StringIO import StringIO
from collections import defaultdict
from distutils.version import LooseVersion

from checks import AgentCheck
from util import get_hostname


class ZKConnectionFailure(Exception):
    pass


class ZKMetric(tuple):
    def __new__(cls, name, value, m_type="gauge"):
        return super(ZKMetric, cls).__new__(cls, [name, value, m_type])


class ZookeeperCheck(AgentCheck):
    version_pattern = re.compile(r'Zookeeper version: ([^.]+)\.([^.]+)\.([^-]+)', flags=re.I)

    SOURCE_TYPE_NAME = 'zookeeper'

    STATUS_TYPES = [
        'leader',
        'follower',
        'observer',
        'standalone',
        'down',
        'inactive',
    ]

    _MNTR_RATES = set(
        [
            'zk_packets_received',
            'zk_packets_sent',
        ]
    )

    def check(self, instance):
        host = instance.get('host', 'localhost')
        port = int(instance.get('port', 2181))
        timeout = float(instance.get('timeout', 3.0))
        expected_mode = (instance.get('expected_mode') or '').strip()
        tags = instance.get('tags', [])
        cx_args = (host, port, timeout)
        sc_tags = ["host:{0}".format(host), "port:{0}".format(port)]
        hostname = get_hostname(self.agentConfig)
        report_instance_mode = instance.get("report_instance_mode", True)

        try:
            ruok_out = self._send_command('ruok', *cx_args)
        except ZKConnectionFailure:

            status = AgentCheck.CRITICAL
            message = 'No response from `ruok` command'
            self.increment('zookeeper.timeouts')

            if report_instance_mode:
                self.report_instance_mode(hostname, 'down', tags)
            raise
        else:
            ruok_out.seek(0)
            ruok = ruok_out.readline()
            if ruok == 'imok':
                status = AgentCheck.OK
            else:
                status = AgentCheck.WARNING
            message = u'Response from the server: %s' % ruok
        finally:
            self.service_check(
                'zookeeper.ruok', status, message=message, tags=sc_tags
            )

        try:
            stat_out = self._send_command('stat', *cx_args)
        except ZKConnectionFailure:
            self.increment('zookeeper.timeouts')
            if report_instance_mode:
                self.report_instance_mode(hostname, 'down', tags)
            raise
        except Exception as e:
            self.warning(e)
            self.increment('zookeeper.datamonitor_client_exception')
            if report_instance_mode:
                self.report_instance_mode(hostname, 'unknown', tags)
            raise
        else:

            metrics, new_tags, mode, zk_version = self.parse_stat(stat_out)

            if mode != 'inactive':
                for metric, value, m_type in metrics:
                    submit_metric = getattr(self, m_type)
                    submit_metric(metric, value, tags=tags + new_tags)

            if report_instance_mode:
                self.report_instance_mode(hostname, mode, tags)

            if expected_mode:
                if mode == expected_mode:
                    status = AgentCheck.OK
                    message = u"Server is in %s mode" % mode
                else:
                    status = AgentCheck.CRITICAL
                    message = u"Server is in %s mode but check expects %s mode" \
                              % (mode, expected_mode)
                self.service_check('zookeeper.mode', status, message=message,
                                   tags=sc_tags)

        if zk_version and LooseVersion(zk_version) > LooseVersion("3.4.0"):
            try:
                mntr_out = self._send_command('mntr', *cx_args)
            except ZKConnectionFailure:
                self.increment('zookeeper.timeouts')
                if report_instance_mode:
                    self.report_instance_mode(hostname, 'down', tags)
                raise
            except Exception as e:
                self.warning(e)
                self.increment('zookeeper.datamonitor_client_exception')
                if report_instance_mode:
                    self.report_instance_mode(hostname, 'unknown', tags)
                raise
            else:
                metrics, mode = self.parse_mntr(mntr_out)
                mode_tag = "mode:%s" % mode
                if mode != 'inactive':
                    for metric, value, m_type in metrics:
                        submit_metric = getattr(self, m_type)
                        submit_metric(metric, value, tags=tags + [mode_tag])

                if report_instance_mode:
                    self.report_instance_mode(hostname, mode, tags)

    def report_instance_mode(self, hostname, mode, tags):
        gauges = defaultdict(int)
        if mode not in self.STATUS_TYPES:
            mode = "unknown"

        tags = tags + ['mode:%s' % mode]
        self.set('zookeeper.instances', hostname, tags=tags)
        gauges[mode] = 1

        for k, v in gauges.iteritems():
            gauge_name = 'zookeeper.instances.%s' % k
            self.gauge(gauge_name, v)

    def _send_command(self, command, host, port, timeout):
        sock = socket.socket()
        sock.settimeout(timeout)
        buf = StringIO()
        chunk_size = 1024
        try:
            try:

                sock.connect((host, port))
                sock.sendall(command)

                chunk = sock.recv(chunk_size)
                buf.write(chunk)
                num_reads = 1
                max_reads = 10000
                while chunk:
                    if num_reads > max_reads:
                        raise Exception("Read %s bytes before exceeding max reads of %s. "
                                        % (buf.tell(), max_reads))
                    chunk = sock.recv(chunk_size)
                    buf.write(chunk)
                    num_reads += 1
            except (socket.timeout, socket.error):
                raise ZKConnectionFailure()
        finally:
            sock.close()
        return buf

    def parse_stat(self, buf):

        metrics = []
        buf.seek(0)

        start_line = buf.readline()
        match = self.version_pattern.match(start_line)
        if match is None:
            return (None, None, "inactive", None)
            raise Exception("Could not parse version from stat command output: %s" % start_line)
        else:
            version_tuple = match.groups()
        has_connections_val = version_tuple >= ('3', '4', '4')
        version = "%s.%s.%s" % version_tuple

        buf.readline()
        connections = 0
        client_line = buf.readline().strip()
        if client_line:
            connections += 1
        while client_line:
            client_line = buf.readline().strip()
            if client_line:
                connections += 1

        _, value = buf.readline().split(':')
        l_min, l_avg, l_max = [int(v) for v in value.strip().split('/')]
        metrics.append(ZKMetric('zookeeper.latency.min', l_min))
        metrics.append(ZKMetric('zookeeper.latency.avg', l_avg))
        metrics.append(ZKMetric('zookeeper.latency.max', l_max))

        _, value = buf.readline().split(':')
        metrics.append(ZKMetric('zookeeper.bytes_received', long(value.strip())))

        _, value = buf.readline().split(':')
        metrics.append(ZKMetric('zookeeper.bytes_sent', long(value.strip())))

        if has_connections_val:

            _, value = buf.readline().split(':')
            metrics.append(ZKMetric('zookeeper.connections', int(value.strip())))
        else:

            metrics.append(ZKMetric('zookeeper.connections', connections))

        _, value = buf.readline().split(':')

        metrics.append(ZKMetric('zookeeper.bytes_outstanding', long(value.strip())))
        metrics.append(ZKMetric('zookeeper.outstanding_requests', long(value.strip())))

        _, value = buf.readline().split(':')

        zxid = long(value.strip(), 16)

        zxid_bytes = struct.pack('>q', zxid)

        (zxid_epoch,) = struct.unpack('>i', zxid_bytes[0:4])

        (zxid_count,) = struct.unpack('>i', zxid_bytes[4:8])

        metrics.append(ZKMetric('zookeeper.zxid.epoch', zxid_epoch))
        metrics.append(ZKMetric('zookeeper.zxid.count', zxid_count))

        _, value = buf.readline().split(':')
        mode = value.strip().lower()
        tags = [u'mode:' + mode]

        _, value = buf.readline().split(':')
        metrics.append(ZKMetric('zookeeper.nodes', long(value.strip())))

        return metrics, tags, mode, version

    def parse_mntr(self, buf):

        buf.seek(0)
        first = buf.readline()
        if first == 'This ZooKeeper instance is not currently serving requests':
            return (None, 'inactive')

        metrics = []
        mode = 'inactive'

        for line in buf:
            try:
                key, value = line.split()

                if key == "zk_server_state":
                    mode = value.lower()
                    continue

                metric_name = self._normalize_metric_label(key)
                metric_type = "rate" if key in self._MNTR_RATES else "gauge"
                metric_value = int(value)
                metrics.append(ZKMetric(metric_name, metric_value, metric_type))

            except ValueError:
                self.log.warning(
                    u"Cannot format `mntr` value. key={key}, value{value}".format(
                        key=key, value=value
                    )
                )
                continue
            except Exception:
                self.log.exception(
                    u"Unexpected exception occurred while parsing `mntr` command content:\n"
                    u"{buf}".format(
                        buf=buf
                    )
                )

        return (metrics, mode)

    def _normalize_metric_label(self, key):
        if re.match('zk', key):
            key = key.replace('zk', 'zookeeper', 1)
        return key.replace('_', '.', 1)
