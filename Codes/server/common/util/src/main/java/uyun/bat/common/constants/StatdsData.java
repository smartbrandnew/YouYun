package uyun.bat.common.constants;

public class StatdsData {
	/**
	 * 
	 * @param 
	 * statsd数据格式<name>:<value>|<metric_type>|@<sample_rate>|#<tag1_name>:<tag1_value>,<tag2_name>:<tag2_value>:<value>...
	 */
	public static String generateStatsdData(String metricName, double value, int interval, String tags) {
		return metricName + ":" + value + "|g|@"+interval+"|#" + tags;
	}
}
