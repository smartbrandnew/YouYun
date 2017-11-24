# coding: utf-8
import platform
from os import path

ROOT_DIR = path.dirname(path.dirname(path.realpath(__file__)))
BIN_DIR = path.join(ROOT_DIR, 'bin')
CIRCLED_PATH = path.join(BIN_DIR, 'circled')
BIN_START = path.join(BIN_DIR, 'start.bat')
UPGRADE_START = path.join(BIN_DIR, 'start_upgrade.bat')

ARCHITECTURE = 64 if '64' in platform.architecture()[0] else 32
NSSM_NAME = 'nssm64.exe' if ARCHITECTURE == 64 else 'nssm32.exe'
NSSM_PATH = path.join(BIN_DIR, NSSM_NAME)

# default endpoints
DEFAULT_ENDPOINT_DEALER = '127.0.0.1:16602'
MSG_END = '__CIRCLE_END__'
