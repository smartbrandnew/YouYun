# !/C:/Python27
# -*- coding: UTF-8 -*-
__author__ = 'fangjc'
__project__ = 'uyun'
__date__ = '2016/8/1'


# stdlib
from itertools import izip
import time

# prject
from uyun.bat.snmp import Target, snmp_walk, snmp_get_oid


# SNMP WALK PORTS
PORT_STATUSES = "1.3.6.1.2.1.2.2.1.8"
PORT_OCTERS_OUT = '1.3.6.1.2.1.2.2.1.16'
PORT_OCTERS_IN = '1.3.6.1.2.1.2.2.1.10'
PORT_BANDWIDTHS = '1.3.6.1.2.1.2.2.1.5'
PORT_NAMES = '1.3.6.1.2.1.2.2.1.2'

class NetEquipPort(object):
    def __init__(self, target):
        self.target = target
        self.oid_names = {}
        self.oid_bandwidths = {}
        self.port_statuses = {}
        self.ports_out_rate = {}
        self.ports_in_rate = {}
        self.port_out_octers = {}
        self.port_in_octers = {}
        self.port_in_bandwidth_pct = {}
        self.port_out_bandwidth_pct = {}
        self.port_bandwidth_pct = {}

        self.in_rates_collect_time = 0
        self.out_rates_collect_time = 0

        self.__collect_ports_names()
        self.__collect_ports_bandwidths()
        self.__port_in_octers()
        self.__port_out_octers()

    def __collect_ports_names(self):
        port_names = snmp_walk(self.target, PORT_NAMES)
        for port_name in port_names:
            oid = str(port_name[0][0]).split('.')[-1]
            name = str(port_name[0][1])
            self.oid_names[oid] = name

    def __collect_ports_bandwidths(self):
        port_bandwidths = snmp_walk(self.target, PORT_BANDWIDTHS)
        for port_bandwidth in port_bandwidths:
            oid = str(port_bandwidth[0][0]).split('.')[-1]
            bandwidth = int(port_bandwidth[0][1])
            self.oid_bandwidths[oid] = bandwidth

    @property
    def status(self):
        statuses = snmp_walk(self.target, PORT_STATUSES)
        for status in statuses:
            oid = str(status[0][0]).split('.')[-1]
            state = int(status[0][1])
            self.port_statuses[self.oid_names[oid]] = state
        return self.port_statuses

    # kbps
    @property
    def in_rates(self):
        this_collect_time = time.time()
        in_octers = snmp_walk(self.target, PORT_OCTERS_IN)
        if not in_octers:
            return None
        for in_octer in in_octers:
            oid = str(in_octer[0][0]).split('.')[-1]
            octer = int(in_octer[0][1])
            self.port_in_octers[oid]["pre"] = self.port_in_octers[oid]["now"]
            self.port_in_octers[oid]["now"] = octer
            period = this_collect_time - self.in_rates_collect_time
            if self.port_in_octers[oid]["now"] < self.port_in_octers[oid]["pre"]:
                in_rate = \
                    round(
                        (self.port_in_octers[oid]["now"] * 8) / (1000 * period), 2)
            else:
                in_rate = \
                    round(
                        ((self.port_in_octers[oid]["now"] -
                         self.port_in_octers[oid]["pre"])*8)/(1000*period), 2
                    )
            self.ports_in_rate[self.oid_names[oid]] = in_rate
        # 第一次采集的端口速率有误差 不上报 返回None
        if not self.in_rates_collect_time:
            self.in_rates_collect_time = this_collect_time
            self.ports_in_rate = {}
            return None
        return self.ports_in_rate

    def __port_in_octers(self):
        if not self.port_in_octers:
            for key in self.oid_names.keys():
                self.port_in_octers[key] = {"pre": 0, "now": 0}

    # kbps
    @property
    def out_rates(self):
        this_collect_time = time.time()
        out_octers = snmp_walk(self.target, PORT_OCTERS_OUT)
        if not out_octers:
            return None
        for out_octer in out_octers:
            oid = str(out_octer[0][0]).split('.')[-1]
            octer = int(out_octer[0][1])
            self.port_out_octers[oid]["pre"] = self.port_out_octers[oid]["now"]
            self.port_out_octers[oid]["now"] = octer
            period = this_collect_time - self.in_rates_collect_time
            if self.port_out_octers[oid]["now"] < self.port_out_octers[oid]["pre"]:
                out_rate = \
                    round(
                        (self.port_out_octers[oid]["now"] * 8) / (1000 * period), 2)
            else:
                out_rate = \
                    round(
                        ((self.port_out_octers[oid]["now"] -
                         self.port_out_octers[oid]["pre"])*8)/(1000*period), 2
                    )
            self.ports_out_rate[self.oid_names[oid]] = out_rate
        # 第一次采集的端口速率有误差 不上报 返回None
        if not self.out_rates_collect_time:
            self.out_rates_collect_time = this_collect_time
            self.ports_out_rate = {}
            return None
        return self.ports_out_rate

    def __port_out_octers(self):
        if not self.port_out_octers:
            for key in self.oid_names.keys():
                self.port_out_octers[key] = {"pre": 0, "now": 0}

    @property
    def in_bandwidth_pcts(self):
        # 第一次采集的端口带宽占用率有误差 不上报 返回None
        if not self.ports_in_rate:
            return None
        for key, value in self.oid_bandwidths.items():
            if value:
                self.port_in_bandwidth_pct[self.oid_names[key]] = \
                    round(
                        (self.ports_in_rate[self.oid_names[key]] / (value/1000)) * 100, 2
                    )
        return self.port_in_bandwidth_pct

    @property
    def out_bandwidth_pcts(self):
        # 第一次采集的端口带宽占用率有误差 不上报 返回None
        if not self.ports_out_rate:
            return None
        for key, value in self.oid_bandwidths.items():
            if value:
                self.port_out_bandwidth_pct[self.oid_names[key]] = \
                    round(
                        (self.ports_out_rate[self.oid_names[key]] / (value/1000)) * 100, 2
                    )
        return self.port_out_bandwidth_pct

    @property
    def bandwidth_pcts(self):
        if not self.port_in_bandwidth_pct or not self.port_out_bandwidth_pct:
            return None
        for key, value in self.port_in_bandwidth_pct.items():
            if value > self.port_out_bandwidth_pct[key]:
                self.port_bandwidth_pct[key] = value
            else:
                self.port_bandwidth_pct[key] = self.port_out_bandwidth_pct[key]
        return self.port_bandwidth_pct

if __name__ == '__main__':
    ip = '10.1.1.1'
    community = 'broadapublic'
    target = Target(ip, community)

    net_equip_ports = NetEquipPort(target)
    print(net_equip_ports.oid_names)
    print(net_equip_ports.status)

    print(net_equip_ports.in_rates)
    time.sleep(5)
    print(net_equip_ports.in_rates)

    print(net_equip_ports.out_rates)
    time.sleep(5)
    print(net_equip_ports.out_rates)
