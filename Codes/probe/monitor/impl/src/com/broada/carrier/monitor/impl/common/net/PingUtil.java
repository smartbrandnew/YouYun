package com.broada.carrier.monitor.impl.common.net;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.common.net.IPUtil;
import com.broada.carrier.monitor.impl.common.net.icmp.IcmpPacket;
import com.broada.component.jnis.nsock.Socket;
import com.broada.component.jnis.nsock.SocketAPI;
import com.broada.component.jnis.nsock.SocketAddress;
import com.broada.component.jnis.nsock.SocketConst;
import com.broada.component.jnis.nsock.SocketSet;

public class PingUtil {
	private static final Log logger = LogFactory.getLog(PingUtil.class);

	/**
	 * 默认ping
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean ping(String ip) {
		return ping(ip, 3, 100, 3000, null);
	}

	/**
	 * 默认ping(需要时间)
	 * 
	 * @param ip
	 * @param ttl
	 * @return
	 */
	public static boolean ping(String ip, StringBuilder ttl) {
		return ping(ip, 3, 100, 3000, ttl);
	}

	/**
	 * ping命令实现(icmp包)
	 * 
	 * @param ip
	 * @param times
	 * @param interval
	 * @param timeout
	 * @param ttl
	 * @return
	 */
	public static synchronized boolean ping2(String ip, int times, long interval, long timeout, StringBuilder ttl) {
		boolean isIPv4 = IPUtil.isIPv4Address(ip);
		boolean isIPv6 = IPUtil.isIPv6Address(ip);
		if (!isIPv4 && !isIPv6) {
			throw new RuntimeException("无效的IP地址");
		}
		boolean isSuccess = false;
		SocketAddress address = new SocketAddress(ip);
		IcmpPacket sendPacket = new IcmpPacket();
		byte[] icmpData = new String("Broada Probe ICMP").getBytes();
		sendPacket.setData(icmpData);

		Socket socket = null;
		SocketSet readset = null;
		byte[] data = null;
		// 连续ping包次数
		for (int i = 0; i < times; i++) {
			try {
				if (isIPv4) {
					socket = new Socket(SocketConst.AF_INET, SocketConst.SOCK_RAW, SocketConst.IPPROTO_ICMP);
					sendPacket.setType(IcmpPacket.TYPE_ECHO_REQUEST);
				} else {
					socket = new Socket(SocketConst.AF_INET6, SocketConst.SOCK_RAW, SocketConst.IPPROTO_ICMPV6);
					sendPacket.setType(IcmpPacket.TYPE_IPV6_ECHO_REQUEST);
				}
				data = sendPacket.decode();
				readset = new SocketSet();
				readset.setSockets(new long[] { socket.getSocket() });

				long startTime = System.currentTimeMillis();
				if (socket.sendto(data, 0, address) == data.length) {
					int ret = SocketAPI.select(readset, null, null, timeout);
					if (ret > 0) {
						if (null != ttl) {
							long span = System.currentTimeMillis() - startTime;
							span = 0 == span ? 1 : span;
							ttl.append(span).append(",");
							if(logger.isDebugEnabled())
								logger.debug(ip + " ping耗时：" + span);
						}
						isSuccess = true;
					} else if (ret <= 0) {
						if(logger.isDebugEnabled())
							logger.debug(ip + " ping不可用");
						try {
							Thread.sleep(interval);
						} catch (InterruptedException e) {
							throw new RuntimeException(e.getMessage());
						}
					}
				}
			
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			} finally {
				if (null != socket) {
					socket.dispose();
				}
			}
			
		}
		if(!isSuccess)
			throw new RuntimeException("套接字可读Select错误");
		
		int total = 0;
		String [] used = ttl.toString().split(",");
		for(String s : used){
			total += Integer.valueOf(s);
		}
		ttl.delete(0, ttl.length());
		ttl.append(String.valueOf(total/used.length));
		return isSuccess;
	}
	
	/**
	 * ping命令实现(icmp包)
	 * 
	 * @param ip
	 * @param times
	 * @param interval
	 * @param timeout
	 * @param ttl
	 * @return
	 */
	public static boolean ping(String ip, int times, long interval, long timeout, StringBuilder ttl) {
		boolean isIPv4 = IPUtil.isIPv4Address(ip);
		boolean isIPv6 = IPUtil.isIPv6Address(ip);
		if (!isIPv4 && !isIPv6) {
			throw new RuntimeException("无效的IP地址");
		}
		
		BufferedReader in = null;
		Runtime r = Runtime.getRuntime(); // 将要执行的ping命令,此命令是windows格式的命令
		String pingCommand = null;
		String os = getOS();
		if(os.equals("windows"))
			pingCommand = "ping " + ip + " -n " + times + " -w " + timeout;  
		else if(os.equals("linux"))
			pingCommand = "ping " + ip + " -c " + times + " -w " + timeout;
		try { // 执行命令并获取输出  
			Process p = r.exec(pingCommand);
			if (p == null) {
				return false;
			}
			in = new BufferedReader(new InputStreamReader(p.getInputStream())); // 逐行检查输出,计算类似出现=23ms TTL=62字样的次数  
			String line = null;
			int result = -1;
			while ((line = in.readLine()) != null) {
				result = isIPv4 ? getCheckResultV4(line) : getCheckResultV6(line);
				if(result != -1)
					ttl.append(result + ",");
			}
			
			if(ttl.toString().equals(""))
				return false;
			
			int total = 0;
			String [] used = ttl.toString().split(",");
			for(String s : used){
				total += Integer.valueOf(s);
			}
			ttl.delete(0, ttl.length());
			ttl.append(String.valueOf(total / used.length));
			return true;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * IPv4
	 * 若line含有=18ms TTL=16字样,说明已经ping通,返回耗时毫秒数,否则返回-1
	 * @param line
	 * @return
	 */
	private static int getCheckResultV4(String line) {
		Pattern pattern = Pattern.compile("[TTL|ttl]=\\d+", Pattern.CASE_INSENSITIVE);  
        Matcher matcher = pattern.matcher(line);  
		while (matcher.find()) {
			String time = null;
			// windows
			if(line.contains("<"))
				time = line.substring(line.indexOf("<") + 1, line.indexOf("ms"));
			// Linux
			else
				time = line.substring(line.indexOf("time=") + 5, line.indexOf("ms"));
			return Double.valueOf(time.trim()).intValue();
		}
		return -1;
	}
	
	/**
	 * IPv6
	 * 若line含有=18ms TTL=16字样,说明已经ping通,返回耗时毫秒数,否则返回-1
	 * @param line
	 * @return
	 */
	private static int getCheckResultV6(String line) {  
		Pattern pattern = Pattern.compile("(\\d+ms)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		while (matcher.find()) {
			pattern = Pattern.compile("(\\s+)(\\d+ms)", Pattern.CASE_INSENSITIVE);
			Matcher matcher2 = pattern.matcher(line);
			while(matcher2.find())
				return -1;
			
			String s = matcher.group();
			s = s.substring(0, s.indexOf("ms"));
			return Integer.valueOf(s);
		}
		return -1;
	}
	
	/**
	 * 获取jdk所在操作系统类别
	 * @return
	 */
	private static String getOS(){
		String osName = System.getProperty("os.name");
		if(osName.toLowerCase().contains("windows"))
			return "windows";
		else
			return "linux";
	}
	
}