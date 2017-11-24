package com.broada.carrier.monitor.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.ThreadUtil;

public class DelayTask {
	private static final Logger logger = LoggerFactory.getLogger(DelayTask.class);
	private String id;
	private long delaySeconds;
	private long lastRunTime;
	private long waitMs;
	private Runnable runnable;
	private Thread thread;

	public DelayTask(String id, long delaySeconds, Runnable runnable) {
		this.id = id;
		this.delaySeconds = delaySeconds * 1000;
		this.runnable = runnable;
	}

	public synchronized void execute() {
		if (thread != null) {
			logger.debug("执行任务取消[{}]", this);
			return;
		}

		long now = System.currentTimeMillis();
		long diff = now - lastRunTime - delaySeconds;
		if (diff > 0) {
			run(now);
		} else {
			waitMs = delaySeconds + diff;
			thread = ThreadUtil.createThread(new TaskRunner());
			thread.start();
		}
	}
	
	private class TaskRunner implements Runnable {
		@Override
		public void run() {
			try {
				logger.debug("执行任务等待[{}]，时间{}ms", this, waitMs);
				Thread.sleep(waitMs);
				DelayTask.this.run(System.currentTimeMillis());
			} catch (Throwable e) {
				ErrorUtil.warn(logger, String.format("执行任务失败[%s]", this), e);
			} finally {
				thread = null;
			}
		}
	}

	private void run(long now) {
		logger.debug("执行任务开始[{}]", this);
		lastRunTime = now;
		runnable.run();
		logger.debug("执行任务完成[{}]", this);
	}

	@Override
	public String toString() {
		return String.format("id: %s runner: %s", id, runnable);
	}
}
