package uyun.bat.web.impl.common.service.rest;

import javax.ws.rs.core.Response;

import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.rest.RpcExceptionMapper;

/**
 * 防止dubbox的报错直接返回前端
 */
public class RPCExceptionMapper extends RpcExceptionMapper {

	public Response toResponse(RpcException e) {
		return RestExceptionMapper.toUYunErrorResponse(e);
	}
}