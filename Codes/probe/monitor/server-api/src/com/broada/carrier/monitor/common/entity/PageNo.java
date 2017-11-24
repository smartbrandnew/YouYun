package com.broada.carrier.monitor.common.entity;

/**
 * 分页参数
 * @author Jiangjw
 */
public class PageNo {
	/**
	 * 全部数据页参数
	 */
	public static final PageNo ALL = new PageNo(0, Integer.MAX_VALUE);
	/**
	 * 第1条记录页参数
	 */
	public static final PageNo ONE = new PageNo(0, 1);
	private int first;
	private int size;
		
	/**
	 * 按页号方式建立，但不推荐这种方式，因为他减少了表达能力
	 * @param index
	 * @param size
	 * @return
	 */
	public static PageNo createByIndex(int index, int size) {
		return new PageNo(index * size, size);
	}
	
	/**
	 * 按第几行后指定行数方式建立，推荐这种方式
	 * @param first
	 * @param size
	 * @return
	 */
	public static PageNo createByFirst(int first, int size) {
		return new PageNo(first, size);
	}
	
	/**
	 * 按第几行后指定行数方式建立
	 * @param first
	 * @param size
	 */
	public PageNo(int first, int size) {
		this.first = first;
		this.size = size;		
	}
	
	/**
	 * 当前页首行记录索引号，0表示所有数据的第1行 
	 * @return
	 */
	public int getFirst() {
		return first;
	}

	/**
	 * 每页数据大小
	 * @return
	 */
	public int getSize() {
		return size;
	}

	/**
	 * 判断是否需要进行分布
	 * @return
	 */
	public boolean isPaged() {
		return size != Integer.MAX_VALUE;
	}	
	
	/**
	 * 下一页首行记录索引号
	 * @return
	 */
	public int getLast() {
		return first + size;
	}
	
	/**
	 * 获取页索引号，0表示第1页
	 * @return
	 * @throws IllegalArgumentException 如果当前提供的参数无法提供整齐的页
	 */
	public int getIndex() {
		if (first == 0)
			return 0;
		else if (first % size == 0)
			return first / size;
		else
			throw new IllegalArgumentException(String.format("分页参数[first: %d size: %d]无法切割整齐的页", first, size));
	}
}
