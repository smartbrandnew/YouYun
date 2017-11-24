package uyun.bat.agent.impl.autosync.entity;


/**
 * 自动同步文件集合，描述一个SyncClient在指定时间的所有文件版本
 * 
 * @author Jiangjw
 */
public class SyncFilesetV2 extends SyncFileset {
	public SyncFilesetV2() {
	}

	public SyncFilesetV2(SyncFileV2[] files) {
		super(files);
	}

	public SyncFilesetV2(String version, SyncFileV2[] files) {
		super(version, files);
	}

	public void setFiles(SyncFileV2[] files) {
		super.setFiles(files);
	}

	public SyncFileV2[] getFiles() {
		return (SyncFileV2[]) super.getFiles();
	}

	public SyncFileV2 getFile(String filename) {
		SyncFile file = super.getFile(filename);
		if (file == null)
			return null;
		else
			return (SyncFileV2) file;
	}
}
