package com.broada.carrier.monitor.impl.mw.websphere.entity.was;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lixy Sep 16, 2008 4:52:51 PM
 */
public class WebSphere {
  private String version;
  private Map<String, Type> types = new HashMap<String, Type>();

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void putType(Type type) {
    types.put(type.getId(), type);
  }

  public Type getType(String typeId) {
    return types.get(typeId);
  }

  public Map<String, Type> getTypes() {
    return types;
  }
}
