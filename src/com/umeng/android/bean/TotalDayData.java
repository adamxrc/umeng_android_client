package com.umeng.android.bean;

import java.io.Serializable;

/**
 * used to describe the statistics data for all apps
 */
public class TotalDayData implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String today_active_users;
	private String yesterday_launches;
	private String yesterday_new_users;
	private String today_new_users;
	private String yesterday_active_users;
	private String installations;
	private String today_launches;
	/**
	 * @return the today_active_users
	 */
	public String getToday_active_users() {
		return today_active_users;
	}
	/**
	 * @param today_active_users the today_active_users to set
	 */
	public void setToday_active_users(String today_active_users) {
		this.today_active_users = today_active_users;
	}
	/**
	 * @return the yesterday_launches
	 */
	public String getYesterday_launches() {
		return yesterday_launches;
	}
	/**
	 * @param yesterday_launches the yesterday_launches to set
	 */
	public void setYesterday_launches(String yesterday_launches) {
		this.yesterday_launches = yesterday_launches;
	}
	/**
	 * @return the yesterday_new_users
	 */
	public String getYesterday_new_users() {
		return yesterday_new_users;
	}
	/**
	 * @param yesterday_new_users the yesterday_new_users to set
	 */
	public void setYesterday_new_users(String yesterday_new_users) {
		this.yesterday_new_users = yesterday_new_users;
	}
	/**
	 * @return the today_new_users
	 */
	public String getToday_new_users() {
		return today_new_users;
	}
	/**
	 * @param today_new_users the today_new_users to set
	 */
	public void setToday_new_users(String today_new_users) {
		this.today_new_users = today_new_users;
	}
	/**
	 * @return the yesterday_active_users
	 */
	public String getYesterday_active_users() {
		return yesterday_active_users;
	}
	/**
	 * @param yesterday_active_users the yesterday_active_users to set
	 */
	public void setYesterday_active_users(String yesterday_active_users) {
		this.yesterday_active_users = yesterday_active_users;
	}
	/**
	 * @return the installations
	 */
	public String getInstallations() {
		return installations;
	}
	/**
	 * @param installations the installations to set
	 */
	public void setInstallations(String installations) {
		this.installations = installations;
	}
	/**
	 * @return the today_launches
	 */
	public String getToday_launches() {
		return today_launches;
	}
	/**
	 * @param today_launches the today_launches to set
	 */
	public void setToday_launches(String today_launches) {
		this.today_launches = today_launches;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o == null || !(o instanceof TotalDayData)){
			return false;
		}
		TotalDayData totalDayData = (TotalDayData) o;
		if(this.today_active_users.equals(totalDayData.today_active_users)&&
				this.today_launches.equals(totalDayData.today_launches)&&
				this.today_new_users.equals(totalDayData.today_new_users)&&
				this.yesterday_active_users.equals(totalDayData.yesterday_active_users)&&
				this.yesterday_launches.equals(totalDayData.yesterday_launches)&&
				this.yesterday_new_users.equals(totalDayData.yesterday_new_users)&&
				this.installations.equals(totalDayData.installations)){
			
			return  true;
		}
		return false;
	}
}
