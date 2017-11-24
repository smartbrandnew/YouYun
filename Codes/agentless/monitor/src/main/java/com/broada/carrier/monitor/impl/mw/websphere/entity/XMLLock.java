package com.broada.carrier.monitor.impl.mw.websphere.entity;

public class XMLLock {
  private static final XMLLock lock = new XMLLock();

  private XMLLock() {
  }

  public static XMLLock getInstance() {
    return lock;
  }
}
