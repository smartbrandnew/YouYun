package com.broada.carrier.monitor.server.api.client.restful;

import java.io.UnsupportedEncodingException;

import com.broada.carrier.monitor.common.remoteio.api.RemoteFile;
import com.broada.carrier.monitor.common.remoteio.api.RemoteIOMode;
import com.broada.carrier.monitor.common.util.Base64Util;
import com.broada.carrier.monitor.server.api.client.restful.entity.OpenFileRequest;
import com.broada.carrier.monitor.server.api.client.restful.entity.SetLastModifiedRequest;
import com.broada.carrier.monitor.server.api.client.restful.entity.WriteFileRequest;
import com.broada.carrier.monitor.server.api.service.BaseFileService;
import com.broada.component.utils.error.ErrorUtil;

public class BaseFileClient extends BaseServiceClient implements BaseFileService {
	public BaseFileClient(String baseServiceUrl, String apiPath) {
		super(baseServiceUrl, apiPath);
	}
	
	public static String encodePath(String path) {
		try {
			return Base64Util.encode(path.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw ErrorUtil.createRuntimeException("编码不存在", e);
		}
	}
	
	public static String decodePath(String data) {
		try {
			byte[] temp = Base64Util.decode(data);
			return new String(temp, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw ErrorUtil.createRuntimeException("编码不存在", e);
		}
	}

	@Override
	public RemoteFile get(String file) {
		return client.post("/file", RemoteFile.class, encodePath(file));
	}

	@Override
	public RemoteFile[] list(String dir) {
		return client.post("/dir", RemoteFile[].class, encodePath(dir));		
	}

	@Override
	public int open(String file, RemoteIOMode mode) {
		return client.post("/0/open", Integer.class, new OpenFileRequest(file, mode));
	}

	@Override
	public void write(int fileId, byte[] data, int offset, int length) {
		client.post("/" + fileId, null, new WriteFileRequest(data, offset, length));
	}

	@Override
	public void close(int fileId) {
		client.post("/" + fileId + "/close");
	}

	@Override
	public byte[] read(int fileId, int len) {
		String data = client.get("/" + fileId, String.class, "len", len);
		return Base64Util.decode(data);
	}

	@Override
	public boolean delete(String file) {
		return client.post("/0/delete", Boolean.class, file);
	}

	@Override
	public boolean setLastModified(String file, long lastModified) {
		return client.post("/0/setLastModified", Boolean.class, new SetLastModifiedRequest(file, lastModified));
	}

}
