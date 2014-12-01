package com.umeng.android.bean;

import java.io.Serializable;
/**
 * Describe a channel statistics information
 */
public class ChannelBean implements Serializable{
	private static final long serialVersionUID = 6425919375045195954L;
	private String totalInstall;
	private String channel;
	private String activeUser;
	/**
	 * daily install by channel. 
	 */
	private String install;
	/**
	 * total install rate by channel
	 */
	private String totalInstallRate;
	
	private String id;

	public ChannelBean(String totalInstall, String channel, String activeUser,
			String install, String totalInstallRate, String id) {
		super();
		this.totalInstall = totalInstall;
		this.channel = channel;
		this.activeUser = activeUser;
		this.install = install;
		this.totalInstallRate = totalInstallRate;
		this.id = id;
	}

	public String getTotalInstall() {
		return totalInstall;
	}

	public void setTotalInstall(String totalInstall) {
		this.totalInstall = totalInstall;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getActiveUser() {
		return activeUser;
	}

	public void setActiveUser(String activeUser) {
		this.activeUser = activeUser;
	}

	public String getInstall() {
		return install;
	}

	public void setInstall(String install) {
		this.install = install;
	}

	public String getTotalInstallRate() {
		return totalInstallRate;
	}

	public void setTotalInstallRate(String totalInstallRate) {
		this.totalInstallRate = totalInstallRate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null||!(o instanceof ChannelBean)){
			return false;
		}
		ChannelBean tmpData = (ChannelBean) o;
		if(this.totalInstall.equals(tmpData.totalInstall)&&
				this.channel.equals(tmpData.channel)&&
				this.activeUser.equals(tmpData.activeUser)&&
				this.install.equals(tmpData.install)&&
				this.totalInstallRate.equals(tmpData.totalInstallRate)
				&&this.id.equals(tmpData.id)){
			
			return true;
		}
		return false;
	}

}
