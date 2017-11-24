package uyun.bat.datastore.api.exception;


import org.junit.Test;

public class DataAccessExceptionTest {

	@Test
	public void testDataAccessException() {
		new DataAccessException();
	}

	@Test
	public void testDataAccessExceptionString() {
		new DataAccessException("");
	}

	@Test
	public void testDataAccessExceptionStringThrowable() {
		new DataAccessException("", new Exception());
	}

}