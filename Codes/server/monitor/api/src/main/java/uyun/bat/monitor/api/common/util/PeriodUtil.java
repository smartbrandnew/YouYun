package uyun.bat.monitor.api.common.util;

import java.util.regex.Pattern;

/**
 * 时间窗格
 */
public abstract class PeriodUtil {
	private static final Pattern pattern = Pattern.compile("^[0-9]+[m,h]$");

	/**
	 * 匹配时间窗格是否合法
	 * 
	 * @param period
	 * @return
	 */
	public static boolean check(String period) {
		if (period == null || period.length() == 0)
			return false;
		return pattern.matcher(period).matches();
	}

	public static Period generatePeriod(String period) {
		if (!check(period))
			return null;
		long end = System.currentTimeMillis();
		long start = 0;
		long interval = 0;

		if (period.endsWith("m")) {
			start = end - Integer.parseInt(period.substring(0, period.length() - 1)) * 60 * 1000;
			interval = Integer.parseInt(period.substring(0, period.length() - 1)) * 60 * 1000;
		} else if (period.endsWith("h")) {
			start = end - Integer.parseInt(period.substring(0, period.length() - 1)) * 60 * 60 * 1000;
			interval = Integer.parseInt(period.substring(0, period.length() - 1)) * 60 * 60 * 1000;
		}
		Period pattern = new Period();
		pattern.setEnd(end);
		pattern.setStart(start);
		pattern.setInterval(interval);
		return pattern;
	}

	public static class Period {
		private long start;
		private long end;
		private long interval;

		public long getStart() {
			return start;
		}

		public void setStart(long start) {
			this.start = start;
		}

		public long getEnd() {
			return end;
		}

		public void setEnd(long end) {
			this.end = end;
		}

		public long getInterval() {
			return interval;
		}

		public void setInterval(long interval) {
			this.interval = interval;
		}
	}

	public static void main(String[] args) {
		Period p = generatePeriod("10h");
		System.out.println(p.getStart());
		System.out.println(p.getEnd());
	}
}
