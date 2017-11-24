package com.broada.carrier.monitor.impl.db.oracle.advancedQueue;
/**
 * 高级队列实体
 * @author zhouqr
 *
 */
public class OracleAdvanceQue {
	public static String[] alermItem = new String[] { "_msgTotal", "_errMsg",
			"_readyMsg", "_avgWait" };

	public static final String[] units = { "秒", "分", "小时","天"};

	public static final int[] unit_type = { 0, 1, 2, 3 };// 0:小时；1:分；2:秒

	private Boolean isWacthed = Boolean.FALSE;

	private String queName = "";// 队列名称

	private String queOwner = "";// 队列拥有者

	private String queTable = "";// 队列表

	private String qid = "";// 队列标识

	private Integer msgTotalNum = new Integer(0);// 消息总数

	private Integer maxMsgTotalNum = new Integer(90);// 消息总数阈值

	private Integer errMsgNum = new Integer(0);// 错误消息数量

	private Integer maxErrMsgNum = new Integer(90);// 错误消息数量阈值

	private Integer readyMsgNum = new Integer(0);// ready状态的消息数量

	private Integer maxReadyMsgNum = new Integer(90);// ready状态的消息数量阈值

	private Double arvWaitTime = new Double(0);// 平均等待时间

	private Double maxArvWaitTime = new Double(1000);// 最大平均等待时间

	private int timeUnit;// 时间单位

	public int getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(int timeUnit) {
		this.timeUnit = timeUnit;
	}

	public String getQid() {
		return qid;
	}

	public void setQid(String qid) {
		this.qid = qid;
	}

	public String getQueOwner() {
		return queOwner;
	}

	public void setQueOwner(String queOwner) {
		this.queOwner = queOwner;
	}

	public String getQueTable() {
		return queTable;
	}

	public void setQueTable(String queTable) {
		this.queTable = queTable;
	}

	public Boolean getIsWacthed() {
		return isWacthed;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public String getQueName() {
		return queName;
	}

	public void setQueName(String queName) {
		this.queName = queName;
	}

	public Integer getErrMsgNum() {
		return errMsgNum;
	}

	public void setErrMsgNum(Integer errMsgNum) {
		this.errMsgNum = errMsgNum;
	}

	public Integer getMsgTotalNum() {
		return msgTotalNum;
	}

	public void setMsgTotalNum(Integer msgTotalNum) {
		this.msgTotalNum = msgTotalNum;
	}

	public Integer getReadyMsgNum() {
		return readyMsgNum;
	}

	public void setReadyMsgNum(Integer readyMsgNum) {
		this.readyMsgNum = readyMsgNum;
	}

	public Double getArvWaitTime() {
		return arvWaitTime;
	}

	public void setArvWaitTime(Double arvWaitTime) {
		this.arvWaitTime = arvWaitTime;
	}

	public Double getMaxArvWaitTime() {
		return maxArvWaitTime;
	}

	public void setMaxArvWaitTime(Double maxArvWaitTime) {
		this.maxArvWaitTime = maxArvWaitTime;
	}

	public Integer getMaxErrMsgNum() {
		return maxErrMsgNum;
	}

	public void setMaxErrMsgNum(Integer maxErrMsgNum) {
		this.maxErrMsgNum = maxErrMsgNum;
	}

	public Integer getMaxMsgTotalNum() {
		return maxMsgTotalNum;
	}

	public void setMaxMsgTotalNum(Integer maxMsgTotalNum) {
		this.maxMsgTotalNum = maxMsgTotalNum;
	}

	public Integer getMaxReadyMsgNum() {
		return maxReadyMsgNum;
	}

	public void setMaxReadyMsgNum(Integer maxReadyMsgNum) {
		this.maxReadyMsgNum = maxReadyMsgNum;
	}

	/** 条件condition的KEY值 */
	public String getMsgTotalConditionName() {
		return queName + ":" + qid + ":" + alermItem[0];
	}

	public String getErrMsgConditionName() {
		return queName + ":" + qid + ":" + alermItem[1];
	}

	public String getReadyMsgConditionName() {
		return queName + ":" + qid + ":" + alermItem[2];
	}

	public String getAvgWaitMsgConditionName() {
		return queName + ":" + qid + ":" + alermItem[3];
	}

	public static int getTimeTypeByStr(String timeUnit) {
		for (int i = 0; i < units.length; i++) {
			if (timeUnit.equals(units[i])) {
				return unit_type[i];
			}
		}
		return -1;// 未知
	}

	public static String getTimeTypeStr(int timeType) {
		if (timeType > unit_type.length || timeType < 0) {
			return "未知";
		}
		return units[timeType];

	}
}
