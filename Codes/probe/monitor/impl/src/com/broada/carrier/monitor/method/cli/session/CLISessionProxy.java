package com.broada.carrier.monitor.method.cli.session;

import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;

/**
 * 采用代理来支持连接失效重连的功能
 * 
 * @author zhoucy (zhoucy@broada.com.cn)
 * @Email : zhoucy@broada.com
 * @Create By 2006-8-17 下午03:08:55
 */
public class CLISessionProxy implements CLISession {

	private CLISession session = null;

	private boolean isWeak = false;

	private volatile int countInUse = 0;
	private volatile boolean closing = false;

	public CLISessionProxy(CLISession session) {
		this.session = session;
	}

	@Override
	public void close() {
		closing = true;
		if (countInUse <= 0) {
			closing = false;
			session.close();
		}
	}

	@Override
	public String execCmd(String cmd, String[] args, String prompt, StringBuffer localBuf, boolean isLogErr) throws CLIException {
		try {
			countInUse++;
			String result = session.execCmd(cmd, args, prompt, localBuf, isLogErr);
			return result;
		} catch (CLIConnectException e) {
			isWeak = true;
			throw e;
		} finally {
			if (--countInUse <= 0 && closing) {
				closing = false;
				session.close();
			}
		}
	}

	public boolean isWeak() {
		return isWeak;
	}

	@Override
	public String execScript(String scriptFile, String[] args) throws CLIException {
		try {
			countInUse++;
			String result = session.execScript(scriptFile, args);
			return result;
		} catch (CLIConnectException e) {
			isWeak = true;
			throw e;
		} catch (CLILoginFailException e) {
			isWeak = true;
			throw e;
		} finally {
			if (--countInUse <= 0 && closing) {
				closing = false;
				session.close();
			}
		}
	}

	@Override
	public void open(Properties options, boolean isLogErr) throws CLILoginFailException, CLIConnectException {
		session.open(options, isLogErr);
	}

	@Override
	public boolean hasContext() {
		return session.hasContext();
	}

	@Override
	public List<String> runSQL(String cmd, String[] args, StringBuffer localBuf) throws CLIException {
		try {
			countInUse++;
			return session.runSQL(cmd, args, localBuf);
		} finally {
			if (--countInUse <= 0 && closing) {
				closing = false;
				session.close();
			}
		}
	}

	@Override
	public boolean isStanding() {
		return session.isStanding();
	}
}
