package com.umeng.android.bean;

import java.io.Serializable;

/**
 * used to describe statistics information for a version
 */
public class AppVersion implements Serializable {
	private static final long serialVersionUID = -8220164429457286662L;
	private String activeUser;
	private String todayInstall;
	private String totalInstall;
	private String totalInstallRate;
	private String version;

	public AppVersion(String totalInstall, String version, String activeUser,
			String todayInstall, String totalInstallRate) {
		super();
		this.totalInstall = totalInstall;
		this.version = version;
		this.activeUser = activeUser;
		this.todayInstall = todayInstall;
		this.totalInstallRate = totalInstallRate;
	}

	public String getActiveUser() {
		return activeUser;
	}

	public String getTodayInstall() {
		return todayInstall;
	}

	public String getTotalInstall() {
		return totalInstall;
	}

	public String getTotalInstallRate() {
		return totalInstallRate;
	}

	public String getVersion() {
		return version;
	}

	public void setActiveUser(String activeUser) {
		this.activeUser = activeUser;
	}

	public void setTodayInstall(String todayInstall) {
		this.todayInstall = todayInstall;
	}

	public void setTotalInstall(String totalInstall) {
		this.totalInstall = totalInstall;
	}

	public void setTotalInstallRate(String totalInstallRate) {
		this.totalInstallRate = totalInstallRate;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof AppVersion)){
			return false;
		}
		AppVersion tmpData = (AppVersion) o;
		if(this.activeUser.equals(tmpData.activeUser)&&this.version.equals(tmpData.version)&&
				this.todayInstall.equals(tmpData.todayInstall)&&this.totalInstall.equals(tmpData.totalInstall)
				&&this.totalInstallRate.equals(tmpData.totalInstallRate)){
			
			return true;
		}
		return false;
	}
	
	
}
