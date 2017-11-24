import sys

import wmi

from checks import AgentCheck


class Hyper_v(AgentCheck):
    def __init__(self, name, init_config, agentConfig, instances=None):
        AgentCheck.__init__(self, name, init_config, agentConfig, instances)
        self.NAMESAPCE = r"root\virtualization\v2"
        self.SystemName = ""
        self.conn = ""
        self.ComputerName = ""

    def get_config(self, instance):

        Host = instance.get("host", '')
        self.ComputerName = instance.get("computername", '')
        UserName = instance.get("username", '')
        PassWord = instance.get("password", '')

        return Host, UserName, PassWord

    def check(self, instance):

        Host, UserName, PassWord = self.get_config(instance)
        try:
            connection = wmi.connect_server(server=Host, namespace=self.NAMESAPCE, user=UserName, password=PassWord)
            conn = wmi.WMI(wmi=connection)

        except Exception as e:
            self.log.exception(e)
            sys.exit()

        self.conn = conn
        self.get_computer_imformation()
        self.get_cpu_imformation()
        self.get_memory_information()
        self.get_disk_imformation()

    def get_computer_imformation(self):
        computer = self.conn.Msvm_ComputerSystem(ElementName=self.ComputerName)
        for item in computer:
            self.SystemName = item.Name
        self.up_data("hyper-v.computer.name", self.ComputerName)

    def get_cpu_imformation(self):

        cpu = self.conn.Msvm_Processor(SystemName=self.SystemName)
        for item in cpu:
            number = item.CPUStatus
            database = item.DataWidth
            percent = item.LoadPercentage
            speed = item.CurrentClockSpeed
            self.up_data("hyper-v.cpu.rate", speed)
            self.up_data("hyper-v.cpu.database", database)
            self.up_data("hyper-v.cpu.number", number)
            self.up_data("hyper-v.cpu.usage", percent)

    def get_disk_imformation(self):

        disk = self.conn.Msvm_LogicalDisk(SystemName=self.SystemName)
        for item in disk:
            size, number = int(item.BlockSize), int(item.NumberOfBlocks)
            total = size * number / 1024 / 1024 / 1024 - 1
            self.up_data("hyper-v.disk.total", total)

    def get_memory_information(self):

        memory = self.conn.Msvm_Memory(SystemName=self.SystemName)
        for item in memory:
            size, number = int(item.BlockSize), int(item.NumberOfBlocks)
            total = size * number / 1024 / 1024
            self.up_data("hyper-v.ram.total", total)

    def up_data(self, name, value, tags=None):
        self.gauge(name, value, tags)
