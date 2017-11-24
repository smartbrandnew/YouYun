# coding: utf-8
import time
import yaml
import sys
import nfs
from subprocess import Popen, PIPE, STDOUT
from constants import logger
from constants import (AGENT_BACK_DIR, ROOT_DIR, TIME_TICK, SYSTEM_SCRIPT_TYPE,
                       STOP_AGENT_TIMEOUT, START_AGENT_TIMEOUT, CONFIG_PATH,
                       CONFIG_NAME, IS_WINDOWS)
from utils import MessageError, copy_files


class DoUpgrade(object):

    def __init__(self, new_agent_dir, http_handler):
        self.new_agent_dir = new_agent_dir
        self.http_handler = http_handler
        self.bin_start = nfs.join(ROOT_DIR, 'bin', 'start' + SYSTEM_SCRIPT_TYPE)
        self.bin_stop = nfs.join(ROOT_DIR, 'bin', 'stop' + SYSTEM_SCRIPT_TYPE)
        self.copy = copy_files if IS_WINDOWS else nfs.copy

    def do_upgrade(self):
        try:
            try:
                self.http_handler.log_ok('Start upgrader')
                self.copy_basic_conf()
                self.stop_agent()
                self.deploy_new_agent()
                self.start_agent()
                if not self.run_hooks():
                    self.roll_back()
                    self.http_handler.log_error(
                        'Upgrade failed. Roll back successfully', done=True)
                else:
                    nfs.remove(AGENT_BACK_DIR)
                    self.http_handler.log_ok('Upgrade successfully', done=True)
            except Exception as e:
                self.http_handler.log_error(str(e))
                logger.error(str(e), exc_info=True)
                self.roll_back()
                self.http_handler.log_error(
                    'Upgrade failed. Roll back successfully', done=True)
        except Exception:
            self.start_agent()
            self.http_handler.log_error(
                'Upgrade failed. Roll back successfully', done=True)

    def stop_agent(self):
        self.http_handler.log_ok('Stopping agent')
        if IS_WINDOWS:
            from circle.winservice import CircleWinService
            if CircleWinService.status() != 'stopped':
                CircleWinService.stop()
        else:
            p = Popen(
                [self.bin_stop],
                stdout=PIPE,
                stderr=STDOUT,
                cwd=ROOT_DIR,
                shell=True)
            last_time = time.time()
            while p.poll() is None:
                time.sleep(TIME_TICK)
                self.http_handler.log_ok('Stopping agent')
                if time.time() - last_time > STOP_AGENT_TIMEOUT:
                    err_msg = 'Stop agent time out'
                    raise MessageError(err_msg)

            poll = p.poll()
            if p.poll() != 0:
                out = p.stdout.read()
                if 'circled' in out and 'closed' in out:
                    self.http_handler.log_ok(
                        'circled not running, no need to stop')
                    return
                elif out.strip() != 'ok':
                    pre = 'Start agent timeout' if poll is None \
                        else 'Start agent error'
                    msg = '{}. Exit code: {}\n{}'.format(pre, poll, out)
                    raise MessageError(msg)
        self.http_handler.log_ok('Stop agent done')

    def copy_basic_conf(self):
        self.http_handler.log_ok('Coping basic conf')
        try:
            new_conf_path = nfs.join(self.new_agent_dir, CONFIG_NAME)
            with open(CONFIG_PATH) as temp:
                yaml.dump(
                    yaml.load(temp.read()),
                    open(new_conf_path, 'a+'),
                    default_flow_style=False)

            self.http_handler.log_ok('Copy conf done')
        except IOError as e:
            self.http_handler.log_error(str(e))
            logger.error(str(e), exc_info=True)
            self.http_handler.log_ok('Skip copy basic conf')

    def deploy_new_agent(self):
        self.http_handler.log_ok('Deploying agent')

        dst = self.new_agent_dir if IS_WINDOWS \
            else nfs.join(self.new_agent_dir, '*')
        self.copy(dst, ROOT_DIR)
        nfs.remove(self.new_agent_dir)
        self.http_handler.log_ok('Deploy done')

    @staticmethod
    def start_win():
        from circle.winservice import CircleWinService
        if CircleWinService.status() != 'stopped':
            CircleWinService.stop()
        CircleWinService.start()

    def start_linux(self):
        p = Popen(
            [self.bin_start],
            stdout=PIPE,
            stderr=STDOUT,
            cwd=ROOT_DIR,
            shell=True)
        last_time = time.time()
        while p.poll() is None:
            time.sleep(TIME_TICK)
            self.http_handler.log_ok('Starting agent...')
            if time.time() - last_time > START_AGENT_TIMEOUT:
                break
        poll = p.poll()
        if poll != 0:
            pre = 'Start agent timeout' if poll is None \
                else 'Start agent error'
            out = p.stdout.read()
            msg = '{}. Exit code: {}\n{}'.format(pre, poll, out)
            raise MessageError(msg)

    def start_agent(self):
        self.http_handler.log_ok('Starting agent...')
        if IS_WINDOWS:
            self.start_win()
        else:
            self.start_linux()
        self.http_handler.log_ok('Start agent done')

    def roll_back(self):
        self.http_handler.log_ok('Rolling back...')
        try:
            self.stop_agent()
            self._roll_back_files()
            self.start_agent()
        except Exception as e:
            self.http_handler.log_error(str(e))
            logger.error(str(e), exc_info=True)
            self._roll_back_files()

        self.http_handler.log_ok('Roll back done', done=True)

    def _roll_back_files(self):
        self.http_handler.log_ok('Rolling back files...')
        if nfs.exists(AGENT_BACK_DIR):
            self.copy(AGENT_BACK_DIR, ROOT_DIR)
        self.http_handler.log_ok('Roll back files done')

    def run_hooks(self):
        hooks_dir = nfs.join(self.new_agent_dir, 'hooks')
        if not nfs.exists(hooks_dir):
            return True

        for dir_name in nfs.listdir(hooks_dir):
            hooks_file = nfs.join(hooks_dir, dir_name)
            if nfs.isfile(hooks_file) and hooks_file.endswith('.py'):
                p = Popen(
                    [sys.executable, hooks_file],
                    stdout=PIPE,
                    stderr=STDOUT,
                    cwd=ROOT_DIR,
                    shell=True)

                while p.poll() is None:
                    time.sleep(1)
                    if p.stdout:
                        logger.info(p.stdout.read())

                if p.poll() != 0:
                    self.http_handler.log_ok('Run hooks {} failed!'
                                             ''.format(hooks_file))
                return False
        return True
