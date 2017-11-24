package uyun.bat.datastore.api.entity;


import org.junit.Assert;
import org.junit.Test;

public class AggregatorTypeTest {

	private static String TEST_ID = "94baaadca64344d2a748dff88fe7159e";
	private static String TEST_NAME = "testName";
	@Test
	public void test() {
		AggregatorType type=AggregatorType.sum;
		type.setId(1);
		type.setName(TEST_NAME);
		type.getId();
		type.getName();
		AggregatorType.checkById(1);
		AggregatorType.checkByName(TEST_NAME);
		Assert.assertNotNull(type);
	}

}
