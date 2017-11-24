# coding: utf-8

import yaml
import sys
import hashlib
from os.path import dirname, realpath, join

import nfs

ROOT_DIR = dirname(realpath(__file__))
CONF_PATH = join(ROOT_DIR, 'config.yaml')


def init_uuid():
    try:
        if nfs.exists(CONF_PATH):
            with open(CONF_PATH) as f:
                conf_dict = yaml.load(f.read()) or {}
                if 'id' in conf_dict and conf_dict['id']:
                    return

                id_str = '{}:{}:{}'.format(conf_dict['tenant'],
                                           conf_dict['network_domain'],
                                           conf_dict['ip'])

                conf_dict.update({'id': hashlib.md5(id_str).hexdigest()})
                result = yaml.dump(conf_dict, default_flow_style=False)

            with open(CONF_PATH, 'w') as f:
                f.write(result)
    except Exception as e:
        print(e)
        sys.exit(1)


if __name__ == '__main__':
    init_uuid()
