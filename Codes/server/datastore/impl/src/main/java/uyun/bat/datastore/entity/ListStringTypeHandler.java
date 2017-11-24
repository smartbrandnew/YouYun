package uyun.bat.datastore.entity;

import java.nio.charset.Charset;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.TypeHandler;

@MappedJdbcTypes(JdbcType.BLOB)
public class ListStringTypeHandler implements TypeHandler<List<String>> {
	private static String DEFAULT_CHARSET="utf-8";
	@Override
	public void setParameter(PreparedStatement paramPreparedStatement, int paramInt, List<String> paramT,
			JdbcType paramJdbcType) throws SQLException {
		if (paramT != null && paramT.size() > 0) {
			StringBuilder result = new StringBuilder();
			for (String value : paramT) {
				if (value == null || value.trim().length() <= 0 || result.indexOf(value) != -1) {
					continue;
				}
				result.append(value).append(";");
			}
			if (result.length() > 0) {
				result.deleteCharAt(result.length() - 1);
			}
			paramPreparedStatement.setBytes(paramInt, result.toString().getBytes(Charset.forName(DEFAULT_CHARSET)));
		} else {
			paramPreparedStatement.setString(paramInt, null);
		}
	}

	@Override
	public List<String> getResult(ResultSet paramResultSet, String paramString) throws SQLException {
		byte[] bytes=paramResultSet.getBytes(paramString);
		return getListString(bytes);
	}

	@Override
	public List<String> getResult(ResultSet paramResultSet, int paramInt) throws SQLException {
		byte[] values = paramResultSet.getBytes(paramInt);
		return getListString(values);
	}

	@Override
	public List<String> getResult(CallableStatement paramCallableStatement, int paramInt) throws SQLException {
		byte[] values = paramCallableStatement.getBytes(paramInt);
		return getListString(values);
	}

	private List<String> getListString(byte[] bytes) {
		if (bytes == null)
			return null;
		String value = new String(bytes, Charset.forName(DEFAULT_CHARSET));
		String[] values = value.split(";");
		Set<String> set = new HashSet<String>();
		for (String val : values) {
			set.add(val);
		}
		return new ArrayList<String>(set);
	}
}
