# -*- coding: utf-8 -*-
# !/opt/datadog-agent/embedded/bin/python


from config import initialize_logging

initialize_logging('updater')

import logging
import os
import sys
import time
import threading
import traceback

from checks.updater import AgentUpdater
from checks.central_configurator import CentralConfigurator
from config import (
    get_confd_path,
    get_config,
    get_parsed_args,
    PathNotFound,
    _unix_confd_path,
    _unix_checksd_path,
)

os.umask(027)

log = logging.getLogger('updater')


class UpdaterProcess(object):
    def __init__(self):
        c = get_config()
        self.update_interval = c['updater_interval']
        self.post_interval = c['post_interval']
        self.server = 'updater_server'
        self.linux_conf = os.path.realpath(os.path.join(_unix_confd_path(), '../'))
        self.windows_conf = 'C:/ProgramData/Datamonitor/'
        self.central_configuration_switch = c['central_configuration_switch']
        self.central_configuration_api_key = c['api_key']
        self.central_configuration_url = c['m_url'].replace('api/v2/gateway/dd-agent', 'api/v2/agent/config/')
        self.api_url = c['m_url'].replace('api/v2/gateway/dd-agent', 'api/autosync/client/')
        self.dirs = {
            'checks': os.path.realpath(os.path.join(_unix_checksd_path(), '../')),
            'conf': os.path.realpath(os.path.join(_unix_confd_path(), '../'))
        }

    def run(self):
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
        os.system('{} {} restart local*'.format(os.path.join(cwd, 'embedded/bin/python'), os.path.join(cwd, "bin/circlectl")))

    def restart(self):
        try:
            from multiprocessing import Process
            p = Process(target=self.do_restart)
            p.start()
        except Exception as e:
            log.error("restart local monitor exception: {}".format(traceback.format_exc()))

    def updater(self):
        while True:
            time.sleep(int(self.update_interval))
            try:
                au = AgentUpdater("Linux", self.server, self.api_url, self.dirs)
                au.do_update()
                self.restart()
                log.info("update agent success")
            except Exception as e:
                log.info("update agent failed %s" % e)

    def configurator(self):
        while True:
            time.sleep(int(self.post_interval))
            try:
                cc = CentralConfigurator("Linux", self.linux_conf, self.windows_conf, self.central_configuration_url,
                                         self.central_configuration_api_key)
                done = cc.do_configurator()
                if done == 1:
                    self.restart()
                    log.info("central configuration success")
                else:
                    log.info("no central configuration needed")
            except Exception, e:
                log.info("centralize configuration failed %s" % e)


def main():
    options, args = get_parsed_args()
    agent_config = get_config(options=options)

    try:
        confd_path = get_confd_path()
        conf_path = os.path.join(confd_path, '%s.yaml' % "updater")
        log.info(conf_path)
    except PathNotFound as e:
        log.warn("Path conf.d does not exist %s." % e)
    except IOError:
        log.info("Not starting updater: no valid configuration found")
    else:
        updater_process = UpdaterProcess()
        updater_process.run()


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception:
        try:
            log.exception("Uncaught error running the Agent")
        except Exception:
            pass
        raise
