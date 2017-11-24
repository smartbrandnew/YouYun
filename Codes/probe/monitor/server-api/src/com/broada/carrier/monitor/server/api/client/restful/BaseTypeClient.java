package com.broada.carrier.monitor.server.api.client.restful;


import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.server.api.service.BaseTypeService;

public class BaseTypeClient extends BaseServiceClient implements BaseTypeService {
	public BaseTypeClient(String baseServiceUrl, String apiUrl) {
		super(baseServiceUrl, apiUrl);
	}

	@Override
	public MonitorType[] getTypes() {
		return client.get("types", MonitorType[].class);
	}

	@Override
	public MonitorType getType(String typeId) {
		if (typeId == null)
			throw new IllegalArgumentException();
		return client.get("types/" + typeId, MonitorType.class);
	}

	@Override
	public MonitorItem getItem(String itemCode) {
		if (itemCode == null)
			throw new IllegalArgumentException();
		return client.get("items/" + itemCode, MonitorItem.class);
	}

	@Override
	public MonitorMethodType getMethodType(String typeId) {
		if (typeId == null)
			throw new IllegalArgumentException();
		return client.get("methodTypes/" + typeId, MonitorMethodType.class);
	}

	@Override
	public MonitorItem[] getItems() {
		return client.get("items", MonitorItem[].class);
	}
}
