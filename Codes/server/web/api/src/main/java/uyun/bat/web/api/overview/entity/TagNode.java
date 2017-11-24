package uyun.bat.web.api.overview.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 总览标签树根节点
 */
public class TagNode {
	/**
	 * 总览标签树显示名称
	 */
	private String name = "";

	/**
	 * 节点对应的标签
	 */
	private String tag;

	private long resource;
	private long warn;
	private long error;
	private long info;
	private List<TagLeaf> child = new ArrayList<TagLeaf>();

	public TagNode() {
		super();
	}

	public TagNode(String name, String tag, long resource, long warn, long error,long info) {
		super();
		this.name = name;
		this.tag = tag;
		this.resource = resource;
		this.warn = warn;
		this.error = error;
		this.info = info;
	}

	public void addChild(TagLeaf leaf) {
		child.add(leaf);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public long getResource() {
		return resource;
	}

	public void setResource(long resource) {
		this.resource = resource;
	}

	public long getWarn() {
		return warn;
	}

	public void setWarn(long warn) {
		this.warn = warn;
	}

	public long getError() {
		return error;
	}

	public void setError(long error) {
		this.error = error;
	}
	
	public long getInfo() {
		return info;
	}

	public void setInfo(long info) {
		this.info = info;
	}

	public List<TagLeaf> getChild() {
		return child;
	}

	public void setChild(List<TagLeaf> child) {
		this.child = child;
	}

}
