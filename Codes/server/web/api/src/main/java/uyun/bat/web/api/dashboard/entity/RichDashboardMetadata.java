package uyun.bat.web.api.dashboard.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 查询dashboard列表
 */
public class RichDashboardMetadata{
	private String id;
	private String name;
	private boolean favourite;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isFavourite() {
		return favourite;
	}

	public void setFavourite(boolean favourite) {
		this.favourite = favourite;
	}
}
