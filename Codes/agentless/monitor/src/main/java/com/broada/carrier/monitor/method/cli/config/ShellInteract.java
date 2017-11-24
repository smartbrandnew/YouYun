package com.broada.carrier.monitor.method.cli.config;

public class ShellInteract {
	private Command shellStart;
	private Command shellEnd;
	private Command shellExec;

	public Command getShellStart() {
		return shellStart;
	}

	public void setShellStart(Command shellStart) {
		this.shellStart = shellStart;
	}

	public Command getShellEnd() {
		return shellEnd;
	}

	public void setShellEnd(Command shellEnd) {
		this.shellEnd = shellEnd;
	}

	public Command getShellExec() {
		return shellExec;
	}

	public void setShellExec(Command shellExec) {
		this.shellExec = shellExec;
	}

}
