package com.broada.carrier.monitor.impl.db.oracle.process;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.broada.utils.NumberUtil;

/*
 * class for v$process
 */
public class VProcess implements Serializable{
  /**
   * 
   */
  private static final long serialVersionUID = -5434658492816035201L;
  private String name;
  private long pgaUsedMem;
  private long pgaAllocMem;
  private long pgaMaxMem;
  private int count = 0;
  private int threshold = -1;
  private String comprator = "-";
  private String sql = "";
  
  private long cpuUsed;
  
  private int res_id;// 所属资源id
  
  public int getRes_id() {
    return res_id;
  }

  public void setRes_id(int res_id) {
    this.res_id = res_id;
  }

  public long getCpuUsed() {
    return cpuUsed;
  }

  public void setCpuUsed(long cpuUsed) {
    this.cpuUsed = cpuUsed;
  }

  public int getThreshold() {
    if (threshold != -1)
      return threshold;
    return count;
  }

  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  public String getComprator() {
    return comprator;
  }

  public void setComprator(String comprator) {
    this.comprator = comprator;
  }

  public static Map<String, String> descMap = new HashMap<String, String>();
  private static Map<String, String> requiredMap = new LinkedHashMap<String, String>();
  static {
    descMap.put("DBW", "写数据进程");
    descMap.put("LGWR", "写日志进程");
    descMap.put("SMON", "系统监视进程");
    descMap.put("PMON", "进程监视进程");

    descMap.put("ARC", "重作日志归档进程");
    descMap.put("CKPT", "记录检查点进程");
    descMap.put("D", "分派器进程");
    descMap.put("LMS", "锁管理器服务进程");
    descMap.put("QMN", "队列监视器进程");
    descMap.put("RECO", "回收器进程");
    descMap.put("S", "共享服务器进程");// tivoli的文档是S000格式,但是数据库中确实SHAD
    descMap.put("SHAD", "用户服务进程");// tivoli的文档是S000格式,但是数据库中确实SHAD
    descMap.put("SNP", "作业队列进程");
    // 我的添加
    descMap.put("CJQ", "作业调度进程");
    descMap.put("MMAN", "内存管理进程");
    descMap.put("MMNL", "Memory Monitor Light process");
    descMap.put("MMON", "Memory Monitor process");
    descMap.put("QMNC", "Queue Manager Controller");
    // descMap.put("Q003", "Queue Manager Controller");
    // 关键进程
    requiredMap.put("DBW", "DBW");
    requiredMap.put("LGWR", "LGWR");
    requiredMap.put("SMON", "SMON");
    requiredMap.put("PMON", "PMON");
  }

  public VProcess() {
  }

  public static VProcess create(String name) {
    VProcess process = new VProcess();
    process.setName(name);
    return process;
  }

  public String getDesc() {
    return descMap.get(name);
  }

  public int getCount() {
    return count;
  }

  public void addCount() {
    this.count++;
  }

  public boolean isRequired() {
    return requiredMap.containsKey(name);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public void setPgaUsedMem(long pgaUsedMem) {
    this.pgaUsedMem = pgaUsedMem;
  }

  public long getPgaUsedMem() {
    return pgaUsedMem;
  }

  public String getPgaUsedMemText() {
    return convert2MText(pgaUsedMem);
  }

  public void addPgaUsedMem(long pgaUsedMem) {
    this.pgaUsedMem += pgaUsedMem;
  }

  public long getPgaAllocMem() {
    return pgaAllocMem;
  }

  public String getPgaAllocMemText() {
    return convert2MText(pgaAllocMem);
  }

  public void addPgaAllocMem(long pgaAllocMem) {
    this.pgaAllocMem += pgaAllocMem;
  }

  public long getPgaMaxMem() {
    return pgaMaxMem;
  }

  public String getPgaMaxMemText() {
    return convert2MText(pgaMaxMem);
  }

  public void addPgaMaxMem(long pgaMaxMem) {
    this.pgaMaxMem += pgaMaxMem;
  }
  
  public void setSql(String sql) {
    this.sql = sql;
  }
  
  public String getSql(){
    return this.sql;
  }
  
  public void addSql(String sql){
    if(StringUtils.isNotEmpty(sql))
      this.sql += sql +"\n";
  }
  
  public void addCpuUsed(long cpuUsed) {
    this.cpuUsed += cpuUsed;
  }
  
  public String getCpuUsedText() {
    // cpuUsed 单位为 ms
    return NumberUtil.round(getCpuUsed()/1000, 2) + "s";
  }
  
  public static String parseName(String program) {
    if (program == null)
      return null;
    program = program.toUpperCase();
    for (String key : descMap.keySet()) {
      if (program.matches(".*(" + key + "\\d{" + (4 - key.length()) + "," + (4 - key.length()) + "}).*"))
        return key;
    }
    return null;
  }

  static String convert2MText(long num) {
    return NumberUtil.round(convert2M(num), 2) + "M";
  }

  static double convert2M(long num) {
    return 1.0 * num / 1024.0 / 1024.0;
  }

  public void copyTo(VProcess target) {
    target.name = this.name;
    target.count = this.count;
    target.pgaAllocMem = this.pgaAllocMem;
    target.pgaMaxMem = this.pgaMaxMem;
    target.pgaUsedMem = this.pgaUsedMem;
    target.cpuUsed = this.cpuUsed;
    target.sql = this.sql;
  }
}

