# coding: utf-8
from __future__ import division

import sys

import psutil

from ._common import cache, execute, add_unit, get_with_bytes_unit
from ._common import sdiskpart, scpuinfo, snetworkinfo

from ._common import LINUX
from ._common import POSIX
from ._common import WINDOWS
from ._common import AIX
from ._common import DARWIN

from ._common import ENCODING

if LINUX:
    from . import _syslinux as _sysplatform

elif WINDOWS:
    from . import _syswindows as _sysplatform

elif AIX:
    from . import _sysaix as _sysplatform

elif DARWIN:
    from . import _sysdarwin as _sysplatform

else:  # pragma: no cover
    raise NotImplementedError('platform %s is not supported' % sys.platform)

__all__ = [
    # exceptions
    'Error',

    # constants
    'LINUX',
    'WINDOWS',
    'AIX',

    # functions
    'physical_cpu_count',
    'logical_cpu_count',
    'cpu_info',
    'physical_memory',
    'swap_memory',
    'disk_info',
    'network_info',
    'gohai_platform_info'
]

# =====================================================================
# --- system processes related functions
# =====================================================================

# --- cpu ---


@add_unit(unit=u'核')
def physical_cpu_count(add_unit=False):
    """Return physical cpu count.
    If add_unit=True, will return with unit"""
    count = psutil.cpu_count(logical=False)
    return count


@add_unit(unit=u'核')
def logical_cpu_count(add_unit=False):
    """Return logical cpu count.
    If add_unit=True, will return with unit"""
    count = psutil.cpu_count(logical=True)
    return count


@cache
def cpu_info():
    if AIX:
        return _sysplatform.cpu_info()
    else:
        import cpuinfo
        cpu_info = cpuinfo.get_cpu_info()
        info = scpuinfo(
            brand=cpu_info.get('brand'),
            frequency=cpu_info.get('hz_advertised'),
            family=cpu_info.get('family'),
            vendor_id=cpu_info.get('vendor_id'),
            stepping=cpu_info.get('stepping'),
            cache_size=cpu_info.get('l2_cache_size'),
            model=cpu_info.get('model'))
        return info


# --- memory ---


@add_unit()
def physical_memory(add_unit=False):
    total = psutil.virtual_memory().total
    return total


@add_unit()
def swap_memory(add_unit=False):
    swap_total = psutil.swap_memory().total
    return swap_total


# --- disk ---


def disk_info(all=False, add_unit=False):
    info = []
    for part in psutil.disk_partitions(all=all):
        if WINDOWS:
            if 'cdrom' in part.opts or part.fstype == '':
                # skip cd-rom drives with no disk in it; they may raise
                # ENOENT, pop-up a Windows GUI error for a non-ready
                # partition or just hang.
                continue
        usage = psutil.disk_usage(part.mountpoint)
        total = get_with_bytes_unit(usage.total) if add_unit else usage.total
        info.append(sdiskpart(part.device, part.mountpoint, total))
    return info


# --- network ---


def network_info():
    info = []
    for name, snics in psutil.net_if_addrs().iteritems():
        name = name.lower()
        if 'loopback' in name or 'vmware' in name or 'virtual' in name or 'tunneling' in name:
            continue

        try:
            name = name.decode(ENCODING).encode('utf8')
        except:
            pass

        mac = ipv4 = ipv6 = netmask = None
        for snic in snics:
            if snic.family in (-1, 17):
                mac = snic.address
            elif snic.family == 2:
                ipv4 = snic.address
                netmask = snic.netmask
            elif snic.family in (23, 10):
                ipv6 = snic.address
                if '%' in ipv6:
                    ipv6 = ipv6.split('%')[0]

        if not ipv4 or (ipv4 and ipv4.startswith(('169', '127.0.0.1'))):
            continue

        info.append(
            snetworkinfo(
                name=name, mac=mac, ipv4=ipv4, ipv6=ipv6, netmask=netmask))
    return info


def weighted_ip(ip):

    def ip_to_int(ip_addr):
        return sum([
            256**index * int(val)
            for index, val in enumerate(ip_addr.split('.')[::-1])
        ])

    ip_int = ip_to_int(ip.strip())

    if ip_to_int('192.168.0.0') <= ip_int <= ip_to_int('192.168.255.255'):
        return 3, ip
    elif ip_to_int('172.16.0.0') <= ip_int <= ip_to_int('172.31.255.255'):
        return 2, ip
    elif ip_to_int('10.0.0.0') <= ip_int <= ip_to_int('10.255.255.255'):
        return 1, ip
    return 4, ip


def main_ip():
    return sorted(weighted_ip(nti.ipv4) for nti in network_info())[0][-1]


# --- machine ---


@cache
def is_virtual_machine():
    if WINDOWS:
        return _sysplatform.is_virtual_machine()
    else:
        output = execute(cmd=_sysplatform.virtual_cmd).lower()
        for keyword in ('kvm', 'hvm', 'virutal', 'vmware'):
            if keyword in output:
                return True
        return False


class NodeType(object):
    VM = 'vm'
    MiniServer = 'MiniServer'
    PCServer = 'PCServer'


@cache
def node_type():
    if AIX:
        return NodeType.MiniServer
    elif is_virtual_machine():
        return NodeType.VM
    else:
        return NodeType.PCServer
