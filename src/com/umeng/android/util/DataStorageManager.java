package com.umeng.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Environment;

import com.umeng.android.bean.User;
import com.umeng.android.common.Constants;

public class DataStorageManager {

	public static String PATH = Environment.getExternalStorageDirectory()
			+ "/umeng/cache/";

	/**
	 * save data according director,filename
	 * 
	 * @param director
	 * @param fileName
	 * @param object
	 */
	public static void saveDataToPhone(String director, String fileName,
			Object object) {
		String saveDirector = "";
		File tmpFile = new File(PATH + director + "/");
		if (!tmpFile.exists()) {
			tmpFile.mkdirs();
		}
		saveDirector = tmpFile.getAbsolutePath();
		try {
			File file = new File(saveDirector, fileName);
			if (file.exists()) {
				file.delete();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			OutputStream outputStream = new FileOutputStream(file);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					outputStream);
			objectOutputStream.writeObject(object);
			objectOutputStream.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * read data according director and filename
	 * 
	 * @param director
	 * @param fileName
	 * @return
	 */
	public static Object readDataFromPhone(String director, String fileName) {
		Object object = null;
		try {
			File file = new File(PATH + director, fileName);
			InputStream inputStream = new FileInputStream(file);
			ObjectInputStream objectInputStream = new ObjectInputStream(
					inputStream);
			object = objectInputStream.readObject();
			objectInputStream.close();
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}

	/**
	 * save data according to director and Constants.FILE_NAME
	 * 
	 * @param directorName
	 *            appkey
	 * @param objects
	 */
	public static void saveDataToPhone(String directorName, Object[] objects) {
		for (int i = 0; i < Constants.FILE_NAMES.length; i++) {
			saveDataToPhone(directorName, Constants.FILE_NAMES[i], objects[i]);
		}
	}

	/**
	 * read data according director and filename
	 * 
	 * @param director
	 * @param fileNames
	 * @return
	 */
	public static Object[] readDataFromPhone(String director, String[] fileNames) {
		if (director == null || director.trim().equals("") || fileNames == null
				|| fileNames.length == 0) {
			return null;
		}
		Object[] objects = new Object[fileNames.length];
		Object tmpObject = null;
		for (int i = 0; i < fileNames.length; i++) {
			tmpObject = readDataFromPhone(director, fileNames[i]);
			if (tmpObject == null) {
				return null;
			}
			objects[i] = tmpObject;
		}
		return objects;
	}

	/**
	 * save user
	 * 
	 * @param context
	 * @param user
	 */
	public static void saveUser(Context context, User user) {
		try {
			context.deleteFile(Constants.USER_INFO);
			OutputStream outputStream = context.openFileOutput(
					Constants.USER_INFO, Context.MODE_PRIVATE);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					outputStream);
			objectOutputStream.writeObject(user);
			objectOutputStream.flush();
			objectOutputStream.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * read user
	 * 
	 * @param context
	 * @return
	 */
	public static User readUser(Context context) {
		try {
			InputStream inputStream = context
					.openFileInput(Constants.USER_INFO);
			ObjectInputStream objectInputStream = new ObjectInputStream(
					inputStream);
			User user = (User) objectInputStream.readObject();
			// if(AppApplication.getInstance().getUser()!=null){
			// }
			objectInputStream.close();
			inputStream.close();
			return user;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * save users
	 * 
	 * @param context
	 * @param users
	 */
	public static void saveUsers(Context context, List<User> users) {
		try {
			context.deleteFile(Constants.USERS_INFO);
			OutputStream outputStream = context.openFileOutput(
					Constants.USERS_INFO, Context.MODE_PRIVATE);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					outputStream);
			objectOutputStream.writeObject(users);
			objectOutputStream.flush();
			objectOutputStream.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * read users
	 * 
	 * @param context
	 * @return
	 */
	public static List<User> readUsers(Context context) {
		try {
			InputStream inputStream = context
					.openFileInput(Constants.USERS_INFO);
			ObjectInputStream objectInputStream = new ObjectInputStream(
					inputStream);
			@SuppressWarnings("unchecked")
			List<User> users = (List<User>) objectInputStream.readObject();
			objectInputStream.close();
			inputStream.close();
			return users;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * clear cache data
	 * 
	 * @param path
	 */
	public static void clearData(String path) {
		try {
			File root = new File(path);
			File[] files = root.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					files[i].delete();
				} else if (files[i].isDirectory()) {
					clearData(files[i].getAbsolutePath());
				}
			}
			root.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isClearData(int time) {
		try {
			File root = new File(PATH);
			File[] files = root.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].exists()) {
					if (new Date().getTime() - files[i].lastModified() >= time) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
