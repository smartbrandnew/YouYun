# coding: utf-8
from __future__ import division

import json
import netifaces
import os
import platform

import psutil
from pf import get_platform
from sysutil import cpu_info
from sysutil import disk_info
from sysutil import get_with_bytes_unit
from sysutil import network_info

from util import get_machine_type, get_hostname

pf_platform = get_platform()


def gohai_consts():
    consts = {
        'build_date': 'Thu Apr 21 18:04:10 UTC 2016',
        'git_hash': '0234f68',
        'git_branch': '5.7.x-agent',
        'go_version': 'go version go1.3.3 linux/amd64'
    }
    return consts


def get_network_info():
    network = []
    for net in network_info():
        network.append({
            'name': net.name,
            'ipaddress': net.ipv4,
            'macaddress': net.mac,
            'ipaddressv6': net.ipv6
        })
    if network:
        return network

    for name in netifaces.interfaces():
        info = netifaces.ifaddresses(name).values()
        if len(info) < 2:
            continue
        else:
            mac = info[0][0].values()[0]
            ipv4 = info[1][0].get('addr')
            if not ipv4 or not mac:
                continue
            network.append({
                'name': name,
                'ipaddress': ipv4,
                'ipaddressv6': '',
                'macaddress': mac,
            })
    return network


def get_file_system_info():
    file_system_info = []
    for disk in disk_info():
        file_system_info.append(
            {
                'mounted_on': disk.mountpoint,
                'kb_size': str(int(disk.total / 1024)),
                'name': None if disk.device == '' else disk.device,
            }
        )
    return file_system_info


def get_platform_info():
    return {
        'pythonV': platform.python_version(),
        'kernel_release': platform.release(),
        'kernel_version': pf_platform.kernel,
        'dist': pf_platform.dist,
        'hostname': '{}_{}'.format(get_hostname(), os.environ.get('ANT_AGENT_IP')),
        'machine': platform.machine(),
        'GOOARCH': 'amd64',
        'kernel_name': platform.uname()[0],
        'hardware_platform': platform.uname()[5],
        'machine_type': get_machine_type(),
        'GOOS': platform.system(),
        'goV': '1.3.3',
        'os': pf_platform.system,
        'processor': platform.processor()
    }


def get_memory_info():
    swap = get_with_bytes_unit(psutil.swap_memory().total, unit='GB')
    physical = get_with_bytes_unit(psutil.virtual_memory().total, unit='GB')
    memory_info = {
        'swap_total': swap,
        'total': physical
    }
    return memory_info


def get_cpu_info():
    cpu_models, cpu_frequencies, cpu_family, vendor_id, stepping, cache_size, model = cpu_info()
    cpu_phy_cores = psutil.cpu_count(logical=False)
    cpu_log_cores = psutil.cpu_count(logical=True)
    if platform.system().lower() == 'linux':
        cpu_data = {
            'family': str(cpu_family),
            'vendor_id': vendor_id,
            'mhz': cpu_frequencies,
            'stepping': str(stepping),
            'cache_size': unicode(cache_size),
            'model': str(model),
            'model_name': cpu_models,
            'cpu_logical_processors': str(cpu_log_cores),
            'cpu_cores': str(cpu_phy_cores)
        }
        return cpu_data

    elif platform.system().lower() == 'windows':
        if not cpu_family:
            import subprocess
            cmd = 'Systeminfo | findstr /i "model'
            out = subprocess.Popen(cmd, shell=True, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE)
            if out.wait() == 0:
                output = out.stdout.read().lower()
                cpu_family = output[output.find('family'):].split(' ')[1]
                stepping = output[output.find('stepping'):].split(' ')[1]
                model = output[output.find('model'):].split(' ')[1]

        cpu_data = {
            'family': str(cpu_family),
            'vendor_id': vendor_id,
            'mhz': cpu_frequencies,
            'stepping': str(stepping),
            'model': str(model),
            'model_name': cpu_models,
            'cpu_logical_processors': str(cpu_log_cores),
            'cpu_cores': str(cpu_phy_cores)
        }
        return cpu_data


def get_gohai_data():
    gohai_data = {
        'network': get_network_info(),
        'filesystem': get_file_system_info(),
        'platform': get_platform_info(),
        'gohai': gohai_consts(),
        'memory': get_memory_info(),
        'cpu': get_cpu_info()
    }
    return json.dumps(gohai_data)
