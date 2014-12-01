package com.umeng.android.activity;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.bean.SettingInfo;
import com.umeng.android.bean.User;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.AppPush;
import com.umeng.client.R;
import com.umeng.message.PushAgent;
import com.umeng.message.proguard.C.e;

public class SettingPushActivity extends ActionBarActivity implements
		OnClickListener {
	private TextView start_time_text;
	private TextView end_time_text;
	private int start_time_hour = 23;
	private int start_time_minute;
	private int end_time_hour = 8;
	private int end_time_minute;
	private ImageView push_switch;
	private PushAgent mPushAgent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_push_setting);
		mPushAgent = PushAgent.getInstance(this);
		getSetData();
		initView();
	}

	/**
	 * 获取设置信息
	 */
	private void getSetData() {
		SettingInfo info = getPushSetting(this);
		start_time_hour = info.getStart_time_hour();
		start_time_minute = info.getStart_time_minute();
		end_time_hour = info.getEnd_time_hour();
		end_time_minute = info.getEnd_time_minute();
	}

	private void initView() {
		// TODO Auto-generated method stub
		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.drawable.actionbar_bg));
		getSupportActionBar().setTitle(R.string.push_setting);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		start_time_text = (TextView) findViewById(R.id.start_time);
		start_time_text.setOnClickListener(this);
		end_time_text = (TextView) findViewById(R.id.end_time);
		end_time_text.setOnClickListener(this);
		
		findViewById(R.id.app_push_setting).setOnClickListener(this);
		
		refreshView();

		push_switch = (ImageView) findViewById(R.id.push_switch);
		flushSwitch();

		push_switch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				savePushSetting();
				if (AppPush.getInstance(SettingPushActivity.this).isMsgPushOpen()) {
					AppPush.getInstance(SettingPushActivity.this).closeMsgPush();
				}else {
					AppPush.getInstance(SettingPushActivity.this).openMsgPush();
				}
				flushSwitch();
			}
		});

	}

	/**
	 * 刷新开/关界面
	 */
	private void flushSwitch() {
		if (AppPush.getInstance(this).isMsgPushOpen()) {
			push_switch.setImageResource(R.drawable.open_button);
		} else {
			push_switch.setImageResource(R.drawable.close_button);
		}
	}

	private String formatString(int i) {
		String pattern = "00";
		java.text.DecimalFormat df = new java.text.DecimalFormat(pattern);
		return df.format(i);
	}

	/**
	 * 保存设置状态
	 */
	@SuppressLint("CommitPrefEdits")
	private void savePushSetting() {
		// TODO Auto-generated method stub
		List<User> users = AppApplication.getInstance().getUsers();
		if (users != null && users.size() > 0) {
			User user = users.get(0);
			SharedPreferences.Editor editor = getSharedPreferences(
					user.getUsername(), MODE_PRIVATE).edit();
			editor.putBoolean("isEnabled", true);
			editor.putInt("start_time_hour", start_time_hour);
			editor.putInt("start_time_minute", start_time_minute);
			editor.putInt("end_time_hour", end_time_hour);
			editor.putInt("end_time_minute", end_time_minute);
			editor.commit();
		}

	}

	/**
	 * 获取设置状态
	 */
	@SuppressLint("CommitPrefEdits")
	public static SettingInfo getPushSetting(Context context) {
		// TODO Auto-generated method stub
		List<User> users = AppApplication.getInstance().getUsers();
		if (users != null && users.size() > 0) {
			User user = users.get(0);
			SettingInfo settingInfo = new SettingInfo();
			SharedPreferences preferences = context.getSharedPreferences(
					user.getUsername(), MODE_PRIVATE);

			settingInfo.setEnabled(preferences.getBoolean("isEnabled", true));
			settingInfo.setStart_time_hour(preferences.getInt(
					"start_time_hour", 23));
			settingInfo.setStart_time_minute(preferences.getInt(
					"start_time_minute", 0));
			settingInfo
					.setEnd_time_hour(preferences.getInt("end_time_hour", 8));
			settingInfo.setEnd_time_minute(preferences.getInt(
					"end_time_minute", 0));
			return settingInfo;
		}
		return null;

	}

	public static boolean isEnabled(Context context) {
		List<User> users = AppApplication.getInstance().getUsers();
		if (users != null && users.size() > 0) {
			User user = users.get(0);
			SharedPreferences preferences = context.getSharedPreferences(
					user.getUsername(), MODE_PRIVATE);
			return preferences.getBoolean("isEnabled", true);
		}
		return true;
	}

	/**
	 * 初始化时间对话框
	 */
	private void initStartTimePickerDialog() {

		new TimePickerDialog(SettingPushActivity.this, new OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				// TODO Auto-generated method stub
				start_time_hour = hourOfDay;
				start_time_minute = minute;
				refreshView();
				setPushTime();
				savePushSetting();
			}

		}, start_time_hour, start_time_minute, true).show();
	}

	/**
	 * 设置push的起始、结束时间
	 */
	private void setPushTime() {
		mPushAgent.setNoDisturbMode(start_time_hour, start_time_minute,
				end_time_hour, end_time_minute);
	}

	/**
	 * 初始化时间对话框
	 */
	private void initEndTimePickerDialog() {

		new TimePickerDialog(SettingPushActivity.this, new OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				// TODO Auto-generated method stub
				end_time_hour = hourOfDay;
				end_time_minute = minute;
				refreshView();
				savePushSetting();
				setPushTime();
			}
		}, end_time_hour, end_time_minute, true).show();
	}

	/**
	 * 刷新界面
	 */
	private void refreshView() {

		if (start_time_hour < end_time_hour) {
			showOneDay();
		} else if (start_time_hour > end_time_hour) {
			showTwoDay();
		} else {
			if (start_time_minute < end_time_minute) {
				showOneDay();
			} else if (start_time_minute > end_time_minute) {
				showTwoDay();
			} else {
				showOneDay();
			}

		}
	}

	/**
	 * 跨天显示
	 */
	private void showTwoDay() {
		start_time_text.setText("每日" + start_time_hour + ":"
				+ formatString(start_time_minute));

		end_time_text.setText("—次日" + end_time_hour + ":"
				+ formatString(end_time_minute));
	}

	/**
	 * 同一天显示
	 */
	private void showOneDay() {
		start_time_text.setText(+start_time_hour + ":"
				+ formatString(start_time_minute));

		end_time_text.setText("—" + end_time_hour + ":"
				+ formatString(end_time_minute));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_time:
			initStartTimePickerDialog();
			break;
		case R.id.end_time:
			initEndTimePickerDialog();
			break;
		case R.id.app_push_setting:
			gotoAppPushSettingActivity();
		}
	}

	private void gotoAppPushSettingActivity() {
		Intent intent = new Intent(SettingPushActivity.this, SettingFeedbackPushActivity.class);
		startActivity(intent);	
	}
}
