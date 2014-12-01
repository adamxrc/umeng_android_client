package com.umeng.android.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.AppVersion;
import com.umeng.android.bean.BasicDayData;
import com.umeng.android.bean.ChannelBean;
import com.umeng.android.bean.ChartDataBean;
import com.umeng.android.bean.DurationTimeBean;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.android.exception.AppException;
import com.umeng.android.logic.DataParseManager;
import com.umeng.android.util.BitmapManager;
import com.umeng.android.util.DataStorageManager;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.StringUtil;
import com.umeng.android.util.ToastUtils;
import com.umeng.client.R;

/**
 * this activity will show new user ,active user , launchs ,top 10 version
 * ,top10 channel
 */
@SuppressLint("UseValueOf")
public class ProductDetailActivity extends BaseActivity implements
		OnClickListener {
	// handle message constant
	private static final int EXCEPTION = 0;
	private static final int initActivity = 1;
	private static final int CURRENT_DATE = 0x01;
	private static final int LAST_MONTH = 0x02;
	private static final int TODAY = 0x03;
	private static final int no_data = 0x30;
	// ExecutorService
	private ExecutorService threadPoll = Executors.newCachedThreadPool();
	// pie chart
	private CategorySeries pieChartSeries = new CategorySeries("");
	private DefaultRenderer pieChartRenderer = new DefaultRenderer();
	private GraphicalView mChartView;
	// line chart datas
	private ChartDataBean chartDataBeanNewuser;
	private ChartDataBean chartDataBeanActiveuser;
	private ChartDataBean chartDataBeanLaunchs;
	private ChartDataBean chartDataBeanTodayNewAdd;
	private ChartDataBean chartDataBeanTodayLaunch;
	private DurationTimeBean durationTimeBean;
	private DurationTimeBean dayDurationTimeBean;
	// distinguish the state of day type , channel and version
	private int dayType = 30;
	private String channelState = "all";
	private String channelStateId = "";
	private String versionState = "all";
	// information of a app
	private AppInformation app;
	// acount users
	private BasicDayData todayData;
	private BasicDayData yesterdatData;
	// loading dialog
	private Dialog dialogLoading;
	// version and channel datas
	private List<AppVersion> versionsBean;
	private List<ChannelBean> channelsBean;
	// private ImageButton refershImageButton;
	private LinearLayout todayDataLinearLayout;
	private LinearLayout todayNewAddLinearLayout;
	private LinearLayout todayLaunchLinearLayout;
	private LinearLayout duringTimeLinearLayout;
	private SharedPreferences seeReportNumbers;
	public static final String REPORT_NUMBER = "report_nums";
	private boolean refersh = true;
	private long time;
	private TextView durtionTimeTextView;
	private ArrayList<String> channleList = new ArrayList<String>();
	private ArrayList<String> versionList = new ArrayList<String>();
	// handler
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (dialogLoading != null && dialogLoading.isShowing()
					&& !ProductDetailActivity.this.isFinishing()) {
				try {
					dialogLoading.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			switch (msg.arg1) {
			case Constants.MSG_SUCCESS:
				if (msg.arg2 == initActivity) {
					todayDataLinearLayout.setVisibility(View.VISIBLE);
					if (popupWindowopupWindowLoadingFial != null
							&& popupWindowopupWindowLoadingFial.isShowing()) {
						popupWindowopupWindowLoadingFial.dismiss();
					}
					initFrameView();
				}
				break;
			case Constants.MSG_FAIL:
				if (msg.arg2 == no_data) {
					AppException exception = (AppException) msg.obj;
					exception.makeToast(ProductDetailActivity.this);
				} else {
					createLoadingPopupwindow();
					todayDataLinearLayout.setVisibility(View.GONE);
					if (!ProductDetailActivity.this.isFinishing()) {
						try {
							popupWindowopupWindowLoadingFial.showAtLocation(
									ProductDetailActivity.this
											.findViewById(R.id.ScrollView),
									Gravity.CENTER_HORIZONTAL
											| Gravity.CENTER_VERTICAL, 0, 0);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		time = System.currentTimeMillis();
		setContentView(R.layout.activity_product_detail);
		todayDataLinearLayout = (LinearLayout) findViewById(R.id.todaydata_gesture);
		todayDataLinearLayout.setVisibility(View.GONE);
		initFrameData();
		getSupportActionBar()
				.setTitle(StringUtil.cutString(app.getName(), 120));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.memu_refresh, menu);
		MenuItem refreshItem = menu.findItem(R.id.refresh);
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
		case R.id.refresh:
			refersh = true;
			if (popupWindowopupWindowLoadingFial != null
					&& popupWindowopupWindowLoadingFial.isShowing()) {
				popupWindowopupWindowLoadingFial.dismiss();
			}
			loadInitData();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * init relation view
	 */
	private void initFrameView() {
		findViewById(R.id.today_data_linerlayout_1).setOnClickListener(this);
		findViewById(R.id.today_data_linerlayout_2).setOnClickListener(this);
		findViewById(R.id.today_data_linerlayout_3).setOnClickListener(this);
		findViewById(R.id.today_data_linerlayout_4).setOnClickListener(this);
		findViewById(R.id.today_data_linerlayout_5).setOnClickListener(this);
		findViewById(R.id.duration_time_linerlayout).setOnClickListener(this);
		findViewById(R.id.remain_user).setOnClickListener(this);
		findViewById(R.id.event_count).setOnClickListener(this);
		findViewById(R.id.feed_back).setOnClickListener(this);
		findViewById(R.id.today_new_add).setOnClickListener(this);
		findViewById(R.id.today_launch).setOnClickListener(this);
		todayNewAddLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_new_add);
		todayLaunchLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_launcher);
		duringTimeLinearLayout = (LinearLayout) findViewById(R.id.duration_time_linear);
		initTextView();
		initChartView();
	}

	/**
	 * init relation data
	 */
	private void initFrameData() {
		Executors.newCachedThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				seeReportNumbers = getPreferences(MODE_PRIVATE);
				int AppNums = seeReportNumbers.getInt(REPORT_NUMBER, 0);
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("__ct__", String.valueOf(AppNums));
				MobclickAgent.onEvent(ProductDetailActivity.this, "report_num",
						map);
				seeReportNumbers.edit().putInt(REPORT_NUMBER, 0).commit();
			}
		});

		Intent intent = this.getIntent();
		if (intent.getSerializableExtra(Constants.APP) == null) {
			return;
		}
		app = (AppInformation) intent.getSerializableExtra(Constants.APP);
		loadInitData();
	}

	/**
	 * init textView and data
	 */
	private void initTextView() {
		TextView todaydatatext = (TextView) findViewById(R.id.todaydata_newuser_text_1);
		todaydatatext.setText(todayData.getNewUsers());
		todaydatatext = (TextView) findViewById(R.id.todaydata_activeuser_text);
		todaydatatext.setText(todayData.getActiveUsers());
		todaydatatext = (TextView) findViewById(R.id.todaydata_starttimes_text);
		todaydatatext.setText(todayData.getLaunches());

		todaydatatext = (TextView) findViewById(R.id.yesterdaydata_newuser_text);
		todaydatatext.setText(yesterdatData.getNewUsers());
		todaydatatext = (TextView) findViewById(R.id.yesterdaydata_activeuser_text);
		todaydatatext.setText(yesterdatData.getActiveUsers());
		todaydatatext = (TextView) findViewById(R.id.yesterdaydata_starttimes_text);
		todaydatatext.setText(yesterdatData.getLaunches());
		todaydatatext = (TextView) findViewById(R.id.today_installations);

		todaydatatext.setText(getResources().getString(R.string.account_users)
				+ "  " + todayData.getInstallations());

		durtionTimeTextView = (TextView) findViewById(R.id.duration_time);
		if (durationTimeBean != null && durationTimeBean.getAverage() != null) {
			durtionTimeTextView.setText(durationTimeBean.getAverage());
		}
		if (channelsBean != null) {
			((TextView) findViewById(R.id.channel_num)).setText("("
					+ channelsBean.size() + "个)");
		}
		if (versionsBean != null) {
			((TextView) findViewById(R.id.version_num)).setText("("
					+ versionsBean.size() + "个)");
		}
	}

	/**
	 * init chart view
	 */
	@SuppressWarnings("deprecation")
	private void initChartView() {

		LinearLayout imageView2 = (LinearLayout) findViewById(R.id.todaydata_image_3);
		BitmapManager.setChartData(this, chartDataBeanNewuser, imageView2,
				dayType, Constants.FORMAT_MM_DD, false,
				getString(R.string.today_data));
		imageView2 = (LinearLayout) findViewById(R.id.todaydata_image_5);
		BitmapManager.setChartData(this, chartDataBeanActiveuser, imageView2,
				dayType, Constants.FORMAT_MM_DD, false,
				getString(R.string.today_data));
		imageView2 = (LinearLayout) findViewById(R.id.todaydata_image_7);
		BitmapManager.setChartData(this, chartDataBeanLaunchs, imageView2,
				dayType, Constants.FORMAT_MM_DD, false,
				getString(R.string.today_data));
		BitmapManager.setChartData(this, chartDataBeanTodayNewAdd,
				todayNewAddLinearLayout, dayType, null, false,
				getString(R.string.today_data));
		BitmapManager.setChartData(this, chartDataBeanTodayLaunch,
				todayLaunchLinearLayout, dayType, null, false,
				getString(R.string.today_data));
		BitmapManager.setStackedBarChart(duringTimeLinearLayout,
				durationTimeBean, Constants.SINGLE_COLORS,
				new String[] { getString(R.string.today_data) });
		// set pie chart (channels)
		LinearLayout piechartChannel = (LinearLayout) findViewById(R.id.todaydata_piechart_channel);
		pieChartRenderer = new DefaultRenderer();
		pieChartSeries = new CategorySeries("");
		pieChartRenderer.setApplyBackgroundColor(true);
		pieChartRenderer.setLabelsColor(Color.parseColor("#9da1a3"));
		pieChartRenderer.setBackgroundColor(Color.TRANSPARENT);

		pieChartRenderer.setLegendHeight(1);
		pieChartRenderer.setInScroll(true);
		pieChartRenderer.setShowLabels(false);
		pieChartRenderer.setClickEnabled(true);
		pieChartRenderer.setPanEnabled(false);
		channleList = new ArrayList<String>();
		List<ChannelBean> channelsTop10 = new ArrayList<ChannelBean>();
		int limit = (channelsBean.size() > 7) ? 6 : channelsBean.size();
		int topTotalInstall = 0;
		for (int i = 0; i < limit; i++) {
			channelsTop10.add(channelsBean.get(i));
			topTotalInstall += Integer.valueOf(channelsBean.get(i)
					.getTotalInstall());
		}
		if (channelsBean.size() > 7) {
			channelsTop10.add(new ChannelBean(String.valueOf(Integer
					.valueOf(todayData.getInstallations()) - topTotalInstall),
					getString(R.string.other), null, null, null, null));
		}
		for (ChannelBean channelBean : channelsTop10) {
			pieChartSeries.add(channelBean.getChannel(),
					new Double(channelBean.getTotalInstall()));
			SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
			renderer.setColor(Constants.COLORS[(pieChartSeries.getItemCount() - 1)
					% Constants.COLORS.length]);
			pieChartRenderer.addSeriesRenderer(renderer);
		}
		for (ChannelBean channelbean : channelsBean) {
			if (!channleList.contains(channelbean.getChannel())) {
				channleList.add(channelbean.getChannel());
			}
		}
		mChartView = ChartFactory.getPieChartView(this, pieChartSeries,
				pieChartRenderer);
		piechartChannel.addView(mChartView, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		// set pie chart (versions)

		LinearLayout piechartVersion = (LinearLayout) findViewById(R.id.todaydata_piechart_version);
		pieChartRenderer = new DefaultRenderer();
		pieChartSeries = new CategorySeries("");
		pieChartRenderer.setApplyBackgroundColor(true);
		pieChartRenderer.setLabelsColor(Color.parseColor("#9da1a3"));
		pieChartRenderer.setBackgroundColor(Color.TRANSPARENT);
		pieChartRenderer.setLegendHeight(1);
		pieChartRenderer.setInScroll(true);
		pieChartRenderer.setShowLabels(false);
		pieChartRenderer.setClickEnabled(true);
		pieChartRenderer.setPanEnabled(false);
		versionList = new ArrayList<String>();
		List<AppVersion> versionsTop10 = new ArrayList<AppVersion>();
		limit = (versionsBean.size() > 7) ? 6 : versionsBean.size();
		topTotalInstall = 0;
		for (int j = 0; j < limit; j++) {
			versionsTop10.add(versionsBean.get(j));
			topTotalInstall += Integer.valueOf(versionsBean.get(j)
					.getTotalInstall());
		}
		if (versionsBean.size() > 7) {
			versionsTop10.add(new AppVersion(String.valueOf(Integer
					.valueOf(todayData.getInstallations()) - topTotalInstall),
					getString(R.string.other), null, null, null));
		}
		for (AppVersion appVersion : versionsTop10) {
			pieChartSeries.add(appVersion.getVersion(),
					new Double(appVersion.getTotalInstall()));
			SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
			renderer.setColor(Constants.COLORS[(pieChartSeries.getItemCount() - 1)
					% Constants.COLORS.length]);
			pieChartRenderer.addSeriesRenderer(renderer);
		}
		for (AppVersion version : versionsBean) {
			if (!versionList.contains(version.getVersion())) {
				versionList.add(version.getVersion());
			}
		}
		mChartView = ChartFactory.getPieChartView(ProductDetailActivity.this,
				pieChartSeries, pieChartRenderer);
		piechartVersion.addView(mChartView, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void startActivity(String type) {
		if ((versionsBean != null && versionsBean.size() != 0 && type
				.equals("version"))
				|| (channelsBean != null && channelsBean.size() != 0 && type
						.equals("channel"))) {
			Intent intent = new Intent(ProductDetailActivity.this,
					Top10Activity.class);
			intent.putExtra(Constants.APP, app);
			intent.putExtra(Constants.TYPE, type);
			intent.putExtra(Constants.TOTAL_INSTALL,
					todayData.getInstallations());
			Bundle bundle = new Bundle();
			ArrayList list = new ArrayList();
			list.add(channelsBean);
			list.add(versionsBean);
			bundle.putParcelableArrayList("list", list);
			intent.putExtra("mybundle", bundle);
			ProductDetailActivity.this.startActivity(intent);
			overridePendingTransition(R.anim.translate_activityin,
					R.anim.translate_activityout);
		} else if ((versionsBean == null || versionsBean.size() == 0)
				&& type.equals("version")) {
			ToastUtils.showMessageShort(ProductDetailActivity.this,
					R.string.no_version);
		} else {
			ToastUtils.showMessageShort(ProductDetailActivity.this,
					R.string.no_channel);
		}
	}

	/**
	 * start activity
	 * 
	 * @param type
	 * @param chartDataBean
	 */
	private void startActivity(String type, ChartDataBean chartDataBean) {
		if (type.equals(Constants.TYPE_NEW_ADD)) {
			if (chartDataBeanTodayNewAdd == null) {
				ToastUtils.showMessageShort(this, R.string.no_new_add);
				return;
			}
		} else if (type.equals(Constants.TYPE_LAUNCH)) {
			if (chartDataBeanTodayLaunch == null) {
				ToastUtils.showMessageShort(this, R.string.no_launch);
				return;
			}
		}
		Intent intent = new Intent(this, ProductTrendDetailActivity.class);
		intent.putExtra(Constants.TYPE, type);
		intent.putExtra(Constants.TYPE_DATA, chartDataBean);
		intent.putExtra(Constants.APP, app);
		startActivity(intent);
		overridePendingTransition(R.anim.translate_activityin,
				R.anim.translate_activityout);
	}

	@SuppressWarnings("unchecked")
	private void startActivity(int page) {
		Intent intent = new Intent(ProductDetailActivity.this,
				TrendActivity.class);
		intent.putExtra(Constants.APP, app);
		intent.putExtra(Constants.PAGE, page);
		intent.putExtra(Constants.DATA_TYPE, dayType);
		intent.putExtra("channelstate", "all");
		intent.putExtra("channelstateid", "");
		intent.putExtra("versionstate", "all");
		Bundle bundle = new Bundle();
		@SuppressWarnings("rawtypes")
		ArrayList list = new ArrayList();
		list.add(channelsBean);
		list.add(versionList);
		list.add(chartDataBeanNewuser);
		list.add(chartDataBeanActiveuser);
		list.add(chartDataBeanLaunchs);
		bundle.putParcelableArrayList("list", list);
		intent.putExtra("mybundle", bundle);
		ProductDetailActivity.this.startActivity(intent);
		overridePendingTransition(R.anim.translate_activityin,
				R.anim.translate_activityout);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			this.finish();
			overridePendingTransition(R.anim.translate_activityin_return,
					R.anim.translate_activityout_return);
			return true;
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * get chart data from service
	 * 
	 * @param datatype
	 * @return
	 */
	public ChartDataBean getChartData(int datatype) throws Exception {
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
		maps.put("start_date", getDayTypeString(LAST_MONTH));
		maps.put("end_date", StringUtil.getDateString(0));
		maps.put("period_type", "daily");
		String json = NetManager.getStingWithGet(path, maps);

		if (!versionState.equals("all")) {
			chartDataBean = DataParseManager.getChartDataBeansVersion(json,
					versionState);
		} else {
			chartDataBean = DataParseManager.getChartDataBeans(json,
					channelState);
		}
		return chartDataBean;
	}

	/**
	 * the url parameter of daytype
	 * 
	 * @return
	 */
	@SuppressLint({ "SimpleDateFormat", "SimpleDateFormat" })
	@SuppressWarnings("deprecation")
	public String getDayTypeString(int type) {
		String dataString = "";
		Date nowdate = new Date();
		switch (type) {
		case CURRENT_DATE:
			nowdate = new Date(nowdate.getYear(), nowdate.getMonth(),
					nowdate.getDate() - 1);
			break;

		case LAST_MONTH:
			nowdate = new Date(nowdate.getYear(), nowdate.getMonth(),
					nowdate.getDate() - dayType + 1);
			break;
		case TODAY:
			break;
		}
		dataString = DataParseManager.format.format(nowdate);
		return dataString;
	}

	/**
	 * the url parameter of sift
	 * 
	 * @return
	 */
	public void getSiftString(Map<String, String> maps) {
		if (channelState.equals("all") && versionState.equals("all")) {
			return;
		} else if (!(channelState.equals("all")) && versionState.equals("all")) {
			maps.put("channels", channelStateId);
		} else {
			maps.put("versions", versionState);
		}
	}

	private void createLoadingPopupwindow() {

		View view = this.getLayoutInflater().inflate(R.layout.loading_failed,
				null);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (popupWindowopupWindowLoadingFial.isShowing()) {
					popupWindowopupWindowLoadingFial.dismiss();
					todayDataLinearLayout.setVisibility(View.GONE);
					loadInitData();
				}
			}
		});
		if (popupWindowopupWindowLoadingFial == null) {
			popupWindowopupWindowLoadingFial = new PopupWindow(view,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}

	}

	/**
	 * integrate umeng analytics
	 */
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (time != -1) {
			time = System.currentTimeMillis() - time;
			time = time / 1000L;
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("__ct__", String.valueOf(time));
			MobclickAgent.onEvent(ProductDetailActivity.this,
					"dashboard_duration", map);
			time = -1;
		}
	}

	/**
	 * new a thread to get today and yesterday data from service
	 */
	public void loadInitData() {
		// loading dialog
		if (dialogLoading == null) {
			dialogLoading = DialogManager
					.getLoadingDialog(ProductDetailActivity.this);
		}
		if (dialogLoading != null && !dialogLoading.isShowing()) {
			if (!ProductDetailActivity.this.isFinishing()) {
				dialogLoading.show();
			}
		}

		threadPoll.execute((new Runnable() {
			@Override
			public void run() {
				Map<String, String> maps = new HashMap<String, String>();
				maps.put("auth_token", AppApplication.getInstance().getToken());
				maps.put("appkey", app.getAppkey());
				Message message = new Message();
				try {
					loadData(maps);
					if (chartDataBeanNewuser == null
							|| chartDataBeanActiveuser == null
							|| chartDataBeanLaunchs == null) {
						message.arg1 = Constants.MSG_FAIL;
						message.arg2 = EXCEPTION;
						return;
					} else {
						message.arg1 = Constants.MSG_SUCCESS;
						message.arg2 = initActivity;
					}
				} catch (Exception e) {
					e.printStackTrace();
					message.arg1 = Constants.MSG_FAIL;
					if (e instanceof AppException) {
						message.arg2 = no_data;
						message.obj = e;
					} else {
						message.arg2 = EXCEPTION;
					}
				} finally {
					handler.sendMessage(message);
				}
			}
		}));
	}

	/**
	 * load data from phone or server
	 * 
	 * @param maps
	 */
	@SuppressWarnings({ "unchecked" })
	private void loadData(Map<String, String> maps) throws Exception {
		if (refersh) {
			loadDataFromServer(maps);
			//refersh = false;
			return;
		}

		Object[] objects = DataStorageManager.readDataFromPhone(
				app.getAppkey(), Constants.FILE_NAMES);
		if ((objects == null)
				|| (objects.length != Constants.FILE_NAMES.length)) {
			loadDataFromServer(maps);
		} else {
			System.out.println("从SD card上获取数据");
			int i = 0;
			todayData = (BasicDayData) objects[i++];
			yesterdatData = (BasicDayData) objects[i++];
			channelsBean = (List<ChannelBean>) objects[i++];
			versionsBean = (List<AppVersion>) objects[i++];
			chartDataBeanNewuser = (ChartDataBean) objects[i++];
			chartDataBeanActiveuser = (ChartDataBean) objects[i++];
			chartDataBeanLaunchs = (ChartDataBean) objects[i++];
			chartDataBeanTodayNewAdd = (ChartDataBean) objects[i++];
			chartDataBeanTodayLaunch = (ChartDataBean) objects[i++];
			durationTimeBean = (DurationTimeBean) objects[i++];
			dayDurationTimeBean = (DurationTimeBean) objects[i];
		}
	}

	/**
	 * save data to phone
	 */
	private void saveDataToPhone() {
		if (todayData != null && yesterdatData != null && channelsBean != null
				&& versionsBean != null && chartDataBeanNewuser != null
				&& chartDataBeanActiveuser != null
				&& chartDataBeanLaunchs != null && durationTimeBean != null) {
			Object[] objects = { todayData, yesterdatData, channelsBean,
					versionsBean, chartDataBeanNewuser,
					chartDataBeanActiveuser, chartDataBeanLaunchs,
					chartDataBeanTodayNewAdd, chartDataBeanTodayLaunch,
					durationTimeBean, dayDurationTimeBean };
			DataStorageManager.saveDataToPhone(app.getAppkey(), objects);
		}
	}

	/**
	 * obtain data from server
	 * 
	 * @param maps
	 * @throws Exception
	 */
	private void loadDataFromServer(Map<String, String> maps) throws Exception {
		loadTodayData(maps);
		loadYesterdayData(maps);
		loadChannelData(maps);
		loadVersionData(maps);
		loadTodayNewAddData(maps);
		loadTodayLaunchData(maps);
		loadDurtionTimeData(maps);
		chartDataBeanNewuser = getChartData(0);
		chartDataBeanActiveuser = getChartData(1);
		chartDataBeanLaunchs = getChartData(2);
		saveDataToPhone();
	}

	/**
	 * load launch today
	 * 
	 * @param maps
	 * @throws Exception
	 */
	private void loadTodayLaunchData(Map<String, String> maps) throws Exception {
		maps.put("start_date", getDayTypeString(TODAY));
		maps.put("end_date", getDayTypeString(TODAY));
		maps.put("period_type", "hourly");
		String json = NetManager.getStingWithGet(Constants.LAUNCHES, maps);
		chartDataBeanTodayLaunch = DataParseManager.getChartDataBeansToday(
				json, "all", true);
	}

	/**
	 * load new adding data of today
	 * 
	 * @param maps
	 * @throws Exception
	 */
	private void loadTodayNewAddData(Map<String, String> maps) throws Exception {
		maps.put("start_date", getDayTypeString(TODAY));
		maps.put("end_date", getDayTypeString(TODAY));
		maps.put("period_type", "hourly");
		String json = NetManager.getStingWithGet(Constants.NEW_USER, maps);
		chartDataBeanTodayNewAdd = DataParseManager.getChartDataBeansToday(
				json, "all", true);
	}

	/**
	 * load durtion time data of today
	 * 
	 * @param maps
	 * @throws Exception
	 */
	private void loadDurtionTimeData(Map<String, String> maps) throws Exception {
		maps.put("start_date", getDayTypeString(CURRENT_DATE));
		maps.put("end_date", getDayTypeString(CURRENT_DATE));
		maps.put("period_type", "daily_per_launch");
		String json = NetManager.getStingWithGet(Constants.DURATION_TIME, maps);
		durationTimeBean = DataParseManager.getDurationTimeBean(json);
		maps.remove("period_type");
		maps.put("period_type", "daily");
		json = NetManager.getStingWithGet(Constants.DURATION_TIME, maps);
		dayDurationTimeBean = DataParseManager.getDurationTimeBean(json);
	}

	/**
	 * load version data from server
	 * 
	 * @param maps
	 * @throws Exception
	 */
	private void loadVersionData(Map<String, String> maps) throws Exception {
		String json = NetManager.getStingWithGet(Constants.VERSIONS, maps);
		List<AppVersion> tmpData = DataParseManager.getVersionBeans(json);
		List<AppVersion> swapData = new ArrayList<AppVersion>();
		if (versionsBean == null || !versionsBean.containsAll(tmpData)) {
			if (versionsBean == null) {
				versionsBean = tmpData;
			} else {
				tag: for (AppVersion appVersion : tmpData) {
					for (AppVersion appVersion2 : versionsBean) {
						if (appVersion2.getVersion().equals(
								appVersion.getVersion())) {
							continue tag;
						}
					}
					swapData.add(appVersion);
				}
				versionsBean.addAll(versionsBean.size(), swapData);
			}
		}
		tmpData = null;
		swapData = null;
	}

	/**
	 * load yesterday from server
	 * 
	 * @param maps
	 * @throws Exception
	 */
	private void loadChannelData(Map<String, String> maps) throws Exception {
		String json = NetManager.getStingWithGet(Constants.CHANNELS, maps);
		List<ChannelBean> tmpData = DataParseManager.getChannelBeans(json);
		List<ChannelBean> list = new ArrayList<ChannelBean>();
		if (channelsBean == null || !channelsBean.containsAll(tmpData)) {
			if (channelsBean == null) {
				channelsBean = tmpData;
			} else {
				Tag: for (ChannelBean channelBean : tmpData) {
					for (ChannelBean chBean : channelsBean) {
						if (channelBean.getChannel()
								.equals(chBean.getChannel())) {
							continue Tag;
						}
					}
					list.add(channelBean);
				}
				channelsBean.addAll(channelsBean.size(), list);
			}
		}
		tmpData = null;
		list = null;
	}

	/**
	 * load yesterday from server
	 * 
	 * @param maps
	 * @throws Exception
	 */
	private void loadYesterdayData(Map<String, String> maps) throws Exception {
		String json = NetManager.getStingWithGet(Constants.YESTERDY_DATA, maps);
		BasicDayData tmpData = DataParseManager.getTodayData(json);
		if (yesterdatData == null || !tmpData.equals(yesterdatData)) {
			yesterdatData = tmpData;
		}
	}

	/**
	 * load today data from server
	 * 
	 * @param maps
	 * @throws Exception
	 */
	private void loadTodayData(Map<String, String> maps) throws Exception {
		String json = NetManager.getStingWithGet(Constants.TODAY_DATA, maps);
		BasicDayData tmpData = DataParseManager.getTodayData(json);
		if (todayData == null || !tmpData.equals(todayData)) {
			todayData = tmpData;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View view) {
		if (time != -1) {
			time = System.currentTimeMillis() - time;
			time = time / 1000L;
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("__ct__", String.valueOf(time));
			MobclickAgent.onEvent(ProductDetailActivity.this,
					"dashboard_duration", map);
			time = -1;
		}
		seeReportNumbers = ProductDetailActivity.this
				.getPreferences(MODE_PRIVATE);
		int reportNums = seeReportNumbers.getInt(REPORT_NUMBER, 0);
		reportNums++;
		seeReportNumbers.edit().putInt(REPORT_NUMBER, reportNums).commit();
		switch (view.getId()) {
		case R.id.today_data_linerlayout_1:
			MobclickAgent.onEvent(ProductDetailActivity.this, "new_users");
			startActivity(0);
			break;
		case R.id.today_data_linerlayout_2:
			MobclickAgent.onEvent(ProductDetailActivity.this, "active_users");
			startActivity(1);
			break;
		case R.id.today_data_linerlayout_3:
			MobclickAgent.onEvent(ProductDetailActivity.this, "launchs");
			startActivity(2);
			break;
		case R.id.today_data_linerlayout_4:
			MobclickAgent.onEvent(ProductDetailActivity.this, "top10_channels");
			startActivity("channel");
			break;
		case R.id.today_data_linerlayout_5:
			MobclickAgent.onEvent(ProductDetailActivity.this, "top10_versions");
			startActivity("version");
			break;
		case R.id.today_new_add:
			MobclickAgent.onEvent(ProductDetailActivity.this, "today_new_add");
			startActivity(Constants.TYPE_NEW_ADD, chartDataBeanTodayNewAdd);
			break;
		case R.id.today_launch:
			MobclickAgent.onEvent(ProductDetailActivity.this, "launches_today");
			startActivity(Constants.TYPE_LAUNCH, chartDataBeanTodayLaunch);
			break;
		case R.id.duration_time_linerlayout:
			Intent intent = new Intent(this, DurationTimeDetailActivity.class);
			intent.putExtra(Constants.APP, app);
			intent.putExtra(DurationTimeDetailActivity.class.getName(),
					durationTimeBean);
			intent.putExtra("dayDatas", dayDurationTimeBean);
			startActivity(intent);
			break;
		case R.id.remain_user:
			Intent remainUserIntent = new Intent(this, RemainUserActivity.class);
			Bundle bundle = new Bundle();
			@SuppressWarnings("rawtypes")
			ArrayList list = new ArrayList();
			list.add(channelsBean);
			list.add(versionList);
			bundle.putParcelableArrayList("list", list);
			remainUserIntent.putExtra("bundle", bundle);
			remainUserIntent.putExtra(Constants.APP, app);
			startActivity(remainUserIntent);
			break;
		case R.id.event_count:
			MobclickAgent.onEvent(ProductDetailActivity.this, "event");
			Intent eventIntent = new Intent(this, EventActivity.class);
			eventIntent.putExtra(Constants.APP, app);
			eventIntent.putStringArrayListExtra("version", versionList);
			eventIntent.putStringArrayListExtra("channel", channleList);
			eventIntent.putStringArrayListExtra("channelIds", getChannelIds());
			startActivity(eventIntent);
			break;
		case R.id.feed_back:
			MobclickAgent.onEvent(ProductDetailActivity.this, "feedback");
			Intent feedBackIntent = new Intent(this, FeedbackActivity.class);
			feedBackIntent.putExtra(Constants.APP, app);
			startActivity(feedBackIntent);
			break;
		// case R.id.back:
		// finish();
		// break;
		}
	}

	private ArrayList<String> getChannelIds() {
		ArrayList<String> lists = new ArrayList<String>();
		for (ChannelBean channelBean : channelsBean) {
			lists.add(channelBean.getId());
		}
		return lists;
	}
}