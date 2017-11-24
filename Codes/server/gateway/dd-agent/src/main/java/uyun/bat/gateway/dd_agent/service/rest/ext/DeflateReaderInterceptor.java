package uyun.bat.gateway.dd_agent.service.rest.ext;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

/**
 * 数据解压缩
 */
public class DeflateReaderInterceptor implements ReaderInterceptor {

	public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
		String contentEnconding = context.getHeaders().getFirst("Content-Encoding");
		if ("deflate".equals(contentEnconding)) {
			InputStream old = context.getInputStream();
			context.setInputStream(new InflaterInputStream(old));
			Object entity = context.proceed();
			context.setInputStream(old);
			return entity;
		} else {
			return context.proceed();
		}
	}

}
