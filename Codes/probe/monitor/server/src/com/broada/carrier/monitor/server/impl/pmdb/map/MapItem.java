package com.broada.carrier.monitor.server.impl.pmdb.map;

import org.dom4j.Element;

public class MapItem {
	private MapItemLocal local;
	private MapItemRemote remote;
	private String function;

	public MapItem(Element root) {
		setLocal(XMLUtil.checkAttribute(root, "local"));
		setRemote(XMLUtil.checkAttribute(root, "remote"));
		function = root.attributeValue("function");
	}

	private void setLocal(String local) {
		this.local = new MapItemLocal(local);
	}

	private void setRemote(String remote) {
		this.remote = new MapItemRemote(remote);
	}

	public MapItemRemoteType getRemoteType() {
		return remote.getType();
	}

	public String getRemoteCode() {
		return remote.getCode();
	}

	public MapObjectType getLocalType() {
		return local.getType();
	}

	public String getLocalCode() {
		return local.getCode();
	}

	public String getFunction() {
		return function;
	}

	@Override
	public String toString() {
		return String.format("%s[%s %s]", getClass().getSimpleName(), local, remote);
	}
}
