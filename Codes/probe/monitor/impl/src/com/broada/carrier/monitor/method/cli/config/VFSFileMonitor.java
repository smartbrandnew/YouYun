package com.broada.carrier.monitor.method.cli.config;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

import com.broada.carrier.monitor.common.util.WorkPathUtil;

public class VFSFileMonitor {
	private static final Log logger = LogFactory.getLog(VFSFileMonitor.class);

	VFSFileMonitor() {

	}

	void monitor(final String path) {
		FileSystemManager fsManager = null;
		FileObject listendir = null;
		try {
			fsManager = VFS.getManager();
			String filePath = new File(path).getPath();
			if(!filePath.startsWith(WorkPathUtil.getRootPath())){
				filePath = WorkPathUtil.getRootPath() + filePath;
			}
			listendir = fsManager.resolveFile(filePath);
		} catch (FileSystemException e) {
			throw new RuntimeException("监测文件系统:" + path + "失败!", e);
		}
		DefaultFileMonitor fMonitor = new DefaultFileMonitor(
				new FileListener() {
					public void fileChanged(FileChangeEvent event)
							throws Exception {
						log(event, "发生了改变");
						reloadCLIConfig();
					}

					public void fileCreated(FileChangeEvent event)
							throws Exception {
						log(event, "被创建");
						reloadCLIConfig();
					}

					public void fileDeleted(FileChangeEvent event)
							throws Exception {
						log(event, "被删除");
						reloadCLIConfig();
					}

					private void log(FileChangeEvent event, String msg) {
						FileObject fileObject = event.getFile();
						FileName fileName = fileObject.getName();
						logger.info(fileName.getPath() + ":" + msg + "\n");
					}

					private void reloadCLIConfig() {
						CLIConfigurationHandler.getInstance().loadCLIConfigs();
					}
				});
		fMonitor.setRecursive(true);
		fMonitor.addFile(listendir);
		fMonitor.start();
	}
}
