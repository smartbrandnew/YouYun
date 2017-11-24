package uyun.bat.agent.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import uyun.bat.agent.api.entity.PageAgent;
import uyun.bat.agent.api.service.AgentService;
import uyun.bat.agent.impl.logic.AgentLogic;
import uyun.bat.agent.impl.logic.LogicManager;
import uyun.bat.agent.impl.logic.YamlFileLogic;

import java.util.List;

@Service(protocol = "dubbo")
public class AgentServiceImpl implements AgentService {

    private AgentLogic agentLogic = LogicManager.getInstance().getAgentLogic();
    private YamlFileLogic yamlFileLogic = LogicManager.getInstance().getYamlFileLogic();

    @Override
    public List<String> queryTags(String tenantId,String source) {
        return agentLogic.queryTags(tenantId,source);
    }

    @Override
    public PageAgent queryByTags(String tenantId,String[] tags,String source,String searchValue,int current, int pageSize) {
        return agentLogic.queryByTags(tenantId,tags,source,searchValue,current,pageSize);
    }

    @Override
    public boolean delete(String id) {
        agentLogic.deleteAgent(id);
        agentLogic.deleteAgentTagById(id);
        yamlFileLogic.delete(id);
        return true;
    }
}
