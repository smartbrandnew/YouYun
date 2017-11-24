package uyun.bat.datastore.api.entity;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ResourceGroupTest {

	@Test
	public void test() {
		ResourceGroup resourceGroup = new ResourceGroup("name");
		resourceGroup.setName("testname");
		resourceGroup.getName();
		resourceGroup.getResources();
		Resource resource = new Resource();
		resourceGroup.addResource(resource);
		List<Resource> resources = new ArrayList<>();
		resourceGroup.setResources(resources);
		resourceGroup.toString();
	}

}
