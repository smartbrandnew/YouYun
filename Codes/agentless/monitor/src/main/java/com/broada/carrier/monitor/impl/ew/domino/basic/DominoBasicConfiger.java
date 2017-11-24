package com.broada.carrier.monitor.impl.ew.domino.basic;

import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by hu on 2015/6/15.
 */
public class DominoBasicConfiger extends SingleInstanceConfiger {
	private static final long serialVersionUID = 1L;
	private DominoParam param = new DominoParam();

	@Override protected JButton[] getButtons() {
		JButton btnConfig = new JButton("配置");
		btnConfig.addActionListener(new BtnConfigActionListener());
		return new JButton[] { btnConfig };
	}

	@Override public boolean getData() {
		getContext().getTask().setParameterObject(param);
		return true;
	}

	@Override public void setData(MonitorConfigContext data) {
		super.setData(data);
		param = data.getTask().getParameterObject(DominoParam.class);
		if (param == null) {
			param = new DominoParam();
		}
	}

	@Override protected Object collect() {
		if (param.getDbName() == null) {
			param.setDbName(JOptionPane.showInputDialog("请输入数据库名称", param.getDbName()));
			return null;
		}
		return super.collect(param);
	}

	protected ServerServiceFactory getServerFactory() {
		return getContext().getServerFactory();
	}

	protected MonitorTask getTask() {
		return getContext().getTask();
	}

	class BtnConfigActionListener implements ActionListener {
		@Override public void actionPerformed(ActionEvent e) {
			param.setDbName(JOptionPane.showInputDialog("请输入数据库名称", param.getDbName()));
		}
	}

}

