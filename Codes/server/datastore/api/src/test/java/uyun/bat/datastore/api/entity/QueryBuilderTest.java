package uyun.bat.datastore.api.entity;



import org.junit.Assert;
import org.junit.Test;

public class QueryBuilderTest {

	private static String TEST = "aaa";
	@Test
	public void testGetMetric() {
		QueryBuilder builder=QueryBuilder.getInstance();

		builder.addMetric(TEST);
		Assert.assertNotNull(builder.getMetric());
	}

	@Test
	public void testAddMetric() {
		QueryBuilder builder=QueryBuilder.getInstance();

		builder.addMetric(TEST);
		Assert.assertNotNull(builder.getMetric());
	}

	@Test
	public void testSetStart() {
		QueryBuilder builder=QueryBuilder.getInstance();
		builder.addMetric(TEST);
		builder.setStart(24, TimeUnit.HOURS);
		Assert.assertEquals(24, builder.getStartRelative().getValue());
	}
	
	@Test
	public void testSetEnd(){
		QueryBuilder builder=QueryBuilder.getInstance();

		builder.addMetric(TEST);
		builder.setEnd(24, TimeUnit.HOURS);
		Assert.assertEquals(24, builder.getEndRelative().getValue());
	}
	
	@Test
	public void testEntity(){
		QueryBuilder builder=QueryBuilder.getInstance();

		builder.setStartAbsolute((long)12);
		builder.getStartAbsolute();
		builder.setEndAbsolute((long)12);
		builder.getEndAbsolute();
	}

}
