package com.umeng.android.exception;


import android.content.Context;

import com.umeng.android.common.AppApplication;
import com.umeng.android.util.ToastUtils;
import com.umeng.client.R;

public class AppException extends Exception {

	private static final long serialVersionUID = 1L;
	private int type;
	private Exception exception;
	
	public final static byte TYPE_NETWORK 	= 0x01;
	public final static byte TYPE_JSON	 	= 0x02;
	public final static byte TYPE_NO_APP	= 0x03;
	
	/**
	 * @param type type of exception
	 * @param exception 
	 */
	public AppException(int type,Exception exception) {
		super();
		this.type = type;
		this.exception = exception;
	}

	public void makeToast(Context context){
		if(AppApplication.DEBUG){
			this.exception.printStackTrace();
		}
		switch (type) {
		case TYPE_NETWORK:
			ToastUtils.showMessageShort(context, R.string.exception_network);
			break;

		case TYPE_JSON:
			ToastUtils.showMessageShort(context, R.string.exception_json);
			break;
		case TYPE_NO_APP:
			ToastUtils.showMessageShort(context, R.string.no_app);
			break;
		}
	}
	
	/**
	 * @param type  type of exception
	 * @param e 
	 * @return a AppException
	 */
	public static AppException makeException(int type,Exception e){
		return new AppException(type,e);
	}
}
