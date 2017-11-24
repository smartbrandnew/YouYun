package uyun.bat.common.rest.ext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.rest.RpcExceptionMapper;

/**
 * 防止dubbox的报错直接返回前端
 */
public class RPCExceptionMapper extends RpcExceptionMapper {
	@Context
	private HttpServletRequest request;

	public Response toResponse(RpcException e) {
		return RestExceptionMapper.toUYunErrorResponse(e, request);
	}
}