package com.broada.carrier.monitor.common.tomcat;

import org.apache.juli.logging.Log;
import org.slf4j.Logger;

public class Slf4jLogger implements Log {
	private Logger target;

	public Slf4jLogger(Logger target) {
		this.target = target;
	}

	@Override
	public boolean isDebugEnabled() {
		return target.isDebugEnabled();
	}

	@Override
	public boolean isErrorEnabled() {
		return target.isErrorEnabled();
	}

	@Override
	public boolean isFatalEnabled() {
		return target.isErrorEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return target.isInfoEnabled();
	}

	@Override
	public boolean isTraceEnabled() {
		return target.isTraceEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return target.isWarnEnabled();
	}

	@Override
	public void trace(Object paramObject) {
		target.trace(paramObject.toString());
	}

	@Override
	public void trace(Object paramObject, Throwable paramThrowable) {
		target.trace(paramObject.toString(), paramThrowable);
	}

	@Override
	public void debug(Object paramObject) {
		target.debug(paramObject.toString());
	}

	@Override
	public void debug(Object paramObject, Throwable paramThrowable) {
		target.debug(paramObject.toString(), paramThrowable);
	}

	@Override
	public void info(Object paramObject) {
		target.info(paramObject.toString());
	}

	@Override
	public void info(Object paramObject, Throwable paramThrowable) {
		target.info(paramObject.toString(), paramThrowable);
	}

	@Override
	public void warn(Object paramObject) {
		target.warn(paramObject.toString());
	}

	@Override
	public void warn(Object paramObject, Throwable paramThrowable) {
		target.warn(paramObject.toString(), paramThrowable);
	}

	@Override
	public void error(Object paramObject) {
		target.error(paramObject.toString());
	}

	@Override
	public void error(Object paramObject, Throwable paramThrowable) {
		target.error(paramObject.toString(), paramThrowable);
	}

	@Override
	public void fatal(Object paramObject) {
		target.error(paramObject.toString());
	}

	@Override
	public void fatal(Object paramObject, Throwable paramThrowable) {
		target.error(paramObject.toString(), paramThrowable);
	}

}
