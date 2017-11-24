package com.broada.carrier.monitor.impl.generic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;
import com.broada.swing.util.ErrorDlg;
import com.broada.utils.TextUtil;

public class GenericConfiger extends MultiInstanceConfiger {
	private static final long serialVersionUID = 1L;
	private ExtParameter param = new ExtParameter();
	private Map<String, MonitorItem> items = new LinkedHashMap<String, MonitorItem>();

	public GenericConfiger() {
	}

	@Override
	protected Object collect() {
		if (getTask().getMethodCode() == null) {
			JOptionPane.showMessageDialog(this, "请选择监测方法");
			return null;
		}
		if (TextUtil.isEmpty(param.getScriptFilePath())) {
			JOptionPane.showMessageDialog(this, "请配置监测脚本");
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
				if (!GenericMonitor.isHideItem(code))
					codes.add(code);
			}
			return codes.toArray(new String[0]);
		}
	}

	@Override
	protected JButton[] getButtons() {
		JButton btnConfigScript = new JButton("脚本配置");
		btnConfigScript.addActionListener(new BtnConfigScriptActionListener());
		return new JButton[] { btnConfigScript };
	}

	private class BtnConfigScriptActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			ScriptChooserDlg dlg = ScriptChooserDlg.createDialog(GenericConfiger.this, getServerFactory());
			dlg.setExtParameter((ExtParameter) param.clone());
			dlg.setVisible(true);
			if (dlg.getExtParameter() != null) {
				param = (ExtParameter) dlg.getExtParameter().clone();
				String filePath = param.getScriptFilePath();
				try {
					getServerFactory().getProbeService().uploadFile(getContext().getNode().getProbeId(),
							filePath, filePath);
				} catch (Exception ex) {
					ErrorDlg.createErrorDlg(GenericConfiger.this,
							"同步脚本文件[" + filePath + "]到Probe[probeCode=" + getContext().getNode().getProbeId() + "]时出错", ex);
					return;
				}

				refresh();
			}
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
		boolean result = super.getData();
		if (!result)
			return false;
		
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
