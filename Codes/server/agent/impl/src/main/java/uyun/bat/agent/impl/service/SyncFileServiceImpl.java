package uyun.bat.agent.impl.service;

import java.io.File;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import uyun.bat.agent.impl.autosync.common.HttpUtil;
import uyun.bat.agent.impl.autosync.entity.Client;
import uyun.bat.agent.impl.autosync.entity.SyncFileset;
import uyun.bat.agent.impl.autosync.entity.SyncFilesetV2;
import uyun.bat.agent.impl.autosync.service.AutoSyncServer;
import uyun.bat.agent.impl.autosync.service.SyncServiceFactory;

import com.alibaba.dubbo.config.annotation.Service;

/**
 * <pre>
 * 
 * 支持以下几个查询：
 * 1. /client/{clientId}
 * 2. /client/{clientId}/version
 * 3. /client/{clientId}/file
 * 4. /client/{clientId}/file/{fileName}
 * </pre>
 */

@Service(protocol = "rest-sync")
@Path("v2/autosync")
public class SyncFileServiceImpl implements SyncFileService {

	private static AutoSyncServer syncService;
	static {
		syncService = SyncServiceFactory.getDefault();
		syncService.startup();
	}

	@GET
	@Path("/client/{clientId}/file/{fileName}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response outClientFile(@PathParam("clientId") String clientId, @PathParam("fileName") String fileName) {
		String filename = HttpUtil.decodeUrl(fileName);
		if (filename == null)
			throw new IllegalArgumentException("illegal fileName：" + fileName);
		File file = syncService.getClientFile(clientId, filename);
		String name = file.getName();
		try {
			name = new String(file.getName().getBytes("utf-8"), "ISO8859-1");
		} catch (UnsupportedEncodingException e) {
			// 转码失败，暂时不处理
		}
		ResponseBuilder response = Response.ok((Object) file);
		response.header("Content-disposition", "attachment; filename=" + name);
		response.header("Content-Length", String.valueOf(file.length()));
		return response.build();
	}

	@GET
	@Path("/client/{clientId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Client outClient(@PathParam("clientId") String clientId) {
		Client client = syncService.getClient(clientId);
		if (client == null)
			throw new RuntimeException("Client synchronization is not enabled yet：" + clientId);
		else
			return client;
	}

	@GET
	@Path("/client/{clientId}/version")
	public String outClientVersion(@PathParam("clientId") String clientId) {
		return syncService.getClientFileset(clientId).getVersion();
	}

	@GET
	@Path("/client/{clientId}/file")
	@Produces(MediaType.APPLICATION_JSON)
	public SyncFileset outClientFileset(@PathParam("clientId") String clientId) {
		return syncService.getClientFileset(clientId);
	}

	@GET
	@Path("/client/{clientId}/file-v2")
	@Produces(MediaType.APPLICATION_JSON)
	public SyncFilesetV2 outClientFilesetV2(@PathParam("clientId") String clientId) {
		return syncService.getClientFilesetV2(clientId);
	}
}
