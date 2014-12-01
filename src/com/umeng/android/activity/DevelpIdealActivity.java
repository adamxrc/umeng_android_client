package com.umeng.android.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.umeng.android.adapter.ViewPagerAdapter;
import com.umeng.client.R;

public class DevelpIdealActivity extends Activity {

	private ViewGroup viewGroup;
	private ViewPager viewPager;
	private ImageView[] imageViews;
	private List<View> views = new ArrayList<View>();
	private View view;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
	private void addViews(){
		for(int i = 0;i<4;i++){
			ImageView imageView = new ImageView(this);
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			imageView.setLayoutParams(params);
			imageView.setBackgroundResource(getResources().getIdentifier("welcom"+(i+1), "drawable", getPackageName()));
			views.add(imageView);
//			try {
//				Field field = R.drawable.class.getField("welcom"+(i+1));
//				System.out.println("field:"+field.getInt(new R.drawable()));
//				System.out.println("resource:"+getResources().getIdentifier("welcom"+(i+1), "drawable", getPackageName()));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
		View lastView = getLayoutInflater().inflate(R.layout.dev_ideal_lastitem, null);
		lastView.findViewById(R.id.dev_ideal).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		views.add(lastView);
	}
}
