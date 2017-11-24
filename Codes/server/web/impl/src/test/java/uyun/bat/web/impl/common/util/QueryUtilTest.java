package uyun.bat.web.impl.common.util;

import static org.junit.Assert.*;

import org.junit.Test;

import uyun.bat.web.impl.common.entity.QueryParam;
import uyun.bat.web.impl.common.entity.SingleValueQueryParam;
import uyun.bat.web.impl.common.entity.Tag;
import uyun.bat.web.impl.common.entity.TopNQueryParam;

public class QueryUtilTest {
	String avg = "avg:system.load.1{host:zhuenliangdeMacBook-Pro-2.local;role:aa}";
	String top = "top(avg:system.load.1{host:zhuenliangdeMacBook-Pro-2.local;role:aa}{host:A},10,'mean', 'desc')";
	//String top = "top(avg:mysql.performance.com_insert_select{*} by{host} exclude{host:A},10,'avg','desc')";
	// String top="top(avg:activemq.broker.store_pct{host:ROFAFLOL}
	// by{数据中心};10;avg;desc)";
	String value = "avg:system.load.1{host:zhuenliangdeMacBook-Pro-2.local;role:aa}";

	@Test
	public void testParseSeriesQuery() {
		QueryParam avgP = QueryUtil.parseSeriesQuery(avg);
		boolean isAvg = avgP.getAggregator().equals("avg") && avgP.getMetric().equals("system.load.1")
				&& avgP.getScope().get(0).getKey().equals("host")
				&& avgP.getScope().get(0).getValue().equals("zhuenliangdeMacBook-Pro-2.local");

		System.out.println("\n\nthis is SeriesQuery\n");
		System.out.println("Aggregator:" + avgP.getAggregator());
		System.out.println("Metric:" + avgP.getMetric());
		for (Tag t : avgP.getScope()) {
			System.out.println("key:" + t.getKey());
			System.out.println("vlaue:" + t.getValue());
		}
		assertTrue(isAvg);
	}

	@Test
	public void testParseValueQuery() {
		SingleValueQueryParam valueP = QueryUtil.parseValueQuery(value);
		boolean isValue = valueP.getAggregator().equals("avg") && valueP.getAggregatorValue().equals("avg")
				&& valueP.getMetric().equals("system.load.1")
				&& valueP.getScope().get(0).getKey().equals("host")
				&& valueP.getScope().get(0).getValue().equals("zhuenliangdeMacBook-Pro-2.local");
		System.out.println("\n\nthis is SingleValueQuery\n");
		System.out.println("Aggregator:" + valueP.getAggregator());
		System.out.println("AggregatorValue:" + valueP.getAggregatorValue());
		System.out.println("Metric:" + valueP.getMetric());
		for (Tag t : valueP.getScope()) {
			System.out.println("Key:" + t.getKey());
			System.out.println("Value:" + t.getValue());
		}
		assertTrue(isValue);
	}

	@Test
	public void testParseTopQuery() {
		TopNQueryParam topP = QueryUtil.parseTopQuery(top);
		boolean isTop = topP.getAggregator().equals("avg") && topP.getMetric().equals("system.load.1")
				&& topP.getScope().get(0).getKey().equals("host")
				&& topP.getScope().get(0).getValue().equals("zhuenliangdeMacBook-Pro-2.local")
				&& topP.getScope().get(1).getKey().equals("role")
				&& topP.getScope().get(1).getValue().equals("aa");
		System.out.println("\n\nthis is TopQuery\n");
		System.out.println("Aggregator:" + topP.getAggregator());
		System.out.println("AggregatorValue:" + topP.getAggregatorValue());
		System.out.println("GroupBy:" + topP.getGroupBy());
		System.out.println("Metric:" + topP.getMetric());
		System.out.println("Order:" + topP.getOrder());
		System.out.println("Limit:" + topP.getLimit());
		for (Tag t : topP.getScope()) {
			System.out.println("Key:" + t.getKey());
			System.out.println("value:" + t.getValue());
		}
		assertTrue(isTop);
	}

}
