package uyun.bat.web.impl.common.service.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.web.api.common.error.RESTError;

/**
 * 使得前端的报错符合uyun的规定
 */
public class RestExceptionMapper implements ExceptionMapper<Throwable> {
	private static final Logger logger = LoggerFactory.getLogger(RestExceptionMapper.class);

	public Response toResponse(Throwable e) {
		return toUYunErrorResponse(e);
	}

	public static Response toUYunErrorResponse(Throwable e) {
		Response.Status status = null;
		if (e instanceof IllegalArgumentException) {
			status = Response.Status.BAD_REQUEST;
		} else {
			if (e instanceof NullPointerException) {
				logger.error("Stack: ", e);
			}
			logger.warn(e.getMessage());
			if (logger.isDebugEnabled())
				logger.debug("Stack：", e);
			status = Response.Status.INTERNAL_SERVER_ERROR;
		}

		// errcode 暂时用http状态码返回，命名啥的太头疼了
		RESTError respContent = new RESTError(status.getStatusCode() + "", e.getMessage());
		return Response.status(status).entity(respContent).type(MediaType.APPLICATION_JSON).build();
	}
}