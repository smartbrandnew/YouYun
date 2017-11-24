package com.broada.carrier.monitor.test;

import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.SystemProperties;
import com.broada.component.utils.lang.ThreadUtil;
import com.broada.component.utils.runcheck.RuntimeInfoEntry;
import com.broada.component.utils.runcheck.RuntimeInfoProvider;
import com.broada.component.utils.text.DateUtil;
import com.broada.component.utils.text.Unit;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RuntimeChecker {
  private static final int EXIT_CODE = 1;
  private static final Log logger = LogFactory.getLog(RuntimeChecker.class);
  private static RuntimeChecker instance;
  private RuntimeChecker.ProviderEntry[] providers;
  private RuntimeInfoProvider[] externProviders;
  private long lastWarnTime;
  private long maxWarnDuration;
  private int checkInterval;
  private boolean checkSelf;
  private Thread selfThread;
  private Thread checkThread;
  private boolean running;
  private long lastCheckTime;
  private String app;
  private boolean warn;
  private Map<String, RuntimeInfoEntry[]> entries;

  public static RuntimeChecker getDefault() {
    if (instance == null) {
      Class var0 = RuntimeChecker.class;
      synchronized(RuntimeChecker.class) {
        if (instance == null) {
          instance = new RuntimeChecker();
        }
      }
    }

    return instance;
  }

  public RuntimeChecker(String app, RuntimeInfoProvider[] providers, long maxWarnDuration, int checkInterval, boolean checkSelf) {
    this.running = false;
    this.entries = new HashMap();
    this.app = app;
    this.initProviders(providers);
    this.maxWarnDuration = maxWarnDuration * 1000L;
    this.checkInterval = checkInterval * 1000;
    this.checkSelf = checkSelf;
    new JmxRuntimeChecker(this);
  }

  private void initProviders(RuntimeInfoProvider[] providers) {
    if (providers != null) {
      this.externProviders = providers;
      this.providers = new RuntimeChecker.ProviderEntry[providers.length];

      for(int i = 0; i < providers.length; ++i) {
        this.providers[i] = new RuntimeChecker.ProviderEntry(providers[i]);
      }

    }
  }

  public RuntimeChecker() {
    this(SystemProperties.get("runcheck.app", "app"), (RuntimeInfoProvider[])null, (long)SystemProperties.get("runcheck.maxWarnDuration", 300), SystemProperties.get("runcheck.checkInterval", 300), SystemProperties.get("runcheck.checkSelf", "true").equals("true"));
  }

  public synchronized void startup() {
    if (!this.isRunning()) {
      if (this.checkInterval <= 0) {
        logger.debug("运行检查无法启动，检查周期必须大于0");
      } else {
        this.initProviders();
        if (this.providers != null && this.providers.length != 0) {
          logger.info("系统运行自检启动");
          this.running = true;
          this.lastWarnTime = 0L;
          this.heartbeat();
          this.checkThread = ThreadUtil.createThread(new RuntimeChecker.RuntimeCheckThread(), "RuntimeChecker.worker");
          this.checkThread.start();
          this.selfThread = ThreadUtil.createThread(new RuntimeChecker.SelfCheckThread(), "RuntimeChecker.self");
          this.selfThread.start();
        } else {
          logger.debug("运行检查无法启动，没有任何运行信息提供者");
        }
      }
    }
  }

  public boolean isRunning() {
    return this.running;
  }

  private void initProviders() {
    if (this.providers == null) {
      ServiceLoader<RuntimeInfoProvider> serviceLoader = ServiceLoader.load(RuntimeInfoProvider.class);
      List<RuntimeInfoProvider> providers = new ArrayList();
      Iterator i$ = serviceLoader.iterator();

      while(i$.hasNext()) {
        RuntimeInfoProvider provider = (RuntimeInfoProvider)i$.next();
        providers.add(provider);
      }

      this.initProviders((RuntimeInfoProvider[])providers.toArray(new RuntimeInfoProvider[providers.size()]));
    }
  }

  public synchronized void shutdown() {
    this.running = false;
    if (this.checkThread != null) {
      this.checkThread.interrupt();
      this.checkThread = null;
    }

    if (this.selfThread != null) {
      this.selfThread.interrupt();
      this.selfThread = null;
    }

    if (logger.isDebugEnabled()) {
      logger.debug("运行检查关闭");
    }

  }

  private void errorExit(String message, int exitCode, String dumpRuntimeInfo) {
    String filename = dumpErrorExit(this.app, message, dumpRuntimeInfo);
    if (filename != null) {
      message = message + "\nJVM日志：" + filename;
    }

    ErrorUtil.exit(logger, message, (Throwable)null, exitCode);
  }

  static String dumpErrorExit(String app, String message, String lastRuntimSummary) {
    try {
      File file = new File(System.getProperty("user.dir"), "logs/" + app + "-crash-" + DateUtil.format(new Date(), "yyMMdd-HHmmss") + ".log");
      file.getParentFile().mkdirs();
      FileWriter writer = new FileWriter(file);
      writer.write(message);
      if (lastRuntimSummary != null) {
        writer.write("\n\n");
        writer.write(lastRuntimSummary);
        writer.write("\n");
      }

      writer.write(JvmInfoProvider.dumpThreads(true));
      writer.close();
      return file.getAbsolutePath();
    } catch (Throwable var5) {
      ErrorUtil.warn(logger, "生成错误退出文件失败", var5);
      return null;
    }
  }

  public RuntimeInfoProvider[] getProviders() {
    return this.externProviders;
  }

  public RuntimeInfoEntry[] getEntries(String providerName) {
    return (RuntimeInfoEntry[])this.entries.get(providerName);
  }

  private void heartbeat() {
    this.lastCheckTime = System.currentTimeMillis();
  }

  public String dumpRuntimeInfo() {
    this.warn = false;

    try {
      StringBuilder sb = new StringBuilder();
      sb.append("运行汇总信息：");
      RuntimeChecker.ProviderEntry[] arr$ = this.providers;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
        RuntimeChecker.ProviderEntry provider = arr$[i$];

        try {
          RuntimeInfoEntry[] items = provider.getRuntimeInfo();
          this.entries.put(provider.getName(), items);
          if (items != null) {
            sb.append("\n").append(provider.getDescr()).append("：");
            RuntimeInfoEntry[] arr1$ = items;
            int len1$ = items.length;

            for(int j = 0; j < len1$; ++j) {
              RuntimeInfoEntry entry = arr1$[j];
              sb.append("\n\t").append(entry.getName()).append(": ").append(entry.getValue());
              if (!entry.isNormal()) {
                sb.append("\t警告");
                this.warn = true;
              }

              if (entry.getDescr() != null) {
                sb.append("\t").append(entry.getDescr());
              }
            }
          }
        } catch (Throwable var11) {
          ErrorUtil.warn(logger, String.format("运行汇总提供失败，将忽略此提供者[%s]", provider), var11);
        }
      }

      return sb.toString();
    } catch (Throwable var12) {
      ErrorUtil.warn(logger, "运行汇总提供失败", var12);
      return ErrorUtil.createMessage("运行汇总提供失败", var12);
    }
  }

  public long getMaxWarnDuration() {
    return this.maxWarnDuration;
  }

  public void setMaxWarnDuration(long maxWarnDuration) {
    this.maxWarnDuration = maxWarnDuration;
  }

  public int getCheckInterval() {
    return this.checkInterval;
  }

  public void setCheckInterval(int checkInterval) {
    this.checkInterval = checkInterval;
    this.heartbeat();
    synchronized(this) {
      this.notifyAll();
    }
  }

  public boolean isCheckSelf() {
    return this.checkSelf;
  }

  public void setCheckSelf(boolean checkSelf) {
    this.checkSelf = checkSelf;
  }

  private static class ProviderEntry {
    private RuntimeInfoProvider provider;
    private int runCount;
    private long runTime;

    public ProviderEntry(RuntimeInfoProvider provider) {
      this.provider = provider;
    }

    public String getDescr() {
      return String.format("%s[%d次 平均耗时：%dms]", this.getName(), this.runCount, this.getAvgRunTime());
    }

    public RuntimeInfoEntry[] getRuntimeInfo() {
      long time = System.currentTimeMillis();
      RuntimeInfoEntry[] result = this.provider.getRuntimeInfo();
      this.addRun(System.currentTimeMillis() - time);
      return result;
    }

    public String getName() {
      return this.provider.getName();
    }

    private void addRun(long runTime) {
      ++this.runCount;
      this.runTime += runTime;
    }

    private long getAvgRunTime() {
      return this.runCount == 0 ? 0L : this.runTime / (long)this.runCount;
    }

    public String toString() {
      return this.getDescr();
    }
  }

  private class RuntimeCheckThread implements Runnable {
    private RuntimeCheckThread() {
    }

    public void run() {
      while(true) {
        if (RuntimeChecker.this.isRunning()) {
          RuntimeChecker.this.heartbeat();

          try {
            this.check();
          } catch (Throwable var4) {
            ErrorUtil.warn(RuntimeChecker.logger, "运行检查线程执行错误，将忽略本次执行", var4);
          }

          RuntimeChecker var1 = RuntimeChecker.this;
          synchronized(RuntimeChecker.this) {
            try {
              RuntimeChecker.this.wait((long)RuntimeChecker.this.checkInterval);
              continue;
            } catch (InterruptedException var5) {
              ;
            }
          }
        }

        return;
      }
    }

    private void check() {
      String dumpRuntimeInfo = RuntimeChecker.this.dumpRuntimeInfo();
      RuntimeChecker.logger.info(dumpRuntimeInfo);
      if (RuntimeChecker.this.warn) {
        long now = System.currentTimeMillis();
        if (RuntimeChecker.this.lastWarnTime <= 0L) {
          RuntimeChecker.this.lastWarnTime = now;
          RuntimeChecker.logger.warn("系统运行出现异常。" + JvmInfoProvider.dumpThreads(true));
        } else {
          long time = now - RuntimeChecker.this.lastWarnTime;
          if (time >= RuntimeChecker.this.maxWarnDuration) {
            RuntimeChecker.this.errorExit(String.format("系统运行存在长时间[%s]异常", Unit.ms.formatPrefer((double)time)), 1, dumpRuntimeInfo);
          }
        }
      } else if (RuntimeChecker.this.lastWarnTime > 0L) {
        RuntimeChecker.logger.info("系统运行恢复正常");
        RuntimeChecker.this.lastWarnTime = 0L;
      }

    }
  }

  private class SelfCheckThread implements Runnable {
    private SelfCheckThread() {
    }

    public void run() {
      while(RuntimeChecker.this.isRunning()) {
        RuntimeChecker var1 = RuntimeChecker.this;
        synchronized(RuntimeChecker.this) {
          try {
            RuntimeChecker.this.wait((long)RuntimeChecker.this.checkInterval);
          } catch (InterruptedException var4) {
            return;
          }
        }

        if (RuntimeChecker.this.checkSelf) {
          long time = System.currentTimeMillis() - RuntimeChecker.this.lastCheckTime;
          if (time > (long)(RuntimeChecker.this.checkInterval * 3)) {
            RuntimeChecker.this.errorExit(String.format("运行检查心跳长时间停止[%s]，当前JVM已出现僵死情况", Unit.ms.formatPrefer((double)time)), 1, (String)null);
          } else if (time > (long)(RuntimeChecker.this.checkInterval * 2)) {
            StringBuilder sb = new StringBuilder();
            sb.append("运行检查心跳不及时，线程情况：");
            JvmInfoProvider.dumpThread(RuntimeChecker.this.checkThread, sb, true);
            RuntimeChecker.logger.warn(sb);
          }
        }
      }

    }
  }
}
