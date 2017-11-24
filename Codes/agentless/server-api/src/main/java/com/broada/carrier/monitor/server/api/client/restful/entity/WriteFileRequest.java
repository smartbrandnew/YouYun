package com.broada.carrier.monitor.server.api.client.restful.entity;

import com.broada.carrier.monitor.common.util.Base64Util;

public class WriteFileRequest {
	private byte[] data;
	private int offset;
	private int length;

	public WriteFileRequest() {
	}

	public WriteFileRequest(byte[] data, int offset, int length) {
		this.data = data;
		this.offset = offset;
		this.length = length;
	}

	public byte[] retData() {
		return data;
	}

	public void putData(byte[] data) {
		this.data = data;
	}
	
	public String getData() {
		if (data == null)
			return null;
		return Base64Util.encode(data);
	}

	public void setData(String data) {
		if (data == null || data.isEmpty())
			this.data = null;
		else
			this.data = Base64Util.decode(data);		
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

}
