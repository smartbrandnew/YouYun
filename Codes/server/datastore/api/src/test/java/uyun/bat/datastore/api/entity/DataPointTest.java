package uyun.bat.datastore.api.entity;

import org.junit.Assert;
import org.junit.Test;

import uyun.bat.datastore.api.exception.DataFormatException;


public class DataPointTest {

	@Test
	public void testGetTimestamp() throws DataFormatException {
		DataPoint point=new DataPoint(System.currentTimeMillis(), 22.3);
		point.getTimestamp();
		point.getValue();
		point.stringValue();
		point.longValue();
		point.doubleValue();
		point.isDoubleValue();
		point.isIntegerValue();
		point.toString();
		point.hashCode();
		Object value = new Object();
		point.equals(value);
		point.setValue(value);
	}
}
