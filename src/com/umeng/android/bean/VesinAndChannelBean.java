package com.umeng.android.bean;

public class VesinAndChannelBean {
	
	private String name;
	private int newUser;
	private int activeUser;
	private float rate;
	public VesinAndChannelBean(String name, int newUser, int activeUser,
			float rate) {
		super();
		this.name = name;
		this.newUser = newUser;
		this.activeUser = activeUser;
		this.rate = rate;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNewUser() {
		return newUser;
	}
	public void setNewUser(int newUser) {
		this.newUser = newUser;
	}
	public int getActiveUser() {
		return activeUser;
	}
	public void setActiveUser(int activeUser) {
		this.activeUser = activeUser;
	}
	public float getRate() {
		return rate;
	}
	public void setRate(float rate) {
		this.rate = rate;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null ||!(o instanceof VesinAndChannelBean)){
			return false;
		}
		VesinAndChannelBean tmpData = (VesinAndChannelBean) o;
		if(this.name.equals(tmpData.name)&&this.newUser == tmpData.newUser
				&&this.activeUser == tmpData.activeUser){
			//compare to double float number 
			if(Math.abs(this.rate - tmpData.rate)< 0.01){
				return true;
			}
		}
		return false;
	}
}
