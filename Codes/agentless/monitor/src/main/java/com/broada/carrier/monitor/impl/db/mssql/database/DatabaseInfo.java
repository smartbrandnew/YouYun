package com.broada.carrier.monitor.impl.db.mssql.database;

import org.jdom2.Element;

public class DatabaseInfo {
  public static final String[] alermItem = new String[] { "_MaxSize", "_MaxDataSize", "_MaxIndexSize" };

  private boolean watch;

	private String databaseName;
  
  private float size;
  
  private float maxSize = 500.0f;
  
  private float dataSize;
  
  private float indexSize;
  
  private float maxDataSize = 500.0f;
  
  private float maxIndexSize = 500.0f;
  
  private float unallocatedSize;
  
  private float unused;
  
  private float reserved;
  
  public boolean isWatch() {
		return watch;
	}

	public void setWatch(boolean watch) {
		this.watch = watch;
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
    setMaxDataSize(Float.parseFloat(e.getAttributeValue("maxDataSize", "500.0")));
    setMaxIndexSize(Float.parseFloat(e.getAttributeValue("maxIndexSize", "500.0")));
  }
  public Element toXMLElement(String name) {
    Element cond_e = new Element(name);
    cond_e.setAttribute("databaseName", getDatabaseName());
    cond_e.setAttribute("maxSize", String.valueOf(getMaxSize()));
    cond_e.setAttribute("maxDataSize", String.valueOf(this.getMaxDataSize()));
    cond_e.setAttribute("maxIndexSize", String.valueOf(this.getMaxIndexSize()));
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

  public float getDataSize() {
    return dataSize;
  }

  public void setDataSize(float dataSize) {
    this.dataSize = dataSize;
  }

  public float getIndexSize() {
    return indexSize;
  }

  public void setIndexSize(float indexSize) {
    this.indexSize = indexSize;
  }

  public float getMaxDataSize() {
    return maxDataSize;
  }

  public void setMaxDataSize(float maxDataSize) {
    this.maxDataSize = maxDataSize;
  }

  public float getMaxIndexSize() {
    return maxIndexSize;
  }

  public void setMaxIndexSize(float maxIndexSize) {
    this.maxIndexSize = maxIndexSize;
  }

  public float getUnallocatedSize() {
    return unallocatedSize;
  }

  public void setUnallocatedSize(float unallocatedSize) {
    this.unallocatedSize = unallocatedSize;
  }

  public float getUnused() {
    return unused;
  }

  public void setUnused(float unused) {
    this.unused = unused;
  }

  public float getReserved() {
    return reserved;
  }

  public void setReserved(float reserved) {
    this.reserved = reserved;
  }

  
}
