package uyun.bat.datastore.entity;

import java.nio.ByteBuffer;
import java.util.SortedMap;
import java.util.TreeMap;

public class DataPointsRowKey {
	private final String m_metricName;
	private final long m_timestamp;
	private final String m_dataType;
	private final SortedMap<String, String> m_tags;
	private boolean m_endSearchKey;

	private ByteBuffer m_serializedBuffer;

	public DataPointsRowKey(String metricName, long timestamp, String dataType)
	{
		this(metricName, timestamp, dataType, new TreeMap<String, String>());
	}

	public DataPointsRowKey(String metricName, long timestamp, String datatype,
			SortedMap<String, String> tags)
	{
		m_metricName = metricName;
		m_timestamp = timestamp;
		m_dataType = datatype;
		m_tags = tags;

	}

	public void addTag(String name, String value)
	{
		m_tags.put(name, value);
	}

	public String getMetricName()
	{
		return m_metricName;
	}

	public SortedMap<String, String> getTags()
	{
		return m_tags;
	}

	public long getTimestamp()
	{
		return m_timestamp;
	}

	public boolean isEndSearchKey()
	{
		return m_endSearchKey;
	}

	public void setEndSearchKey(boolean endSearchKey)
	{
		m_endSearchKey = endSearchKey;
	}

	public String getDataType()
	{
		return m_dataType;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		DataPointsRowKey that = (DataPointsRowKey) o;

		if (m_timestamp != that.m_timestamp)
			return false;
		if (m_dataType != null ? !m_dataType.equals(that.m_dataType) : that.m_dataType != null)
			return false;
		if (!m_metricName.equals(that.m_metricName))
			return false;
		if (!m_tags.equals(that.m_tags))
			return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = m_metricName.hashCode();
		result = 31 * result + (int) (m_timestamp ^ (m_timestamp >>> 32));
		result = 31 * result + (m_dataType != null ? m_dataType.hashCode() : 0);
		result = 31 * result + m_tags.hashCode();
		return result;
	}

	@Override
	public String toString()
	{
		return "DataPointsRowKey{" +
				"m_metricName='" + m_metricName + '\'' +
				", m_timestamp=" + m_timestamp +
				", m_dataType='" + m_dataType + '\'' +
				", m_tags=" + m_tags +
				'}';
	}

	public ByteBuffer getSerializedBuffer()
	{
		return m_serializedBuffer;
	}

	public void setSerializedBuffer(ByteBuffer serializedBuffer)
	{
		m_serializedBuffer = serializedBuffer;
	}
}
