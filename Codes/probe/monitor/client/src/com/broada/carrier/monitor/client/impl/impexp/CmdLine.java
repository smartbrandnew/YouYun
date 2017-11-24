package com.broada.carrier.monitor.client.impl.impexp;

import java.io.InputStream;
import java.util.Date;

import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpFile;
import com.broada.carrier.monitor.client.impl.impexp.error.NeedOptionException;
import com.broada.carrier.monitor.common.util.FileUtil;
import com.broada.component.utils.lang.SimpleProperties;
import com.broada.component.utils.text.DateUtil;

public class CmdLine {
	public static void main(String[] args) {
		if (args.length <= 1)
			printUsage();

		String command = args[0];
		SimpleProperties options = parseOptions(args, 1);

		try {
			if (command.equalsIgnoreCase("exp-numen"))
				expNumen(options);
			else if (command.equalsIgnoreCase("exp-carrier"))
				expCarrier(options);
			else if (command.equalsIgnoreCase("imp"))
				imp(options);
			else
				printUsage();
		} catch (Throwable e) {
			String errorMessage = null;
			if (e instanceof NeedOptionException) {
				errorMessage = "缺少命令参数："
						+ ((NeedOptionException) e).getOption();
			}
			if (errorMessage == null)
				e.printStackTrace();
			else
				System.out.println(errorMessage);
		}
		System.exit(0);
	}

	private static void expCarrier(SimpleProperties options) {
		String ip = options.get("ip", "127.0.0.1");
		int port = Integer.parseInt(options.get("port", "8890"));
		String username = options.get("username", "admin");
		String password = options.get("password", "admin");
		String filename = check(options, "file");
		if (filename == null)
			filename = "carrier-exp-"
					+ DateUtil.format(new Date(), DateUtil.PATTERN_YYYYMMDD)
					+ ".xls";

		Exporter exporter = new ExporterCarrier(ip, port, username, password);
		ImpExpFile file = exporter.exp();
		ImpExpFileWriter writer = new ImpExpFileWriter();
		writer.write(file, filename);
	}

	private static void imp(SimpleProperties options) {
		String ip = options.get("ip", "127.0.0.1:8890");
		String username = options.get("username", "admin");
		String password = options.get("password", "admin");
		String filename = check(options, "file");

		ImpExpFileReader reader = new ImpExpFileReader();
		ImpExpFile file = reader.read(filename);
		Importer imper = new Importer(ip, username, password, file);
		imper.imp();
	}

	private static String check(SimpleProperties options, String key) {
		try {
			return options.check(key);
		} catch (Throwable e) {
			throw new NeedOptionException(key, e);
		}
	}

	private static void expNumen(SimpleProperties options) {
		String url = options.get("url");
		if (url == null) {
			String ip = check(options, "ip");
			int port = options.get("port", 1521);
			String sid = options.get("sid", "orcl");
			url = String.format("jdbc:oracle:thin:@%s:%d:%s", ip, port, sid);
		}
		String username = check(options, "username");
		String password = options.get("password", "admin");
		String filename = options.get("file");
		if (filename == null)
			filename = "numen-exp-"
					+ DateUtil.format(new Date(), DateUtil.PATTERN_YYYYMMDD)
					+ ".xls";

		Exporter exporter = new ExporterNumen(url, username, password);
		ImpExpFile file = exporter.exp();
		ImpExpFileWriter writer = new ImpExpFileWriter();
		writer.write(file, filename);
	}

	private static SimpleProperties parseOptions(String[] args, int start) {
		SimpleProperties options = new SimpleProperties();
		for (int i = start; i < args.length; i++) {
			String item = args[i];
			int pos = item.indexOf("=");
			if (pos < 0)
				printUsage();

			String key = item.substring(0, pos).toLowerCase();
			String value = item.substring(pos + 1);
			options.set(key, value);
		}
		return options;
	}

	private static void printUsage() {
		InputStream is = CmdLine.class.getResourceAsStream("help.txt");
		String help = FileUtil.readString(is, "utf-8");
		System.out.println(help);
		System.exit(1);
	}
}
