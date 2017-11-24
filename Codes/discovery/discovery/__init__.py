import os
import logging
import logging.config


level = 'DEBUG' if os.getenv('ANT_DEBUG') else 'WARNING'
logging_conf = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'verbose': {
            'format':
            # flake8: noqa
            '[%(levelname)s][%(asctime)s][%(module)s][%(process)d] %(message)s'
        },
        'simple': {
            'format': '[%(levelname)s] %(message)s'
        },
    },
    'handlers': {
        'discovery': {
            'level': level,
            'class': 'logging.handlers.RotatingFileHandler',
            'filename': os.path.join(os.getcwd(), 'remote-discovery.log'),
            'maxBytes': 1024 * 1024 * 5,  # 5 MB
            'backupCount': 5,
            'formatter': 'verbose',
        }
    },
    'loggers': {
        'discovery': {
            'handlers': ['discovery'],
            'level': 'INFO',
            'propagate': False,
        }
    }
}
logging.config.dictConfig(logging_conf)
logger = logging.getLogger('discovery')
