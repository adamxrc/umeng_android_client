package com.umeng.android.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

	
	/**
	 * make a toast to show message
	 * @param context
	 * @param message message of string
	 */
	public static void showMessageLong(Context context,String message){
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * make a toast to show message
	 * @param context
	 * @param rid  resource id 
	 */
	public static void showMessageLong(Context context,int rid){
		Toast.makeText(context, rid, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * make a toast to show message
	 * @param context
	 * @param message message of string
	 */
	public static void showMessageShort(Context context,String message){
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * make a toast to show message
	 * @param context
	 * @param rid  resource id 
	 */
	public static void showMessageShort(Context context,int rid){
		Toast.makeText(context, rid, Toast.LENGTH_SHORT).show();
	}
	
}
