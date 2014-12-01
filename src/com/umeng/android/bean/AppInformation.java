package com.umeng.android.bean;

import java.io.Serializable;

/**
 * used to describe the detail of a app
 */
public class AppInformation implements Serializable{
	
	private static final long serialVersionUID = 1579768863560742124L;
	private String name;
    private String appkey;
    private String platform;
    
    public AppInformation(){}
	public AppInformation(String name, String appkey,String platform) {
		super();
		this.name = name;
		this.appkey = appkey;
		this.platform=platform;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAppkey() {
		return appkey;
	}
	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null ||! (o instanceof AppInformation)){
			return false;
		}
		AppInformation appInformation = (AppInformation) o ;
		if(this.appkey.equals(appInformation.appkey)&&this.name.equals(appInformation.name)
				&&this.platform.equals(appInformation.platform)){
			return true;
		}
		return false;
	}
}
