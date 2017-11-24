from __future__ import absolute_import

import logging

import os
import pf
import nfs
import sys
import sysutil
import subprocess
import time

from tornado import web, escape
from framework.config import config
from framework.ioloop import get_io_loop
from framework.schedulers.status import StatusScheduler
from framework.schedulers.heartbeat import HeartbeatScheduler
from framework.schedulers.message import (MessageFetchScheduler,
                                          MessageCacheScheduler)
from framework.schedulers.task import TaskSyncScheduler
from framework.controllers.task import TaskLogHandler, TaskResultHandler
from framework.controllers.file import FileHandler
from framework.message.reactor import reactor
from framework.message.handlers.task import NewTaskHandler, KillTaskHandler
from framework.message import cache
from framework.message.models import Message
from framework.message.type import MessageType
from framework.message.transfers import get_current_transfer
from framework import settings

logger = None


def init_bootstrap():
    bootstrap = os.path.join(os.getcwd(), 'bootstrap.py')
    if os.path.exists(bootstrap):
        logger.info('Detect bootstrap script, ready to perform')
        exit_code = subprocess.call([sys.executable, bootstrap])
        if exit_code != 0:
            logger.info('Abnormal exit code: %d', exit_code)
            sys.exit(exit_code)
        else:
            logger.info('Perform bootstrap script successfully')
            nfs.remove(bootstrap)


def init_files():
    if not os.path.exists(settings.REPO_DIR):
        os.mkdir(settings.REPO_DIR)

    if not os.path.exists(settings.LOGS_DIR):
        os.mkdir(settings.LOGS_DIR)

    if not os.path.exists(settings.CACHE_DIR):
        os.mkdir(settings.CACHE_DIR)

    if not os.path.exists(settings.MESSAGE_CACHE_DIR):
        os.mkdir(settings.MESSAGE_CACHE_DIR)

    if not os.path.exists(settings.MODULES_DIR):
        os.mkdir(settings.MODULES_DIR)

    if not os.path.exists(settings.MESSAGE_CURSOR_FILE):
        with open(settings.MESSAGE_CURSOR_FILE, 'w') as f:
            f.write(
                escape.json_encode({
                    'cursor': 0,
                    'updated_time': time.time()
                }))


def get_first_report():
    platform = pf.get_platform()
    ips = [{
        'ip': info.ipv4,
        'netmask': info.netmask
    } for info in sysutil.network_info()]
    physical_memory = round(sysutil.physical_memory() / (1024 * 1024 * 1024.0),
                            2)
    disk = round(
        sum(part.total
            for part in sysutil.disk_info()) / (1024 * 1024 * 1024.0), 2)

    first_report = {
        'id': config['id'],
        'network_domain': config['network_domain'],
        'version': config['version'],
        'system': platform.system,
        'dist': platform.dist,
        'dist_version': platform.version,
        'arch': platform.cpu,
        'kernel': platform.kernel,
        'ips': ips,
        'ip': config.get('ip') or sysutil.main_ip(),
        'home': settings.ROOT_DIR,
        'is_virtual': sysutil.is_virtual_machine(),
        'node_type': sysutil.node_type(),
        'physical_cpu_count': sysutil.physical_cpu_count(),
        'physical_memory': physical_memory,
        'disk': disk
    }
    logger.debug(first_report)
    return first_report


def init_first_report():
    report = get_first_report()
    message = Message.create(MessageType.INFO, body=report)
    transfer = get_current_transfer()
    result = get_io_loop().run_sync(lambda: transfer.send(message))
    if not result:
        logger.error('Upload report failed')
        sys.exit(-1)


def init_schedulers():
    schedulers = [
        HeartbeatScheduler, MessageFetchScheduler, MessageCacheScheduler,
        TaskSyncScheduler, StatusScheduler
    ]
    logger.info('Initializing schedulers')
    for scheduler_cls in schedulers:
        scheduler = scheduler_cls()
        logger.info('Add %s to ioloop', scheduler_cls.__name__)
        get_io_loop().spawn_callback(scheduler.eventloop)


def init_reactor():
    handlers = [(MessageType.NEW_TASK, NewTaskHandler),
                (MessageType.KILL_TASK, KillTaskHandler)]
    for msg_type, handler_cls in handlers:
        msg_handler = handler_cls()
        logger.info('Add %s to reactor', handler_cls.__name__)
        reactor.add_handler(msg_type, msg_handler)


def init_message_cache():
    logger.info('Initializing message cache: %s', config['message_cache_file'])
    try:
        cache.database.create_table(cache.Message)
    except Exception as exc:
        logger.info('Message cache: %s', exc)  # Table already exists


def init_application():
    app = web.Application([(r'/task/log', TaskLogHandler),
                           (r'/task/result', TaskResultHandler),
                           (r'/file', FileHandler)])
    logger.info('Framework listen on port %d', config['framework_port'])
    app.listen(
        config['framework_port'],
        address='127.0.0.1',
        max_body_size=config.get('framework_max_body_size', 1024 * 1024))


def init_io_loop():
    logger.info('Startup io loop')
    get_io_loop().start()


def startup():
    global logger

    os.umask(0027)
    init_files()
    logging.config.dictConfig(settings.LOGGING)
    logger = logging.getLogger('default')

    init_bootstrap()
    config.reload()
    init_first_report()
    init_reactor()
    init_schedulers()
    init_message_cache()
    init_application()
    init_io_loop()


if __name__ == '__main__':
    startup()
