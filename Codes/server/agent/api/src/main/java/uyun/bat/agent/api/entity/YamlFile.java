package uyun.bat.agent.api.entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class YamlFile {
	private String agentId;
	private String fileName;
	private String tenantId;
	private Date modified;
	private String md5;
	private int size;
	private String content;
	private AgentSource source;
	//转换enum为string
	private String agent_source;
	private boolean enabled;

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public YamlFile() {
		super();
	}

	public YamlFile (String agentId, String fileName, String tenantId, String agent_source){
		this.agentId = agentId;
		this.fileName = fileName;
		this.tenantId = tenantId;
		this.agent_source = agent_source;
		this.source = AgentSource.checkAgentSourceByName(agent_source);
	}

	public YamlFile(String agentId, String fileName, String tenantId, Date modified, String md5, int size,
			String content, AgentSource source) {
		super();
		this.agentId = agentId;
		this.fileName = fileName;
		this.tenantId = tenantId;
		this.modified = modified;
		this.md5 = md5;
		this.size = size;
		this.content = content;
		if (source == null)
			source = AgentSource.agent;
		this.source = source;
		this.agent_source = source.getName();
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@JsonIgnore
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	@JsonIgnore
	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	@JsonIgnore
	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	@JsonIgnore
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public AgentSource getSource() {
		if (source == null) {
			if (agent_source != null)
				source = AgentSource.checkAgentSourceByName(agent_source);
			else
				source = AgentSource.agent;
		}
		return source;
	}

	public void setSource(AgentSource source) {
		this.source = source;
	}

	@JsonIgnore
	public String getAgent_source() {
		if (agent_source == null) {
			if (source != null)
				agent_source = source.getName();
			else
				agent_source = AgentSource.agent.getName();
		}
		return agent_source;
	}

	public void setAgent_source(String agent_source) {
		this.agent_source = agent_source;
		this.source = AgentSource.checkAgentSourceByName(agent_source);
	}

	@Override
	public String toString() {
		return "YamlFile [agentId=" + agentId + ", fileName=" + fileName + ", tenantId=" + tenantId + ", modified="
				+ modified + ", md5=" + md5 + ", size=" + size + ", content=" + content + ", source=" + source + "]";
	}

	public static void main(String[] args) {
		File file = new File("d:/db2.yaml");
		String rs = null;
		try {
			FileReader reader = new FileReader(file);
			BufferedReader buffReader = new BufferedReader(reader);
			StringBuilder builder = new StringBuilder();
			String line = buffReader.readLine();
			while (line != null && line.trim().length() > 0) {
				builder.append(line);
				builder.append("\n");
				line = buffReader.readLine();
			}
			buffReader.close();
			rs = builder.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<YamlFile> list = new ArrayList<YamlFile>();
		YamlFile file1 = new YamlFile();
		file1.setAgentId(UUID.randomUUID().toString());
		file1.setContent(rs);
		file1.setFileName("cassandra");
		list.add(file1);
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(list);
			System.out.println("json: " + json);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
