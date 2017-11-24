package uyun.bat.datastore.entity;

import java.util.concurrent.ConcurrentHashMap;

public class StringPool {
	private ConcurrentHashMap<String, String> m_stringPool;

	public StringPool()
	{
		m_stringPool = new ConcurrentHashMap<String, String>();
	}

	public String getString(String str)
	{
		String ret = m_stringPool.putIfAbsent(str, str);

		return (ret == null) ? str : ret;
	}
}
