import re

from checks import AgentCheck
from utils.platform import Platform
from utils.subprocess_output import (
    get_subprocess_output,
    SubprocessOutputEmptyError,
)

BSD_TCP_METRICS = [
    (re.compile("^\s*(\d+) data packets \(\d+ bytes\) retransmitted\s*$"), 'system.net.tcp.retrans_packs'),
    (re.compile("^\s*(\d+) packets sent\s*$"), 'system.net.tcp.sent_packs'),
    (re.compile("^\s*(\d+) packets received\s*$"), 'system.net.tcp.rcv_packs')
]

SOLARIS_TCP_METRICS = [
    (re.compile("\s*tcpRetransSegs\s*=\s*(\d+)\s*"), 'system.net.tcp.retrans_segs'),
    (re.compile("\s*tcpOutDataSegs\s*=\s*(\d+)\s*"), 'system.net.tcp.in_segs'),
    (re.compile("\s*tcpInSegs\s*=\s*(\d+)\s*"), 'system.net.tcp.out_segs')
]


class Network(AgentCheck):
    SOURCE_TYPE_NAME = 'system'

    TCP_STATES = {
        "ss": {
            "ESTAB": "established",
            "SYN-SENT": "opening",
            "SYN-RECV": "opening",
            "FIN-WAIT-1": "closing",
            "FIN-WAIT-2": "closing",
            "TIME-WAIT": "time_wait",
            "UNCONN": "closing",
            "CLOSE-WAIT": "closing",
            "LAST-ACK": "closing",
            "LISTEN": "listening",
            "CLOSING": "closing",
        },
        "netstat": {
            "ESTABLISHED": "established",
            "SYN_SENT": "opening",
            "SYN_RECV": "opening",
            "FIN_WAIT1": "closing",
            "FIN_WAIT2": "closing",
            "TIME_WAIT": "time_wait",
            "CLOSE": "closing",
            "CLOSE_WAIT": "closing",
            "LAST_ACK": "closing",
            "LISTEN": "listening",
            "CLOSING": "closing",
        }
    }

    CX_STATE_GAUGE = {
        ('udp4', 'connections'): 'system.net.udp4.connections',
        ('udp6', 'connections'): 'system.net.udp6.connections',
        ('tcp4', 'established'): 'system.net.tcp4.established',
        ('tcp4', 'opening'): 'system.net.tcp4.opening',
        ('tcp4', 'closing'): 'system.net.tcp4.closing',
        ('tcp4', 'listening'): 'system.net.tcp4.listening',
        ('tcp4', 'time_wait'): 'system.net.tcp4.time_wait',
        ('tcp6', 'established'): 'system.net.tcp6.established',
        ('tcp6', 'opening'): 'system.net.tcp6.opening',
        ('tcp6', 'closing'): 'system.net.tcp6.closing',
        ('tcp6', 'listening'): 'system.net.tcp6.listening',
        ('tcp6', 'time_wait'): 'system.net.tcp6.time_wait',
    }

    def __init__(self, name, init_config, agentConfig, instances=None):
        AgentCheck.__init__(self, name, init_config, agentConfig, instances=instances)
        if instances is not None and len(instances) > 1:
            raise Exception("Network check only supports one configured instance.")

    def check(self, instance):
        if instance is None:
            instance = {}

        self._excluded_ifaces = instance.get('excluded_interfaces', [])
        self._collect_cx_state = instance.get('collect_connection_state', False)

        self._exclude_iface_re = None
        exclude_re = instance.get('excluded_interface_re', None)
        if exclude_re:
            self.log.debug("Excluding network devices matching: %s" % exclude_re)
            self._exclude_iface_re = re.compile(exclude_re)

        if Platform.is_linux():
            self._check_linux(instance)
        elif Platform.is_bsd():
            self._check_bsd(instance)
        elif Platform.is_solaris():
            self._check_solaris(instance)

    def _submit_devicemetrics(self, iface, vals_by_metric):
        if iface in self._excluded_ifaces or (self._exclude_iface_re and self._exclude_iface_re.match(iface)):
            return False

        expected_metrics = [
            'bytes_rcvd',
            'bytes_sent',
            'packets_in.count',
            'packets_in.error',
            'packets_out.count',
            'packets_out.error',
        ]
        for m in expected_metrics:
            assert m in vals_by_metric
        assert len(vals_by_metric) == len(expected_metrics)

        count = 0
        for metric, val in vals_by_metric.iteritems():
            self.rate('system.net.%s' % metric, val, device_name=iface)
            count += 1
        self.log.debug("tracked %s network metrics for interface %s" % (count, iface))

    def _parse_value(self, v):
        if v == "-":
            return 0
        else:
            try:
                return long(v)
            except ValueError:
                return 0

    def _submit_regexed_values(self, output, regex_list):
        lines = output.splitlines()
        for line in lines:
            for regex, metric in regex_list:
                value = re.match(regex, line)
                if value:
                    self.rate(metric, self._parse_value(value.group(1)))

    def _check_linux(self, instance):
        if self._collect_cx_state:
            try:
                self.log.debug("Using `ss` to collect connection state")
                for ip_version in ['4', '6']:
                    output, _, _ = get_subprocess_output(["ss", "-n", "-u", "-t", "-a", "-{0}".format(ip_version)],
                                                         self.log)
                    lines = output.splitlines()

                    metrics = self._parse_linux_cx_state(lines[1:], self.TCP_STATES['ss'], 1, ip_version=ip_version)
                    for stat, metric in self.CX_STATE_GAUGE.iteritems():
                        if stat[0].endswith(ip_version):
                            self.gauge(metric, metrics.get(metric))

            except OSError:
                self.log.info("`ss` not found: using `netstat` as a fallback")
                output, _, _ = get_subprocess_output(["netstat", "-n", "-u", "-t", "-a"], self.log)
                lines = output.splitlines()

                metrics = self._parse_linux_cx_state(lines[2:], self.TCP_STATES['netstat'], 5)
                for metric, value in metrics.iteritems():
                    self.gauge(metric, value)
            except SubprocessOutputEmptyError:
                self.log.exception("Error collecting connection stats.")

        proc = open('/proc/net/dev', 'r')
        try:
            lines = proc.readlines()
        finally:
            proc.close()
        for l in lines[2:]:
            cols = l.split(':', 1)
            x = cols[1].split()
            if self._parse_value(x[0]) or self._parse_value(x[8]):
                iface = cols[0].strip()
                metrics = {
                    'bytes_rcvd': self._parse_value(x[0]),
                    'bytes_sent': self._parse_value(x[8]),
                    'packets_in.count': self._parse_value(x[1]),
                    'packets_in.error': self._parse_value(x[2]) + self._parse_value(x[3]),
                    'packets_out.count': self._parse_value(x[9]),
                    'packets_out.error': self._parse_value(x[10]) + self._parse_value(x[11]),
                }
                self._submit_devicemetrics(iface, metrics)

        try:
            proc = open('/proc/net/snmp', 'r')
            try:
                lines = proc.readlines()
            finally:
                proc.close()

            tcp_lines = [line for line in lines if line.startswith('Tcp:')]
            udp_lines = [line for line in lines if line.startswith('Udp:')]

            tcp_column_names = tcp_lines[0].strip().split()
            tcp_values = tcp_lines[1].strip().split()
            tcp_metrics = dict(zip(tcp_column_names, tcp_values))

            udp_column_names = udp_lines[0].strip().split()
            udp_values = udp_lines[1].strip().split()
            udp_metrics = dict(zip(udp_column_names, udp_values))

            assert (tcp_metrics['Tcp:'] == 'Tcp:')

            tcp_metrics_name = {
                'RetransSegs': 'system.net.tcp.retrans_segs',
                'InSegs': 'system.net.tcp.in_segs',
                'OutSegs': 'system.net.tcp.out_segs'
            }

            for key, metric in tcp_metrics_name.iteritems():
                self.rate(metric, self._parse_value(tcp_metrics[key]))

            assert (udp_metrics['Udp:'] == 'Udp:')

            udp_metrics_name = {
                'InDatagrams': 'system.net.udp.in_datagrams',
                'NoPorts': 'system.net.udp.no_ports',
                'InErrors': 'system.net.udp.in_errors',
                'OutDatagrams': 'system.net.udp.out_datagrams',
                'RcvbufErrors': 'system.net.udp.rcv_buf_errors',
                'SndbufErrors': 'system.net.udp.snd_buf_errors'
            }
            for key, metric in udp_metrics_name.iteritems():
                if key in udp_metrics:
                    self.rate(metric, self._parse_value(udp_metrics[key]))

        except IOError:
            self.log.debug("Unable to read /proc/net/snmp.")

    def _parse_linux_cx_state(self, lines, tcp_states, state_col, ip_version=None):
        metrics = dict.fromkeys(self.CX_STATE_GAUGE.values(), 0)
        for l in lines:
            cols = l.split()
            if cols[0].startswith('tcp'):
                protocol = "tcp{0}".format(ip_version) if ip_version else ("tcp4", "tcp6")[cols[0] == "tcp6"]
                if cols[state_col] in tcp_states:
                    metric = self.CX_STATE_GAUGE[protocol, tcp_states[cols[state_col]]]
                    metrics[metric] += 1
            elif cols[0].startswith('udp'):
                protocol = "udp{0}".format(ip_version) if ip_version else ("udp4", "udp6")[cols[0] == "udp6"]
                metric = self.CX_STATE_GAUGE[protocol, 'connections']
                metrics[metric] += 1

        return metrics

    def _check_bsd(self, instance):
        netstat_flags = ['-i', '-b']

        if Platform.is_freebsd():
            netstat_flags.append('-W')

        try:
            output, _, _ = get_subprocess_output(["netstat"] + netstat_flags, self.log)
            lines = output.splitlines()

            headers = lines[0].split()

            for h in ("Ipkts", "Ierrs", "Ibytes", "Opkts", "Oerrs", "Obytes", "Coll"):
                if h not in headers:
                    self.logger.error("%s not found in %s; cannot parse" % (h, headers))
                    return False

            current = None
            for l in lines[1:]:

                if "Name" in l:
                    break

                x = l.split()
                if len(x) == 0:
                    break

                iface = x[0]
                if iface.endswith("*"):
                    iface = iface[:-1]
                if iface == current:
                    continue
                else:
                    current = iface

                if self._parse_value(x[-5]) or self._parse_value(x[-2]):
                    iface = current
                    metrics = {
                        'bytes_rcvd': self._parse_value(x[-5]),
                        'bytes_sent': self._parse_value(x[-2]),
                        'packets_in.count': self._parse_value(x[-7]),
                        'packets_in.error': self._parse_value(x[-6]),
                        'packets_out.count': self._parse_value(x[-4]),
                        'packets_out.error': self._parse_value(x[-3]),
                    }
                    self._submit_devicemetrics(iface, metrics)
        except SubprocessOutputEmptyError:
            self.log.exception("Error collecting connection stats.")

        try:
            netstat, _, _ = get_subprocess_output(["netstat", "-s", "-p" "tcp"], self.log)

            self._submit_regexed_values(netstat, BSD_TCP_METRICS)
        except SubprocessOutputEmptyError:
            self.log.exception("Error collecting TCP stats.")

    def _check_solaris(self, instance):
        try:
            netstat, _, _ = get_subprocess_output(["kstat", "-p", "link:0:"], self.log)
            metrics_by_interface = self._parse_solaris_netstat(netstat)
            for interface, metrics in metrics_by_interface.iteritems():
                self._submit_devicemetrics(interface, metrics)
        except SubprocessOutputEmptyError:
            self.log.exception("Error collecting kstat stats.")

        try:
            netstat, _, _ = get_subprocess_output(["netstat", "-s", "-P" "tcp"], self.log)
            self._submit_regexed_values(netstat, SOLARIS_TCP_METRICS)
        except SubprocessOutputEmptyError:
            self.log.exception("Error collecting TCP stats.")

    def _parse_solaris_netstat(self, netstat_output):
        metric_by_solaris_name = {
            'rbytes64': 'bytes_rcvd',
            'obytes64': 'bytes_sent',
            'ipackets64': 'packets_in.count',
            'ierrors': 'packets_in.error',
            'opackets64': 'packets_out.count',
            'oerrors': 'packets_out.error',
        }

        lines = [l for l in netstat_output.splitlines() if len(l) > 0]

        metrics_by_interface = {}

        for l in lines:
            cols = l.split()
            link, n, iface, name = cols[0].split(":")
            assert link == "link"

            ddname = metric_by_solaris_name.get(name, None)
            if ddname is None:
                continue

            metrics = metrics_by_interface.get(iface, {})
            metrics[ddname] = self._parse_value(cols[1])
            metrics_by_interface[iface] = metrics

        return metrics_by_interface
