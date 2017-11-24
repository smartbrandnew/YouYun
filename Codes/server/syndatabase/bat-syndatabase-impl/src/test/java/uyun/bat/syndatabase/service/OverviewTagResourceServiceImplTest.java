package uyun.bat.syndatabase.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uyun.bat.syndatabase.Startup;
import uyun.bat.syndatabase.entity.ResIdTransform;
import uyun.bat.syndatabase.service.impl.OverviewTagResourceServiceImpl;

public class OverviewTagResourceServiceImplTest {
	
	private static OverviewTagResourceServiceImpl overviewTagResourceService = (OverviewTagResourceServiceImpl)Startup.getInstance().getBean("overviewTagResourceService");
	
	@Test
	public void testGetAllResId(){
		overviewTagResourceService.getAllResId();
	}
	
	@Test
	public void testUpdateResId(){
		List<ResIdTransform> resId = new ArrayList<ResIdTransform>();
		ResIdTransform r1 = new ResIdTransform();
		r1.setOldResId("95622AB67001441D81C27B2B24709E89");
		r1.setNewResId("95622AB67001441D81C27B2B24709E8A");
		ResIdTransform r2 = new ResIdTransform();
		r2.setOldResId("D54F07E715B84F6A80EAB58982A53A0D");
		r2.setNewResId("D54F07E715B84F6A80EAB58982A53A0E");
		resId.add(r1);resId.add(r2);
		overviewTagResourceService.updateResId(resId);
	}
}
