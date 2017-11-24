import os
import sys
import re
import yaml
import requests
import pf
import nfs as fs

from contextlib import contextmanager
from subprocess import PIPE, STDOUT, Popen
from ci import logger
from ci.constants import CONFIG_PATH
from ci.constants import PYTHON_TEMPLATE_URL, POSTFIX, SSHHOST, SSHUSER, WINDOWS
from ci.exceptions import ExecuteError


_config = None
platform = pf.get_platform()


def get_version(yaml_file):
    with open(yaml_file, 'r') as f:
        content = yaml.load(f)
    return content['version']


def execute(cmd, shell=True):
    logger.debug('Executing cmd: {}'.format(cmd))
    try:
        p = Popen(cmd, shell=shell, stdout=PIPE, stderr=STDOUT)
        stdout, _ = p.communicate()
    except Exception as e:
        raise ExecuteError(cmd, 1, str(e))

    if stdout:
        logger.info(stdout)
    if p.poll() != 0:
        raise ExecuteError(cmd, p.poll(), stdout)
    return stdout


def copy(source, target, exclude_files, exclude_dirs):
    fs.copy(
        source,
        target,
        exclude_files=exclude_files,
        exclude_dirs=exclude_dirs,
        symlinks=True
    )


def get_config():
    global _config
    if _config:
        return _config

    with open(CONFIG_PATH) as f:
        _config = yaml.load(f)
        if not isinstance(_config, dict):
            logger.info('config.yml should be dict')
            sys.exit(1)

        if {'gitlab', 'dingding'} - _config.viewkeys():
            logger.info(
                'config.yml must contains "gitlab", "dingding"')
            sys.exit(1)
        return _config


def get_chinese_name(username):
    chinese_names = get_config().get('chinese_names')
    if not chinese_names or not isinstance(chinese_names, dict):
        return username

    return chinese_names.get(username, username)


def get_branch_by_ref(ref):
    result = re.findall('refs/heads/([\s\S]+)', ref)
    if result:
        return result[0]
    return ref


def scp_upload_file(source_dir, target_dir):
    cmd = 'scp -r {} {}@{}:{}'.format(source_dir,
                                      SSHUSER,
                                      SSHHOST,
                                      target_dir)
    execute(cmd)


def excutor_cmd(cmd):
    cmd = 'ssh {}@{} \'{}\''.format(SSHUSER, SSHHOST, cmd)
    execute(cmd)


def download(url, target_path):
    logger.info('Downloading from {}'.format(url))
    r = requests.get(url)
    if r.status_code != 200:
        logger.error('Project build faild')
        logger.error('{} do not exists'.format(url))
        sys.exit()
    with open(target_path, 'wb') as code:
        code.write(r.content)
    logger.info('Download done')


def maybe_download_python():
    system = platform.system.lower()
    cwd = os.getcwd()

    if WINDOWS:
        python = fs.join(cwd, 'embedded/python.exe')
        pip = fs.join(cwd, 'embedded/Scripts/pip.exe')
    else:
        python = fs.join(cwd, 'embedded/bin/python')
        pip = fs.join(cwd, 'embedded/bin/pip')

    if not fs.exists(fs.join(cwd, 'embedded')):
        python_name = 'python-{}-{}.{}'.format(system, platform.cpu,
                                               POSTFIX.get(system, 'tgz'))
        python_url = PYTHON_TEMPLATE_URL.format(python_name)
        download(python_url, python_name)
        fs.uncompress(python_name)
        fs.rename('python-{}-{}'.format(system, platform.cpu), 'embedded')

    return python, pip


@contextmanager
def cd(path):
    cwd = os.getcwd()
    os.chdir(path)
    yield
    os.chdir(cwd)
