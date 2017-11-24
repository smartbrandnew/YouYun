package uyun.bat.agent.impl.autosync.common;

public class ErrorResponse {
	private String error;
	private String message;

	public ErrorResponse() {
		super();
	}

	public ErrorResponse(String error, String message) {
		super();
		this.error = error;
		this.message = message;
	}

	public String getError() {
		return error;
	}

	public String getMessage() {
		return message;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
