package com.broada.carrier.monitor.impl.generic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import com.broada.carrier.monitor.common.remoteio.api.RemoteFile;
import com.broada.carrier.monitor.common.remoteio.api.RemoteFileMgrPanel;
import com.broada.carrier.monitor.common.remoteio.api.RemoteIOClient;
import com.broada.carrier.monitor.impl.generic.script.ScriptTester;
import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.numen.agent.script.entity.DynamicParam;
import com.broada.swing.util.ErrorDlg;
import com.broada.swing.util.WinUtil;

public class ScriptChooserDlg extends JDialog {
	public static final String SCRIPT_DIR = "conf/script/";
	private static final long serialVersionUID = -107794138747170763L;	

	private JPanel panelAll = new JPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel jPanelBody = new JPanel();
	private JPanel jPanelAct = new JPanel();
	private JButton jBCanel = new JButton();
	private JButton jBOk = new JButton();
	private JLabel jLabelHint = new JLabel();
	private JLabel jLabelTimeout = new JLabel();
	private JTextField jTFFilePath = new JTextField();
	private SpinnerNumberModel snm = new SpinnerNumberModel(180, 1, Integer.MAX_VALUE, 1);
	private JSpinner jSpTimeout = new JSpinner(snm);
	private JButton jBFind = new JButton();
	private JButton jBEdit = new JButton();
	private ParamEditorPanel paramEditor = new ParamEditorPanel();
	private ExtParameter extPara = null;
	private RemoteFileMgrPanel remoteFileMgr;
	private ServerServiceFactory serverFactory;
	
	public ScriptChooserDlg() {
		initDlg();
	}

	private ScriptChooserDlg(Frame frame, String title, boolean modal, ServerServiceFactory serverFactory) {
		super(frame, title, modal);
		initDlg();
		this.serverFactory = serverFactory;
	}

	public ScriptChooserDlg(Dialog dialog, String title, boolean modal, ServerServiceFactory serverFactory) {
		super(dialog, title, modal);
		initDlg();
		this.serverFactory = serverFactory;
	}

	/**
	 * @wbp.parser.constructor
	 */
	public ScriptChooserDlg(Frame frame, ServerServiceFactory serverFactory) {
		this(frame, "选择采集脚本", true, serverFactory);
	}

	private void initDlg() {
		try {
			jbInit();
			WinUtil.toCenter(this);
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static ScriptChooserDlg createDialog(Component owner, String title, boolean modal, ServerServiceFactory serverFactory) {
		Window window = WinUtil.getWindowForComponent(owner);
		ScriptChooserDlg dlg = null;
		if (window instanceof Frame) {
			dlg = new ScriptChooserDlg((Frame) window, title, modal, serverFactory);
		} else {
			dlg = new ScriptChooserDlg((Dialog) window, title, modal, serverFactory);
		}
		return dlg;
	}

	public static ScriptChooserDlg createDialog(Component owner, ServerServiceFactory serverFactory) {
		return createDialog(owner, "选择采集脚本", true, serverFactory);
	}
	
	private void jbInit() throws Exception {
		setSize(new Dimension(516, 413));
		panelAll.setPreferredSize(new Dimension(500, 320));
		panelAll.setLayout(borderLayout1);
		jPanelAct.setBorder(BorderFactory.createEtchedBorder());
		jPanelAct.setPreferredSize(new Dimension(10, 40));
		jBCanel.setText("取消");
		jBCanel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jBCanel_actionPerformed(e);
			}
		});
		jBOk.setText("确定");
		jBOk.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jBOk_actionPerformed(e);
			}
		});
		jPanelBody.setLayout(new BorderLayout());
		jLabelHint.setBounds(8, 7, 168, 15);
		jLabelHint.setText("请选择需要采集数据的脚本文件");
		jLabelTimeout.setBounds(8, 34, 132, 15);
		jLabelTimeout.setText("脚本执行的超时时间(秒)");
		jSpTimeout.setBounds(145, 32, 90, 20);
		jSpTimeout.setPreferredSize(new Dimension(90, 20));
		jSpTimeout.setMaximumSize(new Dimension(120, 20));
		jTFFilePath.setBounds(181, 5, 168, 20);
		jTFFilePath.setText("");
		jTFFilePath.setEditable(false);
		jTFFilePath.setPreferredSize(new Dimension(261, 20));
		jBFind.setBounds(350, 5, 60, 20);
		jBFind.setText("浏览");
		jBFind.setPreferredSize(new Dimension(60, 20));
		jBFind.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jBFind_actionPerformed(e);
			}
		});
		jBEdit.setBounds(410, 5, 60, 20);
		jBEdit.setText("编辑");
		jBEdit.setPreferredSize(new Dimension(60, 20));
		jBEdit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jBEdit_actionPerformed(e);
			}
		});
		getContentPane().add(panelAll);
		panelAll.add(jPanelBody, BorderLayout.CENTER);
		JPanel jPanelBodyNorth = new JPanel();
		jPanelBodyNorth.setLayout(null);
		jPanelBodyNorth.add(jLabelHint);
		jPanelBodyNorth.add(jTFFilePath);
		jPanelBodyNorth.add(jBFind);
		jPanelBodyNorth.add(jBEdit);
		jPanelBodyNorth.add(jLabelTimeout);
		jPanelBodyNorth.add(jSpTimeout);
		jPanelBodyNorth.setPreferredSize(new Dimension(200, 60));
		// paramEditor.setMinimumSize(new Dimension(340, 100));
		jPanelBody.add(jPanelBodyNorth, BorderLayout.NORTH);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.insertTab("参数", null, paramEditor, "配置脚本参数", 0);
		tabbedPane.setMinimumSize(new Dimension(340, 120));
		jPanelBody.add(tabbedPane, BorderLayout.CENTER);
		jPanelBody.setBorder(new EmptyBorder(10, 15, 10, 15));
		panelAll.add(jPanelAct, BorderLayout.SOUTH);
		jPanelAct.add(jBOk, null);
		jPanelAct.add(jBCanel, null);
	}

	private boolean verifyFile(String filepath) {
		if (filepath == null || filepath.equals("")) {
			JOptionPane.showMessageDialog(this, "请选择或者输入文件路径！", "警告", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	void jBOk_actionPerformed(ActionEvent e) {
		try {
			String rulePath = jTFFilePath.getText().trim();
			if (!verifyFile(rulePath)) 
				return;			
			extPara.setScriptFilePath(SCRIPT_DIR + jTFFilePath.getText().trim());			
			extPara.setTimeout(snm.getNumber().intValue() * 1000);
			paramEditor.applyModify();
			extPara.setParams(paramEditor.getParams());
			dispose();
		} catch (Exception ex) {
			ErrorDlg dlg = ErrorDlg.createErrorDlg(this, true, ex);
			dlg.setVisible(true);
		}
	}

	void jBCanel_actionPerformed(ActionEvent e) {
		this.extPara = null;
		dispose();
	}

	void jBFind_actionPerformed(ActionEvent e) {
		RemoteFile file = getRemoteFileMgr().showSelect();
		if (file == null)
			return;
		
		updateFile(file.getFile());
	}
	
	private void updateFile(String file) {
		jTFFilePath.setText(file);
		try {						
			DynamicParam[] params = ScriptTester.parseParam(serverFactory.getSystemService(), SCRIPT_DIR + file);
			paramEditor.setParams(params);
			extPara.setParams(params);
		} catch (Throwable e1) {
			ErrorDlg.createErrorDlg(ScriptChooserDlg.this, "解析脚本动态参数失败", e1);
			return;
		}		
	}

	void jBEdit_actionPerformed(ActionEvent e) {
		String filename = jTFFilePath.getText();
		if (filename == null || filename.length() == 0) {
			RemoteFile file = getRemoteFileMgr().showCreate();
			if (file == null)
				return;			
			filename = file.getFile();
		} else {
			getRemoteFileMgr().showEdit(filename);
		}
		updateFile(filename);
	}

	private RemoteFileMgrPanel getRemoteFileMgr() {
		if (remoteFileMgr == null) 
			remoteFileMgr = new RemoteFileMgrPanel(getOwner(), new RemoteIOClient(serverFactory.getFileService()), "通用监测脚本管理", SCRIPT_DIR);
		return remoteFileMgr;
	}

	public void setExtParameter(ExtParameter extPara) {
		this.extPara = extPara;
		String path = extPara.getScriptFilePath();
		if (path.startsWith(SCRIPT_DIR))
			path = path.substring(SCRIPT_DIR.length());
		jTFFilePath.setText(path);
		snm.setValue(new Integer(extPara.getTimeout() < 1000 ? 30 : extPara.getTimeout() / 1000));
		paramEditor.setParams(extPara.getParams());
	}

	public ExtParameter getExtParameter() {
		return this.extPara;
	}
}