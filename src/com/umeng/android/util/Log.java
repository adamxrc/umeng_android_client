package com.umeng.android.util;

import com.umeng.android.common.AppApplication;

/**
 * 可控的Log 输出工具 可以操作 boolean变量 <code>LOG</code> 来控制输出
 */
public class Log {
	
	public static void i(String tag,String msg){
		if(AppApplication.DEBUG){
			android.util.Log.i(tag, msg);
		}
	}
	
	public static void i(String tag,String msg,Exception e){
		if(AppApplication.DEBUG){
			android.util.Log.i(tag,e.toString()+":  ["+msg+"]");
		}
	}
	
	public static void e(String tag,String msg){
		if(AppApplication.DEBUG){
			android.util.Log.e(tag, msg);
		}
	}
	
	public static void e(String tag,String msg,Exception e){
		if(AppApplication.DEBUG){
			android.util.Log.e(tag,e.toString()+":  ["+msg+"]");
			StackTraceElement[] stackTrace = e.getStackTrace();
			for (StackTraceElement s : stackTrace)
				android.util.Log.e(tag, "        at	 "+s.toString());
		}
	}
	
	public static void d(String tag,String msg){
		if(AppApplication.DEBUG){
			android.util.Log.d(tag, msg);
		}
	}
	
	public static void d(String tag,String msg,Exception e){
		if(AppApplication.DEBUG){
			android.util.Log.d(tag,e.toString()+":  ["+msg+"]");
		}
	}
	
	public static void v(String tag,String msg){
		if(AppApplication.DEBUG){
			android.util.Log.v(tag, msg);
		}
	}
	
	public static void v(String tag,String msg,Exception e){
		if(AppApplication.DEBUG){
			android.util.Log.v(tag,e.toString()+":  ["+msg+"]");
		}
	}
	
	public static void w(String tag,String msg){
		if(AppApplication.DEBUG){
			android.util.Log.w(tag, msg);
		}
	}
	
	public static void w(String tag,String msg,Exception e){
		if(AppApplication.DEBUG){
			android.util.Log.w(tag,e.toString()+":  ["+msg+"]");
			StackTraceElement[] stackTrace = e.getStackTrace();
			for (StackTraceElement s : stackTrace)
				android.util.Log.w(tag, "        at	 "+s.toString());
		}
	}
	
}
