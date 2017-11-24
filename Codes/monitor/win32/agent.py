# -*- coding: utf-8 -*-

import logging
import multiprocessing
import os
import subprocess
import threading
import time
import sys
import traceback
import win32event
import win32service
from collections import deque
from optparse import Values

import servicemanager

import modules
import monitorstatsd
from checks.central_configurator import CentralConfigurator
from checks.control_script import DealScripts
from checks.collector import Collector
from checks.script_monitor import ScriptMonitor
from checks.updater import AgentUpdater
from config import (
    _unix_root_path,
    _windows_commondata_path,
    _windows_confd_path,
    get_checksd_path,
    get_confd_path,
    get_config,
    get_system_stats,
    load_check_directory,
    PathNotFound,
    set_win32_cert_path,
    set_win32_requests_ca_bundle_path,
)
from emitter import http_emitter
from jmxfetch import JMXFetch
from monitoragent import Application
from util import get_hostname
from utils.jmx import JMXFiles
from utils.profile import AgentProfiler

log = logging.getLogger(__name__)

SERVICE_SLEEP_INTERVAL = 1
MAX_FAILED_HEARTBEATS = 8
DEFAULT_COLLECTOR_PROFILE_INTERVAL = 20
POST_INTERVAL = 120


class AgentSvc(object):
    def __init__(self):
        config = get_config(parse_args=False)

        opts, args = Values({
            'autorestart': False,
            'm_url': None,
            'use_forwarder': True,
            'disabled_dd': False,
            'profile': False
        }), []
        agentConfig = get_config(parse_args=False, options=opts)
        self.api_key = config.get('api_key', 'APIKEYHERE')
        self.hostname = get_hostname(agentConfig)

        self._collector_heartbeat, self._collector_send_heartbeat = multiprocessing.Pipe(False)
        self._collector_failed_heartbeats = 0
        self._max_failed_heartbeats = \
            MAX_FAILED_HEARTBEATS * agentConfig['check_freq'] / SERVICE_SLEEP_INTERVAL

        self._MAX_JMXFETCH_RESTARTS = 3
        self._count_jmxfetch_restarts = 0

        self.procs = {
            'forwarder': ProcessWatchmonitor("forwarder", DDForwarder(config, self.hostname)),
            'collector': ProcessWatchmonitor("collector", Monitoragent(agentConfig, self.hostname,
                                                                       heartbeat=self._collector_send_heartbeat)),
            'monitorstatsd': ProcessWatchmonitor("monitorstatsd", MonitorstatsdProcess(config, self.hostname)),
            'jmxfetch': ProcessWatchmonitor("jmxfetch", JMXFetchProcess(config, self.hostname), 3),
            "updater": ProcessWatchmonitor("updater", UpdaterProcess(config, self.hostname)),
            "customscript": ProcessWatchmonitor("customscript", CustomScripts())
        }

        script_paths = []
        scripts_path = get_checksd_path().replace("checks.d", "scripts")
        file_names = os.listdir(scripts_path)
        for file_name in file_names:
            if file_name.endswith('.vbs') or file_name.endswith('.sh') or \
                    file_name.endswith('.bat') or file_name.endswith('.py'):
                script_paths.append(os.path.join(scripts_path, file_name))
        if script_paths:
            script_caller = ScriptCaller(agentConfig, self.hostname, script_paths=script_paths)
            self.procs.update({'script_caller': ProcessWatchmonitor("script_caller", script_caller)})
        else:
            log.info("Not starting script_caller_process: no valid script found")

    def SvcStop(self):
        self.ReportServiceStatus(win32service.SERVICE_STOP_PENDING)
        win32event.SetEvent(self.hWaitStop)

        self.running = False
        for proc in self.procs.values():
            proc.terminate()

    def SvcDoRun(self):
        if self.api_key == 'APIKEYHERE':
            log.info('Invalid api key, stopping...')
            return
        self.start_ts = time.time()

        for proc in self.procs.values():
            proc.start()

        self.running = True
        while self.running:
            for name, proc in self.procs.iteritems():
                if not proc.is_alive() and proc.is_enabled():
                    servicemanager.LogInfoMsg("%s has died. Restarting..." % name)
                    proc.restart()

            self._check_collector_blocked()

            time.sleep(SERVICE_SLEEP_INTERVAL)

    def _check_collector_blocked(self):
        if self._collector_heartbeat.poll():
            while self._collector_heartbeat.poll():
                self._collector_heartbeat.recv()
            self._collector_failed_heartbeats = 0
        else:
            self._collector_failed_heartbeats += 1
            if self._collector_failed_heartbeats > self._max_failed_heartbeats:
                servicemanager.LogInfoMsg(
                    "%s was unresponsive for too long. Restarting..." % 'collector')
                self.procs['collector'].restart()
                self._collector_failed_heartbeats = 0


class ProcessWatchmonitor(object):
    DEFAULT_MAX_RESTARTS = 5
    _RESTART_TIMEFRAME = 3600

    def __init__(self, name, process, max_restarts=None):
        self._name = name
        self._process = process
        self._restarts = deque([])
        self._max_restarts = max_restarts or self.DEFAULT_MAX_RESTARTS

    def start(self):
        return self._process.start()

    def terminate(self):
        return self._process.terminate()

    def is_alive(self):
        return self._process.is_alive()

    def is_enabled(self):
        return self._process.is_enabled

    def _can_restart(self):
        now = time.time()
        while (self._restarts and self._restarts[0] < now - self._RESTART_TIMEFRAME):
            self._restarts.popleft()

        return len(self._restarts) < self._max_restarts

    def restart(self):
        if not self._can_restart():
            servicemanager.LogInfoMsg(
                "{0} reached the limit of restarts ({1} tries during the last {2}s"
                " (max authorized: {3})). Not restarting..."
                    .format(self._name, len(self._restarts),
                            self._RESTART_TIMEFRAME, self._max_restarts)
            )
            self._process.is_enabled = False
            return

        self._restarts.append(time.time())
        if self._process.is_alive():
            self._process.terminate()

        self._process = self._process.__class__(
            self._process.config, self._process.hostname,
            **self._process.options
        )

        self._process.start()


class ScriptCaller(multiprocessing.Process):
    def __init__(self, agentConfig, hostname, **options):
        multiprocessing.Process.__init__(self, name='script_caller')
        self.config = agentConfig
        self.hostname = hostname
        self.options = options
        self.script_paths = options['script_paths']
        self.is_enabled = True

    def run(self):
        from config import initialize_logging
        initialize_logging('windows_script_caller')
        log.debug("Windows Service - Starting script_caller")
        emitters = self.get_emitters()

        self.ScriptMonitor = ScriptMonitor(
            self.config, self.script_paths, emitters, self.hostname)

        self.ScriptMonitor.run()

    def stop(self):
        log.debug("Windows Service - Stopping script_caller")
        self.ScriptMonitor.stop()
        log.debug("Windows Service - Stopped script_caller - Exit by command")

    def get_emitters(self):
        emitters = [http_emitter]
        custom = [s.strip() for s in self.config.get(
            'custom_emitters', '').split(',')]
        for emitter_spec in custom:
            if not emitter_spec:
                continue
            emitters.append(modules.load(emitter_spec, 'emitter'))

        return emitters


class UpdaterProcess(multiprocessing.Process):
    def __init__(self, config, hostname, **options):
        multiprocessing.Process.__init__(self, name='updater')
        self.is_enabled = True
        self.running = True
        self.hostname = hostname
        self.options = options
        self.interval = config['updater_interval']
        self.server = 'updater_server'
        self.post_interval = config['post_interval']
        self.linux_conf = _unix_root_path()
        self.windows_conf = _windows_commondata_path()
        self.central_configuration_switch = config['central_configuration_switch']
        self.central_configuration_api_key = config['api_key']
        self.central_configuration_url = config['m_url'].replace('api/v2/gateway/dd-agent', 'api/v2/agent/config/')
        self.api_url = config['m_url'].replace('api/v2/gateway/dd-agent', 'api/autosync/client/')
        self.dirs = {'conf': _windows_commondata_path(),
                     'checks': _windows_confd_path()
                     }

    def run(self):
        from config import initialize_logging
        set_win32_requests_ca_bundle_path()
        initialize_logging('windows_updater')
        log.debug("Windows Service - Starting updater")
        t_list = []
        t1 = threading.Thread(target=self.updater)
        t_list.append(t1)
        if self.central_configuration_switch == 'yes':
            t2 = threading.Thread(target=self.configurator)
            t_list.append(t2)
        for t in t_list:
            t.start()
        for t in t_list:
            t.join()

    def do_restart(self):
        cwd = os.environ.get('ANT_AGENT_DIR')
        os.system('{} {} restart local-monitor*'.format(os.path.join(cwd, 'embedded\python.exe'), os.path.join(cwd, "bin\circlectl")))

    def restart(self):
        try:
            from multiprocessing import Process
            p = Process(target=self.do_restart)
            p.start()
        except Exception as e:
            log.error("restart local monitor exception: {}".format(traceback.format_exc()))

    def updater(self):
        while self.running:
            time.sleep(int(self.interval))
            try:
                au = AgentUpdater("Windows", self.server, self.api_url, self.dirs)
                au.do_update()
                self.restart()
                log.info("update agent success")
            except Exception, e:
                log.info("update agent failed %s" % e)

    def configurator(self):
        while self.running:
            time.sleep(int(self.post_interval))
            try:
                cc = CentralConfigurator("Windows", self.linux_conf, self.windows_conf, self.central_configuration_url,
                                         self.central_configuration_api_key)
                done = cc.do_configurator()
                if done == 1:
                    self.restart()
                    log.info("central configuration success")
                else:
                    log.info("no central configuration needed")
            except Exception, e:
                log.info("centralize configuration failed %s" % e)

    def stop(self):
        log.debug("Windows Service - Stopping updater")
        self.running = False
        log.debug("Windows Service - Stopped updater - Exit by command")


class CustomScripts(multiprocessing.Process):
    def __init__(self):
        multiprocessing.Process.__init__(self, name='customscript')
        self.is_enabled = True
        self.running = True
        c = get_config()
        self.request_interval = c.get("request_interval", POST_INTERVAL)

    def do_restart(self):
        cwd = os.environ.get('ANT_AGENT_DIR')
        os.system('{} {} restart local-monitor*'.format(os.path.join(cwd, 'embedded\python.exe'), os.path.join(cwd, "bin\circlectl")))

    def restart(self):
        try:
            from multiprocessing import Process
            p = Process(target=self.do_restart)
            p.start()
        except Exception:
            log.error("restart local monitor exception: {}".format(traceback.format_exc()))

    def run(self):
        from config import initialize_logging
        set_win32_requests_ca_bundle_path()
        initialize_logging('windows_custom_script')
        log.debug("Windows Service - Starting updater")
        while self.running:
            deal_script = DealScripts()
            if deal_script.run():
                self.restart()
            time.sleep(self.request_interval)

    def stop(self):
        log.debug("Windows Service - Stopping script_caller")
        self.running = False
        log.debug("Windows Service - Stopped script_caller - Exit by command")


class Monitoragent(multiprocessing.Process):
    def __init__(self, agentConfig, hostname, **options):
        multiprocessing.Process.__init__(self, name='monitoragent')
        self.config = agentConfig
        self.hostname = hostname
        self.options = options
        self._heartbeat = options.get('heartbeat')
        self.running = True
        self.is_enabled = True

    def run(self):
        from config import initialize_logging
        initialize_logging('windows_collector')
        log.debug("Windows Service - Starting collector")
        set_win32_requests_ca_bundle_path()
        emitters = self.get_emitters()
        systemStats = get_system_stats()
        self.collector = Collector(self.config, emitters, systemStats, self.hostname)

        in_developer_mode = self.config.get('developer_mode')

        collector_profile_interval = self.config.get('collector_profile_interval',
                                                     DEFAULT_COLLECTOR_PROFILE_INTERVAL)
        profiled = False
        collector_profiled_runs = 0

        checksd = load_check_directory(self.config, self.hostname)

        while self.running:
            if self._heartbeat:
                self._heartbeat.send(0)

            if in_developer_mode and not profiled:
                try:
                    profiler = AgentProfiler()
                    profiler.enable_profiling()
                    profiled = True
                except Exception as e:
                    log.warn("Cannot enable profiler: %s" % str(e))

            self.collector.run(checksd=checksd)

            if profiled:
                if collector_profiled_runs >= collector_profile_interval:
                    try:
                        profiler.disable_profiling()
                        profiled = False
                        collector_profiled_runs = 0
                    except Exception as e:
                        log.warn("Cannot disable profiler: %s" % str(e))
                else:
                    collector_profiled_runs += 1

            time.sleep(self.config['check_freq'])

    def stop(self):
        log.debug("Windows Service - Stopping collector")
        self.collector.stop()
        self.running = False

    def get_emitters(self):
        emitters = [http_emitter]
        custom = [s.strip() for s in
                  self.config.get('custom_emitters', '').split(',')]
        for emitter_spec in custom:
            if not emitter_spec:
                continue
            emitters.append(modules.load(emitter_spec, 'emitter'))

        return emitters


class DDForwarder(multiprocessing.Process):
    def __init__(self, agentConfig, hostname, **options):
        multiprocessing.Process.__init__(self, name='ddforwarder')
        self.config = agentConfig
        self.is_enabled = True
        self.hostname = hostname
        self.options = options

    def run(self):
        from config import initialize_logging
        initialize_logging('windows_forwarder')
        log.debug("Windows Service - Starting forwarder")
        set_win32_cert_path()
        set_win32_requests_ca_bundle_path()
        port = self.config.get('listen_port', 17123)
        if port is None:
            port = 17123
        else:
            port = int(port)
        app_config = get_config(parse_args=False)
        self.forwarder = Application(port, app_config, watchmonitor=False)
        try:
            self.forwarder.run()
        except Exception:
            log.exception("Uncaught exception in the forwarder")

    def stop(self):
        log.debug("Windows Service - Stopping forwarder")
        self.forwarder.stop()


class MonitorstatsdProcess(multiprocessing.Process):
    def __init__(self, agentConfig, hostname, **options):
        multiprocessing.Process.__init__(self, name='monitorstatsd')
        self.config = agentConfig
        self.is_enabled = self.config.get('use_monitorstatsd', True)
        self.hostname = hostname
        self.options = options

    def run(self):
        from config import initialize_logging
        initialize_logging('windows_monitorstatsd')
        if self.is_enabled:
            log.debug("Windows Service - Starting monitorstatsd server")
            self.reporter, self.server, _ = monitorstatsd.init(use_forwarder=True)
            self.reporter.start()
            self.server.start()
        else:
            log.info("monitorstatsd is not enabled, not starting it.")

    def stop(self):
        if self.is_enabled:
            log.debug("Windows Service - Stopping monitorstatsd server")
            self.server.stop()
            self.reporter.stop()
            self.reporter.join()


class JMXFetchProcess(multiprocessing.Process):
    def __init__(self, agentConfig, hostname, **options):
        multiprocessing.Process.__init__(self, name='jmxfetch')
        self.config = agentConfig
        self.hostname = hostname
        self.options = options

        try:
            confd_path = get_confd_path()
            self.jmx_daemon = JMXFetch(confd_path, agentConfig)
            self.jmx_daemon.configure()
            self.is_enabled = self.jmx_daemon.should_run()

        except PathNotFound:
            self.is_enabled = False

    def run(self):
        from config import initialize_logging
        initialize_logging('jmxfetch')
        if self.is_enabled:
            log.debug("Windows Service - Starting JMXFetch")
            JMXFiles.clean_exit_file()
            self.jmx_daemon.run()
        else:
            log.info("Windows Service - Not starting JMXFetch: no valid configuration found")

    def terminate(self):
        JMXFiles.write_exit_file()
        self.join()


if __name__ == '__main__':
    multiprocessing.freeze_support()
    agent_svc = AgentSvc()
    agent_svc.SvcDoRun()
