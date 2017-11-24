package uyun.bat.datastore.api.exception;

public class DataAccessException extends Exception {

	private static final long serialVersionUID = 1L;

	public DataAccessException() {

	}

	public DataAccessException(String s) {
		super(s);
	}

	public DataAccessException(String s, Throwable e) {
		super(s, e);
	}
}
