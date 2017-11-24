package com.broada.carrier.monitor.impl.common;

import java.io.IOException;

public class ProcessUtil {
	public static final int READ_INTERVAL = 100;
	public static final int BUFFER_SIZE = 5 * 1024; 
	
	/**
	 * ��ȡһ��������
	 * @param process
	 * @param output
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void readOutput(Process process, StringBuffer output) throws IOException, InterruptedException {
		byte[] data = new byte[BUFFER_SIZE];
		readOutput(process, output, data);
	}
	
	private static void readOutput(Process process, StringBuffer output, byte[] buffer) throws IOException, InterruptedException {
		boolean hadOutput;
		do {
			hadOutput = false;
			
			if (process.getErrorStream().available() > 0) {
				int len = process.getErrorStream().read(buffer);
				if (len > 0) {
					output.append(new String(buffer, 0, len));	
					hadOutput = true;
				}
			}
			
			if (process.getInputStream().available() > 0) {
				int len = process.getInputStream().read(buffer);
				if (len > 0) {
					output.append(new String(buffer, 0, len));	
					hadOutput = true;
				}
			}			
		} while (hadOutput);
	}	
	
	/**
	 * �ж�һ������Ƿ���ֹ
	 * @param process
	 * @return
	 */
	public static boolean isProcessTerminated(Process process) {
		try {
			process.exitValue();
			return true;
		} catch (IllegalThreadStateException err) {
			return false;
		}
	}

	/**
	 * ��ȡһ����̵ı�׼�������������ֱ����̽���
	 * @param process
	 * @return
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static String readOutputUntilTerminal(Process process) throws IOException, InterruptedException {
		StringBuffer output = new StringBuffer(BUFFER_SIZE);
		byte[] data = new byte[BUFFER_SIZE];
		do {
			ProcessUtil.readOutput(process, output, data);
			Thread.sleep(READ_INTERVAL);
		} while (!ProcessUtil.isProcessTerminated(process));
		ProcessUtil.readOutput(process, output, data);	
		
		return output.toString();
	}
}
