package uyun.bat.agent.impl.autosync.entity;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Fileset {	
	private String id;
	private String dir;
	private File dirFile;
	private String dirPath;
	private String userPath;
	private List<FileMatcher> includes;
	private List<FileMatcher> excludes;
	private String deleteMode;

	public Fileset() {
		this(null, null, FileOperMode.ONLY_MARKED, null, null);
	}

	public Fileset(String id, String dir, FileOperMode deleteMode, FileMatcher[] includes, FileMatcher[] excludes) {
		setId(id);
		if (deleteMode == null)
			deleteMode = FileOperMode.ONLY_MARKED;
		this.deleteMode = deleteMode.getId();
		this.dir = dir;
		this.includes = FileMatcher.copy(includes);
		this.excludes = FileMatcher.copy(excludes);
	}

	public Fileset(Fileset copy) {
		this(copy.id, copy.dir, copy.retDeleteMode(),
				copy.includes == null ? null : copy.includes.toArray(new FileMatcher[0]),
				copy.excludes == null ? null : copy.excludes.toArray(new FileMatcher[0]));
	}

	public String getDeleteMode() {
		return deleteMode;
	}

	public void setDeleteMode(String deleteMode) {
		this.deleteMode = deleteMode;
	}

	public FileOperMode retDeleteMode() {
		return FileOperMode.getById(deleteMode);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		if (id == null || id.length() == 0)
			this.id = "default";
		else
			this.id = id;
	}

	public String getDir() {
		return dir;
	}

	public File retDirFile() {
		if (dirFile == null) {
			String dirStr = getDir();
			String[] searchPaths = new String[]{"/" + dirStr + "/", "/../" + dirStr + "/", "/../../" + dirStr + "/", "/src/main/resources/" + dirStr + "/"};
			String userDir = System.getProperty("user.dir");
			for (String path : searchPaths) {
				File file = new File(userDir, path);
				if (file.exists()) {
					return file;
				}
			}
			userDir = new File(userDir).getParent();
			if (dirStr == null)
				dirFile = new File(userDir);
			else
				dirFile = new File(userDir, dirStr);
		}
		return dirFile;
	}

	public String retDirPath() {
		if (dirPath == null) {
			dirPath = toAbsolute(retDirFile().getAbsolutePath());
			if (!dirPath.endsWith("/"))
				dirPath += "/";
		}
		return dirPath;
	}

	public void setDir(String dir) {
		this.dir = dir;
		dirFile = null;
		dirPath = null;
	}

	public List<FileMatcher> getIncludes() {
		return includes;
	}

	public List<FileMatcher> getExcludes() {
		return excludes;
	}

	public void addInclude(FileMatcher include) {
		includes.add(include);
	}

	public void addExclude(FileMatcher exclude) {
		excludes.add(exclude);
	}

	@Override
	public String toString() {
		return String.format("%s[dir: %s includes: %s excludes: %s]", getClass().getSimpleName(), getDir(),
				getIncludes(), getExcludes());
	}

	public boolean match(String path) {
		for (FileMatcher exclude : getExcludes())
			if (exclude.match(path))
				return false;

		if (getIncludes().isEmpty())
			return true;

		for (FileMatcher include : getIncludes())
			if (include.match(path))
				return true;

		return false;
	}

	public LocalFile[] listFiles() {
		File dir = retDirFile();
		if (!dir.exists() || !dir.isDirectory())
			return new LocalFile[0];

		String dirStr = toLinuxPath(dir.getAbsolutePath());
		int offset = endWithsPathSep(dirStr) ? 2 : 1;

		List<LocalFile> result = new ArrayList<LocalFile>();
		@SuppressWarnings("unchecked")
		Collection<File> files = FileUtils.listFiles(dir, null, true);
		for (File file : files) {
			String relativePath = toLinuxPath(file.getAbsolutePath());
			relativePath = relativePath.substring(dirStr.length() + offset);
			if (!match(relativePath))
				continue;
			result.add(createLocalFile(relativePath, file));
		}
		return result.toArray(new LocalFile[result.size()]);
	}

	private String getUserPath() {
		if (userPath == null) {
			userPath = toLinuxPath(new File(System.getProperty("user.dir")).getAbsolutePath());
			if (!userPath.endsWith("/"))
				userPath += "/";
		}
		return userPath;

	}

	protected LocalFile createLocalFile(String relativePath , File file) {
		if (dir != null) {
			relativePath = toLinuxPath(file.getAbsolutePath());
			relativePath = relativePath.substring(getUserPath().length());			
		}
		return new LocalFile(id, relativePath, file);
	}

	protected static boolean endWithsPathSep(String path) {
		return path.charAt(path.length() - 1) == '/';
	}

	public static List<Fileset> copy(List<Fileset> filesets) {
		if (filesets == null)
			return new ArrayList<Fileset>();
		else {
			List<Fileset> result = new ArrayList<Fileset>(filesets.size());
			for (Fileset fileset : filesets)
				result.add(new Fileset(fileset));
			return result;
		}
	}
	
	/**
	 * 将一个路径中的分隔符转换为linux路径表示方法
	 * @param path
	 * @return
	 */
	private static String toLinuxPath(String path) {
		return path.replaceAll("\\\\", "/");
	}
	
	/**
	 * 将相对路径转换为绝对路径
	 * 比如c:\\windows\\..\\system32转换为c:/system32
	 * @param path
	 * @return
	 */
	private static String toAbsolute(String path) {		
		if (path.startsWith(".") || path.startsWith(".."))
			path = new File(path).getAbsolutePath();
		path = toLinuxPath(path);
		path = path.replaceAll("/[^/]*/\\.\\.", "/");
		path = path.replaceAll("/\\.", "/");	
		path = path.replaceAll("//", "/");	
		return path;
	}
}
