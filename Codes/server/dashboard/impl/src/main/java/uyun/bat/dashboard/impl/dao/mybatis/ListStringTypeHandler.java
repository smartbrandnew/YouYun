package uyun.bat.dashboard.impl.dao.mybatis;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.TypeHandler;

import uyun.bat.dashboard.api.entity.Request;
import uyun.bat.dashboard.impl.util.JsonUtil;

@MappedJdbcTypes(JdbcType.BLOB)
public class ListStringTypeHandler implements TypeHandler<List<Request>> {
	private static final String DEFAULT_CHARSET = "utf-8";

	public List<Request> getResult(ResultSet rs, String columnName) throws SQLException {
		Blob blob = rs.getBlob(columnName);
		if (null == blob)
			return null;
		byte[] returnValue = blob.getBytes(1, (int) blob.length());
		try {
			// ###把byte转化成string
			String columnValue = new String(returnValue, DEFAULT_CHARSET);
			return this.getListRequests(columnValue);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Blob Encoding Error!");
		}
	}

	public List<Request> getResult(ResultSet rs, int columnIndex) throws SQLException {
		Blob blob = rs.getBlob(columnIndex);
		if (null == blob)
			return null;
		byte[] returnValue = blob.getBytes(1, (int) blob.length());
		try {
			// ###把byte转化成string
			String columnValue = new String(returnValue, DEFAULT_CHARSET);
			return this.getListRequests(columnValue);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Blob Encoding Error!");
		}
	}

	public List<Request> getResult(CallableStatement cs, int columnIndex) throws SQLException {
		Blob blob = cs.getBlob(columnIndex);
		if (null == blob)
			return null;
		byte[] returnValue = blob.getBytes(1, (int) blob.length());
		try {
			// ###把byte转化成string
			String columnValue = new String(returnValue, DEFAULT_CHARSET);
			return this.getListRequests(columnValue);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Blob Encoding Error!");
		}
	}

	public void setParameter(PreparedStatement ps, int i, List<Request> parameter, JdbcType jdbcType) throws SQLException {
		try {
			String data = parameter != null && parameter.size() > 0 ? JsonUtil.encode(parameter) : "[]";
			// ###把String转化成byte流
			byte[] bytes = data.getBytes(DEFAULT_CHARSET);
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ps.setBinaryStream(i, bis, bytes.length);
		} catch (Exception e) {
			throw new RuntimeException("Blob Encoding Error!");
		}
	}

	private List<Request> getListRequests(String columnValue) throws SQLException {
		if (columnValue == null || columnValue.length() == 0)
			return null;
		List<Request> list = null;
		try {
			list = JsonUtil.getList(columnValue, Request.class);
		} catch (Exception e) {
			throw new SQLException(e);
		}

		return list;
	}
}
