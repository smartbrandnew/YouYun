import hashlib
import logging
import os
import platform
from util import get_uuid

import requests

log = logging.getLogger('__name__')


class CentralConfigurator(object):
    def __init__(self,
                 system,
                 linux_conf_path,
                 window_conf_path,
                 central_configuration_url,
                 central_configuration_api_key):

        self.os = system.lower()
        self.linux_conf_path = linux_conf_path
        self.window_conf_path = window_conf_path
        self.central_configuration_url = central_configuration_url
        self.central_configuration_api_key = central_configuration_api_key
        self.agent_id = get_uuid()
        self.done = 0
        self.path = {
            "linux": os.path.join(self.linux_conf_path, "conf.d/"),
            "windows": os.path.join(self.window_conf_path, "conf.d/"),
        }
        self.apis = {
            "client_info": "{}list?id={}&source=agent&api_key={}".format(
                self.central_configuration_url,
                self.agent_id,
                self.central_configuration_api_key
            ),
            "client_download_file": "{}file?id={}&source=agent&api_key={}&name=".format(
                self.central_configuration_url,
                self.agent_id,
                self.central_configuration_api_key
            ),
        }

    def get_file_md5(self, f, block_size=1024):
        try:
            m = hashlib.md5()
            bfile = open(f, 'rb')
            while True:
                data = bfile.read(block_size)
                if not data:
                    break
                m.update(data)
            bfile.close()
            return m.hexdigest()
        except Exception as e:
            return "ERR", e

    def get_file_list_info(self):
        try:
            r = requests.get(self.apis.get("client_info"), verify=False)
            return r.content
        except Exception as e:
            log.error("get file list error: {}, {}".format(e, str(self.apis.get("client_info"))))
            return "ERR"

    def download_file(self, filename=None, block_size=10 * 1024):
        try:
            url = self.apis.get("client_download_file") + filename
            r = requests.get(url, stream=True, verify=False)
            with open(filename, 'wb') as g:
                for part in r.iter_content(chunk_size=block_size):
                    if part:
                        g.write(part)
            log.info("update: {}".format(filename))
        except Exception as e:
            log.error("download {} file error, {}".format(filename, e))

    def do_configurator(self):
        os.chdir(self.path[self.os])
        file_list_info = self.get_file_list_info().replace("true", "'true'").replace("false", "'false'")
        file_list_info = list(eval(file_list_info))
        if len(file_list_info) > 0 or file_list_info != "ERR":
            for f in file_list_info:
                f_name = f["fileName"]
                md5 = f["md5"]
                if md5 != self.get_file_md5(f_name + ".yaml"):
                    self.download_file(f_name + ".yaml")
                    if not self.correct_file(f_name + ".yaml"):
                        os.remove(f_name + ".yaml")
                    else:
                        self.done = 1
        return self.done

    def correct_file(self, f):
        with open(f) as ff:
            if "instances" in ff.read():
                return True
        return False
