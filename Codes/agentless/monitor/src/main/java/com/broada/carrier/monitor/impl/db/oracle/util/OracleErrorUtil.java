package com.broada.carrier.monitor.impl.db.oracle.util;

import java.sql.SQLException;

import com.broada.carrier.monitor.spi.error.MonitorException;

public class OracleErrorUtil {

	public static MonitorException createError(SQLException e) {
		throw new MonitorException(e.getCause().getCause().getMessage(), e);
	}

}
