package com.broada.carrier.monitor.common.entity;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 数据页
 * @author Jiangjw
 * @param <T>
 */
public class Page<T> {
	private boolean more;
	private T[] rows;

	/**
	 * 构建一个空页
	 */
	public Page() {
		this(null, false);
	}

	/**
	 * 使用rows建立一个页，包含所有数据
	 * @param rows
	 */
	public Page(T[] rows) {
		this(rows, false);
	}

	/**
	 * 使用rows建立一个页，并指示是否还有后续数据
	 * @param rows
	 * @param more
	 */
	public Page(T[] rows, boolean more) {
		this.more = more;
		this.rows = rows;
	}

	/**
	 * 从rows中，按pageNo要求建立一个页
	 * @param rows
	 * @param pageNo
	 */
	public Page(T[] rows, PageNo pageNo) {
		if (rows == null) {
			this.rows = null;
			this.more = false;
		} else if (pageNo.isPaged()) {
			int from = pageNo.getFirst();
			if (from >= rows.length) {
				this.rows = Arrays.copyOf(rows, 0);
				this.more = false;
			} else {
				int to = Math.min(rows.length, pageNo.getLast());
				this.rows = Arrays.copyOfRange(rows, from, to);
				this.more = to < rows.length;
			}
		} else {
			this.rows = rows;
			this.more = false;
		}
	}

	/**
	 * 使用指定的List建立一页
	 * @param rows
	 * @param pageNo
	 * @param array 用于表示类型
	 */
	public Page(List<T> rows, PageNo pageNo, T[] array) {
		if (rows == null || rows.isEmpty()) {
			this.rows = null;
			this.more = false;
		} else if (pageNo.isPaged()) {
			int from = pageNo.getFirst();
			if (from >= rows.size()) {
				from = 0;
				int to = Math.min(rows.size(), pageNo.getLast());
				this.rows = rows.subList(from, to).toArray(array);
				this.more = to < rows.size();
			} else {
				int to = Math.min(rows.size(), pageNo.getLast());
				this.rows = rows.subList(from, to).toArray(array);
				this.more = to < rows.size();
			}
		} else {
			this.rows = rows.toArray(array);
			this.more = false;
		}
	}

	/**
	 * 当前页后是否还有更多数据
	 * @return
	 */
	public boolean isMore() {
		return more;
	}

	/**
	 * 当前页数据
	 * @return
	 */
	public T[] getRows() {
		return rows;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append(" size: ").append(rows.length).append(" more: ").append(more);
		for (int i = 0; i < rows.length; i++)
			sb.append("\n").append(i).append(". ").append(rows[i].toString());
		return sb.toString();
	}

	/**
	 * 确定当前页是否有数据
	 * @return
	 */
	@JsonIgnore
	public boolean isEmpty() {
		return rows == null || rows.length == 0;
	}

	/**
	 * 获取行数量
	 * @return
	 */
	@JsonIgnore
	public int getRowsCount() {
		return rows == null ? 0 : rows.length;
	}
}
