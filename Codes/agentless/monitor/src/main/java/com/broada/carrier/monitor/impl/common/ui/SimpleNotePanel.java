package com.broada.carrier.monitor.impl.common.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * 简单注释面板
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class SimpleNotePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BorderLayout borderLayout1 = new BorderLayout();
  private TitledBorder titledBorder1;
  private JLabel jLabNote = new JLabel();

  public SimpleNotePanel(String title, String note) {
    try {
      jbInit();
      titledBorder1.setTitle(title);
      setNote(note);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public SimpleNotePanel(String note) {
    this("说明", note);
  }

  public SimpleNotePanel() {
    this("说明", "");
  }

  void jbInit() throws Exception {
    titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.
        white, new Color(148, 145, 140)), "说明");
    this.setBorder(titledBorder1);
    this.setLayout(borderLayout1);
    jLabNote.setHorizontalAlignment(SwingConstants.LEADING);
    jLabNote.setVerticalAlignment(SwingConstants.TOP);
    this.add(jLabNote, BorderLayout.CENTER);
  }

  public void setTitle(String title) {
    titledBorder1.setTitle(title);
  }

  public void setNote(String note) {
    jLabNote.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;" + note + "</html>");
  }
}