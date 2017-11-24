package com.broada.carrier.monitor.common.swing;

/**
 * 实体对象编辑器接口
 * @author Jiangjw
 */
public interface BeanEditor<T> {
	/**
	 * <pre>
	 * 输出对象
	 * 一般此方法需要做以下几件事：
	 * 1. 将界面上的输入写到bean
	 * 2. 检查bean的数据完整性，如果不完整可弹出异常或返回null
	 * 3. 将bean保存到业务层，如果保存失败可弹出异常或返回null
	 * 注意，上述操作不应对setData中设置的bean进行直接写操作，因为用户有可能会选择取消编辑
	 * @return 如果成功返回对象，失败则返回null
	 */
	T getData();

	/**
	 * <pre>
	 * 输入对象
	 * 一般此方法需要做以下几件事：
	 * 1. 判断bean是否为null，如是则创建一个bean，并提供默认值
	 * 2. 将bean里的数据写到界面上
	 * @param bean 可以为null，一般表示创建新对象
	 */
	void setData(T bean);
}
