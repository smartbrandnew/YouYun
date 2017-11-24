import os
import sys
import locale

IS_WINDOWS = os.name == 'nt'
ENCODING = locale.getpreferredencoding()

BASE_DIR = os.path.dirname(os.path.realpath(__file__))
ROOT_DIR = os.path.dirname(BASE_DIR)
REPO_DIR = os.path.join(os.getcwd(), 'repo')
LOGS_DIR = os.path.join(os.getcwd(), 'logs')
CACHE_DIR = os.path.join(os.getcwd(), 'cache')
MESSAGE_CACHE_DIR = os.path.join(CACHE_DIR, 'message')
MODULES_DIR = os.path.join(os.getcwd(), 'modules')
MESSAGE_CURSOR_FILE = os.path.join(CACHE_DIR, 'cursor')

REPO_ANT_SPACENAME = 'ant'

CORE_ACTIONS = {
    'core.hello_world':
    [sys.executable, '-m', 'framework.actions.hello_world'],
    'core.scan': [sys.executable, '-m', 'framework.actions.scan'],
    'core.install': [sys.executable, '-m', 'framework.actions.install'],
    'core.upgrade': [sys.executable, '-m', 'framework.actions.upgrade'],
    'core.uninstall': [sys.executable, '-m', 'framework.actions.uninstall'],
    'core.install_module':
    [sys.executable, '-m', 'framework.actions.module.install'],
    'core.uninstall_module':
    [sys.executable, '-m', 'framework.actions.module.uninstall'],
    'core.start_module': [
        sys.executable, '-m', 'framework.actions.module.start'
    ],
    'core.stop_module': [
        sys.executable, '-m', 'framework.actions.module.stop'
    ],
    'core.upgrade_module': [
        sys.executable, '-m', 'framework.actions.module.upgrade'
    ]
}

LOG_LEVEL = 'DEBUG' if os.getenv('ANT_DEBUG') else 'WARN'

LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'verbose': {
            'format':
            '[%(levelname)s][%(asctime)s][%(module)s][%(process)d] %(message)s'  # flake8: noqa
        },
        'simple': {
            'format': '[%(levelname)s] %(message)s'
        },
    },
    'handlers': {
        'console': {
            'level': LOG_LEVEL,
            'class': 'logging.StreamHandler',
            'formatter': 'verbose',
            'stream': sys.stdout
        },
        'default': {
            'level': LOG_LEVEL,
            'class': 'logging.handlers.RotatingFileHandler',
            'filename': os.path.join(LOGS_DIR, 'default.log'),
            'maxBytes': 1024 * 1024 * 5,  # 5 MB
            'backupCount': 5,
            'formatter': 'verbose',
        },
        'tornado.access': {
            'level': LOG_LEVEL,
            'class': 'logging.handlers.RotatingFileHandler',
            'filename': os.path.join(LOGS_DIR, 'tornado.application.log'),
            'maxBytes': 1024 * 1024 * 5,  # 5 MB
            'backupCount': 5,
            'formatter': 'verbose',
        },
        'tornado.application': {
            'level': LOG_LEVEL,
            'class': 'logging.handlers.RotatingFileHandler',
            'filename': os.path.join(LOGS_DIR, 'tornado.application.log'),
            'maxBytes': 1024 * 1024 * 5,  # 5 MB
            'backupCount': 5,
            'formatter': 'verbose',
        },
        'tornado.general': {
            'level': LOG_LEVEL,
            'class': 'logging.handlers.RotatingFileHandler',
            'filename': os.path.join(LOGS_DIR, 'tornado.general.log'),
            'maxBytes': 1024 * 1024 * 5,  # 5 MB
            'backupCount': 5,
            'formatter': 'verbose',
        }
    },
    'loggers': {
        'default': {
            'handlers': ['console', 'default'],
            'level': LOG_LEVEL,
            'propagate': False,
        },
        'tornado.application': {
            'handlers': ['console', 'tornado.application'],
            'level': LOG_LEVEL,
            'propagate': False,
        },
        'tornado.general': {
            'handlers': ['console', 'tornado.general'],
            'level': LOG_LEVEL,
            'propagate': False,
        },
        'tornado.access': {
            'handlers': ['console', 'tornado.access'],
            'level': LOG_LEVEL,
            'propagate': False,
        },
    }
}
