import operator
import platform
import re
import sys
import time

import uptime

try:
    import psutil
except ImportError:
    psutil = None

from checks import Check
from util import get_hostname
from utils.platform import Platform
from utils.subprocess_output import get_subprocess_output

to_float = lambda s: float(s.replace(",", "."))


class IO(Check):
    def __init__(self, logger):
        Check.__init__(self, logger)
        self.header_re = re.compile(r'([%\\/\-_a-zA-Z0-9]+)[\s+]?')
        self.item_re = re.compile(r'^([a-zA-Z0-9\/]+)')
        self.value_re = re.compile(r'\d+\.\d+')

    def _parse_linux2(self, output):
        recentStats = output.split('Device:')[2].split('\n')
        header = recentStats[0]
        headerNames = re.findall(self.header_re, header)
        device = None

        ioStats = {}

        for statsIndex in range(1, len(recentStats)):
            row = recentStats[statsIndex]

            if not row:
                continue

            deviceMatch = self.item_re.match(row)

            if deviceMatch is not None:

                device = deviceMatch.groups()[0]
            else:
                continue

            values = re.findall(self.value_re, row)

            if not values:
                continue

            ioStats[device] = {}

            for headerIndex in range(len(headerNames)):
                headerName = headerNames[headerIndex]
                ioStats[device][headerName] = values[headerIndex]

        return ioStats

    def _parse_darwin(self, output):
        lines = [l.split() for l in output.split("\n") if len(l) > 0]
        disks = lines[0]
        lastline = lines[-1]
        io = {}
        for idx, disk in enumerate(disks):
            kb_t, tps, mb_s = map(float, lastline[(3 * idx):(3 * idx) + 3])
            io[disk] = {
                'system.io.bytes_per_s': mb_s * 2 ** 20,
            }
        return io

    def xlate(self, metric_name, os_name):

        if os_name == "sunos":
            names = {
                "wait": "await",
                "svc_t": "svctm",
                "%b": "%util",
                "kr/s": "rkB/s",
                "kw/s": "wkB/s",
                "actv": "avgqu-sz",
            }
        elif os_name == "freebsd":
            names = {
                "svc_t": "await",
                "%b": "%util",
                "kr/s": "rkB/s",
                "kw/s": "wkB/s",
                "wait": "avgqu-sz",
            }

        return names.get(metric_name, metric_name)

    def check(self, agentConfig):

        io = {}
        try:
            if Platform.is_linux():
                stdout, _, _ = get_subprocess_output(['iostat', '-d', '1', '2', '-x', '-k'], self.logger)
                io.update(self._parse_linux2(stdout))

            elif sys.platform == "sunos5":
                output, _, _ = get_subprocess_output(["iostat", "-x", "-d", "1", "2"], self.logger)
                iostat = output.splitlines()
                lines = [l for l in iostat if len(l) > 0]
                lines = lines[len(lines) / 2:]

                assert "extended device statistics" in lines[0]
                headers = lines[1].split()
                assert "device" in headers
                for l in lines[2:]:
                    cols = l.split()
                    io[cols[0]] = {}
                    for i in range(1, len(cols)):
                        io[cols[0]][self.xlate(headers[i], "sunos")] = cols[i]

            elif sys.platform.startswith("freebsd"):
                output, _, _ = get_subprocess_output(["iostat", "-x", "-d", "1", "2"], self.logger)
                iostat = output.splitlines()
                lines = [l for l in iostat if len(l) > 0]
                lines = lines[len(lines) / 2:]

                assert "extended device statistics" in lines[0]
                headers = lines[1].split()
                assert "device" in headers
                for l in lines[2:]:
                    cols = l.split()
                    io[cols[0]] = {}
                    for i in range(1, len(cols)):
                        io[cols[0]][self.xlate(headers[i], "freebsd")] = cols[i]
            elif sys.platform == 'darwin':
                iostat, _, _ = get_subprocess_output(['iostat', '-d', '-c', '2', '-w', '1'], self.logger)
                io = self._parse_darwin(iostat)
            else:
                return False

            device_blacklist_re = agentConfig.get('device_blacklist_re', None)
            if device_blacklist_re:
                filtered_io = {}
                for device, stats in io.iteritems():
                    if not device_blacklist_re.match(device):
                        filtered_io[device] = stats
            else:
                filtered_io = io
            return filtered_io

        except Exception:
            self.logger.exception("Cannot extract IO statistics")
            return False


class Load(Check):
    def check(self, agentConfig):
        if Platform.is_linux():
            try:
                with open('/proc/loadavg', 'r') as load_avg:
                    uptime = load_avg.readline().strip()
            except Exception:
                self.logger.exception('Cannot extract load')
                return False

        elif sys.platform in ('darwin', 'sunos5') or sys.platform.startswith("freebsd"):
            try:
                uptime, _, _ = get_subprocess_output(['uptime'], self.logger)
            except Exception:
                self.logger.exception('Cannot extract load')
                return False

        load = [res.replace(',', '.') for res in re.findall(r'([0-9]+[\.,]\d+)', uptime)]

        try:
            cores = int(agentConfig.get('system_stats').get('cpuCores'))
            assert cores >= 1, "Cannot determine number of cores"
            return {'system.load.1': float(load[0]),
                    'system.load.5': float(load[1]),
                    'system.load.15': float(load[2]),
                    'system.load.norm.1': float(load[0]) / cores,
                    'system.load.norm.5': float(load[1]) / cores,
                    'system.load.norm.15': float(load[2]) / cores,
                    }
        except Exception:
            return {'system.load.1': float(load[0]),
                    'system.load.5': float(load[1]),
                    'system.load.15': float(load[2])}


class Memory(Check):
    def __init__(self, logger):
        Check.__init__(self, logger)
        macV = None
        if sys.platform == 'darwin':
            macV = platform.mac_ver()
            macV_minor_version = int(re.match(r'10\.(\d+)\.?.*', macV[0]).group(1))

        if macV and (macV_minor_version >= 6):
            self.topIndex = 6
        else:
            self.topIndex = 5

        self.pagesize = 0
        if sys.platform == 'sunos5':
            try:
                pgsz, _, _ = get_subprocess_output(['pagesize'], self.logger)
                self.pagesize = int(pgsz.strip())
            except Exception:
                pass

    def check(self, agentConfig):
        if Platform.is_linux():
            try:
                with open('/proc/meminfo', 'r') as mem_info:
                    lines = mem_info.readlines()
            except Exception:
                self.logger.exception('Cannot get memory metrics from /proc/meminfo')
                return False

            regexp = re.compile(r'^(\w+):\s+([0-9]+)')
            meminfo = {}

            for line in lines:
                try:
                    match = re.search(regexp, line)
                    if match is not None:
                        meminfo[match.group(1)] = match.group(2)
                except Exception:
                    self.logger.exception("Cannot parse /proc/meminfo")

            memData = {}

            try:
                memData['physTotal'] = int(meminfo.get('MemTotal', 0)) / 1024
                memData['physFree'] = int(meminfo.get('MemFree', 0)) / 1024
                memData['physBuffers'] = int(meminfo.get('Buffers', 0)) / 1024
                memData['physCached'] = int(meminfo.get('Cached', 0)) / 1024
                memData['physShared'] = int(meminfo.get('Shmem', 0)) / 1024
                memData['physSlab'] = int(meminfo.get('Slab', 0)) / 1024
                memData['physPageTables'] = int(meminfo.get('PageTables', 0)) / 1024
                memData['physUsed'] = memData['physTotal'] - memData['physFree']

                if 'MemAvailable' in meminfo:
                    memData['physUsable'] = int(meminfo.get('MemAvailable', 0)) / 1024
                else:
                    memData['physUsable'] = memData['physFree'] + memData['physBuffers'] + memData['physCached']

                if memData['physTotal'] > 0:
                    memData['physPctUsable'] = float(memData['physUsable']) / float(memData['physTotal'])
                    memData['physPctUsage'] = (1 - memData['physPctUsable']) * 100
                    memData.pop('physPctUsable')
            except Exception:
                self.logger.exception('Cannot compute stats from /proc/meminfo')

            try:
                memData['swapTotal'] = int(meminfo.get('SwapTotal', 0)) / 1024
                memData['swapFree'] = int(meminfo.get('SwapFree', 0)) / 1024
                memData['swapCached'] = int(meminfo.get('SwapCached', 0)) / 1024

                memData['swapUsed'] = memData['swapTotal'] - memData['swapFree']

                if memData['swapTotal'] > 0:
                    memData['swapPctFree'] = float(memData['swapFree']) / float(memData['swapTotal'])
            except Exception:
                self.logger.exception('Cannot compute swap stats')

            return memData

        elif sys.platform == 'darwin':
            if psutil is None:
                self.logger.error("psutil must be installed on MacOS to collect memory metrics")
                return False

            phys_memory = psutil.virtual_memory()
            swap = psutil.swap_memory()
            return {'physUsed': phys_memory.used / float(1024 ** 2),
                    'physFree': phys_memory.free / float(1024 ** 2),
                    'physUsable': phys_memory.available / float(1024 ** 2),
                    'physPctUsage': phys_memory.percent,
                    'swapUsed': swap.used / float(1024 ** 2),
                    'swapFree': swap.free / float(1024 ** 2)}

        elif sys.platform.startswith("freebsd"):
            try:
                output, _, _ = get_subprocess_output(['sysctl', 'vm.stats.vm'], self.logger)
                sysctl = output.splitlines()
            except Exception:
                self.logger.exception('getMemoryUsage')
                return False

            regexp = re.compile(r'^vm\.stats\.vm\.(\w+):\s+([0-9]+)')
            meminfo = {}

            for line in sysctl:
                try:
                    match = re.search(regexp, line)
                    if match is not None:
                        meminfo[match.group(1)] = match.group(2)
                except Exception:
                    self.logger.exception("Cannot parse sysctl vm.stats.vm output")

            memData = {}

            try:
                pageSize = int(meminfo.get('v_page_size'))

                memData['physTotal'] = (int(meminfo.get('v_page_count', 0))
                                        * pageSize) / 1048576
                memData['physFree'] = (int(meminfo.get('v_free_count', 0))
                                       * pageSize) / 1048576
                memData['physCached'] = (int(meminfo.get('v_cache_count', 0))
                                         * pageSize) / 1048576
                memData['physUsed'] = ((int(meminfo.get('v_active_count'), 0) +
                                        int(meminfo.get('v_wire_count', 0)))
                                       * pageSize) / 1048576
                memData['physUsable'] = ((int(meminfo.get('v_free_count'), 0) +
                                          int(meminfo.get('v_cache_count', 0)) +
                                          int(meminfo.get('v_inactive_count', 0))) *
                                         pageSize) / 1048576

                if memData['physTotal'] > 0:
                    memData['physPctUsage'] = (1 - (float(memData['physUsable']) / float(memData['physTotal']))) * 100
            except Exception:
                self.logger.exception('Cannot compute stats from /proc/meminfo')

            try:
                output, _, _ = get_subprocess_output(['swapinfo', '-m'], self.logger)
                sysctl = output.splitlines()
            except Exception:
                self.logger.exception('getMemoryUsage')
                return False

            assert "Device" in sysctl[0]

            try:
                memData['swapTotal'] = 0
                memData['swapFree'] = 0
                memData['swapUsed'] = 0
                for line in sysctl[1:]:
                    if len(line) > 0:
                        line = line.split()
                        memData['swapTotal'] += int(line[1])
                        memData['swapFree'] += int(line[3])
                        memData['swapUsed'] += int(line[2])
            except Exception:
                self.logger.exception('Cannot compute stats from swapinfo')

            return memData
        elif sys.platform == 'sunos5':
            try:
                memData = {}
                cmd = ["kstat", "-m", "memory_cap", "-c", "zone_memory_cap", "-p"]
                output, _, _ = get_subprocess_output(cmd, self.logger)
                kmem = output.splitlines()

                kv = [l.strip().split() for l in kmem if len(l) > 0]
                entries = dict([(k.split(":")[-1], v) for (k, v) in kv])
                convert = lambda v: int(long(v)) / 2 ** 20
                memData["physTotal"] = convert(entries["physcap"])
                memData["physUsed"] = convert(entries["rss"])
                memData["physFree"] = memData["physTotal"] - memData["physUsed"]
                memData["swapTotal"] = convert(entries["swapcap"])
                memData["swapUsed"] = convert(entries["swap"])
                memData["swapFree"] = memData["swapTotal"] - memData["swapUsed"]

                if memData['swapTotal'] > 0:
                    memData['swapPctFree'] = float(memData['swapFree']) / float(memData['swapTotal'])
                return memData
            except Exception:
                self.logger.exception("Cannot compute mem stats from kstat -c zone_memory_cap")
                return False
        else:
            return False


class Processes(Check):
    def check(self, agentConfig):
        process_exclude_args = agentConfig.get('exclude_process_args', False)
        if process_exclude_args:
            ps_arg = 'aux'
        else:
            ps_arg = 'auxww'
        try:
            output, _, _ = get_subprocess_output(['ps', ps_arg], self.logger)
            processLines = output.splitlines()
        except StandardError:
            self.logger.exception('getProcesses')
            return False

        processes = []

        for line in processLines:
            line = line.split(None, 10)
            processes.append(map(lambda s: s.strip(), line))

        return {'processes': processes,
                'apiKey': agentConfig['api_key'],
                'host': get_hostname(agentConfig)}


class Cpu(Check):
    def check(self, agentConfig):

        def format_results(us, sy, wa, idle, st, usage, guest=None):
            data = {'cpuUser': us, 'cpuSystem': sy, 'cpuWait': wa, 'cpuIdle': idle, 'cpuStolen': st, 'cpuUsage': usage,
                    'cpuGuest': guest}
            return dict((k, v) for k, v in data.iteritems() if v is not None)

        def get_value(legend, data, name, filter_value=None):
            if name in legend:
                value = to_float(data[legend.index(name)])
                if filter_value is not None:
                    if value > filter_value:
                        return None
                return value

            else:
                self.logger.debug("Cannot extract cpu value %s from %s (%s)" % (name, data, legend))
                return 0.0

        try:
            if Platform.is_linux():
                output, _, _ = get_subprocess_output(['mpstat', '1', '3'], self.logger)
                mpstat = output.splitlines()

                legend = [l for l in mpstat if "%usr" in l or "%user" in l]
                avg = [l for l in mpstat if "Average" in l]
                if not avg:
                    avg = [mpstat[-1]]
                if len(legend) == 1 and len(avg) == 1:
                    headers = [h for h in legend[0].split() if h not in ("AM", "PM")]
                    data = avg[0].split()

                    cpu_metrics = {
                        "%usr": None, "%user": None, "%nice": None,
                        "%iowait": None, "%idle": None, "%sys": None,
                        "%irq": None, "%soft": None, "%steal": None,
                        "%guest": None
                    }

                    for cpu_m in cpu_metrics:
                        cpu_metrics[cpu_m] = get_value(headers, data, cpu_m, filter_value=110)

                    if any([v is None for v in cpu_metrics.values()]):
                        self.logger.warning("Invalid mpstat data: %s" % data)

                    cpu_user = cpu_metrics["%usr"] + cpu_metrics["%user"] + cpu_metrics["%nice"]
                    cpu_system = cpu_metrics["%sys"] + cpu_metrics["%irq"] + cpu_metrics["%soft"]
                    cpu_wait = cpu_metrics["%iowait"]
                    cpu_idle = cpu_metrics["%idle"]
                    cpu_stolen = cpu_metrics["%steal"]
                    cpu_guest = cpu_metrics["%guest"]
                    cpu_usage = 100 - cpu_idle

                    return format_results(cpu_user,
                                          cpu_system,
                                          cpu_wait,
                                          cpu_idle,
                                          cpu_stolen,
                                          cpu_usage,
                                          cpu_guest)
                else:
                    return False

            elif sys.platform == 'darwin':
                iostats, _, _ = get_subprocess_output(['iostat', '-C', '-w', '3', '-c', '2'], self.logger)
                lines = [l for l in iostats.splitlines() if len(l) > 0]
                legend = [l for l in lines if "us" in l]
                if len(legend) == 1:
                    headers = legend[0].split()
                    data = lines[-1].split()
                    cpu_user = get_value(headers, data, "us")
                    cpu_sys = get_value(headers, data, "sy")
                    cpu_wait = 0
                    cpu_idle = get_value(headers, data, "id")
                    cpu_st = 0
                    cpu_usage = 100 - cpu_idle
                    return format_results(cpu_user, cpu_sys, cpu_wait, cpu_idle, cpu_st, cpu_usage)
                else:
                    self.logger.warn("Expected to get at least 4 lines of data from iostat instead of just " + str(
                        iostats[:max(80, len(iostats))]))
                    return False

            elif sys.platform.startswith("freebsd"):

                iostats, _, _ = get_subprocess_output(['iostat', '-w', '3', '-c', '2'], self.logger)
                lines = [l for l in iostats.splitlines() if len(l) > 0]
                legend = [l for l in lines if "us" in l]
                if len(legend) == 1:
                    headers = legend[0].split()
                    data = lines[-1].split()
                    cpu_user = get_value(headers, data, "us")
                    cpu_nice = get_value(headers, data, "ni")
                    cpu_sys = get_value(headers, data, "sy")
                    cpu_intr = get_value(headers, data, "in")
                    cpu_wait = 0
                    cpu_idle = get_value(headers, data, "id")
                    cpu_stol = 0
                    cpu_usage = 100 - cpu_idle
                    return format_results(cpu_user + cpu_nice, cpu_sys + cpu_intr, cpu_wait, cpu_idle, cpu_stol,
                                          cpu_usage)

                else:
                    self.logger.warn("Expected to get at least 4 lines of data from iostat instead of just " + str(
                        iostats[:max(80, len(iostats))]))
                    return False

            elif sys.platform == 'sunos5':
                output, _, _ = get_subprocess_output(['mpstat', '-aq', '1', '2'], self.logger)
                mpstat = output.splitlines()
                lines = [l for l in mpstat if len(l) > 0]
                lines = lines[len(lines) / 2:]
                legend = [l for l in lines if "SET" in l]
                assert len(legend) == 1
                if len(legend) == 1:
                    headers = legend[0].split()

                    d_lines = [l for l in lines if "SET" not in l]
                    user = [get_value(headers, l.split(), "usr") for l in d_lines]
                    kern = [get_value(headers, l.split(), "sys") for l in d_lines]
                    wait = [get_value(headers, l.split(), "wt") for l in d_lines]
                    idle = [get_value(headers, l.split(), "idl") for l in d_lines]
                    size = [get_value(headers, l.split(), "sze") for l in d_lines]
                    count = sum(size)
                    rel_size = [s / count for s in size]
                    dot = lambda v1, v2: reduce(operator.add, map(operator.mul, v1, v2))
                    return format_results(dot(user, rel_size),
                                          dot(kern, rel_size),
                                          dot(wait, rel_size),
                                          dot(idle, rel_size),
                                          0.0,
                                          100 - dot(idle, rel_size))
            else:
                self.logger.warn("CPUStats: unsupported platform")
                return False
        except Exception:
            self.logger.exception("Cannot compute CPU stats")
            return False


class System(Check):
    def check(self, agentConfig):
        return {"system.uptime": uptime.uptime()}


def main():
    import logging

    logging.basicConfig(level=logging.DEBUG, format='%(asctime)-15s %(message)s')
    log = logging.getLogger()
    cpu = Cpu(log)
    io = IO(log)
    load = Load(log)
    mem = Memory(log)
    system = System(log)

    config = {"api_key": "666", "device_blacklist_re": re.compile('.*disk0.*')}
    while True:
        print("=" * 10)
        print("--- IO ---")
        print(io.check(config))
        print("--- CPU ---")
        print(cpu.check(config))
        print("--- Load ---")
        print(load.check(config))
        print("--- Memory ---")
        print(mem.check(config))
        print("--- System ---")
        print(system.check(config))
        print("\n\n\n")
        time.sleep(1)


if __name__ == '__main__':
    main()
