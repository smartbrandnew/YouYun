import uptime

from checks import Check

try:
    import psutil
except ImportError:
    psutil = None

try:
    from checks.libs.wmi.sampler import WMISampler
except Exception:
    def WMISampler(*args, **kwargs):

        return

from utils.timeout import TimeoutException


class DriveType(object):
    UNKNOWN, NOROOT, REMOVEABLE, LOCAL, NETWORK, CD, RAM = (0, 1, 2, 3, 4, 5, 6)


B2MB = float(1048576)
KB2MB = B2KB = float(1024)


def should_ignore_disk(name, blacklist_re):
    return name == '_total' or blacklist_re is not None and blacklist_re.match(name)


class Processes(Check):
    def __init__(self, logger):
        Check.__init__(self, logger)

        self.wmi_sampler = WMISampler(
            logger,
            "Win32_PerfRawData_PerfOS_System",
            ["ProcessorQueueLength", "Processes"]
        )

        self.gauge('system.proc.queue_length')
        self.gauge('system.proc.count')

    def check(self, agentConfig):
        try:
            self.wmi_sampler.sample()
        except TimeoutException:
            self.logger.warning(
                u"Timeout while querying Win32_PerfRawData_PerfOS_System WMI class."
                u" Processes metrics will be returned at next iteration."
            )
            return []

        if not (len(self.wmi_sampler)):
            self.logger.warning('Missing Win32_PerfRawData_PerfOS_System WMI class.'
                                ' No process metrics will be returned.')
            return []

        os = self.wmi_sampler[0]
        processor_queue_length = os.get('ProcessorQueueLength')
        processes = os.get('Processes')

        if processor_queue_length is not None:
            self.save_sample('system.proc.queue_length', processor_queue_length)
        if processes is not None:
            self.save_sample('system.proc.count', processes)

        return self.get_metrics()


class Memory(Check):
    def __init__(self, logger):
        Check.__init__(self, logger)

        self.os_wmi_sampler = WMISampler(
            logger,
            "Win32_OperatingSystem",
            ["TotalVisibleMemorySize", "FreePhysicalMemory"]
        )
        self.mem_wmi_sampler = WMISampler(
            logger,
            "Win32_PerfRawData_PerfOS_Memory",
            ["CacheBytes", "CommittedBytes", "PoolPagedBytes", "PoolNonpagedBytes"])

        self.gauge('system.mem.free')
        self.gauge('system.mem.used')
        self.gauge('system.mem.total')

        self.gauge('system.mem.cached')

        self.gauge('system.mem.committed')

        self.gauge('system.mem.paged')

        self.gauge('system.mem.nonpaged')
        self.gauge('system.mem.usable')
        self.gauge('system.mem.pct_usage')

    def check(self, agentConfig):
        try:
            self.os_wmi_sampler.sample()
        except TimeoutException:
            self.logger.warning(
                u"Timeout while querying Win32_OperatingSystem WMI class."
                u" Memory metrics will be returned at next iteration."
            )
            return []

        if not (len(self.os_wmi_sampler)):
            self.logger.warning('Missing Win32_OperatingSystem WMI class.'
                                ' No memory metrics will be returned.')
            return []

        os = self.os_wmi_sampler[0]

        total = 0
        free = 0
        cached = 0

        total_visible_memory_size = os.get('TotalVisibleMemorySize')
        free_physical_memory = os.get('FreePhysicalMemory')

        if total_visible_memory_size is not None and free_physical_memory is not None:
            total = int(total_visible_memory_size) / KB2MB
            free = int(free_physical_memory) / KB2MB
            self.save_sample('system.mem.total', total)
            self.save_sample('system.mem.free', free)
            self.save_sample('system.mem.used', total - free)

        try:
            self.mem_wmi_sampler.sample()
        except TimeoutException:
            self.logger.warning(
                u"Timeout while querying Win32_PerfRawData_PerfOS_Memory WMI class."
                u" Memory metrics will be returned at next iteration."
            )
            return

        if not (len(self.mem_wmi_sampler)):
            self.logger.info('Missing Win32_PerfRawData_PerfOS_Memory WMI class.'
                             ' No memory metrics will be returned.')
            return self.get_metrics()

        mem = self.mem_wmi_sampler[0]

        cache_bytes = mem.get('CacheBytes')
        committed_bytes = mem.get('CommittedBytes')
        pool_paged_bytes = mem.get('PoolPagedBytes')
        pool_non_paged_bytes = mem.get('PoolNonpagedBytes')

        if cache_bytes is not None:
            cached = int(cache_bytes) / B2MB
            self.save_sample('system.mem.cached', cached)
        if committed_bytes is not None:
            self.save_sample('system.mem.committed', int(committed_bytes) / B2MB)
        if pool_paged_bytes is not None:
            self.save_sample('system.mem.paged', int(pool_paged_bytes) / B2MB)
        if pool_non_paged_bytes is not None:
            self.save_sample('system.mem.nonpaged', int(pool_non_paged_bytes) / B2MB)

        usable = free + cached
        self.save_sample('system.mem.usable', usable)
        if total > 0:
            pct_usable = float(usable) / total
            pct_usage = (1 - pct_usable) * 100
            self.save_sample('system.mem.pct_usage', pct_usage)

        return self.get_metrics()


class Cpu(Check):
    def __init__(self, logger):
        Check.__init__(self, logger)

        self.wmi_sampler = WMISampler(
            logger,
            "Win32_PerfRawData_PerfOS_Processor",
            ["Name", "PercentInterruptTime"]
        )

        self.gauge('system.cpu.user')
        self.gauge('system.cpu.idle')
        self.gauge('system.cpu.interrupt')
        self.gauge('system.cpu.system')
        self.gauge('system.cpu.pct_usage')

    def check(self, agentConfig):
        try:
            self.wmi_sampler.sample()
        except TimeoutException:
            self.logger.warning(
                u"Timeout while querying Win32_PerfRawData_PerfOS_Processor WMI class."
                u" CPU metrics will be returned at next iteration."
            )
            return []

        if not (len(self.wmi_sampler)):
            self.logger.warning('Missing Win32_PerfRawData_PerfOS_Processor WMI class.'
                                ' No CPU metrics will be returned')
            return []

        cpu_interrupt = self._average_metric(self.wmi_sampler, 'PercentInterruptTime')
        if cpu_interrupt is not None:
            self.save_sample('system.cpu.interrupt', cpu_interrupt)

        cpu_percent = psutil.cpu_times_percent()

        self.save_sample('system.cpu.user', cpu_percent.user)
        self.save_sample('system.cpu.idle', cpu_percent.idle)
        self.save_sample('system.cpu.system', cpu_percent.system)
        self.save_sample('system.cpu.pct_usage', cpu_percent.system + cpu_percent.user)

        return self.get_metrics()

    def _average_metric(self, sampler, wmi_prop):

        val = 0
        counter = 0
        for wmi_object in sampler:
            if wmi_object['Name'] == '_Total':
                continue

            wmi_prop_value = wmi_object.get(wmi_prop)
            if wmi_prop_value is not None:
                counter += 1
                val += float(wmi_prop_value)

        if counter > 0:
            return val / counter

        return val


class Network(Check):
    def __init__(self, logger):
        Check.__init__(self, logger)

        self.wmi_sampler = WMISampler(
            logger,
            "Win32_PerfRawData_Tcpip_NetworkInterface",
            ["Name", "BytesReceivedPerSec", "BytesSentPerSec"]
        )

        self.gauge('system.net.bytes_rcvd')
        self.gauge('system.net.bytes_sent')

    def check(self, agentConfig):
        try:
            self.wmi_sampler.sample()
        except TimeoutException:
            self.logger.warning(
                u"Timeout while querying Win32_PerfRawData_Tcpip_NetworkInterface WMI class."
                u" Network metrics will be returned at next iteration."
            )
            return []

        if not (len(self.wmi_sampler)):
            self.logger.warning('Missing Win32_PerfRawData_Tcpip_NetworkInterface WMI class.'
                                ' No network metrics will be returned')
            return []

        for iface in self.wmi_sampler:
            name = iface.get('Name')
            bytes_received_per_sec = iface.get('BytesReceivedPerSec')
            bytes_sent_per_sec = iface.get('BytesSentPerSec')

            name = self.normalize_device_name(name)
            if bytes_received_per_sec is not None:
                self.save_sample('system.net.bytes_rcvd', bytes_received_per_sec,
                                 device_name=name)
            if bytes_sent_per_sec is not None:
                self.save_sample('system.net.bytes_sent', bytes_sent_per_sec,
                                 device_name=name)
        return self.get_metrics()


class IO(Check):
    def __init__(self, logger):
        Check.__init__(self, logger)

        self.wmi_sampler = WMISampler(
            logger,
            "Win32_PerfRawData_PerfDisk_LogicalDisk",
            ["Name", "DiskWriteBytesPerSec", "DiskWritesPerSec", "DiskReadBytesPerSec",
             "DiskReadsPerSec", "CurrentDiskQueueLength"]
        )

        self.gauge('system.io.wkb_s')
        self.gauge('system.io.w_s')
        self.gauge('system.io.rkb_s')
        self.gauge('system.io.r_s')
        self.gauge('system.io.avg_q_sz')

    def check(self, agentConfig):
        try:
            self.wmi_sampler.sample()
        except TimeoutException:
            self.logger.warning(
                u"Timeout while querying Win32_PerfRawData_PerfDisk_LogicalDiskUnable WMI class."
                u" I/O metrics will be returned at next iteration."
            )
            return []

        if not (len(self.wmi_sampler)):
            self.logger.warning('Missing Win32_PerfRawData_PerfDisk_LogicalDiskUnable WMI class.'
                                ' No I/O metrics will be returned.')
            return []

        blacklist_re = agentConfig.get('device_blacklist_re', None)
        for device in self.wmi_sampler:
            name = device.get('Name')
            disk_write_bytes_per_sec = device.get('DiskWriteBytesPerSec')
            disk_writes_per_sec = device.get('DiskWritesPerSec')
            disk_read_bytes_per_sec = device.get('DiskReadBytesPerSec')
            disk_reads_per_sec = device.get('DiskReadsPerSec')
            current_disk_queue_length = device.get('CurrentDiskQueueLength')

            name = self.normalize_device_name(name)
            if should_ignore_disk(name, blacklist_re):
                continue
            if disk_write_bytes_per_sec is not None:
                self.save_sample('system.io.wkb_s', int(disk_write_bytes_per_sec) / B2KB,
                                 device_name=name)
            if disk_writes_per_sec is not None:
                self.save_sample('system.io.w_s', int(disk_writes_per_sec),
                                 device_name=name)
            if disk_read_bytes_per_sec is not None:
                self.save_sample('system.io.rkb_s', int(disk_read_bytes_per_sec) / B2KB,
                                 device_name=name)
            if disk_reads_per_sec is not None:
                self.save_sample('system.io.r_s', int(disk_reads_per_sec),
                                 device_name=name)
            if current_disk_queue_length is not None:
                self.save_sample('system.io.avg_q_sz', current_disk_queue_length,
                                 device_name=name)
        return self.get_metrics()


class System(Check):
    def __init__(self, logger):
        Check.__init__(self, logger)
        self.gauge('system.uptime')

    def check(self, agentConfig):
        self.save_sample('system.uptime', uptime.uptime())

        return self.get_metrics()