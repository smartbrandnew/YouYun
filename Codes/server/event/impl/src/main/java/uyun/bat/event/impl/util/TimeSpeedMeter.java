package uyun.bat.event.impl.util;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 支持获取最近多少时间性能的计数器
 * 支持多线程同时调用其step方法
 * 使用方法：
 * 1. 初始化，全局一个就可以了
 *    TimeSpeedMeter meter = new TimeSpeedMeter(15);
 * 2. 每处理完一个页面就增加计数
 *    meter.step();
 * 3. 定期输出日志：
 *    System.out.println(meter.getSpeedDescr(1, 5, 15));
 *    // 输出：最近1分钟[count: 30 tps: 0.5] 最近5分钟[时间不足] 最近15分钟[时间不足]
 */
public class TimeSpeedMeter {
	private static final int WINDOW_SECONDS = 5;
	private int minutes;
	private int windows;
	private LinkedList<Counter> counters = new LinkedList<>();

	public TimeSpeedMeter(int minutes) {
		this.minutes = minutes;
		this.windows = minutes * 60 / WINDOW_SECONDS;
	}

	/**
	 * 用当前时间，增加1个计数
	 */
	public void step() {
		step(1);
	}

	/**
	 * 获取已经连续测量的多久了
	 * @return
	 */
	public long getMeterSeconds() {
		step(0);
		return counters.size() * WINDOW_SECONDS;
	}

	/**
	 * 获取最近几分钟的性能描述
	 * 调用getSpeedDescr(1, 5, 15)，则返回，最近1分钟[count: 2.3 tps: 80] 最近5分钟[count: 2.3 tps: 400] 最近15分钟[时间不足]
	 * @param minutes
	 * @return
	 */
	public String getSpeedDescr(int...minutes) {
		StringBuilder sb = new StringBuilder();
		long meterMinutes = getMeterSeconds() / 60;
		for (int item : minutes) {
			if (sb.length() > 0)
				sb.append(" ");
			sb.append("最近").append(item).append("分钟[");
			if (item <= meterMinutes) {
				long count = getLastMinutesCount(item);
				sb.append("count: ").append(count);
				sb.append(" tps: ").append(new BigDecimal(count / (item * 60.0)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
			} else {
				sb.append("时间不足");
			}
			sb.append("]");
		}
		return sb.toString();
	}

	/**
	 * 获取最近几分钟已经获取多少计数了
	 * @param minutes
	 * @return
	 */
	public long getLastMinutesCount(int minutes) {
		synchronized (counters) {
			step(0);
			long count = 0;
			int windows = minutes * 60 / WINDOW_SECONDS;
			Iterator<Counter> iter = counters.iterator();
			for (int i = 0; i < windows && iter.hasNext(); i++) {
				Counter counter = iter.next();
				count += counter.count;
			}
			return count;
		}
	}

	/**
	 * 用当前时间，增加count个计数
	 * @param count
	 */
	public void step(int count) {
		long window = currentWindow();
		synchronized (counters) {
			Counter counter = counters.peek();
			if (counter == null) {
				counter = new Counter(window, count);
				counters.push(counter);
			} else if (counter.window != window) {
				for (long i = counter.window; i < window; i++) {
					counter = new Counter(i, 0);
					counters.push(counter);
				}
				counter = new Counter(window, count);
				counters.push(counter);
				while (counters.size() > windows)
					counters.pop();
			} else
				counter.count += count;
		}
	}

	private long currentWindow() {
		return System.currentTimeMillis() / (1000 * WINDOW_SECONDS);
	}

	private static class Counter {
		private long window;
		private long count;

		public Counter() {
		}

		public Counter(long window, long count) {
			this.window = window;
			this.count = count;
		}
	}
}
