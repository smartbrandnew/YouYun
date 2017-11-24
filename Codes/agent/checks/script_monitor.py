# !/C:/Python27
# -*- coding: UTF-8 -*-

# stdlib
from copy import deepcopy
import subprocess
import logging
import sys
import time
from threading import Thread


# project
from checks import agent_formatter
from collector import AgentPayload
from utils.logger import log_exceptions
from util import get_os, Timer, get_ip
from aggregator import MetricsAggregator
from uyunuuid import get_uuid

__author__ = 'fangjc'
__project__ = 'Monitor-Agent'
__date__ = '2016/11/23'

log = logging.getLogger(__name__)


FLUSH_LOGGING_PERIOD = 10
FLUSH_LOGGING_INITIAL = 5


class ScriptRunner(object):
    def __init__(self, agent_config, hostname, emitters, path):
        self.run_count = 0
        self.events = []
        self.continue_running = True
        self.agent_config = agent_config
        self.hostname = hostname
        self.emitters = emitters
        self.path = path
        # 解析指定目录脚本预定义规范 默认为
        # {'name': 'default', 'interval': 15, 'version': '1.0.0'}
        self.standard = {'name': None, 'interval': 15, 'version': '1.0.0'}
        self.interval = self.standard['interval']
        # 仿照插件代码写法实例化MetricAggregator
        self.aggregator = MetricsAggregator(
            self.hostname,
            formatter=agent_formatter
        )
        self.tag = self._get_tags()

    def run(self, payload):
        # 运行时间计时器
        log.info("Calling %s" % self.path)
        while self.continue_running:
            timer = Timer()
            payload_temp = deepcopy(payload)
            try:
                self._standard_parser(self.path)
                # stderr输出在测试运行时都是空字符串 不管脚本运行正常还是失败
                # 此变量暂时保存 后续有需求时再处理
                stdout_data, stderr_data = self._get_value()
                for i in stdout_data:
                    if 'metric' in i:
                        self._parse_metric(i)
                    elif 'event' in i:
                        self._parse_events(i)
            except IOError as e:
                io_err_event = self._format_event(
                    str(e),
                    "Reading script file failed",
                    "error",
                    "path:%s" % self.path
                )
                self.events.append(io_err_event)
            except OSError as e:
                os_err_event = self._format_event(
                    str(e),
                    "Executing script file failed",
                    "error",
                    "path:%s" % self.path
                )
                self.events.append(os_err_event)
            except KeyError as e:
                key_err_event = self._format_event(
                    str(e),
                    "Output of script file missing or misspelled, or unsupported script file",
                    "error",
                    "path:%s" % self.path
                )
                self.events.append(key_err_event)
            except Exception as e:
                common_err_event = self._format_event(
                    str(e),
                    "Uncatergorized error when calling script file",
                    "error",
                    "path:%s" % self.path
                )
                self.events.append(common_err_event)
            finally:
                payload_temp["metrics"].extend(self.aggregator.flush())
                payload_temp['events'][self.path] = self.events
                if not payload_temp["metrics"] and not payload_temp['events'][self.path]:
                    no_out_event = self._format_event(
                        "There is no output when executing this script",
                        "Uncatergorized error when calling script file",
                        "error",
                        "path:%s" % self.path
                    )
                    payload_temp['events'][self.path] = no_out_event
                self.events = []
            # log.info(payload_temp['metrics'])
            # log.info(payload_temp['events'])
            collect_duration = timer.step()
            payload_temp.emit(log, self.agent_config, self.emitters, self.continue_running)
            emit_duration = timer.step()

            # 运行状况记录到日志中
            if self.run_count <= FLUSH_LOGGING_INITIAL or self.run_count % FLUSH_LOGGING_PERIOD == 0:
                log.info("Script: %s. Finished run #%s. Collection time: %ss. Emit time: %ss" %
                         (self.path, self.run_count, round(collect_duration, 2), round(emit_duration, 2)))
                if self.run_count == FLUSH_LOGGING_INITIAL:
                    log.info("Script: %s. First flushes done, next flushes will be logged every %s flushes." %
                             self.path, FLUSH_LOGGING_PERIOD)
            else:
                log.debug("Script: %s. Finished run #%s. Collection time: %ss. Emit time: %ss" %
                          (self.path, self.run_count, round(collect_duration, 2), round(emit_duration, 2)))
            time.sleep(self.interval)

    def _parse_metric(self, line):
        metric_dict = format_line(line)
        tags = metric_dict.get('tags', '')
        if tags:
            tags = tags.split(',')
        else:
            tags = []
        tags.extend(self.tag)
        self.__getattribute__(metric_dict['type'])(
            metric_dict['metric'],
            int(metric_dict['value']),
            tags)

    def _parse_events(self, line):
        event_dict = format_line(line)
        tags = [event_dict['tags']]
        tags.extend(self.tag)
        self.event(
            self._format_event(
                event_dict['msg'],
                event_dict['event'],
                event_dict['severity'],
                tags))

    def _format_event(self, msg, title, alert_type, tags):
            timestamp = time.time()
            msg_title = title
            msg_body = msg
            event = {
                'timestamp': timestamp,
                'host': self.hostname,
                'event_type': self.path,
                'msg_title': msg_title,
                'msg_text': msg_body,
                "alert_type": alert_type,
                "tags": tags
            }
            return event

    def _get_tags(self):
        tags = self.agent_config.get('tags', None)
        tag_temp = ["version:%s" % self.standard['version']]
        if tags:
            tag_temp.extend([tag.strip() for tag in tags.split(",")])
        return tag_temp

    def _standard_parser(self, path):
        """Returns a dict contains name, interval, version parsed from file"""
        if not self.standard['name']:
            self.standard['name'] = self.path
            file_object = open(path)
            lines = file_object.readlines()
            for key in self.standard.keys():
                for line in lines:
                    if key in line:
                        index = line.index('=')
                        self.standard[key] = line[index+1:].strip("\n")
                        break
            file_object.close()
            log.info("Script: %s Standards: %s" % (self.path, str(self.standard)))
            self.interval = int(self.standard['interval'])

    def _get_value(self):
        def caller(cmd):
            stdout_data, stderr_data = subprocess.Popen(
                cmd,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE
            ).communicate()
            return stdout_data, stderr_data

        def rm_unexps(out, err):
            quots = ["\"", "\'"]
            for quot in quots:
                out = out.replace(quot, '')
                err = err.replace(quot, '')
            if '\r\n' in out or '\r\n' in err:
                out = out.split('\r\n')
                err = err.split('\r\n')
            elif '\r' in out or '\r' in err:
                out = out.split('\r')
                err = err.split('\r')
            elif '\n' in out or '\n' in err:
                out = out.split('\n')
                err = err.split('\n')
            else:
                out = [out]
                err = [out]

            r_out = []
            r_err = []
            for i in out:
                try:
                    i = u'' + i
                except Exception:
                    i = ''
                finally:
                    r_out.append(i)
            for i in err:
                try:
                    i = u'' + i
                except Exception:
                    i = ''
                finally:
                    r_err.append(i)
            return r_out, r_err

        def vbscript_caller(path):
            cmd = 'cscript ' + path
            stdout_data, stderr_data = caller(cmd)
            # stdout_data = r'' + stdout_data
            return rm_unexps(stdout_data, stderr_data)

        def shscript_caller(path):
            cmd = ['sudo', 'bash', path]
            stdout_data, stderr_data = caller(cmd)
            log.info()
            return rm_unexps(stdout_data, stderr_data)

        def batscrit_caller(path):
            cmd = path
            stdout_data, stderr_data = caller(cmd)
            return rm_unexps(stdout_data, stderr_data)

        def pyscript_caller(path):
            cmd = [sys.executable, path]
            stdout_data, stderr_data = caller(cmd)
            return rm_unexps(stdout_data, stderr_data)

        file_type_methods = {
            'vbs': vbscript_caller,
            'sh': shscript_caller,
            'bat': batscrit_caller,
            'py': pyscript_caller}
        path_sects = self.path.split('.')
        res = file_type_methods[path_sects[-1]](self.path)
        return res

    def gauge(self, metric, value, tags=None, hostname=None, device_name=None, timestamp=None):
        """
        Record the value of a gauge, with optional tags, hostname and device
        name.

        :param metric: The name of the metric
        :param value: The value of the gauge
        :param tags: (optional) A list of tags for this metric
        :param hostname: (optional) A hostname for this metric. Defaults to the current hostname.
        :param device_name: (optional) The device name for this metric
        :param timestamp: (optional) The timestamp for this metric value
        """
        self.aggregator.gauge(metric, value, tags, hostname, device_name, timestamp)

    def increment(self, metric, value=1, tags=None, hostname=None, device_name=None):
        """
        Increment a counter with optional tags, hostname and device name.

        :param metric: The name of the metric
        :param value: The value to increment by
        :param tags: (optional) A list of tags for this metric
        :param hostname: (optional) A hostname for this metric. Defaults to the current hostname.
        :param device_name: (optional) The device name for this metric
        """
        self.aggregator.increment(metric, value, tags, hostname, device_name)

    def decrement(self, metric, value=-1, tags=None, hostname=None, device_name=None):
        """
        Increment a counter with optional tags, hostname and device name.

        :param metric: The name of the metric
        :param value: The value to decrement by
        :param tags: (optional) A list of tags for this metric
        :param hostname: (optional) A hostname for this metric. Defaults to the current hostname.
        :param device_name: (optional) The device name for this metric
        """
        self.aggregator.decrement(metric, value, tags, hostname, device_name)

    def count(self, metric, value=0, tags=None, hostname=None, device_name=None):
        """
        Submit a raw count with optional tags, hostname and device name

        :param metric: The name of the metric
        :param value: The value
        :param tags: (optional) A list of tags for this metric
        :param hostname: (optional) A hostname for this metric. Defaults to the current hostname.
        :param device_name: (optional) The device name for this metric
        """
        self.aggregator.submit_count(metric, value, tags, hostname, device_name)

    def monotonic_count(self, metric, value=0, tags=None,
                        hostname=None, device_name=None):
        """
        Submits a raw count with optional tags, hostname and device name
        based on increasing counter values. E.g. 1, 3, 5, 7 will submit
        6 on flush. Note that reset counters are skipped.

        :param metric: The name of the metric
        :param value: The value of the rate
        :param tags: (optional) A list of tags for this metric
        :param hostname: (optional) A hostname for this metric. Defaults to the current hostname.
        :param device_name: (optional) The device name for this metric
        """
        self.aggregator.count_from_counter(metric, value, tags,
                                           hostname, device_name)

    def rate(self, metric, value, tags=None, hostname=None, device_name=None):
        """
        Submit a point for a metric that will be calculated as a rate on flush.
        Values will persist across each call to `check` if there is not enough
        point to generate a rate on the flush.

        :param metric: The name of the metric
        :param value: The value of the rate
        :param tags: (optional) A list of tags for this metric
        :param hostname: (optional) A hostname for this metric. Defaults to the current hostname.
        :param device_name: (optional) The device name for this metric
        """
        self.aggregator.rate(metric, value, tags, hostname, device_name)

    def histogram(self, metric, value, tags=None, hostname=None, device_name=None):
        """
        Sample a histogram value, with optional tags, hostname and device name.

        :param metric: The name of the metric
        :param value: The value to sample for the histogram
        :param tags: (optional) A list of tags for this metric
        :param hostname: (optional) A hostname for this metric. Defaults to the current hostname.
        :param device_name: (optional) The device name for this metric
        """
        self.aggregator.histogram(metric, value, tags, hostname, device_name)

    def event(self, event):
        """
        Save an event.

        :param event: The event payload as a dictionary. Has the following
        structure:

            {
                "timestamp": int, the epoch timestamp for the event,
                "event_type": string, the event time name,
                "api_key": string, the api key of the account to associate the event with,
                "msg_title": string, the title of the event,
                "msg_text": string, the text body of the event,
                "alert_type": (optional) string, one of ('error', 'warning', 'success', 'info').
                    Defaults to 'info'.
                "source_type_name": (optional) string, the source type name,
                "host": (optional) string, the name of the host,
                "tags": (optional) list, a list of tags to associate with this event
            }
        """
        if event.get('api_key') is None:
            event['api_key'] = self.agent_config['api_key']
        self.events.append(event)


class ScriptMonitor(object):
    def __init__(self, agent_config, script_paths, emitters, hostname):
        self.ip = get_ip(agent_config, log)
        self.agent_config = agent_config
        self.script_paths = script_paths
        self.emitters = emitters
        self.hostname = hostname
        self.os = get_os()
        self.script_runners = []

    @log_exceptions(log)
    def run(self):
        log.info("scripts: %s" % str(self.script_paths))
        # 建造标准上传数据包
        payload = AgentPayload()
        self._build_payload(payload)
        host_tags = []
        if self.agent_config['tags'] is not None:
            host_tags.extend([tag.strip()
                              for tag in self.agent_config['tags'].split(",")])
        if host_tags:
            payload['host-tags']['system'] = host_tags
        if 'system' not in payload['host-tags']:
            payload['host-tags']['system'] = []
        # 收集配置中脚本路径 解析预定义规范 多线程执行脚本
        self.script_runners = []
        for script_path in self.script_paths:
            script_runner = ScriptRunner(
                self.agent_config,
                self.hostname,
                self.emitters,
                script_path
            )
            script_runner_thread = Thread(target=script_runner.run, args=(deepcopy(payload),))
            self.script_runners.append(script_runner_thread)

        for t in self.script_runners:
            t.start()
            time.sleep(1)

        for t in self.script_runners:
            t.join()

    def stop(self):
        for script_runner in self.script_runners:
            script_runner.continue_running = False

    def _build_payload(self, payload):
        """
        Build the payload skeleton, so it contains all of the generic payload data.
        """
        now = time.time()

        payload['ip'] = self.ip
        payload['collection_timestamp'] = now
        payload['os'] = self.os
        payload['python'] = sys.version
        payload['agentVersion'] = self.agent_config['version']
        payload['apiKey'] = self.agent_config['api_key']
        payload['events'] = {}
        payload['metrics'] = []
        payload['service_checks'] = []
        payload['resources'] = {}
        payload['internalHostname'] = self.hostname
        payload['uuid'] = get_uuid()
        payload['host-tags'] = {}
        payload['external_host_tags'] = {}


def format_line(line):
    """Returns a dict might contains metric, value, type, tags parsed or
    event, msg, severity, tags from line

    :param line: The string may contains information string
    """
    result = {'type': 'gauge', 'severity': 'warning'}
    parts = line.split('|')
    for part in parts:
        if '=' in part:
            index = part.index('=')
            result[part[:index]] = part[index+1:]
    return result


def raw(text):
    # 将每个可能的转义字符都进行了替换
    """Returns a raw string representation of text"""
    escape_dict = {
        '\a': r'\a',
        '\b': r'\b',
        '\c': r'\c',
        '\f': r'\f',
        '\n': r'\n',
        '\r': r'\r',
        '\t': r'\t',
        '\v': r'\v',
        '\'': r'\'',
        '\"': r'\"',
        '\0': r'\0',
        '\1': r'\1',
        '\2': r'\2',
        '\3': r'\3',
        '\4': r'\4',
        '\5': r'\5',
        '\6': r'\6',
        '\7': r'\7',
        '\8': r'\8',
        '\9': r'\9'
    }
    new_string = ''
    for char in text:
        try:
            new_string += escape_dict[char]
        except KeyError:
            new_string += char
    return new_string
