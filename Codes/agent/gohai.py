# coding: utf-8
from __future__ import division

import json
import netifaces
import platform
import socket

import psutil
from sysutil import cpu_info
from sysutil import disk_info
from sysutil import network_info
from sysutil import physical_memory, swap_memory

from util import get_machine_type


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
        info = netifaces.ifaddresses(name)
        if len(info) < 2:
            continue
        if len(info) == 2:
            ipv4 = info[socket.AF_INET][0].get('addr')
            info.keys().remove(socket.AF_INET)
            mac = info[info.keys()[0]][0].get('addr')
            ipv6 = ''
        else:
            ipv4 = info[socket.AF_INET][0].get('addr')
            ipv6 = info[socket.AF_INET6][0].get('addr')
            info.keys().remove(socket.AF_INET)
            info.keys().remove(socket.AF_INET6)
            mac = info[info.keys()[0]][0].get('addr')
        network.append({
            'name': name,
            'ipaddress': ipv4,
            'ipaddressv6': ipv6,
            'macaddress': mac,
        })
    return network


def get_file_system_info():
    file_system_info = []
    for disk in disk_info():
        if disk.total != u'0.0 B':
            [size, unit] = disk.total.split(' ')
            if unit == u'B':
                kb_size = float(size) / 1024
            elif unit == u'KB':
                kb_size = float(size)
            elif unit == u'MB':
                kb_size = float(size) * 1024
            elif unit == u'GB':
                kb_size = float(size) * 1024 * 1024
            elif unit == u'TB':
                kb_size = float(size) * 1024 * 1024 * 1024
            file_system_info.append(
                {
                    'mounted_on': disk.mountpoint,
                    'kb_size': str(int(kb_size)),
                    'name': None if disk.device == '' else disk.device,
                }
            )
    return file_system_info


def get_platform_info():
    return {
        'pythonV': platform.python_version(),
        'kernel_release': platform.release(),
        'kernel_version': platform.version(),
        'hostname': platform.node(),
        'machine': platform.machine(),
        'GOOARCH': 'amd64',
        'kernel_name': platform.uname()[0],
        'hardware_platform': platform.uname()[5],
        'machine_type': get_machine_type(),
        'GOOS': platform.system(),
        'goV': '1.3.3',
        'os': platform.system(),
        'processor': platform.processor()
    }


def get_memory_info():
    swap = swap_memory('GB')
    physical = physical_memory('GB')
    if 'GB' in str(swap):
        swap_mem = swap
    else:
        swap_tmp = int(str(swap).split(' ')[0])
        swap_mem = unicode(int(round(swap_tmp / 1024))) + unicode(' GB')
    if 'GB' in str(physical):
        physical_mem = physical
    else:
        physical_tmp = int(str(physical).split(' ')[0])
        physical_mem = unicode(int(round(physical_tmp / 1024))) + unicode(' GB')

    memory_info = {
        'swap_total': swap_mem,
        'total': physical_mem
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
