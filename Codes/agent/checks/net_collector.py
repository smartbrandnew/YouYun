# -*- coding: UTF-8 -*-

# stdlib
from __future__ import division
import cPickle as pickle
import hashlib
import collections
from itertools import izip
import logging
import os
from Queue import PriorityQueue
import tempfile
from threading import Thread
import time
from multiprocessing.dummy import Pool as ThreadPool

# 3p
from IPy import IP

# project
from checks.check_status import EmitterStatus
from config import _windows_commondata_path
from util import get_os
from utils.logger import log_exceptions
from utils.pidfile import PidFile
from utils.platform import Platform


# uyun
from uyun.bat.ping import PingRequest
from uyun.bat.ping.pinger import PingListener
from uyun.bat.snmp import snmp_get_oid, snmp_walk, Target
from uyun.bat.net_equip import discover_type, discover_brand, NetEquipState, NetEquipPort

__author__ = 'fangjc'
__project__ = 'monitor-agent'
__date__ = '2016/04/15'

log = logging.getLogger(__name__)

# 用于记录指定时间窗口内上传的指标数（秒）
RECORD_INTERVAL = 30
# 默认线程池大小
POOL_SIZE = 8
# 网络设备基本信息上传间隙（秒）
BASIS_INFO_INTERVAL = 4*60*60
# 默认完整网段扫描间隙（秒）
NET_DISCOVER_INTERVAL = 86400
# 设备在线状态指标 采集间隙（秒）
ONLINE_STATUS_COLLECT_INTERVAL = 30
# 默认cpu ram 性能指标 采集间隙（秒）
PERFORMANCE_COLLECT_INTERVAL = 30
# 默认端口状态采集间隙（秒）
PORT_STATUS_COLLECT_INTERVAL = 30
# 默认端口速率采集间隙（秒）
PORT_RATE_COLLECT_INTERVAL = 120
# 网段掩码数值对应字典
NET_MASK = {"255.0.0.0": "8", "255.255.0.0": "16", "255.255.255.0": "24"}


class AgentPayload(collections.MutableMapping):
    METADATA_KEYS = frozenset([
        'meta',
        'tags',
        'host-tags',
        'systemStats',
        'agent_checks',
        'gohai',
        'external_host_tags'])
    DUPLICATE_KEYS = frozenset([
        'apiKey',
        'agentVersion'])

    # ENDPOINT 常量 此关键字用于构成上传链接
    COMMON_ENDPOINT = ''
    DATA_ENDPOINT = 'metrics'
    METADATA_ENDPOINT = 'metadata'
    PING_ENDPOINT = 'ping'

    def __init__(self):
        self.data_payload = dict()
        self.meta_payload = dict()

    def payload(self):
        res = self.data_payload.copy()
        res.update(self.meta_payload)
        return res

    payload = property(payload)

    def __getitem__(self, key):
        if key in self.METADATA_KEYS:
            return self.meta_payload[key]
        return self.data_payload[key]

    def __setitem__(self, key, value):
        if key in self.DUPLICATE_KEYS:
            self.data_payload[key] = value
            self.meta_payload[key] = value
        elif key in self.METADATA_KEYS:
            self.meta_payload[key] = value
        else:
            self.data_payload[key] = value

    def __delitem__(self, key):
        if key in self.DUPLICATE_KEYS:
            del self.data_payload[key]
            del self.meta_payload[key]
        elif key in self.METADATA_KEYS:
            del self.meta_payload[key]
        else:
            del self.data_payload[key]

    def __iter__(self):
        for item in self.data_payload:
            yield item

        for item in self.meta_payload:
            yield item

    def __len__(self):
        return len(self.data_payload) + len(self.meta_payload)

    def emit(
        self,
        log,
        config,
        emitters,
        continue_running
    ):
        statuses = []

        def _emit_payload(payload, endpoint):
            statuses = []
            for emitter in emitters:
                if not continue_running:
                    return statuses
                name = emitter.__name__
                emitter_status = EmitterStatus(name)

                try:
                    emitter(payload, log, config, endpoint)
                except Exception as e:
                    log.exception('Error running emitter: %s' %
                                  emitter.__name__)
                    emitter_status = EmitterStatus(name, e)

                statuses.append(emitter_status)

            return statuses

        statuses.extend(
            _emit_payload(self.data_payload, self.PING_ENDPOINT))
        return statuses


class NetCollector(object):
    '''
    TestCollector类负责扫描网段（每一个ip提交一次ping请求）
    对ping通的ip进行snmp查询，确定该ip的设备类型及厂商
    实现发现该设备cpu、ram、端口速率功能
    最后将获得的数据填充payload数据结构
    通过emitter（发射器）进行上传
    '''
    def __init__(self, agent_config, config, emitters, ps):
        self.agent_config = agent_config
        self.config = config
        self.targets = set()
        self.task_queue = PriorityQueue()
        self.os = get_os()
        self.emitters = emitters
        self.run_count = 0
        self.continue_running = True
        self.md5_lst = []
        self.net_equip_details = {}
        self.net_equip_states = {}
        self.net_equip_ports = {}
        self.net_target_type = {}

        # 获得网络设备节点信息文件路径
        self.net_equip_details_path = self._get_pickle_path()

        # 网段设置将从/conf.d/net_collector.yaml中获取参数来建立
        ip_segments = self.config['network_segment'].split(',')
        self.ips = generate_ips(ip_segments)
        log.debug(self.ips)

        # 尝试读取网络设备节点信息文件
        try:
            log.debug("Trying to fetch net_equip_details")
            f = open(self.net_equip_details_path, 'rb')
            d = pickle.load(f)
            f.close()
            self.net_equip_details.update(d)
        except Exception as e:
            log.info('File does not exist, it will be created later %s.' % e)

        self.ps = ps
        self.listener = NetPingListener(self.net_equip_details)

        # 完整网段扫描间隙时间，即网络发现，单位：秒
        self.net_discover_interval = \
            int(
                self.config.get(
                    'net_discover_interval', NET_DISCOVER_INTERVAL))
        # 设备在线状态指标采集上传间隙时间，单位：秒，默认30秒1次
        self.online_status_collect_interval = int(
            self.config.get(
                'online_status_collect_interval',
                ONLINE_STATUS_COLLECT_INTERVAL))
        # 设备性能指标采集上传间隙时间，单位：秒，默认30秒1次
        self.performance_collect_interval = int(
            self.config.get(
                'performance_collect_interval',
                PERFORMANCE_COLLECT_INTERVAL))
        # 设备端口状态采集上传间隙时间，单位：秒，默认30秒1次
        self.port_status_collect_interval = int(
            self.config.get(
                'port_status_collect_interval',
                PORT_STATUS_COLLECT_INTERVAL))
        # 设备端口速率采集上传间隙时间，单位：秒，默认120秒1次
        self.port_rate_collect_interval = int(
            self.config.get(
                'port_rate_collect_interval',
                PORT_RATE_COLLECT_INTERVAL))

        # 线程池大小
        self.pool_size = int(self.config.get('thread', POOL_SIZE))

        self.last_time_net_discovered = 0
        self.last_time_basis_info_collected = 0
        self.last_time_online_status_collected = 0
        self.sum_responses = 0
        self.pinged_ips = set()

        # 用于统计各指标采集的数目
        self.ping_metrics = 0
        self.basis_metrics = 0
        self.performance_metrics = 0
        self.port_status_metrics = 0
        self.port_rate_metrics = 0
        self.last_time_record = 0

        self.ping_metrics_temp = 0
        self.basis_metrics_temp = 0
        self.performance_metrics_temp = 0
        self.port_status_metrics_temp = 0
        self.port_rate_metrics_temp = 0

    # 结束
    def stop(self):
        self.continue_running = False

    # 核心方法 启动
    @log_exceptions(log)
    def run(self):
        # 使agent每次启动都会上传网络设备基本信息 而后4小时上传一次
        if self.net_equip_details:
            for value in self.net_equip_details.values():
                target = value.get('target')
                if target and target != "unknown":
                    target.last_time_basis_info_collected = 0

        ping_timeout = int(self.config['ping'][0]['timeout'])
        ping_retries = int(self.config['ping'][0]['retries'])

        '''
        网段扫描
        获取并保存网络设备节点信息 上传网络设备在线状态
        因为这个指标获取速度很快 不需要多线程处理
        实例化每一个网络设备的Target实例并放置于targets集合中
        '''
        network_scan = Thread(
            target=self.net_segment_scan,
            args=(ping_timeout, ping_retries))
        network_scan.start()

        '''
        从target_queue队列取target
        根据采集周期往task_queue队列中放置采集任务
        '''
        task_generation = Thread(target=self.collect_task_producer)
        task_generation.start()

        '''
        从task_queue获取task
        线程池对多任务进行并发处理
        '''
        pool = ThreadPool(self.pool_size)
        while self.continue_running:
            task = self.task_queue.get()
            try:
                pool.apply(task[1][0], (task[1][1],))
            except Exception as e:
                log.warn('Task {0} error {1}'.format(task, e))
            '''将过去一段时间(RECORD_INTERVAL)
            所发送的各指标数目记录到日志中'''
            self.metrics_amount_record()

        pool.close()
        pool.join()

    def net_segment_scan(self, ping_timeout, ping_retries):
        while self.continue_running:
            # flag旗帜位代表是否将net_equip_details字典写入文件
            # 判断是否可以从文件中获取net_equip_details字典变量
            if self.net_equip_details:
                create = False
                flag = False
            else:
                create = True
                flag = True

            complete_scan_period = time.time() - self.last_time_net_discovered
            incomplete_scan_period = time.time() - self.last_time_online_status_collected
            if (
                complete_scan_period >
                self.net_discover_interval or not self.sum_responses
            ):
                log.info("Net discovering...")
                log.info("net_equip_details %s" % str(self.net_equip_details))
                self.ping(self.ips, ping_timeout, ping_retries)
                self.sum_responses = \
                    sum([i['response'] for i in self.net_equip_details.values()])
                if not self.sum_responses:
                    log.warn("There are no ping responses, check your network")
                    time.sleep(60)
                    continue
                self.last_time_net_discovered = time.time()
            elif self.online_status_collect_interval and \
                    incomplete_scan_period > self.online_status_collect_interval:
                log.info(
                    "Start ping for ips which pinged before: %s" %
                    str(self.pinged_ips)
                )
                self.ping(self.pinged_ips, ping_timeout, ping_retries)
                self.sum_responses = \
                    sum([i['response'] for i in self.net_equip_details.values()])
                if not self.sum_responses:
                    log.warn("There are no ping responses, check your network")
                    time.sleep(self.online_status_collect_interval)
                    continue
                self.get_ping()
                self.last_time_online_status_collected = time.time()

            for ip, value in self.net_equip_details.items():
                if not value['response']:
                    self.net_equip_details.pop(ip)

            # net_equip_details字典变量存储所有必须信息 当从文件获取失败 新建
            if create:
                log.info("Failed to fetch net_equip_details from file, now creating")
                log.info("Maybe a little slow, please stand by...")
                for ip, value in self.net_equip_details.items():
                    if value['response']:
                        self.pinged_ips.add(ip)
                        target = self.get_target(ip)
                        if target != "unknown":
                            self.get_type_producer(target)
                            """
                            获得设备的md5值 如果md5值重复即已存在于md5列表中
                            此设备在节点信息字典中的target值为'unknown'
                            """
                            md5 = self.get_min_mac_md5(target)
                            if md5 and md5 in self.md5_lst:
                                target = "unknown"
                            elif md5 and md5 not in self.md5_lst:
                                self.md5_lst.append(md5)
                                self.targets.add(target)
                        value['target'] = target

            for ip, value in self.net_equip_details.items():
                if value['response']:
                    self.pinged_ips.add(ip)
                    target = value.get('target')
                    if not target:
                        target = self.get_target(ip)
                        if target != "unknown":
                            flag = True
                            self.get_type_producer(target)
                            md5 = self.get_min_mac_md5(target)
                            if md5 and md5 in self.md5_lst:
                                target = "unknown"
                            elif md5 and md5 not in self.md5_lst:
                                self.md5_lst.append(md5)
                        value['target'] = target
                    if target != "unknown":
                        self.targets.add(target)

            # 当将必要信息填充net_equip_details变量中或全网段扫描更新该变量后
            # 存储该字典变量到文件中
            if flag:
                log.info("pinged_ips %s" % str(self.pinged_ips))
                log.info("net_equip_details %s" % str(self.net_equip_details))
                try:
                    f = open(self.net_equip_details_path, 'wb')
                    pickle.dump(self.net_equip_details, f)
                    f.close()
                except IOError:
                    log.warn('Save net_equip_details failed.')
                    return
            # 暂停循环5秒
            time.sleep(5)

    def collect_task_producer(self):
        funcs = {
            'basis_info': self.get_basis_info,
            'cpu_ram': self.get_cpu_ram_usage,
            'port_rate': self.get_port_rate,
            'port_status': self.get_port_status
        }

        while self.continue_running:
            if self.targets:
                for target in self.targets:
                    arrange_task(
                        funcs,
                        target,
                        self.task_queue,
                        BASIS_INFO_INTERVAL,
                        self.performance_collect_interval,
                        self.port_status_collect_interval,
                        self.port_rate_collect_interval
                    )
            time.sleep(5)

    # 提交ping申请
    def ping(self, ips, timeout, retries):
        if timeout and retries:
            for ip in ips:
                request = PingRequest(ip, timeout, retries)
                self.ps.ping(request, self.listener)
        else:
            # 如果ping参数没有在配置文件中配置，实例化会使用默认参数
            # timeout=1000, retries=4
            for ip in ips:
                request = PingRequest(ip)
                self.ps.ping(request, self.listener)

        length = len(ips)
        while True:
            responses = self.net_equip_details.values()
            if len(responses) < length:
                time.sleep(1)
            else:
                break


    def get_basis_info(self, target):
        '''
        提交网络设备基本信息
       默认4小时提交一次 时间间隙是在代码中写死的
       网络设备基本信息指标和其他上传指标以md5值相互关联
        '''
        ip = target.ip
        storage = self.net_equip_details[ip]
        if not storage["type"]:
            return
        storage = self.net_equip_details[ip]
        payload = AgentPayload()
        self._build_payload(payload)
        payload['netEquipment'] = {
            "ip": ip,
            "type": storage.get('type'),
            "producer": storage.get('producer'),
            "host": storage.get('host_name'),
            "descr": storage.get('descr')
        }
        payload['identity'] = storage.get('md5')

        payload.emit(
            log,
            self.agent_config,
            self.emitters,
            self.continue_running)

        self.basis_metrics += 1

    # 建立payload的基本结构
    def _build_payload(self, payload):
        now = time.time()

        payload['collection_timestamp'] = now
        payload['agentVersion'] = self.agent_config['version']
        payload['apiKey'] = self.agent_config['api_key']
        payload['net_collector_tags'] = []
        if self.config.get('tags', None) is not None:
            payload['net_collector_tags'].extend(
                [tag.strip() for tag in self.config['tags'].split(",")])

    def get_target(self, ip):
        '''
        获得一个指定ip的snmp target实例

        Args:
            ip: ip地址，字符串。
        '''
        version, target_dict = self.get_snmp_taregts(ip)

        for key, value in target_dict.items():
            if version in key and value:
                for target in value:
                    type = discover_type(target)
                    if type:
                        log.debug('IP {0} type {1}'.format(target.ip, type))
                        self.net_target_type[target] = type
                        return target

        for key, value in target_dict.items():
            if version not in key and value:
                for target in value:
                    type = discover_type(target)
                    if type:
                        log.debug('IP {0} type {1}'.format(target.ip, type))
                        self.net_target_type[target] = type
                        return target

        return "unknown"

    def get_snmp_taregts(self, ip):
        '''
        从net_equip.yaml文件中获取参数建立targets

        Args:
            ip: ip地址，字符串。

        Returns:
            snmp_version: 配置文件优先指定的snmp版本，字符串。
            targets: 由配置文件中设定的snmp参数建立的targets字典，
                以snmp版本号为键，列表为值。
        '''
        targets = {}
        snmp_config = self.config['snmp'][0]
        snmp_version = snmp_config['version']
        # pysnmp timeout 单位为秒
        timeout = int(snmp_config['timeout'])/1000
        port = int(snmp_config['port'])
        retries = int(snmp_config['retries'])

        v1_v2c_targets = []
        for v1_v2c_parameters in snmp_config['v1_v2c_parameters']:
            community = v1_v2c_parameters['community']
            if community:
                target = Target(ip=ip,
                                community=community,
                                port=port,
                                timeout=timeout,
                                retries=retries)
                v1_v2c_targets.append(target)

        v3_targets = []
        for v3_parameters in snmp_config['v3_parameters']:
            user = v3_parameters['securityUser']
            auth_key = v3_parameters['authPassword']
            encrypt_key = v3_parameters['privPassword']
            auth_proto = v3_parameters['authProtocol']
            encrypt_proto = v3_parameters['privProtocol']
            if user and auth_key and encrypt_key and auth_proto \
                    and encrypt_proto:
                target = Target(ip=ip,
                                port=port,
                                timeout=timeout,
                                retries=retries,
                                user=user,
                                auth_key=auth_key,
                                encrypt_key=encrypt_key,
                                auth_proto=auth_proto,
                                encrypt_proto=encrypt_proto)
                v3_targets.append(target)

        targets.update({'v1_v2c': v1_v2c_targets})
        targets.update({'v3': v3_targets})
        return snmp_version, targets

    # 获取网络设备型号与产商
    def get_type_producer(self, target):
        ip = target.ip
        host_name = snmp_get_oid(target, '1.3.6.1.2.1.1.5.0')
        if not host_name:
            host_name = ip
        else:
            host_name = str(host_name)
        kind = self.net_target_type[target]
        producer = discover_brand(target)
        self.net_equip_details[ip].update(
            {
                'host_name': host_name,
                'type': kind,
                'producer': producer
            }
        )
        if self.net_equip_details[ip]['type']:
            descr = snmp_get_oid(target, '.1.3.6.1.2.1.1.1.0').prettyPrint()
            descr = descr.replace('\r\n', ', ')
            self.net_equip_details[ip].update(
                {
                    'descr': descr
                }
            )

    # 单独上传网络设备ping响应时间
    def get_ping(self):
        for ip, storage in self.net_equip_details.items():
            if not storage.get("type"):
                continue
            payload = AgentPayload()
            self._build_payload(payload)
            payload['metrics'] = []
            payload['identity'] = storage.get('md5')
            payload['metrics'].append(
                [
                    "system.net.ping_response_time",
                    payload['collection_timestamp'],
                    storage['response'],
                    {}
                ]
            )

            payload.emit(
                log,
                self.agent_config,
                self.emitters,
                self.continue_running)

            self.ping_metrics += 1

            log.debug("Get online status data for %s completed" % ip)

    # 获取网络设备cpu与ram使用率
    # jira单要求上传网络设备cpu空闲率
    # 见http://jira.uyunsoft.cn/browse/BAT-200
    def get_cpu_ram_usage(self, target):
        ip = target.ip
        storage = self.net_equip_details[ip]
        if not storage["type"]:
            return
        payload = AgentPayload()
        self._build_payload(payload)
        payload['metrics'] = []
        payload['identity'] = storage.get('md5')

        net_equip_state = self.net_equip_states.get(ip)
        if not net_equip_state:
            net_equip_state = NetEquipState(storage["producer"], target)
            self.net_equip_states[ip] = net_equip_state

        if net_equip_state.producer:
            if net_equip_state.cpu_oid_expression != "unknown":
                cpu_idle, cpu_usage = net_equip_state.cpu_idle
                payload['metrics'].append(
                    [
                        "system.cpu.idle",
                        payload['collection_timestamp'],
                        str(cpu_idle),
                        {}
                    ]
                )

                payload['metrics'].append(
                    [
                        "system.cpu.pct_usage",
                        payload['collection_timestamp'],
                        str(cpu_usage),
                        {}
                    ]
                )
                log.debug("ip %s cpu_idle %s" % (ip, cpu_idle))

            if net_equip_state.mem_oid_expression != "unknown":
                ram_usage = net_equip_state.mem_usage
                payload['metrics'].append(
                    [
                        "system.mem.pct_usage",
                        payload['collection_timestamp'],
                        str(ram_usage),
                        {}
                    ]
                )
                log.debug("ip %s mem.pct_used %s" % (ip, str(ram_usage)))

        if payload['metrics']:
            payload.emit(
                    log,
                    self.agent_config,
                    self.emitters,
                    self.continue_running)

            self.performance_metrics += 1

            log.debug("Get cpu ram data for %s completed" % ip)

    # 获取端口状态
    def get_port_status(self, target):
        ip = target.ip
        storage = self.net_equip_details[ip]
        if not storage["type"]:
            return
        payload = AgentPayload()
        self._build_payload(payload)
        payload['metrics'] = []
        payload['identity'] = storage.get('md5')

        net_equip_port = self.net_equip_ports.get(ip)
        if not net_equip_port:
            net_equip_port = NetEquipPort(target)
            self.net_equip_ports[ip] = net_equip_port

        port_statuses = net_equip_port.status
        for key, value in port_statuses.items():
            payload['metrics'].append(
                [
                    "system.port.status",
                    payload['collection_timestamp'],
                    value,
                    {'tags': ["port:%s" % key]}
                ]
            )

        log.debug("ip %s statuses %s" % (ip, str(port_statuses)))

        if payload['metrics']:
            payload.emit(
                log,
                self.agent_config,
                self.emitters,
                self.continue_running)

            self.port_status_metrics += 1

            log.debug("Get port statuses data for %s completed" % ip)

    # 获取端口速率
    def get_port_rate(self, target):
        ip = target.ip
        storage = self.net_equip_details[ip]
        if not storage["type"]:
            return
        payload = AgentPayload()
        self._build_payload(payload)
        payload['metrics'] = []
        payload['identity'] = storage.get('md5')

        net_equip_port = self.net_equip_ports.get(ip)
        if not net_equip_port:
            net_equip_port = NetEquipPort(target)
            self.net_equip_ports[ip] = net_equip_port

        in_rates = net_equip_port.in_rates
        if in_rates:
            for key, value in in_rates.items():
                if value and value > 0:
                    payload['metrics'].append(
                        [
                            "system.port.in_rate",
                            payload['collection_timestamp'],
                            value,
                            {'tags': ["port:%s" % key]}
                        ]
                    )

        in_bandwidth_pcts = net_equip_port.in_bandwidth_pcts
        if in_bandwidth_pcts:
            for key, value in in_bandwidth_pcts.items():
                if value and value > 0:
                    payload['metrics'].append(
                        [
                            "system.port.bandwidth.in_pct_usage",
                            payload['collection_timestamp'],
                            value,
                            {'tags': ["port:%s" % key]}
                        ]
                    )

        out_rates = net_equip_port.out_rates
        if out_rates:
            for key, value in out_rates.items():
                if value and value > 0:
                    payload['metrics'].append(
                        [
                            "system.port.out_rate",
                            payload['collection_timestamp'],
                            value,
                            {'tags': ["port:%s" % key]}
                        ]
                    )

        out_bandwidth_pcts = net_equip_port.out_bandwidth_pcts
        if out_bandwidth_pcts:
            for key, value in out_bandwidth_pcts.items():
                if value and value > 0:
                    payload['metrics'].append(
                        [
                            "system.port.bandwidth.out_pct_usage",
                            payload['collection_timestamp'],
                            value,
                            {'tags': ["port:%s" % key]}
                        ]
                    )

        port_bandwidth_pcts = net_equip_port.bandwidth_pcts
        if port_bandwidth_pcts:
            for key, value in port_bandwidth_pcts.items():
                if value and value > 0:
                    payload['metrics'].append(
                        [
                            "system.port.bandwidth.pct_usage",
                            payload['collection_timestamp'],
                            value,
                            {'tags': ["port:%s" % key]}
                        ]
                    )

        if out_rates and in_rates and in_bandwidth_pcts \
                and out_bandwidth_pcts and port_bandwidth_pcts:
            log.debug(
                "ip %s out_rates %s in_rates %s in_pcts %s out_pcts %s pcts %s" % (
                    ip,
                    str(out_rates),
                    str(in_rates),
                    str(in_bandwidth_pcts),
                    str(out_bandwidth_pcts),
                    str(port_bandwidth_pcts)
                )
            )

        if out_rates or in_rates or in_bandwidth_pcts \
                or out_bandwidth_pcts or port_bandwidth_pcts:
            payload.emit(
                log,
                self.agent_config,
                self.emitters,
                self.continue_running)

            self.port_rate_metrics += 1

            log.debug("Get port rate data for %s completed" % ip)

    def get_min_mac_md5(self, target):
        ip = target.ip
        mac_lst = []
        mac_lst_number = []
        # 尝试walk '1.3.6.1.2.1.17.1.1'可直接获得最小mac地址
        min_mac_lst = snmp_walk(target, oid='1.3.6.1.2.1.17.1.1')
        if min_mac_lst:
            min_mac = min_mac_lst[0][0][1].__repr__().split("'")[1]
            return self.get_md5(ip, min_mac)
        # 如果直接获得最小mac地址失败，尝试walk'1.3.6.1.2.1.2.2.1.6'
        macs = snmp_walk(target, oid='1.3.6.1.2.1.2.2.1.6')
        if not macs:
            return ''
        for mac in macs:
            mac_addr = mac[0][-1]
            mac_addr = mac_addr.__repr__().split("'")
            mac_addr = mac_addr[1]
            if mac_addr and mac_addr not in ["000000000000", "FFFFFFFFFFFF"]:
                mac_lst.append(mac_addr)
                mac_lst_number.append(int('0x'+mac_addr, 16))
        min_mac = mac_lst[mac_lst_number.index(min(mac_lst_number))]
        return self.get_md5(ip, min_mac)

    def get_md5(self, ip, min_mac):
        if min_mac:
            md5 = hashlib.md5()
            host_name = self.net_equip_details[ip]['host_name']
            md5.update(min_mac+host_name)
            md5_hex = md5.hexdigest()
            self.net_equip_details[ip]['md5'] = md5_hex
            return md5_hex
        else:
            md5_hex = ''
            self.net_equip_details[ip]['md5'] = md5_hex
            return md5_hex

    def metrics_amount_record(self):
        if time.time() - self.last_time_record >= RECORD_INTERVAL:
            log.info("Amounts of metrics submitted during last %d seconds:" % RECORD_INTERVAL)
            log.info("%d basis info" % (self.basis_metrics-self.basis_metrics_temp))
            log.info("%d online status" % (self.ping_metrics-self.ping_metrics_temp))
            log.info("%d performance" % (self.performance_metrics-self.performance_metrics_temp))
            log.info("%d port status" % (self.port_status_metrics-self.port_status_metrics_temp))
            log.info("%d port rate & rate-bandwidth-usage" % (self.port_rate_metrics-self.port_rate_metrics_temp))
            self.basis_metrics_temp = self.basis_metrics
            self.ping_metrics_temp = self.ping_metrics
            self.performance_metrics_temp = self.performance_metrics
            self.port_status_metrics_temp = self.port_status_metrics
            self.port_rate_metrics_temp = self.port_rate_metrics
            self.last_time_record = time.time()

    @classmethod
    def _get_pickle_path(cls):
        if Platform.is_win32():
            path = os.path.join(_windows_commondata_path(), 'Datamonitor')
        elif os.path.isdir(PidFile.get_dir()):
            path = PidFile.get_dir()
        else:
            path = tempfile.gettempdir()
        return os.path.join(path, cls.__name__ + '.pickle')


class NetPingListener(PingListener):

    def __init__(self, container):
        PingListener.__init__(self)
        self.container = container

    def finished(self, request, response):
        ip = request.get_ip()
        avg = response.get_avg()
        if self.container.get(ip).__class__.__name__ == 'dict':
            self.container[ip].update({'response': avg})
        else:
            self.container[ip] = {'response': avg}


def generate_ips(ip_ranges):
    '''
    以配置文件中设置好的网段参数创建网段列表

    Args:
        ip_ranges: 可以接受如下参数或任意组合，以'  , ' 连接
            '10.1.2.242'
            '192.168.0.0/255.255.255.0'
            '192.168.0.0/24'
            '192.168.0.0~192.168.0.255'

    Returns:
        ips: 一个列表，包含所有需求网络设备发现的ip。

    '''
    ips = []
    for ip_range in ip_ranges:
        if not ip_range:
            continue
        elif '~' in ip_range:
            start, end = ip_range.split('~')
            start_digits = start.split('.')
            end_digits = end.split('.')
            start_end = izip(start_digits, end_digits)
            first = next(start_end)
            second = next(start_end)
            third = next(start_end)
            fourth = next(start_end)
            for a in range(int(first[0]), int(first[1])+1, 1):
                for b in range(int(second[0]), int(second[1])+1, 1):
                    for c in range(int(third[0]), int(third[1])+1, 1):
                        for d in range(int(fourth[0]), int(fourth[1])+1, 1):
                            ip = "%d.%d.%d.%d" % (a, b, c, d)
                            ips.append(ip)
            continue
        elif '/' not in ip_range:
            ips.append(ip_range)
            continue
        address, mask = ip_range.split('/')
        if mask in NET_MASK.values():
            ip = IP(ip_range)
            for i in ip:
                ips.append(str(i))
        elif mask in NET_MASK.keys():
            ip = IP(address + '/' + NET_MASK[mask])
            for i in ip:
                ips.append(i)

    return ips


def arrange_task(
        funcs,
        target,
        task_queue,
        basis_info_interval,
        performance_interval,
        port_status_interval,
        port_rate_interval
):
    '''根据各指标时间采集周期和上次采集时间安排采集任务

    Args:
        funcs: 采集方法字典
        target: 采集方法接收的参数，Target实例
        task_queue: Queue实例队列，线程安全
        basis_info_interval: 设定的设备基本信息采集间隙
        performance_interval: 设定的设备性能信息采集间隙
        port_status_interval: 设定的设备端口状态采集间隙
        port_rate_interval: 设定的设备端口速率采集间隙
    '''
    basis_info_collect_period = \
        time.time() - target.last_time_basis_info_collected
    performance_collect_period = \
        time.time() - target.last_time_performance_collected
    port_status_collect_period = \
        time.time() - target.last_time_port_status_collected
    port_rate_collect_period = \
        time.time() - target.last_time_port_rate_collected
    if basis_info_collect_period > basis_info_interval:
        '''task_queue是PriorityQueue实例
        接收(priority_number, data)元祖
        priority最低的先取出 以此类推'''
        basis_info_task = (0, (funcs['basis_info'], target))
        task_queue.put(basis_info_task)
        target.last_time_basis_info_collected = time.time()
    if performance_collect_period > performance_interval:
        performance_task = (1, (funcs['cpu_ram'], target))
        task_queue.put(performance_task)
        target.last_time_performance_collected = time.time()
    if port_status_collect_period > port_status_interval:
        port_status_task = (1, (funcs['port_status'], target))
        task_queue.put(port_status_task)
        target.last_time_port_status_collected = time.time()
    if port_rate_collect_period > port_rate_interval:
        port_rate_task = (1, (funcs['port_rate'], target))
        task_queue.put(port_rate_task)
        target.last_time_port_rate_collected = time.time()

