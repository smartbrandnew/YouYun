package uyun.bat.agent.impl.autosync.service;

import static org.junit.Assert.*;

import org.junit.Test;

import uyun.bat.agent.impl.autosync.common.SyncServerConfig;
import uyun.bat.agent.impl.autosync.entity.AutoSync;
import uyun.bat.agent.impl.autosync.entity.Client;

public class SyncServerImplTest {

	private static String CLIENT_ID = "windows";
	private static String TEST_NAME = "testName";
	@Test
	public void testSyncServerImpl() {
		SyncServerConfig config = SyncServerConfig.getDefault();
		SyncServerImpl syncServerImpl = new SyncServerImpl(config);
		AutoSync as = new AutoSync();
		Client client = new Client();
		client.setId(CLIENT_ID);
		Client[] clients = {client};
		as.setClient(clients);
		syncServerImpl.startup();
		syncServerImpl.getClientFileset(CLIENT_ID);
		syncServerImpl.getClient(CLIENT_ID);
		syncServerImpl.getClientFilesetV2(CLIENT_ID);
		syncServerImpl.shutdown();
	}

}
