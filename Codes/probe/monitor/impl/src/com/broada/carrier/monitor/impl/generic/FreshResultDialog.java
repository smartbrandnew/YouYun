package com.broada.carrier.monitor.impl.generic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.broada.swing.util.WinUtil;

public class FreshResultDialog extends JDialog {
  private static final long serialVersionUID = -543092098756830107L;
  private JTextArea jtext = new JTextArea();
  private JScrollPane jScrollPane = new JScrollPane();
  public FreshResultDialog(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    initDlg();
  }

  public FreshResultDialog(Dialog dialog, String title, boolean modal) {
    super(dialog, title, modal);
    initDlg();
  }

  public FreshResultDialog() {
    this( (Frame)null, "脚本命令执行查看", false);
  }

  public void initDlg() {
    try {
      jbInit();
      WinUtil.toCenter(this);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  /**
   * 创建一个对话框
   * @param owner
   * @param title
   * @param modal
   * @return
   */
  public static FreshResultDialog createDialog(Component owner, String title,
                                              boolean modal) {
    Window window = WinUtil.getWindowForComponent(owner);
    FreshResultDialog dlg = null;
    if (window instanceof Frame) {
      dlg = new FreshResultDialog( (Frame) window, title, modal);
    } else {
      dlg = new FreshResultDialog( (Dialog) window, title, modal);
    }
    return dlg;
  }

  /**
   * 创建一个对话框
   * @param owner
   * @return
   */
  public static FreshResultDialog createDialog(Component owner) {
    return createDialog(owner, "脚本命令执行查看", true);
  }

  private void jbInit() throws Exception {
    setSize(new Dimension(500, 400));
    jScrollPane.getViewport().add(jtext);
    this.add(jScrollPane,BorderLayout.CENTER);
  }
  
  public void setObject(String result){
    this.jtext.setText(result);
  }
  
  /**
   * 关闭处理
   * @param e
   */
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      this.dispose();
    }
  }
  
}
