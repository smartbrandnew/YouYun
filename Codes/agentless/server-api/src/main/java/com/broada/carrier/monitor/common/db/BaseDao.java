package com.broada.carrier.monitor.common.db;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;

/**
 * 基础Dao类，提供基于JPA的Dao工具API
 */
public class BaseDao {
	@PersistenceContext 
	private EntityManager em;  
	
	/**
	 * 新建实体
	 * @param data
	 */
	public void create(Object data) {
		em.persist(data);
	}
	
	/**
	 * 保存实体，如果实体已经存在，则更新，否则为插入
	 * 与insert的区别为：
	 * 1. insert只处理插入
	 * 2. insert效率更高
	 * 3. insert可以生成自动生成id，而save则不可以
	 * @param data
	 */
	public void save(Object data) {
		em.merge(data);
	}

	/**
	 * 删除实体
	 * @param cls
	 * @param id
	 */
	public void delete(Class<?> cls, Object id) {		
		em.remove(em.getReference(cls, id));	
	}

	/**
	 * 使用key获取指定实体
	 * @param cls
	 * @param id
	 * @return
	 */
	public <T> T get(Class<T> cls, Object id) {
		return em.find(cls, id);
	}
	
	/**
	 * 执行一个命名查询并返回列表
	 * @param queryName
	 * @param params
	 * @return
	 */
	public List<?> queryNamedForList(String queryName, Object...params) {
		Query query = createNamedQuery(queryName, params);
		return query.getResultList();		
	}
	
	/**
	 * 使用指定参数建立一个命名查询
	 * @param queryName
	 * @param params
	 * @return
	 */
	public Query createNamedQuery(String queryName, Object...params) {
		Query query;
		query = em.createNamedQuery(queryName);
		for (int i = 0; i < params.length; i++)
			query.setParameter(i + 1, params[i]);		
		return query;
	}

	/**
	 * 执行一个命名查询并返回对象
	 * @param queryName
	 * @param params
	 * @return
	 */
	public Object queryNamedForObject(String queryName, Object...params) {
		Query query = createNamedQuery(queryName, params);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * 执行一个命名查询并返回影响的记录数
	 * @param queryName
	 * @param params
	 * @return
	 */
	public int executeNamed(String queryName, Object...params) {
		Query query = createNamedQuery(queryName, params);
		return query.executeUpdate();
	}
	
	/**
	 * 执行查询SQL并获取指定页的数据
	 * @param sql
	 * @param page
	 * @param array
	 * @return
	 */
	public <T> Page<T> queryForPage(String sql, PageNo page, T[] array) {
		Query query = em.createQuery(sql);
		return queryForPage(query, page, array);
	}
	
	/**
	 * {@link #queryForPage(String, PageNo, Object[])}
	 * @param query
	 * @param page
	 * @param array
	 * @return
	 */
	public <T> Page<T> queryForPage(PrepareQuery query, PageNo page, T[] array) {		
		return queryForPage(query.createQuery(em), page, array);
	}
	
	/**
	 * 执行一个查询，并返回一个值。SQL应该只返回单行单列
	 * @param query
	 * @return
	 */
	public Object queryForObject(PrepareQuery query) {
		return query.createQuery(em).getSingleResult();
	}
	
	/**
	 * 执行一个查询，并返回一个值。SQL应该只返回单行单列
	 * @param sql
	 * @return
	 */
	public Object queryForObject(String sql) {
		return queryForObject(new PrepareQuery(sql));
	}
	
	/**
	 * 查询所有匹配的记录，并以数组形式返回
	 * @param query
	 * @param array
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] queryForArray(PrepareQuery query, T[] array) {
		return (T[]) query.createQuery(em).getResultList().toArray(array);
	}
	
	/**
	 * {@link #queryForPage(String, PageNo, Object[])}
	 * @param query
	 * @param page
	 * @param array
	 * @return
	 */
	public <T> Page<T> queryForPage(Query query, PageNo page, T[] array) {
		if (page.isPaged()) {
			int first = page.getIndex() * page.getSize();
			query.setFirstResult(first);
			query.setMaxResults(page.getSize() + 1);			
			List<?> result = query.getResultList();
			boolean hasNext = result.size() > page.getSize();
			if (hasNext)
				result.remove(result.size() - 1);			
			return new Page<T>(result.toArray(array), hasNext);
		} else {					
			List<?> result = query.getResultList();
			return new Page<T>(result.toArray(array), false);
		}
	}

	/**
	 * 执行一个查询，并每一行包装为一个JavaBean，返回为一个数组
	 * @param sql
	 * @param array
	 * @return
	 */
	public <T> T[] queryForArray(String sql, T[] array) {
		return queryForArray(new PrepareQuery(sql), array);
	}

	/**
	 * 执行一个查询，返回影响的行数
	 * @param sql
	 * @param params
	 * @return
	 */
	public int execute(String sql, Object...params) {
		return execute(new PrepareQuery(sql, params));
	}

	/**
	 * 执行一个查询，返回影响的行数
	 * @param query
	 * @return
	 */
	public int execute(PrepareQuery query) {
		return query.createQuery(em).executeUpdate();
	}

	/**
	 * 执行一个查询，并将每一行包装为一个JavaBean，返回一个Collection
	 * @param query
	 * @return
	 */
	public Collection<?> queryForList(PrepareQuery query) {
		return query.createQuery(em).getResultList();
	}
}
