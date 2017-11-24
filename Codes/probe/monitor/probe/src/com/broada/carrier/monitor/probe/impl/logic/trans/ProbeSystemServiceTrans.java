package com.broada.carrier.monitor.probe.impl.logic.trans;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.base.logic.BaseSystemServiceImpl;
import com.broada.carrier.monitor.probe.api.service.ProbeSystemService;
import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.logic.ProbePolicyServiceImpl;
import com.broada.carrier.monitor.server.api.entity.SystemInfo;
import com.broada.component.utils.lang.ThreadUtil;
import com.broada.component.utils.runcheck.RuntimeChecker;
import com.broada.component.utils.runcheck.RuntimeInfoEntry;
import com.broada.component.utils.runcheck.RuntimeInfoProvider;

public class ProbeSystemServiceTrans extends BaseSystemServiceImpl implements ProbeSystemService {
	private static final Logger logger = LoggerFactory.getLogger(ProbeSystemServiceTrans.class);
	@Autowired
	private ProbeNodeServiceTrans nodeManager;
	@Autowired
	private ProbePolicyServiceImpl policyManager;
	@Autowired
	private ProbeMethodServiceTrans methodManager;

	@Override
	public void deleteAll() {
		nodeManager.deleteAll();
		methodManager.deleteAll();
		policyManager.deleteAll();
	}

	@Override
	public SystemInfo[] getInfos() {
		List<SystemInfo> infos = new ArrayList<SystemInfo>();
		for (RuntimeInfoProvider provider : RuntimeChecker.getDefault().getProviders()) {
			RuntimeInfoEntry[] entites = RuntimeChecker.getDefault().getEntries(provider.getName());
			if (entites == null || entites.length == 0)
				continue;
			for (RuntimeInfoEntry entry : entites) {
				infos.add(new SystemInfo(entry.getName(), entry.getName(), entry.getValue()));
			}
		}
		return infos.toArray(new SystemInfo[infos.size()]);
	}

	@Override
	public void exit(String reason) {
		logger.info("探针收到服务端要求主动退出：" + reason);
		ThreadUtil.createThread(new ExitThread()).start();
		;
	}

	private static class ExitThread implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(3000);
				System.exit(10);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public String getProperty(String code) {
		return Config.getDefault().getProps().get(code);
	}
}
