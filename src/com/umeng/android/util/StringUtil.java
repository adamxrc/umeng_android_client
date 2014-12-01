package com.umeng.android.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;

import com.umeng.android.common.Constants;

/**
 * a math class
 */
public class StringUtil {

	@SuppressLint("SimpleDateFormat")
	public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
	private final static Pattern emailPattern = Pattern
			.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");

	/**
	 * cut a string if it is to long
	 * 
	 * @param all
	 * @param n
	 * @return
	 */
	public static String cutString(String all, int n) {
		if (all.length() > n) {
			return all.substring(0, n) + "..";
		} else {
			return all;
		}
	}

	/**
	 * encrypt username and password after it has bean base64 handle
	 * 
	 * @param oldString
	 * @return
	 */
	public static String encryptionString(String oldString) {
		char[] chars = oldString.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) (chars[i] + 1);
		}
		return new String(chars);
	}

	/**
	 * cut integer when it is more than 1000
	 */
	public static String cutInteger(int value) {

		String resultString = "";
		String tmpString = value + "";
		for (; tmpString.length() > 3;) {
			resultString = tmpString.substring(tmpString.length() - 3,
					tmpString.length()) + "," + resultString;
			tmpString = tmpString.substring(0, tmpString.length() - 3);
		}
		resultString = tmpString + "," + resultString;
		resultString = resultString.substring(0, resultString.length() - 1);
		return resultString;
	}

	/**
	 * obtain max and min element in array elements in the array are guaranteed
	 * to be non-negative
	 * 
	 * @param datas
	 * @return
	 */
	public static double[] getMaxAndMin(double[] datas) {
		if (datas == null || datas.length == 0)
			return new double[] { 0, 0 };
		if (datas.length == 1) {
			return new double[] { 0, datas[0] };
		}

		double max = datas[0];
		double min = datas[0];
		double tmp = datas[0];
		if (datas.length > 1) {
			for (int i = 0; i < (datas.length - 1); i++) {
				tmp = datas[i + 1];
				if (tmp > max) {
					max = tmp;
				}
				if (tmp < min) {
					min = tmp;
				}
			}
		}
		return new double[] { min, max };
	}

	// 38, 2, 5, 10
	// expected: 2, 38
	// actual: 10, 0
	// 3, 3

	/**
	 * this email is illegal
	 * 
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email) {
		if (email == null || email.trim().length() == 0)
			return false;
		return emailPattern.matcher(email).matches();
	}

	/**
	 * obtain current date String
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getDateString(String type) {
		Date date = new Date();
		if (type.equals(Constants.TYPE_TODAY)) {
		} else if (type.equals(Constants.TYPE_YESTERDAY)) {
			date.setDate(date.getDate() - 1);
		} else if (type.equals(Constants.TYPE_LAST_WEEK)) {
			date.setDate(date.getDate() - 7);
		} else if (type.equals(Constants.TYPE_LAST_MONTH)) {
			date.setMonth(date.getMonth() - 1);
		} else if (type.equals(Constants.TYPE_HALF_MONTH)) {
			date.setDate(date.getDate() - 15);
		}
		return simpleDateFormat.format(date);
	}

	/**
	 * obtain current date String,
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getDateString(int days) {
		Date date = new Date();
		date.setDate(date.getDate() - days);
		return simpleDateFormat.format(date);
	}

	/**
	 * date is illeage,this method is so bad...
	 * 
	 * @param today
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static boolean dataIsIlleage(String dateString, boolean today) {
		Date nowDate = null;
		try {
			nowDate = simpleDateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
		Date date = new Date();
		if (nowDate.getYear() == date.getYear()
				&& nowDate.getMonth() == date.getMonth()) {
			if (nowDate.getDate() == date.getDate()) {
				return today;
			}
		}
		if (nowDate.getTime() >= date.getTime()) {
			return false;
		}
		return true;
	}

	/**
	 * @param string
	 * @return
	 */
	public static boolean isEmpty(String string) {
		if (string == null || string.trim().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * @param date
	 * @param days
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getDataString(Date date, int days) {
		date.setDate(date.getDate() - days);
		return simpleDateFormat.format(date);
	}

	/**
	 * @param dateString
	 * @return
	 */
	public static Date getDateFromString(String dateString) {
		Date date = null;
		try {
			date = simpleDateFormat.parse(dateString);
		} catch (ParseException e) {
			date = new Date();
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * get String MD5
	 * 
	 * @param string
	 * @return
	 */
	public static String getMD5(String string) {
		MessageDigest digest = null;
		byte buffer[] = string.getBytes();
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.update(buffer);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return String.format("%1$032x", bigInt);
	}

	public static int getLength(double n) {
		int num = (int) n;
		int length = 0;
		while (num > 0) {
			num = num / 10;
			length++;
		}
		if (length < 2) {
			length = 2;
		}
		return length;
	}
}
