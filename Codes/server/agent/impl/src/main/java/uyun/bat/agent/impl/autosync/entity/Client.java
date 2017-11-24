package uyun.bat.agent.impl.autosync.entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自动同步客户端，注意这只是一个客户端定义，而不是一个布署的客户端实例
 * @author Jiangjw
 */
public class Client {
	private static final Log logger = LogFactory.getLog(Client.class);
	private String id;
	private String version;
	private List<Fileset> filesets;
	private List<Action> actions;

	public Client() {
		this(null, null, null, null);
	}

	public Client(String id, String version, List<Fileset> filesets, List<Action> actions) {
		this.id = id;
		this.version = version;
		this.filesets = Fileset.copy(filesets);
		this.actions = new ArrayList<Action>();
		if (actions != null)
			this.actions.addAll(actions);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<Fileset> getFilesets() {
		return filesets;
	}

	public void setFilesets(List<Fileset> filesets) {
		this.filesets = filesets;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * ID
	 * @return
	 */
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return String.format("%s[%s filesets: %s]", this.getClass().getSimpleName(), getId(), getFilesets());
	}

	public void addFileset(Fileset fileset) {
		if (getFileset(fileset.getId()) != null)
			throw new IllegalArgumentException("Fileset already exists：" + fileset.getId());
		getFilesets().add(fileset);
	}
	
	public void addAction(Action action) {
		getActions().add(action);
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public LocalFile[] listFiles() {
		Map<String, LocalFile> files = new LinkedHashMap<String, LocalFile>();
		for (Fileset fileset : getFilesets()) {
			LocalFile[] filesetFiles = fileset.listFiles();
			if (filesetFiles != null) {
				for (LocalFile syncFile : filesetFiles) {
					LocalFile exists = (LocalFile) files.get(syncFile.getName());
					if (exists != null) {
						if (logger.isDebugEnabled())
							logger.debug(String.format("File duplication[client: %s file: %s exists: %s:%s new: %s:%s]", getId(),
								syncFile.getName(), exists.getFileset(), exists.getFile(), syncFile.getFileset(), syncFile.getFile()));
						continue;
					}

					files.put(syncFile.getName(), syncFile);
				}
			}
		}
		return files.values().toArray(new LocalFile[files.size()]);
	}

	public Fileset getFileset(String filesetId) {
		for (Fileset fileset : filesets) {
			if (fileset.getId().equals(filesetId))
				return fileset;
		}
		return null;
	}

	public Action[] getActions(Event event) {
		if (actions.isEmpty())
			return null;
		
		List<Action> result = new ArrayList<Action>();
		for (Action action : actions)
			if (action.retEvent() == event)
				result.add(action);
		if (result.isEmpty())
			return null;
		return result.toArray(new Action[result.size()]);
	}

	public Fileset checkFileset(String filesetId) {
		Fileset fileset = getFileset(filesetId);
		if (fileset == null)
			throw new IllegalArgumentException("Unknow fileset：" + filesetId);
		return fileset;
	}
}
