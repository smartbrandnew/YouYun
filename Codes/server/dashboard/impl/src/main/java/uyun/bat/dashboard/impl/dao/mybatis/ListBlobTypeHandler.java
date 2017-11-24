package uyun.bat.dashboard.impl.dao.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.TypeHandler;

@MappedJdbcTypes(JdbcType.BLOB)
public class ListBlobTypeHandler implements TypeHandler<List<String>> {

	public List<String> getResult(ResultSet rs, String columnName) throws SQLException {
		String columnValue = rs.getString(columnName);
		return this.getListBlob(columnValue);
	}

	public List<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
		String columnValue = rs.getString(columnIndex);
		return this.getListBlob(columnValue);
	}

	public List<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String columnValue = cs.getString(columnIndex);
		return this.getListBlob(columnValue);
	}

	public void setParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
		if (parameter != null && parameter.size() > 0) {
			StringBuilder result = new StringBuilder();
			for (String value : parameter)
				result.append(value).append(",");
			result.deleteCharAt(result.length() - 1);
			ps.setString(i, result.toString());
		} else {
			ps.setString(i, null);
		}

	}

	private List<String> getListBlob(String columnValue) {
		if (columnValue == null)
			return null;
		List<String> list = new ArrayList<String>();
		String[] values = columnValue.split(",");
		for (String s : values) {
			list.add(s);
		}
		return list;
	}
}
