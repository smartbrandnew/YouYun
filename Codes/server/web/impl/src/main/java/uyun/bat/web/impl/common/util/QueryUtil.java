package uyun.bat.web.impl.common.util;

import java.util.ArrayList;
import java.util.List;

import uyun.bat.datastore.api.entity.AggregatorType;
import uyun.bat.web.impl.common.entity.QueryParam;
import uyun.bat.web.impl.common.entity.SingleValueQueryParam;
import uyun.bat.web.impl.common.entity.Tag;
import uyun.bat.web.impl.common.entity.TopNQueryParam;

public abstract class QueryUtil {

	/**
	 * 解析时间序列数据
	 *
	 * @param q
	 * @return
	 */
	static public QueryParam parseSeriesQuery(String q) {
		String[] oldTemp = q.split(" ");
		if(oldTemp.length>3){
			oldTemp[1] = oldTemp[1] + " " + oldTemp[2];
			oldTemp[2] = oldTemp[3];
		}
		String[] temp = oldTemp[0].trim().split("[{;}]");
		String[] firstTemp = temp[0].split(":");
		QueryParam queryParam = new QueryParam();
		queryParam.setAggregator(firstTemp[0]);
		queryParam.setMetric(firstTemp[1]);
		if (q.contains("by{")) {
			String[] groupTemp = oldTemp[1].split("by\\{");
			String groupBy = groupTemp[1].replace("}", "");
			queryParam.setGroupBy(groupBy);
		}
		if (q.contains("exclude{")) {
			String[] excludeTemp = oldTemp[2].split("exclude\\{");
			String exclude = excludeTemp[1].replace("}", "");
			queryParam.setExclude(exclude);
		}
		List<Tag> tags = new ArrayList<Tag>();
		for (int i = 1; i < temp.length; i++) {
			Tag t = new Tag();
			if ("*".equals(temp[i]))
				t.setKey(temp[i]);
			else {
				String[] tempTag = temp[i].split(":", 2);
				if (tempTag.length > 1) {
					t.setKey(tempTag[0]);
					t.setValue(tempTag[1]);
				} else {
					t.setKey(tempTag[0]);
				}
			}
			tags.add(t);
		}
		queryParam.setScope(tags);
		return queryParam;
	}

	/**
	 * 解析标签数据
	 *
	 * @param q
	 * @return
	 */
	static public SingleValueQueryParam parseValueQuery(String q) {
		QueryParam queryParam = parseSeriesQuery(q);
		SingleValueQueryParam singleValueQueryParam = new SingleValueQueryParam();
		singleValueQueryParam.setAggregator(queryParam.getAggregator());
		singleValueQueryParam.setMetric(queryParam.getMetric());
		singleValueQueryParam.setScope(queryParam.getScope());
		singleValueQueryParam.setAggregatorValue(AggregatorType.avg.toString());
		return singleValueQueryParam;
	}

	/**
	 * 解析topN数据
	 *
	 * @param q
	 * @return
	 */
	static public TopNQueryParam parseTopQuery(String q) {
		// 切分的太烂；欢迎改进
		String[] temp = q.split("}")[0].split("[\\({]");
		String[] firstTemp = temp[1].split(":");
		TopNQueryParam queryParam = new TopNQueryParam();
		queryParam.setAggregator(firstTemp[0]);
		queryParam.setMetric(firstTemp[1]);
		String[] lastTemp = temp[2].split(";");
		List<Tag> tags = new ArrayList<Tag>();
		for (int i = 0; i < lastTemp.length; i++) {
			Tag t = new Tag();
			if ("*".equals(lastTemp[0])) {
				t.setKey(lastTemp[0]);
			} else {
				String[] tempTag = lastTemp[i].split(":", 2);
				if (tempTag.length > 1) {
					t.setKey(tempTag[0]);
					t.setValue(tempTag[1]);
				} else {
					t.setKey(tempTag[0]);
				}
			}
			tags.add(t);
		}
		queryParam.setScope(tags);
		String[] t = new String[4];
		if (q.split("}")[1].indexOf("by") > 0) {
			queryParam.setGroupBy(q.split("}")[1].split("\\{")[1]);
		} else {
			t = q.split("}")[1].replace(")", "").replaceAll(" ", "").replaceAll("\'", "").split(",");
		}
		if (q.split("}")[2].indexOf("exclude") > 0) {
			String[] tempstr = q.split("}")[2].split("\\{");
			if (tempstr.length > 1) {
				queryParam.setExclude(q.split("}")[2].split("\\{")[1]);
			} else {
				queryParam.setExclude("");
			}
			t = q.split("}")[3].replace(")", "").replaceAll(" ", "").replaceAll("\'", "").split(",");
		} else {
			t = q.split("}")[2].replace(")", "").replaceAll(" ", "").replaceAll("\'", "").split(",");
		}
		queryParam.setLimit(Integer.parseInt(t[1]));
		queryParam.setAggregatorValue(t[2]);
		queryParam.setOrder(t[3]);
		return queryParam;
	}

	public static void main(String[] args) {
		// String t =
		// "top(avg:system.cpu.pct_usage{host:zhuenliangdeMacBook-Pro-2.local;role}
		// by{业务系统},10,avg,desc)";
		// parseTopQuery(t);
		String s = "avg:system.load.1{host:zhuenliangdeMacBook-Pro-2.local;role:aa} by{业务系统}";
		parseSeriesQuery(s);
	}

}
