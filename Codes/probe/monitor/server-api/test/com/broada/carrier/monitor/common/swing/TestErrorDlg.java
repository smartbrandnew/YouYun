package com.broada.carrier.monitor.common.swing;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class TestErrorDlg {
	public static void main(String[] args) throws Exception {
		InputStream is = ErrorDlg.class.getResourceAsStream("ErrorMessage.txt");
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] data = new byte[1024];
			while (true) {
				int len = is.read(data);
				if (len <= 0)
					break;
				os.write(data, 0, len);
			}
			String str = new String(os.toByteArray());
			ErrorDlg.show(str);
		} finally {
			is.close();
		}
	}
}
