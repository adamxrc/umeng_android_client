package com.umeng.android.common;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

import com.umeng.android.bean.User;
import com.umeng.android.util.DataStorageManager;
import com.umeng.message.PushAgent;

public class AppApplication extends Application {

	private User user;
	private List<User> users;
	private List<User> rememberUsers;
	private static AppApplication instance;
	private String token;
	public static final boolean DEBUG = true;
	public static int width = 0;
	public static int height = 0;

	@Override
	public void onCreate() {
		super.onCreate();
		// 自动更新下载时开新进程会重新执行一遍该部分代码，确认不会产生异常，否则进行相关判断处理
		instance = this;
		com.umeng.common.Log.LOG = true;
		loadUsersFromCache();
		
		AppPush.getInstance(this).init();
	}

	public static AppApplication getInstance() {
		return instance;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(User user) {
		this.user = user;
		if (user == null) {
			return;
		}
		if (users == null) {
			users = new ArrayList<User>();
		}
		int location = -1;
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				location = i;
			}
		}
		if (location > -1) {
			users.remove(location);
		}
		users.add(0, user);
		if (rememberUsers == null) {
			return;
		}
		location = -1;
		for (int i = 0; i < rememberUsers.size(); i++) {
			if (rememberUsers.get(i).getUsername().equals(user.getUsername())) {
				location = i;
			}
		}
		if (location > -1) {
			rememberUsers.remove(location);
			rememberUsers.add(0, user);
			DataStorageManager.saveUsers(this, rememberUsers);
		}
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token
	 *            the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * obtain a user from file
	 */
	public void loadUsersFromCache() {
		rememberUsers = DataStorageManager.readUsers(this);
		if (rememberUsers == null) {
			rememberUsers = new ArrayList<User>();
		}
		users = new ArrayList<User>();
		users.addAll(rememberUsers);
	}

	public List<User> getRememberUsers() {
		return rememberUsers;
	}

	public boolean isLogin(User user) {
		for (User iUser : users) {
			if (iUser.getUsername().equals(user.getUsername())) {
				return true;
			}
		}
		return false;
	}

	public boolean isRemembered(User user) {
		for (User iUser : rememberUsers) {
			if (iUser.getUsername().equals(user.getUsername())) {
				return true;
			}
		}
		return false;
	}

	public void saveUser(User user) {
		if (user == null) {
			return;
		}
		if (rememberUsers == null) {
			rememberUsers = new ArrayList<User>();
		}
		int location = -1;
		for (int i = 0; i < rememberUsers.size(); i++) {
			if (rememberUsers.get(i).getUsername().equals(user.getUsername())) {
				location = i;
			}
		}
		if (location > -1) {
			rememberUsers.remove(location);
		}
		rememberUsers.add(0, user);
		DataStorageManager.saveUsers(this, rememberUsers);
	}

	public void forgetUser(User user) {
		if (user == null) {
			return;
		}
		if (rememberUsers == null) {
			return;
		}
		int location = -1;
		for (int i = 0; i < rememberUsers.size(); i++) {
			if (rememberUsers.get(i).getUsername().equals(user.getUsername())) {
				location = i;
			}
		}
		if (location > -1) {
			rememberUsers.remove(location);
			DataStorageManager.saveUsers(this, rememberUsers);
		}
	}
}
