package com.broada.carrier.monitor.impl.generic;

/**
 * 指标引用异常
 * 
 * @author Sting
 *
 */
public class ItemReferenceException extends Exception{

	private static final long serialVersionUID = 8311511064876797885L;

	public ItemReferenceException(){
	}
	
	public ItemReferenceException(String message){
		super(message);
	}
	
	public ItemReferenceException(String message, Throwable cause){
		super(message, cause);
	}
	
	public ItemReferenceException(Throwable cause){
		super(cause);
	}
}
