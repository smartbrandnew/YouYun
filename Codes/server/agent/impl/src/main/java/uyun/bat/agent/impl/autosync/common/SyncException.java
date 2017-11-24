package uyun.bat.agent.impl.autosync.common;

public class SyncException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SyncException(String message, Throwable cause) {
		super(message, cause);
	}

	public SyncException(String message) {
		super(message);
	}

}
