package uyun.bat.datastore.api.entity;




import org.junit.Assert;
import org.junit.Test;

public class TimeUnitTest {

	@Test
	public void test() {
		TimeUnit uint=TimeUnit.HOURS;
		Assert.assertNotNull(uint);
	}

	@Test
	public void test1() {
		DataPoint dataPoint1 = new DataPoint(1, "s");
		DataPoint dataPoint2 = new DataPoint(1, "s");
		Assert.assertTrue(dataPoint1.equals(dataPoint2));
	}

}
