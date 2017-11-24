package uyun.bat.datastore.api.entity;

import java.io.Serializable;

import uyun.bat.datastore.api.exception.DataFormatException;
import uyun.bat.datastore.api.util.PreConditions;

public class DataPoint implements Serializable {
	private static final long serialVersionUID = 1L;
	private long timestamp;
	private Object value;

	public DataPoint(long timestamp, Object value)
	{
		this.timestamp = timestamp;
		this.value = PreConditions.checkNotNull(value);
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public Object getValue()
	{
		return value;
	}

	public String stringValue() throws DataFormatException
	{
		return value.toString();
	}

	public long longValue() throws DataFormatException
	{
		try
		{
			return ((Number) value).longValue();
		} catch (Exception e)
		{
			throw new DataFormatException("Value is not a long");
		}
	}

	public double doubleValue() throws DataFormatException
	{
		try
		{
			return ((Number) value).doubleValue();
		} catch (Exception e)
		{
			throw new DataFormatException("Value is not a double");
		}
	}

	public boolean isDoubleValue()
	{
		return !(((Number) value).doubleValue() == Math.floor(((Number) value).doubleValue()));
	}

	public boolean isIntegerValue()
	{
		return ((Number) value).doubleValue() == Math.floor(((Number) value).doubleValue());
	}

	@Override
	public String toString()
	{
		return "DataPoint{" +
				"timestamp=" + timestamp +
				", value=" + value +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		DataPoint dataPoint = (DataPoint) o;

		return timestamp == dataPoint.timestamp && value.equals(dataPoint.value);

	}

	@Override
	public int hashCode()
	{
		int result = (int) (timestamp ^ (timestamp >>> 32));
		result = 31 * result + value.hashCode();
		return result;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
