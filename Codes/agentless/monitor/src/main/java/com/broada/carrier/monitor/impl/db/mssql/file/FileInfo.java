package com.broada.carrier.monitor.impl.db.mssql.file;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jdom2.Element;

public class FileInfo {
  public static final String FILE_SEPARATOR = "->";
  
  private boolean watch;
  private String databaseName;
  private String groupName;
  private String name;
  private float size;//MB
  private String maxCapability;//MB
  private String growth;//example:100MB or 10%
  private String fileName;
  private float maxSize = 500; //MB
  
  public boolean isWatch() {
		return watch;
	}

	public void setWatch(boolean watch) {
		this.watch = watch;
	}
	
  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMaxCapability() {
    return maxCapability;
  }

  public void setMaxCapability(String maxCapability) {
    this.maxCapability = maxCapability;
  }

  public String getGrowth() {
    return growth;
  }

  public void setGrowth(String growth) {
    this.growth = growth;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  public void setValues(Element e){
    setDatabaseName(e.getAttributeValue("databaseName"));
    setMaxSize(Float.parseFloat(e.getAttributeValue("maxSize", "500.0")));
    setName(e.getAttributeValue("name"));
  }
  public Element toXMLElement(String name) {
    Element cond_e = new Element(name);
    cond_e.setAttribute("databaseName", getDatabaseName());
    cond_e.setAttribute("name", this.getName());
    cond_e.setAttribute("maxSize", String.valueOf(getMaxSize()));
    return cond_e;
  }

  public float getSize() {
    return size;
  }

  public void setSize(float size) {
    this.size = size;
  }

  public float getMaxSize() {
    return maxSize;
  }

  public void setMaxSize(float maxSize) {
    this.maxSize = maxSize;
  }

  public int hashCode() {
    return new HashCodeBuilder().append(databaseName).append(databaseName).append(name).toHashCode();
  }
}
