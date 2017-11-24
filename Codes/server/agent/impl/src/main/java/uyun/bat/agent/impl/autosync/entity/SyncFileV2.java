package uyun.bat.agent.impl.autosync.entity;

public class SyncFileV2 extends SyncFile {
	private String md5;

	public SyncFileV2() {
		super();
	}

	public SyncFileV2(String fileset, String name, long lastModified, long size, String md5) {
		super(fileset, name, lastModified, size);		
		this.md5 = md5;
	}

	public SyncFileV2(SyncFileV2 copy) {
		this(copy.getFileset(), copy.getName(), copy.getLastModified(), copy.getSize(), copy.getMd5());
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	@Override
	public boolean equalsFile(SyncFile other) {
		if (other instanceof SyncFileV2) {
			SyncFileV2 o = (SyncFileV2) other;
			return this.getSize() == other.getSize()
				&& this.getMd5() != null && this.getMd5().equals(o.getMd5());
		} else
			return super.equalsFile(other);
	}
	
	public static SyncFileV2[] copy(SyncFileV2[] copy) {
		SyncFileV2[] result = new SyncFileV2[copy.length];
		for (int i = 0; i < result.length; i++)
			result[i] = new SyncFileV2(copy[i]);
		return result;
	}
}
