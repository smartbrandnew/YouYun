import sys
import os
from subprocess import check_call, CalledProcessError

import nfs as fs
import requests

from build import logger
from build.constants import (IS_WINDOWS, PROJECT_ROOT, EXCLUDE_DIRS,
                             EXCLUDE_FILES)


class Project(object):
    def __init__(self, name, url):
        self.name = name
        self.url = url
        self.path = fs.join(PROJECT_ROOT, name)

    def exists(self):
        return fs.exists(self.path)

    def remove(self):
        fs.remove(self.path)


class GitProject(Project):
    def __init__(self, name, url, is_plugin=False):
        self.is_plugin = is_plugin
        super(GitProject, self).__init__(name, url)

    def check(self, dst=PROJECT_ROOT, branch='develop'):
        logger.info('----------------------'*3)
        logger.info('Check project: {!r}'.format(self.name))
        if self.exists():
            self.remove()
        self.clone(dst)
        self.checkout_branch(dst, branch=branch)

    def pull(self, dst=PROJECT_ROOT):
        fs.chdir(fs.join(dst, self.name))
        check_call(['git', 'pull'])

    def clone(self, dst=PROJECT_ROOT):
        fs.makedirs(dst)
        fs.chdir(dst)
        check_call(['git', 'clone', self.url, self.name])
        fs.chdir('../')

    def checkout_branch(self, dst=PROJECT_ROOT, branch='develop'):
        fs.chdir(fs.join(dst, self.name))
        try:
            check_call(['git', 'checkout', branch])
        except CalledProcessError as e:
            if e.returncode == 1:
                check_call(['git', 'checkout', '-b',
                            'develop', 'origin/develop'])
        fs.chdir('../../')

    def copy_to(self, dst, plugin_parent='plugins'):
        if self.is_plugin:
            plugin_yml_path = fs.join(self.path, 'module.yml')
            if not fs.exists(plugin_yml_path):
                plugin_yml_path = fs.join(self.path, 'plugin.yml')

            if fs.exists(plugin_yml_path):
                import yaml
                info = yaml.load(open(plugin_yml_path))
                fullname = '{}@{}'.format(info['name'], info['version'])
                dst = fs.join(dst, plugin_parent, fullname)
                fs.makedirs(dst)
            else:
                logger.error('module.yml or plugin.yml not exists')
                sys.exit(1)

        logger.info('Copy project: {!r} from {!r} to {!r}'
                    .format(self.name, self.path, dst))

        for dirname in fs.listdir(self.path):
            dirpath = fs.join(self.path, dirname)
            if dirname in (EXCLUDE_DIRS + EXCLUDE_FILES) \
                    or dirname.startswith('.'):
                continue
            fs.copy(dirpath, dst,
                    exclude_dirs=EXCLUDE_DIRS,
                    exclude_files=['*.exe', '*.bat']
                    if not IS_WINDOWS else ['*.sh'])
        return dst


class HttpProject(Project):
    def download(self, dst=PROJECT_ROOT):
        logger.info('Download from {!r} to {!r}'.format(self.url, dst))
        filename = self.url.split('/')[-1]
        dst = fs.join(dst, filename)

        req = requests.get(self.url, stream=True)
        with open(dst, 'wb') as f:
            for chunk in req.iter_content(chunk_size=1024):
                if chunk:
                    f.write(chunk)
        return dst

    def check(self, dst=PROJECT_ROOT):
        logger.info('----------------------'*3)
        logger.info('Check project: {!r}'.format(self.name))
        if not fs.exists(dst):
            fs.makedirs(dst)
            fs.chdir(dst)

        if self.exists():
            self.remove()
        compress_path = self.download(dst)
        logger.info('Uncompress from {!r} to {!r}'
                    .format(compress_path, dst))
        fs.uncompress(compress_path,
                      dst=PROJECT_ROOT,
                      temp_dir=PROJECT_ROOT)

