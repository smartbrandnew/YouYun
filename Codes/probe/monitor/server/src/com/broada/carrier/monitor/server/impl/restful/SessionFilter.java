package com.broada.carrier.monitor.server.impl.restful;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import com.broada.carrier.monitor.common.restful.BaseController;
import com.broada.carrier.monitor.common.restful.ErrorResponse;
import com.broada.carrier.monitor.common.util.SerializeUtil;
import com.broada.carrier.monitor.server.impl.logic.SessionManager;
import com.broada.component.utils.error.ErrorUtil;

public class SessionFilter extends OncePerRequestFilter {
	private static final Logger logger = LoggerFactory.getLogger(SessionFilter.class);	
	private static final String URL_SESSION = "/v1/monitor/system/sessions";

	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		boolean hasSession = !request.getRequestURI().contains(URL_SESSION);
				
		if (hasSession) {
			String sessionId = request.getHeader("sessionId");
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("访问：%s 会话ID：%s", request.getRequestURI(), sessionId));
			}
			if (sessionId == null) 
				hasSession = false;
			else {
				try {
					SessionManager.setSessionId(sessionId);
				} catch (Throwable e) {
					writeErrorResponse(e, response);
					return;
				}
			}
		}
		
		try {
			chain.doFilter(request, response);
		} finally {
			if (hasSession)
				SessionManager.reset();
		}		
	}

	private void writeErrorResponse(Throwable error, HttpServletResponse response) {		
		try {			
			ErrorResponse errorResponse = BaseController.processException(error);
			String textResponse = SerializeUtil.encodeJson(errorResponse);
			byte[] dataResponse = textResponse.getBytes("UTF-8");
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setContentLength(dataResponse.length);			
			response.getOutputStream().write(dataResponse);						
			response.getOutputStream().close();
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "转换错误输出失败", e);
		}
	}
}
