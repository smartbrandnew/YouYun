import collections
import logging
import pprint
import socket
import sys
import time

import checks.system.unix as u
import checks.system.win32 as w32
import modules
from checks import AGENT_METRICS_CHECK_NAME, AgentCheck, create_service_check
from checks.check_status import (
    CheckStatus,
    CollectorStatus,
    EmitterStatus,
    STATUS_ERROR,
    STATUS_OK,
)
from checks.datamonitor import DdForwarder, monitorstreams
from checks.ganglia import Ganglia
from config import get_system_stats, get_version
from gohai import get_gohai_data
from util import (
    EC2,
    GCE,
    get_os,
    Timer,
    get_ip,
    get_machine_type,
)
from util import get_uuid
from utils.jmx import JMXFiles
from utils.logger import log_exceptions
from utils.platform import Platform

log = logging.getLogger(__name__)

FLUSH_LOGGING_PERIOD = 10
FLUSH_LOGGING_INITIAL = 5
DD_CHECK_TAG = 'dd_check:{0}'


class AgentPayload(collections.MutableMapping):
    METADATA_KEYS = frozenset(['meta', 'tags', 'host-tags', 'systemStats',
                               'agent_checks', 'external_host_tags'])

    DUPLICATE_KEYS = frozenset(['apiKey', 'agentVersion'])

    COMMON_ENDPOINT = ''
    DATA_ENDPOINT = 'metrics'
    METADATA_ENDPOINT = 'metadata'

    def __init__(self):
        self.data_payload = dict()
        self.meta_payload = dict()

    @property
    def payload(self):

        res = self.data_payload.copy()
        res.update(self.meta_payload)

        return res

    def __getitem__(self, key):
        if key in self.METADATA_KEYS:
            return self.meta_payload[key]
        else:
            return self.data_payload[key]

    def __setitem__(self, key, value):
        if key in self.DUPLICATE_KEYS:
            self.data_payload[key] = value
            self.meta_payload[key] = value
        elif key in self.METADATA_KEYS:
            self.meta_payload[key] = value
        else:
            self.data_payload[key] = value

    def __delitem__(self, key):
        if key in self.DUPLICATE_KEYS:
            del self.data_payload[key]
            del self.meta_payload[key]
        elif key in self.METADATA_KEYS:
            del self.meta_payload[key]
        else:
            del self.data_payload[key]

    def __iter__(self):
        for item in self.data_payload:
            yield item
        for item in self.meta_payload:
            yield item

    def __len__(self):
        return len(self.data_payload) + len(self.meta_payload)

    def emit(self, log, config, emitters, continue_running, merge_payloads=True):

        statuses = []

        def _emit_payload(payload, endpoint):
            statuses = []
            for emitter in emitters:
                if not continue_running:
                    return statuses
                name = emitter.__name__
                emitter_status = EmitterStatus(name)
                try:
                    emitter(payload, log, config, endpoint)
                except Exception, e:
                    log.exception("Error running emitter: %s"
                                  % emitter.__name__)
                    emitter_status = EmitterStatus(name, e)
                statuses.append(emitter_status)
            return statuses

        if merge_payloads:
            statuses.extend(_emit_payload(self.payload, self.COMMON_ENDPOINT))
        else:
            statuses.extend(_emit_payload(self.data_payload, self.DATA_ENDPOINT))
            statuses.extend(_emit_payload(self.meta_payload, self.METADATA_ENDPOINT))

        return statuses


class Collector(object):
    def __init__(self, agentConfig, emitters, systemStats, hostname):
        self.ip = get_ip(agentConfig)
        self.emit_duration = None
        self.agentConfig = agentConfig
        self.hostname = hostname
        self.agentConfig['system_stats'] = systemStats
        self.os = get_os()
        self.plugins = None
        self.emitters = emitters
        self.check_timings = agentConfig.get('check_timings')
        self.push_times = {
            'host_metadata': {
                'start': time.time(),
                'interval': int(agentConfig.get('metadata_interval', 4 * 60 * 60))
            },
            'external_host_tags': {
                'start': time.time() - 3 * 60,
                'interval': int(agentConfig.get('external_host_tags', 5 * 60))
            },
            'agent_checks': {
                'start': time.time(),
                'interval': int(agentConfig.get('agent_checks_interval', 10 * 60))
            },
        }
        socket.setdefaulttimeout(15)
        self.run_count = 0
        self.continue_running = True
        self.hostname_metadata_cache = None
        self.initialized_checks_d = []
        self.init_failed_checks_d = {}

        self._unix_system_checks = {
            'io': u.IO(log),
            'load': u.Load(log),
            'memory': u.Memory(log),
            'processes': u.Processes(log),
            'cpu': u.Cpu(log),
            'system': u.System(log)
        }

        self._win32_system_checks = {
            'io': w32.IO(log),
            'proc': w32.Processes(log),
            'memory': w32.Memory(log),
            'network': w32.Network(log),
            'cpu': w32.Cpu(log),
            'system': w32.System(log)
        }

        self._ganglia = Ganglia(log)
        self._monitorstream = monitorstreams.init(log, self.agentConfig)
        self._ddforwarder = DdForwarder(log, self.agentConfig)

        self._agent_metrics = None

        self._metrics_checks = []

        for module_spec in [s.strip() for s in self.agentConfig.get('custom_checks', '').split(',')]:
            if len(module_spec) == 0:
                continue
            try:
                self._metrics_checks.append(modules.load(module_spec, 'Check')(log))
                log.info("Registered custom check %s" % module_spec)
                log.warning(
                    "Old format custom checks are deprecated. They should be moved to the checks.d interface as old custom checks will be removed in a next version")
            except Exception:
                log.exception('Unable to load custom check module %s' % module_spec)

    def stop(self):

        self.continue_running = False
        for check in self.initialized_checks_d:
            check.stop()

    @staticmethod
    def _stats_for_display(raw_stats):
        return pprint.pformat(raw_stats, indent=4)

    @log_exceptions(log)
    def run(self, checksd=None, start_event=True, configs_reloaded=False):

        log.debug("Found {num_checks} checks".format(num_checks=len(checksd['initialized_checks'])))
        timer = Timer()
        if not Platform.is_windows():
            cpu_clock = time.clock()
        self.run_count += 1
        log.debug("Starting collection run #%s" % self.run_count)

        if checksd:
            self.initialized_checks_d = checksd['initialized_checks']
            self.init_failed_checks_d = checksd['init_failed_checks']

        payload = AgentPayload()

        if not self._agent_metrics or configs_reloaded:
            for check in self.initialized_checks_d:
                if check.name == AGENT_METRICS_CHECK_NAME:
                    self._agent_metrics = check
                    self.initialized_checks_d.remove(check)
                    break

        self._build_payload(payload)

        metrics = payload['metrics']
        events = payload['events']
        service_checks = payload['service_checks']

        if Platform.is_windows():
            try:
                metrics.extend(self._win32_system_checks['memory'].check(self.agentConfig))
                metrics.extend(self._win32_system_checks['cpu'].check(self.agentConfig))
                metrics.extend(self._win32_system_checks['network'].check(self.agentConfig))
                metrics.extend(self._win32_system_checks['io'].check(self.agentConfig))
                metrics.extend(self._win32_system_checks['proc'].check(self.agentConfig))
                metrics.extend(self._win32_system_checks['system'].check(self.agentConfig))
            except Exception:
                log.exception('Unable to fetch Windows system metrics.')
        else:
            sys_checks = self._unix_system_checks

            load = sys_checks['load'].check(self.agentConfig)
            payload.update(load)

            system = sys_checks['system'].check(self.agentConfig)
            payload.update(system)

            memory = sys_checks['memory'].check(self.agentConfig)

            if memory:
                memstats = {
                    'memPhysUsed': memory.get('physUsed'),
                    'memPhysPctUsage': memory.get('physPctUsage'),
                    'memPhysFree': memory.get('physFree'),
                    'memPhysTotal': memory.get('physTotal'),
                    'memPhysUsable': memory.get('physUsable'),
                    'memSwapUsed': memory.get('swapUsed'),
                    'memSwapFree': memory.get('swapFree'),
                    'memSwapPctFree': memory.get('swapPctFree'),
                    'memSwapTotal': memory.get('swapTotal'),
                    'memCached': memory.get('physCached'),
                    'memBuffers': memory.get('physBuffers'),
                    'memShared': memory.get('physShared'),
                    'memSlab': memory.get('physSlab'),
                    'memPageTables': memory.get('physPageTables'),
                    'memSwapCached': memory.get('swapCached')
                }
                payload.update(memstats)

            ioStats = sys_checks['io'].check(self.agentConfig)
            if ioStats:
                payload['ioStats'] = ioStats

            processes = sys_checks['processes'].check(self.agentConfig)
            payload.update({'processes': processes})

            cpuStats = sys_checks['cpu'].check(self.agentConfig)
            if cpuStats:
                payload.update(cpuStats)

        gangliaData = self._ganglia.check(self.agentConfig)
        monitorstreamData = self._monitorstream.check(self.agentConfig)
        ddforwarderData = self._ddforwarder.check(self.agentConfig)

        if gangliaData is not False and gangliaData is not None:
            payload['ganglia'] = gangliaData

        if monitorstreamData:
            monitorstreamEvents = monitorstreamData.get('monitorstreamEvents', None)
            if monitorstreamEvents:
                if 'monitorstream' in payload['events']:
                    events['monitorstream'].extend(monitorstreamEvents)
                else:
                    events['monitorstream'] = monitorstreamEvents
                del monitorstreamData['monitorstreamEvents']

            payload.update(monitorstreamData)

        if ddforwarderData:
            payload['datamonitor'] = ddforwarderData

        for metrics_check in self._metrics_checks:
            res = metrics_check.check(self.agentConfig)
            if res:
                metrics.extend(res)

        check_statuses = []
        for check in self.initialized_checks_d:
            if not self.continue_running:
                return
            log.info("Running check %s" % check.name)
            instance_statuses = []
            metric_count = 0
            event_count = 0
            service_check_count = 0
            check_start_time = time.time()
            check_stats = None

            try:
                instance_statuses = check.run()

                current_check_metrics = check.get_metrics()
                current_check_events = check.get_events()
                check_stats = check._get_internal_profiling_stats()

                current_check_metadata = check.get_service_metadata()

                metrics.extend(current_check_metrics)
                if current_check_events:
                    if check.name not in events:
                        events[check.name] = current_check_events
                    else:
                        events[check.name] += current_check_events

                metric_count = len(current_check_metrics)
                event_count = len(current_check_events)

            except Exception:
                log.exception("Error running check %s" % check.name)

            check_status = CheckStatus(
                check.name, instance_statuses, metric_count,
                event_count, service_check_count, service_metadata=current_check_metadata,
                library_versions=check.get_library_info(),
                source_type_name=check.SOURCE_TYPE_NAME or check.name,
                check_stats=check_stats
            )

            service_check_tags = ["check:%s" % check.name]
            if check_status.status == STATUS_OK:
                status = AgentCheck.OK
            elif check_status.status == STATUS_ERROR:
                status = AgentCheck.CRITICAL
            check.service_check('datamonitor.agent.check_status', status, tags=service_check_tags)

            current_check_service_checks = check.get_service_checks()
            if current_check_service_checks:
                service_checks.extend(current_check_service_checks)
            service_check_count = len(current_check_service_checks)

            check_status.service_check_count = service_check_count
            check_statuses.append(check_status)

            check_run_time = time.time() - check_start_time
            log.debug("Check %s ran in %.2f s" % (check.name, check_run_time))

            if self.check_timings:
                metric = 'datamonitor.agent.check_run_time'
                meta = {'tags': ["check:%s" % check.name]}
                metrics.append((metric, time.time(), check_run_time, meta))

        for check_name, info in self.init_failed_checks_d.iteritems():
            if not self.continue_running:
                return
            check_status = CheckStatus(check_name, None, None, None, None,
                                       init_failed_error=info['error'],
                                       init_failed_traceback=info['error'])
            check_statuses.append(check_status)

        service_checks.append(create_service_check('datamonitor.agent.up', AgentCheck.OK,
                                                   hostname=self.hostname))

        payload['metrics'] = metrics
        payload['events'] = events
        payload['service_checks'] = service_checks

        self._populate_payload_metadata(payload, check_statuses, start_event)

        collect_duration = timer.step()

        if self._agent_metrics:
            metric_context = {
                'collection_time': collect_duration,
                'emit_time': self.emit_duration,
            }
            if not Platform.is_windows():
                metric_context['cpu_time'] = time.clock() - cpu_clock

            self._agent_metrics.set_metric_context(payload, metric_context)
            self._agent_metrics.run()
            agent_stats = self._agent_metrics.get_metrics()
            payload['metrics'].extend(agent_stats)
            if self.agentConfig.get('developer_mode'):
                log.debug("\n Agent developer mode stats: \n {0}".format(
                    Collector._stats_for_display(agent_stats))
                )
            self._agent_metrics.get_service_metadata()

        emitter_statuses = payload.emit(log, self.agentConfig, self.emitters,
                                        self.continue_running)
        self.emit_duration = timer.step()

        try:
            CollectorStatus(check_statuses, emitter_statuses,
                            self.hostname_metadata_cache).persist()
        except Exception:
            log.exception("Error persisting collector status")

        if self.run_count <= FLUSH_LOGGING_INITIAL or self.run_count % FLUSH_LOGGING_PERIOD == 0:
            log.info("Finished run #%s. Collection time: %ss. Emit time: %ss" %
                     (self.run_count, round(collect_duration, 2), round(self.emit_duration, 2)))
            if self.run_count == FLUSH_LOGGING_INITIAL:
                log.info("First flushes done, next flushes will be logged every %s flushes." %
                         FLUSH_LOGGING_PERIOD)
        else:
            log.debug("Finished run #%s. Collection time: %ss. Emit time: %ss" %
                      (self.run_count, round(collect_duration, 2), round(self.emit_duration, 2)))

        return payload

    @staticmethod
    def run_single_check(check, verbose=True):
        log.info("Running check %s" % check.name)
        instance_statuses = []
        metric_count = 0
        event_count = 0
        service_check_count = 0
        check_stats = None

        try:

            instance_statuses = check.run()

            current_check_metrics = check.get_metrics()
            current_check_events = check.get_events()
            current_service_checks = check.get_service_checks()
            current_service_metadata = check.get_service_metadata()

            check_stats = check._get_internal_profiling_stats()

            metric_count = len(current_check_metrics)
            event_count = len(current_check_events)
            service_check_count = len(current_service_checks)

            print "Metrics: \n{0}".format(pprint.pformat(current_check_metrics))
            print "Events: \n{0}".format(pprint.pformat(current_check_events))
            print "Service Checks: \n{0}".format(pprint.pformat(current_service_checks))
            print "Service Metadata: \n{0}".format(pprint.pformat(current_service_metadata))

        except Exception:
            log.exception("Error running check %s" % check.name)

        check_status = CheckStatus(
            check.name, instance_statuses, metric_count,
            event_count, service_check_count,
            library_versions=check.get_library_info(),
            source_type_name=check.SOURCE_TYPE_NAME or check.name,
            check_stats=check_stats
        )

        return check_status

    def _emit(self, payload):
        statuses = []
        for emitter in self.emitters:
            if not self.continue_running:
                return statuses
            name = emitter.__name__
            emitter_status = EmitterStatus(name)
            try:
                emitter(payload, log, self.agentConfig)
            except Exception, e:
                log.exception("Error running emitter: %s" % emitter.__name__)
                emitter_status = EmitterStatus(name, e)
            statuses.append(emitter_status)
        return statuses

    def _is_first_run(self):
        return self.run_count <= 1

    def _build_payload(self, payload):
        now = time.time()

        payload['ip'] = self.ip
        payload['collection_timestamp'] = now
        payload['os'] = self.os
        payload['python'] = sys.version
        payload['agentVersion'] = self.agentConfig['version']
        payload['apiKey'] = self.agentConfig['api_key']
        payload['events'] = {}
        payload['metrics'] = []
        payload['service_checks'] = []
        payload['resources'] = {}
        payload['internalHostname'] = self.hostname
        payload['uuid'] = get_uuid()
        payload['machine_type'] = get_machine_type()
        log.info('UUID: {0}'.format(payload['uuid']))
        log.info('Machine_Type: {0}'.format(payload['machine_type']))
        payload['host-tags'] = {}
        payload['external_host_tags'] = {}

    def _populate_payload_metadata(self, payload, check_statuses, start_event=True):
        now = time.time()

        if start_event and self._is_first_run():
            payload['systemStats'] = self.agentConfig.get('system_stats', {})
            payload['events']['System'] = [{
                'api_key': self.agentConfig['api_key'],
                'host': payload['internalHostname'],
                'timestamp': now,
                'event_type': 'Agent Startup',
                'msg_text': 'Version %s' % get_version()
            }]

        if self._should_send_additional_data('host_metadata'):
            payload['gohai'] = get_gohai_data()
            payload['systemStats'] = get_system_stats()
            payload['meta'] = self._get_hostname_metadata()
            log.info('GOHAI data: {0}'.format(payload['gohai']))

            self.hostname_metadata_cache = payload['meta']
            host_tags = []
            if self.agentConfig['tags'] is not None:
                host_tags.extend([tag.strip()
                                  for tag in self.agentConfig['tags'].split(",")])

            if self.agentConfig['collect_ec2_tags']:
                host_tags.extend(EC2.get_tags(self.agentConfig))

            if host_tags:
                payload['host-tags']['system'] = host_tags

            if self.agentConfig['create_dd_check_tags']:
                app_tags_list = [DD_CHECK_TAG.format(c.name) for c in self.initialized_checks_d]
                app_tags_list.extend([DD_CHECK_TAG.format(cname) for cname
                                      in JMXFiles.get_jmx_appnames()])

                if 'system' not in payload['host-tags']:
                    payload['host-tags']['system'] = []

                payload['host-tags']['system'].extend(app_tags_list)

            GCE_tags = GCE.get_tags(self.agentConfig)
            if GCE_tags is not None:
                payload['host-tags'][GCE.SOURCE_TYPE_NAME] = GCE_tags

            if self._is_first_run():
                log.info("Hostnames: %s, tags: %s" %
                         (repr(self.hostname_metadata_cache), payload['host-tags']))

        external_host_tags = []
        if self._should_send_additional_data('external_host_tags'):
            for check in self.initialized_checks_d:
                try:
                    getter = getattr(check, 'get_external_host_tags')
                    check_tags = getter()
                    external_host_tags.extend(check_tags)
                except AttributeError:
                    pass

        if external_host_tags:
            payload['external_host_tags'] = external_host_tags

        if self._should_send_additional_data('agent_checks'):
            agent_checks = []
            for check in check_statuses:
                if check.instance_statuses is not None:
                    for i, instance_status in enumerate(check.instance_statuses):
                        agent_checks.append(
                            (
                                check.name, check.source_type_name,
                                instance_status.instance_id,
                                instance_status.status,

                                instance_status.error or instance_status.warnings or "",
                                check.service_metadata[i]
                            )
                        )
                else:
                    agent_checks.append(
                        (
                            check.name, check.source_type_name,
                            "initialization",
                            check.status, repr(check.init_failed_error)
                        )
                    )
            payload['agent_checks'] = agent_checks
            payload['meta'] = self.hostname_metadata_cache

    def _get_hostname_metadata(self):

        metadata = EC2.get_metadata(self.agentConfig)
        if metadata.get('hostname'):
            metadata['ec2-hostname'] = metadata.get('hostname')
            del metadata['hostname']

        if self.agentConfig.get('hostname'):
            metadata['agent-hostname'] = self.agentConfig.get('hostname')
        else:
            try:
                metadata["socket-hostname"] = socket.gethostname()
            except Exception:
                pass
        try:
            metadata["socket-fqdn"] = socket.getfqdn()
        except Exception:
            pass

        metadata["hostname"] = self.hostname
        metadata["timezones"] = sanitize_tzname(time.tzname)

        host_aliases = GCE.get_host_aliases(self.agentConfig)
        if host_aliases:
            metadata['host_aliases'] = host_aliases

        return metadata

    def _should_send_additional_data(self, data_name):
        if self._is_first_run():
            return True
        now = time.time()
        if now - self.push_times[data_name]['start'] >= self.push_times[data_name]['interval']:
            log.debug('%s interval has passed. Sending it.' % data_name)
            self.push_times[data_name]['start'] = now
            return True

        return False


def sanitize_tzname(tzname):
    if tzname[0] == '\x93\x8c\x8b\x9e (\x95W\x8f\x80\x8e\x9e)':
        log.debug('tzname from TOKYO detected and converted')
        return ('JST', 'JST')
    elif tzname[0] == '\xd6\xd0\xb9\xfa\xb1\xea\xd7\xbc\xca\xb1\xbc\xe4':
        log.debug('tzname from CHINA detected and converted')
        return ('UTC', 'UTC')
    else:
        return tzname
