package com.umeng.android.bean;

public class SettingInfo {

	private int start_time_hour = 23;
	private int start_time_minute;
	private int end_time_hour = 8;
	private int end_time_minute;
	private boolean isEnabled;

	public int getStart_time_hour() {
		return start_time_hour;
	}

	public void setStart_time_hour(int start_time_hour) {
		this.start_time_hour = start_time_hour;
	}

	public int getStart_time_minute() {
		return start_time_minute;
	}

	public void setStart_time_minute(int start_time_minute) {
		this.start_time_minute = start_time_minute;
	}

	public int getEnd_time_hour() {
		return end_time_hour;
	}

	public void setEnd_time_hour(int end_time_hour) {
		this.end_time_hour = end_time_hour;
	}

	public int getEnd_time_minute() {
		return end_time_minute;
	}

	public void setEnd_time_minute(int end_time_minute) {
		this.end_time_minute = end_time_minute;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

}
