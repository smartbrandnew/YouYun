package uyun.bat.agent.api.entity;

public enum AgentStatus {
	online(0,"在线"),offline(1,"离线");
	private int id;
	private String name;
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	private AgentStatus(int id,String name){
		this.id=id;
		this.name=name;
	}
	
	//默认返回在线
	public AgentStatus checkAgentByName(String name){
		for(AgentStatus status:AgentStatus.values()){
			if(status.getName().equalsIgnoreCase(name)){
				return status;
			}
		}
		return AgentStatus.online;
	}
	
}
