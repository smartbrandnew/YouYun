package com.broada.carrier.monitor.impl.db.dm.redolog;

import java.util.LinkedHashMap;

/**
 * DM redo日志监测参数
 * 
 * @author Zhouqa Create By 2016年4月8日 上午9:27:18
 */
public class DmRedoLogParameter {
	public static final String FIELD_UNALLOCS = "unAllocs";
	public static final String FIELD_ARCHCOUNT = "archCount";
	public static final String FIELD_AVESIZE = "aveSize";
	public static final LinkedHashMap<String, String> items = new LinkedHashMap<String, String>();
	static {
		// 初始化顺序跟保存到数据库库性能项的次序一致
		items.put(FIELD_UNALLOCS, "重做日志缓冲中用户进程不能分配空间的次数");
		items.put(FIELD_ARCHCOUNT, "归档重做日志文件的数目");
		items.put(FIELD_AVESIZE, "重做条目的平均大小");

	}

}
