package com.umeng.android.bean;

import java.io.Serializable;
/**
 * Represents a login user. 
 */
public class User implements Serializable{
	private static final long serialVersionUID = 5610584051722185476L;
	private String username;
	private String password;
	
	
	public User(){}
	
	public User(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof User)){
			return false;
		}
		User user = (User) o;
		if(this.username.equals(user.username)&&this.password.equals(user.password)){
			return true;
		}
		return false;
	}

}
