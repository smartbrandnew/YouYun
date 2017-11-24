package uyun.bat.datastore.mq.macro.metric;

import uyun.bat.datastore.api.mq.ComplexMetricData;
import uyun.bat.datastore.mq.macro.AbstractMetricMacro;

public class OpenAPISingleMetricMacro extends AbstractMetricMacro {

	@Override
	public int getCode() {
		return ComplexMetricData.TYPE_OPENAPI_SINGLE_METRICS;
	}

	@Override
	public void exec(ComplexMetricData complexMetricData) {
		insertPerf(complexMetricData.getPerfMetricList());
	}
}
