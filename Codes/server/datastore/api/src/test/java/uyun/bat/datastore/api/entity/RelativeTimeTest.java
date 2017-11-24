package uyun.bat.datastore.api.entity;



import org.junit.Assert;
import org.junit.Test;

public class RelativeTimeTest {

	@Test
	public void testGetTimeRelativeTo() {
		
		RelativeTime time=new RelativeTime(24, TimeUnit.HOURS);
		time.getUnit();
		time.hashCode();
		
		Assert.assertEquals(24, time.getValue());
		
	}

}
