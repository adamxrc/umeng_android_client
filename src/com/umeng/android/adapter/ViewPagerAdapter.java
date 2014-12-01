package com.umeng.android.adapter;

import java.util.List;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class ViewPagerAdapter extends PagerAdapter {
	
	private List<View> lists;
	public ViewPagerAdapter(List<View> lists) {
		this.lists = lists;
	}

	@Override
	public void destroyItem(View view, int arg1, Object arg2) {
		((ViewPager) view).removeView(lists.get(arg1));
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public int getCount() {
		return lists == null?0:lists.size();
	}

	@Override
	public Object instantiateItem(View arg0, int arg1) {
		((ViewPager) arg0).addView(lists.get(arg1), 0);
		return lists.get(arg1);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
	}

	/**
	 * @param lists the lists to set
	 */
	public void setLists(List<View> lists) {
		this.lists = lists;
		notifyDataSetChanged();
	}
}
