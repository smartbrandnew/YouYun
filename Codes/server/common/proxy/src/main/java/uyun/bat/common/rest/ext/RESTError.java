package uyun.bat.common.rest.ext;

/**
 * 单次请求错误返回，非批量<br>
 * US-HTTPAPI-V1.2 HTTP API开发规范<br>
 * http://www.uyunsoft.cn/kb/pages/viewpage.action?pageId=7181733
 */
public class RESTError {
	private InnerError error;

	public RESTError(String errCode, String message) {
		super();
		this.error = new InnerError(errCode, message);
	}

	public InnerError getError() {
		return error;
	}

	public void setError(InnerError error) {
		this.error = error;
	}

	public static class InnerError {
		private String code;
		private String message;

		public InnerError(String errCode, String message) {
			super();
			this.code = errCode;
			this.message = message;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}

}
