package uyun.bat.datastore.dao;

import uyun.bat.datastore.api.entity.State;

public interface StateDao {
	State[] getByTenantId(String tenantId);

	State get(String tenantId, String name);

	void create(String tenantId, State state);

	void update(State state);

	void delete(String tenantId, String name);
}
