package com.broada.carrier.monitor.impl.host.cli.directory;

import java.io.Serializable;


public class CLIDirectory implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 监控 */
  private Boolean isWacthed = Boolean.FALSE;

  /** 文件目录路径 */
  private String path = "";

  /** 监控子目录 */
  private Boolean isWacthSubdir = Boolean.FALSE;

  /** 子目录级数 */
  private Integer subdirLayerCount = new Integer(1);

  public Boolean getIsWacthed() {
    return isWacthed;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Boolean getIsWacthSubdir() {
    return isWacthSubdir;
  }

  public void setIsWacthSubdir(Boolean isWacthSubdir) {
    this.isWacthSubdir = isWacthSubdir;
  }

  public Integer getSubdirLayerCount() {
    if (isWacthSubdir.booleanValue()) {
      return subdirLayerCount;
    } else {
      return new Integer(1);
    }
  }

  public void setSubdirLayerCount(Integer subdirLayerCount) {
    this.subdirLayerCount = subdirLayerCount;
  }
}
