# coding: utf-8
import os
from os import path

DISABLE_POSTFIX = '.disable'
PKG_YAML_NAME = 'manifest.yaml'
FULLNAME_SEP = '@'

REQUIRED_PKG_FIELDS = ('name', 'version', 'description', 'author')

# directory or path
ROOT_DIR = os.getcwd()
LOG_DIR = path.join(ROOT_DIR, 'logs')
PKG_DIR = path.join(ROOT_DIR, 'modules')

PKG_CACHE_DIR = path.join(PKG_DIR, '.cache')
PKG_UNCOMPRESS_DIR = path.join(PKG_DIR, '.uncompress')
PKG_UPGRADE_DIR = path.join(PKG_DIR, '.upgrade')

LOCK_FILE = path.join(PKG_DIR, '.module.yaml')

PLATFORM_LIST = [
    'Linux', 'Windows', 'CentOS', 'Debian', 'Redhat', 'Amazon', 'AIX', 'Darwin',
    'openSUSE', 'SUSE', 'Arista', 'Ubuntu', 'FreeBSD', 'NeoKylin'
]
