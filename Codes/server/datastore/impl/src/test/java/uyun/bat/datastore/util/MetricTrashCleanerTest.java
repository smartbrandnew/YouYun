package uyun.bat.datastore.util;

import org.junit.Test;

import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.entity.RelativeTime;
import uyun.bat.datastore.api.entity.TimeUnit;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class MetricTrashCleanerTest {
	private static MetricTrashCleaner cleaner=Startup.getInstance().getBean(MetricTrashCleaner.class);
	private static final String TENANT_ID = "12345678910111213141516171819202";
	@Test
	public void testDeleteMetricDataStringSetMultimapOfStringStringRelativeTime() {
		SetMultimap<String, String> tags = HashMultimap.create();
		tags.put("host", "zhaoyn,myPC");
		tags.put("tenantId", TENANT_ID);
		boolean ig = cleaner.deleteMetricData("test.cpu.point", tags, new RelativeTime(24, TimeUnit.HOURS));
		System.out.println(ig);
	}

}
