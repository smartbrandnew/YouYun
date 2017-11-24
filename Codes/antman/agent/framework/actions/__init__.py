# coding: utf-8
import os
import sys
import logging
import logging.config

import nfs
from framework.actions.constants import LOG_DIR

os.umask(0027)

if not nfs.exists(LOG_DIR):
    nfs.makedirs(LOG_DIR)

LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'verbose': {
            'format':
            '[%(levelname)s][%(asctime)s][%(module)s][%(process)d] %(message)s'
        },
        'module': {
            'format': '[%(levelname)s][%(asctime)s][%(process)d] %(message)s'
        },
        'simple': {
            'format': '%(message)s'
        },
    },
    'handlers': {
        'console': {
            'level': 'INFO',
            'class': 'logging.StreamHandler',
            'formatter': 'simple',
            'stream': sys.stdout
        },
        'agent_actions': {
            'level': 'INFO',
            'class': 'logging.handlers.RotatingFileHandler',
            'filename': os.path.join(LOG_DIR, 'agent.log'),
            'maxBytes': 1024 * 1024 * 5,  # 5 MB
            'backupCount': 5,
            'formatter': 'verbose',
        },
        'module': {
            'level': 'INFO',
            'class': 'logging.handlers.RotatingFileHandler',
            'filename': os.path.join(LOG_DIR, 'module.log'),
            'maxBytes': 1024 * 1024 * 5,  # 5 MB
            'backupCount': 5,
            'formatter': 'module',
        },
    },
    'loggers': {
        'agent': {
            'handlers': ['console', 'agent_actions'],
            'level': 'INFO',
            'propagate': False,
        },
        'module': {
            'handlers': ['console', 'module'],
            'level': 'INFO',
            'propagate': False,
        },
    }
}

logging.config.dictConfig(LOGGING)
logger = logging.getLogger('agent')
module_logger = logging.getLogger('module')
