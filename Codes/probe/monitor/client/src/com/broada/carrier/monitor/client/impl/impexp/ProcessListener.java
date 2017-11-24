package com.broada.carrier.monitor.client.impl.impexp;

import javax.swing.JProgressBar;

/**
 * 导入导出进度的监听
 * 
 * @author xusc 
 * Create By 2016年6月20日 下午5:06:11
 */
public abstract class ProcessListener {
	private int maxLength;
	private int progress;
	private JProgressBar progressBar;
	
	public ProcessListener(JProgressBar progressBar) {
		this.progressBar = progressBar;
		this.progressBar.setMinimum(0);
		this.progressBar.setMaximum(100);
	}

	/**
	 * 获取最大进度值
	 * @return
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * 设置最大进度值，调用该方法会将当前进度重置为0
	 * @param maxLength
	 */
	public void setMaxLength(int maxLength){
		this.maxLength = maxLength;
		setProgress(0);
	}
	
	/**
	 * 获取当前进度值
	 * @return
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * 设置显示的进度
	 * @param currentProgress
	 */
	public void setProgress(int currentProgress) {
		if(maxLength < 0)
			throw new NullPointerException("无效的导出进度最大值：" + maxLength);
		if(currentProgress > maxLength)
			throw new IllegalArgumentException("当前进度：" + currentProgress + "，大于最大进度：" + maxLength);
		this.progress = currentProgress;
		progressBar.setValue(this.progress * 100 / maxLength);
		progress();
	}
	
	/**
	 * 获取当前进度百分比，介于0-100
	 * @return
	 */
	public int getCurrentProgress(){
		return this.progress * 100 / maxLength;
	}
	
	/**
	 * 继承类自定义的进度更新时操作
	 */
	public abstract void progress();
	
	/**
	 * 向消息框中添加消息
	 * @param appendMsg
	 */
	public abstract void appendMsg(String appendMsg);
	
	/**
	 * 为消息框设置显示消息
	 * @param msg
	 */
	public abstract void setMsg(String msg);
}
