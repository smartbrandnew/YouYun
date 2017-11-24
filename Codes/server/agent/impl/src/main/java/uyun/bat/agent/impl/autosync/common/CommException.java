package uyun.bat.agent.impl.autosync.common;

public class CommException extends SyncException {
	private static final long serialVersionUID = 1L;

	public CommException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommException(String message) {
		super(message);
	}

}
