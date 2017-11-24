# coding: utf-8
import os
import time
from subprocess import Popen, PIPE, STDOUT

from circle import logger
from circle.constants import (NSSM_PATH, CIRCLED_PATH, ROOT_DIR, BIN_START,
                              UPGRADE_START)
from circle.errors import CallError, AlreadyExist, NotExistError, TimeOutError
from circle.util import get_status_output


def _get_clear_output(output):
    return output.replace('\x00', '').replace('\r', '')


def _nssm_run(option, service_name, *args):
    args = [NSSM_PATH, option, service_name] + list(args)
    logger.debug('NSSM args: {}'.format(args))
    p = Popen(args, stdout=PIPE, stderr=STDOUT)
    p.wait()
    returncode = p.returncode
    output = _get_clear_output(p.stdout.read())
    return returncode, output


def _sc_run(option, service_name):
    return get_status_output(['sc', option, service_name])


def _wait_util(service, status, timeout=5):
    last_time = time.time()
    while True:
        if service.status() == status:
            break
        if time.time() - last_time > timeout:
            raise TimeOutError('Timed out.')
        time.sleep(0.5)


class CircleWinService(object):
    name = 'ant-agent'
    exist_msg = 'Service: {} has already installed.'.format(name)
    not_exist_msg = "Service: {} hasn't been installed.".format(name)
    remove_err_msg = 'Error removing service: {}. ' \
                     '(Usually caused by running status)'.format(name)
    access_err_msg = 'Access denied. Please make sure run as administrator authority.'
    start_err_msg = 'Start error. Please check configuration ' \
                    'of service: {}.'.format(name)

    @classmethod
    def exists(cls):
        try:
            cls.status()
            return True
        except NotExistError:
            return False

    @classmethod
    def start(cls, *args):
        logger.info('Starting ant-agent service')

        # CircleWinService.config(*args)
        returncode, output = _sc_run('start', cls.name)
        if returncode != 0:
            raise CallError(output)
        _wait_util(cls, 'running')

        logger.info('ant-agent started')

    @classmethod
    def stop(cls):
        logger.info('Stopping ant-agent service')

        returncode, output = _sc_run('stop', cls.name)
        if returncode != 0:
            raise CallError(output)
        _wait_util(cls, 'stopped')

        logger.info('ant-agent stopped')

    @classmethod
    def status(cls):
        returncode, output = _nssm_run('status', cls.name)
        if returncode == 0:
            if 'SERVICE_STOPPED' in output:
                return 'stopped'
            elif 'SERVICE_RUNNING' in output:
                return 'running'
            elif 'SERVICE_PAUSED' in output:
                return 'paused'
            elif 'START_PENDING' in output:
                return 'starting'
            elif 'STOP_PENDING' in output:
                return 'stopping'
        elif returncode == 3:
            raise NotExistError(cls.not_exist_msg)
        else:
            raise CallError(output)

    @classmethod
    def install(cls, *args):
        logger.info('Installing ant-agent service')

        if not os.path.exists(CIRCLED_PATH):
            raise CallError('{} not exists.'.format(CIRCLED_PATH))

        returncode, output = _nssm_run('install', cls.name, BIN_START)
        if returncode == 0 and 'Administrator access' in output:
            raise CallError(output)
        elif returncode == 5:
            raise AlreadyExist(cls.exist_msg)
        elif returncode != 0:
            raise CallError(output)

        logger.info('ant-agent installed')
        # CircleWinService.config(*args)

    @classmethod
    def remove(cls):
        """If you want to remove the service, please make sure stop it first.
        Otherwise you will get error message: "Error deleting". """
        logger.info('Removing ant-agent service')

        returncode, output = _nssm_run('remove', cls.name, 'confirm')
        if returncode == 3:
            raise NotExistError(cls.not_exist_msg)
        elif returncode == 4:
            raise CallError(cls.remove_err_msg)
        elif returncode != 0:
            raise CallError(output)
        logger.info('ant-agent removed')

    @classmethod
    def config(cls, *args):
        logger.debug('Configuring ant-agent service parameters')

        returncode, output = _nssm_run('set', cls.name, 'AppDirectory',
                                       ROOT_DIR)
        if returncode != 0:
            if '\x07c\x9a' in output:
                raise CallError(cls.not_exist_msg)
            elif '\xd2b\xdd' in output:
                raise CallError(cls.access_err_msg)
            raise CallError(output)

        returncode, output = _nssm_run('set', cls.name, 'AppParameters',
                                       *([CIRCLED_PATH] + list(args)))
        if returncode != 0:
            raise CallError(output)
        logger.debug('ant-agent service parameters configured')


class UpgradeWinService(object):
    name = 'ant-upgrade'
    exist_msg = 'Service: {} has already installed.'.format(name)
    not_exist_msg = "Service: {} hasn't been installed.".format(name)
    remove_err_msg = 'Error removing service: {}. ' \
                     '(Usually caused by running status)'.format(name)
    access_err_msg = 'Access denied. Please make sure run as administrator authority.'
    start_err_msg = 'Start error. Please check configuration ' \
                    'of service: {}.'.format(name)

    @classmethod
    def exists(cls):
        try:
            cls.status()
            return True
        except NotExistError:
            return False

    @classmethod
    def start(cls, *args):
        logger.info('Starting {} service'.format(cls.name))

        # CircleWinService.config(*args)
        returncode, output = _sc_run('start', cls.name)
        if returncode != 0:
            raise CallError(output)
        _wait_util(cls, 'running')

        logger.info('{} started'.format(cls.name))

    @classmethod
    def stop(cls):
        logger.info('Stopping ant-upgrade service')

        returncode, output = _sc_run('stop', cls.name)
        if returncode != 0:
            raise CallError(output)
        _wait_util(cls, 'stopped')

        logger.info('ant-upgrade stopped')

    @classmethod
    def status(cls):
        returncode, output = _nssm_run('status', cls.name)
        if returncode == 0:
            if 'SERVICE_STOPPED' in output:
                return 'stopped'
            elif 'SERVICE_RUNNING' in output:
                return 'running'
            elif 'SERVICE_PAUSED' in output:
                return 'paused'
            elif 'START_PENDING' in output:
                return 'starting'
            elif 'STOP_PENDING' in output:
                return 'stopping'
        elif returncode == 3:
            raise NotExistError(cls.not_exist_msg)
        else:
            raise CallError(output)

    @classmethod
    def install(cls, *args):
        logger.info('Installing ant-upgrade service')

        returncode, output = _nssm_run('install', cls.name, UPGRADE_START)
        if returncode == 0 and 'Administrator access' in output:
            raise CallError(output)
        elif returncode == 5:
            raise AlreadyExist(cls.exist_msg)
        elif returncode != 0:
            raise CallError(output)

        logger.info('ant-upgrade installed')

    @classmethod
    def remove(cls):
        """If you want to remove the service, please make sure stop it first.
        Otherwise you will get error message: "Error deleting". """
        logger.info('Removing ant-upgrade service')

        returncode, output = _nssm_run('remove', cls.name, 'confirm')
        if returncode == 3:
            raise NotExistError(cls.not_exist_msg)
        elif returncode == 4:
            raise CallError(cls.remove_err_msg)
        elif returncode != 0:
            raise CallError(output)
        logger.info('ant-upgrade removed')
