# -*- coding: utf-8 -*-

from config import initialize_logging  # noqa
initialize_logging('custom_script')
import sys
import os
import time
import logging
import traceback


from checks.control_script import DealScripts
from config import (
    check_yaml,
    get_confd_path,
    get_config,
    get_parsed_args,
    PathNotFound,
)


os.umask(027)
log = logging.getLogger('custom_script')
POST_INTERVAL = 120


class CustomScripts(object):
    def __init__(self):
        c = get_config()
        self.request_interval = c.get("request_interval", POST_INTERVAL)

    def do_restart(self):
        cwd = os.environ.get('ANT_AGENT_DIR')
        os.system('{} {} restart local*'.format(os.path.join(cwd, 'embedded/bin/python'), os.path.join(cwd, "bin/circlectl")))

    def restart(self):
        try:
            from multiprocessing import Process
            p = Process(target=self.do_restart)
            p.start()
        except Exception:
            log.error("restart local monitor exception: {}".format(traceback.format_exc()))

    def run(self):
        while 1:
            deal_script = DealScripts()
            if deal_script.run():
                self.restart()
            time.sleep(self.request_interval)


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
        cs = CustomScripts()
        cs.run()


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception:
        try:
            log.exception("Uncaught error running the Agent")
        except Exception:
            pass
        raise