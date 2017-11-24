package com.broada.carrier.monitor.impl.db.postgresql;

import java.util.Map;

public interface PostgreSQLService {
  public Map<String, String> getBasicInfo() throws PostgreSQLException;
}
