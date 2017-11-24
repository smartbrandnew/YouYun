package com.broada.carrier.monitor.impl.mw.iis.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.iis.common.IISTempData;
import com.broada.carrier.monitor.impl.mw.iis.files.IISFilesMonitor;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * IIS用户监测
 * 
 * @author 杨帆
 * 
 */
public class IISUsersMonitor implements Monitor {
	private static final Logger logger = LoggerFactory.getLogger(IISFilesMonitor.class);

	public static final String INDEX_CURR_ANM_USERS = "IIS-USERS-1";

	public static final String INDEX_CURR_UNANM_USERS = "IIS-USERS-2";

	public static final String INDEX_ANM_USER_PERS = "IIS-USERS-3";

	public static final String INDEX_UNANM_USER_PERS = "IIS-USERS-4";

	public static final String INDEX_MAX_ANM_USERS = "IIS-USERS-5";

	public static final String INDEX_MAX_UNANM_USERS = "IIS-USERS-6";

	public static final String INDEX_RUNTIME = "IIS-USERS-7";

	@Override
	public MonitorResult monitor(MonitorContext context) {
		IISTempData tempData = context.getTempData(IISTempData.class);
		if (tempData == null)
			tempData = new IISTempData();
		MonitorResult result = collect(new CollectContext(context), context.getTask().getId(), tempData);
		context.setTempData(tempData);
		return result;
	}

	@Override
	public Serializable collect(CollectContext context) {
		return collect(context, "-1", new IISTempData());
	}

	private MonitorResult collect(CollectContext context, String taskId, IISTempData tempData) {
		MonitorResult result = new MonitorResult();
		result.setState(MonitorConstant.MONITORSTATE_NICER);

		CLIResult cliResult = null;

		try {
			long replyTime = System.currentTimeMillis();
			cliResult = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(),
					CLIConstant.COMMAND_IISUSERS);
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0)
				replyTime = 1L;
			result.setResponseTime(replyTime);
		} catch (CLILoginFailException fe) {
			result.setResultDesc("登录目标服务器失败，请检查监测配置的用户/密码等是否正确.");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		} catch (CLIConnectException ce) {
			result.setResultDesc("无法连接目标服务器，可能目标服务器没开启或者网络断开.");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		} catch (CLIResultParseException e) {
			result.setResultDesc("解析IIS用户登陆信息采集结果失败：" + e.getMessage());
			logger.error("解析IIS用户登陆信息采集结果失败.@" + context.getNode().getIp(), e);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		} catch (Throwable e) {
			String msg = e.getMessage();
			if (StringUtils.contains(msg, "RPC 服务器不可用") || StringUtils.contains(msg, "RPC server is unavailable")) {
				result.setResultDesc("无法连接目标服务器，可能目标服务器没开启或者网络断开或者未启用RPC服务.");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			}
			result.setResultDesc("获取IIS用户登陆信息失败:" + e.getMessage());
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		}
		List<Properties> users = cliResult.getListTableResult();

		List<Properties> realUsers = getRealData(users, context, tempData);

		tempData.getDatas().clear();
		tempData.getDatas().addAll(users);

		for (Properties properties : realUsers) {
			String webName = (String) properties.get("Name");
			MonitorResultRow row = new MonitorResultRow(webName);

			String currentAnonymousUsers = (String) properties.get("CurrentAnonymousUsers");
			row.setIndicator(INDEX_CURR_ANM_USERS, Integer.parseInt(currentAnonymousUsers));

			String currentNonAnonymousUsers = (String) properties.get("CurrentNonAnonymousUsers");
			row.setIndicator(INDEX_CURR_UNANM_USERS, Integer.parseInt(currentNonAnonymousUsers));

			String anonymousUsersPersec = (String) properties.get("AnonymousUsersPersec");
			row.setIndicator(INDEX_ANM_USER_PERS, Integer.parseInt(anonymousUsersPersec));

			String nonAnonymousUsersPersec = (String) properties.get("NonAnonymousUsersPersec");
			row.setIndicator(INDEX_UNANM_USER_PERS, Integer.parseInt(nonAnonymousUsersPersec));

			String maximumAnonymousUsers = (String) properties.get("MaximumAnonymousUsers");
			row.setIndicator(INDEX_MAX_ANM_USERS, Integer.parseInt(maximumAnonymousUsers));

			String maximumNonAnonymousUsers = (String) properties.get("MaximumNonAnonymousUsers");
			row.setIndicator(INDEX_MAX_UNANM_USERS, Integer.parseInt(maximumNonAnonymousUsers));

			String serviceUptime = (String) properties.get("ServiceUptime");
			row.setIndicator(INDEX_RUNTIME, Double.parseDouble(serviceUptime));

			result.addRow(row);
		}

		return result;
	}

	/**
	 * 根据上次保存的性能值计算真实的流量
	 * 
	 * @param users
	 *            当前实时获取到的数据
	 * @param srv
	 * @return
	 */
	private List<Properties> getRealData(List<Properties> users, CollectContext context, IISTempData lastData) {
		List<Properties> realData = new ArrayList<Properties>();
		for (int index = 0; index < users.size(); index++) {
			Properties properties = (Properties) users.get(index);
			Properties Last_data = null;
			for (int i = 0; i < lastData.getDatas().size(); i++) {
				Properties last_propertie = (Properties) lastData.getDatas().get(i);
				if (((String) last_propertie.getProperty("Name")).equals((String) properties.get("Name"))) {
					Last_data = last_propertie;
					break;
				}
			}
			Properties newProp = new Properties();
			newProp.setProperty("Name", (String) properties.get("Name"));
			newProp.setProperty("MaximumAnonymousUsers", (String) properties.get("MaximumAnonymousUsers"));
			newProp.setProperty("MaximumNonAnonymousUsers", (String) properties.get("MaximumNonAnonymousUsers"));
			newProp.setProperty("ServiceUptime", (String) properties.get("ServiceUptime"));
			if (Last_data == null) {
				// 没有历史数据则直接获取这次的数据,并设置相关值为0
				newProp.setProperty("CurrentAnonymousUsers", "0");
				newProp.setProperty("CurrentNonAnonymousUsers", "0");
				newProp.setProperty("AnonymousUsersPersec", "0");
				newProp.setProperty("NonAnonymousUsersPersec", "0");
			} else {
				// 有历史数据的情况下计算真实数据
				double last_uptime = Double.parseDouble((String) Last_data.get("ServiceUptime"));
				double realCurrAnmUsers = 0;
				double realCurrUnAnmUsers = 0;
				double realAnmUserPers = 0;
				double realUnAnmUserpers = 0;

				double per_curr_anm_users = Double.parseDouble((String) Last_data.get("CurrentAnonymousUsers"));
				double per_curr_unanm_users = Double.parseDouble((String) Last_data.get("CurrentNonAnonymousUsers"));
				double per_anm_user_pers = Double.parseDouble((String) Last_data.get("AnonymousUsersPersec"));
				double per_unanm_user_pers = Double.parseDouble((String) Last_data.get("NonAnonymousUsersPersec"));

				realCurrAnmUsers = (Double.parseDouble((String) properties.get("CurrentAnonymousUsers")) - (Double
						.parseDouble((String) Last_data.get("CurrentAnonymousUsers"))))
						/ (Double.parseDouble((String) properties.get("ServiceUptime")) - last_uptime);

				// 如果数据没有发生变化或则不合理则取上一次保存的性能数据
				if (realCurrAnmUsers == 0 || Double.isNaN(realCurrAnmUsers) || realCurrAnmUsers < 0) {
					realCurrAnmUsers = per_curr_anm_users;
				}

				realCurrUnAnmUsers = (Double.parseDouble((String) properties.get("CurrentNonAnonymousUsers")) - (Double
						.parseDouble((String) Last_data.get("CurrentNonAnonymousUsers"))))
						/ (Double.parseDouble((String) properties.get("ServiceUptime")) - last_uptime);

				if (realCurrUnAnmUsers == 0 || Double.isNaN(realCurrUnAnmUsers) || realCurrUnAnmUsers < 0) {
					realCurrUnAnmUsers = per_curr_unanm_users;
				}

				realAnmUserPers = (Double.parseDouble((String) properties.get("AnonymousUsersPersec")) - (Double
						.parseDouble((String) Last_data.get("AnonymousUsersPersec"))))
						/ (Double.parseDouble((String) properties.get("ServiceUptime")) - last_uptime);

				if (realAnmUserPers == 0 || Double.isNaN(realAnmUserPers) || realCurrUnAnmUsers < 0) {
					realAnmUserPers = per_anm_user_pers;
				}

				realUnAnmUserpers = (Double.parseDouble((String) properties.get("NonAnonymousUsersPersec")) - (Double
						.parseDouble((String) Last_data.get("NonAnonymousUsersPersec"))))
						/ (Double.parseDouble((String) properties.get("ServiceUptime")) - last_uptime);

				if (realUnAnmUserpers == 0 || Double.isNaN(realUnAnmUserpers) || realCurrUnAnmUsers < 0) {
					realUnAnmUserpers = per_unanm_user_pers;
				}

				newProp.setProperty("CurrentNonAnonymousUsers",
						String.valueOf(new Double(realCurrUnAnmUsers).intValue()));
				newProp.setProperty("CurrentAnonymousUsers", String.valueOf(new Double(realCurrAnmUsers).intValue()));
				newProp.setProperty("AnonymousUsersPersec", String.valueOf(new Double(realAnmUserPers).intValue()));
				newProp.setProperty("NonAnonymousUsersPersec", String.valueOf(new Double(realUnAnmUserpers).intValue()));
			}
			realData.add(newProp);
		}
		return realData;
	}
}
