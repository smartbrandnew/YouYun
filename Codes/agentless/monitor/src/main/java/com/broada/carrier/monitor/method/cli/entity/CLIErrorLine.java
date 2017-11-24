package com.broada.carrier.monitor.method.cli.entity;

import java.io.Serializable;

/**
 * 封装一个CLI解析失败的行
 * @author Jiangjw
 */
public class CLIErrorLine implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String content;

	public CLIErrorLine(int id, String content) {
		super();
		this.id = id;
		this.content = content;
	}

	public int getId() {
		return id;
	}

	public String getContent() {
		return content;
	}

}
