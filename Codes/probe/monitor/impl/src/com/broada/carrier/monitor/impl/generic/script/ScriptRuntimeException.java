package com.broada.carrier.monitor.impl.generic.script;

public class ScriptRuntimeException extends RuntimeException{

	private static final long serialVersionUID = 1403682050078585946L;

	public ScriptRuntimeException(){
		super();
	}
	
	public ScriptRuntimeException(String message){
		super(message);
	}
	
	public ScriptRuntimeException(String message,Throwable cause){
		super(message,cause);
	}
	
	public ScriptRuntimeException(Throwable cause){
		super(cause);
	}
}
