# -*- coding: UTF-8 -*-
# !/C:/Python27

# 对 SNMP v1/v2c v3 的各类操作方法的参数进行封装
# 统一方法 减少由于参数的变化出现大量的方法重载


class Target():
    def __init__(self,
                 ip,
                 community=None,
                 port=161,
                 timeout=1,
                 retries=1,
                 user=None,
                 auth_key=None,
                 encrypt_key=None,
                 auth_proto='sha',
                 encrypt_proto='aes128',
                 ):
        self.ip = ip
        self.community = community
        self.user = user
        self.auth_key = auth_key
        self.encrypt_key = encrypt_key
        self.auth_proto = auth_proto
        self.encrypt_proto = encrypt_proto
        self.port = port
        self.retries = retries
        self.timeout = timeout
        self.last_time_basis_info_collected = 0
        self.last_time_performance_collected = 0
        self.last_time_port_status_collected = 0
        self.last_time_port_rate_collected = 0

    def get_device(self):
        if self.community:
            snmp_device = (self.ip, self.community, self.port)
        else:
            snmp_device = (self.ip, self.port)
        return snmp_device

    def get_user(self):
        if self.user and self.auth_key and self.encrypt_key:
            snmp_user = (self.user, self.auth_key, self.encrypt_key)
            return snmp_user
        else:
            return None

    def get_auth_proto(self):
        return self.auth_proto

    def get_encrypt_proto(self):
        return self.encrypt_proto

    def get_display_errors(self):
        return self.display_errors

    def get_retries(self):
        return self.retries

    def get_timeout(self):
        return self.timeout
