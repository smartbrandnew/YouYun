package com.broada.carrier.monitor.common.util;

public class SpeedCounter {
	private MinuteValueQueue<Item> queue;

	public SpeedCounter(int max) {
		queue = new MinuteValueQueue<Item>(max);
	}

	public void step(int count, long time) {
		MinuteValueQueue.MinuteValue<Item> mv = queue.getValue();
		if (mv.getValue() == null)
			mv.setValue(new Item());
		mv.getValue().step(count, time);
	}

	public int getCount() {
		int count = 0;
		for (MinuteValueQueue.MinuteValue<Item> mv : queue.getValues()) {
			count += mv.getValue().getCount();			
		}
		return count;
	}
	
	public long getTime() {
		long time = 0;
		for (MinuteValueQueue.MinuteValue<Item> mv : queue.getValues()) {
			time += mv.getValue().getTime();
		}
		return time;
	}
	
	public double getSpeed() {
		return Math.round((getCount() / (getTime() / 1000.0)) * 10) / 10;
	}

	private static class Item {
		private int count;
		private long time;

		public void step(int count, long time) {
			this.count += count;
			this.time += time;			
		}

		public int getCount() {
			return count;
		}

		public long getTime() {
			return time;
		}

	}
}
