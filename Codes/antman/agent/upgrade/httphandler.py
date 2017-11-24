# coding: utf-8

import requests
import os
import yaml
import uuid
import urlparse
from constants import logger, CONFIG_PATH


class HttpHandler(object):

    def __init__(self, task_id):
        self.task_id = task_id
        self.conf_dict = {}

    def _load_config(self):
        if os.path.exists(CONFIG_PATH):
            try:
                with open(CONFIG_PATH) as f:
                    self.conf_dict = yaml.load(f.read())
            except yaml.YAMLError as exc:
                logger.error('Invalid configuration: %s', exc)

    def _init_url(self):
        url = urlparse.urljoin(self.conf_dict['upstream'], 'dispatcher/message')
        self.message_url = "{}?tenant={}".format(url, self.conf_dict['tenant'])
        self.download_url = urlparse.urljoin(self.conf_dict['upstream'],
                                             'file/')

    def initialization(self):
        self._load_config()
        self._init_url()

    def post_message(self, msg, done, status):
        if done:
            body = {
                'task_id': self.task_id,
                'result': msg,
                'exit_code': status,
                'is_timeout': False,
                'is_aborted': False
            }
            type = 102
        else:
            body = {'task_id': self.task_id, 'log': msg}
            type = 103
        payload = {
            'messages': [{
                'id': uuid.uuid4().hex,
                'type': type,
                'body': body
            }]
        }
        logger.info(payload)
        try:
            r = requests.post(self.message_url, json=payload, verify=False)
            if r.status_code != 200:
                logger.error('Post task log:{} failed, code {} !!!'
                             .format(payload, r.status_code))
        except Exception as e:
            logger.error('Post task log:{} failed, exception: {}!!!'
                         .format(payload, e))

    def log_ok(self, msg, done=False, status=0):
        self.post_message(msg, done, status)

    def log_error(self, msg, done=False, status=-1):
        self.post_message(msg, done, status)

    def download(self, filename, dst):
        dst_name = os.path.join(dst, filename)
        file_url = urlparse.urljoin(self.download_url, filename)
        r = requests.get(file_url, stream=True, verify=False)
        with open(dst_name, 'wb') as code:
            for chunk in r.iter_content(chunk_size=10240000):
                if chunk:
                    code.write(chunk)
        return dst_name
