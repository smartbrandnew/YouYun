import re
import ssl
import time
from Queue import Empty, Queue
from copy import deepcopy
from datetime import datetime, timedelta
from hashlib import md5

from pyVim import connect
from pyVmomi import vim

from checks import AgentCheck
from checks.libs.thread_pool import Pool
from checks.libs.vmware.all_metrics import ALL_METRICS
from checks.libs.vmware.basic_metrics import BASIC_METRICS
from util import Timer
from util import get_hostname

SOURCE_TYPE = 'vsphere'
REAL_TIME_INTERVAL = 20

DEFAULT_SIZE_POOL = 4

REFRESH_MORLIST_INTERVAL = 3 * 60
REFRESH_METRICS_METADATA_INTERVAL = 10 * 60
BATCH_MORLIST_SIZE = 50

JOB_TIMEOUT = 10

EXCLUDE_FILTERS = {
    'AlarmStatusChangedEvent': [r'Gray'],
    'TaskEvent': [
        r'Initialize powering On',
        r'Power Off virtual machine',
        r'Power On virtual machine',
        r'Reconfigure virtual machine',
        r'Relocate virtual machine',
        r'Suspend virtual machine',
        r'Migrate virtual machine',
    ],
    'VmBeingHotMigratedEvent': [],
    'VmMessageEvent': [],
    'VmMigratedEvent': [],
    'VmPoweredOnEvent': [],
    'VmPoweredOffEvent': [],
    'VmReconfiguredEvent': [],
    'VmResumedEvent': [],
    'VmSuspendedEvent': [],
}

MORLIST = 'morlist'
METRICS_METADATA = 'metrics_metadata'
LAST = 'last'
INTERVAL = 'interval'


class VSphereEvent(object):
    UNKNOWN = 'unknown'

    def __init__(self, raw_event, event_config=None):
        self.raw_event = raw_event
        if self.raw_event and self.raw_event.__class__.__name__.startswith('vim.event'):
            self.event_type = self.raw_event.__class__.__name__[10:]
        else:
            self.event_type = VSphereEvent.UNKNOWN

        self.timestamp = int((self.raw_event.createdTime.replace(tzinfo=None) - datetime(1970, 1, 1)).total_seconds())
        self.payload = {
            "timestamp": self.timestamp,
            "event_type": SOURCE_TYPE,
            "source_type_name": SOURCE_TYPE,
        }
        if event_config is None:
            self.event_config = {}
        else:
            self.event_config = event_config

    def _is_filtered(self):
        if self.event_type not in EXCLUDE_FILTERS:
            return True

        filters = EXCLUDE_FILTERS[self.event_type]
        for f in filters:
            if re.search(f, self.raw_event.fullFormattedMessage):
                return True

        return False

    def get_datamonitor_payload(self):
        if self._is_filtered():
            return None

        transform_method = getattr(self, 'transform_%s' % self.event_type.lower(), None)
        if callable(transform_method):
            return transform_method()

        try:
            host_name = get_hostname()
            import socket
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.connect(('www.baidu.com', 0))
            ip = s.getsockname()[0]
        except Exception, e:
            self.log.info("Can Not get host_name or ip")
            host_name = ip = ""
        self.payload["msg_title"] = u"{0}".format(self.event_type)
        self.payload["msg_text"] = u"@@@\n ip:{0},hostname:{1} {2}\n@@@".format(ip, hostname,
                                                                                self.raw_event.fullFormattedMessage)
        self.log.info(self.payload["msg_text"])
        return self.payload

    def transform_vmbeinghotmigratedevent(self):
        self.payload["msg_title"] = u"VM {0} is being migrated".format(self.raw_event.vm.name)
        self.payload["msg_text"] = u"{user} has launched a hot migration of this virtual machine:\n".format(
            user=self.raw_event.userName)
        changes = []
        pre_host = self.raw_event.host.name
        new_host = self.raw_event.destHost.name
        pre_dc = self.raw_event.datacenter.name
        new_dc = self.raw_event.destDatacenter.name
        pre_ds = self.raw_event.ds.name
        new_ds = self.raw_event.destDatastore.name
        if pre_host == new_host:
            changes.append(u"- No host migration: still {0}".format(new_host))
        else:
            changes = [u"- Host MIGRATION: from {0} to {1}".format(pre_host, new_host)] + changes
        if pre_dc == new_dc:
            changes.append(u"- No datacenter migration: still {0}".format(new_dc))
        else:
            changes = [u"- Datacenter MIGRATION: from {0} to {1}".format(pre_dc, new_dc)] + changes
        if pre_ds == new_ds:
            changes.append(u"- No datastore migration: still {0}".format(new_ds))
        else:
            changes = [u"- Datastore MIGRATION: from {0} to {1}".format(pre_ds, new_ds)] + changes

        self.payload["msg_text"] += "\n".join(changes)

        self.payload['host'] = self.raw_event.vm.name
        self.payload['tags'] = [
            'vsphere_host:%s' % pre_host,
            'vsphere_host:%s' % new_host,
            'vsphere_datacenter:%s' % pre_dc,
            'vsphere_datacenter:%s' % new_dc,
        ]
        return self.payload

    def transform_alarmstatuschangedevent(self):
        if self.event_config.get('collect_vcenter_alarms') is None:
            return None

        def get_transition(before, after):
            vals = {
                'gray': -1,
                'green': 0,
                'yellow': 1,
                'red': 2
            }
            before = before.lower()
            after = after.lower()
            if before not in vals or after not in vals:
                return None
            if vals[before] < vals[after]:
                return 'Triggered'
            else:
                return 'Recovered'

        TO_ALERT_TYPE = {
            'green': 'success',
            'yellow': 'warning',
            'red': 'error'
        }

        def get_agg_key(alarm_event):
            return 'h:{0}|dc:{1}|a:{2}'.format(
                md5(alarm_event.entity.name).hexdigest()[:10],
                md5(alarm_event.datacenter.name).hexdigest()[:10],
                md5(alarm_event.alarm.name).hexdigest()[:10]
            )

        if self.raw_event.entity.entity.__class__ == vim.VirtualMachine:
            host_type = 'VM'
        elif self.raw_event.entity.entity.__class__ == vim.HostSystem:
            host_type = 'host'
        else:
            return None
        host_name = self.raw_event.entity.name

        trans_before = getattr(self.raw_event, 'from')
        trans_after = self.raw_event.to
        transition = get_transition(trans_before, trans_after)
        if transition is None:
            return None

        self.payload['msg_title'] = u"[{transition}] {monitor} on {host_type} {host_name} is now {status}".format(
            transition=transition,
            monitor=self.raw_event.alarm.name,
            host_type=host_type,
            host_name=host_name,
            status=trans_after
        )
        self.payload['alert_type'] = TO_ALERT_TYPE[trans_after]
        self.payload['event_object'] = get_agg_key(self.raw_event)
        self.payload[
            'msg_text'] = u"""vCenter monitor status changed on this alarm, it was {before} and it's now {after}.""".format(
            before=trans_before,
            after=trans_after
        )
        self.payload['host'] = host_name
        return self.payload

    def transform_vmmessageevent(self):
        self.payload["msg_title"] = u"VM {0} is reporting".format(self.raw_event.vm.name)
        self.payload["msg_text"] = u"@@@\n{0}\n@@@".format(self.raw_event.fullFormattedMessage)
        self.payload['host'] = self.raw_event.vm.name
        return self.payload

    def transform_vmmigratedevent(self):
        self.payload["msg_title"] = u"VM {0} has been migrated".format(self.raw_event.vm.name)
        self.payload["msg_text"] = u"@@@\n{0}\n@@@".format(self.raw_event.fullFormattedMessage)
        self.payload['host'] = self.raw_event.vm.name
        return self.payload

    def transform_vmpoweredoffevent(self):
        self.payload["msg_title"] = u"VM {0} has been powered OFF".format(self.raw_event.vm.name)
        self.payload["msg_text"] = u"""{user} has powered off this virtual machine. It was running on:
- datacenter: {dc}
- host: {host}
""".format(
            user=self.raw_event.userName,
            dc=self.raw_event.datacenter.name,
            host=self.raw_event.host.name
        )
        self.payload['host'] = self.raw_event.vm.name
        return self.payload

    def transform_vmpoweredonevent(self):
        self.payload["msg_title"] = u"VM {0} has been powered ON".format(self.raw_event.vm.name)
        self.payload["msg_text"] = u"""{user} has powered on this virtual machine. It is running on:
- datacenter: {dc}
- host: {host}
""".format(
            user=self.raw_event.userName,
            dc=self.raw_event.datacenter.name,
            host=self.raw_event.host.name
        )
        self.payload['host'] = self.raw_event.vm.name
        return self.payload

    def transform_vmresumingevent(self):
        self.payload["msg_title"] = u"VM {0} is RESUMING".format(self.raw_event.vm.name)
        self.payload["msg_text"] = u"""{user} has resumed {vm}. It will soon be powered on.""".format(
            user=self.raw_event.userName,
            vm=self.raw_event.vm.name
        )
        self.payload['host'] = self.raw_event.vm.name
        return self.payload

    def transform_vmsuspendedevent(self):
        self.payload["msg_title"] = u"VM {0} has been SUSPENDED".format(self.raw_event.vm.name)
        self.payload["msg_text"] = u"""{user} has suspended this virtual machine. It was running on:
- datacenter: {dc}
- host: {host}
""".format(
            user=self.raw_event.userName,
            dc=self.raw_event.datacenter.name,
            host=self.raw_event.host.name
        )
        self.payload['host'] = self.raw_event.vm.name
        return self.payload

    def transform_vmreconfiguredevent(self):
        self.payload["msg_title"] = u"VM {0} configuration has been changed".format(self.raw_event.vm.name)
        self.payload["msg_text"] = u"{user} saved the new configuration:\n@@@\n".format(user=self.raw_event.userName)
        config_change_lines = [line for line in self.raw_event.configSpec.__repr__().splitlines() if
                               'unset' not in line]
        self.payload["msg_text"] += u"\n".join(config_change_lines)
        self.payload["msg_text"] += u"\n@@@"
        self.payload['host'] = self.raw_event.vm.name
        return self.payload


def atomic_method(method):
    def wrapper(*args, **kwargs):
        try:
            method(*args, **kwargs)
        except Exception as e:
            args[0].exceptionq.put("A worker thread crashed:\n" + str(e))

    return wrapper


class VSphereCheck(AgentCheck):
    SERVICE_CHECK_NAME = 'vcenter.can_connect'

    def __init__(self, name, init_config, agentConfig, instances):
        AgentCheck.__init__(self, name, init_config, agentConfig, instances)
        self.time_started = time.time()
        self.pool_started = False
        self.exceptionq = Queue()

        self.server_instances = {}

        self.event_config = {}
        self.cache_times = {}
        for instance in self.instances:
            i_key = self._instance_key(instance)
            self.cache_times[i_key] = {
                MORLIST: {
                    LAST: 0,
                    INTERVAL: init_config.get('refresh_morlist_interval',
                                              REFRESH_MORLIST_INTERVAL)
                },
                METRICS_METADATA: {
                    LAST: 0,
                    INTERVAL: init_config.get('refresh_metrics_metadata_interval',
                                              REFRESH_METRICS_METADATA_INTERVAL)
                }
            }

            self.event_config[i_key] = instance.get('event_config')

        self.morlist_raw = {}
        self.morlist = {}
        self.metrics_metadata = {}

        self.latest_event_query = {}

    def stop(self):
        self.stop_pool()

    def start_pool(self):
        self.log.info("Starting Thread Pool")
        self.pool_size = int(self.init_config.get('threads_count', DEFAULT_SIZE_POOL))

        self.pool = Pool(self.pool_size)
        self.pool_started = True
        self.jobs_status = {}

    def stop_pool(self):
        self.log.info("Stopping Thread Pool")
        if self.pool_started:
            self.pool.terminate()
            self.pool.join()
            self.jobs_status.clear()
            assert self.pool.get_nworkers() == 0
            self.pool_started = False

    def restart_pool(self):
        self.stop_pool()
        self.start_pool()

    def _clean(self):
        now = time.time()
        for name in self.jobs_status.keys():
            start_time = self.jobs_status[name]
            if now - start_time > JOB_TIMEOUT:
                self.log.critical("Restarting Pool. One check is stuck.")
                self.restart_pool()
                break

    def _query_event(self, instance):
        i_key = self._instance_key(instance)
        last_time = self.latest_event_query.get(i_key)

        server_instance = self._get_server_instance(instance)
        event_manager = server_instance.content.eventManager

        if not last_time:
            last_time = self.latest_event_query[i_key] = \
                event_manager.latestEvent.createdTime + timedelta(seconds=1)

        query_filter = vim.event.EventFilterSpec()
        time_filter = vim.event.EventFilterSpec.ByTime(beginTime=self.latest_event_query[i_key])
        query_filter.time = time_filter

        try:
            new_events = event_manager.QueryEvents(query_filter)
            self.log.debug("Got {0} events from vCenter event manager".format(len(new_events)))
            for event in new_events:
                normalized_event = VSphereEvent(event, self.event_config[i_key])
                event_payload = normalized_event.get_datamonitor_payload()
                if event_payload is not None:
                    self.event(event_payload)
                last_time = event.createdTime + timedelta(seconds=1)
        except Exception as e:
            self.log.warning("Unable to fetch Events %s", e)
            last_time = event_manager.latestEvent.createdTime + timedelta(seconds=1)

        self.latest_event_query[i_key] = last_time

    def _instance_key(self, instance):
        i_key = instance.get('name')
        if i_key is None:
            raise Exception("Must define a unique 'name' per vCenter instance")
        return i_key

    def _should_cache(self, instance, entity):
        i_key = self._instance_key(instance)
        now = time.time()
        return now - self.cache_times[i_key][entity][LAST] > self.cache_times[i_key][entity][INTERVAL]

    def _get_server_instance(self, instance):
        i_key = self._instance_key(instance)

        service_check_tags = [
            'vcenter_server:{0}'.format(instance.get('name')),
            'vcenter_host:{0}'.format(instance.get('host')),
        ]

        ssl_verify = instance.get('ssl_verify', True)
        ssl_capath = instance.get('ssl_capath', None)
        if not ssl_verify:
            context = ssl.SSLContext(ssl.PROTOCOL_SSLv23)
            context.verify_mode = ssl.CERT_NONE
        elif ssl_capath:
            context = ssl.SSLContext(ssl.PROTOCOL_SSLv23)
            context.verify_mode = ssl.CERT_REQUIRED
            context.load_verify_locations(capath=ssl_capath)

        if not ssl_verify and ssl_capath:
            self.log.debug("Your configuration is incorrectly attempting to "
                           "specify both a CA path, and to disable SSL "
                           "verification. You cannot do both. Proceeding with "
                           "disabling ssl verification.")

        if i_key not in self.server_instances:
            try:
                server_instance = connect.SmartConnect(
                    host=instance.get('host'),
                    user=instance.get('username'),
                    pwd=instance.get('password'),
                    sslContext=context if not ssl_verify or ssl_capath else None
                )
            except Exception as e:
                err_msg = "Connection to %s failed: %s" % (instance.get('host'), e)
                self.service_check(self.SERVICE_CHECK_NAME, AgentCheck.CRITICAL,
                                   tags=service_check_tags, message=err_msg)
                raise Exception(err_msg)

            self.server_instances[i_key] = server_instance

        try:
            self.server_instances[i_key].RetrieveContent()
            self.service_check(self.SERVICE_CHECK_NAME, AgentCheck.OK,
                               tags=service_check_tags)
        except Exception as e:
            err_msg = "Connection to %s died unexpectedly: %s" % (instance.get('host'), e)
            self.service_check(self.SERVICE_CHECK_NAME, AgentCheck.CRITICAL,
                               tags=service_check_tags, message=err_msg)
            raise Exception(err_msg)

        return self.server_instances[i_key]

    def _compute_needed_metrics(self, instance, available_metrics):
        if instance.get('all_metrics', False):
            return available_metrics

        i_key = self._instance_key(instance)
        wanted_metrics = []
        for metric in available_metrics:
            if (i_key not in self.metrics_metadata
                or metric.counterId not in self.metrics_metadata[i_key]):
                continue
            if self.metrics_metadata[i_key][metric.counterId]['name'] in BASIC_METRICS:
                wanted_metrics.append(metric)

        return wanted_metrics

    def get_external_host_tags(self):
        self.log.info("Sending external_host_tags now")
        external_host_tags = []
        for instance in self.instances:
            i_key = self._instance_key(instance)
            mor_list = self.morlist[i_key].items()
            for mor_name, mor in mor_list:
                external_host_tags.append((mor['hostname'], {SOURCE_TYPE: mor['tags']}))

        return external_host_tags

    @atomic_method
    def _cache_morlist_raw_atomic(self, i_key, obj_type, obj, tags, regexes=None):
        t = Timer()
        self.log.debug("job_atomic: Exploring MOR {0} (type={1})".format(obj, obj_type))
        tags_copy = deepcopy(tags)

        if obj_type == 'rootFolder':
            for datacenter in obj.childEntity:
                if not hasattr(datacenter, 'hostFolder'):
                    continue
                self.pool.apply_async(
                    self._cache_morlist_raw_atomic,
                    args=(i_key, 'datacenter', datacenter, tags_copy, regexes)
                )

        elif obj_type == 'datacenter':
            dc_tag = "vsphere_datacenter:%s" % obj.name
            tags_copy.append(dc_tag)
            for compute_resource in obj.hostFolder.childEntity:
                if not hasattr(compute_resource, 'host'):
                    continue
                self.pool.apply_async(
                    self._cache_morlist_raw_atomic,
                    args=(i_key, 'compute_resource', compute_resource, tags_copy, regexes)
                )

        elif obj_type == 'compute_resource':
            if obj.__class__ == vim.ClusterComputeResource:
                cluster_tag = "vsphere_cluster:%s" % obj.name
                tags_copy.append(cluster_tag)
            for host in obj.host:
                if not hasattr(host, 'vm'):
                    continue
                self.pool.apply_async(
                    self._cache_morlist_raw_atomic,
                    args=(i_key, 'host', host, tags_copy, regexes)
                )

        elif obj_type == 'host':
            if regexes and regexes.get('host_include') is not None:
                match = re.search(regexes['host_include'], obj.name)
                if not match:
                    self.log.debug(u"Filtered out VM {0} because of host_include_only_regex".format(obj.name))
                    return
            watched_mor = dict(mor_type='host', mor=obj, hostname=obj.name, tags=tags_copy + ['vsphere_type:host'])
            self.morlist_raw[i_key].append(watched_mor)

            host_tag = "vsphere_host:%s" % obj.name
            tags_copy.append(host_tag)
            for vm in obj.vm:
                if vm.runtime.powerState != 'poweredOn':
                    continue
                self.pool.apply_async(
                    self._cache_morlist_raw_atomic,
                    args=(i_key, 'vm', vm, tags_copy, regexes)
                )

        elif obj_type == 'vm':
            if regexes and regexes.get('vm_include') is not None:
                match = re.search(regexes['vm_include'], obj.name)
                if not match:
                    self.log.debug(u"Filtered out VM {0} because of vm_include_only_regex".format(obj.name))
                    return
            watched_mor = dict(mor_type='vm', mor=obj, hostname=obj.name, tags=tags_copy + ['vsphere_type:vm'])
            self.morlist_raw[i_key].append(watched_mor)

        self.histogram('datamonitor.agent.vsphere.morlist_raw_atomic.time', t.total())

    def _cache_morlist_raw(self, instance):

        i_key = self._instance_key(instance)
        self.log.debug("Caching the morlist for vcenter instance %s" % i_key)
        if i_key in self.morlist_raw and len(self.morlist_raw[i_key]) > 0:
            self.log.debug(
                "Skipping morlist collection now, RAW results "
                "processing not over (latest refresh was {0}s ago)".format(
                    time.time() - self.cache_times[i_key][MORLIST][LAST])
            )
            return
        self.morlist_raw[i_key] = []

        server_instance = self._get_server_instance(instance)
        root_folder = server_instance.content.rootFolder

        instance_tag = "vcenter_server:%s" % instance.get('name')
        regexes = {
            'host_include': instance.get('host_include_only_regex'),
            'vm_include': instance.get('vm_include_only_regex')
        }
        self.pool.apply_async(
            self._cache_morlist_raw_atomic,
            args=(i_key, 'rootFolder', root_folder, [instance_tag], regexes)
        )
        self.cache_times[i_key][MORLIST][LAST] = time.time()

    @atomic_method
    def _cache_morlist_process_atomic(self, instance, mor):
        t = Timer()
        i_key = self._instance_key(instance)
        server_instance = self._get_server_instance(instance)
        perfManager = server_instance.content.perfManager

        self.log.debug(
            "job_atomic: Querying available metrics"
            " for MOR {0} (type={1})".format(mor['mor'], mor['mor_type'])
        )

        available_metrics = perfManager.QueryAvailablePerfMetric(
            mor['mor'], intervalId=REAL_TIME_INTERVAL)

        mor['metrics'] = self._compute_needed_metrics(instance, available_metrics)
        mor_name = str(mor['mor'])

        if mor_name in self.morlist[i_key]:
            self.morlist[i_key][mor_name]['metrics'] = mor['metrics']
        else:
            self.morlist[i_key][mor_name] = mor

        self.morlist[i_key][mor_name]['last_seen'] = time.time()

        self.histogram('datamonitor.agent.vsphere.morlist_process_atomic.time', t.total())

    def _cache_morlist_process(self, instance):
        i_key = self._instance_key(instance)
        if i_key not in self.morlist:
            self.morlist[i_key] = {}

        batch_size = self.init_config.get('batch_morlist_size', BATCH_MORLIST_SIZE)

        for i in xrange(batch_size):
            try:
                mor = self.morlist_raw[i_key].pop()
                self.pool.apply_async(self._cache_morlist_process_atomic, args=(instance, mor))
            except (IndexError, KeyError):
                self.log.debug("No more work to process in morlist_raw")
                return

    def _vacuum_morlist(self, instance):
        i_key = self._instance_key(instance)
        morlist = self.morlist[i_key].items()

        for mor_name, mor in morlist:
            last_seen = mor['last_seen']
            if (time.time() - last_seen) > 2 * REFRESH_MORLIST_INTERVAL:
                del self.morlist[i_key][mor_name]

    def _cache_metrics_metadata(self, instance):

        t = Timer()

        i_key = self._instance_key(instance)
        self.log.info("Warming metrics metadata cache for instance {0}".format(i_key))
        server_instance = self._get_server_instance(instance)
        perfManager = server_instance.content.perfManager

        new_metadata = {}
        for counter in perfManager.perfCounter:
            d = dict(
                name="%s.%s" % (counter.groupInfo.key, counter.nameInfo.key),
                unit=counter.unitInfo.key,
                instance_tag='instance'
            )
            new_metadata[counter.key] = d
        self.cache_times[i_key][METRICS_METADATA][LAST] = time.time()

        self.log.info("Finished metadata collection for instance {0}".format(i_key))

        self.metrics_metadata[i_key] = new_metadata

        self.histogram('datamonitor.agent.vsphere.metric_metadata_collection.time', t.total())

    def _transform_value(self, instance, counter_id, value):

        i_key = self._instance_key(instance)
        if counter_id in self.metrics_metadata[i_key]:
            unit = self.metrics_metadata[i_key][counter_id]['unit']
            if unit == 'percent':
                return float(value) / 100

        return value

    @atomic_method
    def _collect_metrics_atomic(self, instance, mor):

        t = Timer()

        i_key = self._instance_key(instance)
        server_instance = self._get_server_instance(instance)
        perfManager = server_instance.content.perfManager
        query = vim.PerformanceManager.QuerySpec(maxSample=1,
                                                 entity=mor['mor'],
                                                 metricId=mor['metrics'],
                                                 intervalId=20,
                                                 format='normal')
        results = perfManager.QueryPerf(querySpec=[query])

        if results:
            for result in results[0].value:
                if result.id.counterId not in self.metrics_metadata[i_key]:
                    self.log.debug("Skipping this metric value, because there is no metadata about it")
                    continue
                instance_name = result.id.instance or "none"
                value = self._transform_value(instance, result.id.counterId, result.value[0])

                if ALL_METRICS[self.metrics_metadata[i_key][result.id.counterId]['name']]['s_type'] == 'rate':
                    record_metric = self.rate
                else:
                    record_metric = self.gauge
                ip = "unknown"
                content = server_instance.RetrieveContent()
                for child in content.rootFolder.childEntity:
                    if hasattr(child, 'vmFolder'):
                        datacenter = child
                        vmFolder = datacenter.vmFolder
                        vmList = vmFolder.childEntity
                        for vm in vmList:
                            if isinstance(vm, vim.VirtualMachine):
                                ip = vm.summary.guest.ipAddress
                                self.log.info("Get VM ip {} by VMtools".format(ip))
                if ip != "unknown" and ip != "None":
                    record_metric(
                        "vsphere.%s" % self.metrics_metadata[i_key][result.id.counterId]['name'],
                        value,
                        hostname=mor['hostname'],
                        tags=['instance:%s' % instance_name, 'ip:%s' % ip, 'type:VM']
                    )
                else:
                    record_metric(
                        "vsphere.%s" % self.metrics_metadata[i_key][result.id.counterId]['name'],
                        value,
                        hostname=mor['hostname'],
                        tags=['instance:%s' % instance_name, "type:VM"]
                    )

        self.histogram('datamonitor.agent.vsphere.metric_colection.time', t.total())

    def collect_metrics(self, instance):

        i_key = self._instance_key(instance)
        if i_key not in self.morlist:
            self.log.debug("Not collecting metrics for this instance, nothing to do yet: {0}".format(i_key))
            return

        mors = self.morlist[i_key].items()
        self.log.debug("Collecting metrics of %d mors" % len(mors))

        vm_count = 0

        for mor_name, mor in mors:
            if mor['mor_type'] == 'vm':
                vm_count += 1
            if 'metrics' not in mor:
                continue

            self.pool.apply_async(self._collect_metrics_atomic, args=(instance, mor))

        self.gauge('vsphere.vm.count', vm_count, tags=["vcenter_server:%s" % instance.get('name')])

    def check(self, instance):
        if not self.pool_started:
            self.start_pool()
        self.gauge('datamonitor.agent.vsphere.queue_size', self.pool._workq.qsize(), tags=['instant:initial'])

        if self._should_cache(instance, METRICS_METADATA):
            self._cache_metrics_metadata(instance)

        if self._should_cache(instance, MORLIST):
            self._cache_morlist_raw(instance)
        self._cache_morlist_process(instance)
        self._vacuum_morlist(instance)

        self.collect_metrics(instance)
        self._query_event(instance)

        self._clean()

        thread_crashed = False
        try:
            while True:
                self.log.critical(self.exceptionq.get_nowait())
                thread_crashed = True
        except Empty:
            pass
        if thread_crashed:
            self.stop_pool()
            raise Exception("One thread in the pool crashed, check the logs")

        self.gauge('datamonitor.agent.vsphere.queue_size', self.pool._workq.qsize(), tags=['instant:final'])


if __name__ == '__main__':
    check, _instances = VSphereCheck.from_yaml('conf.d/vsphere.yaml')
    try:
        for i in xrange(200):
            print "Loop %d" % i
            for instance in check.instances:
                check.check(instance)
                if check.has_events():
                    print 'Events: %s' % (check.get_events())
                print 'Metrics: %d' % (len(check.get_metrics()))
            time.sleep(10)
    except Exception as e:
        print "Whoops something happened {0}".format(e)
    finally:
        check.stop()
