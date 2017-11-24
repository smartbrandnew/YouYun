package uyun.bat.console.db;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import uyun.bat.common.config.Config;
import uyun.bird.dbversion.dao.VersionDao;
import uyun.bird.dbversion.entity.ConfigParameter;
import uyun.bird.dbversion.logic.enhanced.VersionManagerImpl;
import uyun.whale.common.util.system.SystemConfig;

public abstract class DBInit {
	private static final Logger logger = LoggerFactory.getLogger(DBInit.class);
	private static DBInit instance = new DBInit() {
	};
	private VersionDao versionDao;

	public VersionDao getVersionDao() {
		return versionDao;
	}

	public void setVersionDao(VersionDao versionDao) {
		this.versionDao = versionDao;
	}

	public static DBInit getInstance() {
		return instance;
	}

	private static File getDatabaseFile() {
		String[] searchPaths ;
		if(Config.getInstance().isChinese()){//判断初始化中英文数据库
		searchPaths = new String[]{
				"console/conf/database/database_create.sql",
				"/conf/database/database_create.sql",
				"/../conf/database/database_create.sql",
				"/src/main/resources/conf/database/database_create.sql",
		};
		}else{
		searchPaths = new String[]{
					"console/conf/databaseInEnglish/database_create.sql",
					"/conf/databaseInEnglish/database_create.sql",
					"/../conf/databaseInEnglish/database_create.sql",
					"/src/main/resources/conf/databaseInEnglish/database_create.sql",
			};
		}
		String userDir = SystemConfig.getUserDir();
		for (String path : searchPaths) {
			File file = new File(userDir, path);
			logger.debug("Test database file: {}", file);
			if (file.exists()) {
				logger.info("Load database from :" + file);
				return file;
			}
		}
		throw new RuntimeException("Cannot find file database_create_sql");
	}

	public void initDB() {
		logger.info("check if database need to init or update......");
		File databaseFile = getDatabaseFile();
		try {
			List<String> readLines = FileUtils.readLines(databaseFile, "UTF-8");
			Pattern linePattern = Pattern
					.compile("^--version [0-9]+[.][0-9]+[.][0-9]+,build [0-9]{4}-[0-9]{1,2}-[0-9]{1,2}.*");
			String lastVersionLine = null;
			for (String line : readLines) {
				if (line != null && linePattern.matcher(line).matches()) {
					lastVersionLine = line;
				}
			}

			if (lastVersionLine == null) {
				throw new RuntimeException("database init error，database_init.sql version format error");
			}

			Pattern pattern = Pattern.compile("[0-9]+[.-][0-9]+[.-][0-9]+");
			Matcher matcher = pattern.matcher(lastVersionLine);
			matcher.find();
			String versionNumber = matcher.group();
			matcher.find();
			String buildDate = matcher.group();

			ConfigParameter config = new ConfigParameter("bat", versionNumber, buildDate, "UTF-8");
			Resource sqlScript = new FileSystemResource(databaseFile);
			VersionManagerImpl versionManager = new VersionManagerImpl(config, versionDao, sqlScript);
			versionManager.init();
		} catch (Exception e) {
			logger.error("database init error", e);
			throw new RuntimeException("database init error", e);
		}
		logger.info("database init and update successfully!");
	}

	public static void main(String[] args) {
		Pattern versionPattern = Pattern
				.compile("^--version [0-9]+[.][0-9]+[.][0-9]+,build [0-9]{4}-[0-9]{1,2}-[0-9]{1,2}.*");

		String line = "--version 1.0.0,build 2016-04-16 bat表初始化";
		Matcher matcher = versionPattern.matcher(line);
		System.out.println(matcher.matches());
		Pattern pattern = Pattern.compile("[0-9]+[.-][0-9]+[.-][0-9]+");
		matcher = pattern.matcher(line);
		while (matcher.find()) {
			System.out.println(matcher.group());
		}
	}

}
