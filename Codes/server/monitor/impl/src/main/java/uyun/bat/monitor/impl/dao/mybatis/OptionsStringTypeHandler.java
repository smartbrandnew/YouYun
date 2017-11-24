package uyun.bat.monitor.impl.dao.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import uyun.bat.monitor.api.entity.Options;
import uyun.bat.monitor.impl.util.JsonUtil;

@MappedTypes({ Options.class })
@MappedJdbcTypes(JdbcType.LONGVARCHAR)
public class OptionsStringTypeHandler implements TypeHandler<Options> {

	public Options getResult(ResultSet rs, String columnName) throws SQLException {
		String columnValue = rs.getString(columnName);
		if (columnValue == null)
			return null;

		return this.generateOptions(columnValue);
	}

	public Options getResult(ResultSet rs, int columnIndex) throws SQLException {
		String columnValue = rs.getString(columnIndex);
		if (columnValue == null)
			return null;

		return this.generateOptions(columnValue);
	}

	public Options getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String columnValue = cs.getString(columnIndex);
		if (columnValue == null)
			return null;
		return this.generateOptions(columnValue);
	}

	public void setParameter(PreparedStatement ps, int i, Options parameter, JdbcType jdbcType) throws SQLException {
		try {
			String data = parameter != null ? JsonUtil.encode(parameter) : "{}";

			ps.setString(i, data);
		} catch (Exception e) {
			throw new RuntimeException("Options Encoding Error!");
		}
	}

	private Options generateOptions(String columnValue) throws SQLException {
		if (columnValue == null || columnValue.length() == 0)
			return null;
		Options options = null;
		try {
			options = JsonUtil.decode(columnValue, Options.class);
		} catch (Exception e) {
			throw new SQLException(e);
		}

		return options;
	}
}
