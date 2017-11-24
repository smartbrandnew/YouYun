package com.broada.carrier.monitor.probe.impl.openapi.entity;

import java.util.ArrayList;
import java.util.List;

public class DeviceDetail {
	
	private List<Network> network = new ArrayList<Network>();
	private List<FileSystem> filesystem = new ArrayList<FileSystem>();
	private Platform platform;
	private Memory memory;
	private CPU cpu;
	private Gohai gohai;
	
	public DeviceDetail() {
		// TODO Auto-generated constructor stub
	}
	public DeviceDetail(List<Network> network, List<FileSystem> filesystem, Platform platform,
			Memory memory, CPU cpu, Gohai gohai) {
		this.network = network;
		this.filesystem = filesystem;
		this.platform = platform;
		this.memory = memory;
		this.cpu = cpu;
		this.gohai = gohai;
	}
	
	public List<Network> getNetwork() {
		return network;
	}
	public void setNetwork(List<Network> network) {
		this.network = network;
	}
	public List<FileSystem> getFilesystem() {
		return filesystem;
	}
	public void setFilesystem(List<FileSystem> filesystem) {
		this.filesystem = filesystem;
	}
	public Platform getPlatform() {
		return platform;
	}
	public void setPlatform(Platform platform) {
		this.platform = platform;
	}
	public Memory getMemory() {
		return memory;
	}
	public void setMemory(Memory memory) {
		this.memory = memory;
	}
	public CPU getCpu() {
		return cpu;
	}
	public void setCpu(CPU cpu) {
		this.cpu = cpu;
	}
	public Gohai getGohai() {
		return gohai;
	}
	public void setGohai(Gohai gohai) {
		this.gohai = gohai;
	}
	
	public void addFileSystem(FileSystem fileSystem){
		this.filesystem.add(fileSystem);
	}
	
}
