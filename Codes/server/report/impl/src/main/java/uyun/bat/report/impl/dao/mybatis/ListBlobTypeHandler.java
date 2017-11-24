package uyun.bat.report.impl.dao.mybatis;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.TypeHandler;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@MappedJdbcTypes(JdbcType.BLOB)
public class ListBlobTypeHandler implements TypeHandler<List<String>> {

	private byte[] bytes;

	public List<String> getResult(ResultSet rs, String columnName) throws SQLException {
		Blob columnValue = rs.getBlob(columnName);
		return this.getListBlob(columnValue);
	}

	public List<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
		Blob columnValue = rs.getBlob(columnIndex);
		return this.getListBlob(columnValue);
	}

	public List<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
		Blob columnValue = cs.getBlob(columnIndex);
		return this.getListBlob(columnValue);
	}

	public void setParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
		if (parameter != null && parameter.size() > 0) {
			StringBuilder result = new StringBuilder();
			for (String value : parameter)
				result.append(value).append(";");
			result.deleteCharAt(result.length() - 1);
			ps.setString(i, result.toString());
		} else {
			ps.setString(i, null);
		}

	}

	private List<String> getListBlob(Blob blob) {
		if (blob == null)
			return null;
		List<String> list = new ArrayList<String>();
		String columnValue = "";
		try {
			byte[] returnValue = blob.getBytes(1, (int) blob.length());
			columnValue = new String(returnValue, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String[] values = columnValue.split(";");
		for (String s : values) {
			try {
				byte[] bytes =  s.getBytes("UTF-8");
				String str = new String(bytes, "UTF-8");
				list.add(str);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
}
