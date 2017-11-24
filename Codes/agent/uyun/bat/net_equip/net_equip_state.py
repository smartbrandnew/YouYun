# -*- coding: UTF-8 -*-
__author__ = 'fangjc'
__project__ = 'uyun'
__date__ = '2016/07/28'

# stdlib
from copy import deepcopy
from itertools import izip
import time

# project
from uyun.bat.snmp import Target, snmp_walk, snmp_get_oid
from uyun.bat.net_equip import (
    l1_analysis,
    RFC1213_OID,
    SYS_OBJECTED_MAP,
    ENTERPRISE_NUMBER,
    discover_type,
    discover_brand,
    Bdcom,
    HP,
    Linksys,
    Alliedtelesis,
    Neusoft,
    Angelltech,
    Nortel,
    Red_Giant,
    InternetSecurityOneLtd,
    Enterasys,
    Cisco,
    Micom,
    Array,
    Unkonwn,
    Maipu,
    Nokia,
    Netscreen,
    Netgear,
    Moxa,
    ZTE,
    Lenovo,
    Enterasys,
    Redware,
    Alcatel,
    Juniper,
    Bluecoat,
    H3C,
    Nsfocus,
    Fortinet,
    DigitalChina,
    Foundry,
    Opzoon,
    Airespace,
    Secgate,
    F5,
    Venustech,
    Nortel,
    Harbour,
    NetScaler,
    Extreme,
    Hillstone,
    Topsec,
    Centec,
    Sangfor,
    Juniper,
    Alteon,
    Secworld,
    HUAWEI,
    Future
)

PRODUCERS = {
    'Bdcom': Bdcom(),
    'HP': HP(),
    'Linksys': Linksys(),
    'Alliedtelesis': Alliedtelesis(),
    'Neusoft': Neusoft(),
    'Angelltech': Angelltech(),
    'Nortel': Nortel(),
    'Red-Giant': Red_Giant(),
    'InternetSecurityOneLtd': InternetSecurityOneLtd(),
    'Enterasys': Enterasys(),
    'Cisco': Cisco(),
    'Micom': Micom(),
    'Array': Array(),
    'Unkonwn': Unkonwn(),
    'Maipu': Maipu(),
    'Nokia': Nokia(),
    'Netscreen': Netscreen(),
    'Netgear': Netgear(),
    'Moxa': Moxa(),
    'ZTE': ZTE(),
    'Lenovo': Lenovo(),
    'Enterasys': Enterasys(),
    'Redware': Redware(),
    'Alcatel': Alcatel(),
    'Juniper': Juniper(),
    'Bluecoat': Bluecoat(),
    'H3C': H3C(),
    'Nsfocus': Nsfocus(),
    'Fortinet': Fortinet(),
    'DigitalChina': DigitalChina(),
    'Foundry': Foundry(),
    'Opzoon': Opzoon(),
    'Airespace': Airespace(),
    'Secgate': Secgate(),
    'F5': F5(),
    'Venustech': Venustech(),
    'Nortel': Nortel(),
    'Harbour': Harbour(),
    'NetScaler': NetScaler(),
    'Extreme': Extreme(),
    'Hillstone': Hillstone(),
    'Topsec': Topsec(),
    'Centec': Centec(),
    'Sangfor': Sangfor(),
    'Alteon': Alteon(),
    'Secworld': Secworld(),
    'HUAWEI': HUAWEI(),
    'Future': Future()
}


class NetEquipState(object):
    def __init__(self, producer, target):
        self.producer = PRODUCERS.get(producer)
        self.target = target
        self.cpu_oid_expression = ''
        self.mem_oid_expression = ''

        if self.producer:
            self.__get_cpu_men_oids()

    @property
    def cpu_idle(self):
        cpu_idle = 100
        if self.cpu_oid_expression:
            cpu_oids = self._oid_expression_parser(self.cpu_oid_expression)
            if len(cpu_oids) == 1:
                cpu_usage = snmp_get_oid(self.target, cpu_oids[0])
            else:
                values = []
                temp_expression = deepcopy(self.cpu_oid_expression)
                cpu_oids = self._lst_del_repeat(cpu_oids)
                for cpu_oid in cpu_oids:
                    values.append(str(snmp_get_oid(self.target, cpu_oid)))
                cpu_compute_equation = self._check_and_get(
                    *(temp_expression, cpu_oids, values))
                cpu_usage = l1_analysis(cpu_compute_equation).split('.')[0]
        if not cpu_usage:
            cpu_usage = 0
        cpu_idle -= cpu_usage
        return cpu_idle, cpu_usage

    @property
    def mem_usage(self):
        mem_usage = 0
        if self.mem_oid_expression:
            mem_oids = self._oid_expression_parser(self.mem_oid_expression)
            if len(mem_oids) == 1:
                mem_usage = snmp_get_oid(self.target, mem_oids[0])
            else:
                values = []
                temp_expression = deepcopy(self.mem_oid_expression)
                mem_oids = self._lst_del_repeat(mem_oids)
                for mem_oid in mem_oids:
                    values.append(str(snmp_get_oid(self.target, mem_oid)))
                mem_compute_equation = self._check_and_get(
                    *(temp_expression, mem_oids, values))
                mem_usage = l1_analysis(mem_compute_equation).split('.')[0]
        if not mem_usage:
            mem_usage = 0
        return mem_usage

    def __get_cpu_men_oids(self):
        cpu_parameters = self._effective_cpu_oids()
        mem_parameters = self._effective_mem_oids()
        if cpu_parameters:
            self.cpu_oid_expression = self._check_and_get(*cpu_parameters)
        else:
            self.cpu_oid_expression = "unknown"
        if mem_parameters:
            self.mem_oid_expression = self._check_and_get(*mem_parameters)
        else:
            self.mem_oid_expression = "unknown"

    def _effective_cpu_oids(self):
        count = 5
        while count >= 0:
            for value in self.producer.cpu.values():
                temp = self._oid_expression_parser(value)
                oid = temp[0]
                cpu_usage_lst = snmp_walk(self.target, oid)
                if cpu_usage_lst:
                    for i in cpu_usage_lst:
                        cpu_oid = str(i[0][0])
                        cpu_usage = i[0][-1]
                        if cpu_usage:
                            suffix = self._sub_strings(cpu_oid, oid)
                            return value, temp, [i+suffix for i in temp]
            time.sleep(1)
            count -= 1
        return None

    def _effective_mem_oids(self):
        count = 5
        while count >= 0:
            for value in self.producer.mem.values():
                temp = self._oid_expression_parser(value)
                oid = temp[0]
                mem_usage_lst = snmp_walk(self.target, oid)
                if mem_usage_lst:
                    for i in mem_usage_lst:
                        mem_oid = str(i[0][0])
                        mem_usage = i[0][-1]
                        if mem_usage:
                            suffix = self._sub_strings(mem_oid, oid)
                            return value, temp, [i+suffix for i in temp]
            time.sleep(1)
            count -= 1
        return None

    def _oid_expression_parser(self, oid_expression):
        oids = []
        temp_expression = deepcopy(oid_expression)
        if ' ' not in temp_expression:
            oids.append(temp_expression)
        else:
            head = "1.3.6.1.4.1."
            operators = ['+', '-', '*', '/', '(', ')']
            for operator in operators:
                temp_expression = temp_expression.replace(operator, '')
            for expression in temp_expression.split(' '):
                if head in expression:
                    oids.append(expression)
        return oids

    def _sub_strings(self, subtrahend, minuend):
        suffix = subtrahend.replace(minuend, '')
        return suffix

    def _check_and_get(self, *args):
        oid_expression, old_oids, new_oids = args
        old_oids = self._lst_del_repeat(old_oids)
        new_oids = self._lst_del_repeat(new_oids)
        for old_oid, new_oid in izip(old_oids, new_oids):
            oid_expression = oid_expression.replace(old_oid, new_oid)
        return oid_expression

    def _lst_del_repeat(self, lst):
        result = []
        for i in lst:
            if i not in result:
                result.append(i)
        return result
