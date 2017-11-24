import copy
import json
import logging
import numbers
import os
import re
import time
import timeit
import unicodedata
from collections import defaultdict
from types import ListType, TupleType

try:
    import psutil
except ImportError:
    psutil = None

import yaml

from checks import check_status
from util import get_hostname, get_next_id, LaconicFilter, yLoader
from util import decrypted, convert_to_str
from utils.platform import Platform
from utils.profile import pretty_statistics

if Platform.is_windows():
    pass

log = logging.getLogger(__name__)

DEFAULT_PSUTIL_METHODS = ['memory_info', 'io_counters']

AGENT_METRICS_CHECK_NAME = 'agent_metrics'


class CheckException(Exception):
    pass


class Infinity(CheckException):
    pass


class NaN(CheckException):
    pass


class UnknownValue(CheckException):
    pass


class Check(object):
    def __init__(self, logger):

        self._sample_store = {}
        self._counters = {}
        self.logger = logger
        try:
            self.logger.addFilter(LaconicFilter())
        except Exception:
            self.logger.exception("Trying to install laconic log filter and failed")

    def normalize(self, metric, prefix=None):

        name = re.sub(r"[,\+\*\-/()\[\]{}\s]", "_", metric)
        name = re.sub(r"__+", "_", name)

        name = re.sub(r"^_", "", name)
        name = re.sub(r"_$", "", name)
        name = re.sub(r"\._", ".", name)
        name = re.sub(r"_\.", ".", name)

        if prefix is not None:
            return prefix + "." + name
        else:
            return name

    def normalize_device_name(self, device_name):
        return device_name.strip().lower().replace(' ', '_')

    def counter(self, metric):

        self._counters[metric] = True
        self._sample_store[metric] = {}

    def is_counter(self, metric):
        return metric in self._counters

    def gauge(self, metric):
        self._sample_store[metric] = {}

    def is_metric(self, metric):
        return metric in self._sample_store

    def is_gauge(self, metric):
        return self.is_metric(metric) and \
               not self.is_counter(metric)

    def get_metric_names(self):
        return self._sample_store.keys()

    def save_gauge(self, metric, value, timestamp=None, tags=None, hostname=None, device_name=None):
        if not self.is_gauge(metric):
            self.gauge(metric)
        self.save_sample(metric, value, timestamp, tags, hostname, device_name)

    def save_sample(self, metric, value, timestamp=None, tags=None, hostname=None, device_name=None):
        from util import cast_metric_val

        if timestamp is None:
            timestamp = time.time()
        if metric not in self._sample_store:
            raise CheckException("Saving a sample for an undefined metric: %s" % metric)
        try:
            value = cast_metric_val(value)
        except ValueError, ve:
            raise NaN(ve)

        if tags is not None:
            if type(tags) not in [type([]), type(())]:
                raise CheckException("Tags must be a list or tuple of strings")
            else:
                tags = tuple(sorted(tags))

        key = (tags, device_name)
        if self.is_gauge(metric):
            self._sample_store[metric][key] = ((timestamp, value, hostname, device_name),)
        elif self.is_counter(metric):
            if self._sample_store[metric].get(key) is None:
                self._sample_store[metric][key] = [(timestamp, value, hostname, device_name)]
            else:
                self._sample_store[metric][key] = self._sample_store[metric][key][-1:] + [
                    (timestamp, value, hostname, device_name)]
        else:
            raise CheckException(
                "%s must be either gauge or counter, skipping sample at %s" % (metric, time.ctime(timestamp)))

        if self.is_gauge(metric):
            assert len(self._sample_store[metric][key]) == 1, self._sample_store[metric]
        elif self.is_counter(metric):
            assert len(self._sample_store[metric][key]) in (1, 2), self._sample_store[metric]

    @classmethod
    def _rate(cls, sample1, sample2):
        try:
            interval = sample2[0] - sample1[0]
            if interval == 0:
                raise Infinity()

            delta = sample2[1] - sample1[1]
            if delta < 0:
                raise UnknownValue()

            return (sample2[0], delta / interval, sample2[2], sample2[3])
        except Infinity:
            raise
        except UnknownValue:
            raise
        except Exception, e:
            raise NaN(e)

    def get_sample_with_timestamp(self, metric, tags=None, device_name=None, expire=True):

        if tags is not None and isinstance(tags, ListType):
            tags.sort()
            tags = tuple(tags)
        key = (tags, device_name)

        if metric not in self._sample_store:
            raise UnknownValue()

        elif self.is_counter(metric) and len(self._sample_store[metric][key]) < 2:
            raise UnknownValue()

        elif self.is_counter(metric) and len(self._sample_store[metric][key]) >= 2:
            res = self._rate(self._sample_store[metric][key][-2], self._sample_store[metric][key][-1])
            if expire:
                del self._sample_store[metric][key][:-1]
            return res

        elif self.is_gauge(metric) and len(self._sample_store[metric][key]) >= 1:
            return self._sample_store[metric][key][-1]

        else:
            raise UnknownValue()

    def get_sample(self, metric, tags=None, device_name=None, expire=True):
        x = self.get_sample_with_timestamp(metric, tags, device_name, expire)
        assert isinstance(x, TupleType) and len(x) == 4, x
        return x[1]

    def get_samples_with_timestamps(self, expire=True):
        values = {}
        for m in self._sample_store:
            try:
                values[m] = self.get_sample_with_timestamp(m, expire=expire)
            except Exception:
                pass
        return values

    def get_samples(self, expire=True):
        values = {}
        for m in self._sample_store:
            try:
                values[m] = self.get_sample_with_timestamp(m, expire=expire)[1]
            except Exception:
                pass
        return values

    def get_metrics(self, expire=True):

        metrics = []
        for m in self._sample_store:
            try:
                for key in self._sample_store[m]:
                    tags, device_name = key
                    try:
                        ts, val, hostname, device_name = self.get_sample_with_timestamp(m, tags, device_name, expire)
                    except UnknownValue:
                        continue
                    attributes = {}
                    if tags:
                        attributes['tags'] = list(tags)
                    if hostname:
                        attributes['host_name'] = hostname
                    if device_name:
                        attributes['device_name'] = device_name
                    metrics.append((m, int(ts), val, attributes))
            except Exception:
                pass
        return metrics


class AgentCheck(object):
    OK, WARNING, CRITICAL, UNKNOWN = (0, 1, 2, 3)

    SOURCE_TYPE_NAME = None

    DEFAULT_MIN_COLLECTION_INTERVAL = 0

    _enabled_checks = []

    @classmethod
    def is_check_enabled(cls, name):
        return name in cls._enabled_checks

    def __init__(self, name, init_config, agentConfig, instances=None):

        from aggregator import MetricsAggregator

        self._enabled_checks.append(name)
        self._enabled_checks = list(set(self._enabled_checks))

        self.name = name
        self.init_config = init_config or {}
        self.agentConfig = agentConfig
        self.in_developer_mode = agentConfig.get('developer_mode') and psutil
        self._internal_profiling_stats = None

        self.hostname = agentConfig.get('checksd_hostname') or get_hostname(agentConfig)
        self.log = logging.getLogger('%s.%s' % (__name__, name))
        self.aggregator = MetricsAggregator(
            self.hostname,
            formatter=agent_formatter,
            recent_point_threshold=agentConfig.get('recent_point_threshold', None),
            histogram_aggregates=agentConfig.get('histogram_aggregates'),
            histogram_percentiles=agentConfig.get('histogram_percentiles')
        )

        self.events = []
        self.service_checks = []
        if instances:
            jsoned_instances = json.dumps(instances)
            encrypted_passwd_list = re.findall('>>>.*?<<<', jsoned_instances)
            if encrypted_passwd_list:
                for encrypted_passwd in encrypted_passwd_list:
                    decrypted_passwd = decrypted(encrypted_passwd)
                    jsoned_instances = jsoned_instances.replace(encrypted_passwd, decrypted_passwd)
                self.instances = convert_to_str(json.loads(jsoned_instances, encoding='utf-8'))
            else:
                self.instances = instances
        else:
            self.instances = []
        self.warnings = []
        self.library_versions = None
        self.last_collection_time = defaultdict(int)
        self._instance_metadata = []
        self.svc_metadata = []
        self.historate_dict = {}

    def instance_count(self):
        return len(self.instances)

    def gauge(self, metric, value, tags=None, hostname=None, device_name=None, timestamp=None):
        self.aggregator.gauge(metric, value, tags, hostname, device_name, timestamp)

    def increment(self, metric, value=1, tags=None, hostname=None, device_name=None):

        self.aggregator.increment(metric, value, tags, hostname, device_name)

    def decrement(self, metric, value=-1, tags=None, hostname=None, device_name=None):
        self.aggregator.decrement(metric, value, tags, hostname, device_name)

    def count(self, metric, value=0, tags=None, hostname=None, device_name=None):
        self.aggregator.submit_count(metric, value, tags, hostname, device_name)

    def monotonic_count(self, metric, value=0, tags=None,
                        hostname=None, device_name=None):
        self.aggregator.count_from_counter(metric, value, tags,
                                           hostname, device_name)

    def rate(self, metric, value, tags=None, hostname=None, device_name=None):
        self.aggregator.rate(metric, value, tags, hostname, device_name)

    def histogram(self, metric, value, tags=None, hostname=None, device_name=None):
        self.aggregator.histogram(metric, value, tags, hostname, device_name)

    @classmethod
    def generate_historate_func(cls, excluding_tags):
        def fct(self, metric, value, tags=None, hostname=None, device_name=None):
            cls.historate(self, metric, value, excluding_tags,
                          tags=tags, hostname=hostname, device_name=device_name)

        return fct

    @classmethod
    def generate_histogram_func(cls, excluding_tags):
        def fct(self, metric, value, tags=None, hostname=None, device_name=None):
            tags = list(tags)
            for tag in list(tags):
                for exc_tag in excluding_tags:
                    if tag.startswith(exc_tag + ":"):
                        tags.remove(tag)

            cls.histogram(self, metric, value, tags=tags, hostname=hostname,
                          device_name=device_name)

        return fct

    def historate(self, metric, value, excluding_tags, tags=None, hostname=None, device_name=None):

        tags = list(tags)
        context = [metric]
        if tags is not None:
            context.append("-".join(sorted(tags)))
        if hostname is not None:
            context.append("host:" + hostname)
        if device_name is not None:
            context.append("device:" + device_name)

        now = time.time()
        context = tuple(context)

        if context in self.historate_dict:
            if tags is not None:
                for tag in list(tags):
                    for exc_tag in excluding_tags:
                        if tag.startswith("{0}:".format(exc_tag)):
                            tags.remove(tag)

            prev_value, prev_ts = self.historate_dict[context]
            rate = float(value - prev_value) / float(now - prev_ts)
            self.aggregator.histogram(metric, rate, tags, hostname, device_name)

        self.historate_dict[context] = (value, now)

    def set(self, metric, value, tags=None, hostname=None, device_name=None):
        self.aggregator.set(metric, value, tags, hostname, device_name)

    def event(self, event):
        if event.get('api_key') is None:
            event['api_key'] = self.agentConfig['api_key']
        self.events.append(event)

    def service_check(self, check_name, status, tags=None, timestamp=None,
                      hostname=None, check_run_id=None, message=None):

        if hostname is None:
            hostname = self.hostname
        if message is not None:
            message = unicode(message)
        self.service_checks.append(
            create_service_check(check_name, status, tags, timestamp,
                                 hostname, check_run_id, message)
        )

    def service_metadata(self, meta_name, value):

        self._instance_metadata.append((meta_name, unicode(value)))

    def has_events(self):

        return len(self.events) > 0

    def get_metrics(self):
        return self.aggregator.flush()

    def get_events(self):
        events = self.events
        self.events = []
        return events

    def get_service_checks(self):
        service_checks = self.service_checks
        self.service_checks = []
        return service_checks

    def _roll_up_instance_metadata(self):
        self.svc_metadata.append(dict((k, v) for (k, v) in self._instance_metadata))
        self._instance_metadata = []

    def get_service_metadata(self):
        if self._instance_metadata:
            self._roll_up_instance_metadata()
        service_metadata = self.svc_metadata
        self.svc_metadata = []
        return service_metadata

    def has_warnings(self):
        return len(self.warnings) > 0

    def warning(self, warning_message):
        warning_message = str(warning_message)

        self.log.warning(warning_message)
        self.warnings.append(warning_message)

    def get_library_info(self):
        if self.library_versions is not None:
            return self.library_versions
        try:
            self.library_versions = self.get_library_versions()
        except NotImplementedError:
            pass

    def get_library_versions(self):
        raise NotImplementedError

    def get_warnings(self):
        warnings = self.warnings
        self.warnings = []
        return warnings

    @staticmethod
    def _get_statistic_name_from_method(method_name):
        return method_name[4:] if method_name.startswith('get_') else method_name

    @staticmethod
    def _collect_internal_stats(methods=None):
        current_process = psutil.Process(os.getpid())

        methods = methods or DEFAULT_PSUTIL_METHODS
        filtered_methods = [m for m in methods if hasattr(current_process, m)]

        stats = {}

        for method in filtered_methods:
            stat_name = AgentCheck._get_statistic_name_from_method(method)
            try:
                raw_stats = getattr(current_process, method)()
                try:
                    stats[stat_name] = raw_stats._asdict()
                except AttributeError:
                    if isinstance(raw_stats, numbers.Number):
                        stats[stat_name] = raw_stats
                    else:
                        log.warn("Could not serialize output of {0} to dict".format(method))

            except psutil.AccessDenied:
                log.warn("Cannot call psutil method {} : Access Denied".format(method))

        return stats

    def _set_internal_profiling_stats(self, before, after):
        self._internal_profiling_stats = {'before': before, 'after': after}

    def _get_internal_profiling_stats(self):
        stats = self._internal_profiling_stats
        self._internal_profiling_stats = None
        return stats

    def run(self):
        before, after = None, None
        if self.in_developer_mode and self.name != AGENT_METRICS_CHECK_NAME:
            try:
                before = AgentCheck._collect_internal_stats()
            except Exception:
                self.log.debug("Failed to collect Agent Stats before check {0}".format(self.name))

        instance_statuses = []
        for i, instance in enumerate(self.instances):
            try:
                min_collection_interval = instance.get(
                    'min_collection_interval', self.init_config.get(
                        'min_collection_interval',
                        self.DEFAULT_MIN_COLLECTION_INTERVAL
                    )
                )
                now = time.time()
                if now - self.last_collection_time[i] < min_collection_interval:
                    self.log.debug(
                        "Not running instance #{0} of check {1} as it ran less than {2}s ago".format(i, self.name,
                                                                                                     min_collection_interval))
                    continue

                self.last_collection_time[i] = now

                check_start_time = None
                if self.in_developer_mode:
                    check_start_time = timeit.default_timer()
                self.check(copy.deepcopy(instance))

                instance_check_stats = None
                if check_start_time is not None:
                    instance_check_stats = {'run_time': timeit.default_timer() - check_start_time}

                if self.has_warnings():
                    instance_status = check_status.InstanceStatus(
                        i, check_status.STATUS_WARNING,
                        warnings=self.get_warnings(), instance_check_stats=instance_check_stats
                    )
                else:
                    instance_status = check_status.InstanceStatus(
                        i, check_status.STATUS_OK,
                        instance_check_stats=instance_check_stats
                    )
            except Exception, e:
                self.log.exception("Check '%s' instance #%s failed" % (self.name, i))
                instance_status = check_status.InstanceStatus(
                    i, check_status.STATUS_ERROR,
                    error=str(e), tb=str(e)
                )
            finally:
                self._roll_up_instance_metadata()

            instance_statuses.append(instance_status)

        if self.in_developer_mode and self.name != AGENT_METRICS_CHECK_NAME:
            try:
                after = AgentCheck._collect_internal_stats()
                self._set_internal_profiling_stats(before, after)
                log.info("\n \t %s %s" % (self.name, pretty_statistics(self._internal_profiling_stats)))
            except Exception:
                self.log.debug("Failed to collect Agent Stats after check {0}".format(self.name))

        return instance_statuses

    def check(self, instance):
        raise NotImplementedError()

    def stop(self):
        pass

    @classmethod
    def from_yaml(cls, path_to_yaml=None, agentConfig=None, yaml_text=None, check_name=None):
        if path_to_yaml:
            check_name = os.path.basename(path_to_yaml).split('.')[0]
            try:
                f = open(path_to_yaml)
            except IOError:
                raise Exception('Unable to open yaml config: %s' % path_to_yaml)
            yaml_text = f.read()
            f.close()

        config = yaml.load(yaml_text, Loader=yLoader)
        try:
            check = cls(check_name, config.get('init_config') or {}, agentConfig or {},
                        config.get('instances'))
        except TypeError:
            check = cls(check_name, config.get('init_config') or {}, agentConfig or {})
        return check, config.get('instances', [])

    def normalize(self, metric, prefix=None, fix_case=False):
        if isinstance(metric, unicode):
            metric_name = unicodedata.normalize('NFKD', metric).encode('ascii', 'ignore')
        else:
            metric_name = metric

        if fix_case:
            name = self.convert_to_underscore_separated(metric_name)
            if prefix is not None:
                prefix = self.convert_to_underscore_separated(prefix)
        else:
            name = re.sub(r"[,\+\*\-/()\[\]{}\s]", "_", metric_name)
        name = re.sub(r"__+", "_", name)
        name = re.sub(r"^_", "", name)
        name = re.sub(r"_$", "", name)
        name = re.sub(r"\._", ".", name)
        name = re.sub(r"_\.", ".", name)

        if prefix is not None:
            return prefix + "." + name
        else:
            return name

    FIRST_CAP_RE = re.compile('(.)([A-Z][a-z]+)')
    ALL_CAP_RE = re.compile('([a-z0-9])([A-Z])')
    METRIC_REPLACEMENT = re.compile(r'([^a-zA-Z0-9_.]+)|(^[^a-zA-Z]+)')
    DOT_UNDERSCORE_CLEANUP = re.compile(r'_*\._*')

    def convert_to_underscore_separated(self, name):
        metric_name = self.FIRST_CAP_RE.sub(r'\1_\2', name)
        metric_name = self.ALL_CAP_RE.sub(r'\1_\2', metric_name).lower()
        metric_name = self.METRIC_REPLACEMENT.sub('_', metric_name)
        return self.DOT_UNDERSCORE_CLEANUP.sub('.', metric_name).strip('_')

    @staticmethod
    def read_config(instance, key, message=None, cast=None):
        val = instance.get(key)
        if val is None:
            message = message or 'Must provide `%s` value in instance config' % key
            raise Exception(message)

        if cast is None:
            return val
        else:
            return cast(val)


def agent_formatter(metric, value, timestamp, tags, hostname, device_name=None,
                    metric_type=None, interval=None):
    attributes = {}
    if tags:
        attributes['tags'] = list(tags)
    if hostname:
        attributes['hostname'] = hostname
    if device_name:
        attributes['device_name'] = device_name
    if metric_type:
        attributes['type'] = metric_type
    if interval:
        pass
    if attributes:
        return (metric, int(timestamp), value, attributes)
    return (metric, int(timestamp), value)


def create_service_check(check_name, status, tags=None, timestamp=None,
                         hostname=None, check_run_id=None, message=None):
    if check_run_id is None:
        check_run_id = get_next_id('service_check')
    return {
        'id': check_run_id,
        'check': check_name,
        'status': status,
        'host_name': hostname,
        'tags': tags,
        'timestamp': float(timestamp or time.time()),
        'message': message
    }
