package uyun.bat.web.impl.common.util;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;

import uyun.bat.web.api.favourite.entity.FavouriteDashboard;

public class DashboardComparator implements Comparator<FavouriteDashboard>{

	@Override
	public int compare(FavouriteDashboard o1, FavouriteDashboard o2) {
		String[] strings = new String[]{o1.getName(),o2.getName()};
		Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
		Arrays.sort(strings, com);
		//Arrays.sort(strings,String.CASE_INSENSITIVE_ORDER);
		if(strings[0].equals(o1.getName()))
			return -1;
			else
				return 1;
	}
}
