package com.broada.carrier.monitor.impl.storage;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.generic.ExtParameter;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;

public class StorageConfiger extends MultiInstanceConfiger {
	private static final long serialVersionUID = 1L;
	private ExtParameter param = new ExtParameter();
	private Map<String, MonitorItem> items = new LinkedHashMap<String, MonitorItem>();

	public StorageConfiger() {
	}

	@Override
	protected Object collect() {  
		if (getTask().getMethodCode() == null) {
			JOptionPane.showMessageDialog(this, "请选择监测方法");
			return null;
		}  
		items.clear();		
		MonitorResult result = (MonitorResult) super.collect(param);
		if (result == null)
			return null;
		if (result.getItems() != null) {
			for (MonitorItem item : result.getItems())
				items.put(item.getCode(), item);
		}
		return result;
	}
	
	@Override
	protected String[] getItemCodes() {
		if (items == null)
			return super.getItemCodes();
		else {
			Set<String> codes = new LinkedHashSet<String>();
			for (String code : items.keySet()) {
				if ((code.startsWith("attr.") || code.startsWith("perf.")||code.startsWith("state.")) && !code.equals("attr.storResCode")) {
					codes.add(code);
				}
			}
			return codes.toArray(new String[0]);
		}
	}

	@Override
	protected MonitorItem checkItem(String itemCode) {
		MonitorItem item = items.get(itemCode);
		if (item != null)
			return item;
		return super.checkItem(itemCode);
	}

	@Override
	public boolean getData() {
		getContext().getTask().setParameterObject(param);
		return true;
	}

	@Override
	public void setData(MonitorConfigContext data) {		
		super.setData(data);
		param = data.getTask().getParameterObject(ExtParameter.class);
		if (param == null)
			param = new ExtParameter();
	}	
}
