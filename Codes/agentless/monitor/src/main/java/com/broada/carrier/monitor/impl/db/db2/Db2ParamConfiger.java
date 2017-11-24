package com.broada.carrier.monitor.impl.db.db2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.broada.carrier.monitor.impl.common.ui.EditAddrDialog;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.spi.MonitorConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;

/**
 * IBM DB2 服务监测类型的监测参数配置界面
 * 
 * @author lixy (lixy@broada.com.cn) Create By 2007-3-22 下午05:10:46
 */
public class Db2ParamConfiger implements MonitorConfiger {
	public static final String[] keys = new String[] { "db_status", "db_conn_time", "appls_cur_cons", "total_cons",
			"last_backup_time" };

	public static final boolean[] isStringType = new boolean[] { true, true, false, false, true };

	public static final String[] colNames = new String[] { "数据库状态", "连接时间", "当前连接数", "连接总数", "最后备份时间" };

	public class BaseParamPanel extends JPanel {
		/**
		 * <code>serialVersionUID</code>
		 */
		private static final long serialVersionUID = 2261091962843700789L;

		private BorderLayout bodyLayout = new BorderLayout();

		// 基本配置面板
		private BorderLayout borderPanBase = new BorderLayout();

		private TitledBorder titledBorderBase;

		private JCheckBox jChkConAddrs = new JCheckBox();

		private JTextField jTxtConAddrs = new JTextField();

		private Map descMap = new HashMap();

		/* 对应编辑的参数 */
		private Db2Parameter param = new Db2Parameter();

		BorderLayout borderLayout1 = new BorderLayout();

		JPanel jPanBase = new JPanel();

		JPanel jPanWonted = new JPanel();

		/**
		 * 默认构造函数
		 */
		public BaseParamPanel() {
			try {
				jbInit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Panel界面初始化
		 * 
		 * @throws Exception
		 */
		private void jbInit() throws Exception {
			descMap.put("LA", "合法连接地址:您可以编辑一个允许访问数据的客户端序列,可以输入机器名(推荐)或是机器的IP地址.若是在在这以外的机器访问访问了数据库,就会产生一个告警,提示你有非法用户登陆了.");
			descMap.put("CC", "连接数目:当前连接到数据库的连接数,这个连接数区别于用户数目.一般比较稳定,若是该值会随慢慢地增加,则说明您的某个应用存在问题.");

			/*
			 * 基本配置面板初始化
			 */
			// 初始化告警面板
			titledBorderBase = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),
					"基本配置");
			jPanWonted.setBorder(titledBorderBase);
			jPanWonted.setLayout(null);

			// 初始化基本配置面板
			jPanBase.setLayout(borderPanBase);
			// 向基本配置面板添加告警面板
			jPanBase.add(jPanWonted, BorderLayout.CENTER);

			/*
			 * 性能监测面板初始化
			 */
			jPanWonted.setLayout(null);

			jChkConAddrs.setText("合法连接地址(标准IP地址)");
			jChkConAddrs.setBounds(new Rectangle(22, 20, 200, 24));
			jChkConAddrs.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					jChkConAddr_itemStateChanged(e);
				}
			});

			jTxtConAddrs.setBackground(Color.white);
			jTxtConAddrs.setEnabled(false);
			jTxtConAddrs.setDisabledTextColor(Color.black);
			jTxtConAddrs.setEditable(false);
			jTxtConAddrs.setText("点击编辑合法地址列表");
			jTxtConAddrs.setBounds(new Rectangle(133, 45, 245, 22));
			jTxtConAddrs.addMouseListener(new MouseAdapter() {
				// 点击弹出编辑菜单
				public void mouseClicked(MouseEvent e) {
					if (jChkConAddrs.isSelected()) {
						EditAddrDialog ead = new EditAddrDialog(jTxtConAddrs);
						ead.show(jTxtConAddrs, 0, 22);
					}
				}
			});

			jPanWonted.add(jTxtConAddrs, null);
			jPanWonted.add(jChkConAddrs, null);

			this.setLayout(bodyLayout);
			this.add(jPanBase, BorderLayout.CENTER);
		}

		public void setParameters(String xml) {
			param = Db2Parameter.decode(xml);			
			jChkConAddrs.setSelected(param.isChkConAddrs());
			if (jChkConAddrs.isSelected()) {
				jTxtConAddrs.setText(param.getConAddrs());
				jTxtConAddrs.setEnabled(true);
			}
		}

		public String getParameters() {
			if (jChkConAddrs.isSelected()) {
				param.setConAddrs(jTxtConAddrs.getText());
			} else {
				param.setConAddrs("");
			}
			return param.getParameters();
		}

		public boolean verify() {
			if (jChkConAddrs.isSelected()
					&& (jTxtConAddrs.getText().length() <= 0 || jTxtConAddrs.getText().indexOf("点击") != -1)) {
				JOptionPane.showMessageDialog(this, "您没有输入任何地址.");
				jTxtConAddrs.requestFocus();
				return false;
			}

			return true;
		}

		public void jChkConAddr_itemStateChanged(ItemEvent e) {
			jTxtConAddrs.setEnabled(jChkConAddrs.isSelected());
		}

		public List getConditions() {
			return new ArrayList();
		}

		public void setConditions(List conditions) {
		}
	}
	
	private BaseParamPanel panel = new BaseParamPanel();
	private MonitorConfigContext data;

	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public boolean getData() {
		String result = panel.getParameters();
		if (result == null)
			return false;
		data.getTask().setParameter(result);
		return true;
	}

	@Override
	public void setData(MonitorConfigContext data) {
		this.data = data;
		this.panel.setParameters(data.getTask().getParameter());
	}

	@Override
	public void setMethod(MonitorMethod method) {
	}
}
