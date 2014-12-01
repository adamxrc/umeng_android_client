package com.umeng.android.bean;

import java.io.Serializable;

/**
 * Describe the daily statistics information
 */
public class BasicDayData implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String launches;
	private String activeUsers;
	private String newUsers;
	private String installations;
	
	public BasicDayData(){}
	public BasicDayData(String launches, String activeUsers, String newUsers,String installations) {
		super();
		this.launches = launches;
		this.activeUsers = activeUsers;
		this.newUsers = newUsers;
		this.installations=installations;
	}
	public String getLaunches() {
		return launches;
	}
	public void setLaunches(String launches) {
		this.launches = launches;
	}
	public String getActiveUsers() {
		return activeUsers;
	}
	public void setActiveUsers(String activeUsers) {
		this.activeUsers = activeUsers;
	}
	public String getNewUsers() {
		return newUsers;
	}
	public void setNewUsers(String newUsers) {
		this.newUsers = newUsers;
	}
	public String getInstallations() {
		return installations;
	}
	public void setInstallations(String installations) {
		this.installations = installations;
	}
	
	@Override
	public boolean equals(Object o) {
		try {
			if(!(o instanceof BasicDayData)){
				return false;
			}
			BasicDayData basicDayData = (BasicDayData) o;
			if(basicDayData.activeUsers.equals(this.activeUsers)&&basicDayData.installations.equals(this.installations)&&
					basicDayData.launches.equals(this.launches)&&basicDayData.newUsers.equals(this.newUsers)){
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

}
