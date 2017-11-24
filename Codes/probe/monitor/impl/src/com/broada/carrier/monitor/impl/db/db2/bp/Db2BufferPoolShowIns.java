package com.broada.carrier.monitor.impl.db.db2.bp;

/**
 * DB2监测实体展现类
 * @author 杨帆
 * 
 */
public class Db2BufferPoolShowIns {
  public static String[] keys = { "POOL_DATA_RATIO", "POOL_INDEX_RATIO", "DIRECT_READS", "DIRECT_WRITES",
      "CAT_CACHE_RATIO", "PKG_CACHE_RATIO" };

  public static String[] names = { "数据缓冲池命中率(1-数据页物理读次数/数据页逻辑读次数)", "索引缓冲池命中率(1-索引页物理读次数/索引页逻辑读次数)", "直接读次数", "直接写次数",
      "目录缓冲命中率", "包缓冲命中率" };

  public static Double[] gateValues = { new Double(80), new Double(80), new Double(0), new Double(0), new Double(80),
      new Double(80) };

  public static String[] units = { "%", "%", "次", "次", "%", "%" };

  /* 监测项目名 */
  String name;

  /* 监测项目实际值 */
  String value;

  /* 监测比较类型 */
  String comType;

  /* 监测项目阈值 */
  Double gateValue = new Double(0);

  /* 监测项目单位 */
  String unit;

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public Double getGateValue() {
    return gateValue;
  }

  public void setGateValue(Double gateValue) {
    this.gateValue = gateValue;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getComType() {
    return comType;
  }

  public void setComType(String comType) {
    this.comType = comType;
  }
}
