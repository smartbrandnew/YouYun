package uyun.bat.common.rest.ext;

public class TimeException extends RuntimeException {

	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "非法时间戳。";
	}

}
