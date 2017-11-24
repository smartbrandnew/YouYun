import os
import yaml
import contextlib

from framework.settings import BASE_DIR, ROOT_DIR

import logging

logger = logging.getLogger('default')


class Config(dict):

    def __init__(self):
        super(Config, self).__init__()
        self.reload()

    def reload(self):
        self._load_manifest_config()
        self._load_template_config()
        self._load_user_config()

    def _load_config(self, filename):
        with open(filename) as f:
            try:
                self.update(yaml.load(f.read()))
            except yaml.YAMLError as exc:
                logger.error('Invalid configuration: %s', exc)

    def _load_manifest_config(self):
        manifest_file = os.path.join(ROOT_DIR, 'manifest.yaml')
        logger.info('Loading manifest configuration: %s', manifest_file)
        if os.path.exists(manifest_file):
            self._load_config(manifest_file)

    def _load_template_config(self):
        template_file = os.path.join(BASE_DIR, 'config.template.yaml')
        logger.info('Loading template configuration: %s', template_file)
        self._load_config(template_file)

    def _load_user_config(self):
        user_file = os.path.join(os.getcwd(), 'config.yaml')
        logger.info('Loading user configuration: %s', user_file)
        if os.path.exists(user_file):
            self._load_config(user_file)
        else:
            logger.warn('User configuration does not exist')

    @contextlib.contextmanager
    def mock(self, d):
        origin_d = {}
        for k in d:
            assert k in self
            origin_d[k] = self[k]
            self[k] = d[k]
        yield
        for k in origin_d:
            self[k] = origin_d[k]


config = Config()
