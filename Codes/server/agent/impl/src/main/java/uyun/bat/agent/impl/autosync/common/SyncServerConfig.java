package uyun.bat.agent.impl.autosync.common;

import org.apache.commons.digester.Digester;
import uyun.bat.agent.impl.autosync.entity.*;
import uyun.whale.common.util.error.ErrorUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 自动同步服务配置文件类
 * @author Jiangjw
 */
public class SyncServerConfig {
	private static SyncServerConfig instance;
	private AutoSync autoSync;

	/**
	 * 获取默认实例
	 * @return
	 */
	public static SyncServerConfig getDefault() {
		if (instance == null) {
			synchronized (SyncServerConfig.class) {
				if (instance == null)
					try {
						instance = new SyncServerConfig();
					} catch (FileNotFoundException e) {
						throw new RuntimeException("Load Exception");
					}
			}
		}
		return instance;
	}
	
	public SyncServerConfig() throws FileNotFoundException {
		this(new FileInputStream(getConfFile()));
	}
	
	public SyncServerConfig(String filename) {
		init(filename);		
	}
	
	private void init(Object arg) {
		Digester digester = new Digester();  		
		createRule(digester);
		autoSync = new AutoSync();
		digester.push(autoSync);				
		try {
			if (arg instanceof String)
				digester.parse((String)arg);
			else if (arg instanceof InputStream)
				digester.parse((InputStream)arg);
			else
				throw new IllegalArgumentException("Unknow Type");
		} catch (Throwable e) {
			throw new RuntimeException(ErrorUtil.createMessage("Auto synchronization service config load failure：" + arg, e));
		}
	}

	private static void createRule(Digester digester) {
		String path = "autosync/server";
		createRuleList(digester, path, Server.class, "setServer");
			
		path = "autosync/client";
		createRuleList(digester, path, Client.class, "addClient");
		
		String actionPath = path + "/action";
		createRuleList(digester, actionPath, Action.class, "addAction");
		createRuleList(digester, actionPath + "/condition", Condition.class, "addCondition");
		
		String filesetPath = path + "/fileset";
		createRuleList(digester, filesetPath, ServerFileset.class, "addFileset");
		createRuleList(digester, filesetPath + "/include", FileMatcher.class, "addInclude");
		createRuleList(digester, filesetPath + "/exclude", FileMatcher.class, "addExclude");		
	}

	private static void createRuleList(Digester digester, String path, Class<?> cls, String method) {
		digester.addObjectCreate(path, cls);
		digester.addSetProperties(path);
		digester.addSetNext(path, method);
	}

	public SyncServerConfig(InputStream stream) {
		init(stream);	
	}

	@Deprecated
	public static String getUserDir() {
		return System.getProperty("user.dir");
	}

	private static File getConfFile() {
		String[] searchPaths = new String[] { "/conf/", "/../conf/", "/../../conf/", "/src/main/resources/conf/" };
		String dir = System.getProperty("work.dir", System.getProperty("user.dir"));
		for (String path : searchPaths) {
			File file = new File(dir, path + "autosync.xml");
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}
	
	public AutoSync getAutoSync() {
		return autoSync;
	}
}
