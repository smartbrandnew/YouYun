package com.broada.carrier.monitor.common.db;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * <pre>
 * 预备查询
 * 
 * 主要用于生成JPA Query对象
 * 提供了比较方便的API来配对参数，而不需要记忆参数对应位置
 * </pre>
 */
public class PrepareQuery {
	private boolean nativeSql = false;
	private StringBuilder sql;
	private List<Object> params;

	public PrepareQuery() {
		this("");
	}

	/**
	 * <pre>
	 * 使用指定的sql与参数，构建一个查询
	 * 如：new PrepareQuery("select * from table where name = ?1 and age > ?2", "name", 18);
	 * @param sql 参数使用?+序号的方式
	 * @param params
	 */
	public PrepareQuery(String sql, Object...params) {
		this.sql = new StringBuilder(sql);
		for (Object param : params)
			getParams().add(param);
	}

	/**
	 * 获取所有参数
	 * @return
	 */
	private List<Object> getParams() {
		if (params == null)
			params = new ArrayList<Object>(5);
		return params;
	}

	/**
	 * 追加SQL
	 * @param sql
	 * @return
	 */
	public PrepareQuery append(String sql) {
		if (this.sql.length() >= 0)
			this.sql.append(" ");
		this.sql.append(sql);
		return this;
	}
	
	/**
	 * <pre>
	 * 追加SQL，以及其参数
	 * 可以不增加参数占位符，如：append("and name = ", value);
	 * @param sql
	 * @param param
	 * @return
	 */
	public PrepareQuery append(String sql, Object param) {
		if (this.sql.length() >= 0)
			this.sql.append(" ");		
		this.sql.append(sql).append("?").append(getParams().size() + 1);
		getParams().add(param);
		return this;
	}
	
	/**
	 * <pre>
	 * 追加SQL，以及模糊匹配参数
	 * 如：appendLike("and name like", "abc")
	 * @param sql
	 * @param param
	 * @return
	 */
	public PrepareQuery appendLike(String sql, Object param) {
		if (this.sql.length() >= 0)
			this.sql.append(" ");			
		this.sql.append(sql).append("?").append(getParams().size() + 1);
		getParams().add("%" + param + "%");
		return this;
	}	
	
	/**
	 * 生成JPA查询
	 * @param em
	 * @return
	 */
	public Query createQuery(EntityManager em) {
		Query query;
		if (isNativeSql())
			query = em.createNativeQuery(sql.toString());
		else
			query = em.createQuery(sql.toString());
		if (params != null) {
			for (int i = 0; i < getParams().size(); i++)
				query.setParameter(i + 1, getParams().get(i));
		}
		return query;
	}

	/**
	 * 设置是否是一个DB原始查询
	 * @return
	 */
	public boolean isNativeSql() {
		return nativeSql;
	}
	
	public void setNativeSql(boolean nativeSql) {
		this.nativeSql = nativeSql;
	}
}
