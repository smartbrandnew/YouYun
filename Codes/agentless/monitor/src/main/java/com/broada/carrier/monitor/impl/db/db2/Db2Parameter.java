package com.broada.carrier.monitor.impl.db.db2;

import java.io.Serializable;

import com.broada.carrier.monitor.common.util.AnyObject;
import com.broada.carrier.monitor.common.util.SerializeUtil;
import com.broada.carrier.monitor.common.util.TextUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * IBM Db2 数据库监测参数实体类
 * 
 * @author lixy (lixy@broada.com.cn) Create By 2007-3-22 下午05:09:29
 */
public class Db2Parameter implements Serializable {
	private static final long serialVersionUID = 1L;
	private String conAddrs;

	public String getConAddrs() {
		return conAddrs;
	}

	public void setConAddrs(String conAddrs) {
		this.conAddrs = conAddrs;
	}
	
	@JsonIgnore
	public boolean isChkConAddrs() {
		return !TextUtil.isEmpty(conAddrs);
	}

	public static Db2Parameter decode(String data) {
		Db2Parameter result = AnyObject.decode(data, Db2Parameter.class);
		if (result == null)
			result = new Db2Parameter();
		return result;
	}

	@JsonIgnore
	public String getParameters() {
		return SerializeUtil.encodeJson(this);
	}
}
