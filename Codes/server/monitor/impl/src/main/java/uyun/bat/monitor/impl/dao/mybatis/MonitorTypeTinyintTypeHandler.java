package uyun.bat.monitor.impl.dao.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import uyun.bat.monitor.api.entity.MonitorType;

@MappedTypes({ MonitorType.class })
@MappedJdbcTypes(JdbcType.TINYINT)
public class MonitorTypeTinyintTypeHandler implements TypeHandler<MonitorType> {

	public MonitorType getResult(ResultSet rs, String columnName) throws SQLException {
		short columnValue = rs.getShort(columnName);
		return this.generateMonitorType(columnValue);
	}

	public MonitorType getResult(ResultSet rs, int columnIndex) throws SQLException {
		short columnValue = rs.getShort(columnIndex);
		return this.generateMonitorType(columnValue);
	}

	public MonitorType getResult(CallableStatement cs, int columnIndex) throws SQLException {
		short columnValue = cs.getShort(columnIndex);
		return this.generateMonitorType(columnValue);
	}

	public void setParameter(PreparedStatement ps, int i, MonitorType parameter, JdbcType jdbcType) throws SQLException {
		try {
			if (parameter != null)
				ps.setShort(i, parameter.getValue());
			else
				throw new RuntimeException("The monitor type is empty!");
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}

	private MonitorType generateMonitorType(short columnValue) throws SQLException {
		MonitorType result = null;
		try {
			result = MonitorType.checkByValue(columnValue);
		} catch (Exception e) {
			throw new SQLException(e);
		}

		return result;
	}
}
