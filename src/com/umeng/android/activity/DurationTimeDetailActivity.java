package com.umeng.android.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.adapter.DurationTimeAdapter;
import com.umeng.android.adapter.ViewPagerAdapter;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.DurationTimeBean;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.android.logic.DataParseManager;
import com.umeng.android.util.BitmapManager;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.StringUtil;
import com.umeng.client.R;

public class DurationTimeDetailActivity extends BaseActivity{

	private AppInformation app;
	private DurationTimeBean durationTimeBean;
	private DurationTimeBean dayDurationTimeBean;
	private Dialog loadingDialog;
	private String resultString = StringUtil.getDateString(1);
	private String[] durations_events;
	private ViewPager viewPager;
	private ViewGroup viewGroup;
	private ImageView[] imageViews;
	private View view;
	private int currentIndex = 0;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(loadingDialog!=null&&loadingDialog.isShowing()){
				try {
					loadingDialog.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			switch (msg.what) {
			case Constants.MSG_SUCCESS:
				initFrameView();
				break;

			case Constants.MSG_FAIL:
				createPopupWindowLoadingFail(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadData(true);
                        popupWindowopupWindowLoadingFial.dismiss();
                    }
                });
				showLoadFailPopupWindow(findViewById(R.id.duration_viewpager));
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.activity_duration_time_detail, null);
		initFrameData();
        getSupportActionBar().setTitle(StringUtil.cutString(app.getName(), 120));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.memu_calendar, menu);
        MenuItem refreshItem = menu.findItem(R.id.calendar);
        MenuItemCompat.setShowAsAction(refreshItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.calendar:
                MobclickAgent.onEvent(DurationTimeDetailActivity.this, "choose_data_durations");
                Intent intent = new Intent(Intent.ACTION_PICK).
                        setDataAndType(null, CalendarActivity.MIME_TYPE);
                intent.putExtra("today", false);
                intent.putExtra("date", resultString);
                startActivityForResult(intent, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
	/**
	 * init relation view
	 */
	private void initFrameView(){
		viewPager = (ViewPager) view.findViewById(R.id.duration_viewpager);
		initViewPager(viewPager);
		viewGroup = (ViewGroup) view.findViewById(R.id.viewGroup);
		if(imageViews == null||imageViews.length<=0){
			imageViews = new ImageView[2];
	        for(int i=0;i<2;i++){
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
		viewPager.setCurrentItem(currentIndex);
		MobclickAgent.onEvent(DurationTimeDetailActivity.this, "durations", durations_events[currentIndex]);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				imageViews[position].setBackgroundResource(R.drawable.page_indicator_focused);
				for(int i=0;i<imageViews.length;i++){
					if(position !=i){
						imageViews[i].setBackgroundResource(R.drawable.page_indicator);
					}
				}
				MobclickAgent.onEvent(DurationTimeDetailActivity.this, "durations", durations_events[position]);
				currentIndex = position;
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			@Override
			public void onPageScrollStateChanged(int position) {}
		});
		setContentView(view);
	}
	/**
	 * init relation data
	 */
	private void initFrameData(){
		durations_events = getResources().getStringArray(R.array.durations_list);
		app = (AppInformation) getIntent().getSerializableExtra(Constants.APP);
		durationTimeBean = (DurationTimeBean) getIntent().getSerializableExtra(DurationTimeDetailActivity.class.getName());
		dayDurationTimeBean = (DurationTimeBean) getIntent().getSerializableExtra("dayDatas");
		if(durationTimeBean == null||durationTimeBean.getDatas()==null||dayDurationTimeBean == null){
			loadData(false);
		}else{
			initFrameView();
		}
	}
	/**
	 * add pager view data
	 * @param viewPager
	 */
	private void initViewPager(ViewPager viewPager){
		List<View> views = new ArrayList<View>();
		for(int i = 0;i<2;i++){
			View view = getLayoutInflater().inflate(R.layout.duration_head, null);
			TextView timelabelTextView = (TextView) view.findViewById(R.id.text_name);
			TextView todayTimeTextView =(TextView) view.findViewById(R.id.text_date);
			todayTimeTextView.setText(resultString);
			TextView timeTextView = (TextView) view.findViewById(R.id.text_time);
			LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.durtion_time_linear);
			ListView listView = (ListView) view.findViewById(R.id.listView);
			DurationTimeAdapter adapter = null;
			if(i == 0){
				timelabelTextView.setText(R.string.single_time);
				timeTextView.setText(durationTimeBean.getAverage());
				adapter = new DurationTimeAdapter(this, durationTimeBean);
				BitmapManager.setBarChart(linearLayout, durationTimeBean, 
						Constants.SINGLE_COLORS, new String[]{getString(R.string.today_data)});
			}else{
				timelabelTextView.setText(R.string.single_day_time);
				((TextView)view.findViewById(R.id.trend_single_listview_title_1)).setText(R.string.user_num);
				timeTextView.setText(dayDurationTimeBean.getAverage());
				adapter = new DurationTimeAdapter(this, dayDurationTimeBean);
				BitmapManager.setBarChart(linearLayout, dayDurationTimeBean, 
						Constants.SINGLE_COLORS, new String[]{getString(R.string.today_data)});
			}
			listView.setAdapter(adapter);
			views.add(view);
		}
		viewPager.setAdapter(new ViewPagerAdapter(views));
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	/**
	 * load data from server
	 */
	private void loadData(final boolean isChoose){
		if(loadingDialog == null){
			loadingDialog = DialogManager.getLoadingDialog(DurationTimeDetailActivity.this);
		}
		if((loadingDialog!=null&&!loadingDialog.isShowing()&&!DurationTimeDetailActivity.this.isFinishing())){
			loadingDialog.show();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if(!NetManager.isOnline(DurationTimeDetailActivity.this)){
						throw new Exception(getString(R.string.net_error));
					}
					Map<String, String> maps = new HashMap<String, String>();
					maps.put("auth_token", AppApplication.getInstance().getToken());
					maps.put("appkey", app.getAppkey());
					if (isChoose) {
						maps.put("start_date",resultString);
						maps.put("end_date",resultString);
					}else{
						maps.put("start_date",
								StringUtil.getDateString(Constants.TYPE_YESTERDAY));
						maps.put("end_date",
								StringUtil.getDateString(Constants.TYPE_YESTERDAY));
					}
					maps.put("period_type", "daily_per_launch");
					String json = NetManager.getStingWithGet(Constants.DURATION_TIME, maps);
					durationTimeBean = DataParseManager.getDurationTimeBean(json);
					maps.remove("period_type");
					maps.put("period_type", "daily");
					json = NetManager.getStingWithGet(Constants.DURATION_TIME, maps);
					dayDurationTimeBean = DataParseManager.getDurationTimeBean(json);
					handler.sendEmptyMessage(Constants.MSG_SUCCESS);
				}catch (Exception e) {
					e.printStackTrace();
					Message message = handler.obtainMessage();
					message.what  =  Constants.MSG_FAIL;
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK){
			resultString = data.getStringExtra("result");
			loadData(true);
		}
	}
}
