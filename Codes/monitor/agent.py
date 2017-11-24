#!./embedded/bin/python

from config import get_version, initialize_logging

initialize_logging('collector')

import logging
import os
import signal
import sys
import time

os.umask(027)

from checks.check_status import CollectorStatus
from checks.collector import Collector
from config import (
    get_config,
    get_parsed_args,
    get_system_stats,
    load_check_directory,
)
from daemon import AgentSupervisor, Daemon
from emitter import http_emitter
from util import (
    EC2,
    get_hostname,
    Watchmonitor,
)
from utils.flare import configcheck, Flare
from utils.jmx import jmx_command
from utils.pidfile import PidFile
from utils.profile import AgentProfiler

PID_NAME = "monitor-agent"
PID_DIR = None
WATCHmonitor_MULTIPLIER = 10
RESTART_INTERVAL = 4 * 24 * 60 * 60
START_COMMANDS = ['start', 'restart', 'foreground']
DD_AGENT_COMMANDS = ['check', 'flare', 'jmx']

DEFAULT_COLLECTOR_PROFILE_INTERVAL = 20

log = logging.getLogger('collector')


class Agent(Daemon):
    def __init__(self, pidfile, autorestart, start_event=True, in_developer_mode=False):
        Daemon.__init__(self, pidfile, autorestart=autorestart)
        self.run_forever = True
        self.collector = None
        self.start_event = start_event
        self.in_developer_mode = in_developer_mode
        self._agentConfig = {}
        self._checksd = []
        self.collector_profile_interval = DEFAULT_COLLECTOR_PROFILE_INTERVAL
        self.check_frequency = None
        self.configs_reloaded = False

    def _handle_sigterm(self, signum, frame):

        log.debug("Caught sigterm. Stopping run loop.")
        self.run_forever = False

        if self.collector:
            self.collector.stop()
        log.debug("Collector is stopped.")

    def _handle_sigusr1(self, signum, frame):

        self._handle_sigterm(signum, frame)
        self._do_restart()

    def _handle_sighup(self, signum, frame):

        log.info("SIGHUP caught!")
        self.reload_configs()
        self.configs_reloaded = True

    def reload_configs(self):

        log.info("Attempting a configuration reload...")

        hostname = get_hostname(self._agentConfig)
        self._checksd = load_check_directory(self._agentConfig, hostname)

        num_checks = len(self._checksd['initialized_checks'])
        if num_checks > 0:
            log.info("Successfully reloaded {num_checks} checks".
                     format(num_checks=num_checks))
        else:
            log.info("No checksd configs found")

    @classmethod
    def info(cls, verbose=None):
        logging.getLogger().setLevel(logging.ERROR)
        return CollectorStatus.print_latest_status(verbose=verbose)

    def run(self, config=None):

        signal.signal(signal.SIGTERM, self._handle_sigterm)

        signal.signal(signal.SIGUSR1, self._handle_sigusr1)

        signal.signal(signal.SIGINT, self._handle_sigterm)

        signal.signal(signal.SIGHUP, self._handle_sighup)

        CollectorStatus().persist()

        if not config:
            config = get_config(parse_args=True)

        self._agentConfig = self._set_agent_config_hostname(config)
        hostname = get_hostname(self._agentConfig)
        systemStats = get_system_stats()
        emitters = self._get_emitters()

        self._checksd = load_check_directory(self._agentConfig, hostname)

        self.collector = Collector(self._agentConfig, emitters, systemStats, hostname)

        self.collector_profile_interval = self._agentConfig.get('collector_profile_interval',
                                                                DEFAULT_COLLECTOR_PROFILE_INTERVAL)

        self.check_frequency = int(self._agentConfig['check_freq'])
        watchmonitor = self._get_watchmonitor(self.check_frequency)

        self.restart_interval = int(self._agentConfig.get('restart_interval', RESTART_INTERVAL))
        self.agent_start = time.time()

        profiled = False
        collector_profiled_runs = 0

        while self.run_forever:
            log.debug("Found {num_checks} checks".format(num_checks=len(self._checksd['initialized_checks'])))

            if self.in_developer_mode and not profiled:
                try:
                    profiler = AgentProfiler()
                    profiler.enable_profiling()
                    profiled = True
                except Exception as e:
                    log.warn("Cannot enable profiler: %s" % str(e))

            self.collector.run(checksd=self._checksd,
                               start_event=self.start_event,
                               configs_reloaded=self.configs_reloaded)
            if self.configs_reloaded:
                self.configs_reloaded = False
            if profiled:
                if collector_profiled_runs >= self.collector_profile_interval:
                    try:
                        profiler.disable_profiling()
                        profiled = False
                        collector_profiled_runs = 0
                    except Exception as e:
                        log.warn("Cannot disable profiler: %s" % str(e))

            if self.autorestart and self._should_restart():
                self._do_restart()

            if self.run_forever:
                if watchmonitor:
                    watchmonitor.reset()
                if profiled:
                    collector_profiled_runs += 1
                log.debug("Sleeping for {0} seconds".format(self.check_frequency))
                time.sleep(self.check_frequency)

        try:
            CollectorStatus.remove_latest_status()
        except Exception:
            pass

        log.info("Exiting. Bye bye.")
        sys.exit(0)

    def _get_emitters(self):
        return [http_emitter]

    def _get_watchmonitor(self, check_freq):
        watchmonitor = None
        if self._agentConfig.get("watchmonitor", True):
            watchmonitor = Watchmonitor(check_freq * WATCHmonitor_MULTIPLIER,
                                        max_mem_mb=self._agentConfig.get('limit_memory_consumption', None))
            watchmonitor.reset()
        return watchmonitor

    def _set_agent_config_hostname(self, agentConfig):
        if agentConfig.get('hostname') is None and agentConfig.get('use_ec2_instance_id'):
            instanceId = EC2.get_instance_id(agentConfig)
            if instanceId is not None:
                log.info("Running on EC2, instanceId: %s" % instanceId)
                agentConfig['hostname'] = instanceId
            else:
                log.info('Not running on EC2, using hostname to identify this server')
        return agentConfig

    def _should_restart(self):
        if time.time() - self.agent_start > self.restart_interval:
            return True
        return False

    def _do_restart(self):
        log.info("Running an auto-restart.")
        if self.collector:
            self.collector.stop()
        sys.exit(AgentSupervisor.RESTART_EXIT_STATUS)


def main():
    options, args = get_parsed_args()
    agentConfig = get_config(options=options)
    autorestart = agentConfig.get('autorestart', False)
    hostname = get_hostname(agentConfig)
    in_developer_mode = agentConfig.get('developer_mode')
    COMMANDS_AGENT = [
        'start',
        'stop',
        'restart',
        'status',
        'foreground',
    ]

    COMMANDS_NO_AGENT = [
        'info',
        'check',
        'configcheck',
        'jmx',
        'flare',
    ]

    COMMANDS = COMMANDS_AGENT + COMMANDS_NO_AGENT

    if len(args) < 1:
        sys.stderr.write("Usage: %s %s\n" % (sys.argv[0], "|".join(COMMANDS)))
        return 2

    command = args[0]
    if command not in COMMANDS:
        sys.stderr.write("Unknown command: %s\n" % command)
        return 3

    if command not in DD_AGENT_COMMANDS:
        from utils.deprecations import deprecate_old_command_line_tools
        deprecate_old_command_line_tools()

    if command in COMMANDS_AGENT:
        agent = Agent(PidFile(PID_NAME, PID_DIR).get_path(), autorestart, in_developer_mode=in_developer_mode)

    if command in START_COMMANDS:
        log.info('Agent version %s' % get_version())

    if 'start' == command:
        log.info('Start daemon')
        agent.start()

    elif 'stop' == command:
        log.info('Stop daemon')
        agent.stop()

    elif 'restart' == command:
        log.info('Restart daemon')
        agent.restart()

    elif 'status' == command:
        agent.status()

    elif 'info' == command:
        return Agent.info(verbose=options.verbose)

    elif 'foreground' == command:
        logging.info('Running in foreground')
        if autorestart:
            logging.info('Running Agent with auto-restart ON')

            def child_func():
                agent.start(foreground=True)

            def parent_func():
                agent.start_event = False

            AgentSupervisor.start(parent_func, child_func)
        else:
            agent.start(foreground=True)

    elif 'check' == command:
        if len(args) < 2:
            sys.stderr.write(
                "Usage: %s check <check_name> [check_rate]\n"
                "Add check_rate as last argument to compute rates\n"
                % sys.argv[0]
            )
            return 1

        check_name = args[1]
        try:
            import checks.collector
            print getattr(checks.collector, check_name)(log).check(agentConfig)
        except Exception:
            checks = load_check_directory(agentConfig, hostname)
            for check in checks['initialized_checks']:
                if check.name == check_name:
                    if in_developer_mode:
                        check.run = AgentProfiler.wrap_profiling(check.run)

                    cs = Collector.run_single_check(check, verbose=True)
                    print CollectorStatus.render_check_status(cs)

                    if len(args) == 3 and args[2] == 'check_rate':
                        print "Running 2nd iteration to capture rate metrics"
                        time.sleep(1)
                        cs = Collector.run_single_check(check, verbose=True)
                        print CollectorStatus.render_check_status(cs)

                    check.stop()

    elif 'configcheck' == command or 'configtest' == command:
        configcheck()

    elif 'jmx' == command:
        jmx_command(args[1:], agentConfig)

    elif 'flare' == command:
        Flare.check_user_rights()
        case_id = int(args[1]) if len(args) > 1 else None
        f = Flare(True, case_id)
        f.collect()
        try:
            f.upload()
        except Exception, e:
            print 'The upload failed:\n{0}'.format(str(e))

    return 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except StandardError:
        try:
            log.exception("Uncaught error running the Agent")
        except Exception:
            pass
        raise