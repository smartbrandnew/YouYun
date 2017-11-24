#coding:utf-8

import wmi
import sys 
from checks import AgentCheck

class Hyper_v(AgentCheck):
	#继承父类AgentCheck
	
	def __init__(self, name, init_config, agentConfig, instances=None):
		AgentCheck.__init__(self, name, init_config, agentConfig, instances)
		self.NAMESAPCE = r"root\virtualization\v2"
		self.SystemName = "" 
		self.conn = ""
		self.ComputerName=""
		
	#NAMESAPCE 默认是root\virtualization\v2
	#SystemName是一串字符串EE6CCB0F-D88E-4EE7-9E3F-ECBBC6185B32

	def get_config(self,instance):
	
		#Host，UserName，PassWord 可在yaml配置文件中配置
		#获取yaml配置文件中对应的配置
		Host = instance.get("host",'')
		self.ComputerName = instance.get("computername",'')
		UserName = instance.get("username",'')
		PassWord = instance.get("password",'')
		
		return Host,UserName,PassWord
		
	def check(self,instance):
	
		Host,UserName,PassWord = self.get_config(instance)
		try:
			connection = wmi.connect_server(server=Host,namespace=self.NAMESAPCE,user=UserName,password=PassWord)
			conn = wmi.WMI(wmi=connection)
			#使用wmi进行连接虚拟机
			
			# self.log.debug("connect success ")
		except Exception as e:
			self.log.exception(e)
			sys.exit()

		self.conn = conn
		self.get_computer_imformation()
		self.get_cpu_imformation()
		self.get_memory_information()
		self.get_disk_imformation()
		#调用方法，，获得相应的属性值

	def get_computer_imformation(self):
		#获得电脑相应的属性值
		#通过对应的计算机名获取系统名字
		computer = self.conn.Msvm_ComputerSystem(ElementName=self.ComputerName)
		#获得一个包含instance对象的list对象，
		for item in computer:
			self.SystemName = item.Name
		#调用上传方法，上传数据
		self.up_data("hyper-v.computer.name",self.ComputerName)
		
		
	def get_cpu_imformation(self):
	
		#获得CPU相应的属性值
		cpu = self.conn.Msvm_Processor(SystemName=self.SystemName)
		#获得一个包含instance对象的list对象，
		for item in cpu:
			number = item.CPUStatus
			database = item.DataWidth
			percent = item.LoadPercentage
			speed = item.CurrentClockSpeed
			#调用上传方法，上传数据
			self.up_data("hyper-v.cpu.rate",speed)
			self.up_data("hyper-v.cpu.database",database)
			self.up_data("hyper-v.cpu.number",number)
			self.up_data("hyper-v.cpu.usage",percent)

	def get_disk_imformation(self):
	
		#获得硬盘相应的属性值
		
		disk = self.conn.Msvm_LogicalDisk(SystemName= self.SystemName)
		#获得一个包含instance对象的list对象，
		for item in disk:
			size ,number = int(item.BlockSize),int(item.NumberOfBlocks)
			#计算并转换成相应的单位
			total = size*number/1024/1024/1024-1
			#调用上传方法，上传数据
			self.up_data("hyper-v.disk.total",total)

	def get_memory_information(self):
	
		#获得内存相应的属性值
		memory = self.conn.Msvm_Memory(SystemName = self.SystemName)
		#获得一个包含instance对象的list对象，
		for item in memory:
			size , number = int(item.BlockSize) , int(item.NumberOfBlocks)
			#计算并转换成相应的单位
			total = size*number/1024/1024
			#调用上传方法，上传数据
			self.up_data("hyper-v.ram.total",total)

	def up_data(self,name,value,tags=None):
		#封装成函数以后可拓展，上传指标方法
		self.gauge(name, value, tags)
		

	# def get_filepath_information(self):

	# 	filepath = conn.Msvm_VirtualSystemSettingData(InstanceID = "Microsoft:"+self.SystemName)
	# 	for i in filepath:
	#    		print i.ConfigurationDataRoot

