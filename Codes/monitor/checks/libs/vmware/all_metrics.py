CPU_METRICS = {
    'cpu.capacity.contention': {
        's_type': 'rate',
        'unit': 'percent',
        'rollup': 'average',
        'entity': ['ResourcePool']
    },
    'cpu.capacity.demand': {
        's_type': 'rate',
        'unit': 'megaHertz',
        'rollup': 'average',
        'entity': ['ResourcePool']
    },
    'cpu.capacity.entitlement': {
        's_type': 'absolute',
        'unit': 'megaHertz',
        'rollup': 'average',
        'entity': ['ResourcePool']
    },

    'cpu.capacity.provisioned': {
        's_type': 'absolute',
        'unit': 'megaHertz',
        'rollup': 'average',
        'entity': []
    },

    'cpu.capacity.usage': {
        's_type': 'rate',
        'unit': 'megaHertz',
        'rollup': 'average',
        'entity': ['ResourcePool']
    },

    'cpu.coreUtilization': {
        's_type': 'rate',
        'unit': 'percent',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'cpu.corecount.contention': {
        's_type': 'rate',
        'unit': 'percent',
        'rollup': 'average',
        'entity': ['ResourcePool']
    },

    'cpu.corecount.provisioned': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['ResourcePool']
    },

    'cpu.corecount.usage': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'average',
        'entity': []
    },

    'cpu.costop': {
        's_type': 'delta',
        'unit': 'millisecond',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'cpu.cpuentitlement': {
        's_type': 'absolute',
        'unit': 'megaHertz',
        'rollup': 'latest',
        'entity': ['ResourcePool']
    },

    'cpu.demand': {
        's_type': 'rate',
        'unit': 'megaHertz',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'cpu.entitlement': {
        's_type': 'absolute',
        'unit': 'megaHertz',
        'rollup': 'latest',
        'entity': ['VirtualMachine']
    },

    'cpu.extra': {
        's_type': 'delta',
        'unit': 'millisecond',
        'rollup': 'summation',
        'entity': ['VirtualMachine']
    },

    'cpu.guaranteed': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'latest',
        'entity': ['VirtualMachine']
    },
    'cpu.idle': {
        's_type': 'delta',
        'unit': 'millisecond',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'cpu.latency': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'cpu.maxlimited': {
        's_type': 'delta',
        'unit': 'millisecond',
        'rollup': 'summation',
        'entity': ['VirtualMachine']
    },

    'cpu.overlap': {
        's_type': 'delta',
        'unit': 'millisecond',
        'rollup': 'summation',
        'entity': ['VirtualMachine']
    },

    'cpu.ready': {
        's_type': 'delta',
        'unit': 'millisecond',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },
    'cpu.reservedCapacity': {
        's_type': 'absolute',
        'unit': 'megaHertz',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'cpu.run': {
        's_type': 'delta',
        'unit': 'millisecond',
        'rollup': 'summation',
        'entity': ['VirtualMachine']
    },

    'cpu.swapwait': {
        's_type': 'delta',
        'unit': 'millisecond',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'cpu.system': {
        's_type': 'delta',
        'unit': 'millisecond',
        'rollup': 'summation',
        'entity': ['VirtualMachine']
    },

    'cpu.totalCapacity': {
        's_type': 'absolute',
        'unit': 'megaHertz',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'cpu.totalmhz': {
        's_type': 'rate',
        'unit': 'megaHertz',
        'rollup': 'average',
        'entity': []
    },

    'cpu.usage': {
        's_type': 'rate',
        'unit': 'percent',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'cpu.usagemhz': {
        's_type': 'rate',
        'unit': 'megaHertz',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'ResourcePool']
    },

    'cpu.used': {
        's_type': 'delta',
        'unit': 'millisecond',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'cpu.utilization': {
        's_type': 'rate',
        'unit': 'percent',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'cpu.wait': {
        's_type': 'delta',
        'unit': 'millisecond',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },
}

DATASTORE_METRICS = {

    'datastore.busResets': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['Datastore']
    },

    'datastore.commandsAborted': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['Datastore']
    },

    'datastore.datastoreIops': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'datastore.datastoreMaxQueueDepth': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'datastore.datastoreNormalReadLatency': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'datastore.datastoreNormalWriteLatency': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'datastore.datastoreReadBytes': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'datastore.datastoreReadIops': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'datastore.datastoreReadLoadMetric': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'datastore.datastoreReadOIO': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'datastore.datastoreWriteBytes': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'datastore.datastoreWriteIops': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'datastore.datastoreWriteLoadMetric': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'datastore.datastoreWriteOIO': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'datastore.maxTotalLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'datastore.numberReadAveraged': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'Datastore']
    },

    'datastore.numberWriteAveraged': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'Datastore']
    },

    'datastore.read': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'Datastore']
    },

    'datastore.sizeNormalizedDatastoreLatency': {
        's_type': 'absolute',
        'unit': 'microsecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'datastore.throughput.contention': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['Datastore']
    },

    'datastore.throughput.usage': {
        's_type': 'absolute',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['Datastore']
    },

    'datastore.totalReadLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'datastore.totalWriteLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'datastore.write': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'Datastore']
    },
}

DISK_METRICS = {

    'disk.busResets': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem', 'Datastore']
    },

    'disk.capacity.contention': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'average',
        'entity': ['Datastore']
    },

    'disk.capacity.provisioned': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['Datastore']
    },

    'disk.capacity.usage': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['Datastore']
    },

    'disk.commands': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'disk.commandsAborted': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem', 'Datastore']
    },

    'disk.commandsAveraged': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'disk.deltaused': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'latest',
        'entity': []
    },

    'disk.deviceLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'disk.deviceReadLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'disk.deviceWriteLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'disk.kernelLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'disk.kernelReadLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'disk.kernelWriteLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'disk.maxQueueDepth': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'disk.maxTotalLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'disk.numberRead': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'disk.numberReadAveraged': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'Datastore']
    },

    'disk.numberWrite': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'disk.numberWriteAveraged': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'Datastore']
    },

    'disk.queueLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'disk.queueReadLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'disk.queueWriteLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'disk.read': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'Datastore']
    },

    'disk.scsiReservationCnflctsPct': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'average',
        'entity': []
    },

    'disk.scsiReservationConflicts': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': []
    },

    'disk.throughput.contention': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['ResourcePool']
    },

    'disk.throughput.usage': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['ResourcePool']
    },

    'disk.totalLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystemDatastore']
    },

    'disk.totalReadLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'disk.totalWriteLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'disk.usage': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'disk.write': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'Datastore']
    },
}

HBR_METRICS = {

    'hbr.hbrNetRx': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'hbr.hbrNetTx': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'hbr.hbrNumVms': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['HostSystem']
    },
}

MANAGEMENTAGENT_METRICS = {

    'managementAgent.cpuUsage': {
        's_type': 'rate',
        'unit': 'megaHertz',
        'rollup': 'average',
        'entity': []
    },

    'managementAgent.memUsed': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'managementAgent.swapIn': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'managementAgent.swapOut': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'managementAgent.swapUsed': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['HostSystem']
    },
}

MEM_METRICS = {

    'mem.active': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'ResourcePool']
    },

    'mem.activewrite': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'mem.capacity.contention': {
        's_type': 'rate',
        'unit': 'percent',
        'rollup': 'average',
        'entity': ['ResourcePool']
    },

    'mem.capacity.entitlement': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['ResourcePool']
    },

    'mem.capacity.provisioned': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['ResourcePool']
    },

    'mem.capacity.usable': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': []
    },

    'mem.capacity.usage': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['ResourcePool']
    },

    'mem.capacity.usage.userworld': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': []
    },

    'mem.capacity.usage.vm': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': []
    },

    'mem.capacity.usage.vmOvrhd': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': []
    },

    'mem.capacity.usage.vmkOvrhd': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': []
    },

    'mem.compressed': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'ResourcePool']
    },

    'mem.compressionRate': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'ResourcePool']
    },

    'mem.consumed': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'ResourcePool']
    },

    'mem.consumed.userworlds': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': []
    },

    'mem.consumed.vms': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': []
    },

    'mem.decompressionRate': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'ResourcePool']
    },

    'mem.entitlement': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine']
    },

    'mem.granted': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'ResourcePool']
    },

    'mem.heap': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'mem.heapfree': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'mem.latency': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'mem.llSwapIn': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'mem.llSwapInRate': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'mem.llSwapOut': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'mem.llSwapOutRate': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'mem.llSwapUsed': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'mem.lowfreethreshold': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'mem.mementitlement': {
        's_type': 'absolute',
        'unit': 'megaBytes',
        'rollup': 'latest',
        'entity': ['ResourcePool']
    },

    'mem.overhead': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'ResourcePool']
    },

    'mem.overheadMax': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine']
    },

    'mem.overheadTouched': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine']
    },

    'mem.reservedCapacity': {
        's_type': 'absolute',
        'unit': 'megaBytes',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'mem.reservedCapacity.userworld': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': []
    },

    'mem.reservedCapacity.vm': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': []
    },

    'mem.reservedCapacity.vmOvhd': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': []
    },

    'mem.reservedCapacity.vmkOvrhd': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': []
    },

    'mem.reservedCapacityPct': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'average',
        'entity': []
    },

    'mem.shared': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'ResourcePool']
    },

    'mem.sharedcommon': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'mem.state': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'mem.swapin': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'mem.swapinRate': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'mem.swapout': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'mem.swapoutRate': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'mem.swapped': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'ResourcePool']
    },

    'mem.swaptarget': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine']
    },

    'mem.swapunreserved': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': []
    },

    'mem.swapused': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'mem.sysUsage': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'mem.totalCapacity': {
        's_type': 'absolute',
        'unit': 'megaBytes',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'mem.totalmb': {
        's_type': 'absolute',
        'unit': 'megaBytes',
        'rollup': 'average',
        'entity': []
    },

    'mem.unreserved': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'mem.usage': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'mem.vmmemctl': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'ResourcePool']
    },

    'mem.vmmemctltarget': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine']
    },

    'mem.zero': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'ResourcePool']
    },

    'mem.zipSaved': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'latest',
        'entity': ['VirtualMachine']
    },

    'mem.zipped': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'latest',
        'entity': ['VirtualMachine']
    },
}

NETWORK_METRICS = {

    'network.broadcastRx': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'network.broadcastTx': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'network.bytesRx': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'network.bytesTx': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'network.droppedRx': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'network.droppedTx': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'network.errorsRx': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['HostSystem']
    },

    'network.errorsTx': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['HostSystem']
    },

    'network.multicastRx': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'network.multicastTx': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'network.packetsRx': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'network.packetsTx': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'network.received': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'network.throughput.contention': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['ResourcePool']
    },

    'network.throughput.packetsPerSec': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': []
    },

    'network.throughput.provisioned': {
        's_type': 'absolute',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': []
    },

    'network.throughput.usable': {
        's_type': 'absolute',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': []
    },

    'network.throughput.usage': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['ResourcePool']
    },

    'network.throughput.usage.ft': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': []
    },

    'network.throughput.usage.hbr': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': []
    },

    'network.throughput.usage.iscsi': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': []
    },

    'network.throughput.usage.nfs': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': []
    },

    'network.throughput.usage.vm': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': []
    },

    'network.throughput.usage.vmotion': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': []
    },

    'network.transmitted': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'network.unknownProtos': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['HostSystem']
    },

    'network.usage': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem']
    },
}

POWER_METRICS = {

    'power.capacity.usable': {
        's_type': 'absolute',
        'unit': 'watt',
        'rollup': 'average',
        'entity': []
    },

    'power.capacity.usage': {
        's_type': 'absolute',
        'unit': 'watt',
        'rollup': 'average',
        'entity': []
    },
    'power.capacity.usagePct': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'average',
        'entity': []
    },

    'power.energy': {
        's_type': 'delta',
        'unit': 'joule',
        'rollup': 'summation',
        'entity': ['VirtualMachine', 'HostSystem', 'ResourcePool']
    },

    'power.power': {
        's_type': 'absolute',
        'unit': 'watt',
        'rollup': 'average',
        'entity': ['VirtualMachine', 'HostSystem', 'ResourcePool']
    },

    'power.powerCap': {
        's_type': 'absolute',
        'unit': 'watt',
        'rollup': 'average',
        'entity': ['HostSystem']
    },
}

RESCPU_METRICS = {

    'rescpu.actav1': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.actav15': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.actav5': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.actpk1': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.actpk15': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.actpk5': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.maxLimited1': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.maxLimited15': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.maxLimited5': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.runav1': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.runav15': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.runav5': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.runpk1': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.runpk15': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.runpk5': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.sampleCount': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },

    'rescpu.samplePeriod': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },
}

STORAGEADAPTER_METRICS = {

    'storageAdapter.OIOsPct': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'average',
        'entity': []
    },

    'storageAdapter.commandsAveraged': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'storageAdapter.maxTotalLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'storageAdapter.numberReadAveraged': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'storageAdapter.numberWriteAveraged': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'storageAdapter.outstandingIOs': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'average',
        'entity': []
    },

    'storageAdapter.queueDepth': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'average',
        'entity': []
    },

    'storageAdapter.queueLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': []
    },

    'storageAdapter.queued': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'average',
        'entity': []
    },

    'storageAdapter.read': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'storageAdapter.throughput.cont': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': []
    },

    'storageAdapter.throughput.usag': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': []
    },

    'storageAdapter.totalReadLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'storageAdapter.totalWriteLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'storageAdapter.write': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },
}

STORAGEPATH_METRICS = {

    'storagePath.busResets': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': []
    },

    'storagePath.commandsAborted': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': []
    },

    'storagePath.commandsAveraged': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'storagePath.maxTotalLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'storagePath.numberReadAveraged': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'storagePath.numberWriteAveraged': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'storagePath.read': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'storagePath.throughput.cont': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': []
    },

    'storagePath.throughput.usage': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': []
    },

    'storagePath.totalReadLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'storagePath.totalWriteLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'storagePath.write': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['HostSystem']
    },
}

SYSTEM_METRICS = {

    'system.cosDiskUsage': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.diskUsage': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.heartbeat': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': ['VirtualMachine']
    },

    'system.osUptime': {
        's_type': 'absolute',
        'unit': 'second',
        'rollup': 'latest',
        'entity': ['VirtualMachine']
    },

    'system.resourceCpuAct1': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceCpuAct5': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceCpuAllocMax': {
        's_type': 'absolute',
        'unit': 'megaHertz',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceCpuAllocMin': {
        's_type': 'absolute',
        'unit': 'megaHertz',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceCpuAllocShares': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceCpuMaxLimited1': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceCpuMaxLimited5': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceCpuRun1': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceCpuRun5': {
        's_type': 'absolute',
        'unit': 'percent',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceCpuUsage': {
        's_type': 'rate',
        'unit': 'megaHertz',
        'rollup': 'average',
        'entity': ['HostSystem']
    },

    'system.resourceMemAllocMax': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceMemAllocMin': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceMemAllocShares': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceMemCow': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceMemMapped': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceMemOverhead': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceMemShared': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceMemSwapped': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceMemTouched': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.resourceMemZero': {
        's_type': 'absolute',
        'unit': 'kiloBytes',
        'rollup': 'latest',
        'entity': ['HostSystem']
    },

    'system.uptime': {
        's_type': 'absolute',
        'unit': 'second',
        'rollup': 'latest',
        'entity': ['VirtualMachine', 'HostSystem']
    },
}

VIRTUALDISK_METRICS = {

    'virtualDisk.busResets': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': []
    },

    'virtualDisk.commandsAborted': {
        's_type': 'delta',
        'unit': 'number',
        'rollup': 'summation',
        'entity': []
    },

    'virtualDisk.numberReadAveraged': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['VirtualMachine']
    },

    'virtualDisk.numberWriteAveraged': {
        's_type': 'rate',
        'unit': 'number',
        'rollup': 'average',
        'entity': ['VirtualMachine']
    },

    'virtualDisk.read': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine']
    },

    'virtualDisk.readLoadMetric': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['VirtualMachine']
    },

    'virtualDisk.readOIO': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['VirtualMachine']
    },

    'virtualDisk.throughput.cont': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': []
    },

    'virtualDisk.throughput.usage': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': []
    },

    'virtualDisk.totalReadLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['VirtualMachine']
    },

    'virtualDisk.totalWriteLatency': {
        's_type': 'absolute',
        'unit': 'millisecond',
        'rollup': 'average',
        'entity': ['VirtualMachine']
    },

    'virtualDisk.write': {
        's_type': 'rate',
        'unit': 'kiloBytesPerSecond',
        'rollup': 'average',
        'entity': ['VirtualMachine']
    },

    'virtualDisk.writeLoadMetric': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['VirtualMachine']
    },

    'virtualDisk.writeOIO': {
        's_type': 'absolute',
        'unit': 'number',
        'rollup': 'latest',
        'entity': ['VirtualMachine']
    },
}

ALL_METRICS = {}
ALL_METRICS.update(CPU_METRICS)
ALL_METRICS.update(DATASTORE_METRICS)
ALL_METRICS.update(DISK_METRICS)
ALL_METRICS.update(HBR_METRICS)
ALL_METRICS.update(MANAGEMENTAGENT_METRICS)
ALL_METRICS.update(MEM_METRICS)
ALL_METRICS.update(NETWORK_METRICS)
ALL_METRICS.update(POWER_METRICS)
ALL_METRICS.update(RESCPU_METRICS)
ALL_METRICS.update(STORAGEADAPTER_METRICS)
ALL_METRICS.update(STORAGEPATH_METRICS)
ALL_METRICS.update(VIRTUALDISK_METRICS)
