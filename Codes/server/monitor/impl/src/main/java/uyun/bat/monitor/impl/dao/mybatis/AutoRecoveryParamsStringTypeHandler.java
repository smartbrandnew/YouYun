package uyun.bat.monitor.impl.dao.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import uyun.bat.monitor.api.entity.AutoRecoveryParams;
import uyun.bat.monitor.impl.util.JsonUtil;

@MappedTypes({ AutoRecoveryParams.class })
@MappedJdbcTypes(JdbcType.LONGVARCHAR)
public class AutoRecoveryParamsStringTypeHandler implements TypeHandler<AutoRecoveryParams> {

	public AutoRecoveryParams getResult(ResultSet rs, String columnName) throws SQLException {
		String columnValue = rs.getString(columnName);
		if (columnValue == null)
			return null;

		return this.generateAutoRecoveryParams(columnValue);
	}

	public AutoRecoveryParams getResult(ResultSet rs, int columnIndex) throws SQLException {
		String columnValue = rs.getString(columnIndex);
		if (columnValue == null)
			return null;

		return this.generateAutoRecoveryParams(columnValue);
	}

	public AutoRecoveryParams getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String columnValue = cs.getString(columnIndex);
		if (columnValue == null)
			return null;
		return this.generateAutoRecoveryParams(columnValue);
	}

	public void setParameter(PreparedStatement ps, int i, AutoRecoveryParams parameter, JdbcType jdbcType) throws SQLException {
		try {
			String data = parameter != null ? JsonUtil.encode(parameter) : "{}";

			ps.setString(i, data);
		} catch (Exception e) {
			throw new RuntimeException("AutoRecoveryParams Encoding Error!");
		}
	}

	private AutoRecoveryParams generateAutoRecoveryParams(String columnValue) throws SQLException {
		if (columnValue == null || columnValue.length() == 0)
			return null;
		AutoRecoveryParams autoRecoveryParams = null;
		try {
			autoRecoveryParams = JsonUtil.decode(columnValue, AutoRecoveryParams.class);
		} catch (Exception e) {
			throw new SQLException(e);
		}

		return autoRecoveryParams;
	}
}
