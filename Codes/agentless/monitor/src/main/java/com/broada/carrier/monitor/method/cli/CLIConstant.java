package com.broada.carrier.monitor.method.cli;

public interface CLIConstant {
	public final static String DIR = System.getProperty("user.dir");
  /**
   * 采用digester来解析CLI配置文件，这里定义了解析的规则
   */
  public final static String DIGESTER_RULES = DIR + "/conf/cli-config/rule/digester-rules.xml";

  public final static String CLI_CONFIG = DIR + "/conf/cli-config/";
  
  /**
   * 增加客户自定义CLI配置文件存放路径
   */  
  public final static String CLI_CUST_CONFIG = DIR + "/conf/cli-config/custom/";
  /**
   * 放置cli错误提示中文化配置文件
   */
  public final static String CLI_MESSAGES_ZH_CONFIG = DIR + "/conf/cli-config/cliMessages_zh.properties";
  /**
   * 分行符号
   */
  public final static String LINE_SPILTTER = "\n";

  /**
   * 解析表格数据
   */
  public final static String DATATYPE_TABLE = "table";

  /**
   * 解析属性数据
   */
  public final static String DATATYPE_PROPERTY = "property";
  
  /**
   * 解析多行快状数据
   */
  public final static String DATATYPE_BLOCK = "block";

  /**
   * 预处理BeanShell中的采集到的结果名字
   */
  public final static String BSH_COLLECT_DATA = "collectData";

  /**
   * 预处理BeanShell中的解析规则
   */
  public final static String BSH_PARSERRULE = "parserRule";

  /**
   * 用来标记采用何种方式获取数据(Agent、Telnet、SSH)
   */
  public final static String SESSION_AGENT = "agent";

  public final static String SESSION_TELNET = "telnet";

  public final static String SESSION_SSH = "ssh";

  public final static String SESSION_WMI = "wmi";

  /** options选项* */
  public final static String OPTIONS_SESSIONNAME = "sessionName";
	public final static String OPTIONS_REMOTEHOST = "remoteHost";
  public final static String OPTIONS_REMOTEPORT = "remotePort";
  public final static String OPTIONS_LOGINNAME = "loginName";
  public final static String OPTIONS_PASSWORD = "password";
  public final static String OPTIONS_LOGINTIMEOUT = "loginTimeout";
  public final static String OPTIONS_PROMPT = "prompt";
  public final static String OPTIONS_OS = "sysname";
  public final static String OPTIONS_OSVERSION = "sysversion";
  public final static String OPTIONS_LOGINPROMPT = "loginPrompt";
  public final static String OPTIONS_AGENTNAME = "agentName";
  public final static String OPTIONS_PASSWORDPROMPT = "passwdPrompt";
  public final static String OPTIONS_TERMINALTYPE = "terminalType";  

  public static final String RESULT_OS = "os";
  public final static String RESULT_VERSION = "sysversion";

  /**
   * 最大登录重试次数
   */
  public final static int MAX_TRYTIMES = 2;

  /**
   * 最大等待重试次数
   */
  public final static int MAX_RETRYTIMES = 5;

  /**
   * 等待时间(ms)
   */
  public final static int WAIT_TIME = 3000;

  /**
   * 监测CPU对应的命令名称
   */
  public final static String COMMAND_CPU = "cpu";

  /**
   * 监测内存总量对应的命令名称
   */
  public static final String COMMAND_TOTALMEMORY = "totalmemory";

  /**
   * 监测Memory对应的命令名称
   */
  public final static String COMMAND_MEMORY = "memory";

  /**
   * 文件监测
   */
  public static final String COMMAND_FILELIST = "ls";

  /**
   * iis运行信息监测
   */
  public static final String COMMAND_IISBASEINFO="baseinfo";
  
  /**
   * iis字节传输监测
   */
  public static final String COMMAND_IISTRANSFERBYTES="bytes";
  
  
  /**
   * iis文件传输监测
   */
  public static final String COMMAND_IISTRANSFERFILES="files";
  
  /**
   * iis连接监测
   */
  public static final String COMMAND_IISCONNS="connections";
  
  /**
   * iisWEB请求监测
   */
  public static final String COMMAND_IISWEBREQUEST="webrequest";
  
  /**
   * iis用户监测
   */
  public static final String COMMAND_IISUSERS="users";
  
  /**
   * 目录监测
   */
  public static final String COMMAND_DIRECTORY = "cd";
  
  /**
   * Windows注册用户监测
   */
  public static final String COMMAND_WIN_ACCOUNT_USERS = "winAccountUsers";

  /**
   * Windows登陆用户监测
   */
  public static final String COMMAND_WIN_LOGON_USERS = "winLogonUsers";
  
  /**
   * Windows事件日志监测
   */
  public static final String COMMAND_WIN_EVENTLOG = "winEventLog";
  
  /**
   * 获取当前登陆用户的命令
   */
  public static final String COMMAND_CURRENTUSER = "userinfo";

  /**
   * 获取历史登陆用户的命令
   */
  public static final String COMMAND_HISUSERINFO = "hisuserinfo";

  /**
   * 获取注册帐户的命令
   */
  public static final String COMMAND_USERACCOUNTS = "useraccounts";
  
  /**
   * 监测磁盘对应的命令名称
   */
  public static final String COMMAND_DISK = "disk";

  /**
   * 获取进程信息对应的命令名称
   */
  public static final String COMMAND_PROCESS = "process";

  /**
   * 获取进程状态对应的命令名称
   */
  public static final String COMMAND_PROCESSSTATE = "processstate";
  
  /**
   * 获取进程状态对应的命令名称
   */
  public static final String COMMAND_SYSTEMTIME = "systemtime";
  
  /**
   * 获取系统参数和状态
   */
  public static final String COMMAND_HOSTINFO = "hostinfo";
  
  /**
   * 获取网络接口状况
   */
  public static final String COMMAND_NETSTAT = "netstat";

  /**
   * 获取AIX系统最新15条错误日志
   */
  public static final String COMMAND_LATEST15 = "latest15";

  /**
   * 获取AIX系统错误日志的最新序列号
   */
  public static final String COMMAND_LATESTSEQ = "latestSeq";
  
  /**
   * 获取AIX系统错误日志的最新序列号
   */
  public static final String COMMAND_DETAIL = "detail";
  
  /**
   * 设备IO
   */
  public static final String COMMAND_IO = "io";

  /**
   * 获取HACMP的主备机IP配置是否一致
   */
  public final static String COMMAND_HACMP_MATCHIP = "hacmp4matchip";
  
  /**
   * 命令参数的开始符号,例如下面的命令列表 <br>
   * &lt;command&gt;cmd1&lt;/command&gt; <br>
   * &lt;command&gt;cmd2 #1&lt;/command&gt; <br>
   * #1表示命令cmd1执行的结果做为命令cmd2的输入
   */
  public final static String PARAM_START = "#";

  /**
   * 连接的健康状况
   */
  public static final int HEALTH = 10;
  
  public static final String INITIALIZE = "initialize";
  
  public static final String COMMAND_HACMP_NODESTATE = "hacmp4nodestate";
  
  public static final String COMMAND_HACMP_STATE = "hacmp4state";
  /**
   * db2通过agent方式获取数据库相关信息
   */
  public final static String DB2AGENT = "db2agent";
}
