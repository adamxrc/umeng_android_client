package com.umeng.android.bean;

import java.io.Serializable;

public class RetentionBean implements Serializable{

	private static final long serialVersionUID = 1L;
	private String install_period;
	private String total_install;
	private double[] retention_rate;
	
	public String getInstall_period() {
		return install_period;
	}
	
	public void setInstall_period(String install_period) {
		this.install_period = install_period;
	}
	
	public String getTotal_install() {
		return total_install;
	}
	
	public void setTotal_install(String total_install) {
		this.total_install = total_install;
	}
	
	public double[] getRetention_rate() {
		return retention_rate;
	}
	
	public void setRetention_rate(double[] retention_rate) {
		this.retention_rate = retention_rate;
	}
	
}
