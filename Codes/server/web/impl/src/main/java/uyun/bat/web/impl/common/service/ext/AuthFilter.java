package uyun.bat.web.impl.common.service.ext;

import uyun.bat.web.api.common.error.RESTError;
import uyun.bat.web.impl.common.entity.TenantConstants;
import uyun.bat.web.impl.common.service.ServiceManager;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ContainerRequestFilter-->ClientRequestFilter-->Readerinterceptor-->
 * Writerinterceptor
 * 
 * @Priority(2000)-->@Priority(1000)
 * @Priority(Priorities.HEADER_DECORATOR)
 */
@Priority(1000)
public class AuthFilter implements ContainerRequestFilter {

	public void filter(ContainerRequestContext requestContext) throws IOException {
		// TODO 权限验证
		boolean unAuthorized = true;
		Map<String, Cookie> cookieMap = requestContext.getCookies();
		if (cookieMap != null) {
			Cookie token = cookieMap.get(TenantConstants.COOKIE_TOKEN);
			if (token != null) {
				Map<String, Object> userInfo = ServiceManager.getInstance().getUserService().verify(token.getValue());
				//测试使用
				/*Map<String, Object> userInfo = new HashMap<String,Object>();
				userInfo.put("tenantId", "ba1c5f3233384ce5abda4b61d95d39f4");
				userInfo.put("userId", "ba1c5f3233384ce5abda4b61d95d39f4");*/
				if (userInfo != null && userInfo.size() > 0){
					unAuthorized = false;
					requestContext.getHeaders().add(TenantConstants.COOKIE_TENANT_ID, (String)userInfo.get("tenantId"));
					requestContext.getHeaders().add(TenantConstants.COOKIE_USERID, (String)userInfo.get("userId"));
				}
			}
			
		}
		//测试使用
		//unAuthorized=false;
		if (unAuthorized) {
			// TODO 暂缺规范错误
			RESTError respContent = new RESTError(Response.Status.UNAUTHORIZED.getStatusCode() + "",
					Response.Status.UNAUTHORIZED.getReasonPhrase());
			Response response = Response.ok(respContent).status(Response.Status.UNAUTHORIZED)
					.type(MediaType.APPLICATION_JSON).build();
			requestContext.abortWith(response);
		}
	}
}
