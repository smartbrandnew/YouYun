# coding:utf-8

#3p
import requests
import json
from checks import AgentCheck
import collections
from lxml import etree
import libvirt


libvirt = None
libvirt_type = 'kvm'
libvirt_uri = ''

Instance = collections.namedtuple('Instance', ['name', 'UUID', 'state'])
CPUStats = collections.namedtuple('CPUStats', ['number', 'util'])
Interface = collections.namedtuple('Interface', ['name', 'mac',
                                                 'fref', 'parameters'])
InterfaceStats = collections.namedtuple('InterfaceStats',
                                        ['rx_bytes', 'rx_packets',
                                         'tx_bytes', 'tx_packets'])
Disk = collections.namedtuple('Disk', ['device'])
DiskStats = collections.namedtuple('DiskStats',
                                   ['read_bytes', 'read_requests',
                                    'write_bytes', 'write_requests',
                                    'errors'])
DiskSize = collections.namedtuple(
    'DiskSize', ['total', 'allocation', 'physical'])
Memory = collections.namedtuple('Memory', ['total', 'used', 'util'])


class InspectorException(Exception):

    def __init__(self, message=None):
        super(InspectorException, self).__init__(message)


class InstanceNotFoundException(InspectorException):
    pass


class LibvirtInspector():

    per_type_uris = dict(uml='uml:///system', xen='xen:///', lxc='lxc:///')

    def __init__(self):
        self.uri = self._get_uri()
        self.connection = None

    def _get_uri(self):
        return libvirt_uri or self.per_type_uris.get(libvirt_type,
                                                     'qemu:///system')

    def _get_connection(self):
        if not self.connection or not self._test_connection():
            global libvirt
            if libvirt is None:
                libvirt = __import__('libvirt')
                # LOG.debug('Connecting to libvirt: %s', self.uri)
            self.connection = libvirt.open(self.uri)
        return self.connection

    def _test_connection(self):
        try:
            self.connection.getCapabilities()
            return True
        except libvirt.libvirtError as e:
            if (e.get_error_code() == libvirt.VIR_ERR_SYSTEM_ERROR and
                e.get_error_domain() in (libvirt.VIR_FROM_REMOTE,
                                         libvirt.VIR_FROM_RPC)):
                # LOG.debug('Connection to libvirt broke')
                return False
            raise

    def _lookup_by_name(self, instance_name):
        try:
            return self._get_connection().lookupByName(instance_name)
        except Exception as ex:
            if not libvirt or not isinstance(ex, libvirt.libvirtError):
                raise InspectorException(unicode(ex))
            error_code = ex.get_error_code()
            msg = ("Error from libvirt while looking up %(instance_name)s: "
                   "[Error Code %(error_code)s] "
                   "%(ex)s" % {'instance_name': instance_name,
                               'error_code': error_code,
                               'ex': ex})
            raise InstanceNotFoundException(msg)

    def inspect_instances(self):
        if self._get_connection().numOfDomains() > 0:
            for domain_id in self._get_connection().listDomainsID():
                try:
                    # We skip domains with ID 0 (hypervisors).
                    if domain_id != 0:
                        domain = self._get_connection().lookupByID(domain_id)
                        state = domain.state(0)[0]
                        if state != 1:
                            state = 0
                        yield Instance(name=domain.name(),
                                       UUID=domain.UUIDString(), state=state)
                except libvirt.libvirtError:
                    # Instance was deleted while listing... ignore it
                    pass
                    # shut off instances

    def inspect_defined_domains(self):
        if self._get_connection().numOfDomains() > 0:
            for instance_name in self._get_connection().listDefinedDomains():
                domain = self._lookup_by_name(instance_name)
                state = domain.state(0)[0]
                if state != 1:
                    state = 0
                yield Instance(name=instance_name,
                               UUID=domain.UUIDString(), state=state)

    def inspect_disk_info_for_down(self, instance_name):
        domain = self._lookup_by_name(instance_name)
        # mem_total = domain.info()[1]
        tree = etree.fromstring(domain.XMLDesc(0))
        for device in filter(
                bool,
                [target.get("dev")
                 for target in tree.findall('devices/disk/target')]):
            disk = Disk(device=device)
            try:
                disk_size = domain.blockInfo(device, 0)
            except libvirt.libvirtError:
                disk_size = [0, 0, 0]
                pass
            size = DiskSize(total=disk_size[0] / (1024 * 1024),
                            allocation=disk_size[1] / (1024 * 1024),
                            physical=disk_size[2] / (1024 * 1024))
            yield (disk, size)

    def inspect_vnics_info_for_down(self, instance_name):

        domain = self._lookup_by_name(instance_name)
        tree = etree.fromstring(domain.XMLDesc(0))
        for iface in tree.findall('devices/interface'):
            target = iface.find('target')
            if target is not None:
                name = target.get('dev')
            else:
                continue
            mac = iface.find('mac')
            if mac is not None:
                mac_address = mac.get('address')
            else:
                continue
            fref = iface.find('filterref')
            if fref is not None:
                fref = fref.get('filter')
            params = dict((p.get('name').lower(), p.get('value'))
                          for p in iface.findall('filterref/parameter'))
            yield Interface(name=name, mac=mac_address,
                            fref=fref, parameters=params)

    def inspect_mem_info_for_down(self, instance_name):

        domain = self._lookup_by_name(instance_name)
        mem_total = domain.info()[1]
        return mem_total

    def inspect_cpus(self, instance_name):

        domain = self._lookup_by_name(instance_name)
        try:
            (_, _, _, num_cpu, cpu_time_start) = domain.info()
            import time
            real_time_start = time.time()
            time.sleep(1)
            (_, _, _, _, cpu_time_end) = domain.info()
            real_time_end = time.time()
            real_diff_time = real_time_end - real_time_start
            util = 100 * (cpu_time_end - cpu_time_start) / \
                (float)(num_cpu * real_diff_time * 1000000000)
            if util > 100:
                util = 100.0
            if util < 0:
                util = 0.0
            return CPUStats(number=num_cpu, util=str(util))
        except libvirt.libvirtError:
            pass

    def inspect_memory(self, instance_name):

        try:
            domain = self._lookup_by_name(instance_name)
            domain.setMemoryStatsPeriod(5)
            meminfo = domain.memoryStats()
            free_mem = float(meminfo['unused'])
            total_mem = float(meminfo['available'])
            util = ((total_mem - free_mem) / total_mem) * 100
            return Memory(
                total=total_mem,
                used=total_mem -
                free_mem,
                util=util)
        except:
            pass
        try:
            domain = self._lookup_by_name(instance_name)
            actual = float(domain.memoryStats()['actual'])
            rss = float(domain.memoryStats()['rss'])
            rss = rss - 150000
            if rss >= actual:
                rss = rss - 250000
            if rss <= 0:
                rss = rss + 150000
                # util = str(int((rss / actual)*100))
            util = (rss / actual) * 100
            # import decimal
            # util = decimal.Decimal(str(round(util, 0)))
            return Memory(total=actual, used=rss, util=util)
        except libvirt.libvirtError:
            pass

    def inspect_vnics(self, instance_name):

        domain = self._lookup_by_name(instance_name)
        tree = etree.fromstring(domain.XMLDesc(0))
        for iface in tree.findall('devices/interface'):
            target = iface.find('target')
            if target is not None:
                name = target.get('dev')
            else:
                continue
            mac = iface.find('mac')
            if mac is not None:
                mac_address = mac.get('address')
            else:
                continue
            fref = iface.find('filterref')
            if fref is not None:
                fref = fref.get('filter')
            params = dict((p.get('name').lower(), p.get('value'))
                          for p in iface.findall('filterref/parameter'))
            interface = Interface(name=name, mac=mac_address,
                                  fref=fref, parameters=params)
            try:
                rx_bytes, rx_packets, _, _, \
                    tx_bytes, tx_packets, _, _ = domain.interfaceStats(name)
                stats = InterfaceStats(rx_bytes=rx_bytes,
                                       rx_packets=rx_packets,
                                       tx_bytes=tx_bytes,
                                       tx_packets=tx_packets)
                yield (interface, stats)
            except libvirt.libvirtError:
                pass

    def inspect_disks(self, instance_name):

        domain = self._lookup_by_name(instance_name)
        tree = etree.fromstring(domain.XMLDesc(0))
        for device in filter(
                bool,
                [target.get("dev")
                 for target in tree.findall('devices/disk/target')]):
            disk = Disk(device=device)
            block_stats = domain.blockStats(device)
            stats = DiskStats(read_requests=block_stats[0],
                              read_bytes=block_stats[1],
                              write_requests=block_stats[2],
                              write_bytes=block_stats[3],
                              errors=block_stats[4])
            try:
                disk_size = domain.blockInfo(device, 0)
            except libvirt.libvirtError:
                disk_size = [0, 0, 0]
                pass
            size = DiskSize(total=disk_size[0] / (1024 * 1024),
                            allocation=disk_size[1] / (1024 * 1024),
                            physical=disk_size[2] / (1024 * 1024))
            yield (disk, stats, size)


def output():

    libvirtInspector = LibvirtInspector()
    instances = libvirtInspector.inspect_instances()
    list = []
    cpu_number = 0
    domain_Memory = 0

    for instance in instances:
        memory = libvirtInspector.inspect_memory(instance.name)
        dict = {}
        dict['domain_name'] = instance.name
        cpus = libvirtInspector.inspect_cpus(instance.name)
        dict['domain.cpu.number'] = cpus.number
        cpu_number = cpu_number + dict['domain.cpu.number']
        dict['domain.cpu.usage'] = cpus.util
        dict['domain.memory.total'] = memory.total / 1024.0
        dict['domain.memory.used'] = memory.used / 1024.0
        domain_Memory = domain_Memory + memory.used / 1024.0
        dict['domain.memory.usage'] = memory.util
        # get the nic infomation
        nics = libvirtInspector.inspect_vnics(instance.name)
        nicList = []
        for nic in nics:
            nicdict = {}
            nicdict['domain.net.send.read'] = nic[1].tx_bytes
            nicdict['domain.net.receive.write'] = nic[1].rx_bytes
            nicdict['domain.net.send.request'] = nic[1].tx_packets
            nicdict['domain.net.receive.reques'] = nic[1].rx_packets
            nicList.append(nicdict)
        dict['nics'] = nicList
        # get the disk infomation
        disks = libvirtInspector.inspect_disks(instance.name)
        diskList = []
        for disk in disks:
            diskdict = {}
            diskdict['domain.device'] = disk[0].device
            diskdict['domain.total.size'] = disk[2].total / 1024.0
            diskdict['domain.used.size'] = disk[2].physical / 1024.0
            diskdict['domain.disk.read'] = disk[1].read_bytes
            diskdict['domain.disk.write'] = disk[1].write_bytes
            diskdict['domain.disk.read.request'] = disk[1].read_requests
            diskdict['domain.disk.write.request'] = disk[1].write_requests
            diskList.append(diskdict)
        dict['disks'] = diskList
        list.append(dict)

    return list, cpu_number, domain_Memory


class GetName(object):

    def __init__(self, keystone_host, nova_host, tenant_name, user, password):

        self.keystone_host = keystone_host
        self.nove_host = ""
        self.tenant_name = tenant_name
        self.user = user
        self.password = password

    def get_auth_token_v2(self):

        auth_info = {
            "auth": {
                "tenantName": self.tenant_name,
                "passwordCredentials": {
                    "username": self.user,
                    "password": str(
                        self.password)}}}
        auth_info = json.dumps(auth_info)

        header = {'Content-Type': 'application/json'}

        res = requests.post(
            self.keystone_host +
            '/tokens',
            data=auth_info,
            headers=header)

        if res.status_code != 200:
            raise Exception('请求发送失败:{}'.format(res.content))
        else:
            content = json.loads(res.content)
            servicelog = content.get('access', {}).get('serviceCatalog', [])
            for entry in servicelog:
                if entry['name'] == 'nova':
                    valid_endpoints = {}
                    for ep in entry['endpoints']:
                        for interface in [
                                'publicURL', 'internalURL', 'adminURL']:

                            valid_endpoints[interface] = ep[interface]

                    if valid_endpoints:
                        # Favor public endpoints over internal
                        self.nove_host = valid_endpoints.get(
                            "adminURL", valid_endpoints.get("internalURL"))

            return content['access']['token']['id']

    def get_instance_name(self):

        use_token = self.get_auth_token_v2()
        name_dict = {}
        header = {'X-Auth-Token': use_token}
        res = requests.get(
            self.nove_host +
            '/servers/detail?all_tenants=true',
            headers=header)
        if res.status_code != 200:
            raise Exception('请求发送失败:{}'.format(res.content))
        else:
            servers = res.json()
            print(servers)
            addr = ''
            for server in servers['servers']:
                if 'provider' in server['addresses']:
                    addr = server['addresses']['provider'][0]['addr']
                elif 'selfservice' in server['addresses']:
                    addr = server['addresses']['selfservice'][0]['addr']

                instance_name = server['OS-EXT-SRV-ATTR:instance_name']
                server_name = server['name']
                if addr and instance_name:
                    name_dict.setdefault(
                        instance_name, server_name + '@' + addr)

                if not addr:
                    name_dict.setdefault(instance_name, server_name)

        return name_dict


class OpenStackDomain(AgentCheck):

    def __init__(self, name, init_config, agentConfig, instances=None):
        AgentCheck.__init__(self, name, init_config, agentConfig, instances)

    def get_config(self, instance):

        keystone_host = instance.get("keystone_host", '')
        nova_host = instance.get("nova_host", '')
        tenant_name = instance.get('tenant_name', '')
        user = instance.get("user", '')
        password = instance.get("password", '')

        return keystone_host, nova_host, tenant_name, user, password

    def check(self, instance):

        self.deal_data(instance)

    def up_data(
            self,
            metric,
            value,
            tags=None,
            hostname=None,
            device_name=None,
            timestamp=None):
        self.gauge(metric, value, tags, hostname, device_name, timestamp)

    def deal_data(self, instance):
        keystone_host, nova_host, tenant_name, user, password = self.get_config(
            instance)
        getname = GetName(
            keystone_host,
            nova_host,
            tenant_name,
            user,
            password)
        name_dict = getname.get_instance_name()
        data_list, cpu_number, domain_Memory = output()
        domain_name = ''

        for i in data_list:
            if 'domain_name' in i:
                if i['domain_name'] in name_dict.keys():
                    domain_name = name_dict[i['domain_name']]

            for k, v in i.items():
                if isinstance(v, list):
                    for j in v:
                        for x, y in j.items():
                            self.up_data(
                                x, y, tags=['openstack:vm'], hostname=domain_name)
                else:
                    self.up_data(
                        k, v, tags=['openstack:vm'], hostname=domain_name)
        if cpu_number != 0:
            self.up_data(
                "hypervisor.cpu.number",
                cpu_number,
                tags=['openstack:hypervisor'])
        if domain_Memory != 0:
            self.up_data(
                'hypervisor.ues.memory',
                domain_Memory,
                tags=['openstack:hypervisor'])
