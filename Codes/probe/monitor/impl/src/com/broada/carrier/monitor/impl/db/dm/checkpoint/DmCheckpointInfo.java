package com.broada.carrier.monitor.impl.db.dm.checkpoint;

/**
 * 监测项目点属性类
 * 
 * @author Zhouqa
 * Create By 2016年4月7日 上午10:49:37
 */
public class DmCheckpointInfo {
  public static String[] keys ={"CHECKPOINT_TOTAL_COUNT","CHECKPOINT_BY_REDO_RESERVE","CHECKPOINT_TIME(MS)_USED"};
  public static String[] names = {"检查点发生数","重储检查发生点数","检查已消耗时间"};
  public static Integer[] gateValues = {new Integer(5),new Integer(5),new Integer(50)};
  public static String[] units = {"次","次","毫秒"};
	 /* 监测项目名 */
  String name;

  /* 监测项目实际值 */
  int value;

  /* 监测比较类型 */
  String comType;

  /* 监测项目阈值 */
  Integer gateValue = new Integer(0);

  /* 监测项目单位 */
  String unit;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getComType() {
		return comType;
	}

	public void setComType(String comType) {
		this.comType = comType;
	}

	public Integer getGateValue() {
		return gateValue;
	}

	public void setGateValue(Integer gateValue) {
		this.gateValue = gateValue;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
  
}
