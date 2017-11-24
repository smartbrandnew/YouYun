package uyun.bat.monitor.impl.dao.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import uyun.bat.monitor.api.entity.MonitorState;

@MappedTypes({ MonitorState.class })
@MappedJdbcTypes(JdbcType.TINYINT)
public class MonitorStateTinyintTypeHandler implements TypeHandler<MonitorState> {

	public MonitorState getResult(ResultSet rs, String columnName) throws SQLException {
		short columnValue = rs.getShort(columnName);
		return this.generateMonitorStatus(columnValue);
	}

	public MonitorState getResult(ResultSet rs, int columnIndex) throws SQLException {
		short columnValue = rs.getShort(columnIndex);
		return this.generateMonitorStatus(columnValue);
	}

	public MonitorState getResult(CallableStatement cs, int columnIndex) throws SQLException {
		short columnValue = cs.getShort(columnIndex);
		return this.generateMonitorStatus(columnValue);
	}

	public void setParameter(PreparedStatement ps, int i, MonitorState parameter, JdbcType jdbcType) throws SQLException {
		try {
			// 没有状态则默认是ok
			if (parameter != null)
				ps.setShort(i, parameter.getValue());
			else
				ps.setShort(i, MonitorState.OK.getValue());

		} catch (Exception e) {
			throw new SQLException(e);
		}
	}

	private MonitorState generateMonitorStatus(short columnValue) throws SQLException {
		MonitorState result = null;
		try {
			result = MonitorState.checkByValue(columnValue);
		} catch (Exception e) {
			throw new SQLException(e);
		}

		return result;
	}
}
