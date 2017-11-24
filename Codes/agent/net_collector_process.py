# -*- coding: utf-8 -*-
#!./embedded/bin/python


# set up logging before importing any other components
from config import initialize_logging # noqa
initialize_logging('net_collector')

# stdlib
import logging
import os
import sys
import time

# For pickle & PID files, see issue 293
os.umask(027)

# project
from checks.net_collector import NetCollector
from config import (
    check_yaml,
    get_confd_path,
    get_config,
    get_parsed_args,
    PathNotFound
)
from emitter import http_emitter

# uyun
from uyun.bat.ping import PingService


# Globals
log = logging.getLogger("net_collector")


class NetCollectorProcess(object):
    def __init__(self, agent_config, config):
        self.config = agent_config
        self.net_config = config['instances'][0]
        self.running = True

    def run(self):
        log.debug("Starting net_collector")
        emitters = self.get_emitters()

        ps = PingService()
        ps.startup()

        self.collector = NetCollector(self.config, self.net_config, emitters, ps)

        self.collector.run()

    def get_emitters(self):
        return [http_emitter]


def main():
    options, args = get_parsed_args()
    agent_config = get_config(options=options)

    try:
        confd_path = get_confd_path()
        conf_path = os.path.join(confd_path, '%s.yaml' % "net_collector")
        config = check_yaml(conf_path)
        log.debug("Net scan config: %s" % config)
    except PathNotFound as e:
        log.warn("Not starting net_collector_process: path conf.d does not exist %s." % e)
        time.sleep(6)
        return 0
    except IOError:
        log.info("Not starting net_collector_process: no valid configuration found")
        time.sleep(6)
        return 0
    else:
        net_collector_process = NetCollectorProcess(agent_config, config)
        net_collector_process.run()

if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception:
        # Try our best to log the error.
        try:
            log.exception("Uncaught error running the Agent")
        except Exception:
            pass
        raise
