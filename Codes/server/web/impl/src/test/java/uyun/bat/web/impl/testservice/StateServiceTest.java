package uyun.bat.web.impl.testservice;

import uyun.bat.datastore.api.entity.Checkperiod;
import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.datastore.api.entity.State;
import uyun.bat.datastore.api.service.StateService;

import java.util.List;

public class StateServiceTest implements StateService{

	@Override
	public String saveState(String tenantId, State state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteState(String tenantId, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public State[] getStates(String tenantId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveCheckpoint(Checkpoint cp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Checkpoint getCheckpoint(String state, String[] tags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteCheckpoints(String state, String[] tags) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteCheckpoints(String state, String tenantId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getCheckpointsCount(String state, String[] tags, String value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Checkpoint[] getCheckpoints(String state, String[] tags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Checkperiod[] getCheckperiods(String state, String[] tags, long firstTime, long lastTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Checkperiod[] getCheckperiods(String tenantId, String state, String objectId, long firstTime,
			long lastTime) {
		// TODO Auto-generated method stub
		Checkperiod[] checkperiods=new Checkperiod[1];
		Checkperiod c=new Checkperiod();
		c.setValue("123");
		c.setLastTime(lastTime);
		c.setFirstTime(firstTime);
		
		return null;
	}

	@Override
	public Checkperiod getLastCheckperiod(String tenantId, String state, String objectId) {
		return null;
	}

	@Override
	public List<String> getTagsByState(String tenantId, String state) {
		return null;
	}

	@Override
	public Checkperiod[] getLastCheckperiods(String state, String[] tags, long firstTime, long lastTime) {
		return new Checkperiod[0];
	}

}
