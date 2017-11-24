import os
import logging
import logging.config

os.umask(0027)

BASE_DIR = os.path.dirname(os.path.realpath(__file__))
LOGS_DIR = os.path.join(os.getcwd(), 'logs')
UPGRADE_PORT = '16601'
IS_WINDOWS = os.name == 'nt'
START_AGENT_TIMEOUT = 20
STOP_AGENT_TIMEOUT = 20
TIME_TICK = 5

if not os.path.exists(LOGS_DIR):
    os.mkdir(LOGS_DIR)

LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'verbose': {
            'format':
            '[%(levelname)s][%(asctime)s][%(module)s][%(process)d] %(message)s'
        },
        'simple': {
            'format': '[%(levelname)s] %(message)s'
        },
    },
    'handlers': {
        'console': {
            'level': 'INFO',
            'class': 'logging.StreamHandler',
            'formatter': 'verbose'
        },
        'rotating': {
            'level': 'INFO',
            'class': 'logging.handlers.RotatingFileHandler',
            'filename': os.path.join(LOGS_DIR, 'upgrade.log'),
            'maxBytes': 1024 * 1024 * 5,
            'backupCount': 5,
            'formatter': 'verbose',
        },
    },
    'loggers': {
        'upgrade': {
            'handlers': ['console', 'rotating'],
            'level': 'INFO',
            'propagate': False,
        },
    }
}

logging.config.dictConfig(LOGGING)
logger = logging.getLogger('upgrade')

ROOT_DIR = os.getcwd()
CONFIG_NAME = 'config.yaml'
CONFIG_PATH = os.path.join(ROOT_DIR, CONFIG_NAME)
SYSTEM_SCRIPT_TYPE = '.bat' if IS_WINDOWS else '.sh'
BIN_START = os.path.join(ROOT_DIR, 'bin', 'start' + SYSTEM_SCRIPT_TYPE)
BIN_STOP = os.path.join(ROOT_DIR, 'bin', 'stop' + SYSTEM_SCRIPT_TYPE)

AGENT_BACK_DIRNAME = '.backup'
AGENT_DOWNLOAD_DIRNAME = '.download_agents'
AGENT_UNCOMPRESS_DIRNAME = '.uncompress-agent'
AGENT_BACK_DIR = os.path.join(ROOT_DIR, AGENT_BACK_DIRNAME)
AGENT_DOWNLOAD_DIR = os.path.join(ROOT_DIR, AGENT_DOWNLOAD_DIRNAME)
AGENT_UNCOMPRESS_DIR = os.path.join(ROOT_DIR, AGENT_UNCOMPRESS_DIRNAME)

EXCLUDE_BACK_DIRS = ('logs', '.download_agents', '.uncompress-agent', '.backup',
                     'modules', '.embedded')
