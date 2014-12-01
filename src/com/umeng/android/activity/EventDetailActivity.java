package com.umeng.android.activity;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.adapter.TrendAdapter;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.ChartDataBean;
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

public class EventDetailActivity extends BaseActivity {

	private AppInformation app;
	// private EventBean eventBean;
	private Dialog loadingDialog;
	private LabelEventBean labelEventBean;
	private ChartDataBean timeChartDataBean;
	private ListView listView;
	private TrendAdapter eventDetailAdapter;
	private LinearLayout chartLinearLayout;
	private String eventLabel;
	private ArrayList<String> versions;
	private ArrayList<String> channels;
	private ArrayList<String> channelIds;
	private String versionState = "";
	private String channelState = "";
	private String channelStateId = "";
	private int selectedTimeslotIndex;
	private int selectedChannelIndex;
	private int selectedVersionIndex;
	private int daytype = 0;
	private ArrayList<String> timeList = new ArrayList<String>();
	private TextView textDesTextView;
	private String group_id = "";
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
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
						loadData(daytype);
						popupWindowopupWindowLoadingFial.dismiss();
					}
				});
				showLoadFailPopupWindow(findViewById(R.id.trend_single_gestrue));
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_detail);
		initFrameData();
		listView = (ListView) findViewById(R.id.trend_single_listview_1);
		((TextView) findViewById(R.id.trend_single_listview_title))
				.setText(R.string.label_event_text_2);
		((TextView) findViewById(R.id.trend_single_text_1))
				.setText(R.string.event_detail);
		textDesTextView = (TextView) findViewById(R.id.text_date);
		findViewById(R.id.trend_view_hidestate).setVisibility(View.GONE);
		if (labelEventBean != null) {
			textDesTextView.setText(labelEventBean.getLabel());
		} else {
			textDesTextView.setText(R.string.event_detail);
			findViewById(R.id.trend_view_hidestate).setVisibility(View.GONE);
		}
		getSupportActionBar().setTitle(eventLabel);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
			Intent intent = new Intent(EventDetailActivity.this,
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

	/**
	 * init relation view
	 */
	private void initFrameView() {
		eventDetailAdapter = new TrendAdapter(this, timeChartDataBean,
				Constants.FORMAT_YYYY_MM_DD);
		listView.setAdapter(eventDetailAdapter);
		listView.setBackgroundColor(Color.parseColor("#F3F6F6"));
		listView.setClickable(false);
		chartLinearLayout = (LinearLayout) findViewById(R.id.trend_single_gestrue);
		((ImageView) findViewById(R.id.trend_single_image_type))
				.setBackgroundResource(R.drawable.start_times);
		BitmapManager
				.setChartData(this, timeChartDataBean, chartLinearLayout, 7,
						Constants.FORMAT_MM_DD, true,
						getString(R.string.today_data));
		boolean isSetSymble = false;
		if (!StringUtil.isEmpty(channelState) && !channelState.equals("all")) {
			textDesTextView.setText(channelState);
			isSetSymble = true;
		}
		if (!StringUtil.isEmpty(versionState) && !"all".equals(versionState)) {
			if (isSetSymble) {
				findViewById(R.id.trend_view_hidestate).setVisibility(
						View.VISIBLE);
				((TextView) findViewById(R.id.text_time)).setText(versionState);
			} else {
				textDesTextView.setText(versionState);
				findViewById(R.id.trend_view_hidestate)
						.setVisibility(View.GONE);
			}
		} else {
			if (!isSetSymble && labelEventBean != null) {
				textDesTextView.setText(labelEventBean.getLabel());
			}
			findViewById(R.id.trend_view_hidestate).setVisibility(View.GONE);
		}
	}

	/**
	 * init relation data
	 */
	private void initFrameData() {
		app = (AppInformation) getIntent().getSerializableExtra(Constants.APP);
		// eventBean = (EventBean)
		// getIntent().getSerializableExtra(Constants.SIMPLE_EVENT);
		labelEventBean = (LabelEventBean) getIntent().getSerializableExtra(
				Constants.LABEL_EVENT);
		group_id = getIntent().getStringExtra("group_id");
		eventLabel = getIntent().getStringExtra("eventLabel");
		versions = getIntent().getStringArrayListExtra("versions");
		channels = getIntent().getStringArrayListExtra("channels");
		List<String> lists = Arrays.asList(getResources().getStringArray(
				R.array.time_frequency));
		timeList.addAll(lists);
		channelIds = getIntent().getStringArrayListExtra("chinnelIds");
		if (versions != null) {
			selectedVersionIndex = versions.size() - 1;
		}
		if (channels != null) {
			selectedChannelIndex = channels.size() - 1;
		}
		if (app == null) {
			return;
		}
		loadData(daytype);
	}

	private void loadData(final int time) {
		if (loadingDialog == null) {
			loadingDialog = DialogManager.getLoadingDialog(this);
		}
		if ((loadingDialog != null && !loadingDialog.isShowing() && !EventDetailActivity.this
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
				maps.put("appkey", app.getAppkey());
				maps.put("type", "count");
				// maps.put("event_id", eventBean.getEvent_id());
				if (time == 0) {
					maps.put("start_date", StringUtil.getDateString(6));
				} else {
					maps.put("start_date", StringUtil.getDateString(time));
				}
				maps.put("end_date", StringUtil.getDateString(0));
				maps.put("channels", channelStateId);
				maps.put("versions", versionState);
				maps.put("group_id", group_id);
				try {
					if (labelEventBean != null) {
						maps.put("label",
								URLEncoder.encode(labelEventBean.getLabel()));
					}
					String json = NetManager.getStingWithGet(
							Constants.EVENT_DAILY, maps);
					timeChartDataBean = DataParseManager.getChartDataBeans(
							json, "all");
					// if(labelEventBean!=null){
					// maps.remove("group_id");
					// maps.put("label",
					// URLEncoder.encode(labelEventBean.getLabel()));
					// json =
					// NetManager.getStingWithGet(Constants.PARAMTER_DATA,
					// maps);
					// chartDataBean = DataParseManager.getChartDataBeans(json,
					// "all");
					// }
					if (timeChartDataBean == null) {
						message.what = Constants.MSG_FAIL;
					} else {
						message.what = Constants.MSG_SUCCESS;
					}
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
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			selectedTimeslotIndex = data.getIntExtra(
					FilterActivity.INTENT_KEY_TIMESLOT_SELECTED_INDEX, 0);
			selectedVersionIndex = data.getIntExtra(
					FilterActivity.INTENT_KEY_VERSION_SELECTED_INDEX, 0);
			selectedChannelIndex = data.getIntExtra(
					FilterActivity.INTENT_KEY_CHANNEL_SELECTED_INDEX, 0);
			daytype = FilterActivity.TIMESLOT_TYPE[selectedTimeslotIndex] - 1;
			versionState = selectedVersionIndex == versions.size() - 1 ? ""
					: versions.get(selectedVersionIndex);
			channelStateId = selectedChannelIndex == channels.size() - 1 ? ""
					: channelIds.get(selectedChannelIndex);
			if (selectedChannelIndex != -1) {
				channelState = (selectedChannelIndex == channels.size() - 1) ? "all"
						: channels.get(selectedChannelIndex);
			}
			loadData(daytype);
		}
	}
}
