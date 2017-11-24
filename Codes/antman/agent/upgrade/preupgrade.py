# coding: utf-8
import os
import nfs
import semver
from constants import (AGENT_BACK_DIR, AGENT_DOWNLOAD_DIR, AGENT_UNCOMPRESS_DIR,
                       AGENT_UNCOMPRESS_DIRNAME, ROOT_DIR, logger,
                       SYSTEM_SCRIPT_TYPE, EXCLUDE_BACK_DIRS)
from httphandler import HttpHandler
from utils import MessageError, get_agent_version


class PreUpgrade(object):

    def __init__(self, task_message):
        self.task_message = task_message
        self.bin_start = nfs.join(ROOT_DIR, 'bin', 'start' + SYSTEM_SCRIPT_TYPE)
        self.bin_stop = nfs.join(ROOT_DIR, 'bin', 'stop' + SYSTEM_SCRIPT_TYPE)

    def validate(self):
        if not isinstance(self.task_message, dict):
            logger.error('Info should be dict')
            return False
        for key in ('version', 'task_id', 'filename'):
            if key not in self.task_message:
                logger.error('{!r} is missing in info'.format(key))
                return False
        return True

    def _init_http_handler(self):
        try:
            self.http_handler = HttpHandler(self.task_message['task_id'])
            self.http_handler.initialization()
        except Exception as e:
            logger.error(str(e), exc_info=True)
            return False
        return True

    def execute(self):
        try:
            if not self.validate():
                return
            if not self._init_http_handler():
                return

            nfs.makedirs(AGENT_DOWNLOAD_DIR)
            self.check_version(self.task_message['version'])
            compress_agent_path = self.download()
            uncompress_agent_path = self.umcompress(compress_agent_path)
            # Backup files
            self.backup_files()
            # Run upgrader
            from doupgrade import DoUpgrade
            do_upgrade = DoUpgrade(uncompress_agent_path, self.http_handler)
            do_upgrade.do_upgrade()
        except Exception as e:
            self.http_handler.log_error(str(e), done=True)
            logger.error(str(e), exc_info=True)

    def check_version(self, upgrade_version):
        self.http_handler.log_ok('Checking version')
        current_version = get_agent_version()
        if semver.Version(upgrade_version) == semver.Version(current_version):
            raise MessageError('Upgrade version: {} == agent current version: '
                               '{}, will not upgrade'
                               .format(upgrade_version, current_version))
        self.http_handler.log_ok('Version valid')

    def download(self):
        compress_name = self.task_message['filename']
        try:
            self.http_handler.log_ok('Downloading package: {!r}'
                                     ''.format(compress_name))
            filepath = self.http_handler.download(compress_name,
                                                  AGENT_DOWNLOAD_DIR)
            self.http_handler.log_ok('Download package: {!r} done'
                                     ''.format(compress_name))
            return filepath
        except Exception as e:
            raise MessageError('Download package: {!r} failed. {}'
                               ''.format(compress_name, str(e)))

    def umcompress(self, compress_agent_path):
        self.http_handler.log_ok('Removing {!r}...'
                                 ''.format(AGENT_UNCOMPRESS_DIRNAME))
        if nfs.exists(AGENT_UNCOMPRESS_DIR):
            nfs.remove(AGENT_UNCOMPRESS_DIR)
        self.http_handler.log_ok('Remove {!r} done'
                                 ''.format(AGENT_UNCOMPRESS_DIRNAME))
        self.http_handler.log_ok('Uncompressing {}...'
                                 ''.format(self.task_message['filename']))
        os.makedirs(AGENT_UNCOMPRESS_DIR)
        uncompress_agent_path = nfs.uncompress(
            compress_agent_path,
            AGENT_UNCOMPRESS_DIR,
            temp_dir=AGENT_UNCOMPRESS_DIR)
        self.http_handler.log_ok('Uncompress done')
        return uncompress_agent_path

    def backup_files(self):
        if nfs.exists(AGENT_BACK_DIR):
            nfs.remove(nfs.join(AGENT_BACK_DIR, '*'))
        else:
            nfs.makedirs(AGENT_BACK_DIR)
        # Copy
        self.http_handler.log_ok('Backup files')
        for dir_name in nfs.listdir(ROOT_DIR):
            if dir_name in EXCLUDE_BACK_DIRS:
                continue
            nfs.copy(nfs.join(ROOT_DIR, dir_name), AGENT_BACK_DIR)
        self.http_handler.log_ok('Backup done')
