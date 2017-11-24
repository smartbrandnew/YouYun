package uyun.bat.datastore.dao.mybatis;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import uyun.bat.datastore.entity.Binary;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes({Binary.class})
@MappedJdbcTypes({JdbcType.BINARY})
public class BinaryTypeHandler implements TypeHandler<Binary> {
	@Override
	public void setParameter(PreparedStatement preparedStatement, int i, Binary s, JdbcType jdbcType) throws SQLException {
		preparedStatement.setBytes(i, s == null ? null : s.getValue());
	}

	@Override
	public Binary getResult(ResultSet resultSet, String s) throws SQLException {
		return box(resultSet.getBytes(s));
	}

	private Binary box(byte[] bytes) {
		if (bytes == null)
			return null;
		else
			return new Binary(bytes);
	}

	@Override
	public Binary getResult(ResultSet resultSet, int i) throws SQLException {
		return box(resultSet.getBytes(i));
	}

	@Override
	public Binary getResult(CallableStatement callableStatement, int i) throws SQLException {
		return box(callableStatement.getBytes(i));
	}
}
