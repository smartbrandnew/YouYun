package uyun.bat.agent.impl.autosync.entity;


import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;

public class FileMatcher {
	private static AntPathMatcher matcher = new AntPathMatcher();
	private String name;
	
	public FileMatcher() {
	}

	public FileMatcher(String name) {		
		this.name = name;
	}

	public FileMatcher(FileMatcher copy) {
		this(copy.name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getName());
	}
	
	public boolean match(String path) {		
		return matcher.match(getName(), path);
	}

	public static List<FileMatcher> copy(FileMatcher[] copy) {
		if (copy == null)
			return new ArrayList<FileMatcher>();
		else {
			List<FileMatcher> result = new ArrayList<FileMatcher>(copy.length);
			for (FileMatcher matcher : copy)
				result.add(new FileMatcher(matcher));
			return result;
		}
	}
}
