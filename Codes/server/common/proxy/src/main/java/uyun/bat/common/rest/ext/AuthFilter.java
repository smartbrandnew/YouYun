package uyun.bat.common.rest.ext;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.constants.RestConstants;
import uyun.bat.common.proxy.ProxyFactory;
import uyun.bird.tenant.api.TenantService;
import uyun.bird.tenant.api.entity.Tenant;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * ContainerRequestFilter-->ClientRequestFilter-->Readerinterceptor-->
 * Writerinterceptor
 * 
 * @Priority(2000)-->@Priority(1000)
 * @Priority(Priorities.HEADER_DECORATOR)
 */
public class AuthFilter implements ContainerRequestFilter {
	private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
	private static TenantService tenantService = ProxyFactory.createProxy(TenantService.class);
	@Context
	private HttpServletRequest request;

	public void filter(ContainerRequestContext requestContext) throws IOException {
		// TODO 权限验证

		String apikey = null != request.getParameter("apikey") ? request.getParameter("apikey") : request
				.getParameter("api_key");

		String tenantId = null;
		if (apikey != null && apikey.length() > 0) {
			try {
				tenantId = cache.get(apikey);
			} catch (Throwable e) {
				// 查询租户id为空
				logger.warn(e.getMessage());
				if (logger.isDebugEnabled()) {
					logger.debug("Stack:", e);
				}
			}
		}

		if (tenantId == null || tenantId.length() == 0) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("gateway receive inaccurate apikey:%s", apikey));
			}
			// TODO 暂缺规范错误
			RESTError respContent = new RESTError(Response.Status.UNAUTHORIZED.getStatusCode() + "",
					Response.Status.UNAUTHORIZED.getReasonPhrase());
			Response response = Response.ok(respContent).status(Response.Status.UNAUTHORIZED)
					.type(MediaType.APPLICATION_JSON).build();
			requestContext.abortWith(response);
		} else {
			request.setAttribute(RestConstants.TENANT_ID, tenantId);
			// request.setAttribute(GatewayConstants.IP, getIp(request));
		}

	}

	/**
	 * 从Request对象中获得客户端IP，处理了HTTP代理服务器和Nginx的反向代理截取了ip
	 * 
	 * @param request
	 * @return ip
	 */
	private static String getIp(HttpServletRequest request) {
		String remoteAddr = request.getRemoteAddr();
		String forwarded = request.getHeader("X-Forwarded-For");
		String realIp = request.getHeader("X-Real-IP");

		String ip = null;
		if (realIp == null) {
			if (forwarded == null) {
				ip = remoteAddr;
			} else {
				ip = forwarded.split(",")[0];
			}
		} else {
			if (realIp.equals(forwarded)) {
				ip = realIp;
			} else {
				if (forwarded != null) {
					forwarded = forwarded.split(",")[0];
				}
				ip = forwarded;
			}
		}
		return ip;
	}

	private static final Object LOCK = new Object();

	private static LoadingCache<String, String> cache;

	static {
		if (cache == null) {
			synchronized (LOCK) {
				if (cache == null) {
					// 先临时默认最大300租户，超时5分钟
					cache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).initialCapacity(50).maximumSize(1000)
							.build(new CacheLoader<String, String>() {
								@Override
								public String load(String key) throws Exception {
									Tenant tenant = tenantService.selectByApikey(key);
									return tenant != null ? tenant.getTenantId() : "";
								}
							});
				}
			}
		}
	}
}
