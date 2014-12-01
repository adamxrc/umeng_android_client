package com.umeng.android.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.adapter.TrendAdapter;
import com.umeng.android.adapter.ViewPagerAdapter;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.ChannelBean;
import com.umeng.android.bean.ChartDataBean;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.android.exception.AppException;
import com.umeng.android.logic.DataParseManager;
import com.umeng.android.util.BitmapManager;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.StringUtil;
import com.umeng.android.util.ToastUtils;
import com.umeng.client.R;

public class TrendActivity extends BaseActivity {

	// list footer
	private TextView teViewline;
	// handle message constant
	private static final int loadingFail = 0;
	private static final int initActivity = 2;
	// ExecutorService
	ExecutorService threadPoll = Executors.newSingleThreadExecutor();
	// mark whetherthe thread had finished after dialog dissmiss
	protected static final int REQUEST_CODE_FILTER = 1;
	// loading fail
	// show the current version and channel
	private TextView markNewUserVearsion;
	private TextView markNewUserChannel;
	private TextView markActiveUserVearsion;
	private TextView markActiveUserChannel;
	private TextView markLaunchVearsion;
	private TextView markLaunchChannel;
	// used to hide vearsion and channel mark when they are "all"
	private LinearLayout newUserSiftMark;
	private LinearLayout activeUserSiftMark;
	private LinearLayout launchUserSiftMark;
	private TextView textView_title1;
	// used to distinguish handler switch
	private Dialog loadingDialog;
	private ArrayList<ChannelBean> channelBeanList = new ArrayList<ChannelBean>();
	private ArrayList<String> channelList = new ArrayList<String>();
	private ArrayList<String> versionsList = new ArrayList<String>();
	private ArrayList<String> timeslotList = new ArrayList<String>();

	private ListView newUserListview;
	private ListView activeUserListview;
	private ListView launchListview;

	private TrendAdapter newUserAdapter;
	private TrendAdapter activeUserAdapter;
	private TrendAdapter launchAdapter;
	// intent information from other activity
	private AppInformation app = null;
	// chart data bean
	private ChartDataBean newUserChartDataBean;
	private ChartDataBean activeUserChartDataBean;
	private ChartDataBean launchsChartDataBean;

	// 3 layout to add chart view
	private LinearLayout newUserChartLayout;
	private LinearLayout activeUserChartLayout;
	private LinearLayout launchsChartLayout;
	// now sift state
	private int daytype = 30;
	private String channelState = "all";
	private String channelStateId = "";
	private String versionState = "all";

	/**
	 * information about viewpager
	 */
	private ViewPager viewPager;
	private List<View> viewList;
	private ViewGroup viewGroup;
	private ImageView[] imageViews;
	private int currIndex = 0;
	private int threadLock;
	// private int listviewLastItem = -1;
	private View view;

	private int selectedTimeslotIndex;
	private int selectedVersionIndex;
	private int selectedChannelIndex;

	private static final byte NEW_USER = 0x01;
	private static final byte ACTIVE_USER = 0x02;
	private static final byte LAUNCHER = 0x03;
	// handler
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				try {
					loadingDialog.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			switch (msg.arg1) {
			case Constants.MSG_SUCCESS:
				initFrameView();
				// listviewLastItem = -1;
				break;
			case Constants.MSG_FAIL:
				createPopupWindowLoadingFail(new OnClickListener() {
					@Override
					public void onClick(View v) {
						loadChartData();
						popupWindowopupWindowLoadingFial.dismiss();
					}
				});
				showLoadFailPopupWindow(findViewById(R.id.trend_vPager));
				// listviewLastItem = -1;
				break;
			default:
				break;
			}
		}

	};

	@SuppressLint("UseValueOf")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		teViewline = new TextView(TrendActivity.this);
		teViewline.setHeight(1);
		teViewline.setBackgroundColor(Color.parseColor("#33e0e0e0"));
		super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.activity_trend, null);
		initFrameView();
		initFrameData();
		getSupportActionBar()
				.setTitle(StringUtil.cutString(app.getName(), 120));
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
			Intent intent = new Intent(TrendActivity.this, FilterActivity.class);
			intent.putStringArrayListExtra(
					FilterActivity.INTENT_KEY_TIMESLOT_LIST, timeslotList);
			intent.putStringArrayListExtra(
					FilterActivity.INTENT_KEY_CHANNEL_LIST, channelList);
			intent.putStringArrayListExtra(
					FilterActivity.INTENT_KEY_VERSION_LIST, versionsList);
			intent.putExtra(FilterActivity.INTENT_KEY_TIMESLOT_SELECTED_INDEX,
					selectedTimeslotIndex);
			intent.putExtra(FilterActivity.INTENT_KEY_CHANNEL_SELECTED_INDEX,
					selectedChannelIndex);
			intent.putExtra(FilterActivity.INTENT_KEY_VERSION_SELECTED_INDEX,
					selectedVersionIndex);

			intent.putExtra(FilterActivity.INTENT_KEY_FROM_PAGE,
					getCurrentPageLabel());

			startActivityForResult(intent, REQUEST_CODE_FILTER);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * init relation view
	 */
	private void initFrameView() {
		viewGroup = (ViewGroup) view.findViewById(R.id.viewGroup);
		viewPager = (ViewPager) view.findViewById(R.id.trend_vPager);
		viewList = new ArrayList<View>();
		// add view in viewList
		loadView(NEW_USER);
		loadView(ACTIVE_USER);
		loadView(LAUNCHER);
		// show current version and channel
		setVersionAndChannelIndicate();
		viewPager.setAdapter(new ViewPagerAdapter(viewList));
		// viewPager.setCurrentItem(currIndex);
		viewPager.setCurrentItem(currIndex, true);
		if (imageViews == null || imageViews.length <= 0) {
			imageViews = new ImageView[viewList.size()];
			for (int i = 0; i < viewList.size(); i++) {
				ImageView imageView = new ImageView(this);
				imageView.setScaleType(ScaleType.FIT_XY);
				imageView.setLayoutParams(new LayoutParams(12, 12));
				imageView.setPadding(20, 0, 20, 0);
				imageViews[i] = imageView;
				if (i == currIndex) {
					imageViews[i]
							.setBackgroundResource(R.drawable.page_indicator_focused);
				} else {
					imageViews[i]
							.setBackgroundResource(R.drawable.page_indicator);
				}
				viewGroup.addView(imageViews[i]);
			}
		}
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				imageViews[position]
						.setBackgroundResource(R.drawable.page_indicator_focused);
				for (int i = 0; i < imageViews.length; i++) {
					if (position != i) {
						imageViews[i]
								.setBackgroundResource(R.drawable.page_indicator);
						currIndex = position;
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int position) {
			}
		});

		setContentView(view);
	}

	/**
	 * init relation data
	 */
	@SuppressWarnings("unchecked")
	private void initFrameData() {
		Intent intent = this.getIntent();
		app = (AppInformation) intent.getSerializableExtra(Constants.APP);
		if (app == null) {
			return;
		}
		currIndex = intent.getIntExtra(Constants.PAGE, 0);
		channelState = intent.getStringExtra("channelstate");
		channelStateId = intent.getStringExtra("channelstateid");
		versionState = intent.getStringExtra("versionstate");

		Bundle mybundle = intent.getBundleExtra("mybundle");
		@SuppressWarnings("rawtypes")
		ArrayList list = mybundle.getParcelableArrayList("list");
		channelBeanList = (ArrayList<ChannelBean>) list.get(0);
		versionsList = (ArrayList<String>) list.get(1);
		versionsList.add(getResources().getString(R.string.all));
		List<String> tmpList = Arrays.asList(getResources().getStringArray(
				R.array.time_frequency));
		timeslotList.addAll(0, tmpList);
		for (ChannelBean bean : channelBeanList) {
			channelList.add(bean.getChannel());
		}
		channelList.add(getResources().getString(R.string.all));
		selectedTimeslotIndex = 2; // 30 days
		if (!StringUtil.isEmpty(channelState) && !channelState.equals("all")) {
			int i = 0;
			for (ChannelBean channelBean : channelBeanList) {
				if (channelBean.getId().equals(channelStateId)) {
					channelState = channelBean.getChannel();
					selectedChannelIndex = channelList.indexOf(channelBean
							.getChannel());
					break;
				}
				i++;
			}
			if (i >= channelBeanList.size() - 1) {
				selectedChannelIndex = channelBeanList.size() - 1;
			}
		} else {
			selectedChannelIndex = channelList.size() - 1;
		}

		if (!StringUtil.isEmpty(versionState) && !versionState.equals("all")) {
			selectedVersionIndex = versionsList.indexOf(versionState);
		} else {
			selectedVersionIndex = versionsList.size() - 1;
		}

		if ("all".equals(versionState) && "all".equals(channelState)) {
			daytype = intent.getIntExtra(Constants.DATA_TYPE, -1);
			newUserChartDataBean = (ChartDataBean) list.get(2);
			activeUserChartDataBean = (ChartDataBean) list.get(3);
			launchsChartDataBean = (ChartDataBean) list.get(4);
		}
		if (("all".equals(versionState) && "all".equals(channelState))) {
			Message message = handler.obtainMessage();
			message.arg1 = Constants.MSG_SUCCESS;
			message.arg2 = initActivity;
			handler.sendMessage(message);
		} else {
			loadChartData();
		}
	}

	/**
	 * load view to show at PageView
	 * 
	 * @return
	 */
	private void loadView(int type) {
		int imageRid = 0;
		int titleRid = 0;
		LinearLayout tmpSiftMark = null;
		LinearLayout ChartLayout = null;
		ListView tmpListView = null;
		TrendAdapter tmpAdapter = null;
		TextView tmpVersionTextView = null;
		TextView tmpLunnchelTextView = null;
		LayoutInflater mInflater = getLayoutInflater();
		View view = mInflater.inflate(R.layout.trend_single, null);
		ImageView image = (ImageView) view
				.findViewById(R.id.trend_single_image_type);
		TextView textView = (TextView) view
				.findViewById(R.id.trend_single_text_1);
		tmpVersionTextView = (TextView) view
				.findViewById(R.id.trend_single_text_version);
		tmpLunnchelTextView = (TextView) view
				.findViewById(R.id.trend_single_text_channel);
		textView_title1 = (TextView) view
				.findViewById(R.id.trend_single_listview_title);
		tmpSiftMark = (LinearLayout) view
				.findViewById(R.id.trend_view_hidestate_1);
		ChartLayout = (LinearLayout) view
				.findViewById(R.id.trend_single_gestrue);
		tmpListView = (ListView) view
				.findViewById(R.id.trend_single_listview_1);
		tmpAdapter = null;
		if (type == NEW_USER) {
			imageRid = R.drawable.new_users;
			titleRid = R.string.new_users;
			newUserSiftMark = tmpSiftMark;
			newUserChartLayout = ChartLayout;
			tmpAdapter = new TrendAdapter(TrendActivity.this,
					newUserChartDataBean, Constants.FORMAT_MM_DD);
			markNewUserVearsion = tmpVersionTextView;
			markNewUserChannel = tmpLunnchelTextView;
			newUserAdapter = tmpAdapter;
			newUserListview = tmpListView;
			BitmapManager.setChartData(this, newUserChartDataBean,
					newUserChartLayout, daytype, Constants.FORMAT_MM_DD, true,
					getString(R.string.today_data), getTimeChartListener());
		} else if (type == ACTIVE_USER) {
			imageRid = R.drawable.active_users;
			titleRid = R.string.activity_users;
			activeUserSiftMark = tmpSiftMark;
			activeUserChartLayout = ChartLayout;
			tmpAdapter = new TrendAdapter(TrendActivity.this,
					activeUserChartDataBean, Constants.FORMAT_MM_DD);
			markActiveUserVearsion = tmpVersionTextView;
			markActiveUserChannel = tmpLunnchelTextView;
			activeUserAdapter = tmpAdapter;
			activeUserListview = tmpListView;
			BitmapManager.setChartData(this, activeUserChartDataBean,
					activeUserChartLayout, daytype, Constants.FORMAT_MM_DD,
					true, getString(R.string.today_data),
					getTimeChartListener());
		} else {
			imageRid = R.drawable.start_times;
			titleRid = R.string.launchs;
			launchUserSiftMark = tmpSiftMark;
			launchsChartLayout = ChartLayout;
			tmpAdapter = new TrendAdapter(TrendActivity.this,
					launchsChartDataBean, Constants.FORMAT_MM_DD);
			launchAdapter = tmpAdapter;
			launchListview = tmpListView;
			markLaunchVearsion = tmpVersionTextView;
			markLaunchChannel = tmpLunnchelTextView;
			BitmapManager.setChartData(this, launchsChartDataBean,
					launchsChartLayout, daytype, Constants.FORMAT_MM_DD, true,
					getString(R.string.today_data), getTimeChartListener());
		}
		image.setImageResource(imageRid);
		textView.setText(titleRid);
		textView_title1.setText(titleRid);
		tmpListView.addFooterView(teViewline);
		tmpListView.setAdapter(tmpAdapter);
		viewList.add(view);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_FILTER) {
			if (resultCode == RESULT_OK) {

				selectedTimeslotIndex = data.getIntExtra(
						FilterActivity.INTENT_KEY_TIMESLOT_SELECTED_INDEX, 0);
				selectedVersionIndex = data.getIntExtra(
						FilterActivity.INTENT_KEY_VERSION_SELECTED_INDEX, 0);
				selectedChannelIndex = data.getIntExtra(
						FilterActivity.INTENT_KEY_CHANNEL_SELECTED_INDEX, 0);

				daytype = FilterActivity.TIMESLOT_TYPE[selectedTimeslotIndex];
				versionState = (selectedVersionIndex == versionsList.size() - 1) ? "all"
						: versionsList.get(selectedVersionIndex);
				channelState = (selectedChannelIndex == channelList.size() - 1) ? "all"
						: channelList.get(selectedChannelIndex);
				channelStateId = (selectedChannelIndex == channelList.size() - 1) ? "all"
						: channelBeanList.get(selectedChannelIndex).getId();
				loadChartData();
			}
		}
	}

	/**
	 * get chart data from service
	 * 
	 * @param datatype
	 * @return
	 */
	public ChartDataBean getChartData(int datatype) throws Exception {
		if (!NetManager.isOnline(TrendActivity.this)) {
			ToastUtils.showMessageShort(TrendActivity.this, R.string.net_error);
		}
		ChartDataBean chartDataBean = null;
		String path;
		Map<String, String> maps = new HashMap<String, String>();

		if (datatype == 0) {
			path = Constants.NEW_USER;
		} else if (datatype == 1) {
			path = Constants.ACTIVE_USER;
		} else {
			path = Constants.LAUNCHES;
		}

		maps.put("appkey", app.getAppkey());
		maps.put("auth_token", AppApplication.getInstance().getToken());
		maps.put("period_type", "daily");
		getDayTypeString(maps);
		getSiftString(maps);
		String json = NetManager.getStingWithGet(path, maps);

		if (!versionState.equals("all") && channelState.equals("all")) {
			chartDataBean = DataParseManager.getChartDataBeansVersion(json,
					versionState);
		} else if (versionState.equals("all") && !channelState.equals("all")) {
			chartDataBean = DataParseManager.getChartDataBeans(json,
					channelState);
		} else {
			chartDataBean = DataParseManager.getChartDataBeans(json, "all");
		}
		return chartDataBean;

	}

	/**
	 * the url parameter of daytype
	 * 
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("deprecation")
	public void getDayTypeString(Map<String, String> maps) {
		Date nowdate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		maps.put("end_date", sdf.format(nowdate));
		Date lastDate = new Date(nowdate.getYear(), nowdate.getMonth(),
				nowdate.getDate() - daytype + 1);
		maps.put("start_date", sdf.format(lastDate));
	}

	/**
	 * the url parameter of sift
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public void getSiftString(Map<String, String> maps) {

		if ("all".equals(channelState) && "all".equals(versionState)) {
			return;
		} else if (!("all".equals(channelState)) && "all".equals(versionState)) {
			maps.put("channels", channelStateId);
		} else if ((channelState.equals("all")) && !versionState.equals("all")) {
			maps.put("versions", URLEncoder.encode(versionState));
		} else {
			maps.put("channels", channelStateId);
			try {
				maps.put("versions", URLEncoder.encode(versionState, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * get current page label
	 * 
	 * @return
	 */
	private String getCurrentPageLabel() {
		String label = "";
		switch (currIndex) {
		case 0:
			label = "new_users";
			break;
		case 1:
			label = "active_users";
			break;
		case 2:
			label = "launches";
			break;
		}
		return label;
	}

	/**
	 * set indicate for version and channel
	 */
	public void setVersionAndChannelIndicate() {
		if (!"all".equals(versionState) && "all".equals(channelState)) {
			newUserSiftMark.setVisibility(View.VISIBLE);
			activeUserSiftMark.setVisibility(View.VISIBLE);
			launchUserSiftMark.setVisibility(View.VISIBLE);
			markNewUserVearsion.setText(versionState);
			markNewUserChannel.setText("");
			markNewUserChannel.setVisibility(View.GONE);
			markActiveUserVearsion.setText(versionState);
			markActiveUserChannel.setText("");
			markActiveUserChannel.setVisibility(View.GONE);
			markLaunchVearsion.setText(versionState);
			markLaunchChannel.setText("");
			markLaunchChannel.setVisibility(View.GONE);
		}
		if ("all".equals(versionState) && !"all".equals(channelState)) {
			newUserSiftMark.setVisibility(View.VISIBLE);
			activeUserSiftMark.setVisibility(View.VISIBLE);
			launchUserSiftMark.setVisibility(View.VISIBLE);
			markNewUserVearsion.setText("");
			markNewUserVearsion.setVisibility(View.GONE);
			markNewUserChannel.setText(channelState);
			markActiveUserVearsion.setText("");
			markActiveUserVearsion.setVisibility(View.GONE);
			markActiveUserChannel.setText(channelState);
			markLaunchVearsion.setText("");
			markLaunchVearsion.setVisibility(View.GONE);
			markLaunchChannel.setText(channelState);
		}
		if (!"all".equals(versionState) && !"all".equals(channelState)) {
			newUserSiftMark.setVisibility(View.VISIBLE);
			activeUserSiftMark.setVisibility(View.VISIBLE);
			launchUserSiftMark.setVisibility(View.VISIBLE);
			markNewUserVearsion.setText(versionState);
			markNewUserChannel.setText(channelState);
			markActiveUserVearsion.setText(versionState);
			markActiveUserChannel.setText(channelState);
			markLaunchVearsion.setText(versionState);
			markLaunchChannel.setText(channelState);
		}
		if ("all".equals(versionState) && "all".equals(channelState)) {
			newUserSiftMark.setVisibility(View.GONE);
			activeUserSiftMark.setVisibility(View.GONE);
			launchUserSiftMark.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			this.finish();
			overridePendingTransition(R.anim.translate_activityin_return,
					R.anim.translate_activityout_return);
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * execute this method to load when enter this activity or refresh this
	 * activity
	 */
	public void loadChartData() {
		if (loadingDialog == null) {
			loadingDialog = DialogManager.getLoadingDialog(this);
		}
		if (loadingDialog != null && !loadingDialog.isShowing()
				&& !TrendActivity.this.isFinishing()) {
			loadingDialog.show();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				Message message = new Message();
				try {
					Looper.prepare();
					loadInitData();
					if (newUserChartDataBean == null
							|| activeUserChartDataBean == null
							|| launchsChartDataBean == null) {
						message.arg1 = Constants.MSG_FAIL;
						message.arg2 = loadingFail;
						message.obj = AppException.makeException(
								AppException.TYPE_NETWORK, new Exception());
						handler.sendMessage(message);
						return;
					}
					message.arg1 = Constants.MSG_SUCCESS;
					message.arg2 = initActivity;
					handler.sendMessage(message);
				} catch (Exception e) {
					e.printStackTrace();
					message.arg1 = Constants.MSG_FAIL;
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	private void loadInitData() throws Exception {
		newUserChartDataBean = getChartData(0);
		activeUserChartDataBean = getChartData(1);
		launchsChartDataBean = getChartData(2);
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		if (AppApplication.getInstance().getToken() == null) {
			finish();
		}
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private OnClickListener getTimeChartListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				final int pos;
				if (threadLock == 0) {
					threadLock = -1;
					String label = getCurrentPageLabel();
					MobclickAgent.onEvent(TrendActivity.this,
							"line_chart_click", label);
					GraphicalView mv = (GraphicalView) v;

					SeriesSelection seriesSelection = mv
							.getCurrentSeriesAndPoint();
					if (seriesSelection != null) {
						pos = seriesSelection.getPointIndex();
						// update listview
						newUserAdapter.setSelection(newUserAdapter.getCount()
								- 1 - pos);
						activeUserAdapter.setSelection(activeUserAdapter
								.getCount() - 1 - pos);
						launchAdapter.setSelection(launchAdapter.getCount()
								- 1 - pos);
						newUserListview.setSelection(newUserAdapter.getCount()
								- 1 - pos);
						activeUserListview.setSelection(activeUserAdapter
								.getCount() - 1 - pos);
						launchListview.setSelection(launchAdapter.getCount()
								- 1 - pos);
					}
					threadLock = 0;
				}
			}
		};
	}
}