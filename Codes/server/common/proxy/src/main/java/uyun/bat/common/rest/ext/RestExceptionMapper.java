package uyun.bat.common.rest.ext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使得前端的报错符合uyun的规定
 */
public class RestExceptionMapper implements ExceptionMapper<Throwable> {
	private static final Logger logger = LoggerFactory.getLogger(RestExceptionMapper.class);
	@Context
	private HttpServletRequest request;

	public Response toResponse(Throwable e) {
		return toUYunErrorResponse(e, request);
	}

	public static Response toUYunErrorResponse(Throwable e, HttpServletRequest request) {
		Response.Status status = null;
		if(e instanceof UnrecognizedPropertyException){
			status = Response.Status.BAD_REQUEST;
			RESTError respContent = new RESTError(status.getStatusCode() + "", "mapper error，there are some superfluous fields："
					+ ((UnrecognizedPropertyException) e).getUnrecognizedPropertyName());
			return Response.status(status).entity(respContent).type(MediaType.APPLICATION_JSON).build();
		}
		if(e instanceof JsonMappingException){
			status = Response.Status.BAD_REQUEST;
			RESTError respContent = new RESTError(status.getStatusCode() + "",
					((JsonMappingException) e).getPath().get(0).getFieldName() + "Type Error");
			return Response.status(status).entity(respContent).type(MediaType.APPLICATION_JSON).build();
		}
		if (e instanceof IllegalArgumentException || e instanceof TimeException) {
			// 参数异常或者是时间超过服务端时间太长
			status = Response.Status.BAD_REQUEST;
		} else {
			StringBuilder sb = new StringBuilder();
			if (request != null) {
				sb.append("URL:");
				sb.append(request.getRequestURI());
				String queryString = request.getQueryString();
				if (queryString != null && queryString.length() > 0) {
					sb.append("?");
					sb.append(request.getQueryString());
				}
				sb.append(",IP:");
				sb.append(getIp(request));
				sb.append(System.getProperty("line.separator", "\n"));
			}
			sb.append(e.getMessage());
			logger.warn(sb.toString());
			if (logger.isDebugEnabled())
				logger.debug("Stack：", e);
			status = Response.Status.INTERNAL_SERVER_ERROR;
		}

		// errcode 暂时用http状态码返回，命名啥的太头疼了
		RESTError respContent = new RESTError(status.getStatusCode() + "", e.getMessage());
		return Response.status(status).entity(respContent).type(MediaType.APPLICATION_JSON).build();
	}

	private static String getIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip != null && ip.length() > 0 && !"unKnown".equalsIgnoreCase(ip)) {
			// 多次反向代理后会有多个ip值，第一个ip才是真实ip
			int index = ip.indexOf(",");
			if (index != -1) {
				return ip.substring(0, index);
			} else {
				return ip;
			}
		}
		ip = request.getHeader("X-Real-IP");
		if (ip != null && ip.length() > 0 && !"unKnown".equalsIgnoreCase(ip)) {
			return ip;
		}
		return request.getRemoteAddr();
	}
}