package com.broada.carrier.monitor.impl.host.snmp.util;

/**
 * 封装内存对象信息
 */
public class Memory {
	private String name;
	private long size;
	private long used;
	
	/**
	 * 构建一个内存对象
	 * @param name 内存名称
	 * @param size 大小，单位字节
	 * @param used 已使用大小，单位字节
	 */
	public Memory(String name, long size, long used) {
		super();
		this.name = name;
		this.size = size;
		this.used = used;
	}
	public String getName() {
		return name;
	}
	public long getSize() {
		return size;
	}
	public long getUsed() {
		return used;
	}
}
