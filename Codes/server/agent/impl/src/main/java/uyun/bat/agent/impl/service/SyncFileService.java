package uyun.bat.agent.impl.service;

import javax.ws.rs.core.Response;

import uyun.bat.agent.impl.autosync.entity.Client;
import uyun.bat.agent.impl.autosync.entity.SyncFileset;
import uyun.bat.agent.impl.autosync.entity.SyncFilesetV2;

public interface SyncFileService {

	public Response outClientFile(String clientId, String fileName);

	public Client outClient(String clientId);

	public String outClientVersion(String clientId);

	public SyncFileset outClientFileset(String clientId);

	public SyncFilesetV2 outClientFilesetV2(String clientId);
}
