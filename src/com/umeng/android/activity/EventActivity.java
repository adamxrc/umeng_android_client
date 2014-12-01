package com.umeng.android.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.adapter.EventAdapter;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.EventBean;
import com.umeng.android.bean.GroupBean;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.android.exception.AppException;
import com.umeng.android.logic.DataParseManager;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.StringUtil;
import com.umeng.client.R;

public class EventActivity extends BaseActivity {

	private AppInformation app;
	private EventAdapter eventAdapter;
	private ListView listView;
	private List<GroupBean> lists;
	private Dialog loadingDialog;
	private ArrayList<String> versions;
	private ArrayList<String> channel;
	private ArrayList<String> channelIds;
	private SharedPreferences seeEventNumbers;
	private int selectedVersionIndex = 0;
	private String versionState = "";
	private String eventLabel;
	public static final String EVENT_CLICK_NUMBER = "event_click_nums";
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
						loadData(null);
						popupWindowopupWindowLoadingFial.dismiss();
					}
				});
				showLoadFailPopupWindow(findViewById(R.id.listView));
				break;
			case Constants.MSG_NO_DATA:
				Toast.makeText(EventActivity.this, R.string.event_no_data,
						Toast.LENGTH_SHORT).show();
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event);
		initFrameData();
		getSupportActionBar().setTitle(R.string.event_list);
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
			MobclickAgent.onEvent(EventActivity.this, "version_choose_event");
			Intent intent = new Intent(EventActivity.this, FilterActivity.class);
			intent.putStringArrayListExtra(
					FilterActivity.INTENT_KEY_VERSION_LIST, versions);
			intent.putExtra(FilterActivity.INTENT_KEY_VERSION_SELECTED_INDEX,
					selectedVersionIndex);

			startActivityForResult(intent, 0);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initFrameView() {
		listView = (ListView) findViewById(R.id.listView);
		eventAdapter = new EventAdapter(this, lists);
		listView.setAdapter(eventAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				loadData(lists.get(position).getGroup_id());
				if (StringUtil.isEmpty(lists.get(position).getDisplay_name())) {
					if (StringUtil.isEmpty(lists.get(position).getName())) {
						eventLabel = lists.get(position).getGroup_id();
					} else {
						eventLabel = lists.get(position).getName();
					}
				} else {
					eventLabel = lists.get(position).getDisplay_name();
				}
				MobclickAgent.onEvent(EventActivity.this, "look_over_event");
			}
		});
	}

	private void initFrameData() {
		Executors.newCachedThreadPool().execute(new Runnable() {

			@Override
			public void run() {
				seeEventNumbers = EventActivity.this
						.getPreferences(MODE_PRIVATE);
				int eventNums = seeEventNumbers.getInt(EVENT_CLICK_NUMBER, -1);
				if (eventNums >= 0) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("__ct__", String.valueOf(eventNums));
					MobclickAgent.onEvent(EventActivity.this, "event", map);
				}
				seeEventNumbers.edit().putInt(EVENT_CLICK_NUMBER, 0).commit();
			}
		});
		app = (AppInformation) getIntent().getSerializableExtra(Constants.APP);
		versions = getIntent().getStringArrayListExtra("version");
		if (versions != null) {
			versions.add(getResources().getString(R.string.all));
			selectedVersionIndex = versions.size() - 1;
		}
		channel = getIntent().getStringArrayListExtra("channel");
		if (channel != null) {
			channel.add(getResources().getString(R.string.all));
		}
		channelIds = getIntent().getStringArrayListExtra("channelIds");
		if (app != null) {
			loadData(null);
		}
	}

	private void loadData(final String groupid) {
		if (loadingDialog == null) {
			loadingDialog = DialogManager.getLoadingDialog(this);
		}
		if ((loadingDialog != null && !loadingDialog.isShowing() && !EventActivity.this
				.isFinishing())) {
			loadingDialog.show();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				Message message = new Message();
				try {
					Map<String, String> maps = new HashMap<String, String>();
					maps.put("auth_token", AppApplication.getInstance()
							.getToken());
					maps.put("appkey", app.getAppkey());
					maps.put("versions", versionState);
					maps.put("page", "1");
					maps.put("per_page", "1000");
					if (groupid != null) {
						maps.put("group_id", groupid);
						String json = NetManager.getStingWithGet(
								Constants.EVENT_EVENT_LIST, maps);
						if (json.equals("[]")) {
							message.what = Constants.MSG_NO_DATA;
						} else {
							List<EventBean> listsBeans = DataParseManager
									.getEventBeans(json);
							startActivity(listsBeans, groupid);
						}
					} else {
						maps.put("start_date", StringUtil.getDateString(1));
						maps.put("end_date", StringUtil.getDateString(1));
						String json = NetManager.getStingWithGet(
								Constants.EVENT_GROUP_LIST, maps);
						lists = DataParseManager.getGroupBeans(json);
						if (lists == null) {
							message.what = Constants.MSG_FAIL;
							message.obj = AppException.makeException(
									AppException.TYPE_NETWORK, new Exception());
						} else {
							message.what = Constants.MSG_SUCCESS;
						}
					}
				} catch (Exception e) {
					message.what = Constants.MSG_FAIL;
					message.obj = AppException.makeException(
							AppException.TYPE_NETWORK, e);
				} finally {
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	@SuppressWarnings("unchecked")
	private void startActivity(List<EventBean> lists, String group_id)
			throws Exception {
		// if(lists == null||lists.size()<=0){
		//
		// }
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("auth_token", AppApplication.getInstance().getToken());
		maps.put("appkey", app.getAppkey());
		maps.put("type", "label_count");
		maps.put("event_id", lists.get(0).getEvent_id());
		String label_count = NetManager.getStingWithGet(
				Constants.PARAMTER_LIST, maps);
		int count = 0;
		try {
			count = Integer.valueOf(label_count);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Intent intent = null;
		if (count == 0) {
			// simple event
			intent = new Intent(this, EventDetailActivity.class);
			String label = getResources().getStringArray(R.array.event_list)[0];
			MobclickAgent.onEvent(EventActivity.this, "event", label);
		} else if (count >= 1) {
			// label event or EKV event
			intent = new Intent(this, EKVEventActivity.class);
			Bundle bundle = new Bundle();
			@SuppressWarnings("rawtypes")
			ArrayList arrayList = new ArrayList();
			arrayList.add(lists);
			bundle.putParcelableArrayList(Constants.EKV_EVENT, arrayList);
			intent.putExtra("bundle", bundle);
		}
		intent.putExtra(Constants.GROUP_ID, group_id);
		intent.putExtra(Constants.APP, app);
		intent.putStringArrayListExtra("versions", versions);
		intent.putStringArrayListExtra("channels", channel);
		intent.putStringArrayListExtra("chinnelIds", channelIds);
		intent.putExtra("eventLabel", eventLabel);
		seeEventNumbers = EventActivity.this.getPreferences(MODE_PRIVATE);
		int eventNums = seeEventNumbers.getInt(EVENT_CLICK_NUMBER, 0);
		eventNums++;
		seeEventNumbers.edit().putInt(EVENT_CLICK_NUMBER, eventNums).commit();
		startActivity(intent);
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			selectedVersionIndex = data.getIntExtra(
					FilterActivity.INTENT_KEY_VERSION_SELECTED_INDEX, 0);
			versionState = (selectedVersionIndex == versions.size() - 1) ? ""
					: versions.get(selectedVersionIndex);
			loadData(null);
		}
	}
}
