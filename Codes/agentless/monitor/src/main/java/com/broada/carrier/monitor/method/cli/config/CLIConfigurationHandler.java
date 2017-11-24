package com.broada.carrier.monitor.method.cli.config;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.utils.StringUtil;
import com.broada.utils.cache.Cache;
import com.broada.utils.cache.OSCache;

/**
 * CLI配置文件处理入口
 * 
 * @author Eric Liu (liudh@broada.com.cn) huangjb 2007/12/05 通过version长度逐渐递减与配置表中的Category项比较取得最接近匹配版本
 */
public class CLIConfigurationHandler {

  private int selSysNameIndex = 0;// 提供给外部选择索引

  private static final Log logger = LogFactory.getLog(CLIConfigurationHandler.class);

  private List<CLIConfiguration> cliConfigurations = new ArrayList<CLIConfiguration>();

  /** 采用单例模式 */
  private static CLIConfigurationHandler handler;

  /** CLI配置的查询索引 */
  private CLIConfigurationIndex cliIndex;

  private VFSFileMonitor fileMonitor = null;
  
  static {
	  try {
		  handler = new CLIConfigurationHandler();
	  } catch (Throwable e) {
		  logger.warn("初始化失败", e);
	  }
  }

  private CLIConfigurationHandler() {
    synchronized (this) {
      if (fileMonitor == null) {
        fileMonitor = new VFSFileMonitor();
      }
    }
    loadCLIConfigs();
    fileMonitor.monitor(CLIConstant.CLI_CUST_CONFIG);
  }

  /**
   * 通过工厂单例方法获取Handler
   * 
   * @return 处理的Handler
   */
  public static CLIConfigurationHandler getInstance() {
    return handler;
  }

  /**
   * 加载配置文件，采用digester来解析配置文件
   */
  @SuppressWarnings("deprecation")
	private void loadConfigFile(String configFile) throws CLIException {
    File rules = new File(CLIConstant.DIGESTER_RULES);
    File input = new File(configFile);
    CollectionDefine cd = null;
    try {
      Digester digester = DigesterLoader.createDigester(rules.toURL());
      digester.setValidating(false);
      cd = (CollectionDefine) digester.parse(input);
    } catch (Exception e) {
      throw new CLIException("解析配置文件发生错误：" + configFile, e);
    }
    // 如果文件修改过了，就会重新加载配置文件(先删掉具有相同sysname的配置,然后再将加载后的配置对象加入到列表)
    synchronized (this) {
      List<CLIConfiguration> cf = cd.getCliConfigurations();
      Iterator<CLIConfiguration> iterator = cf.iterator();
      while (iterator.hasNext()) {
        CLIConfiguration cliConfiguration = iterator.next();

        CLIConfiguration clicf = getCLIConfigurationFromSysname(cliConfigurations, cliConfiguration.getSysname());

        if (clicf == null) {
          cliConfigurations.add(cliConfiguration);
        } else {
          recoverCategories(clicf, cliConfiguration);
          // clicf.getCategories().addAll(cliConfiguration.getCategories());
        }
      }

      if (cliIndex != null)
        cliIndex.close();
      cliIndex = CLIConfigurationIndex.buildCLIConfigurationIndex(cliConfigurations);
    }
  }

  /**
   * 根据categoryName,sysName,sysVersion进行覆盖
   * @param oldConfig
   * @param newConfig
   */
  private void recoverCategories(CLIConfiguration oldConfig, CLIConfiguration newConfig) {
    List<Category> oldCates = oldConfig.getCategories();
    List<Category> newCates = newConfig.getCategories();
    for (int m = 0; m < newCates.size(); m++) {
      Category newCate = newCates.get(m);
      if (oldCates.contains(newCate)) {
        oldCates.remove(newCate);// 通过重写比较来实现
      }
      oldCates.add(newCate);
    }
  }

  /**
   * 加载指定路径下的所有CLI配置文件(包括客户定义的)
   */
  public void loadCLIConfigs() {
    synchronized (this) {
      cliConfigurations = new ArrayList<CLIConfiguration>();
      loadCLIConfig(CLIConstant.CLI_CONFIG);
      loadCLIConfig(CLIConstant.CLI_CUST_CONFIG);// 加载客户自定义CLI脚本文件,注意一定后加载(覆盖用)
    }
  }

  /**
   * 加载指定路径下的所有CLI配置文件
   * @param path
   * @throws CLIException
   */
  private void loadCLIConfig(String path) {
    if (logger.isInfoEnabled()) {
      logger.info("加载" + path);
    }
    File file = new File(path);
    // String[] fileNames = file.list();
    File[] xmlFiles = file.listFiles(new FilenameFilter() {
      // 过滤掉指定目录下不包含.xml的配置文件
      public boolean accept(File dir, String name) {
        if (name.indexOf(".xml") != -1) {
          return true;
        }
        return false;
      }
    });
    for (int index = 0; index < xmlFiles.length; index++) {
      // String cfgFile = (path.endsWith("" + File.separatorChar) ? path : (path + File.separatorChar)) +
      // fileNames[index];
      File xml = xmlFiles[index];
      if (xml.isFile()) {
        try {
          addConfig(xml.getPath());
        } catch (CLIException e) {
          logger.error("加载" + xml.getPath() + "失败\n", e);
        }
      }
    }
  }

  /**
   * 用于在已加载列表中查找相同sysName的配置
   */
  private CLIConfiguration getCLIConfigurationFromSysname(List<CLIConfiguration> cliConfigurations, String sysname) {
    for (int index = 0; index < cliConfigurations.size(); index++) {
      CLIConfiguration cliConfiguration = cliConfigurations.get(index);
      if (cliConfiguration.getSysname().equalsIgnoreCase(sysname))
        return cliConfiguration;
    }
    return null;
  }

  /**
   * 获取系统名的序列
   * @param name
   * @return
   */
  private int getSysNameIndexByName(String name) {
    List<String> sysNames = getSysNames();
    for (int i = 0; i < sysNames.size(); i++) {
      String sysName = sysNames.get(i);
      if (sysName.equals(name)) {
        return i;
      }
    }
    return 0;
  }

  public List<String> getSysNames() {
    List<String> sysNames = new ArrayList<String>();
    for (int index = 0; index < cliConfigurations.size(); index++) {
      CLIConfiguration cliConfiguration = cliConfigurations.get(index);
      sysNames.add(cliConfiguration.getSysname());
    }

    return sysNames;
  }

  /**
   * 加入一个cli-collection的定义配置xml文件
   * 
   * @param configFile 定义cli采集配置的xml文件
   * @throws CLIException
   */
  private void addConfig(String configFile) throws CLIException {
  	if (logger.isDebugEnabled())
  		logger.debug("加载或更新CLI解析配置文件" + configFile);
    loadConfigFile(configFile);
  }

  /**
   * 根据系统和系统版本号，查找最佳匹配CLI类别项(Category)
   * </p>
   * categoryName,sysName都是精确匹配,sysVersion为前缀最优匹配,如果具体版本匹配不到，则获取通用版本类别项
   * 
   * @param categoryName 精确匹配，区分大小写
   * @param sysName 精确匹配，区分大小写
   * @param sysVersion, null/""/"all"/"ALL"/"All"都不判断版本,将会获取第一个匹配的通用版本的类别项
   * @return 最匹配的类别项，如果匹配不到，返回null
   */
  public Category getCLIConfiguration(String categoryName, String sysName, String sysVersion) {
    // 考虑是否用锁
    CLIConfigurationIndex index = cliIndex;
    if (StringUtil.isNullOrBlank(sysVersion) || sysVersion.equalsIgnoreCase("all"))
      sysVersion = "all";
    Category cate = index.getCLIConfiguration(categoryName, sysName, sysVersion);
    // 如果具体版本没有获取到，则获取all版本
    if (logger.isDebugEnabled())
    	logger.debug("找不到系统" + sysName + "版本:" + sysVersion + "的配置,尝试寻找通用配置[all]");
    if (cate == null && !sysVersion.equalsIgnoreCase("all")) {
      sysVersion = "all";
      cate = index.getCLIConfiguration(categoryName, sysName, sysVersion);
    }

    if (cate == null && !sysVersion.equalsIgnoreCase("all")) {
      cate = getCLIConfiguration(categoryName, sysName, "all");
    }
    return cate;
  }

  /**
   * 获取最佳匹配CLI类别项(Category)
   * 
   * @param categoryName  sysName都是精确匹配,sysVersion为前缀最优匹配,如果具体版本匹配不到，则获取通用版本类别项
   * @param host  主机类型
   * @param sysName  精确匹配，区分大小写
   * @param sysVersion  null/""/"all"/"ALL"/"All"都不判断版本,将会获取第一个匹配的通用版本的类别项
   * @return  最匹配的类别项，如果匹配不到，返回null
   */
  public Category getCLIConfiguration(String categoryName, String host, String sysName, String sysVersion) {
    for (int index = 0; index < cliConfigurations.size(); index++) {
      CLIConfiguration cliConfiguration = (CLIConfiguration) cliConfigurations.get(index);
      if (cliConfiguration.getSysname().equalsIgnoreCase(sysName)) {
        List<Category> categories = cliConfiguration.getCategories();
        for (int m = 0; m < categories.size(); m++) {
          Category category = (Category) categories.get(m);
          if (!StringUtil.isNullOrBlank(category.getHost())
              && category.getHost().trim().indexOf(host.trim() + ",") != -1
              && categoryName.equalsIgnoreCase(category.getName())) {
            return category;
          }
        }
      }
    }

    return getCLIConfiguration(categoryName, sysName, sysVersion);
  }

  /**
   * 根据系统,得到获取系统版本类别项(Category)
   * </p>
   * 这里判断系统版本的类别项的名称为"sysversion"
   * 
   * @param sysName 精确匹配，区分大小写
   * @return 系统版本类别项，如果找不到，返回null
   */
  public Category getSysVersionConfiguration(String sysName) {
    return getCLIConfiguration("sysversion", sysName, "all");
  }

  public void setCliConfigurations(List<CLIConfiguration> cliConfigurations) {
    this.cliConfigurations = cliConfigurations;
  }

  public int getSelSysNameIndex() {
    return selSysNameIndex;
  }

  public void setSelSysNameIndex(String sysName) {
    this.selSysNameIndex = getSysNameIndexByName(sysName);
  }

  /**
   * 返回索引默认值
   */
  public void backDefaultIndex() {
    if (this.selSysNameIndex != 0) {
      this.selSysNameIndex = 0;
    }
  }
}

/**
 * CLI的系统配置项目索引
 * 
 * @author Eric Liu (liudh@broada.com.cn)
 */
class CLIConfigurationIndex {

  private static final Log logger = LogFactory.getLog(CLIConfigurationIndex.class);

  private Map<String, CLICategoryIndex> confMap = new HashMap<String, CLICategoryIndex>();

  // 缓存管理对象
  private final static Cache cache = OSCache.getInstance();

  private final static String CACHE_KEY = "CLI_CATEGORY_CACHE";

  private CLIConfigurationIndex() {
  }

  /**
   * 得到CLI配置的索引
   */
  public static CLIConfigurationIndex buildCLIConfigurationIndex(List<CLIConfiguration> cliConfigurations) {
    CLIConfigurationIndex index = new CLIConfigurationIndex();
    for (Iterator<CLIConfiguration> iter = cliConfigurations.iterator(); iter.hasNext();) {
      CLIConfiguration cliConf = (CLIConfiguration) iter.next();
      index.confMap.put(cliConf.getSysname().toUpperCase(), new CLICategoryIndex(cliConf));
    }
    return index;
  }

  private CLICategoryIndex getCLIConfiguration(String sysName) {
    return (CLICategoryIndex) confMap.get(sysName.toUpperCase());
  }

  private void putCategoryToCache(String categoryName, String sysName, String sysVersion, Category cate) {
    Map<String, Category> map = (Map<String, Category>) cache.getObject(CACHE_KEY);
    if (map == null) {
      map = new HashMap<String, Category>();
      map.put(sysName.toUpperCase() + "|" + categoryName + "|" + sysVersion, cate);
      cache.putObject(CACHE_KEY, map);
    }
  }

  private Category getCategoryFromCache(String categoryName, String sysName, String sysVersion) {
    Map<String, Category> map = (Map<String, Category>) cache.getObject(CACHE_KEY);
    if (map == null)
      return null;
    else
      return (Category) map.get(sysName.toUpperCase() + "|" + categoryName + "|" + sysVersion);
  }

  /**
   * 清除缓存，由于oscache使用了全局的缓存
   */
  private void releaseCache() {
    cache.removeObject(CACHE_KEY);
  }

  /**
   * 关闭索引，释放资源
   */
  public void close() {
    releaseCache();
  }

  /**
   * 查找对应的命令类别项
   * </p>
   * sysVersion采用前缀最接近匹配法,如果不特别指定sysVersion,则sysVersion一定为all
   */
  public Category getCLIConfiguration(String categoryName, String sysName, String sysVersion) {
    sysName = sysName.toUpperCase();
    Category cacheCate = getCategoryFromCache(categoryName, sysName, sysVersion);
    if (cacheCate != null)
      return cacheCate;
    if (!confMap.containsKey(sysName)) {
    	if (logger.isDebugEnabled())
    		logger.debug("在CLI的配置中找不到系统" + sysName + "配置信息.");
      return null;
    } else {
      CLICategoryIndex index = getCLIConfiguration(sysName);
      Category cate = index.getCategory(categoryName, sysVersion);
      // 是否会按照refresh period定时情况旧的缓存?
      putCategoryToCache(categoryName, sysName, sysVersion, cate);
      return cate;
    }
  }

}

/**
 * CLI的类别项索引
 * 
 * @author Eric Liu (liudh@broada.com.cn)
 */
class CLICategoryIndex {

  private static final Log logger = LogFactory.getLog(CLICategoryIndex.class);

  /** cateMap的结构 [categoryName - List(经过排序的CategoryItem)] */
  private Map<String, List<CategoryItem>> cateMap = new HashMap<String, List<CategoryItem>>();

  private CLIConfiguration cliConfiguration;

  // /////////////////////////////////////////////
  private static class CategoryItem implements Comparable<CategoryItem> {
    private String sysName;

    private String sysVersion;

    private Category category;

    public CategoryItem(String sysName, String sysVersion, Category category) {
      this.sysName = sysName;
      this.sysVersion = sysVersion;
      this.category = category;
    }

    public Category getCategory() {
      return category;
    }

    public String getSysName() {
      return sysName;
    }

    public String getSysVersion() {
      return sysVersion;
    }

    public int compareTo(CategoryItem o) {
      CategoryItem item = (CategoryItem) o;
      // 比较系统,由于系统应该一致,这里实际可以忽略
      int sysComp = getSysName().compareTo(item.getSysName());
      if (sysComp != 0)
        return sysComp;
      // 比较系统版本
      String s1 = (String) getSysVersion();
      String s2 = (String) item.getSysVersion();
      if (s2.startsWith(s1) || s1.startsWith(s2)) {
        // 如果有前缀关系,则将compare的数值转化一下，保证字符串长的反而小，排序在前面
        return -(s1.length() - s2.length());
      }
      return s1.compareTo(s2); // 保证非前缀字符串的顺序
    }
  }

  /** 构造器 */
  public CLICategoryIndex(CLIConfiguration cliConfiguration) {
    this.cliConfiguration = cliConfiguration;
    for (Iterator<Category> iter = cliConfiguration.getCategories().iterator(); iter.hasNext();) {
      Category cate = (Category) iter.next();
      // 将category的版本标示转换为标准的"all"
      if (cate.getSysversion() == null || cate.getSysversion().length() == 0
          || cate.getSysversion().equalsIgnoreCase("all"))
        cate.setSysversion("all");
      putToCateMap(cate.getName(), new CategoryItem(cliConfiguration.getSysname(), cate.getSysversion(), cate));
    }
    indexCateMap();
  }

  private void putToCateMap(String categoryName, CategoryItem item) {
    List<CategoryItem> cateList;
    if (!cateMap.containsKey(categoryName)) {
      cateList = new ArrayList<CategoryItem>();
      cateMap.put(categoryName, cateList);
    } else {
      cateList = cateMap.get(categoryName);
    }
    cateList.add(item);
  }

  private void indexCateMap() {
    for (Iterator<List<CategoryItem>> iter = cateMap.values().iterator(); iter.hasNext();) {
      List<CategoryItem> cateList = iter.next();
      Collections.sort(cateList);
    }
  }

  /**
   * 查询某类CLI命令类别项是否包含在索引中
   */
  public boolean containCategoryName(String categoryName) {
    return cateMap.containsKey(categoryName);
  }

  /**
   * 通过索引查询CLI命令类别项，如果检索不到返回null
   * </p>
   * version采用前缀最接近匹配法,如果不指定version,则version一定为all
   */
  public Category getCategory(String categoryName, String version) {
    if (!containCategoryName(categoryName)) {
    	if (logger.isDebugEnabled())
    		logger.debug("在CLI的配置中找不到系统" + cliConfiguration.getSysname() + "的配置类别项" + categoryName + "的信息.");
      return null;
    }
    List<CategoryItem> cateList = cateMap.get(categoryName);
    // modify by huangjb 2007/12/05 修改 通过version长度逐渐递减与配置表中的Category项比较
    String lversion = null;
    for (int indx = version.length(); indx > 0; indx--) {
      lversion = version.substring(0, indx);
      for (Iterator<CategoryItem> iter = cateList.iterator(); iter.hasNext();) {
        CategoryItem item = (CategoryItem) iter.next();
        if (!StringUtil.isNullOrBlank(item.category.getHost())) {
          continue;
        }
        // 由list的顺序保障
        if (lversion.startsWith(item.getSysVersion())) {
          return item.getCategory();
        }
      }
    }
    if (logger.isDebugEnabled())
    	logger.debug("在CLI的配置中找不到系统" + cliConfiguration.getSysname() + "的配置类别项" + categoryName + "[系统版本:" + version
        + "]的信息.");
    return null;
  }
}