package com.umeng.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

public class MyEditText extends EditText {

	
	public MyEditText(Context context) {
		super(context);
	}
	public MyEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MyEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	//避免多次点击出现事件分发异常
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			return super.onTouchEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}
}
