package uyun.bat.agent.impl.autosync.entity;



public enum FileOperMode {	
	/**
	 * 总是删除server端不存在的文件
	 */
	ALWAYS("always"),
	/**
	 * 仅删除server端标记的文件
	 */
	ONLY_MARKED("onlyMarked"),
	/**
	 * 从不删除server端的文件
	 */
	NEVER("never");
	
	private String id;

	private FileOperMode(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public static FileOperMode getById(String id) {
		for (FileOperMode mode : values())
			if (mode.getId().equalsIgnoreCase(id))
				return mode;
		return null;			
	}
}
