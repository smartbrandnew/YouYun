package com.broada.carrier.monitor.impl.db.mysql;

import java.util.Date;

/**
 * MySQL数据库中表的状态描述
 *
 * @author zhoucy (zhoucy@broada.com.cn)
 * @Email : zhoucy@broada.com
 * @Create By 2006-7-27 上午10:08:01
 */
public class TableStatus {

  /*
   * 表明
   */
  private String name;

  private String engine;

  private String version;

  private String rowFormat;

  private long rows;

  private int avgRowLength;

  /*
   * 数据文件长度
   */
  private long dataLength;

  private long maxDataLength = 4294967295L;

  /*
   * 索引文件长度
   */
  private long indexLength;

  private long dataFree;

  private int autoIncrement;

  private Date createTime;

  private Date checkTime;

  private Date updateTime;

  private String collation;

  private String createOptions;

  private String comment;

  public int getAutoIncrement() {
    return autoIncrement;
  }

  public void setAutoIncrement(int autoIncrement) {
    this.autoIncrement = autoIncrement;
  }

  public int getAvgRowLength() {
    return avgRowLength;
  }

  public void setAvgRowLength(int avgRowLength) {
    this.avgRowLength = avgRowLength;
  }

  public Date getCheckTime() {
    return checkTime;
  }

  public void setCheckTime(Date checkTime) {
    this.checkTime = checkTime;
  }

  public String getCollation() {
    return collation;
  }

  public void setCollation(String collation) {
    this.collation = collation;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getCreateOptions() {
    return createOptions;
  }

  public void setCreateOptions(String createOptions) {
    this.createOptions = createOptions;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public long getDataFree() {
    return dataFree;
  }

  public void setDataFree(long dataFree) {
    this.dataFree = dataFree;
  }

  public long getDataLength() {
    return dataLength;
  }

  public void setDataLength(long dataLength) {
    this.dataLength = dataLength;
  }

  public String getEngine() {
    return engine;
  }

  public void setEngine(String engine) {
    this.engine = engine;
  }

  public long getIndexLength() {
    return indexLength;
  }

  public void setIndexLength(long indexLength) {
    this.indexLength = indexLength;
  }

  public long getMaxDataLength() {
    return maxDataLength;
  }

  public void setMaxDataLength(long maxDataLength) {
    this.maxDataLength = maxDataLength;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRowFormat() {
    return rowFormat;
  }

  public void setRowFormat(String rowFormat) {
    this.rowFormat = rowFormat;
  }

  public long getRows() {
    return rows;
  }

  public void setRows(long rows) {
    this.rows = rows;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
