package com.umeng.android.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.adapter.ViewPagerAdapter;
import com.umeng.android.common.AppApplication;
import com.umeng.client.R;

public class WelcomeActivity extends Activity {

	/**
	 * To mark whether the app is at its first launch. If yes, it will play the
	 * welcome page, otherwise, not.
	 */
	private static final String FIRST_LAUNCH = "first_launch";
	private ViewGroup viewGroup;
	private ViewPager viewPager;
	private ImageView[] imageViews;
	private List<View> views = new ArrayList<View>();
	private View view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dev_ideal);
		loadScreenWidthAndHeight();
		initFrameView();
		initFrameData();
		saveDate();
	}

	/**
	 * obtain the width and height of screen
	 */
	private void loadScreenWidthAndHeight(){
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		AppApplication.width = dm.widthPixels;
		AppApplication.height = dm.heightPixels;
	}
	/**
	 * init relation view
	 */
	private void initFrameView(){
		view = getLayoutInflater().inflate(R.layout.activity_dev_ideal, null);
		viewGroup = (ViewGroup) view.findViewById(R.id.viewGroup);
		viewPager = (ViewPager) view.findViewById(R.id.viewpager);
		addViews();
		ViewPagerAdapter adapter = new ViewPagerAdapter(views);
		viewPager.setAdapter(adapter);
		if(imageViews == null||imageViews.length<=0){
			imageViews = new ImageView[views.size()];
	        for(int i=0;i<views.size();i++){
	        	ImageView imageView = new ImageView(this);
	        	imageView.setLayoutParams(new LayoutParams(12,12));
	        	imageView.setPadding(20, 0, 20, 0);
	        	imageViews[i] = imageView;
	        	if(i==0){
	        		imageViews[i].setBackgroundResource(R.drawable.page_indicator_focused);
	        	}else{
	        		imageViews[i].setBackgroundResource(R.drawable.page_indicator);
	        	}
	        	viewGroup.addView(imageViews[i]);
	        }
		}
		
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				imageViews[position].setBackgroundResource(R.drawable.page_indicator_focused);
				for(int i=0;i<imageViews.length;i++){
					if(position !=i){
						imageViews[i].setBackgroundResource(R.drawable.page_indicator);
					}
				}
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {}
		});
		setContentView(view);
	}
	/**
	 * add views
	 */
	private void addViews(){
		for(int i = 0;i<4;i++){
			ImageView imageView = new ImageView(this);
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			imageView.setLayoutParams(params);
			imageView.setBackgroundResource(getResources().getIdentifier("welcom"+(i+1), "drawable", getPackageName()));
			views.add(imageView);
		}
		View lastView = getLayoutInflater().inflate(R.layout.dev_ideal_lastitem, null);
		lastView.findViewById(R.id.dev_ideal).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startLoginActivity();
			}
		});
		views.add(lastView);
	}
	/**
	 * init relation data
	 */
	private void initFrameData(){
		
		boolean isFirstLaunch = this.getPreferences(MODE_PRIVATE)
				.getBoolean(FIRST_LAUNCH, true);
		if (!isFirstLaunch) {
			startLoginActivity();
		}
	}
	private void startLoginActivity() {
		Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * save date of isFirst launching
	 */
	private void saveDate(){
		Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putBoolean(FIRST_LAUNCH, false);
		editor.commit();
	}
	/**
	 * integrate umeng_analytics
	 */
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unBindDrawable(findViewById(R.id.viewGroup));
		System.gc();
	}
	private void unBindDrawable(View view){
		if(view.getBackground()!=null){
			view.getBackground().setCallback(null);
		}
		if(view instanceof ViewGroup){
			for(int i = 0;i<((ViewGroup)view).getChildCount();i++){
				unBindDrawable(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup)view).removeAllViews();
		}
	}
}