package com.broada.carrier.monitor.impl.host.cli.file;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import com.broada.component.utils.text.DateUtil;

public class TestCLIFileExecutor extends TestCase {
	private static void assertEquals(Date date, String text) {
		assertEquals(DateUtil.format(date, DateUtil.PATTERN_YYYYMMDD_HHMMSS), text);
	}
	
	public void testCLIParse() throws ParseException {
		Date now = DateUtil.parse("2015-03-04 00:00:00");
		
		String cli = "drwxrwxrwt  18 bin      bin           20480 Apr 20 11:44 tmp";
		CLIFileMonitorCondition result = CLIFileExecutor.parseFileline(cli, now);
		assertEquals(result.getFilepath(), "tmp");
		assertEquals(result.getSize(), round(20480 / 1024.0 / 1024, 2));
		assertEquals(result.getUser(), "bin");
		assertEquals(result.getModifiedTime(), "2014-04-20 11:44:00");
		
		cli = "drwxrwxr-x   2 root     system          256 Jul 08 2010  tftpboot";
		result = CLIFileExecutor.parseFileline(cli);
		assertEquals(result.getFilepath(), "tftpboot");
		assertEquals(result.getSize(), round(256 / 1024.0 / 1024, 2));
		assertEquals(result.getUser(), "root");
		assertEquals(result.getModifiedTime(), "2010-07-08 00:00:00");
		
		cli = "dr-xr-xr-x  70 root root     0  Feb 20 15:53 proc";
		result = CLIFileExecutor.parseFileline(cli, now);
		assertEquals(result.getFilepath(), "proc");
		assertEquals(result.getSize(), round(0 / 1024.0 / 1024, 2));
		assertEquals(result.getUser(), "root");		
		assertEquals(result.getModifiedTime(), "2015-02-20 15:53:00");				
		
		cli = "drwxr-xr-x   2 root root  4096 2004-08-13  opt";
		result = CLIFileExecutor.parseFileline(cli);
		assertEquals(result.getFilepath(), "opt");
		assertEquals(result.getSize(), round(4096 / 1024.0 / 1024, 2));
		assertEquals(result.getUser(), "root");
		assertEquals(result.getModifiedTime(), "2004-08-13 00:00:00");
		
		cli = "lrwxrwxrwx  1 root root      4 2010-12-23  egrep -> grep";
		result = CLIFileExecutor.parseFileline(cli);
		assertEquals(result.getFilepath(), "egrep");
		assertEquals(result.getSize(), round(4 / 1024.0 / 1024, 2));
		assertEquals(result.getUser(), "root");
		assertEquals(result.getModifiedTime(), "2010-12-23 00:00:00");
		
		cli = "drwxr-xr-x  2 root  root    4096 12-16 10:14 file name";
		result = CLIFileExecutor.parseFileline(cli, now);
		assertEquals(result.getFilepath(), "file name");
		assertEquals(result.getSize(), round(4096 / 1024.0 / 1024, 2));
		assertEquals(result.getUser(), "root");
		assertEquals(result.getModifiedTime(), "2014-12-16 10:14:00");
		
		cli = "-rw-r--r--   1 root     sys          705 2013   8 30 /etc/passwd";
		result = CLIFileExecutor.parseFileline(cli, now);
		assertEquals(result.getFilepath(), "/etc/passwd");
		assertEquals(result.getSize(), round(705 / 1024.0 / 1024, 2));
		assertEquals(result.getUser(), "root");
		assertEquals(result.getModifiedTime(), "2013-08-30 00:00:00");
	}
	
	private static double round(double value, int scale) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(scale, BigDecimal.ROUND_HALF_UP);
		double d = bd.doubleValue();
		bd = null;
		return d;
	}		
	
	public static void main(String[] args) throws ParseException {		
		SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
		System.out.println(format.parse("Jul 08 2010"));

		format = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss");
		System.out.println(format.parse("2007/3/7_20:00:00"));		
		
		String collectData = "10493952||2013/4/11_3:3:14||d:\\temp03.ora";
		String[] lines = collectData.split("\n");
		List result = new ArrayList();
		for (int index = 0; index < lines.length; index++) {
			String[] fields = lines[index].split("\\|\\|");
			if (fields.length < 3)
				continue;
			CLIFileMonitorCondition row = new CLIFileMonitorCondition();
			row.setFilepath(fields[2]);
			row.setModifiedTime(fields[1]);
			double size = Double.parseDouble(fields[0]) / (1024 * 1024);			
			BigDecimal bd = new BigDecimal(size);
			bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
			System.out.println(bd.doubleValue());
			row.setSize(bd.doubleValue());
			result.add(row);
		}
		System.out.println(result);
	}
}
