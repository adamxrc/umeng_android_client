package com.umeng.android.bean;

import java.io.Serializable;

public class GroupBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private String display_name;
	private int count;
	private String name;
	private String group_id;
	
	public String getDisplay_name() {
		return display_name;
	}
	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGroup_id() {
		return group_id;
	}
	public void setGroup_id(String group_id) {
		this.group_id = group_id;
	}
	
}
