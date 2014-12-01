package com.umeng.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/** 
 * desc:避免出现数据已经更新，但UI线程还没有收到通知导致的异常退出
 */
public class FeedbackListView extends ListView {

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public FeedbackListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public FeedbackListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public FeedbackListView(Context context) {
		super(context);
	}
	@Override
	protected void layoutChildren() {
		try {
			super.layoutChildren();
		} catch (Exception e) {}
	}
	
}
