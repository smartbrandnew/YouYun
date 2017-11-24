package com.broada.carrier.monitor.impl.generic.script;

public class ScriptException extends Exception{

	private static final long serialVersionUID = -4111417863636683707L;

	public ScriptException(){
		super();
	}
	
	public ScriptException(String message){
		super(message);
	}
	
	public ScriptException(String message,Throwable cause){
		super(message,cause);
	}
	
	public ScriptException(Throwable cause){
		super(cause);
	}
}
