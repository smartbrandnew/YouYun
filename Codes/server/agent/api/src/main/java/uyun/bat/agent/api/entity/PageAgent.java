package uyun.bat.agent.api.entity;

import java.io.Serializable;
import java.util.List;

public class PageAgent implements Serializable{

    private int count;
    private List<Agent> agents;

    public PageAgent() {
    }

    public PageAgent(int count, List<Agent> agents) {
        this.count = count;
        this.agents = agents;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }
}
