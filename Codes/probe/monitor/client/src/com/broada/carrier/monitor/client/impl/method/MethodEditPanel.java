package com.broada.carrier.monitor.client.impl.method;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.common.swing.BeanEditPanel;
import com.broada.carrier.monitor.common.swing.BeanEditWindow;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.spi.MonitorMethodConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.swing.util.WinUtil;

public class MethodEditPanel extends BeanEditPanel<MonitorMethodConfigContext> {
	private static final long serialVersionUID = 1L;
	private JTextField txtTypeName;
	private JTextField txtCode;
	private JTextField txtDescr;
	private JTextArea txtTypeDescr = new JTextArea();
	private MonitorMethodConfigContext context;
	private MonitorMethodType methodType;
	private MonitorMethodConfiger configer;
	private JTextField txtName;
	private boolean create;
	
	public MethodEditPanel(boolean create) {
		this();
		this.create = create;
	}
	
	public MethodEditPanel() {
		setLayout(new BorderLayout(0, 0));		
		setPreferredSize(new Dimension(550, 500));
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 110));
		panel.setBorder(new TitledBorder(null, "基本信息", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel, BorderLayout.NORTH);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("类型：");
		lblNewLabel.setBounds(10, 23, 69, 15);
		panel.add(lblNewLabel);
		
		txtTypeName = new JTextField();
		txtTypeName.setEditable(false);
		txtTypeName.setBounds(48, 20, 162, 21);
		panel.add(txtTypeName);
		txtTypeName.setColumns(10);
		
		JLabel label = new JLabel("编码：");
		label.setBounds(10, 51, 69, 15);
		panel.add(label);
		
		txtCode = new JTextField();
		txtCode.setColumns(10);
		txtCode.setBounds(48, 48, 195, 21);
		panel.add(txtCode);
		
		JLabel label_1 = new JLabel("说明：");
		label_1.setBounds(10, 79, 69, 15);
		panel.add(label_1);
		
		txtDescr = new JTextField();
		txtDescr.setColumns(10);
		txtDescr.setBounds(48, 76, 487, 21);
		panel.add(txtDescr);
		
		txtName = new JTextField();
		txtName.setColumns(10);
		txtName.setBounds(309, 48, 226, 21);
		panel.add(txtName);
		
		JLabel label_2 = new JLabel("名称：");
		label_2.setBounds(265, 51, 47, 15);
		panel.add(label_2);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "说明", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
				
		txtTypeDescr.setPreferredSize(new Dimension(4, 40));
		txtTypeDescr.setBackground(UIManager.getColor("Panel.background"));
		txtTypeDescr.setEditable(false);
		panel_1.add(txtTypeDescr);
	}

	@Override
	public String getTitle() {
		return "监测方法配置";
	}

	@Override
	public MonitorMethodConfigContext getData() {
		if (!getConfigerData())
			return null;
		
		context.getMethod().setCode(txtCode.getText().trim());
		context.getMethod().setName(txtName.getText().trim());
		context.getMethod().setDescr(txtDescr.getText().trim());				
		context.getMethod().setTypeId(methodType.getId());
		context.getMethod().verify();
		if (create)
			context.getServerFactory().getMethodService().createMethod(context.getMethod());
		else
			context.getServerFactory().getMethodService().saveMethod(context.getMethod());
		return context;
	}

	private boolean getConfigerData() {
		return configer.getData();
	}

	@Override
	public void setData(MonitorMethodConfigContext context) {		
		this.context = context;
		this.methodType = ServerUtil.checkMethodType(ServerContext.getServerFactory().getTypeService(), context.getMethod().getTypeId());
		txtTypeName.setText(methodType.getName());
		txtTypeDescr.setText(methodType.getDescr());
		txtCode.setEditable(context.getMethod().getCode() == null || context.getMethod().getCode().isEmpty());
		txtCode.setText(context.getMethod().getCode());
		txtName.setText(context.getMethod().getName());
		txtDescr.setText(context.getMethod().getDescr());
		setConfigerData();
	}

	private void setConfigerData() {
		try {
			configer = (MonitorMethodConfiger) Class.forName(methodType.getConfiger()).newInstance();
			configer.setData(this.context);
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException("建立监测方法配置界面失败", e);
		}
		add(configer.getComponent(), BorderLayout.CENTER);
		WinUtil.getWindowForComponent(this).pack();
		WinUtil.toCenter(WinUtil.getWindowForComponent(this));
	}
	
	public static boolean show(Window owner, MonitorMethodConfigContext context) {
		return BeanEditWindow.show(owner, new MethodEditPanel(), context) != null;
	}

	public static boolean showCreate(Window owner, MonitorMethodConfigContext context) {
		return BeanEditWindow.show(owner, new MethodEditPanel(true), context) != null;
	}
}
