package uyun.bat.web.api.agent.entity;

import java.util.List;

public class MineAgent {
    private int count;
    private List<AgentVO> agents;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<AgentVO> getAgents() {
        return agents;
    }

    public void setAgents(List<AgentVO> agents) {
        this.agents = agents;
    }
}
