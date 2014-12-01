package com.umeng.android.activity;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.common.AppPush;
import com.umeng.android.common.Constants;
import com.umeng.client.R;
import com.umeng.message.PushAgent;

public class SettingFeedbackPushActivity extends ActionBarActivity{

	ListView mListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.drawable.actionbar_bg));
		getSupportActionBar().setTitle(R.string.app_push_switch_setting);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		setContentView(R.layout.activity_app_push_swtich_setting);
		mListView = (ListView) findViewById(R.id.activity_app_push_swtich_setting_listview);
		MyAdapter myAdapter = new MyAdapter(this, AppPush.getInstance(this).getAppsFromCache());
		mListView.setAdapter(myAdapter);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}

class MyAdapter extends BaseAdapter{

	Context context;
	List<AppInformation> apps;
	
	public MyAdapter(Context context, List<AppInformation> apps) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.apps = apps;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return apps.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return apps.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder mViewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_app_push_setting, null);
			mViewHolder = new ViewHolder();
			mViewHolder.appNameView = (TextView) convertView.findViewById(R.id.app_name);
			mViewHolder.imgView = (ImageView) convertView.findViewById(R.id.app_push_switch_img);
			mViewHolder.switchView = convertView.findViewById(R.id.app_push_switch);
			
			convertView.setTag(mViewHolder);
		}
		else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		
		final AppInformation app = apps.get(position);
		mViewHolder.appNameView.setText(app.getName());
		mViewHolder.imgView.setImageResource(
				AppPush.getInstance(context).isAppPushOpen(app.getAppkey()) ? R.drawable.push_on : R.drawable.push_off);
		mViewHolder.switchView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (AppPush.getInstance(context).isAppPushOpen(app.getAppkey())) {
					AppPush.getInstance(context).closeAppPush(app.getAppkey());
					mViewHolder.imgView.setImageResource(R.drawable.push_off);
				}else {
					AppPush.getInstance(context).openAppPush(app.getAppkey());
					mViewHolder.imgView.setImageResource(R.drawable.push_on);
				}
			}
		});
		
		return convertView;
	}
	
	
	public static class ViewHolder{
		public ImageView imgView;
		public TextView appNameView;
		public View switchView;
	}
}
