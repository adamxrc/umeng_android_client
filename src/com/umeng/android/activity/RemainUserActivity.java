package com.umeng.android.activity;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.ChannelBean;
import com.umeng.android.bean.RetentionBean;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.android.logic.DataParseManager;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.StringUtil;
import com.umeng.android.util.ToastUtils;
import com.umeng.client.R;

public class RemainUserActivity extends BaseActivity {

	private AppInformation app;
	private Dialog loadingDialog;
	private List<RetentionBean> retentionBeans = new ArrayList<RetentionBean>();
	private static final int WIDTH = AppApplication.width / 9;
	private static final int HEIGHT = (int) (AppApplication.height / 14.5);
	private int itemWidth = 0;
	private LinearLayout contentViewLinearLayout;
	private boolean first = true;
	private List<ChannelBean> channelBeans;
	private ArrayList<String> channels = new ArrayList<String>();
	private ArrayList<String> versions;
	private ArrayList<String> times = new ArrayList<String>();
	private int daytype;
	private String versionState = "";
	private String channelState = "";
	private String[] retention_events;
	private int selectedTimeslotIndex = 1;
	private int selectedChannelIndex;
	private int selectedVersionIndex;
	private int resId;
	private String timeLabel = "daily";
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			switch (msg.what) {
			case Constants.MSG_SUCCESS:
				if (retentionBeans != null && retentionBeans.size() == 0) {
					ToastUtils.showMessageShort(RemainUserActivity.this,
							R.string.no_data);
				} else {
					initFrameView();
				}
				break;
			case Constants.MSG_FAIL:
				createPopupWindowLoadingFail(new OnClickListener() {
					@Override
					public void onClick(View v) {
						loadData();
						popupWindowopupWindowLoadingFial.dismiss();
					}
				});
				showLoadFailPopupWindow(findViewById(R.id.contentView));
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resId = R.string.day_s;
		setContentView(R.layout.activity_remain_user);
		initFrameData();
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
				// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(actionBar.getThemedContext(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, getResources().getStringArray(
								R.array.time_granularity)),
				new ActionBar.OnNavigationListener() {

					@Override
					public boolean onNavigationItemSelected(int arg0, long arg1) {
						MobclickAgent.onEvent(RemainUserActivity.this, "retention", retention_events[arg0]);
						switch (arg0) {
						case 0:
							resId = R.string.day_s;
							timeLabel = "daily";
							loadData();
							break;
						case 1:
							resId = R.string.day_w;
							timeLabel = "weekly";
							loadData();
							break;
						case 2:
							resId = R.string.day_m;
							timeLabel = "monthly";
							loadData();
							break;
						}
						return false;
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.memu_filter, menu);
		MenuItem refreshItem = menu.findItem(R.id.filter);
		MenuItemCompat.setShowAsAction(refreshItem,
				MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.filter:
			startActivity();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * init relation view
	 */
	private void initFrameView() {
		contentViewLinearLayout = (LinearLayout) findViewById(R.id.contentView);
		contentViewLinearLayout.removeAllViews();

		int tableLen = 0;
		tableLen = retentionBeans.get(0).getRetention_rate().length;
		if (tableLen > 5) {
			tableLen = 5;
		}
		LinearLayout headerLayout = (LinearLayout) findViewById(R.id.tableHeaderView);
		headerLayout.removeAllViews();
		headerLayout.addView(getTextView(WIDTH, HEIGHT,
				getString(R.string.date), 0));
		headerLayout.addView(getTextView((int) (WIDTH * 2.36), HEIGHT,
				getString(R.string.new_user_add), 0));
		for (int j = 0; j < tableLen; j++) {
			if (itemWidth <= 0) {
				itemWidth = (AppApplication.width - (int) (WIDTH * 3.36))
						/ tableLen;
			}
			headerLayout.addView(getTextView(itemWidth, HEIGHT, (j + 1)
					+ getString(resId), 0));
		}
		headerLayout.setBackgroundResource(R.drawable.retention_table_head);

		for (int i = 0; i < retentionBeans.size(); i++) {
			LinearLayout linearLayout = new LinearLayout(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, HEIGHT);
			int len = 0;
			len = retentionBeans.get(i).getRetention_rate().length;
			if (len > 5) {
				len = 5;
			}
			if (i == retentionBeans.size() - 1) {
				params.bottomMargin = 1;
			}
			linearLayout.setLayoutParams(params);
			TextView textView = getTextView(WIDTH, HEIGHT,
					getDateString(retentionBeans.get(i)), 0);
			textView.setLineSpacing(-7.0f, 1.0f);
			linearLayout.addView(textView);
			linearLayout.addView(getTextView((int) (WIDTH * 2.36), HEIGHT,
					retentionBeans.get(i).getTotal_install(), 0));
			int color = 0;
			for (int j = 0; j < len; j++) {
				double value = retentionBeans.get(i).getRetention_rate()[j];
				if (value >= 60) {
					color = Constants.ORETENTION_COLORS[0];
				} else if (value >= 40 && value < 60) {
					color = Constants.ORETENTION_COLORS[1];
				} else if (value >= 20 && value < 40) {
					color = Constants.ORETENTION_COLORS[2];
				} else {
					color = Constants.ORETENTION_COLORS[3];
				}
				if (itemWidth <= 0) {
					itemWidth = (AppApplication.width - (int) (WIDTH * 3.36))
							/ len;
				}
				linearLayout.addView(getTextView(itemWidth, HEIGHT,
						retentionBeans.get(i).getRetention_rate()[j] + "%",
						color));
			}
			linearLayout.setBackgroundResource(R.drawable.retention_cell_bg);
			contentViewLinearLayout.addView(linearLayout);

		}
	}

	/**
	 * init relation data
	 */
	@SuppressWarnings("unchecked")
	private void initFrameData() {
		retention_events = getResources().getStringArray(R.array.retention_list);
		app = (AppInformation) getIntent().getSerializableExtra(Constants.APP);
		if (app == null) {
			return;
		}
		Bundle bundle = getIntent().getBundleExtra("bundle");
		@SuppressWarnings("rawtypes")
		ArrayList lists = bundle.getParcelableArrayList("list");
		channelBeans = (List<ChannelBean>) lists.get(0);
		versions = (ArrayList<String>) lists.get(1);
		if (versions != null) {
			versions.add(getResources().getString(R.string.all));
		}
		if (channelBeans != null && channelBeans.size() > 0) {
			for (ChannelBean channelBean : channelBeans) {
				channels.add(channelBean.getChannel());
			}
		}
		channels.add(getResources().getString(R.string.all));
		String[] s = getResources().getStringArray(R.array.time_frequency);
		for (String tmpString : s) {
			times.add(tmpString);
		}
		if (channels != null) {
			selectedChannelIndex = channels.size() - 1;
		}
		if (versions != null) {
			selectedVersionIndex = versions.size() - 1;
		}
//		loadData();
	}

	/**
	 * load data from server
	 */
	private void loadData() {
		if (loadingDialog == null) {
			loadingDialog = DialogManager.getLoadingDialog(this);
		}
		if ((loadingDialog != null && !loadingDialog.isShowing() && !RemainUserActivity.this
				.isFinishing())) {
			loadingDialog.show();
		}
		new Thread(new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				Message message = new Message();
				Map<String, String> maps = new HashMap<String, String>();
				maps.put("auth_token", AppApplication.getInstance().getToken());
				if (daytype > 0) {
					maps.put("end_date", StringUtil.getDateString(1));
					maps.put("start_date",
							StringUtil.getDateString(daytype + 1));
				} else {
					maps.put("end_date", StringUtil.getDateString(1));
					maps.put("start_date",
							StringUtil.getDateString(15 + 1));
				}
				maps.put("page", "1");
				maps.put("appkey", app.getAppkey());
				maps.put("channels", URLEncoder.encode(channelState));
				maps.put("versions", versionState);
				maps.put("time_unit", timeLabel);
				try {
					String json = NetManager.getStingWithGet(
							Constants.RETENTION, maps);
					retentionBeans.clear();
					retentionBeans = DataParseManager.getRetentionBean(json);
					if (retentionBeans == null) {
						message.what = Constants.MSG_FAIL;
					} else {
						message.what = Constants.MSG_SUCCESS;
					}
				} catch (Exception e) {
					e.printStackTrace();
					message.what = Constants.MSG_FAIL;
				} finally {
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	/**
	 * get a textView
	 * 
	 * @param width
	 * @param height
	 * @param text
	 * @param color
	 * @return
	 */
	private TextView getTextView(int width, int height, String text, int color) {
		TextView textView = new TextView(this);
		textView.setText(text);
		textView.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,
				height);
		params.gravity = Gravity.CENTER_VERTICAL;
		textView.setLayoutParams(params);
		textView.setTextSize(11);
		if (color != 0) {
			textView.setBackgroundColor(color);
			if (color == Constants.ORETENTION_COLORS[0]) {
				textView.setTextColor(Color.parseColor("#FFFFFF"));
			} else {
				textView.setTextColor(Color.parseColor("#000000"));
			}
			params.leftMargin = 1;
		} else if (width > 2 * WIDTH) {
			if (!first) {
				textView.setTextColor(Constants.ORETENTION_COLORS[0]);
			} else {
				textView.setTextColor(getResources()
						.getColor(R.color.textColor));
			}
			first = false;
		} else {
			textView.setTextColor(getResources().getColor(R.color.textColor));
		}
		return textView;
	}

	/**
	 * get showwing date string
	 * 
	 * @param retentionBean
	 * @return
	 */
	private String getDateString(RetentionBean retentionBean) {
		String s = "";
		try {
			s = retentionBean.getInstall_period();
			if (s.contains("~")) {
				return s;
			}
			s = s.substring(5);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * start choose time activity
	 */
	private void startActivity() {
		Intent intent = new Intent(this, FilterActivity.class);
		intent.putStringArrayListExtra(FilterActivity.INTENT_KEY_TIMESLOT_LIST,
				times);
		intent.putStringArrayListExtra(FilterActivity.INTENT_KEY_CHANNEL_LIST,
				channels);
		intent.putStringArrayListExtra(FilterActivity.INTENT_KEY_VERSION_LIST,
				versions);
		intent.putExtra(FilterActivity.INTENT_KEY_TIMESLOT_SELECTED_INDEX,
				selectedTimeslotIndex);
		intent.putExtra(FilterActivity.INTENT_KEY_CHANNEL_SELECTED_INDEX,
				selectedChannelIndex);
		intent.putExtra(FilterActivity.INTENT_KEY_VERSION_SELECTED_INDEX,
				selectedVersionIndex);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {

			selectedTimeslotIndex = data.getIntExtra(
					FilterActivity.INTENT_KEY_TIMESLOT_SELECTED_INDEX, 0);
			selectedVersionIndex = data.getIntExtra(
					FilterActivity.INTENT_KEY_VERSION_SELECTED_INDEX, 0);
			selectedChannelIndex = data.getIntExtra(
					FilterActivity.INTENT_KEY_CHANNEL_SELECTED_INDEX, 0);

			daytype = FilterActivity.TIMESLOT_TYPE[selectedTimeslotIndex];
			versionState = (selectedVersionIndex == versions.size() - 1) ? ""
					: versions.get(selectedVersionIndex);
			channelState = (selectedChannelIndex == channels.size() - 1) ? ""
					: channelBeans.get(selectedChannelIndex).getId();
			loadData();
		}
	}
}
