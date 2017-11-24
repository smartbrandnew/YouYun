package com.broada.carrier.monitor.impl.host.ipmi.sdk.core.common;

import java.io.Serializable;

/**
 * 网卡信息
 * 
 * @author pippo Create By 2014-5-13 下午7:06:03
 */
public class ChannelInfo implements Serializable {
	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -5087084240165507979L;
	private String title;
	private String medium;
	private String protocol;
	private String support;
	private String count;
	private String vendor;

	private boolean isEmpty(String str) {
		if (str == null || "".equals(str.trim())) {
			return true;
		}
		return false;
	}
	
	public boolean isEmpty() {
		if (isEmpty(medium) && isEmpty(protocol) && isEmpty(support) && isEmpty(count) && isEmpty(vendor)) {
			return true;
		}
		return false;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMedium() {
		return medium;
	}

	public void setMedium(String medium) {
		this.medium = medium;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getSupport() {
		return support;
	}

	public void setSupport(String support) {
		this.support = support;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
}
