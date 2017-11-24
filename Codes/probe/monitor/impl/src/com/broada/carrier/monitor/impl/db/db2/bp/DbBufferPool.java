package com.broada.carrier.monitor.impl.db.db2.bp;

/**
 * DB2缓冲池监测实体类
 * @author 杨帆
 * 
 */
public class DbBufferPool {
  int dataLogicReads;

  int dataPhysicsReads;

  int indexLogicReads;

  int indexPhysicsReads;

  double dataRatio;

  double indexRatio;

  int directReads;

  int directWrites;

  double catCacheRatio;

  double pkgCacheRatio;

  public double getCatCacheRatio() {
    return catCacheRatio;
  }

  public void setCatCacheRatio(double catCacheRatio) {
    this.catCacheRatio = catCacheRatio;
  }

  public double getPkgCacheRatio() {
    return pkgCacheRatio;
  }

  public void setPkgCacheRatio(double pkgCacheRatio) {
    this.pkgCacheRatio = pkgCacheRatio;
  }

  public int getDataLogicReads() {
    return dataLogicReads;
  }

  public void setDataLogicReads(int dataLogicReads) {
    this.dataLogicReads = dataLogicReads;
  }

  public int getDataPhysicsReads() {
    return dataPhysicsReads;
  }

  public void setDataPhysicsReads(int dataPhysicsReads) {
    this.dataPhysicsReads = dataPhysicsReads;
  }

  public double getDataRatio() {
    return dataRatio;
  }

  public void setDataRatio(double dataRatio) {
    this.dataRatio = dataRatio;
  }

  public int getDirectReads() {
    return directReads;
  }

  public void setDirectReads(int directReads) {
    this.directReads = directReads;
  }

  public int getDirectWrites() {
    return directWrites;
  }

  public void setDirectWrites(int directWrites) {
    this.directWrites = directWrites;
  }

  public int getIndexLogicReads() {
    return indexLogicReads;
  }

  public void setIndexLogicReads(int indexLogicReads) {
    this.indexLogicReads = indexLogicReads;
  }

  public int getIndexPhysicsReads() {
    return indexPhysicsReads;
  }

  public void setIndexPhysicsReads(int indexPhysicsReads) {
    this.indexPhysicsReads = indexPhysicsReads;
  }

  public double getIndexRatio() {
    return indexRatio;
  }

  public void setIndexRatio(double indexRatio) {
    this.indexRatio = indexRatio;
  }

}
