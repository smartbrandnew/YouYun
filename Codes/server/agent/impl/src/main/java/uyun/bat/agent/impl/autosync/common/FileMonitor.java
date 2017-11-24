package uyun.bat.agent.impl.autosync.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import uyun.bat.agent.impl.autosync.entity.Client;
import uyun.bat.agent.impl.autosync.entity.Fileset;
import uyun.whale.common.util.concurrent.ThreadUtil;
import uyun.whale.common.util.io.FileUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 文件变更检查
 * 本检查会按时间间隔来分析，如果文件变更后，超过时间时间没有文件继续变更，才产生一个文件变更通知
 * @author Jiangjw
 */
public class FileMonitor {
	private static final String VFS_FILE_PREFIX = "file:///";
	private static final Log logger = LogFactory.getLog(FileMonitor.class);
	private DefaultFileMonitor monitor = new DefaultFileMonitor(new Listener());
	private int notifyInterval;
	private Thread notifyThread;	
	private Map<String, NotifyClient> clients = new HashMap<String, NotifyClient>();
	private FileChangedListener listener;
	
	/**
	 * 构造函数
	 * @param listener 文件变更监听器
	 * @param notifyInterval 文件变更通知周期
	 */
	public FileMonitor(FileChangedListener listener, int notifyInterval) {
		this.listener = listener;
		this.notifyInterval = notifyInterval * 1000;
	}
	
	/**
	 * 启动，必须在启动前完成监视目录的添加操作
	 */
	public void startup() {
		monitor = new DefaultFileMonitor(new Listener());
		monitor.setRecursive(true);
		Set<String> dirs = new HashSet<String>();
		for (NotifyClient client : clients.values()) {
			for (Fileset fileset : client.client.getFilesets()) {
				String dir = fileset.retDirPath();
				if (dirs.contains(dir))
					continue;
				try {
					if (!fileset.retDirFile().exists()) {
						boolean result = fileset.retDirFile().mkdirs();
						if (logger.isDebugEnabled())
							logger.debug(String.format("Monitor directory does not exist，try to create[%s result：%s]", dir, result));
					}
					monitor.addFile(VFS.getManager().resolveFile(dir));
				} catch (FileSystemException e) {
					throw new IllegalArgumentException();
				}
			}			
		}
		monitor.start();
		notifyThread = ThreadUtil.createThread(new NotifyThread());
		notifyThread.start();		
	}
	
	/**
	 * 关闭
	 */
	public void shutdown() {
		monitor.stop();
		monitor = null;
		
		notifyThread.interrupt();
		notifyThread = null;
		
		clients.clear();
	}
	
	/**
	 * 是否在运行
	 * @return
	 */
	public boolean isRunning() {
		return monitor != null;
	}

	private class Listener implements FileListener {
		@Override
		public void fileChanged(FileChangeEvent event) throws Exception {
			if (logger.isDebugEnabled())
				logger.debug("File modify：" + event.getFile());		
			fireFileChanged(event.getFile());
		}

		@Override
		public void fileCreated(FileChangeEvent event) throws Exception {
			if (logger.isDebugEnabled())
				logger.debug("File create：" + event.getFile());		
			fireFileChanged(event.getFile());
		}

		@Override
		public void fileDeleted(FileChangeEvent event) throws Exception {
			if (logger.isDebugEnabled())
				logger.debug("File delete：" + event.getFile());
			fireFileChanged(event.getFile());
		}		
	}

	/**
	 * 添加要监视的目录
	 *
	 */
	public void add(Client client) {
		if (!isRunning())
			throw new IllegalStateException("Can not add new monitor directories in the startup process");
		NotifyClient nc = clients.get(client.getId());
		if (nc == null) {
			nc = new NotifyClient(client);
			clients.put(client.getId(), nc);
		}		
	}
	
	static String toAbsolute(String file) {		
		if (file.startsWith(VFS_FILE_PREFIX)) {
			file = file.substring(VFS_FILE_PREFIX.length());
			if (file.charAt(1) != ':')
				file = "/" + file;
		}
		return FileUtil.toAbsolute(file);
	}
	
	private void fireFileChanged(FileObject file) {		
		String filename = toAbsolute(file.toString());
		long now = System.currentTimeMillis();
		for (NotifyClient client : clients.values()) {			
			if (client.match(filename)) {
				if (client.lastChanged <= 0) {
					if (logger.isDebugEnabled())
						logger.debug(String.format("Start to time the client file modification [%s]", client.client.getId()));
					client.lastChanged = now;
				}
			}
		}
	}

	private class NotifyThread implements Runnable {
		public void run() {
			long loopInterval = notifyInterval / 10;
			if (loopInterval < 100)
				loopInterval = 100;
			if (logger.isDebugEnabled())
				logger.debug(String.format("file synchronization check thread startup [loop: %dms notify: %dms]", loopInterval, notifyInterval));	
			while (isRunning()) {
				try {
					Thread.sleep(loopInterval);
				} catch (InterruptedException e) {
					break;
				}
				
				long now = System.currentTimeMillis();
				for (NotifyClient client : clients.values()) {
					if (client.lastChanged <= 0)
						continue;
					
					long diff = now - client.lastChanged;
					if (diff >= notifyInterval) {
						if (logger.isDebugEnabled())
							logger.debug(String.format("Client file modification notify [%s]", client.client.getId()));
						listener.onChanged(client.client.getId());
						client.reset();
					}
				}
			}
		}
	} 
	
	private static class NotifyClient {
		private Client client;
		private long lastChanged;
		
		public NotifyClient(Client client) {
			super();
			this.client = client;
		}		
			
		public boolean match(String filename) {
			for (Fileset fileset : client.getFilesets()) {
				if (!filename.startsWith(fileset.retDirPath()))
					continue;				
				if (fileset.match(filename.substring(fileset.retDirPath().length())))
					return true;
			}
			return false;
		}

		public void reset() {
			lastChanged = 0;
		}
	}
}
