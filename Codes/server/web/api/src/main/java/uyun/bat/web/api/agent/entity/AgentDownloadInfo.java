package uyun.bat.web.api.agent.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AgentDownloadInfo {
	private String command;
	private List<AgentFile> files;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public List<AgentFile> getFiles() {
		return files;
	}

	public void setFiles(List<AgentFile> files) {
		this.files = files;
	}

	/**
	 * 解析文件夹目录，生产对应的agent下载数据 {OS} |-XXX-32 |-XXX-64
	 * 
	 * @param basePath 基础路径,类似/downloads/agent/
	 * @param file
	 * @return
	 */
	public static AgentDownloadInfo parseInfo(List<Map<String, String>> files) {
		if (files == null || files.size() == 0)
			return null;

		AgentDownloadInfo info = new AgentDownloadInfo();
		info.files = new ArrayList<AgentDownloadInfo.AgentFile>();

		for (Map<String, String> temp : files) {
			AgentFile af = new AgentFile();
			af.setName(temp.get("name"));
			af.setUrl(temp.get("url"));

			if (af.getName().indexOf("64") != -1) {
				if (info.files.size() > 0)
					info.files.add(1, af);
				else
					info.files.add(af);
			} else {
				info.files.add(0, af);
			}
		}

		return info;
	}

	public static class AgentFile {
		private String name;
		private String url;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

	}
}
