package uyun.bat.datastore.api.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResourceGroup implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private List<Resource> resources;

	public ResourceGroup(String name) {
		this.name = name;
		this.resources = new ArrayList<Resource>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ResourceGroup addResource(Resource resource) {
		if (!resources.contains(resource))
			this.resources.add(resource);
		return this;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	@Override
	public String toString() {
		return "ResourceGroup [name=" + name + ", resources=" + resources + "]";
	}

}
