package com.broada.carrier.monitor.server.impl.logic;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.acm.session.SessionContext;
import com.broada.carrier.monitor.common.error.BaseException;
import com.broada.carrier.monitor.common.util.FileUtil;
import com.broada.carrier.monitor.server.impl.config.Config;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.ThreadUtil;

public class SessionManager {
	private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
	private static Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();
	private static ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
	private static File sessionsDir;
	
	static {
		sessionsDir = new File(Config.getTempDir() + "/sessions");
		sessionsDir.mkdirs();
		
		loadSessions();
		
		ThreadUtil.createThread(new SessionClean()).start();
	}
	
	public static void setSessionId(String sessionId) {
		Session session = checkSession(sessionId);
		currentSession.set(session);
	}

	private static void loadSessions() {
		try {
			File[] files = sessionsDir.listFiles();
			for (File file : files) {
				if (!file.isFile())
					continue;
				
				try {
					Session session = (Session) FileUtil.readObject(file);
					sessions.put(session.context.getSessionId().toString(), session);
				} catch (Throwable e) {
					ErrorUtil.warn(logger, "会话文件加载失败：" + file, e);
				}
			}
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "加载会话列表失败", e);
		}
	}

	private static Session checkSession(String sessionId) {
		Session session = sessions.get(sessionId);
		if (session == null)
			throw new BaseException("会话无效，用户已注销：" + sessionId);
		return session;
	}

	public static void reset() {
		currentSession.set(null);
	}

	public static void addSession(SessionContext context) {
		String id = context.getSessionId().toString();
		Session session = new Session(context);
		sessions.put(id, session);		
		FileUtil.writeObject(getFile(id), session);		
	}

	private static File getFile(String id) {
		return new File(sessionsDir, id);
	}

	public static void removeSession(String sessionId) {
		sessions.remove(sessionId);
		getFile(sessionId).delete();
	}
	
	private static Session checkSession() {
		Session session = currentSession.get();
		if (session == null)
			throw new BaseException("用户未登录");
		session.active();
		return session;
	}

	public static String checkSessionUserId() {
		return checkSession().context.getAuthentication().getUser().getId();
	}

	public static String checkSessionDomainId() {
		String domainId = checkSession().context.getAuthentication().getUser().getDefaultDomainId();
		if (domainId == null)
			domainId = "rootDomain";
		return domainId;
	}
	
	private static class SessionClean implements Runnable {

		@Override
		public void run() {
			while (true) {
				long now = System.currentTimeMillis();
				Session[] items = sessions.values().toArray(new Session[0]); 
				for (Session session : items) {
					if (session.isTimeout(now))
						removeSession(session.context.getSessionId().toString());
				}
				
				try {
					Thread.sleep(60 * 1000);
				} catch (InterruptedException e) {
					break;
				}
			}
			
		}
		
	}

	private static class Session implements Serializable {
		private static final long serialVersionUID = 1L;
		private SessionContext context;
		private long lastActiveTime;

		public Session(SessionContext context) {
			this.context = context;
			active();
		}
		
		public void active() {
			lastActiveTime = System.currentTimeMillis();
		}
		
		public boolean isTimeout(long now) {
			long time = now - lastActiveTime;
			return time >= Config.getDefault().getSessionTimeout();
		}
	}
}
