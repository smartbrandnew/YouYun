package com.broada.carrier.monitor.common.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class MinuteValueQueue<T> {
	private Queue<MinuteValue<T>> queue;
	private MinuteValue<T> last;
	private int max;
	
	public MinuteValueQueue(int max) {
		this.max = max;
		this.queue = new LinkedList<MinuteValue<T>>();
	}
	
	public MinuteValue<T> getValue() {
		long minute = getMinute();
		
		if (last == null || last.getMinute() != minute) {
			last = new MinuteValue<T>(minute);
			add(last);			
		}
		
		return last;
	}
	
	private void add(MinuteValue<T> mv) {
		if (queue.size() >= max)
			queue.poll();
		queue.add(mv);
	}

	private long getMinute() {
		return System.currentTimeMillis() / 60000;
	}
	
	public Collection<MinuteValue<T>> getValues() {
		return queue;
	}

	public static class MinuteValue<T> {
		private long minute;
		private T value;

		public MinuteValue(long minute) {
			this.minute = minute;
		}

		public long getMinute() {
			return minute;
		}

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}

	}
}
