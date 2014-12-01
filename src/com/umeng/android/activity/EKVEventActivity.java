package com.umeng.android.activity;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.adapter.LabelEventAdapter;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.ChartDataBean;
import com.umeng.android.bean.EventBean;
import com.umeng.android.bean.LabelEventBean;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.android.exception.AppException;
import com.umeng.android.logic.DataParseManager;
import com.umeng.android.util.BitmapManager;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.StringUtil;
import com.umeng.client.R;

public class EKVEventActivity extends BaseActivity {

	private AppInformation app;
	private List<EventBean> eventBeans;
	private List<String> eventNames;
	private EventBean eventBean = null;
	private LinearLayout ekvChartLinearLayout;
	private ListView listView;
	private Dialog loadingDialog;
	private List<LabelEventBean> labelEventBeans;
	private String group_id;
	private ChartDataBean chartDataBean;
	private LabelEventAdapter labelEventAdapter;
	private ArrayList<String> versions;
	private ArrayList<String> channels;
	private ArrayList<String> timeList = new ArrayList<String>();
	private ArrayList<String> channelIds;
	private String channelStatusId = "";
	private String channelState = "";
	private String versionState = "";
	private String eventLabel;
	private int selectedTimeslotIndex;
	private int selectedChannelIndex;
	private int selectedVersionIndex;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			switch (msg.what) {
			case Constants.MSG_SUCCESS:
				initFrameView();
				break;
			case Constants.MSG_FAIL:
				createPopupWindowLoadingFail(new OnClickListener() {
					@Override
					public void onClick(View v) {
						loadData(eventBeans.get(0), 0);
						popupWindowopupWindowLoadingFial.dismiss();
					}
				});
				showLoadFailPopupWindow(findViewById(R.id.listView));
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ekv_event);
		initFrameData();
		findViewById(R.id.trend_view_hidestate).setVisibility(View.GONE);
		final ActionBar actionBar = getSupportActionBar();
		if (eventBeans.size() == 1) {
			actionBar.setTitle(eventLabel);
			actionBar.setDisplayHomeAsUpEnabled(true);
			String label = getResources().getStringArray(R.array.event_list)[1];
			MobclickAgent.onEvent(EKVEventActivity.this, "event", label);
		} else {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			// Set up the dropdown list navigation in the action bar.
			actionBar.setListNavigationCallbacks(
					// Specify a SpinnerAdapter to populate the dropdown list.
					new ArrayAdapter<String>(actionBar.getThemedContext(),
							R.layout.simple_list_item_1,
							R.id.text1, eventNames),
					new OnNavigationListener() {

						@Override
						public boolean onNavigationItemSelected(int arg0, long arg1) {
							loadData(eventBeans.get(arg0), 0);
							return false;
						}
					});
			String label = getResources().getStringArray(R.array.event_list)[2];
			MobclickAgent.onEvent(EKVEventActivity.this, "event", label);
		}
		
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
			Intent intent = new Intent(EKVEventActivity.this,
					FilterActivity.class);
			intent.putStringArrayListExtra(
					FilterActivity.INTENT_KEY_TIMESLOT_LIST, timeList);
			intent.putStringArrayListExtra(
					FilterActivity.INTENT_KEY_CHANNEL_LIST, channels);
			intent.putStringArrayListExtra(
					FilterActivity.INTENT_KEY_VERSION_LIST, versions);
			intent.putExtra(FilterActivity.INTENT_KEY_TIMESLOT_SELECTED_INDEX,
					selectedTimeslotIndex);
			intent.putExtra(FilterActivity.INTENT_KEY_CHANNEL_SELECTED_INDEX,
					selectedChannelIndex);
			intent.putExtra(FilterActivity.INTENT_KEY_VERSION_SELECTED_INDEX,
					selectedVersionIndex);
			startActivityForResult(intent, 0);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("unchecked")
	private void initFrameData() {
		app = (AppInformation) getIntent().getSerializableExtra(Constants.APP);
		eventBeans = (List<EventBean>) getIntent().getBundleExtra("bundle")
				.getParcelableArrayList(Constants.EKV_EVENT).get(0);
		eventNames = new ArrayList<String>();
		for (EventBean bean : eventBeans) {
			eventNames.add(bean.getName());
		}
		group_id = getIntent().getStringExtra(Constants.GROUP_ID);
		versions = getIntent().getStringArrayListExtra("versions");
		channels = getIntent().getStringArrayListExtra("channels");
		channelIds = getIntent().getStringArrayListExtra("chinnelIds");
		eventLabel = getIntent().getStringExtra("eventLabel");
		if (channels != null) {
			selectedChannelIndex = channels.size() - 1;
		}
		if (versions != null) {
			selectedVersionIndex = versions.size() - 1;
		}
		List<String> list = Arrays.asList(getResources().getStringArray(
				R.array.time_frequency));
		timeList.addAll(list);
		if (app == null || eventBeans == null || eventBeans.size() == 0) {
			return;
		}
		loadData(eventBeans.get(0), 0);
	}

	private void initFrameView() {
		boolean isSetText = false;
		if (!StringUtil.isEmpty(channelState) && !"all".equals(channelState)) {
			((TextView) findViewById(R.id.text_date)).setText(channelState);
			isSetText = true;
		}
		if (!StringUtil.isEmpty(versionState) && !"all".equals(versionState)) {
			if (isSetText) {
				findViewById(R.id.trend_view_hidestate).setVisibility(
						View.VISIBLE);
				((TextView) findViewById(R.id.text_time)).setText(versionState);
			} else {
				((TextView) findViewById(R.id.text_date)).setText(versionState);
			}
		} else {
			if (!isSetText) {
				((TextView) findViewById(R.id.text_date))
						.setText(R.string.total_trend);
			}
			findViewById(R.id.trend_view_hidestate).setVisibility(View.GONE);
		}
		ekvChartLinearLayout = (LinearLayout) findViewById(R.id.ekv_chart);
		listView = (ListView) findViewById(R.id.listView);
		labelEventAdapter = new LabelEventAdapter(this, labelEventBeans);
		listView.setAdapter(labelEventAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Intent intent = new Intent(EKVEventActivity.this,
						EventDetailActivity.class);
				intent.putExtra(Constants.APP, app);
				intent.putExtra(Constants.SIMPLE_EVENT, eventBean);
				intent.putExtra(Constants.LABEL_EVENT,
						labelEventBeans.get(position));
				intent.putExtra("eventLabel", eventLabel);
				intent.putExtra("group_id", group_id);
				intent.putStringArrayListExtra("versions", versions);
				intent.putStringArrayListExtra("channels", channels);
				intent.putStringArrayListExtra("timeList", timeList);
				intent.putStringArrayListExtra("chinnelIds", channelIds);
				startActivity(intent);
			}
		});
		BitmapManager
				.setChartData(this, chartDataBean, ekvChartLinearLayout, 7,
						Constants.FORMAT_MM_DD, true,
						getString(R.string.today_data));
	}

	private void loadData(final EventBean eventBean, final int time) {
		if (loadingDialog == null) {
			loadingDialog = DialogManager.getLoadingDialog(this);
		}
		if ((loadingDialog != null && !loadingDialog.isShowing() && !EKVEventActivity.this
				.isFinishing())) {
			loadingDialog.show();
		}
		this.eventBean = eventBean;
		new Thread(new Runnable() {
			@Override
			public void run() {
				Message message = new Message();
				Map<String, String> maps = new HashMap<String, String>();
				maps.put("auth_token", AppApplication.getInstance().getToken());
				maps.put("appkey", app.getAppkey());
				maps.put("type", "count");
				maps.put("event_id", eventBean.getEvent_id());
				maps.put("start_date", StringUtil.getDateString(6));
				maps.put("end_date", StringUtil.getDateString(0));
				maps.put("page", "1");
				maps.put("per_page", "10000");
				getSiftString(maps);
				try {
					maps.remove("start_date");
					if (time != 0) {
						maps.put("start_date", StringUtil.getDateString(time));
						// getSiftString(maps, channel, version);
					} else {
						int timetype = FilterActivity.TIMESLOT_TYPE[selectedTimeslotIndex] - 1;
						maps.put("start_date", StringUtil.getDateString(timetype));
					}
					String json = NetManager.getStingWithGet(
							Constants.PARAMTER_LIST, maps);
					labelEventBeans = DataParseManager.getLabelEventBeans(json);
					maps.remove("event_id");
					
					maps.put("group_id", group_id);
					
					
					String tmpString = NetManager.getStingWithGet(
							Constants.EVENT_DAILY, maps);
					chartDataBean = DataParseManager.getChartDataBeans(
							tmpString, "all");
					message.what = Constants.MSG_SUCCESS;
				} catch (Exception e) {
					e.printStackTrace();
					message.what = Constants.MSG_FAIL;
					message.obj = AppException.makeException(
							AppException.TYPE_NETWORK, e);
				} finally {
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			selectedTimeslotIndex = data.getIntExtra(
					FilterActivity.INTENT_KEY_TIMESLOT_SELECTED_INDEX, 0);
			int versionIndex = data.getIntExtra(
					FilterActivity.INTENT_KEY_VERSION_SELECTED_INDEX, -1);
			int channelIndex = data.getIntExtra(
					FilterActivity.INTENT_KEY_CHANNEL_SELECTED_INDEX, -1);
			if (versionIndex != -1) {
				versionState = (versionIndex == versions.size() - 1) ? "all"
						: versions.get(versionIndex);
				selectedVersionIndex = versionIndex;
			}
			if (channelIndex != -1) {
				channelState = (channelIndex == channels.size() - 1) ? "all"
						: channels.get(channelIndex);
				// channelState = "all";
				if (channelIndex < channelIds.size()) {
					channelStatusId = channelIds.get(channelIndex);
				}
				selectedChannelIndex = channelIndex;
			}
			int daytype = FilterActivity.TIMESLOT_TYPE[selectedTimeslotIndex] - 1;
			loadData(eventBean, daytype);
		}
	}

	/**
	 * the url parameter of sift
	 * 
	 * @return
	 */
	private void getSiftString(Map<String, String> maps) {
		try {
			if (channelState.equals("all") && versionState.equals("all")) {
				return;
			} else if (!(channelState.equals("all"))
					&& versionState.equals("all")) {
				maps.put("channels", channelStatusId);
			} else if ((channelState.equals("all"))
					&& !versionState.equals("all")) {
				maps.put("versions", URLEncoder.encode(versionState, "utf-8"));
			} else {
				maps.put("channels", channelStatusId);
				maps.put("versions", URLEncoder.encode(versionState, "utf-8"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
