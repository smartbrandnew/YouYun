#!./embedded/bin/python


import base64
import cPickle as pickle
import hashlib
import logging
import os
import sys
import tempfile

import requests

from config import _windows_commondata_path
from utils.pidfile import PidFile
from utils.platform import Platform

reload(sys)
sys.setdefaultencoding('utf8')

log = logging.getLogger('__name__')


def get_urls(agent_os=None, api_url=None):
    urls = {
        "client": api_url + agent_os,
        "client_version": api_url + agent_os + "/version",
        "client_file_v2": api_url + agent_os + "/file-v2",
        "client_download": api_url + agent_os + "/file/"
    }
    return urls


class AgentUpdater(object):
    def __init__(self, system, server, api_url, dirs):
        self.os = system.lower()
        self.server = server
        self.api_url = api_url
        self.dirs = dirs
        self.urls = get_urls(self.os, self.api_url)
        self.next_md5 = {}
        try:
            self.last_md5_file_path = self._get_pickle_path("last_md5")
            f = open(self.last_md5_file_path, 'rb')
            self.last_md5 = pickle.load(f)
        except IOError:
            self.last_md5 = {}

        try:
            self.file_list_path = self._get_pickle_path("filelist")
            f = open(self.file_list_path, 'rb')
            self.file_list = pickle.load(f)
        except IOError:
            self.file_list = {}

        try:
            self.version_file_path = self._get_pickle_path("version")
            f = open(self.version_file_path, 'rb')
            self.version = pickle.load(f)
        except IOError:
            self.version = ""
        log.info("--------------{} agent start update------------".format(self.os))

    @classmethod
    def _get_pickle_path(cls, name):
        if Platform.is_win32():
            path = os.path.join(_windows_commondata_path(), 'Datamonitor')
        elif os.path.isdir(PidFile.get_dir()):
            path = PidFile.get_dir()
        else:
            path = tempfile.gettempdir()
        return os.path.join(path, name + '.pickle')

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
        except Exception:
            return "File Not Exist"

    def download_client(self):
        try:
            r = requests.get(self.urls.get("client"), verify=False)
            return r.content
        except IOError:
            log.error("download client error")
            return "Error"

    def download_version(self):
        try:
            r = requests.get(self.urls.get("client_version"), verify=False)
            return r.content
        except IOError:
            log.error("download version error")
            return "Error"

    def download_file_v2(self):
        try:
            r = requests.get(self.urls.get("client_file_v2"), verify=False)
            f = open(self.file_list_path, 'wb')
            pickle.dump(r.content, f)
            f.close()
            log.debug("download new file list")
            return r.content
        except IOError:
            log.error("download file list error")
            return "Error"

    def download_file(self, f_dir=None, filename=None, block_size=10 * 1024):
        try:
            url = self.urls.get("client_download") + base64.b64encode(filename)  # BASE64 encode
            f = os.path.join(f_dir, filename)

            r = requests.get(url, stream=True, verify=False)
            with open(f, 'wb') as g:
                for part in r.iter_content(chunk_size=block_size):
                    if part:
                        g.write(part)
            log.debug("update {}".format(filename))
            return "Success"
        except IOError:
            if "/" in filename:
                rf = f[::-1]
                pos = rf.index("/")
                dirs = f[:-pos]
                name = f[-pos:]
                if not os.path.exists(dirs):
                    os.makedirs(dirs)
            open(name, "wb")
            self.download_file(f_dir, filename)
        except Exception as e:
            log.error(e)
            return "Error"

    def do_update(self):
        version = self.download_version()
        if version == self.version:
            log.info("agent still newest, no need to update")
            return

        try:
            f = open(self.version_file_path, 'wb')
            pickle.dump(version, f)
            f.close()
            log.info("current agent version is updated %s, now: " % version)
        except IOError:
            log.critical("could not update version, update script terminated")
            return

        try:
            self.download_file_v2()
            f = open(self.file_list_path, 'rb')
            self.file_list = pickle.load(f)
            self.file_list = eval(self.file_list)
            for file_info in self.file_list["files"]:
                f_name = file_info["name"]

                if "conf.d" in f_name and f_name.endswith(".yaml"):
                    continue

                if self.last_md5.has_key(f_name) and self.last_md5.has_key(f_name) and self.last_md5[f_name] == \
                        file_info["md5"]:
                    self.next_md5[f_name] = file_info["md5"]
                    continue

                if "conf.d" in f_name:
                    f_dir = self.dirs["conf"]
                else:
                    f_dir = self.dirs["checks"]
                f_md5 = self.get_file_md5(os.path.join(f_dir, f_name))

                if "File Not Exist" == f_md5:
                    log.debug("agent add new file {}".format(f_name))
                    self.download_file(f_dir, f_name)
                elif file_info["md5"] != f_md5:
                    log.debug("file {} updated".format(f_name))
                    self.download_file(f_dir, f_name)
                self.next_md5[f_name] = file_info["md5"]

                try:
                    if not self.correct_file(os.path.join(f_dir, f_name)):
                        os.remove(os.path.join(f_dir, f_name))

                except Exception as e:
                    log.error(e)

            self.remove_extra_file()
            self.store_md5_for_next_use()
        except KeyError as e:
            log.error("error on update agent, self.last_md5 has no key: {}".format(e))
        except IOError as e:
            log.error(e)
        except Exception as e:
            log.error(e)

    def store_md5_for_next_use(self):
        try:
            f = open(self.last_md5_file_path, 'wb')
            pickle.dump(self.next_md5, f)
            f.close()
            log.info("store md5 file completed for next use")
        except Exception as e:
            log.error("error on store md5. {}".format(e))

    def remove_extra_file(self):
        try:
            to_remove = list(set(self.last_md5.keys()).difference(set(self.next_md5)))
            for f_name in to_remove:
                if "conf.d" in f_name and f_name.endswith(".yaml"):
                    continue
                elif "cond.d" in f_name and f_name.endswith(".default"):
                    continue
                elif "conf.d" in f_name and f_name.endswith(".yaml"):
                    f_dir = self.dirs["conf"]
                else:
                    f_dir = self.dirs["checks"]
                os.remove(os.path.join(f_dir, f_name))
                log.info("remove extra file %s" % f_name)
        except Exception as e:
            log.error("remove extra file error, {}".format(e))

    def correct_file(self, f):
        with open(f) as ff:
            if "instances" or "AgentCheck" in ff.read():
                return True
        return False
