# coding: utf-8

import os
import logging
import traceback

import cPickle as pickle
import requests
from subprocess import Popen, PIPE, STDOUT
from config import get_config, get_checksd_path, get_confd_path
from util import get_uuid


log = logging.getLogger(__name__)
IS_WINDOWS = os.name == 'nt'


class InfoError(Exception):
    """ error raised when get scripts info error"""
    pass


class DealScripts(object):

    def __init__(self):
        c = get_config()
        self.request_interval = c.get("request_interval", 120)
        self.requests = requests.session()
        self.uuid = get_uuid()
        self.apikey = c.get("api_key")
        self.post_param = {"apikey": self.apikey}
        self.urls = self.init_url(c)
        self.run_path = os.path.join(os.path.dirname(get_confd_path()), 'run')
        self.script_path = get_checksd_path().replace("checks.d", "scripts")
        self.need_restart = False

    def init_url(self, config):
        urls = {}
        m_url = config.get("m_url")
        urls['script_info_url'] = m_url.replace("/api/v2/gateway/dd-agent", "/api/v2/agent/config/script/info")
        urls['download_url'] = m_url.replace("/api/v2/gateway/dd-agent", "/api/v2/agent/config/script/download")
        urls['post_verify_url'] = m_url.replace("/api/v2/gateway/dd-agent", "/api/v2/agent/config/script/status")
        return urls

    def get_script_info(self):
        reps = self.requests.get(self.urls.get("script_info_url"),
                                 params={"apikey": self.apikey, "agentId": self.uuid},
                                 verify=False)
        if reps.status_code != 200:
            raise InfoError("get scripts info error")
        log.debug("get url: {}".format(reps.url))
        log.debug("script info: {}".format(reps.json()))
        return reps.json()

    def run(self):
        try:
            script_info = self.get_script_info()
            added_verify_scirpts, deleted_verify_scripts, added_apply_scirpts, deleted_apply_scripts = self.compare_script_info(script_info)
            self.delete_scripts(deleted_apply_scripts)
            self.download_scripts(added_verify_scirpts, added_apply_scirpts)
            result = self.verify_scripts(added_verify_scirpts)
            self.post_verify_reulst(result)
            if added_apply_scirpts or added_apply_scirpts or self.need_restart:
                return True
        except Exception as e:
            log.error("exception: {}".format(traceback.format_exc()))

    def list_to_dict(self, script_info_list):
        """
        :param script_info_list:
        :return: dict
        """
        return {k['md5']: k for k in script_info_list}

    def compare_script_info(self, infos):
        deleted_apply_scripts = []
        added_verify_scirpts = deleted_verify_scripts = infos['verify']
        log.debug("added_verify_scirpts: {}".format(added_verify_scirpts))
        applys_info = infos['apply']
        next_apply_dict = self.list_to_dict(applys_info)
        apply_pickle = os.path.join(self.run_path, 'apply.pickle')
        if os.path.exists(apply_pickle):
            with open(apply_pickle, 'r') as f:
                last_apply_dict = pickle.load(f)
                log.info("last_apply_dict: {}".format(last_apply_dict))
                added_apply_scirpts, deleted_apply_scripts = self.compare_last_info(last_apply_dict, next_apply_dict)
                log.info("added_apply_scirpts: {}, deleted_apply_scripts: {}".format(added_apply_scirpts, deleted_apply_scripts))
                if not (added_verify_scirpts or added_apply_scirpts or deleted_apply_scripts):
                    log.info("This time need no validation and application")
                    return [], [], [], []
        else:
            added_apply_scirpts = applys_info
        with open(apply_pickle, 'w') as f:
            log.info("next_apply_dict: {}".format(next_apply_dict))
            pickle.dump(next_apply_dict, f)
        log.debug("{}, {}, {}, {}".format(added_verify_scirpts, deleted_verify_scripts, added_apply_scirpts, deleted_apply_scripts))
        return added_verify_scirpts, deleted_verify_scripts, added_apply_scirpts, deleted_apply_scripts

    def compare_last_info(self, apply_dict, next_apply_dict):
        # comparison of the last time and the application
        # return type: list
        added_apply_scirpts = []
        deleted_apply_scripts = []
        delete_scripts = apply_dict.viewkeys() - next_apply_dict.viewkeys()
        added_scripts = next_apply_dict.viewkeys() - apply_dict.viewkeys()
        for md5 in added_scripts:
            added_apply_scirpts.append(next_apply_dict[md5])
        for md5 in delete_scripts:
            deleted_apply_scripts.append(apply_dict[md5])
        # need_apply need_delete is list
        return added_apply_scirpts, deleted_apply_scripts

    def download_scripts(self, *files_list):
        # post interface get script content
        log.info("files_list: {}".format(files_list))
        if not files_list:
            return True
        files_id = []
        for files in files_list:
            log.debug("files: {}".format(files))
            for file in files:
                log.debug("file: {}".format(file))
                files_id.append(file["scriptId"])
        if files_id:
            json = {"agentId": self.uuid, "scripts": files_id}
            log.info("json: {}".format(json))
            scripts_infos = self.requests.post(self.urls.get("download_url"), json=json, params=self.post_param, verify=False)
            log.debug("download_url : {}".format(scripts_infos.json()))
            if scripts_infos.status_code != 200:
                log.error("download script file error: {}".format(scripts_infos.raise_for_status()))
                return
            for script in scripts_infos.json()['scripts']:
                log.debug("script: {}".format(script))
                log.info("script path: {}".format(os.path.join(self.script_path, script['scriptName'])))
                file_name = os.path.join(self.script_path, script['scriptName'])
                with open(file_name, 'w') as f:
                    f.write(script['content'])
                if script['scriptName'].endswith("sh"):
                    self.execute("chmod +x {}".format(file_name), 1)
                log.info("Now, download {} success".format(script['scriptName']))
            return True

    def post_verify_reulst(self, data):
        # post verify info if exist json
        if data:
            post_json = {"agentId": self.uuid}
            post_json.update(data)
            log.debug("post verify info: {}".format(post_json))
            r = self.requests.post(self.urls.get("post_verify_url"), params=self.post_param, json=post_json, verify=False)
            log.debug("post url: {}".format(r.url))
            log.debug("post url code : {}".format(r.status_code))
            if r.status_code != 500:
                log.info("post script verify result sucess")
            else:
                log.info("post script verify result failed: {}".format(r.raise_for_status()))

    def delete_scripts(self, *files_list):
        # delete verify files and no apply files
        log.info("This time need delete files: {}".format(files_list))
        for files in files_list:
            for file in files:
                try:
                    file_path = os.path.join(self.script_path, file['scriptName'])
                    if os.path.exists(file_path):
                        log.debug("delete file: {}".format(file['scriptName']))
                        os.remove(file_path)
                except OSError as e:
                    log.error("delete {} failed {}".format(file['scriptName'], e))

    def delete_store_file(self, file):
        # delete apply.pickle
        log.info("This time need delete files: {}".format(file))
        try:
            if os.path.exists(file):
                os.remove(file)
        except OSError as e:
            log.error("delete {} failed {}".format(file, e))

    def verify_scripts(self, files):
        # verify script only support
        checkedList = []
        failedList = []
        if not files:
            return None
        result = {"checkedList": [],
                  "failedList": []}
        for file in files:
            file_path = os.path.join(self.script_path, file['scriptName'])
            log.debug("This time need verify: {}".format(file_path))
            if os.path.exists(file_path):
                if file_path.endswith('py'):
                    cwd = os.environ.get('ANT_AGENT_DIR')
                    if IS_WINDOWS:
                        python_execute = os.path.join(cwd, "embedded\python.exe")
                    else:
                        python_execute = os.path.join(cwd, 'embedded/bin/python')
                    stdout_data, stderr_data = self.execute([python_execute, file_path])
                else:
                    stdout_data, stderr_data = self.execute(file_path, 1)
                log.debug("result:{}, error:{}".format(stdout_data, stderr_data))

                if stdout_data and not stderr_data and self.exist_keywords(stdout_data):
                    result["checkedList"].append({"scriptId": file["scriptId"]})
                    log.debug("Added {} to the application for validation to succeed".format(file['scriptName']))
                    checkedList.append(file)
                else:
                    if stderr_data:
                        log.info("stderr_json: {}".format(stderr_data))
                        stderr_data = ''.join(stderr_data.split(":")[2:]).replace("\n", '')
                        result["failedList"].append({"scriptId": file["scriptId"], "reason": stderr_data})
                    else:
                        reason = "The keyword is not defined such as metric value type"
                        result["failedList"].append({"scriptId": file["scriptId"], "reason": reason})
                    log.debug("Add {} to delete files list for validation fails".format(file['scriptName']))
                    failedList.append(file)
        self.delete_scripts(failedList)
        if checkedList:
            self.need_restart = True
            apply_pickle = os.path.join(self.run_path, 'apply.pickle')
            with open(apply_pickle, 'r') as f:
                last_apply_dict = pickle.load(f)
            last_apply_dict.update(self.list_to_dict(checkedList))
            with open(apply_pickle, 'w') as f:
                pickle.dump(last_apply_dict, f)
        return result

    def exist_keywords(self, stdout):
        return True if "metric" in stdout and "value" in stdout and "type" in stdout else False

    def execute(self, cmd, shell=0):
        proc = Popen(cmd, stdout=PIPE, stderr=STDOUT, shell=shell)
        result, error = proc.communicate()
        if proc.returncode != 0:
            log.error("execute {} failed".format(cmd))
        else:
            log.info("execute {} success".format(cmd))
        return result, error
