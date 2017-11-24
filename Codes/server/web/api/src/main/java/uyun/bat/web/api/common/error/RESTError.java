package uyun.bat.web.api.common.error;

/**
 * 单次请求错误返回，非批量<br>
 * US-HTTPAPI-V1.2 HTTP API开发规范<br>
 * http://www.uyunsoft.cn/kb/pages/viewpage.action?pageId=7181733
 */
public class RESTError {
	private String errCode;
	private String message;

	public RESTError() {
		super();
	}

	public RESTError(String errCode, String message) {
		super();
		this.errCode = errCode;
		this.message = message;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
