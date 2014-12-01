package com.umeng.android.bean;

import java.io.Serializable;

public class EventBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private String event_id;
	private String display_name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEvent_id() {
		return event_id;
	}
	public void setEvent_id(String event_id) {
		this.event_id = event_id;
	}
	public String getDisplay_name() {
		return display_name;
	}
	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}
	
}
