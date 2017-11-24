package uyun.bat.agent.impl.autosync.common;

public class ServiceException extends SyncException {
	private static final long serialVersionUID = 1L;
	private String error;

	public ServiceException(String error, String message) {
		super(message);
		this.error = error;
	}

	public String getError() {
		return error;
	}

}
