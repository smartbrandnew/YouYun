# coding=utf-8
import urlparse
import json
from robot.api import logger
import requests
import time
from library.utils import create_ssh, excutor_cmd
from library.constants import (
    INSTALL_AGENT_API_FORMAT, INSTALL_AGENT_SUCCESS_MSG,
    DEFAULT_AGENT_INSTALL_PATH, DEFAULT_MANAGER_HOST, DEPLOY_MECHINE,
    UNINSTALL_AGENT_API_FORMAT, UNINSTALL_AGENT_SUCCESS_MSG,
    UPGRADE_AGENT_API_FORMAT, UPGRADE_AGENT_SUCCESS_MSG, INSTALL_DIR,
    TENANT_LOGIN_URL, GET_NETWORK_DOMAIN_URL, SAVE_PACKAGE_MECHINE,
    AGENT_INFO_API_FORMAT, COMPUTE_OBJECT_INFO_API_FORMAT, DIST_ROOT,
    GET_AGENT_BY_IP_URL
)


class ManagerLibrary(object):
    def __init__(self):
        self.user_name = "admin@uyun.cn"
        self.passwd = "Ant123456;"
        self.token = self.get_token(self.user_name, self.passwd)
        self.network_domain = self.get_network_domain()

    def get_network_domain(self):
        target_uri = self.__generate_manager_api_uri(
            GET_NETWORK_DOMAIN_URL
            , DEFAULT_MANAGER_HOST)
        response = requests.get(
            target_uri,
            cookies={"token": self.token}
        )
        if response.status_code != 200:
            raise Exception("Get network domain faild")

        for network in json.loads(response.content):
            if network["name"] == u"默认域":
                self.network_domain = network["id"]

        return self.network_domain

    def get_token(self, user_name, password):
        login_json = {
            "email": user_name,
            "passwd": password}
        target_url = self.__generate_manager_api_uri(
            TENANT_LOGIN_URL,
            DEFAULT_MANAGER_HOST
        )
        resposne = json.loads(requests.post(target_url, json=login_json).content)
        return resposne['data']['token']

    def install_agent(self, ips, user_name, password):
        logger.info("Send Install Agent Request...", html=True, also_console=True)

        target_uri = self.__generate_manager_api_uri(
            INSTALL_AGENT_API_FORMAT,
            DEFAULT_MANAGER_HOST)

        json_arg = {
            "parentAgentId": "SERVER",
            "agentInfos": [{
                "username": user_name,
                "password": password,
                "installPath": DEFAULT_AGENT_INSTALL_PATH,
                "port": 22,
                "networkDomainId": self.network_domain,
                "ips": ips,
                "modules": []}
            ],
            "mapping": []
        }
        response = requests.post(
            target_uri,
            json=json_arg,
            cookies={"token": self.token})
        self.__response_should_success_with_expected_msg(
            response, INSTALL_AGENT_SUCCESS_MSG)

    def uninstall_agent(self, ips):
        logger.info("Send Uninstall Agent Request...", html=True, also_console=True)

        for ip in json.loads(ips):
            agent = self._get_content_by_ip(ip, GET_AGENT_BY_IP_URL)

            if not agent:
                raise Exception("Can not find Agent on %s" % ip)

            agent_id = agent["attrValues"]["agentID"]

            target_uri = self.__generate_manager_api_uri(
                UNINSTALL_AGENT_API_FORMAT, DEFAULT_MANAGER_HOST)

            json_arg = {
                "parentAgentId": "SERVER",
                "agentIds": [agent_id]
            }

            response = requests.post(
                target_uri,
                json=json_arg,
                cookies={"token": self.token}
            )
            self.__response_should_success_with_expected_msg(
                response, UNINSTALL_AGENT_SUCCESS_MSG)

    def upgrade_agent(self, ips):
        logger.info("Send Upgrade Agent Request...", html=True, also_console=True)
        for ip in json.loads(ips):
            compute_object = self._get_content_by_ip(ip, GET_AGENT_BY_IP_URL)
            if not compute_object:
                raise Exception("Can not find Compute Object with IP %s" % ip)
            agent_id = compute_object["agentId"]

            target_uri = self.__generate_manager_api_uri(
                UPGRADE_AGENT_API_FORMAT, DEFAULT_MANAGER_HOST)

            json_arg = {
                "parentAgentId": "SERVER",
                "agentIds": [agent_id]
            }

            token = self.get_token("admin@uyun.cn", "Ant123456;")
            response = requests.post(target_uri, json=json_arg,
                                     cookies={"token": token})

            self.__response_should_success_with_expected_msg(
                response, UPGRADE_AGENT_SUCCESS_MSG)

    def agent_should_have_been_uninstalled(self, ips, timeout=60):
        for ip in json.loads(ips):
            compute_object = self._get_content_by_ip(ip, GET_AGENT_BY_IP_URL)

            if not compute_object:
                raise Exception('Can not find computer_object')

            target_uri = self.__generate_manager_api_uri(
                AGENT_INFO_API_FORMAT, DEFAULT_MANAGER_HOST) \
                         + "?id=" + compute_object["id"]

            print target_uri,'---------------'
            limit_time = time.time() + timeout
            while True:
                response = requests.get(target_uri, cookies={"token": self.token})
                if 200 != response.status_code:
                    raise Exception(
                        "Request fail. Status Code: " + str(response.status_code))

                if not response.content:
                    return True
                else:
                    if time.time() > limit_time:
                        break
                    else:
                        time.sleep(20)

            raise Exception("Agent uninstallation not completed in limited time.")

    def get_agent_version(self, ips, timeout=60):
        for ip in json.loads(ips):
            compute_object = self._get_content_by_ip(ip, COMPUTE_OBJECT_INFO_API_FORMAT)

            if not compute_object:
                raise Exception('can not find')

            target_uri = self.__generate_manager_api_uri(
                AGENT_INFO_API_FORMAT, DEFAULT_MANAGER_HOST) \
                         + "?id=" + compute_object["id"]

            time_remain = timeout
            while time_remain > 0:
                response = requests.get(target_uri, cookies={"token": self.token})

                if 200 == response.status_code and response.content:
                    content_obj = json.loads(response.content)
                    if 1 == content_obj["runStatus"]:
                        return content_obj

                time_remain -= 20

            raise Exception("Agent installation not completed in limited time.")

    def agent_install_info_should_be_in_store(self, ips, timeout=20):
        for ip in json.loads(ips):
            compute_object = self._get_content_by_ip(ip, COMPUTE_OBJECT_INFO_API_FORMAT)
            if not compute_object:
                raise Exception('can not find')

            target_uri = self.__generate_manager_api_uri(
                AGENT_INFO_API_FORMAT, DEFAULT_MANAGER_HOST) \
                         + "?id=" + compute_object["id"]

            time_remain = timeout
            while time_remain > 0:
                time.sleep(20)
                response = requests.get(target_uri, cookies={"token": self.token})

                if 200 == response.status_code and response.content:
                    content_obj = json.loads(response.content)
                    if 1 == content_obj["runStatus"]:
                        return content_obj
                    if 2 == content_obj["runStatus"]:
                        return content_obj

                time_remain -= 20

                #raise Exception("Agent installation not completed in limited time.")

    def upload_assign_version_agent(self, version, system):
        if 'win' in system:
            postfix = 'zip'
        else:
            postfix = 'tar.gz'
        version = '2.0.{}'.format(version.lstrip('R'))
        agent_name = 'agent-{}-{}.{}'.format(system, version, postfix)
        agent_latest = 'agent-{}-{}.{}'.format(system, version, postfix)
        agent_path = '{}/{}'.format(DIST_ROOT, agent_name)

        ip, user, passwd = SAVE_PACKAGE_MECHINE['ip'], \
                           SAVE_PACKAGE_MECHINE['user'], \
                           SAVE_PACKAGE_MECHINE['passwd']
        target_path = '{}@{}:{}/repo'.format(
            DEPLOY_MECHINE['user'],
            DEPLOY_MECHINE['ip'],
            INSTALL_DIR['dispatcher']
        )
        with create_ssh(ip, user, passwd) as ssh:
            out, err = excutor_cmd(ssh, 'ls {}'.format(agent_path))
            if not out:
                raise Exception('agent-{} do not exists,need build agent package'.format(version))
            excutor_cmd(ssh, 'scp {} {}'.format(agent_path, target_path))

        ip, user, passwd = DEPLOY_MECHINE['ip'], \
                           DEPLOY_MECHINE['user'], \
                           DEPLOY_MECHINE['passwd']

        with create_ssh(ip, user, passwd) as ssh:
            agent_path = '{}/repo/{}'.format(
                INSTALL_DIR['dispatcher'],
                agent_name
            )
            out, err = excutor_cmd(ssh, 'ls {}'.format(agent_path))
            if not out:
                raise Exception("agent package upload faild")
            excutor_cmd(ssh, 'cd {}/repo && rm -rf {} && ln -s {} {}'.format(
                INSTALL_DIR['dispatcher'],
                agent_latest,
                agent_name,
                agent_latest
            ))

    def agent_version_should_be_upgrade(self):
        pass

    def _get_content_by_ip(self, ip, format_url):
        target_uri = self.__generate_manager_api_uri(
            format_url,
            DEFAULT_MANAGER_HOST,
            {"ip": ip, "networkDomainId": self.network_domain}
        )
        print target_uri,'-----------------'
        response = requests.get(
            target_uri,
            cookies={"token": self.token}
        )
        if response.status_code != 200:
            raise Exception("Get Computer Object Faild")
        try:
            content = json.loads(response.content)
        except:
            content = response.content

        return content \
            if 200 == response.status_code else None

    def agent_install_directory_should_be_exists(
            self, ips, user, passwd, install_dir
    ):
        for ip in json.loads(ips):
            with create_ssh(ip, user, passwd) as ssh:
                out, err = excutor_cmd(ssh, 'ls {}'.format(install_dir))
                if not out:
                    raise Exception('{} not exists on {}'.format(install_dir, ip))

    def agent_install_directory_should_be_delete(
            self, ips, user, passwd, install_dir
    ):
        for ip in json.loads(ips):
            with create_ssh(ip, user, passwd) as ssh:
                out, err = excutor_cmd(ssh,
                                       'python -c "import os; exit(int(os.path.exists(\'{}\')))" '.format(install_dir))
                if out:
                    raise Exception('{} not exists on {}'.format(install_dir, ip))

    def agent_process_shuold_be_exists_on(
            self, ips, user, passwd
    ):
        check_cmd = 'ps -ef|grep "agent/embedded/bin/python"|grep -v grep|wc -l'
        for ip in json.loads(ips):
            with create_ssh(ip, user, passwd) as ssh:
                out, err = excutor_cmd(ssh, check_cmd)
                if int(out) < 3:
                    raise Exception('Install faild,Only {} process on {}'.format(int(out), ip))

    def agent_process_shuold_not_be_exists_on(
            self, ips, user, passwd
    ):
        check_cmd = 'ps -ef|grep "agent/embedded/bin/python"|grep -v grep|wc -l'
        for ip in json.loads(ips):
            with create_ssh(ip, user, passwd) as ssh:
                out, err = excutor_cmd(ssh, check_cmd)
                if int(out) != 0:
                    raise Exception('Install faild,Only {} process on {}'.format(int(out), ip))

    @staticmethod
    def __response_should_success_with_expected_msg(response, expected_msg):
        if 200 != response.status_code:
            raise Exception("Request failed.")
        if expected_msg != json.loads(response.content)["message"]:
            raise Exception("Unexpected success  message.")

    @staticmethod
    def __generate_manager_api_uri(api_format, manager_host, query_params={}):
        if not query_params:
            tmp_uri = api_format.format(manager_host)
            return tmp_uri
        else:
            tmp_uri = api_format.format(manager_host) + "?%s"
            import urllib
            return tmp_uri % urllib.urlencode(query_params)


class AgentInstallationArg(object):
    """Arguments for installing agent."""

    def __init__(self):
        # 目标计算对象的父级节点ID。Agent将被安装在目标计算对象上
        self.parentAgentId = ""

        # 用于登录目标计算对象
        self.userName = ""

        # 用于登录目标计算对象
        self.password = ""

        # 用于远程登录（SSH）目标计算对象的端口号
        self.port = ""

        # Agent的安装路径
        self.installPath = ""

        # Agent安装的目标网络域ID
        self.networkDomainId = ""

        # 目标计算对象的IP
        self.ip = ""

        # 需要在Agent上安装的模块的代码
        self.moduleCodes = []

    def convert_to_json_arg(self):
        return {
            "parentAgentId": self.parentAgentId,
            "agentInfos": [{
                "username": self.userName,
                "password": self.password,
                "installPath": self.installPath,
                "port": self.port,
                "networkDomainId": self.networkDomainId,
                "ips": [self.ip],
                "modules": self.moduleCodes}
            ],
            "mapping": []
        }


def get_default_network_domain(service_host, api_key):
    payload = {
        "conditions": {
            "items": [{
                "field": "classCode",
                "value": "NetworkDomain"
            }, {
                "field": "code",
                "value": "defaultZone"
            }]
        }
    }
    url = urlparse.urljoin('http://{}'.format(service_host),
                           'store/openapi/v2/resources/query')
    response = requests.post(url, json=payload, headers={'apikey': api_key},
                             timeout=5, verify=False)
    if response.status_code == 200 and len(
            response.json().get('dataList')) != 0:
        value = response.json().get('dataList')[0].get('id')
        return value
    else:
        raise Exception("Fail to get default network domain ID.")
