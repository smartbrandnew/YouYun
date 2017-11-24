package uyun.bat.dashboard.impl.dao.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import uyun.bat.dashboard.api.entity.LineData;
import uyun.bat.dashboard.impl.util.JsonUtil;

public class ListLineDatasTypeHandler implements TypeHandler<List<LineData>> {

	public List<LineData> getResult(ResultSet rs, String columnName) throws SQLException {
		String columnValue = rs.getString(columnName);
		return this.getListLineData(columnValue);
	}

	public List<LineData> getResult(ResultSet rs, int columnIndex) throws SQLException {
		String columnValue = rs.getString(columnIndex);
		return this.getListLineData(columnValue);
	}

	public List<LineData> getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String columnValue = cs.getString(columnIndex);
		return this.getListLineData(columnValue);
	}

	public void setParameter(PreparedStatement ps, int i, List<LineData> parameter, JdbcType jdbcType)
			throws SQLException {
		if (parameter != null && parameter.size() > 0) {
			try {
				ps.setString(i, JsonUtil.encode(parameter));
			} catch (Exception e) {
				throw new SQLException(e);
			}
		} else {
			ps.setString(i, null);
		}

	}

	private List<LineData> getListLineData(String columnValue) throws SQLException {
		if (columnValue == null || columnValue.length() == 0)
			return null;
		List<LineData> list = null;
		try {
			list = JsonUtil.getList(columnValue, LineData.class);
		} catch (Exception e) {
			throw new SQLException(e);
		}
		return list;
	}
}
