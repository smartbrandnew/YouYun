package uyun.bat.monitor.impl.dao.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import uyun.bat.monitor.impl.util.JsonUtil;

@MappedTypes({ List.class })
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ListStringTypeHandler implements TypeHandler<List<String>> {

	public List<String> getResult(ResultSet rs, String columnName) throws SQLException {
		String columnValue = rs.getString(columnName);
		return this.getListUserId(columnValue);
	}

	public List<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
		String columnValue = rs.getString(columnIndex);
		return this.getListUserId(columnValue);
	}

	public List<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String columnValue = cs.getString(columnIndex);
		return this.getListUserId(columnValue);
	}

	public void setParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
		try {
			ps.setString(i, parameter != null && parameter.size() > 0 ? JsonUtil.encode(parameter) : "[]");
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}

	private List<String> getListUserId(String columnValue) throws SQLException {
		if (columnValue == null || columnValue.length() == 0)
			return null;
		List<String> list = null;
		try {
			list = JsonUtil.getList(columnValue, String.class);
		} catch (Exception e) {
			throw new SQLException(e);
		}

		return list;
	}
}
