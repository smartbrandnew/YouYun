package com.broada.carrier.monitor.impl.host.cli;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.host.cli.cpu.CLICPUMonitor;
import com.broada.carrier.monitor.impl.host.cli.cpu.CLICPUParamConfiger;
import com.broada.carrier.monitor.impl.host.cli.disk.CLIDiskMonitor;
import com.broada.carrier.monitor.impl.host.cli.disk.CLIDiskParamConfiger;
import com.broada.carrier.monitor.impl.host.cli.file.CLIFileMonitor;
import com.broada.carrier.monitor.impl.host.cli.file.CLIFileParamConfiger;
import com.broada.carrier.monitor.impl.host.cli.info.CLIHostInfoConfiger;
import com.broada.carrier.monitor.impl.host.cli.info.CLIHostInfoMonitor;
import com.broada.carrier.monitor.impl.host.cli.memory.CLIMemoryMonitor;
import com.broada.carrier.monitor.impl.host.cli.memory.CLIMemoryParamConfiger;
import com.broada.carrier.monitor.impl.host.cli.netstat.CLINetStatMonitor;
import com.broada.carrier.monitor.impl.host.cli.netstat.CLINetStatParamConfiger;
import com.broada.carrier.monitor.impl.host.cli.process.CLIProcessMonitor;
import com.broada.carrier.monitor.impl.host.cli.process.CLIProcessParamConfiger;
import com.broada.carrier.monitor.impl.host.cli.processstate.CLIProStateMonitor;
import com.broada.carrier.monitor.impl.host.cli.processstate.CLIProStateParamConfiger;
import com.broada.carrier.monitor.impl.host.cli.usermanager.CLIUserInfoMonitor;
import com.broada.carrier.monitor.impl.host.cli.usermanager.CLIUserInfoParamConfiger;
import com.broada.carrier.monitor.impl.host.cli.usermanager.useraccount.CLIUserAccountMonitor;
import com.broada.carrier.monitor.impl.host.cli.usermanager.useraccount.CLIUserAccountParamConfig;
import com.broada.carrier.monitor.impl.host.cli.usermanager.win.accountUser.AccountUserMonitor;
import com.broada.carrier.monitor.impl.host.cli.usermanager.win.accountUser.AccountUserParamConfig;
import com.broada.carrier.monitor.impl.host.cli.usermanager.win.logonUser.LogonUserMonitor;
import com.broada.carrier.monitor.impl.host.cli.usermanager.win.logonUser.LogonUserParamConfig;
import com.broada.carrier.monitor.impl.host.cli.winservice.CLIWinServiceMonitor;
import com.broada.carrier.monitor.method.cli.CLIMethodConfiger;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.cli.entity.WmiMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class HostCLIMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "OS" };
		String[] targetTypeIdsWIN = new String[] { "Windows" };
		String[] targetTypeIdsLinux = new String[] { "Linux", "UNIX" };
		String[] methodTypeIds = new String[] { CLIMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("CLI", "CLI-HOSTINFO", "主机基本信息采集(CLI)",
						"通过远程命令行或脚本等方式监测主机的状态和基本信息，可用方式包括Telnet/SSH/WMI/Agent等", CLIHostInfoConfiger.class.getName(),
						CLIHostInfoMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("CLI", "CLI-HOSTCPU", "主机CPU使用率监测(CLI)",
						"通过远程命令行或脚本等方式进行主机CPU使用率监测，可用方式包括Telnet/SSH/WMI/Agent等", CLICPUParamConfiger.class.getName(),
						CLICPUMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("CLI", "CLI-WIN-DIRECTORY", "系统目录文件监测(CLI)",
						"通过远程命令行或脚本等方式监测主机目录下的所有文件的大小、增长率等信息，可用方式包括WMI。",
						com.broada.carrier.monitor.impl.host.cli.directory.win.CLIDirectoryParamConfiger.class
								.getName(),
						com.broada.carrier.monitor.impl.host.cli.directory.win.CLIDirectoryMonitor.class.getName(),
						index++, targetTypeIdsWIN, methodTypeIds),

				new MonitorType("CLI", "CLI-UNIX-DIRECTORY", "系统目录文件监测(CLI)",
						"通过远程命令行或脚本等方式进行主机系统目录文件监测，可用方式包括Telnet/SSH/WMI/Agent等",
						com.broada.carrier.monitor.impl.host.cli.directory.unix.CLIDirectoryParamConfiger.class
								.getName(),
						com.broada.carrier.monitor.impl.host.cli.directory.unix.CLIDirectoryMonitor.class.getName(),
						index++, targetTypeIdsLinux, methodTypeIds),

				new MonitorType("CLI", "CLI-DISKSPACE", "主机磁盘监测(CLI)",
						"通过远程命令行或脚本等方式进行主机磁盘可用性和分区使用率监测，可用方式包括Telnet/SSH/WMI/Agent等.",
						CLIDiskParamConfiger.class.getName(), CLIDiskMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("CLI", "CLI-HOSTFILE", "主机文件监测(CLI)",
						"通过远程命令行或脚本等方式监测主机文件是否存在,以及文件大小，可用方式包括Telnet/SSH/WMI/Agent等。",
						CLIFileParamConfiger.class.getName(), CLIFileMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("CLI", "CLI-DEVICEIO", "主机设备IO监测(CLI)",
						"通过远程命令行或脚本等方式进行主机磁盘IO使用率监测，可用方式包括Telnet/SSH/WMI/Agent等.",
						com.broada.carrier.monitor.impl.host.cli.io.CLIIOParamConfiger.class.getName(),
						com.broada.carrier.monitor.impl.host.cli.io.CLIIOMonitor.class.getName(), index++,
						new String[] { "UNIX" }, methodTypeIds),

				new MonitorType("CLI", "CLI-LINUXDEVICEIO", "主机设备IO监测(CLI)",
						"通过远程命令行或脚本等方式进行主机磁盘IO使用率监测，可用方式包括Telnet/SSH/WMI/Agent等.",
						com.broada.carrier.monitor.impl.host.cli.linuxio.CLIIOParamConfiger.class.getName(),
						com.broada.carrier.monitor.impl.host.cli.linuxio.CLIIOMonitor.class.getName(), index++,
						new String[] { "LINUX" }, methodTypeIds),

				new MonitorType("CLI", "CLI-WINDOWSDEVICEIO", "主机设备IO监测(CLI)",
						"通过远程命令行或脚本等方式进行主机磁盘IO使用率监测，可用方式包括Telnet/SSH/WMI/Agent等.",
						com.broada.carrier.monitor.impl.host.cli.windowsio.CLIIOParamConfiger.class.getName(),
						com.broada.carrier.monitor.impl.host.cli.windowsio.CLIIOMonitor.class.getName(), index++,
						targetTypeIdsWIN, methodTypeIds),

				new MonitorType("CLI", "CLI-HOSTMEMORY", "主机内存使用率监测(CLI)",
						"通过远程命令行或脚本等方式进行主机内存使用率监测，可用方式包括Telnet/SSH/WMI/Agent等.",
						CLIMemoryParamConfiger.class.getName(), CLIMemoryMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("CLI", "CLI-NETSTAT", "主机网络接口监测(CLI)",
						"通过远程命令行或脚本等方式获取网络接口状态、收发数据包，可用方式包括Telnet/SSH/WMI/Agent等。",
						CLINetStatParamConfiger.class.getName(), CLINetStatMonitor.class.getName(), index++,
						new String[] { "UNIX" }, methodTypeIds),

				new MonitorType("CLI", "CLI-PROCESS", "主机进程监测(CLI)",
						"通过远程命令行或脚本等方式进行主机进程的运行情况和性能监测，可用方式包括Telnet/SSH/WMI/Agent等.",
						CLIProcessParamConfiger.class.getName(), CLIProcessMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("CLI", "CLI-PROCESSSTATE", "主机进程状态监测(CLI)",
						"通过远程命令行或脚本等方式进行主机进程状态监测，可用方式包括Telnet/SSH/WMI/Agent等.",
						CLIProStateParamConfiger.class.getName(), CLIProStateMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("CLI", "CLI-HOSTUSER", "主机当前登陆用户信息监测(CLI)",
						"通过远程命令行或脚本等方式进行主机当前登陆用户监测，可用方式包括Telnet/SSH/WMI/Agent等.",
						CLIUserInfoParamConfiger.class.getName(), CLIUserInfoMonitor.class.getName(), index++,
						targetTypeIdsLinux, methodTypeIds),
               /*
				new MonitorType("CLI", "CLI-USERACCOUNT", "主机注册用户信息监测(CLI)",
						"通过远程命令行或脚本等方式进行主机注册用户信息监测，可用方式包括Telnet/SSH/WMI/Agent等.",
						CLIUserAccountParamConfig.class.getName(), CLIUserAccountMonitor.class.getName(), index++,
						targetTypeIdsLinux, methodTypeIds),
						*/

				new MonitorType("CLI", "WIN-LOGONUSER", "系统登陆用户信息监测(CLI)", "监测Windows主机登陆用户信息。",
						LogonUserParamConfig.class.getName(), LogonUserMonitor.class.getName(), index++,
						targetTypeIdsWIN, methodTypeIds),
						/*
				new MonitorType("CLI", "WIN-ACCOUNTUSER", "系统注册用户信息监测(CLI)", "监测Windows主机注册用户信息。",
						AccountUserParamConfig.class.getName(), AccountUserMonitor.class.getName(), index++,
						targetTypeIdsWIN, methodTypeIds),
						*/

				new MonitorType("CLI", "WIN-SERVICE", "Windows服务监控(CLI)", "监测Windows服务工作状态是否正常。",
						MultiInstanceConfiger.class.getName(), CLIWinServiceMonitor.class.getName(), index++,
						targetTypeIdsWIN, methodTypeIds), };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] { new MonitorItem( "CLI-HOSTINFO", "CLI-HOSTINFO-1", "CPU数目", "个", "", MonitorItemType.NUMBER),
				new MonitorItem( "CLI-HOSTINFO", "CLI-HOSTINFO-2", "机器型号", "", "", MonitorItemType.TEXT),
				new MonitorItem( "CLI-HOSTINFO", "CLI-HOSTINFO-3", "系统名称", "", "", MonitorItemType.TEXT),
				new MonitorItem( "CLI-HOSTINFO", "CLI-HOSTINFO-4", "系统版本", "", "", MonitorItemType.TEXT),
				new MonitorItem( "CLI-HOSTINFO", "CLI-HOSTINFO-6", "内存大小", "MB", "", MonitorItemType.NUMBER),
				new MonitorItem( "CLI-HOSTINFO", "CLI-HOSTINFO-7", "总进程数目", "个", "", MonitorItemType.NUMBER),
				new MonitorItem( "CLI-HOSTINFO", "CLI-HOSTINFO-8", "端口列表", "", "", MonitorItemType.TEXT),
				new MonitorItem( "CLI-HOSTINFO", "CLI-HOSTINFO-9", "磁盘", "", "", MonitorItemType.TEXT),
				new MonitorItem( "CLI-HOSTINFO", "host-mac-min", "MAC地址", "", "主机最小MAC地址", MonitorItemType.TEXT),
				new MonitorItem( "CLI-HOSTINFO", "host-name", "主机名", "", "主机名", MonitorItemType.TEXT),

				new MonitorItem("CLI-HOSTCPU", "CLI-HOSTCPU-1", "CPU总使用率", "%", "CPU的总使用率", MonitorItemType.NUMBER),
				new MonitorItem("CLI-HOSTCPU", "CLI-HOSTCPU-2", "CPU系统使用率", "%", "CPU的系统使用率", MonitorItemType.NUMBER),
				new MonitorItem("CLI-HOSTCPU", "CLI-HOSTCPU-3", "CPU用户使用率", "%", "CPU的用户使用率", MonitorItemType.NUMBER),

				new MonitorItem("CLI-WIN-DIRECTORY", "CLI-WIN-DIRECTORY-1", "文件大小", "MB", "指定文件的大小", MonitorItemType.NUMBER),
				new MonitorItem("CLI-WIN-DIRECTORY", "CLI-WIN-DIRECTORY-2", "文件增速", "KB/s", "指定文件的大小增速", MonitorItemType.NUMBER),
				new MonitorItem("CLI-WIN-DIRECTORY", "file-modified", "文件修改时间", "", "指定文件的最后修改时间", MonitorItemType.TEXT),

				new MonitorItem("CLI-UNIX-DIRECTORY", "CLI-UNIX-DIRECTORY-1", "文件大小", "MB", "指定文件的大小", MonitorItemType.NUMBER),
				new MonitorItem("CLI-UNIX-DIRECTORY", "CLI-UNIX-DIRECTORY-2", "文件增速", "KB/s", "指定文件的大小增速", MonitorItemType.NUMBER),

				new MonitorItem("CLI-DISKSPACE", "CLI-DISKSPACE-1", "分区总空间", "MB", "指定分区的总空间", MonitorItemType.NUMBER),
				new MonitorItem("CLI-DISKSPACE", "CLI-DISKSPACE-2", "未使用空间", "MB", "指定分区的未使用空间", MonitorItemType.NUMBER),
				new MonitorItem("CLI-DISKSPACE", "CLI-DISKSPACE-3", "磁盘使用率", "%", "指定分区的磁盘使用率", MonitorItemType.NUMBER),

				new MonitorItem("CLI-HOSTFILE", "CLI-HOSTFILE-1", "文件大小", "MB", "指定文件的大小", MonitorItemType.NUMBER),
				new MonitorItem("CLI-HOSTFILE", "CLI-HOSTFILE-2", "修改时间", "", "指定文件的最后修改时间", MonitorItemType.TEXT),
				new MonitorItem("CLI-HOSTFILE", "CLI-HOSTFILE-3", "状态", "", "文件是否存在", MonitorItemType.TEXT),

				new MonitorItem("CLI-DEVICEIO", "CLI-DEVICEIO-1", "传送请求占比", "%", "设备忙时，传送请求所占时间的百分比", MonitorItemType.NUMBER),
				new MonitorItem("CLI-DEVICEIO", "CLI-DEVICEIO-2", "未完成请求均值", "个", "队列满时，未完成请求数量的平均值", MonitorItemType.NUMBER),
				new MonitorItem("CLI-DEVICEIO", "CLI-DEVICEIO-3", "每秒读写", "次", "每秒从设备读取/写入数据量", MonitorItemType.NUMBER),
				new MonitorItem("CLI-DEVICEIO", "CLI-DEVICEIO-4", "每秒传送", "块", "每秒传送的块数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-DEVICEIO", "CLI-DEVICEIO-5", "请求等待均值", "ms", "队列满时传送请求等待的平均时间", MonitorItemType.NUMBER),
				new MonitorItem("CLI-DEVICEIO", "CLI-DEVICEIO-6", "请求完成耗时均值", "ms", "完成传送请求所需平均时间", MonitorItemType.NUMBER),

				new MonitorItem("CLI-LINUXDEVICEIO", "CLI-LINUXDEVICEIO-1", "忙时占比", "%", "I/O操作占用百分率", MonitorItemType.NUMBER),
				new MonitorItem("CLI-LINUXDEVICEIO", "CLI-LINUXDEVICEIO-2", "每秒Merge读", "次", "每秒进行merge读操作数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-LINUXDEVICEIO", "CLI-LINUXDEVICEIO-3", "每秒Merge写", "次", "每秒进行merge写操作数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-LINUXDEVICEIO", "CLI-LINUXDEVICEIO-4", "每秒IO读", "次", "每秒完成读I/O次数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-LINUXDEVICEIO", "CLI-LINUXDEVICEIO-5", "每秒IO写", "次", "每秒完成读I/O写数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-LINUXDEVICEIO", "CLI-LINUXDEVICEIO-6", "每秒读扇区", "个", "每秒读扇区数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-LINUXDEVICEIO", "CLI-LINUXDEVICEIO-7", "每秒写扇区", "个", "每秒写扇区数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-LINUXDEVICEIO", "CLI-LINUXDEVICEIO-8", "读速率", "KB/s", "每秒读K字节数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-LINUXDEVICEIO", "CLI-LINUXDEVICEIO-9", "写速率", "KB/s", "每秒写K字节数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-LINUXDEVICEIO", "CLI-LINUXDEVICEIO-10", "数据大小均值", "", "平均每次设备I/O操作的数据大小", MonitorItemType.NUMBER),
				new MonitorItem("CLI-LINUXDEVICEIO", "CLI-LINUXDEVICEIO-11", "队列长度均值", "个", "平均I/O队列长度", MonitorItemType.NUMBER),
				new MonitorItem("CLI-LINUXDEVICEIO", "CLI-LINUXDEVICEIO-12", "IO等待均值", "ms", "平均每次设备I/O操作的等待时间", MonitorItemType.NUMBER),
				new MonitorItem("CLI-LINUXDEVICEIO", "CLI-LINUXDEVICEIO-13", "IO服务均值", "ms", "平均每次设备I/O操作的服务时间", MonitorItemType.NUMBER),

				new MonitorItem("CLI-WINDOWSDEVICEIO", "CLI-WINDOWSDEVICEIO-1", "队列长度", "", "磁盘当前队列长度", MonitorItemType.NUMBER),
				new MonitorItem("CLI-WINDOWSDEVICEIO", "CLI-WINDOWSDEVICEIO-2", "每秒IO读", "次", "每秒完成读I/O操作次数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-WINDOWSDEVICEIO", "CLI-WINDOWSDEVICEIO-3", "每秒IO写", "次", "每秒完成写I/O操作次数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-WINDOWSDEVICEIO", "CLI-WINDOWSDEVICEIO-4", "读速率", "KB/s", "每秒读K字节数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-WINDOWSDEVICEIO", "CLI-WINDOWSDEVICEIO-5", "写速率", "KB/s", "每秒写K字节数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-WINDOWSDEVICEIO", "CLI-WINDOWSDEVICEIO-6", "读操作占比", "%", "磁盘忙于读请求所占的时间百分比", MonitorItemType.NUMBER),
				new MonitorItem("CLI-WINDOWSDEVICEIO", "CLI-WINDOWSDEVICEIO-7", "写操作占比", "%", "磁盘忙于写请求所占的时间百分比", MonitorItemType.NUMBER),
				new MonitorItem("CLI-WINDOWSDEVICEIO", "CLI-WINDOWSDEVICEIO-8", "忙时占比", "%", "磁盘忙于读/写活动所用时间的百分比", MonitorItemType.NUMBER),
				new MonitorItem("CLI-WINDOWSDEVICEIO", "CLI-WINDOWSDEVICEIO-9", "闲时占比", "%", "在指定的间隔时间内磁盘空闲所占用的百分比", MonitorItemType.NUMBER),

				new MonitorItem("CLI-HOSTMEMORY", "CLI-HOSTMEMORY-1", "物理内存使用率", "%", "主机的物理内存使用量与总物理内存的百分比", MonitorItemType.NUMBER),
				new MonitorItem("CLI-HOSTMEMORY", "CLI-HOSTMEMORY-2", "虚拟内存使用率", "%", "主机的虚拟内存使用量与总虚拟内存的百分比", MonitorItemType.NUMBER),
				new MonitorItem("CLI-HOSTMEMORY", "CLI-HOSTMEMORY-3", "物理内存使用量", "MB", "主机的物理内存使用大小", MonitorItemType.NUMBER),
				new MonitorItem("CLI-HOSTMEMORY", "CLI-HOSTMEMORY-4", "虚拟内存使用量", "MB", "主机的虚拟内存使用大小", MonitorItemType.NUMBER),

				new MonitorItem("CLI-NETSTAT", "CLI-NETSTAT-1", "名称", "", "名称", MonitorItemType.TEXT),
				new MonitorItem("CLI-NETSTAT", "CLI-NETSTAT-2", "网络", "", "网络", MonitorItemType.TEXT),
				new MonitorItem("CLI-NETSTAT", "CLI-NETSTAT-3", "地址", "", "地址", MonitorItemType.TEXT),
				new MonitorItem("CLI-NETSTAT", "CLI-NETSTAT-4", "最大传输单元", "", "最大传输单元", MonitorItemType.NUMBER),
				new MonitorItem("CLI-NETSTAT", "CLI-NETSTAT-5", "接收包数", "", "接收包数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-NETSTAT", "CLI-NETSTAT-6", "接收包错误数", "", "接收包错误数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-NETSTAT", "CLI-NETSTAT-7", "发送包", "", "发送包", MonitorItemType.NUMBER),
				new MonitorItem("CLI-NETSTAT", "CLI-NETSTAT-8", "发送包错误数", "", "发送包错误数", MonitorItemType.NUMBER),
				new MonitorItem("CLI-NETSTAT", "CLI-NETSTAT-9", "冲突数", "", "冲突数", MonitorItemType.NUMBER),

				new MonitorItem("CLI-PROCESS", "CLI-PROCESS-1", "内存使用量", "MB", "进程所使用内存的大小", MonitorItemType.NUMBER),
				new MonitorItem("CLI-PROCESS", "CLI-PROCESS-2", "CPU使用率", "%", "进程的CPU使用率", MonitorItemType.NUMBER),
				new MonitorItem("CLI-PROCESS", "CLI-PROCESS-3", "内存使用率", "%", "进程所使用内存与总物理内存的百分比", MonitorItemType.NUMBER),
				new MonitorItem("CLI-PROCESS", "CLI-PROCESS-4", "运行状态", "", "当前是否正在运行", MonitorItemType.TEXT),

				new MonitorItem("CLI-PROCESSSTATE", "CLI-PROCESSSTATE-1", "虚拟内存使用量", "KB", "进程虚拟内存使用量", MonitorItemType.NUMBER),
				new MonitorItem("CLI-PROCESSSTATE", "CLI-PROCESSSTATE-2", "启动时间", "", "进程启动时间", MonitorItemType.TEXT),
				new MonitorItem("CLI-PROCESSSTATE", "CLI-PROCESSSTATE-3", "运行时间", "", "进程持续时间", MonitorItemType.TEXT),

				new MonitorItem("CLI-HOSTUSER", "CLI-HOSTUSER-1", "用户", "", "当前登陆用户", MonitorItemType.TEXT),
				new MonitorItem("CLI-HOSTUSER", "CLI-HOSTUSER-2", "登陆IP", "", "当前登陆用户终端的IP地址", MonitorItemType.TEXT),
				new MonitorItem("CLI-HOSTUSER", "CLI-HOSTUSER-3", "登陆时间", "", "当前登陆用户登陆的时间", MonitorItemType.TEXT),

				new MonitorItem("CLI-USERACCOUNT","CLI-USERACCOUNT-1", "用户密码(加密)", "", "用户密码(加密)", MonitorItemType.TEXT),
				new MonitorItem("CLI-USERACCOUNT","CLI-USERACCOUNT-2", "用户ID", "", "用户ID", MonitorItemType.TEXT),
				new MonitorItem("CLI-USERACCOUNT","CLI-USERACCOUNT-3", "用户组ID", "", "用户组ID", MonitorItemType.TEXT),
				new MonitorItem("CLI-USERACCOUNT","CLI-USERACCOUNT-4", "说明", "", "说明", MonitorItemType.TEXT),
				new MonitorItem("CLI-USERACCOUNT","CLI-USERACCOUNT-5", "主目录", "", "主目录", MonitorItemType.TEXT),

				new MonitorItem("WIN-LOGONUSER", "WIN-LOGONUSER-1", "计算机", "", "登陆用户的计算机", MonitorItemType.TEXT),
				new MonitorItem("WIN-LOGONUSER", "WIN-LOGONUSER-2", "用户", "", "登陆用户的名称", MonitorItemType.TEXT),
				new MonitorItem("WIN-LOGONUSER", "WIN-LOGONUSER-3", "会话时间", "", "会话启动的时间", MonitorItemType.TEXT),

				new MonitorItem("WIN-ACCOUNTUSER","WIN-ACCOUNTUSER-1", "名称", "", "帐户的名称", MonitorItemType.TEXT),
				new MonitorItem("WIN-ACCOUNTUSER","WIN-ACCOUNTUSER-2", "全名称", "", "帐户的全名称", MonitorItemType.TEXT),
				new MonitorItem("WIN-ACCOUNTUSER","WIN-ACCOUNTUSER-3", "域", "", "帐户所在的域", MonitorItemType.TEXT),
				new MonitorItem("WIN-ACCOUNTUSER","WIN-ACCOUNTUSER-4", "描述", "", "帐户的描述", MonitorItemType.TEXT), };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(CLIMonitorMethodOption.TYPE_ID, "CLI监测协议",
				CLIMethodConfiger.class) };

	}
}
