package com.broada.carrier.monitor.impl.mw.iis.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;

public class IISTempData implements Serializable {
	private static final long serialVersionUID = 1L;
	private ArrayList<Properties> datas = new ArrayList<Properties>();

	public ArrayList<Properties> getDatas() {
		return datas;
	}

	public void setDatas(ArrayList<Properties> datas) {
		this.datas = datas;
	}

}
