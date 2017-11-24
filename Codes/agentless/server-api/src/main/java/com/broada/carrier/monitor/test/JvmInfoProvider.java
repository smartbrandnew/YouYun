package com.broada.carrier.monitor.test;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.SystemProperties;
import com.broada.component.utils.runcheck.RuntimeInfoEntry;
import com.broada.component.utils.runcheck.RuntimeInfoProvider;
import com.broada.component.utils.text.Unit;
import com.broada.carrier.monitor.test.JmxJvmInfoProvider;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JvmInfoProvider implements RuntimeInfoProvider {
  public static final String NAME = "JVM";
  private static final Log logger = LogFactory.getLog(JvmInfoProvider.class);
  private long startTime;
  private long maxHeapUsed;
  private long maxNonHeapUsed;
  private int maxThreads;
  private int outThreadsStackIncrNum;
  private int lastThreadCount;
  private RuntimeInfoEntry[] entries;

  public JvmInfoProvider() {
    this(getThreshold("runcheck.jvm.maxHeapUsage", 95, getHeapSize()), getThreshold("runcheck.jvm.maxNonHeapUsage", 95, getNonHeapSize()), SystemProperties.get("runcheck.jvm.maxThreads", 999), SystemProperties.get("runcheck.jvm.outThreadsStackIncrNum", 10), SystemProperties.get("runcheck.jvm.lastThreadCount", 20));
  }

  public JvmInfoProvider(long maxHeapUsed, long maxNonHeapUsed, int maxThreads, int outThreadsStackIncrNum, int lastThreadCount) {
    this.startTime = System.currentTimeMillis();
    this.maxHeapUsed = maxHeapUsed;
    this.maxNonHeapUsed = maxNonHeapUsed;
    this.maxThreads = maxThreads;
    this.outThreadsStackIncrNum = outThreadsStackIncrNum;
    this.lastThreadCount = lastThreadCount;
    new JmxJvmInfoProvider(this);
  }

  public long getRunTime() {
    return System.currentTimeMillis() - this.startTime;
  }

  public RuntimeInfoEntry[] getRuntimeInfo() {
    if (this.entries == null) {
      this.entries = new RuntimeInfoEntry[]{new RuntimeInfoEntry("运行时间", Unit.ms.formatPrefer((double)this.getRunTime())), new RuntimeInfoEntry("堆大小（当前/阈值）", (Object)null), new RuntimeInfoEntry("非堆大小（当前/阈值）", (Object)null), new RuntimeInfoEntry("加载类数", (Object)null), new RuntimeInfoEntry("线程数量（当前/阈值）", (Object)null)};
    }

    long heapSize = getHeapUsed();
    long nonHeapSize = getNonHeapUsed();
    int threadCount = getThreadCount();
    this.entries[0].setValue(Unit.ms.formatPrefer((double)(System.currentTimeMillis() - this.startTime)));
    this.entries[1].setValue(Unit.B.formatPrefer((double)heapSize) + "/" + Unit.B.formatPrefer((double)this.maxHeapUsed));
    this.entries[1].setNormal(heapSize < this.maxHeapUsed);
    this.entries[2].setValue(Unit.B.formatPrefer((double)nonHeapSize) + "/" + Unit.B.formatPrefer((double)this.maxNonHeapUsed));
    this.entries[2].setNormal(nonHeapSize < this.maxNonHeapUsed);
    this.entries[3].setValue(getLoaderClassCount());
    this.entries[4].setValue(threadCount + "/" + this.maxThreads);
    this.entries[4].setNormal(threadCount < this.maxThreads);
    if (this.entries[4].isNormal()) {
      this.entries[4].setNormal(canCreateThread());
      if (this.entries[4].isNormal()) {
        this.entries[4].setDescr((String)null);
      } else {
        this.entries[4].setDescr("无法创建新线程");
      }
    } else {
      this.entries[4].setDescr("线程数量超阈值");
    }

    boolean outThreadsStack = threadCount - this.lastThreadCount >= this.outThreadsStackIncrNum;
    if (outThreadsStack) {
      logger.info(String.format("线程增长[上次：%d 本次：%d]超过指定阈值[%d]，将输出线程栈", this.lastThreadCount, threadCount, this.outThreadsStackIncrNum));
      this.lastThreadCount = threadCount;
    }

    logThreads(outThreadsStack);
    return this.entries;
  }

  private static boolean canCreateThread() {
    try {
      (new JvmInfoProvider.JvmTestThread()).start();
      return true;
    } catch (Throwable var1) {
      ErrorUtil.warn(logger, "创建测试线程失败", var1);
      return false;
    }
  }

  private static void logThreads(boolean outThreadsStack) {
    try {
      if (outThreadsStack) {
        logger.info(dumpThreads(true));
      } else if (logger.isDebugEnabled()) {
        logger.debug(dumpThreads(true));
      } else if (logger.isInfoEnabled()) {
        logger.info(dumpThreads(false));
      }
    } catch (Throwable var2) {
      ErrorUtil.warn(logger, "线程列表记录失败", var2);
    }

  }

  private static long getThreshold(String name, int defPercent, long total) {
    String value = System.getProperty(name);
    if (value != null) {
      try {
        long num = Long.parseLong(value.substring(0, value.length() - 1));
        value = value.trim().toLowerCase();
        if (value.endsWith("%")) {
          return (long)((double)num / 100.0D * (double)total);
        }

        if (value.endsWith("m")) {
          return num * 1024L * 1024L;
        }
      } catch (NumberFormatException var7) {
        ErrorUtil.warn(logger, String.format("系统属性读取失败，将使用默认值[%s=%s%%]", name, defPercent), var7);
      }
    }

    return (long)((double)defPercent / 100.0D * (double)total);
  }

  public static long getHeapSize() {
    return Runtime.getRuntime().maxMemory();
  }

  public static long getHeapUsed() {
    return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }

  public static long getHeapFree() {
    return getHeapSize() - getHeapUsed();
  }

  public static double getHeapUsage() {
    return (double)getHeapUsed() * 100.0D / (double)getHeapSize();
  }

  public static long getNonHeapUsed() {
    return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
  }

  public static int getThreadCount() {
    return ManagementFactory.getThreadMXBean().getThreadCount();
  }

  public static long getNonHeapSize() {
    return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getMax();
  }

  public static double getNonHeapUsage() {
    long used = getNonHeapUsed();
    long max = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getMax();
    return (double)used * 100.0D / (double)max;
  }

  public String getName() {
    return "JVM";
  }

  public static String dumpThreads(boolean includeStack) {
    StringBuilder sb = new StringBuilder();
    Thread[] threads = findAllThreads();
    sb.append("线程总数：").append(threads.length);

    for(int i = 0; i < threads.length; ++i) {
      try {
        Thread thread = threads[i];
        sb.append("\n").append(i + 1).append(". ");
        dumpThread(thread, sb, includeStack);
      } catch (Throwable var5) {
        sb.append("\n").append(i + 1).append(". 线程信息dump错误：").append(var5);
      }
    }

    return sb.toString();
  }

  public static Thread[] findAllThreads() {
    ThreadGroup group = Thread.currentThread().getThreadGroup();

    ThreadGroup topGroup;
    for(topGroup = group; group != null; group = group.getParent()) {
      topGroup = group;
    }

    int estimatedSize = topGroup.activeCount() * 2;
    Thread[] slackList = new Thread[estimatedSize];
    int actualSize = topGroup.enumerate(slackList);
    Thread[] list = new Thread[actualSize];
    System.arraycopy(slackList, 0, list, 0, actualSize);
    Arrays.sort(list, new Comparator<Thread>() {
      public int compare(Thread o1, Thread o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return list;
  }

  public static void dumpThread(Thread thread, StringBuilder sb, boolean includeStack) {
    sb.append(String.format("线程[id: %-3d %-15s%s", thread.getId(), thread.getState(), thread.getName()));
    if (includeStack) {
      sb.append("]：");
      StackTraceElement[] stack = (StackTraceElement[])Thread.getAllStackTraces().get(thread);
      if (stack != null) {
        StackTraceElement[] arr$ = stack;
        int len$ = stack.length;

        for(int i$ = 0; i$ < len$; ++i$) {
          StackTraceElement s = arr$[i$];
          sb.append("\n\t    at " + s);
        }
      }
    } else {
      sb.append("]");
    }

  }

  public long getMaxHeapUsed() {
    return this.maxHeapUsed;
  }

  public void setMaxHeapUsed(long maxHeapUsed) {
    this.maxHeapUsed = maxHeapUsed;
  }

  public long getMaxNonHeapUsed() {
    return this.maxNonHeapUsed;
  }

  public void setMaxNonHeapUsed(long maxNonHeapUsed) {
    this.maxNonHeapUsed = maxNonHeapUsed;
  }

  public int getMaxThreads() {
    return this.maxThreads;
  }

  public void setMaxThreads(int maxThreads) {
    this.maxThreads = maxThreads;
  }

  public static int getLoaderClassCount() {
    return ManagementFactory.getClassLoadingMXBean().getLoadedClassCount();
  }

  private static class JvmTestThread extends Thread {
    public JvmTestThread() {
      super(JvmInfoProvider.JvmTestThread.class.getSimpleName());
      this.setDaemon(false);
    }

    public void run() {
      if (JvmInfoProvider.logger.isDebugEnabled()) {
        JvmInfoProvider.logger.debug("测试线程运行");
      }

    }
  }
}
