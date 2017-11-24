# -*- coding: UTF-8 -*-

import os
import logging
import subprocess
import sys
import time
from copy import deepcopy
from threading import Thread

from aggregator import MetricsAggregator
from checks import agent_formatter
from collector import AgentPayload
from util import get_os, Timer, get_ip, get_machine_type
from util import get_uuid
from utils.logger import log_exceptions

log = logging.getLogger(__name__)

FLUSH_LOGGING_PERIOD = 10
FLUSH_LOGGING_INITIAL = 5
IS_WINDOWS = os.name == 'nt'


class ScriptRunner(object):
    def __init__(self, agent_config, hostname, emitters, path):
        self.run_count = 0
        self.events = []
        self.continue_running = True
        self.agent_config = agent_config
        self.hostname = hostname
        self.emitters = emitters
        self.path = path
        self.standard = {'name': None, 'interval': 15, 'version': '1.0.0'}
        self.interval = self.standard['interval']
        self.aggregator = MetricsAggregator(
            self.hostname,
            formatter=agent_formatter
        )
        self.tag = self._get_tags()

    def run(self, payload):
        log.info("Calling %s" % self.path)
        while self.continue_running:
            timer = Timer()
            payload_temp = deepcopy(payload)
            try:
                self._standard_parser(self.path)
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
            collect_duration = timer.step()
            payload_temp.emit(log, self.agent_config, self.emitters, self.continue_running)
            emit_duration = timer.step()

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
        if not self.standard['name']:
            self.standard['name'] = self.path
            file_object = open(path)
            lines = file_object.readlines()
            for key in self.standard.keys():
                for line in lines:
                    if key in line:
                        index = line.index('=')
                        self.standard[key] = line[index + 1:].strip("\n")
                        break
            file_object.close()
            log.info("Script: %s Standards: %s" % (self.path, str(self.standard)))
            self.interval = int(self.standard['interval'])

    def _get_value(self):
        def caller(cmd, shell=0):
            stdout_data, stderr_data = subprocess.Popen(
                cmd,
                shell=shell,
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
            log.info("cmd:{}, stdout_data:{}, stderr_data: {}".format(cmd, stdout_data, stderr_data))
            return rm_unexps(stdout_data, stderr_data)

        def shscript_caller(path):
            cmd = path
            stdout_data, stderr_data = caller(cmd, 1)
            log.info("cmd:{}, stdout_data:{}, stderr_data: {}".format(cmd, stdout_data, stderr_data))
            return rm_unexps(stdout_data, stderr_data)

        def batscrit_caller(path):
            cmd = path
            stdout_data, stderr_data = caller(cmd)
            log.info("cmd:{}, stdout_data:{}, stderr_data: {}".format(cmd, stdout_data, stderr_data))
            return rm_unexps(stdout_data, stderr_data)

        def pyscript_caller(path):
            cwd = os.environ.get('ANT_AGENT_DIR')
            if IS_WINDOWS:
                python_execute = os.path.join(cwd, "embedded\python.exe")
            else:
                python_execute = os.path.join(cwd, 'embedded/bin/python')
            cmd = [python_execute, path]
            stdout_data, stderr_data = caller(cmd)
            log.info("cmd:{}, stdout_data:{}, stderr_data: {}".format(cmd, stdout_data, stderr_data))
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

    def event(self, event):
        if event.get('api_key') is None:
            event['api_key'] = self.agent_config['api_key']
        self.events.append(event)


class ScriptMonitor(object):
    def __init__(self, agent_config, script_paths, emitters, hostname):
        self.ip = get_ip(agent_config)
        self.agent_config = agent_config
        self.script_paths = script_paths
        self.emitters = emitters
        self.hostname = hostname
        self.os = get_os()
        self.script_runners = []

    @log_exceptions(log)
    def run(self):
        log.info("scripts: %s" % str(self.script_paths))
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
        payload['machine_type'] = get_machine_type()
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
    result = {'type': 'gauge', 'severity': 'warning'}
    parts = line.split('|')
    for part in parts:
        if '=' in part:
            index = part.index('=')
            result[part[:index]] = part[index + 1:]
    return result


def raw(text):
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
