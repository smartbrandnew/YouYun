package com.broada.carrier.monitor.impl.stdsvc.https;

import com.broada.carrier.monitor.impl.stdsvc.http.HTTPParameter;

/**
 * HTTP 监测参数实体类
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class HTTPSParameter extends HTTPParameter {
	private static final long serialVersionUID = 1L;

	public HTTPSParameter() {
  }

  public HTTPSParameter(String str) {
    super(str);
  }

	@Override
	public int getPort() {		
		return get("port", 443);
	}
}
