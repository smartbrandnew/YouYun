package uyun.bat.agent.impl.autosync.entity;

/**
 * 自动同步服务文件信息
 * @author Jiangjw
 */
public class SyncFile {
	public static final String MARK_DELETED = "__del__";
	private String name;
	private long lastModified;
	private long size;
	private String fileset;
	
	public SyncFile() {
		super();
	}

	public SyncFile(String fileset, String name, long lastModified, long size) {
		super();
		this.name = name;
		this.lastModified = lastModified;
		this.size = size;
		this.fileset = fileset;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public SyncFile(SyncFile copy) {
		this(copy.fileset, copy.name, copy.lastModified, copy.size);
	}
	
	public String getFileset() {
		return fileset;
	}

	public void setFileset(String fileset) {
		this.fileset = fileset;
	}

	/**
	 * 文件名称，含相对路径
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 最后修改时间
	 * @return
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * 文件大小，单位B
	 * @return
	 */
	public long getSize() {
		return size;
	}

	@Override
	public String toString() {
		return String.format("%s[name: %s size: %d lastMod: %s]", getClass().getSimpleName(), getName(), getSize(), getLastModified());
	}

	public static SyncFile[] copy(SyncFile[] copy) {
		SyncFile[] result = new SyncFile[copy.length];
		for (int i = 0; i < result.length; i++)
			result[i] = new SyncFile(copy[i]);
		return result;
	}

	public boolean equalsFile(SyncFile other) {
		return this.getLastModified() == other.getLastModified() 
				&& this.getSize() == other.getSize();
	}
	
	public String retDeletedName() {
		int pos = name.lastIndexOf("/");
		if (pos < 0)
			return MARK_DELETED + name;
		else
			return name.substring(0, pos + 1) + MARK_DELETED + name.substring(pos + 1);
	}	

	public boolean retMarkDeleted() {
		int pos = name.lastIndexOf("/");
		if (pos < 0)
			return name.startsWith(MARK_DELETED);
		else
			return name.substring(pos + 1).startsWith(MARK_DELETED);
	}
}
