package uyun.bat.web.impl.common.service.rest;


import static org.junit.Assert.*;

import javax.validation.constraints.AssertTrue;

import org.junit.Test;

import com.alibaba.dubbo.rpc.RpcException;

public class ExceptionMapperTest {

	@Test
	public void testResponse() {
		Throwable e = new Throwable();
		
		RestExceptionMapper r = new RestExceptionMapper();
		
		RPCExceptionMapper rpec = new RPCExceptionMapper();
		RpcException re = new RpcException();
		
		assertTrue(rpec.toResponse(re) != null && r.toResponse(e)!= null 
				&& RestExceptionMapper.toUYunErrorResponse(e) != null);
	}

}
