import os
import re

import simplejson as json

from checks import AgentCheck
from config import _is_affirmative
from utils.subprocess_output import get_subprocess_output


class Ceph(AgentCheck):
    DEFAULT_CEPH_CMD = '/usr/bin/ceph'
    DEFAULT_CEPH_CLUSTER = 'ceph'
    NAMESPACE = "openstack.ceph"

    def _collect_raw(self, ceph_cmd, ceph_cluster, instance):

        use_sudo = _is_affirmative(instance.get('use_sudo', False))
        ceph_args = []
        if use_sudo:
            test_sudo = os.popen('setsid sudo -l < /dev/null').read()
            if test_sudo != 0:
                raise Exception('The monitor-agent user does not have sudo access')
            ceph_args = ['sudo', ceph_cmd]
        else:
            ceph_args = ['sudo', ceph_cmd]
        ceph_args += ["--cluster", ceph_cluster]
        args = ceph_args + ['version']
        try:
            output, _, _ = get_subprocess_output(args, self.log)
        except Exception, e:
            raise Exception('Unable to run cmd=%s: %s' % (' '.join(args), str(e)))
        raw = {}
        for cmd in ('mon_status', 'status', 'df detail', 'osd stat', 'osd pool stats', 'health', 'mds stat'):
            try:
                args = ceph_args + cmd.split() + ['-fjson']
                output, _, _ = get_subprocess_output(args, self.log)
                res = json.loads(output)
            except Exception as e:
                self.log.warning('Unable to parse data from cmd=%s: %s' % (cmd, str(e)))
                continue
            name = cmd.replace(' ', '_')
            raw[name] = res
        return raw

    def _extract_tags(self, raw, instance):

        tags = instance.get('tags', [])
        if 'mon_status' in raw:
            fsid = raw['mon_status']['monmap']['fsid']
            tags.append(self.NAMESPACE + '_fsid:%s' % fsid)
            tags.append(self.NAMESPACE + '_mon_state:%s' % raw['mon_status']['state'])
        return tags

    def _publish(self, raw, func, keyspec, tags):
        try:
            for k in keyspec:
                raw = raw[k]
            func(self.NAMESPACE + '.' + k, raw, tags)
        except KeyError:
            return

    def _extract_metrics(self, raw, tags):
        if raw:
            try:
                for osdperf in raw['osd_perf']['osd_perf_infos']:
                    local_tags = tags + ['ceph_osd:osd%s' % osdperf['id']]
                    self._publish(osdperf, self.gauge, ['perf_stats', 'apply_latency_ms'], local_tags)
                    self._publish(osdperf, self.gauge, ['perf_stats', 'commit_latency_ms'], local_tags)
            except KeyError:
                self.log.debug('Error retrieving osdperf metrics')

            try:
                health = {'num_near_full_osds': 0, 'num_full_osds': 0}
                if raw['health_detail']['summary'] != []:
                    for osdhealth in raw['health_detail']['detail']:
                        osd, pct = self._osd_pct_used(osdhealth)
                        if osd:
                            local_tags = tags + ['ceph_osd:%s' % osd.replace('.', '')]

                            if 'near' in osdhealth:
                                health['num_near_full_osds'] += 1
                                local_health = {'osd.pct_used': pct}
                                self._publish(local_health, self.gauge, ['osd.pct_used'], local_tags)
                            else:
                                health['num_full_osds'] += 1
                                local_health = {'osd.pct_used': pct}
                                self._publish(local_health, self.gauge, ['osd.pct_used'], local_tags)

                self._publish(health, self.gauge, ['num_full_osds'], tags)
                self._publish(health, self.gauge, ['num_near_full_osds'], tags)
            except KeyError:
                self.log.debug('Error retrieving health metrics')

            try:
                for osdinfo in raw['osd_pool_stats']:
                    name = osdinfo.get('pool_name')
                    local_tags = tags + ['ceph_pool:%s' % name]
                    ops = 0
                    try:
                        self._publish(osdinfo, self.gauge, ['client_io_rate', 'read_op_per_sec'], local_tags)
                        ops += osdinfo['client_io_rate']['read_op_per_sec']
                    except KeyError:
                        osdinfo['client_io_rate'].update({'read_op_per_sec': 0})
                        self._publish(osdinfo, self.gauge, ['client_io_rate', 'read_op_per_sec'], local_tags)

                    try:
                        self._publish(osdinfo, self.gauge, ['client_io_rate', 'write_op_per_sec'], local_tags)
                        ops += osdinfo['client_io_rate']['write_op_per_sec']
                    except KeyError:
                        osdinfo['client_io_rate'].update({'write_op_per_sec': 0})
                        self._publish(osdinfo, self.gauge, ['client_io_rate', 'write_op_per_sec'], local_tags)

                    try:
                        osdinfo['client_io_rate']['op_per_sec']
                        self._publish(osdinfo, self.gauge, ['client_io_rate', 'op_per_sec'], local_tags)
                    except KeyError:
                        osdinfo['client_io_rate'].update({'op_per_sec': ops})
                        self._publish(osdinfo, self.gauge, ['client_io_rate', 'op_per_sec'], local_tags)

                    try:
                        osdinfo['client_io_rate']['read_bytes_sec']
                        self._publish(osdinfo, self.gauge, ['client_io_rate', 'read_bytes_sec'], local_tags)
                    except KeyError:
                        osdinfo['client_io_rate'].update({'read_bytes_sec': 0})
                        self._publish(osdinfo, self.gauge, ['client_io_rate', 'read_bytes_sec'], local_tags)

                    try:
                        osdinfo['client_io_rate']['write_bytes_sec']
                        self._publish(osdinfo, self.gauge, ['client_io_rate', 'write_bytes_sec'], local_tags)
                    except KeyError:
                        osdinfo['client_io_rate'].update({'write_bytes_sec': 0})
                        self._publish(osdinfo, self.gauge, ['client_io_rate', 'write_bytes_sec'], local_tags)
            except KeyError:
                self.log.debug('Error retrieving osd_pool_stats metrics')

            try:
                pgstatus = raw['status']['pgmap']
                self._publish(pgstatus, self.gauge, ['num_pgs'], tags)
                for pgstate in pgstatus['pgs_by_state']:
                    s_name = pgstate['state_name'].replace("+", "_")
                    self.gauge(self.NAMESPACE + '.pgstate.' + s_name, pgstate['count'], tags)
            except KeyError:
                self.log.debug('Error retrieving pgstatus metrics')

            try:
                num_mons = len(raw['mon_status']['monmap']['mons'])
                self.gauge(self.NAMESPACE + '.num_mons', num_mons, tags)
            except KeyError:
                self.log.debug('Error retrieving mon_status metrics')

            try:
                stats = raw['df_detail']['stats']
                self._publish(stats, self.gauge, ['total_objects'], tags)
                used = float(stats['total_used_bytes'])
                total = float(stats['total_bytes'])
                if total > 0:
                    self.gauge(self.NAMESPACE + '.aggregate_pct_used', 100.0 * used / total, tags)

                l_pools = raw['df_detail']['pools']
                self.gauge(self.NAMESPACE + '.num_pools', len(l_pools), tags)
                for pdata in l_pools:
                    local_tags = list(tags + [self.NAMESPACE + '_pool:%s' % pdata['name']])
                    stats = pdata['stats']
                    used = float(stats['bytes_used'])
                    avail = float(stats['max_avail'])
                    total = used + avail
                    if total > 0:
                        self.gauge(self.NAMESPACE + '.pct_used', 100.0 * used / total, local_tags)
                    self.gauge(self.NAMESPACE + '.num_objects', stats['objects'], local_tags)
                    self.rate(self.NAMESPACE + '.read_bytes', stats['rd_bytes'], local_tags)
                    self.rate(self.NAMESPACE + '.write_bytes', stats['wr_bytes'], local_tags)

            except (KeyError, ValueError):
                self.log.debug('Error retrieving df_detail metrics')

            try:
                osdstatus = raw['status']['osdmap']['osdmap']
                self._publish(osdstatus, self.gauge, ['num_osds'], tags)
                self._publish(osdstatus, self.gauge, ['num_in_osds'], tags)
                self._publish(osdstatus, self.gauge, ['num_up_osds'], tags)

            except KeyError:
                self.log.debug('Error retrieving osdstatus metrics')

            try:
                stats = raw['df_detail']['stats']
                total = float(stats['total_bytes'])
                used = float(stats['total_used_bytes'])
                avail = float(stats['total_avail_bytes'])
                self.gauge("openstack.ceph.cluster.space_used", used / 1024.0 / 1024.0 / 1024.0, tags)
                self.gauge("openstack.ceph.cluster.space_total", total / 1024.0 / 1024.0 / 1024.0, tags)
                self.gauge("openstack.ceph.cluster.space_avail", avail / 1024.0 / 1024.0 / 1024.0, tags)
                l_pools = raw['df_detail']['pools']
                for pool in l_pools:
                    stats = pool['stats']
                    self.rate("openstack.ceph.pg.stat_iops_wr." + pool['name'], stats['wr_bytes'], tags)
                    self.rate("openstack.ceph.pg.stat_iops_rd." + pool['name'], stats['rd_bytes'], tags)
                    self.gauge("openstack.ceph.pg.stat_write." + pool['name'], stats['wr'], tags)
                    self.gauge("openstack.ceph.pg.stat_read." + pool['name'], stats['rd'], tags)
            except KeyError:
                self.log.debug('Error retrieving df detail metrics')

            try:
                mons_list = raw['health']['health']['health_services']
                for mon_dict in mons_list:
                    if mon_dict.has_key('mons'):
                        mon_detail = mon_dict['mons']
                        for mon in mon_detail:
                            self.gauge("openstack.ceph." + "cluster.usage." + mon['name'], 100 - mon['avail_percent'],
                                       tags)
            except KeyError:
                self.log.debug('Error retrieving  cluster usage metrics')
            try:
                num_osds = raw['osd_stat']['num_osds']
                self.gauge("openstack.ceph.blocked.osd", num_osds, tags)
            except KeyError:
                self.log.debug('Error retrieving block osd num metrics')

    def _osd_pct_used(self, health):
        pct = re.compile('\d+%').findall(health)
        osd = re.compile('osd.\d+').findall(health)
        if len(pct) > 0 and len(osd) > 0:
            return osd[0], int(pct[0][:-1])
        else:
            return None, None

    def _perform_service_checks(self, raw, tags):
        if 'status' in raw:
            s_status = raw['status']['health']['overall_status']
            if s_status.find('_OK') != -1:
                status = AgentCheck.OK
            else:
                status = AgentCheck.CRITICAL
            self.service_check(self.NAMESPACE + '.overall_status', status, tags)
        try:
            mds_standbys = raw['mds_stat']['fsmap']['standbys']
            for mds_standby in mds_standbys:
                mds_status = mds_standby['state']
                mds_stauts = AgentCheck.OK if 'up:' in mds_status else AgentCheck.CRITICAL
                self.service_check("openstack.ceph.msd.status." + mds_standby['name'], mds_stauts, tags)
        except KeyError:
            self.log.debug('Error retrieving msd status metrics')

        try:
            mons = raw['status']['health']['timechecks']['mons']
            for mon in mons:
                health_status = AgentCheck.OK if "_OK" in mon['health'] else AgentCheck.CRITICAL
                self.service_check('openstack.ceph.mon.status.' + mon['name'], health_status, tags)
        except KeyError:
            self.log.debug('Error retrieving mons status metrics')

        try:
            num_osds = raw['osd_stat']['num_osds']
            num_up_osds = raw['osd_stat']['num_up_osds']
            self.gauge("openstack.ceph.blocked.osd", num_osds, tags)
            if num_osds == num_up_osds:
                self.service_check("openstack.ceph.osd.status", AgentCheck.OK, tags)
            else:
                self.service_check("openstack.ceph.osd.status", AgentCheck.CRITICAL, tags)
        except KeyError:
            self.log.debug('Error retrieving osd status metrics')

        try:
            health = raw['status']['health']['overall_status']
            if health.find('_ok') != -1:
                self.service_check("openstack.ceph.health.status", AgentCheck.OK, tags)
            else:
                self.service_check("openstack.ceph.health.status", AgentCheck.CRITICAL, tags)
        except KeyError:
            self.log.debug('Error retrieving health metrics')

    def _osd_pct_used(self, health):
        pct = re.compile('\d+%').findall(health)
        osd = re.compile('osd.\d+').findall(health)
        if len(pct) > 0 and len(osd) > 0:
            return osd[0], int(pct[0][:-1])
        else:
            return None, None

    def check(self, instance):
        ceph_cmd = instance.get('ceph_cmd') or self.DEFAULT_CEPH_CMD
        ceph_cluster = instance.get('ceph_cluster') or self.DEFAULT_CEPH_CLUSTER
        raw = self._collect_raw(ceph_cmd, ceph_cluster, instance)
        tags = self._extract_tags(raw, instance)
        self._extract_metrics(raw, tags)
        self._perform_service_checks(raw, tags)
