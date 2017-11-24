package com.broada.carrier.monitor.impl.storage.huawei.oceanstor.raid;

import java.io.Serializable;


/**
 * 存储池运行状态
 * 
 * @author yanwl 
 * Create By 2017-08-07 下午16:30:29
 */
public enum RunningStatus implements Serializable{
	
	UNKNOW(0, "未知"), 
	
	NORMAL(1, "正常"), 

	RUNNING(2, "运行"), 
	
	NOT_RUNNING(3, "未运行"),
	
	NOT_EXISTED(4, "不存在"), 
	
	SLEEP_IN_HIGH_TEMPERATURE(5, "高温休眠"), 
	
	STARTING(6, "正在启动"), 
	
	POWER_FAILURE_PROTECTION(7, "掉电保护"), 
	
	SPIN_DOWN(8, "休眠"),
	
	STARTED(9, "已启动"),
	
	LINK_UP(10, "已连接"),
	
	LINK_DOWN(11, "未连接"),
	
	POWERING_ON(12, "正在上电"),
	
	POWERED_OFF(13, "已下电"),
	
	PRECOPY(14, "预拷贝"),
	
	COPYBACK(15, "回拷"),
	
	RECONSTRUCTION(16, "重构"),
	
	EXPANSION(17, "扩容"),
	
	UNFORMATTED(18, "未格式化"),
	
	FORMATTING(19, "正在格式化"),
	
	UNMAPPED(20, "未映射"),
	
	INITIAL_SYNCHRONIZING(21, "正在初始同步"),
	
	CONSISTENT(22, "数据一致"),
	
	SYNCHRONIZING(23, "正在同步"),
	
	SYNCHRONIZED(24, "已同步"),
	
	UNSYNCHRONIZED(25, "未同步"),
	
	SPLITED(26, "已分裂"),
	
	ONLINE(27, "在线"),
	
	OFFLINE(28, "离线"),
	
	LOCKED(29, "已锁定"),
	
	ENABLED(30, "已启用"),
	
	DISABLED(31, "已禁用"),
	
	BALANCING(32, "正在均衡"),
	
	TO_BE_RECOVERED(33, "待恢复"),
	
	INTERRUPTED(34, "异常断开"),
	
	INVALID(35, "失效"),
	
	NOT_START(36, "未启动"),
	
	QUEUING(37, "正在排队"),
	
	STOPPED(38, "已停止"),
	
	COPYING(39, "正在拷贝"),
	
	COMPLETED(40, "拷贝完成/完成"),
	
	PAUSED(41, "暂停"),
	
	REVERSE_SYNCHRONIZING(42, "正在反向同步"),
	
	ACTIVATED(43, "已激活"),
	
	RESTORE(44, "正在回滚"),
	
	INACTIVE(45, "未激活"),
	
	IDLE(46, "等待"),
	
	POWERING_OFF(47, "正在下电"),
	
	CHARGING(48, "正在充电"),
	
	CHARGING_COMPLETED(49, "充电完成"),
	
	DISCHARGING(50, "正在放电"),
	
	UPGRADING(51, "正在升级"),
	
	POWER_LOST(52, "掉电中"),
	
	INITIALIZING(53, "初始化中"),
	
	APPLY_CHANGE(54, "正在应用变更"),
	
	ONLINE_DISABLE(55, "在线禁用"),
	
	OFFLINE_DISABLE(56, "离线禁用"),
	
	ONLINE_FROZEN(57, "在线冻结"),
	
	OFFLINE_FROZEN(58, "离线冻结"),
	
	CLOSED(59, "已关闭"),
	
	REMOVING(60, "(节点)删除中"),
	
	INSERVICE(61, "服务中"),
	
	OUTOF_SERVICE(62, "退出服务"),
	
	RUNNING_NORMAL(63, "正在销毁"),
	
	RUNNING_FAIL(64, "销毁失败"),
	
	RUNNING_SUCCESS(65, "销毁成功"),
	
	RUNNING_SUCCESSED(66, "任务执行成功"),
	
	RUNNING_FAILED(67, "任务执行失败"),
	
	WAITING(68, "任务正在等待"),
	
	CANCELLING(69, "任务正在取消"),
	
	CANCELLED(70, "任务已取消"),
	
	ABOUT_TO_SYNCHRONIZE(71, "在线|即将灾备同步"),
	
	SYNCHRONIZING_DATA(72, "在线|正在灾备同步"),
	
	FAILED_TO_SYNCHRONIZE(73, "在线|灾备同步失败"),
	
	FAULT(74, "迁移故障"),
	
	MIGRATING(75, "迁移中"),
	
	MIGRATED(76, "迁移完成"),
	
	ACTIVATING(77, "正在激活"),
	
	DEACTIVATING(78, "正在取消激活"),
	
	START_FAILED(79, "启动失败"),
	
	STOP_FAILED(80, "停止失败"),
	
	DECOMMISSIONING(81, "正在退出服务"),
	
	DECOMMISSIONED(82, "已经退出服务"),
	
	RECOMMISSIONING(83, "重新进入服务"),
	
	REPLACING_NODE(84, "正在替换节点"),
	
	SCHEDULING(85, "正在调度"),
	
	PAUSING(86, "暂停中"),
	
	SUSPENDING(87, "挂起中"),
	
	SUSPENDED(88, "挂起"),
	
	OVER_LOAD(89, "超载"),
	
	TO_BE_SWITCH(90, "等待切换"),
	
	SWITCHING(91, "切换中"),
	
	TO_BE_CLEARUP(92, "等待清理"),
	
	FORCED_START(93, "运行状态：强制启动"),
	
	ERROR(94, "运行状态：故障"),
	
	JOB_COMPLETED(95, "任务结束"),
	
	PARTITION_MIGRATING(96, "分区迁移中"),
	
	MOUNT(97, "已挂载"),
	
	UMOUNT(98, "未挂载"),
	
	INSTALLING(99, "正在安装中"),
	
	TO_BE_SYNCHRONIZED(100, "待同步"),
	
	CONNECTING(101, "正在连接");
	
  private int label;
  private String value;

  public static RunningStatus parseFromValue(String value)
  {
    for (RunningStatus type : values()) {
      if (type.getValue() == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型value=：" + value);
  }
  
  public static RunningStatus parseFromLable(int label)
  {
    for (RunningStatus type : values()) {
      if (type.getLabel() == label) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型label=：" + label);
  }

  private RunningStatus(int label, String value) {
    this.label = label;
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
  
  public int getLabel() {
    return this.label;
  }
  
  public String toString() {
    return this.label + "[" + this.value + "]";
  }
}
