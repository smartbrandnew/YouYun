package com.broada.carrier.monitor.impl.generic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.agent.config.HostAgentClient;
import com.broada.carrier.monitor.impl.generic.script.session.server.HostServerClient;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.numen.agent.manage.service.ManageAgent;
import com.broada.numen.agent.script.entity.Parameter;
import com.broada.numen.agent.script.entity.Result;
import com.broada.numen.agent.script.service.ExecuteException;
import com.broada.numen.agent.script.service.ExecuteService;
import com.broada.numen.agent.util.FileInfo;

public class GenericExecutor {
	protected static final Log logger = LogFactory.getLog(GenericExecutor.class);

	/**
	 * 通过isServerSide判断是在服务端还是在代理端执行脚本
	 * @param srv  监测服务
	 * @param port  远程端口
	 * @param isServerSide  是否是服务端
	 * @param extPara  扩展参数
	 * @return
	 * @throws RemoteException
	 * @throws NotBoundException
	 * @throws ExecuteException
	 * @throws FileNotFoundException 
	 */
	public static Result executeScript(MonitorNode node, MonitorTask srv, int port, boolean isServerSide, ExtParameter extPara, MonitorMethod method)
	throws RemoteException, NotBoundException, ExecuteException,ItemReferenceException, FileNotFoundException {
		Result result = null;
		Parameter param = extPara.createParameter();    
		File file = new File(System.getProperty("user.dir"), extPara.getScriptFilePath());

		if (!file.exists()) {
			result = new Result();
			result.setExecuteText("待执行的脚本[" + file.getPath() + "]不存在，请检查是否被删除。");
			throw new FileNotFoundException(result.getExecuteText());
		}

		//处理参数传入groovy脚本
		//srv、node
		//ScriptMonitorParameter    
		// 当执行结果为空时，重试的次数
		int retryTimes = 1;
		if(isServerSide){
			param.set("monitorNode", new NumenMonitorNode(node));
			param.set("monitorService", srv);
			for (int retry = 0; retry < retryTimes; retry++) {
				result = HostServerClient.executeInTime(extPara.getScriptFilePath(), param, method, node.getIp());
				Set rows = result.getRows();
				if (rows != null && !rows.isEmpty())
					break;
			}
		}else{
			ExecuteService commonCLIAgent = HostAgentClient.getCommonCLIAgent(node.getIp(), port, "executeService");
			ManageAgent agent = HostAgentClient.getManageAgent(node.getIp(), port);
			// 获取Server端的脚本版本信息
			FileInfo scriptFileInfo = null;
			List<FileInfo> fileInfos = new ArrayList<FileInfo>();
			scriptFileInfo = FileInfo.get(FileInfo.getFilename(extPara.getScriptFilePath()));
			fileInfos.add(scriptFileInfo);

			for (FileInfo fileInfo : fileInfos) {
				file = new File(FileInfo.getAbstractPath(fileInfo.getFilename()));
				// 检查脚本是否已经推送过了
				if (!checkScriptExist(agent, fileInfo)) {
					if (logger.isDebugEnabled()) {
						logger.debug("开始推送脚本:" + fileInfo.getFilename());
					}
					agent.putScript(file.getName(), readContent(file), fileInfo);// 推送到script目录下
					if (logger.isDebugEnabled()) {
						logger.debug("推送脚本:" + fileInfo.getFilename() + "结束");
					}
				}
			}
			for (int retry = 0; retry < retryTimes; retry++) {
				// 文件名以./开头，表示这个文件名是以Agent安装目录为根目录的相对路径
				result = commonCLIAgent.exceute("./" + scriptFileInfo.getFilename(), param);
				Set rows = result.getRows();
				if (rows != null && !rows.isEmpty())
					break;
			}
		}

		if (result == null) {
			result = new Result();
			result.setExecuteText("执行脚本结果没有返回任何数据。");
			return result;
		}

		return result;
	}

	private static byte[] readContent(File file) throws ExecuteException {
		// 事实上能够运行到这一步，脚本文件通过了是否存在判断这一步
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = in.read(buffer)) >= 0) {
				out.write(buffer, 0, len);
				len = -1;
			}
			return out.toByteArray();
		} catch (FileNotFoundException e) {
			logger.error("Can't find file:" + file.getPath(), e);
			throw new ExecuteException(ExecuteException.TYPE_SCRIPT_NOT_EXIST, "文件[" + file.getPath() + "]不存在。");
		} catch (IOException e) {
			logger.error("read file:" + file.getPath() + " error", e);
			throw new ExecuteException(ExecuteException.TYPE_SCRIPT_ERROR, "文件[" + file.getPath() + "]时发生错误。", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 由于通过agent方式推送过去的脚本都放置到script目录下，因此只要获取远目录下的文件列表进行判断即可
	 * @param agent
	 * @param scriptFileInfo
	 * @return
	 * @throws RemoteException
	 */
	private static boolean checkScriptExist(ManageAgent agent, FileInfo scriptFileInfo) throws RemoteException {
		FileInfo remoteFileInfo = agent.getFileInfo(scriptFileInfo.getFilename());
		return scriptFileInfo.equalsIgnoreFilename(remoteFileInfo);
	}


	public static boolean isErrorConnectFailed(String message) {
		return StringUtils.contains(message, "Connection reset") || StringUtils.contains(message, "Connection refused");
	}
}
