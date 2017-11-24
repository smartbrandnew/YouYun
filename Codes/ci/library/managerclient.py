# coding=utf-8
# This module is used to communicate with Ant-Manager


class ManagerClient(object):
    def login_server(self, user_name, password):
        """
        Login uyun server.
        "token" in response can be used for executing other Manager operations\
            that require authentication/authorization.
        :return return a (json) dictionary of response content if success,
            else throw exception.
            Example:
                {
                    "errCode": null,
                    "message": null,
                    "data": {
                        "tenantId": "e10adc3949ba59abbe56e057f20f88dd",
                        "language": "zh_CN",
                        "userId": "e10adc3949ba59abbe56e057f20f88dd",
                        "token": "757ef7bbaa94dfbafc3d9362c95b78cea252af7c8794266f73440543e0c1acdc"
                    },
                    "mode": "offline",
                    "language": "zh_CN"
                }
        """
        # TODO: implement me
        pass

    def list_network_domain(self, user_name, password):
        """
        Get all Network Domains.
        :param user_name: used to login uyun server
        :param password: used to login uyun server
        :return: return a (json) dictionary of response content if success,
            else throw exception.
            Example:
                [
                    {
                        "deviceNum": 0,
                        "displayMark": "cyan",
                        "id": "59db94f725e84023bf02c397",
                        "name": "默认域"
                    }
                ]
        """
        # TODO: implement me
        pass

    def install_agent(self, user_name, password, agent_install_arg):
        """
        Send a request to install Agent.
        :param user_name: used to login uyun server
        :param password: used to login uyun server
        :param agent_install_arg: an AgentInstallationArgument
        :return: return True if success, else throw exception
        """
        # TODO: implement me
        pass

    def uninstall_agent(self, user_name, password, parent_agent_id, agent_ids):
        """
        Send to request to uninstall Agent.
        :param user_name: used to login uyun server
        :param password: used to login uyun server
        :param agent_ids: a list of Agent ID,
            specifies which Agents should be uninstalled
        :return: return True if success, else throw exception
        """
        # TODO: implement me
        pass

    def upgrade_agent(self, user_name, password, parent_agent_id, agent_ids):
        """
        Send a request to upgrade Agent.
        :param user_name: used to login uyun server
        :param password: used to login uyun server
        :param agent_ids: a list of Agent ID,
            specifies which Agents should be upgraded
        :return: return True if success, else throw exception
        """
        # TODO: implement me
        pass

    def get_agent_by_compute_object_id(self, user_name, password,
                                       compute_object_id):
        """
        Get Agent info by ComputeObject ID
        :param user_name: used to login uyun server
        :param password: used to login uyun server
        :return: return a (json) dictionary of response content if success,
            else throw exception.
            Example:
                [
                    {
                        "id": "u82wgsyauwixksldoeydhsnak294762",
                        "agentId": "agentId",
                        "name": "节点名称",
                        "agentName": "所属管理器",
                        "runStatus": "1",
                        "installPath": "root",
                        "agentUpdateTime": "2017-06-06 12:23:12",
                        "version": "2.0.0",
                        "osType": "操作系统",
                        "cpuCores": "cpu核数",
                        "memoryCapacity": "内存容量",
                        "storageSize": "存储容量",
                        "parentNode": "上级节点",
                        "desc": "描述",
                        "netZone": [
                            {
                                "name": "默认域"
                            }
                        ],
                        "ip": "ip",
                        "cmdbUrl": "查看配置项CMDB链接，未安装CMDB，该url为空",
                        "haveChildNode": "1",
                        "ipAddresses": [
                            "192.168.15.201",
                            "192.168.15.201",
                            "192.168.15.203"
                        ]
                    }
                ]
        """
        # TODO: implement me
        pass

    def install_modules(self, user_name, password, parent_agent_id, agent_ids,
                        module_codes):
        """
        Send a request to install modules.
        :param user_name: used to login uyun server
        :param password: used to login uyun server
        :param parent_agent_id:
        :param agent_ids: a list of Agent ID,
            specifies modules should be installed to which Agents
        :param module_codes: a list of module codes,
            specifies which modules should be installed
        :return: return True if success, else throw exception
        """
        # TODO: implement me
        pass

    def uninstall_modules(self, user_name, password, parent_agent_id, agent_ids,
                          module_codes):
        """
        Send a request to uninstall modules.
        :param user_name: used to login uyun server
        :param password: used to login uyun server
        :param agent_ids: a list of Agent ID,
            specifies which Agents should uninstall modules
        :param module_codes: a list of module codes,
            specifies which modules should be uninstalled
        :return: return True if success, else throw exception
        """
        # TODO: implement me
        pass

    def upgrade_modules(self, user_name, password, parent_agent_id, agent_ids,
                        module_codes):
        """
        Send a request to upgrade modules.
        :param user_name: used to login uyun server
        :param password: used to login uyun server
        :param agent_ids: a list of Agent ID,
            specifies which Agents should upgrade modules
        :param module_codes: a list of module codes,
            specifies which modules should be upgraded
        :return: return True if success, else throw exception
        """
        # TODO: implement me
        pass

    def start_modules(self, user_name, password, parent_agent_id, agent_ids,
                      module_codes):
        """
        Send a request to start modules.
        :param user_name: used to login uyun server
        :param password: used to login uyun server
        :param agent_ids: a list of Agent ID,
            specifies which Agents should start modules
        :param module_codes: a list of module codes,
            specifies which modules should be started
        :return: return True if success, else throw exception
        """
        # TODO: implement me
        pass

    def stop_modules(self, user_name, password, parent_agent_id, agent_ids,
                     module_codes):
        """
        Send a request to stop modules.
        :param user_name: used to login uyun server
        :param password: used to login uyun server
        :param agent_ids: a list of Agent ID,
            specifies which Agents should stop modules
        :param module_codes: a list of module codes,
            specifies which modules should be stopped
        :return: return True if success, else throw exception
        """
        # TODO: implement me
        pass

    def get_modules(self, user_name, password, agent_ids):
        """
        Get modules by Agent ID
        :param user_name: used to login uyun server
        :param password: used to login uyun server
        :param agent_ids: a list of Agent ID,
            specifies which Agents' modules should be retrieved
        :return: return a (json) dictionary of response content if success,
            else throw exception
            Example:
                [
                    {
                        "code": "local-monitor",
                        "name": "本地监控",
                        "status": "2",
                        "version": "2.0.0"
                    },
                    {
                        "code": "remote-monitor",
                        "name": "远程监控",
                        "status": "1",
                        "version": "1.0.0"
                    }
                ]
        """
        # TODO: implement me
        pass


class AgentInstallationArgument(object):
    def __init__(self):
        self.parent_agent_id = ""
        self.agent_info_list = []  # A list of AgentInstallationInfo
        self.mapping = []  # Mapping of IP and Compute Object ID

    def convert_to_json_arg(self):
        """
        Convert to a (json) dictionary for Ant-Manager.
        """
        # TODO: implement me
        pass


class AgentInstallationInfo(object):
    def __init__(self):
        self.user_name = ""  # Used to login target ComputeObject
        self.password = ""  # Used to login target ComputeObject
        self.install_path = ""  # Agent will be installed in this path
        self.port = ""  # Used to login target ComputeObject (remotely)
        self.network_domain_id = ""  # Agent will be installed in this NetworkDomain
        self.ips = []  # IP of ComputeObjects where Agent will be installed.
        self.modules = []  # Module codes
