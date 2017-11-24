package uyun.bat.agent.impl.dao.typehandler;

import java.nio.charset.Charset;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.TypeHandler;

/**
 * 用户将string转换为blob类型
 * utf-8编码
 * @author WIN
 *
 */
@MappedJdbcTypes(JdbcType.BLOB)
public class StringBlobTypeHandler implements TypeHandler<String> {
	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	@Override
	public void setParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
		//blob类型对应byte[]为null好像插不进去
		if (parameter == null)
			parameter = "";
		byte[] bytes = parameter.getBytes(DEFAULT_CHARSET);
		ps.setBytes(i, bytes);
	}

	@Override
	public String getResult(ResultSet rs, String columnName) throws SQLException {
		byte[] bytes = rs.getBytes(columnName);
		return transBytes2String(bytes);
	}

	@Override
	public String getResult(ResultSet rs, int columnIndex) throws SQLException {
		byte[] bytes = rs.getBytes(columnIndex);
		return transBytes2String(bytes);
	}

	@Override
	public String getResult(CallableStatement cs, int columnIndex) throws SQLException {
		byte[] bytes = cs.getBytes(columnIndex);
		return transBytes2String(bytes);
	}

	private String transBytes2String(byte[] bytes) {
		if(bytes == null )
			return null;
		return new String(bytes, DEFAULT_CHARSET);
	}

}
