package com.broada.carrier.monitor.impl.db.db2;

import java.sql.SQLException;

public class DB2LoginException extends SQLException {
	private static final long serialVersionUID = 1L;

	public DB2LoginException() {
    super();
  }

  public DB2LoginException(String reason) {
    super(reason);
  }

  public DB2LoginException(String reason, String SQLState) {
    super(reason, SQLState);
  }

  public DB2LoginException(String reason, String SQLState, int vendorCode) {
    super(reason, SQLState, vendorCode);
  }

}
