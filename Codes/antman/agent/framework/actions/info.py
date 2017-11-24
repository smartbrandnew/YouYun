# coding: utf-8

import pf
import json
import sysutil
import time

from framework.actions.utils import get_agent_config


def get_first_report():
    platform = pf.get_platform()
    config = get_agent_config()

    num = 0
    while not config.get('id') and num < 10:
        time.sleep(1)
        config = get_agent_config()
        num += 1

    if not config.get('id'):
        print(json.dumps({}))
        return

    first_report = {
        'id': config.get('id'),
        'system': platform.system,
        'dist': platform.dist,
        'dist_version': platform.version,
        'arch': platform.cpu,
        'kernel': platform.kernel,
        'network_domain': config.get('network_domain'),
        'ip': config.get('ip') or sysutil.main_ip()
    }
    print(json.dumps(first_report))
    return first_report


get_first_report()
