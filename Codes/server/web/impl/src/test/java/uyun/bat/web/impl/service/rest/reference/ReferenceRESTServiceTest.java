package uyun.bat.web.impl.service.rest.reference;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import uyun.bat.web.api.reference.entity.ResourceReference;
import uyun.bat.web.impl.testservice.StartService;

public class ReferenceRESTServiceTest extends StartService{
	ReferenceRESTService referenceREST = new ReferenceRESTService();
	

	@Test
	public void testGetResourceRefByName() {
		String name = "Java";
		ResourceReference resource=referenceREST.getResourceRefByName(name);
		assertTrue(resource!=null);
	}

	@Test
	public void testGetResourceRefs() {
		List<ResourceReference> list = referenceREST.getResourceRefs();
		assertTrue(list!=null);
	}

}
