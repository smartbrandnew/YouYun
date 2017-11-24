package com.broada.carrier.monitor.impl.db.dm.dtfile;

import com.broada.utils.StringUtil;

/**
 * Dm数据文件模型
 * 
 * @author Zhouqa
 * Create By 2016年4月7日 下午3:39:28
 */
public class DmDtfileInfo {
	private String dfName;                     //文件名称
  private String dfStatus;                   //文件状态
  private String dfModifyTime;    //文件修改时间
  private Double dfTotalSize = new Double(0);     //文件大小
  private Double dfFreeSize = new Double(0);     //文件剩余大小
  private Double maxDfSize = new Double(2048);  //文件大小阈值
  private Double dfRTS = new Double(0);      //文件请求读次数
  private Double dfWRTS = new Double(0);     //文件请求写次数
  private Boolean isWacthed = Boolean.FALSE;
	public String getDfName() {
		return dfName;
	}
	public void setDfName(String dfName) {
		this.dfName = dfName;
	}
	public String getDfStatus() {
		return dfStatus;
	}
	public void setDfStatus(String dfStatus) {
		this.dfStatus = dfStatus;
	}
	public Double getDfTotalSize() {
		return dfTotalSize;
	}
	public void setDfTotalSize(Double dfTotalSize) {
		this.dfTotalSize = dfTotalSize;
	}
	public Double getDfFreeSize() {
		return dfFreeSize;
	}
	public void setDfFreeSize(Double dfFreeSize) {
		this.dfFreeSize = dfFreeSize;
	}
	public Double getMaxDfSize() {
		return maxDfSize;
	}
	public void setMaxDfSize(Double maxDfSize) {
		this.maxDfSize = maxDfSize;
	}
	public Double getDfRTS() {
		return dfRTS;
	}
	public void setDfRTS(Double dfRTS) {
		this.dfRTS = dfRTS;
	}
	public Double getDfWRTS() {
		return dfWRTS;
	}
	public void setDfWRTS(Double dfWRTS) {
		this.dfWRTS = dfWRTS;
	}
	public String getDfModifyTime() {
		return dfModifyTime;
	}
	public void setDfModifyTime(String dfModifyTime) {
		this.dfModifyTime = dfModifyTime;
	}
	public Boolean getIsWacthed() {
		return isWacthed;
	}
	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}
  
  public String getDesc() {
    StringBuffer sb = new StringBuffer("");
    if (!StringUtil.isNullOrBlank(dfName)) {
      sb.append(dfName).append(";");
    }
    if (!StringUtil.isNullOrBlank(dfStatus)) {
      sb.append(dfStatus).append(";");
    }
    sb.append(dfTotalSize).append(";");
    sb.append(dfFreeSize).append(";");
    sb.append(dfRTS).append(";");
    sb.append(dfWRTS).append(";");
    sb.append(dfModifyTime).append(";");
    return sb.toString();
  }
}
