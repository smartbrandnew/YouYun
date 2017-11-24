# coding: utf-8
"""
Usage:
  bootstrap --tenant=<tenant-id>
            [--network-domain=<network-domain>]
            [--upstream=<upstream>]
            [--ip=<ip>]
            [--user=<user>]

Options:
  -h, --help             Show help.
  --tenant <tenant>      Tenant id
  --network-domain <network-domain> The id of agent's network resource id!
  --upstream <upstream>  Agent's upstream. It is usually its higher level
                         ant agent or ant server's ip.
  --ip <ip>              Agent's ip for server
  --user <user>          The runner for agent

"""
import sys
import platform
import yaml
import os
import urlparse
from docopt import docopt
from subprocess import check_call, Popen, PIPE, STDOUT

import nfs
from circle.winservice import CircleWinService, UpgradeWinService
from framework.actions.utils import get_agent_ip, upstream_validate, ip_validate
from framework.actions.constants import (
    IS_WINDOWS, BIN_START, CONF_PATH, PYTHON, UPGRADE_PYTHON, CIRCLED_PATH,
    CIRCLECTL_PATH, CIRCLE_CONF_DIR, CIRCLE_LOG_PATH, SERVICE_NAME,
    SERVICE_FILE, SERVICE_TEMPLATE, SERVICE_PATH, BOOT_SCRIPT, DESCRIPTION,
    UPSTREAM_SUFFIX, ROOT_DIR, NGINX_CONF, UPGRADE_SERVICE_NAME,
    UPGRADE_SERVICE_TEMPLATE, UPGRADE_SERVICE_FILE, UPGRADE_BIN_START,
    NGINX_PORT, PYTHON_DIR, UPGRADE_PYTHON_DIR)
from framework.actions.errors import RegisterServiceError, MessageError
from framework.actions import logger

PLATFORM = platform.platform().lower()
os.umask(0027)


def execute(cmd):
    proc = Popen(cmd, stdout=PIPE, stderr=STDOUT, shell=True)
    result, _ = proc.communicate()
    return proc.returncode, result


def init_bootstrap():
    bootstrap_file = nfs.join(ROOT_DIR, 'templates', 'bootstrap.py')
    nfs.copy(bootstrap_file, ROOT_DIR)


def init_conf(conf_dict):
    logger.info('Preparing ant agent ...')
    with open(CONF_PATH, 'a+') as f:
        yaml.dump(conf_dict, f, default_flow_style=False)
    logger.info('Prepare done')


def init_openresty(baseurl, upstream, runner):
    if not nfs.exists(NGINX_CONF):
        return

    with open(NGINX_CONF) as temp:
        content = temp.read()
        content = content.replace('UPSTREAM', upstream)
        content = content.replace('BASEURL', baseurl)
        if not IS_WINDOWS and runner:
            content = content.replace('RUNNER', runner)

        if IS_WINDOWS:
            BASEDIR = os.path.normpath(ROOT_DIR).replace('\\', '\\\\')
        else:
            BASEDIR = ROOT_DIR
        content = content.replace('BASEDIR', BASEDIR)

    with open(NGINX_CONF, 'w') as conf:
        conf.write(content)

    log_dir = nfs.join(ROOT_DIR, 'openresty', 'logs')
    if not nfs.exists(log_dir):
        nfs.makedirs(log_dir, 0750)


def register_service(su_cmd):
    if not IS_WINDOWS:
        logger.info('Register service')
        if 'ubuntu' in PLATFORM or 'debian' in PLATFORM:
            format_template(DESCRIPTION['like_debian'], su_cmd)
            register_status, _ = execute('update-rc.d {} defaults'
                                         .format(SERVICE_NAME))
            if register_status != 0:
                raise RegisterServiceError('Register {} Service Error'
                                           .format(SERVICE_NAME))
        elif any(
                _p in PLATFORM
                for _p in ('centos', 'fedora', 'suse', 'redhat')):
            format_template(DESCRIPTION['like_redhat'], su_cmd)
            chk_status, _ = execute('chkconfig {} on'.format(SERVICE_NAME))
            service_path = nfs.join(SERVICE_PATH, SERVICE_NAME)
            echo_status, _ = execute('echo "{service_path} start" '
                                     '| tee --append {path} >/dev/null'.format(
                                         service_path=service_path,
                                         path=BOOT_SCRIPT))
            chmod_status, _ = execute('chmod +x {}'.format(BOOT_SCRIPT))
            if chk_status != 0 or echo_status != 0 or chmod_status != 0:
                raise RegisterServiceError('Register {} Service Error'
                                           .format(SERVICE_NAME))
        else:
            logger.warn('Unsupported platform')


def format_template(desc, su_cmd):
    desc = desc.format(service_name=SERVICE_NAME)
    with open(SERVICE_TEMPLATE) as temp:
        content = temp.read().format(
            description=desc,
            service_name=SERVICE_NAME,
            python=PYTHON,
            circled=CIRCLED_PATH,
            circlectl=CIRCLECTL_PATH,
            circle_conf=CIRCLE_CONF_DIR,
            circle_log=CIRCLE_LOG_PATH,
            ant_su_cmd=su_cmd)

    with open(SERVICE_FILE, 'w') as conf:
        conf.write(content)
    chmod_status, _ = execute('chmod 750 {}'.format(SERVICE_FILE))
    cp_status, _ = execute('cp {} {}'.format(SERVICE_FILE, SERVICE_PATH))
    if chmod_status != 0 or cp_status != 0:
        raise RegisterServiceError('Register {} Service Error'
                                   .format(SERVICE_NAME))


def register_upgrade_service(su_cmd):
    if not IS_WINDOWS:
        logger.info('Register upgrade service')

        if 'ubuntu' in PLATFORM or 'debian' in PLATFORM:
            format_upgrade_template(DESCRIPTION['like_debian'], su_cmd)
            register_status, _ = execute('update-rc.d {} defaults'
                                         .format(UPGRADE_SERVICE_NAME))
            if register_status != 0:
                raise RegisterServiceError('Register {} Service Error'
                                           .format(UPGRADE_SERVICE_NAME))
        elif any(
                _p in PLATFORM
                for _p in ('centos', 'fedora', 'suse', 'redhat')):
            format_upgrade_template(DESCRIPTION['like_redhat'], su_cmd)
            chk_status, _ = execute('chkconfig {} on'
                                    .format(UPGRADE_SERVICE_NAME))
            service_path = nfs.join(SERVICE_PATH, UPGRADE_SERVICE_NAME)
            echo_status, _ = execute('echo "{service_path} start" '
                                     '| tee --append {path} >/dev/null'.format(
                                         service_path=service_path,
                                         path=BOOT_SCRIPT))
            chmod_status, _ = execute('chmod +x {}'.format(BOOT_SCRIPT))
            if chk_status != 0 or echo_status != 0 or chmod_status != 0:
                raise RegisterServiceError('Register {} Service Error'
                                           .format(UPGRADE_SERVICE_NAME))
        else:
            logger.warn('Unsupported platform')

        status, output = execute('{} "{}"'.format(su_cmd, UPGRADE_BIN_START))
        if status != 0:
            raise MessageError('Start Upgrade failed: reason: {}'.format(
                output))
        logger.info('Start Upgrade done')
    else:
        UpgradeWinService.install()
        UpgradeWinService.start()


def format_upgrade_template(desc, su_cmd):
    desc = desc.format(service_name=UPGRADE_SERVICE_NAME)
    with open(UPGRADE_SERVICE_TEMPLATE) as temp:
        content = temp.read().format(
            description=desc,
            service_name=UPGRADE_SERVICE_NAME,
            python=UPGRADE_PYTHON,
            ant_dir=ROOT_DIR,
            ant_su_cmd=su_cmd)

    with open(UPGRADE_SERVICE_FILE, 'w') as conf:
        conf.write(content)
    chmod_status, _ = execute('chmod 750 {}'.format(UPGRADE_SERVICE_FILE))
    cp_status, _ = execute('cp {} {}'.format(UPGRADE_SERVICE_FILE,
                                             SERVICE_PATH))
    if chmod_status != 0 or cp_status != 0:
        raise RegisterServiceError('Register {} Service Error'
                                   .format(UPGRADE_SERVICE_NAME))


def start_circled(su_cmd):
    logger.info('Starting Agent ...')
    if IS_WINDOWS:
        check_call([BIN_START], shell=True)
    else:
        status, _ = execute('{} "{}"'.format(su_cmd, BIN_START))
        if status != 0:
            raise MessageError('Start Agent failed')
    logger.info('Start Agent done')


def check_win_agent():
    if CircleWinService.exists():
        answer = raw_input('Ant agent has installed as windows service '
                           '("Agent") before. \n'
                           'Do you want to remove it?(yes/no)')
        if answer == 'yes':
            if CircleWinService.status() != 'stopped':
                CircleWinService.stop()
                CircleWinService.remove()
        else:
            logger.info('exit')
            return False

    if UpgradeWinService.exists():
        if UpgradeWinService.status() != 'stopped':
            UpgradeWinService.stop()
        UpgradeWinService.remove()
    return True


def handle_cli():
    try:
        cli_args = docopt(__doc__)
        if IS_WINDOWS and not check_win_agent():
            return

        if not nfs.exists(UPGRADE_PYTHON_DIR):
            nfs.copy(nfs.join(PYTHON_DIR, '*'), UPGRADE_PYTHON_DIR)

        # Get upstream message
        if cli_args['--upstream']:
            baseurl = cli_args['--upstream']
            upstream = urlparse.urljoin(cli_args['--upstream'], UPSTREAM_SUFFIX)
            upstream_mes = upstream_validate(cli_args['--upstream'])
            if not upstream_mes:
                logger.error('The upstream: {} is wrong!'
                             ''.format(cli_args['--upstream']))
                return
        else:
            upstream_ip = os.environ['SSH_CLIENT'].split()[0]
            baseurl = 'http://{}:{}/'.format(upstream_ip, NGINX_PORT)
            upstream = '{}{}'.format(baseurl, UPSTREAM_SUFFIX)
            upstream_mes = [upstream_ip, NGINX_PORT]

        if cli_args['--ip']:
            if not ip_validate(cli_args['--ip']):
                raise ValueError('Ant agent ip: {} is invalid'
                                 ''.format(cli_args['--ip']))
            else:
                agent_ip = cli_args['--ip']
        else:
            agent_ip = get_agent_ip(upstream_mes[0], int(upstream_mes[1]))

        runner = cli_args['--user'] if cli_args['--user'] else os.environ.get(
            'USER')
        su_cmd = '' if runner == 'root' else 'su - {} -c '.format(runner)

        conf_dict = {
            'tenant': cli_args['--tenant'],
            'ip': agent_ip,
            'upstream': upstream,
            'network_domain': cli_args['--network-domain']
        }
        init_conf(conf_dict)
        init_bootstrap()
        init_openresty(baseurl, upstream, runner)
        register_service(su_cmd)

        if runner and not IS_WINDOWS and runner != 'root':
            status, result = execute('chown -R {user}:{user} {path}'.format(
                user=runner, path=ROOT_DIR))
            if status != 0:
                raise MessageError('Change log path owen failed! Error[{}]: {}'
                                   .format(status, result))

        register_upgrade_service(su_cmd)
        start_circled(su_cmd)
    except Exception as e:
        logger.error(e)
        sys.exit(1)


if __name__ == '__main__':
    handle_cli()
