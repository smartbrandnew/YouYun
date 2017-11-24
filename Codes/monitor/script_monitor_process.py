# -*- coding: utf-8 -*-

__author__ = 'fangjc'
__project__ = 'Monitor-Agent'
__date__ = '2016/12/1'

from config import initialize_logging, get_checksd_path

initialize_logging('script_caller')

import logging
import os
import sys
import time

os.umask(027)

from checks.script_monitor import ScriptMonitor
from config import get_config, get_parsed_args
from emitter import http_emitter
from util import get_hostname

log = logging.getLogger("script_caller")


class ScriptCallerProcess(object):
    def __init__(self, agent_config, script_paths, hostname):
        self.hostname = hostname
        self.config = agent_config
        self.script_paths = script_paths

    def run(self):
        log.debug("Starting script_caller")
        emitters = self.get_emitters()
        script_monitor = ScriptMonitor(self.config, self.script_paths, emitters, self.hostname)
        script_monitor.run()

    def get_emitters(self):
        return [http_emitter]


def main():
    options, args = get_parsed_args()
    agent_config = get_config(options=options)
    hostname = get_hostname(agent_config)
    file_paths = []
    scripts_path = get_checksd_path().replace("checks.d", "scripts")
    file_names = os.listdir(scripts_path)
    for file_name in file_names:
        if file_name.endswith('.example'):
            continue
        file_paths.append(os.path.join(scripts_path, file_name))

    if file_paths:
        script_caller_process = ScriptCallerProcess(agent_config, file_paths, hostname)
        script_caller_process.run()
    else:
        log.info("Not starting script_caller_process: no valid script found")
        time.sleep(6)
        return 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception:
        try:
            log.exception("Uncaught error running the Agent")
        except Exception:
            pass
        raise
