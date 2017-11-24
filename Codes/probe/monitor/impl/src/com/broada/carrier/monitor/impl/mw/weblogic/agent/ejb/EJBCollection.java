package com.broada.carrier.monitor.impl.mw.weblogic.agent.ejb;

import java.util.ArrayList;
import java.util.List;

public class EJBCollection {
	private List ejbCollections = new ArrayList();
  /***************************异常*************************/
  private String message;
  private String detail;
	public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void addEJBRuntimeInformation(Object ejbRuntimeInformation){
		ejbCollections.add(ejbRuntimeInformation);
	}

	public List getEjbCollections() {
		return ejbCollections;
	}
}
