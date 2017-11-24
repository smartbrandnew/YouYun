if __name__ == '__main__':
    from config import initialize_logging

    initialize_logging('jmxfetch')

import glob
import logging
import os
import signal
import sys
import tempfile
import time
from contextlib import nested

import yaml

from config import (
    DEFAULT_CHECK_FREQUENCY,
    get_confd_path,
    get_config,
    get_logging_config,
    PathNotFound,
)
from util import yLoader
from utils.jmx import JMX_FETCH_JAR_NAME, JMXFiles
from utils.platform import Platform
from utils.subprocess_output import subprocess

log = logging.getLogger('jmxfetch')

JAVA_LOGGING_LEVEL = {
    logging.CRITICAL: "FATAL",
    logging.DEBUG: "DEBUG",
    logging.ERROR: "ERROR",
    logging.FATAL: "FATAL",
    logging.INFO: "INFO",
    logging.WARN: "WARN",
    logging.WARNING: "WARN",
}

_JVM_DEFAULT_MAX_MEMORY_ALLOCATION = " -Xmx200m"
_JVM_DEFAULT_INITIAL_MEMORY_ALLOCATION = " -Xms50m"
JMXFETCH_MAIN_CLASS = "org.datadog.jmxfetch.App"
JMX_CHECKS = [
    'activemq',
    'activemq_58',
    'cassandra',
    'jmx',
    'solr',
    'tomcat',
]
JMX_COLLECT_COMMAND = 'collect'
JMX_LIST_COMMANDS = {
    'list_everything': 'List every attributes available that has a type supported by JMXFetch',
    'list_collected_attributes': 'List attributes that will actually be collected by your current instances configuration',
    'list_matching_attributes': 'List attributes that match at least one of your instances configuration',
    'list_not_matching_attributes': "List attributes that don't match any of your instances configuration",
    'list_limited_attributes': "List attributes that do match one of your instances configuration but that are not being collected because it would exceed the number of metrics that can be collected",
    JMX_COLLECT_COMMAND: "Start the collection of metrics based on your current configuration and display them in the console"}

LINK_TO_DOC = "See http://docs.datamonitorhq.com/integrations/java/ for more information"


class InvalidJMXConfiguration(Exception):
    pass


class JMXFetch(object):
    def __init__(self, confd_path, agentConfig):
        self.confd_path = confd_path
        self.agentConfig = agentConfig
        self.logging_config = get_logging_config()
        self.check_frequency = DEFAULT_CHECK_FREQUENCY

        self.jmx_process = None
        self.jmx_checks = None

    def terminate(self):
        self.jmx_process.terminate()

    def _handle_sigterm(self, signum, frame):
        log.debug("Caught sigterm. Stopping subprocess.")
        self.jmx_process.terminate()

    def register_signal_handlers(self):
        try:
            signal.signal(signal.SIGTERM, self._handle_sigterm)

            signal.signal(signal.SIGINT, self._handle_sigterm)

        except ValueError:
            log.exception("Unable to register signal handlers.")

    def configure(self, checks_list=None, clean_status_file=True):
        if clean_status_file:
            JMXFiles.clean_status_file()

        self.jmx_checks, self.invalid_checks, self.java_bin_path, self.java_options, \
        self.tools_jar_path, self.custom_jar_paths = \
            self.get_configuration(self.confd_path, checks_list=checks_list)

    def should_run(self):
        return self.jmx_checks is not None and self.jmx_checks != []

    def run(self, command=None, checks_list=None, reporter=None, redirect_std_streams=False):

        if checks_list or self.jmx_checks is None:
            self.configure(checks_list)

        try:
            command = command or JMX_COLLECT_COMMAND

            if len(self.invalid_checks) > 0:
                try:
                    JMXFiles.write_status_file(self.invalid_checks)
                except Exception:
                    log.exception("Error while writing JMX status file")

            if len(self.jmx_checks) > 0:
                return self._start(self.java_bin_path, self.java_options, self.jmx_checks,
                                   command, reporter, self.tools_jar_path, self.custom_jar_paths, redirect_std_streams)
            else:
                time.sleep(4)
                log.info("No valid JMX integration was found. Exiting ...")
        except Exception:
            log.exception("Error while initiating JMXFetch")
            raise

    @classmethod
    def get_configuration(cls, confd_path, checks_list=None):
        jmx_checks = []
        java_bin_path = None
        java_options = None
        tools_jar_path = None
        custom_jar_paths = []
        invalid_checks = {}

        for conf in glob.glob(os.path.join(confd_path, '*.yaml')):
            filename = os.path.basename(conf)
            check_name = filename.split('.')[0]

            if os.path.exists(conf):
                f = open(conf)
                try:
                    check_config = yaml.load(f.read(), Loader=yLoader)
                    assert check_config is not None
                    f.close()
                except Exception:
                    f.close()
                    log.error("Unable to parse yaml config in %s" % conf)
                    continue

                try:
                    is_jmx, check_java_bin_path, check_java_options, check_tools_jar_path, check_custom_jar_paths = \
                        cls._is_jmx_check(check_config, check_name, checks_list)
                    if is_jmx:
                        jmx_checks.append(filename)
                        if java_bin_path is None and check_java_bin_path is not None:
                            java_bin_path = check_java_bin_path
                        if java_options is None and check_java_options is not None:
                            java_options = check_java_options
                        if tools_jar_path is None and check_tools_jar_path is not None:
                            tools_jar_path = check_tools_jar_path
                        if check_custom_jar_paths:
                            custom_jar_paths.extend(check_custom_jar_paths)
                except InvalidJMXConfiguration, e:
                    log.error("%s check does not have a valid JMX configuration: %s" % (check_name, e))
                    check_name = check_name.encode('ascii', 'ignore')
                    invalid_checks[check_name] = str(e)

        return (jmx_checks, invalid_checks, java_bin_path, java_options, tools_jar_path, custom_jar_paths)

    def _start(self, path_to_java, java_run_opts, jmx_checks, command, reporter, tools_jar_path, custom_jar_paths,
               redirect_std_streams):
        if reporter is None:
            statsd_host = self.agentConfig.get('bind_host', 'localhost')
            statsd_port = self.agentConfig.get('monitorstatsd_port', "8125")
            reporter = "statsd:%s:%s" % (statsd_host, statsd_port)

        log.info("Starting jmxfetch:")
        try:
            path_to_java = path_to_java or "java"
            java_run_opts = java_run_opts or ""
            path_to_jmxfetch = self._get_path_to_jmxfetch()
            path_to_status_file = JMXFiles.get_status_file_path()

            classpath = path_to_jmxfetch
            if tools_jar_path is not None:
                classpath = r"%s:%s" % (tools_jar_path, classpath)
            if custom_jar_paths:
                classpath = r"%s:%s" % (':'.join(custom_jar_paths), classpath)

            subprocess_args = [
                path_to_java,
                '-classpath',
                classpath,
                JMXFETCH_MAIN_CLASS,
                '--check_period', str(self.check_frequency * 1000),
                '--conf_directory', r"%s" % self.confd_path,
                '--log_level', JAVA_LOGGING_LEVEL.get(self.logging_config.get("log_level"), "INFO"),
                '--log_location', r"%s" % self.logging_config.get('jmxfetch_log_file'),
                '--reporter', reporter,
                '--status_location', r"%s" % path_to_status_file,
                command,
            ]

            if Platform.is_windows():
                path_to_exit_file = JMXFiles.get_python_exit_file_path()
                subprocess_args.insert(len(subprocess_args) - 1, '--exit_file_location')
                subprocess_args.insert(len(subprocess_args) - 1, path_to_exit_file)

            subprocess_args.insert(4, '--check')
            for check in jmx_checks:
                subprocess_args.insert(5, check)

            if "Xmx" not in java_run_opts and "XX:MaxHeapSize" not in java_run_opts:
                java_run_opts += _JVM_DEFAULT_MAX_MEMORY_ALLOCATION
            if "Xms" not in java_run_opts and "XX:InitialHeapSize" not in java_run_opts:
                java_run_opts += _JVM_DEFAULT_INITIAL_MEMORY_ALLOCATION

            for opt in java_run_opts.split():
                subprocess_args.insert(1, opt)

            log.info("Running %s" % " ".join(subprocess_args))

            with nested(tempfile.TemporaryFile(), tempfile.TemporaryFile()) as (stdout_f, stderr_f):
                jmx_process = subprocess.Popen(
                    subprocess_args,
                    close_fds=not redirect_std_streams,
                    stdout=stdout_f if redirect_std_streams else None,
                    stderr=stderr_f if redirect_std_streams else None
                )
                self.jmx_process = jmx_process

                self.register_signal_handlers()

                jmx_process.wait()

                if redirect_std_streams:
                    stderr_f.seek(0)
                    err = stderr_f.read()
                    stdout_f.seek(0)
                    out = stdout_f.read()
                    sys.stdout.write(out)
                    sys.stderr.write(err)

            return jmx_process.returncode

        except OSError:
            java_path_msg = "Couldn't launch JMXTerm. Is Java in your PATH ?"
            log.exception(java_path_msg)
            invalid_checks = {}
            for check in jmx_checks:
                check_name = check.split('.')[0]
                check_name = check_name.encode('ascii', 'ignore')
                invalid_checks[check_name] = java_path_msg
            JMXFiles.write_status_file(invalid_checks)
            raise
        except Exception:
            log.exception("Couldn't launch JMXFetch")
            raise

    @staticmethod
    def _is_jmx_check(check_config, check_name, checks_list):
        init_config = check_config.get('init_config', {}) or {}
        java_bin_path = None
        java_options = None
        is_jmx = False
        is_attach_api = False
        tools_jar_path = init_config.get("tools_jar_path")
        custom_jar_paths = init_config.get("custom_jar_paths")

        if init_config is None:
            init_config = {}

        if checks_list:
            if check_name in checks_list:
                is_jmx = True

        elif init_config.get('is_jmx') or check_name in JMX_CHECKS:
            is_jmx = True

        if is_jmx:
            instances = check_config.get('instances', [])
            if type(instances) != list or len(instances) == 0:
                raise InvalidJMXConfiguration("You need to have at least one instance "
                                              "defined in the YAML file for this check")

            for inst in instances:
                if type(inst) != dict:
                    raise InvalidJMXConfiguration("Each instance should be"
                                                  " a dictionary. %s" % LINK_TO_DOC)
                host = inst.get('host', None)
                port = inst.get('port', None)
                conf = inst.get('conf', init_config.get('conf', None))
                tools_jar_path = inst.get('tools_jar_path')

                proc_regex = inst.get('process_name_regex')
                jmx_url = inst.get('jmx_url')
                name = inst.get('name')

                if proc_regex is not None:
                    is_attach_api = True
                elif jmx_url is not None:
                    if name is None:
                        raise InvalidJMXConfiguration("A name must be specified when using a jmx_url")
                else:
                    if host is None:
                        raise InvalidJMXConfiguration("A host must be specified")
                    if port is None or type(port) != int:
                        raise InvalidJMXConfiguration("A numeric port must be specified")

                if conf is None:
                    log.warning("%s doesn't have a 'conf' section. Only basic JVM metrics"
                                " will be collected. %s" % (inst, LINK_TO_DOC))
                else:
                    if type(conf) != list or len(conf) == 0:
                        raise InvalidJMXConfiguration("'conf' section should be a list"
                                                      " of configurations %s" % LINK_TO_DOC)

                    for config in conf:
                        include = config.get('include', None)
                        if include is None:
                            raise InvalidJMXConfiguration("Each configuration must have an"
                                                          " 'include' section. %s" % LINK_TO_DOC)

                        if type(include) != dict:
                            raise InvalidJMXConfiguration("'include' section must"
                                                          " be a dictionary %s" % LINK_TO_DOC)

            if java_bin_path is None:
                if init_config and init_config.get('java_bin_path'):
                    java_bin_path = init_config.get('java_bin_path')

                else:
                    for instance in instances:
                        if instance and instance.get('java_bin_path'):
                            java_bin_path = instance.get('java_bin_path')

            if java_options is None:
                if init_config and init_config.get('java_options'):
                    java_options = init_config.get('java_options')
                else:
                    for instance in instances:
                        if instance and instance.get('java_options'):
                            java_options = instance.get('java_options')

            if is_attach_api:
                if tools_jar_path is None:
                    for instance in instances:
                        if instance and instance.get("tools_jar_path"):
                            tools_jar_path = instance.get("tools_jar_path")

                if tools_jar_path is None:
                    raise InvalidJMXConfiguration("You must specify the path to tools.jar"
                                                  " in your JDK.")
                elif not os.path.isfile(tools_jar_path):
                    raise InvalidJMXConfiguration("Unable to find tools.jar at %s" % tools_jar_path)
            else:
                tools_jar_path = None

            if custom_jar_paths:
                if isinstance(custom_jar_paths, basestring):
                    custom_jar_paths = [custom_jar_paths]
                for custom_jar_path in custom_jar_paths:
                    if not os.path.isfile(custom_jar_path):
                        raise InvalidJMXConfiguration("Unable to find custom jar at %s" % custom_jar_path)

        return is_jmx, java_bin_path, java_options, tools_jar_path, custom_jar_paths

    def _get_path_to_jmxfetch(self):
        if not Platform.is_windows():
            return os.path.realpath(os.path.join(os.path.realpath(__file__), "..", "checks",
                                                 "libs", JMX_FETCH_JAR_NAME))
        return os.path.realpath(os.path.join(os.path.realpath(__file__), "..", "..",
                                             "jmxfetch", JMX_FETCH_JAR_NAME))


def init(config_path=None):
    agentConfig = get_config(parse_args=False, cfg_path=config_path)
    try:
        confd_path = get_confd_path()
    except PathNotFound, e:
        log.error("No conf.d folder found at '%s' or in the directory where"
                  "the Agent is currently deployed.\n" % e.args[0])

    return confd_path, agentConfig


def main(config_path=None):
    confd_path, agentConfig = init(config_path)

    jmx = JMXFetch(confd_path, agentConfig)
    return jmx.run()


if __name__ == '__main__':
    sys.exit(main())
