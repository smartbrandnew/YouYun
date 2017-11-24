package uyun.bat.web.impl.common.service.ext;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import uyun.bat.web.impl.testservice.StartService;

public class AuthFilterTest extends StartService{

	@Test
	public void testAuthFilter() throws IOException {
		AuthFilter authFilter = new AuthFilter();
		ContainerRequestContext requestContext = new ContainerRequestContext() {
			
			@Override
			public void setSecurityContext(SecurityContext context) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setRequestUri(URI baseUri, URI requestUri) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setRequestUri(URI requestUri) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setProperty(String name, Object object) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setMethod(String method) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setEntityStream(InputStream input) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void removeProperty(String name) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean hasEntity() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public UriInfo getUriInfo() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public SecurityContext getSecurityContext() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Request getRequest() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Collection<String> getPropertyNames() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object getProperty(String name) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getMethod() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MediaType getMediaType() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getLength() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Locale getLanguage() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MultivaluedMap<String, String> getHeaders() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getHeaderString(String name) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public InputStream getEntityStream() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Date getDate() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Map<String, Cookie> getCookies() {
				Map<String,Cookie>map = new HashMap<>();
				Cookie cookie = new Cookie("test", "value");
				map.put("test", cookie);
				return map;
			}
			
			@Override
			public List<MediaType> getAcceptableMediaTypes() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public List<Locale> getAcceptableLanguages() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void abortWith(Response response) {
				// TODO Auto-generated method stub
				
			}
		};
		authFilter.filter(requestContext);
	}
	

}
