package uyun.bat.datastore.api.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class DataFormatExceptionTest {

	@Test
	public void testDataFormatException() {
		new DataFormatException();
	}

	@Test
	public void testDataFormatExceptionString() {
		new DataFormatException("");
	}

}
