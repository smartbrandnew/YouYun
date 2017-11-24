package uyun.bat.agent.impl.autosync.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uyun.bat.agent.impl.autosync.common.FileChangedListener;
import uyun.bat.agent.impl.autosync.common.FileMonitor;
import uyun.bat.agent.impl.autosync.common.SyncServerConfig;
import uyun.bat.agent.impl.autosync.entity.*;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自动同步服务实现
 * @author Jiangjw
 */
public class SyncServerImpl implements AutoSyncServer {
	private static final Log logger = LogFactory.getLog(SyncServerImpl.class);
	private Client[] clients;
	private Map<String, SyncFilesetV2> clientsFileSet;
	private FileMonitor fileMonitor;
	private SyncServerConfig config;
	
	public SyncServerImpl() {
		this(SyncServerConfig.getDefault());		
	}
	
	public SyncServerImpl(SyncServerConfig config) {
		this.config = config;
	}
	
	private Client checkClient(String clientId) {
		for (Client client : clients) {
			if (client.getId().equalsIgnoreCase(clientId))
				return client;
		}
		throw new IllegalArgumentException("Client does not exist：" + clientId);
	}

	@Override
	public SyncFileset getClientFileset(String clientId) {
		SyncFileset fileset = getClientFilesetNoCopy(clientId);
		return new SyncFileset(fileset.getVersion(), SyncFileV2.copy(fileset.getFiles()));
	}	
	
	private SyncFilesetV2 getClientFilesetNoCopy(String clientId) {
		checkRunning();
		clientId = clientId.toLowerCase();
		SyncFilesetV2 fileset = clientsFileSet.get(clientId);
		if (fileset == null) {
			synchronized (this) {
				fileset = clientsFileSet.get(clientId);
				if (fileset == null) {
					fileset = createClientFileSet(clientId);
					clientsFileSet.put(clientId, fileset);
				}
			}
		}
		return fileset;
	}

	private SyncFilesetV2 createClientFileSet(String clientId) {
		Client client = checkClient(clientId);
		SyncFilesetV2 result = new SyncFilesetV2(client.listFiles());		
		logger.info(String.format("Client fileset update[%s version：%s]", client.getId(), result.getVersion()));
		if (logger.isDebugEnabled())
			logger.debug("fileset：" + result);
		return result;
	}

	@Override
	public synchronized void startup() {
		if (isRunning())
			return;
		clients = config.getAutoSync().getClients().toArray(new Client[0]);
		clientsFileSet = new ConcurrentHashMap<String, SyncFilesetV2>();
		fileMonitor = new FileMonitor(new Listener(), config.getAutoSync().getServer().getVersionChangeInterval());
		for (Client client : config.getAutoSync().getClients()) {
			fileMonitor.add(client);					
		}
		fileMonitor.startup();
	}

	@Override
	public synchronized void shutdown() {
		clients = null;
		clientsFileSet = null;
		if (fileMonitor != null) {
			fileMonitor.shutdown();
			fileMonitor = null;
		}
	}

	@Override
	public boolean isRunning() {
		return clientsFileSet != null;
	}

	private void checkRunning() {
		if (!isRunning())
			throw new IllegalStateException();
	}
	
	private class Listener implements FileChangedListener {
		@Override
		public void onChanged(String clientId) {			
			logger.info(String.format("Client version reset[%s]", clientId));
			clientId = clientId.toLowerCase();			
			clientsFileSet.remove(clientId.toLowerCase());
		}
	}

	@Override
	public File getClientFile(String clientId, String filename) {
		SyncFileset fileset = getClientFilesetNoCopy(clientId);
		if (fileset == null)
			throw new IllegalArgumentException("Client does not exist：" + clientId);
		LocalFile syncFile = null;
		for (SyncFile f : fileset.getFiles()) {
			if (f.getName().contains(filename))
				syncFile = (LocalFile) f;
		}
		if (syncFile == null)
			throw new IllegalArgumentException(String.format("The file does not exist in this client[%s:%s]", clientId, filename));

		return syncFile.getFile();
	}

	@Override
	public Client getClient(String clientId) {
		for (Client client : clients) {
			if (client.getId().equalsIgnoreCase(clientId)) {				
				Client result = new Client(client.getId(), getClientFileset(clientId).getVersion(), null, client.getActions());
				for (Fileset fileset : client.getFilesets()) {
					if (fileset instanceof ServerFileset) {
						ServerFileset sf = (ServerFileset) fileset;
						fileset = new Fileset(sf.getId(), sf.getClient(), sf.retDeleteMode(), sf.getIncludes().toArray(new FileMatcher[0]),
								sf.getExcludes().toArray(new FileMatcher[0]));						
					}
					result.addFileset(fileset);
				}
				return result;
			}
		}
		return null;		
	}

	@Override
	public SyncFilesetV2 getClientFilesetV2(String clientId) {
		SyncFilesetV2 fileset = getClientFilesetNoCopy(clientId);
		return new SyncFilesetV2(fileset.getVersion(), SyncFileV2.copy(fileset.getFiles()));
	}
}
