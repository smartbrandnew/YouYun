package com.broada.carrier.monitor.method.cli.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.entity.CLIErrorLine;

/**
 * 用于表示没有采集到任何对象，避免Collector返回null产生NPE
 * @author Jiangjw
 */
public class EmptyCLIResult extends AbstractCLIResult {	
	private List<Properties> list = new ArrayList<Properties>(0);
	private Properties props = new Properties();
	
	public EmptyCLIResult(CLIErrorLine[] errorLines) {
		errLines = errorLines;
	}

	public EmptyCLIResult() {
	}

	@Override
	public List<Properties> getListTableResult() {
		return list;
	}

	@Override
	public Properties getPropResult() {
		return props;
	}
}
