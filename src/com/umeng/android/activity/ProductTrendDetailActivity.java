package com.umeng.android.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.adapter.TrendAdapter;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.ChartDataBean;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.android.exception.AppException;
import com.umeng.android.logic.DataParseManager;
import com.umeng.android.util.BitmapManager;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.StringUtil;
import com.umeng.client.R;

public class ProductTrendDetailActivity extends BaseActivity implements
		OnClickListener {

	private AppInformation app;
	private ChartDataBean chartDataBean;
	private ChartDataBean compareChartDataBean;
	private ListView listView;
	private TrendAdapter adapter;
	private LinearLayout graphicsLinearLayout;
	private TextView textNameTextView;
	private TextView textTitleTextView;
	private TextView yesterdayTextView;
	private TextView lastWeekTextView;
	private TextView lastMonthTextView;
	private TextView anyTextView;
	private TextView compareTimeTextView;
	private ImageView disapperImageView;
	private ImageView chooseImageView;
	private PopupWindow popupWindow;
	private String loadType;
	private int threadLock;
	private boolean dismiss = true;
	private Dialog loadingDialog;
	private List<ChartDataBean> lists = new ArrayList<ChartDataBean>();
	private boolean compare;
	private TextView timeTextView;
	private TextView userTypeTextView;
	private String[] titles = new String[2];
	private String startTimeString = "";
	private String result = "";
	private String path;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (loadingDialog != null) {
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
						loadData(null, true);
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
		setContentView(R.layout.activity_product_trend_detail);
		startTimeString = StringUtil.simpleDateFormat.format(new Date());
		result = StringUtil.simpleDateFormat.format(new Date());
		initFrameData();
		getSupportActionBar()
				.setTitle(StringUtil.cutString(app.getName(), 120));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.memu_calendar, menu);
		MenuItem refreshItem = menu.findItem(R.id.calendar);
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
		case R.id.calendar:
			MobclickAgent.onEvent(ProductTrendDetailActivity.this,
					"choose_date");
			Intent intent = new Intent(Intent.ACTION_PICK).setDataAndType(null,
					CalendarActivity.MIME_TYPE);
			intent.putExtra("date", startTimeString);
			startActivityForResult(intent, 0);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * init relation view
	 */
	private void initFrameView() {

		titles[0] = startTimeString + getString(R.string.data);
		listView = (ListView) findViewById(R.id.listView);
		adapter = new TrendAdapter(this, chartDataBean, Constants.FORMAT_HH_MM);
		listView.setAdapter(adapter);
		graphicsLinearLayout = (LinearLayout) findViewById(R.id.trend_single_gestrue);

		lists.clear();
		lists.add(chartDataBean);
		if (compareChartDataBean != null) {
			lists.add(compareChartDataBean);
		}

		if (compare) {
			BitmapManager.setChartData(this, lists, graphicsLinearLayout, 7,
					Constants.FORMAT_HH_MM, true, titles, null);
		} else {
			BitmapManager
					.setChartData(this, lists, graphicsLinearLayout, 7,
							Constants.FORMAT_HH_MM, true,
							new String[] { startTimeString
									+ getString(R.string.data) },
							getTimeChartListener());
		}
		compareTimeTextView = (TextView) findViewById(R.id.compare_text_title);
		if (compareChartDataBean != null && compare) {
			((RelativeLayout) compareTimeTextView.getParent())
					.setVisibility(View.VISIBLE);
			compareTimeTextView.setText(result);
		} else {
			((RelativeLayout) compareTimeTextView.getParent())
					.setVisibility(View.GONE);
		}
		compare = false;
		textNameTextView = (TextView) findViewById(R.id.text_name);
		if (loadType.equals(Constants.TYPE_NEW_ADD)) {
			textNameTextView.setText(R.string.new_users);
		} else if (loadType.equals(Constants.TYPE_LAUNCH)) {
			textNameTextView.setText(R.string.launch_user);
		}
		textTitleTextView = (TextView) findViewById(R.id.text_title);
		textTitleTextView.setText(startTimeString);
		chooseImageView = (ImageView) findViewById(R.id.choose_time);
		chooseImageView.setOnClickListener(this);
		timeTextView = (TextView) findViewById(R.id.time);
		timeTextView.setText(R.string.period);
		userTypeTextView = (TextView) findViewById(R.id.trend_single_listview_title);
		if (loadType.equals(Constants.TYPE_NEW_ADD)) {
			userTypeTextView.setText(R.string.new_users);
		} else {
			userTypeTextView.setText(R.string.launch_user);
		}
	}

	/**
	 * init frame data
	 */
	private void initFrameData() {
		if (getIntent().getSerializableExtra(Constants.APP) == null) {
			return;
		}
		app = (AppInformation) getIntent().getSerializableExtra(Constants.APP);
		chartDataBean = (ChartDataBean) getIntent().getSerializableExtra(
				Constants.TYPE_DATA);
		loadType = getIntent().getStringExtra(Constants.TYPE);
		if (loadType.equals(Constants.TYPE_NEW_ADD)) {
			path = Constants.NEW_USER;
		} else {
			path = Constants.LAUNCHES;
		}
		loadData(Constants.TYPE_TODAY, false);
	}

	private void loadData(final String type, final boolean isCompare) {
		if (chartDataBean != null && !isCompare) {
			initFrameView();
			return;
		}
		if (loadingDialog == null) {
			loadingDialog = DialogManager.getLoadingDialog(this);
		}
		if (loadingDialog != null && !loadingDialog.isShowing()
				&& !ProductTrendDetailActivity.this.isFinishing()) {
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
					maps.put("period_type", "hourly");
					maps.put("start_date", startTimeString);
					maps.put("end_date", startTimeString);
					String json = "";
					json = NetManager.getStingWithGet(path, maps);
					if (isCompare) {
						chartDataBean = DataParseManager
								.getChartDataBeansToday(json, "all", true);
						maps.remove("start_date");
						maps.remove("end_date");
						maps.put("end_date", result);
						maps.put("start_date", result);
						json = NetManager.getStingWithGet(path, maps);
						compareChartDataBean = DataParseManager
								.getChartDataBeansToday(json, "all", false);
					} else {
						chartDataBean = DataParseManager
								.getChartDataBeansToday(json, "all", false);
					}
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.choose_time:
			// create popupWindow
			showPopupWindow();
			break;
		case R.id.yesterday:
			compare = true;
			// MobclickAgent.onEvent(ProductTrendDetailActivity.this,
			// "yesterday_compare");
			result = StringUtil.getDataString(
					StringUtil.getDateFromString(startTimeString), 1);
			titles[1] = result + getString(R.string.data);
			loadData(Constants.TYPE_YESTERDAY, true);
			popupWindow.dismiss();
			break;
		case R.id.last_week:
			compare = true;
			// MobclickAgent.onEvent(ProductTrendDetailActivity.this,
			// "weekly_compare");
			result = StringUtil.getDataString(
					StringUtil.getDateFromString(startTimeString), 7);
			titles[1] = result + getString(R.string.data);
			loadData(Constants.TYPE_LAST_WEEK, true);
			popupWindow.dismiss();
			break;
		case R.id.last_month:
			compare = true;
			// MobclickAgent.onEvent(ProductTrendDetailActivity.this,
			// "month_compare");
			result = StringUtil.getDataString(
					StringUtil.getDateFromString(startTimeString), 30);
			titles[1] = result + getString(R.string.data);
			loadData(Constants.TYPE_LAST_MONTH, true);
			popupWindow.dismiss();
			break;
		case R.id.any:
			compare = true;
			// MobclickAgent.onEvent(ProductTrendDetailActivity.this,
			// "other_compare");
			Intent intent = new Intent(Intent.ACTION_PICK).setDataAndType(null,
					CalendarActivity.MIME_TYPE);
			intent.putExtra("date", result);
			startActivityForResult(intent, 0);
			popupWindow.dismiss();
			break;
		case R.id.choose_disapper:
			popupWindow.dismiss();
			break;

		}
	}

	/**
	 * show a popup window
	 */
	private void showPopupWindow() {
		createPopUpWindow();
		if (dismiss) {
			popupWindow.showAtLocation(this.findViewById(R.id.choose_time),
					Gravity.RIGHT | Gravity.BOTTOM, 130, 0);
			dismiss = false;
		} else {
			popupWindow.dismiss();
		}
	}

	/**
	 * create a popupWindow
	 */
	@SuppressWarnings("deprecation")
	private void createPopUpWindow() {
		LayoutInflater inflate = this.getLayoutInflater();
		View view = (LinearLayout) inflate.inflate(R.layout.time_choose, null);
		view.findViewById(R.id.top_line).setLayoutParams(
				new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
						AppApplication.height / 15));
		yesterdayTextView = (TextView) view.findViewById(R.id.yesterday);
		yesterdayTextView.setOnClickListener(this);
		lastWeekTextView = (TextView) view.findViewById(R.id.last_week);
		lastWeekTextView.setOnClickListener(this);
		lastMonthTextView = (TextView) view.findViewById(R.id.last_month);
		lastMonthTextView.setOnClickListener(this);
		anyTextView = (TextView) view.findViewById(R.id.any);
		anyTextView.setOnClickListener(this);
		disapperImageView = (ImageView) view.findViewById(R.id.choose_disapper);
		disapperImageView.setOnClickListener(this);
		popupWindow = new PopupWindow(view, AppApplication.width,
				AppApplication.height / 12);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popupWindow.setAnimationStyle(R.style.popupwindow);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				dismiss = true;
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			chartDataBean = null;
			compareChartDataBean = null;
			if (compare) {
				result = data.getStringExtra("result");
				titles[1] = result + getString(R.string.data);
				loadData(null, true);
			} else {
				// choose determination day
				startTimeString = data.getStringExtra("result");
				titles[0] = startTimeString + getString(R.string.data);
				loadData(null, false);
			}
		}
	}

	private OnClickListener getTimeChartListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				final int pos;
				if (threadLock == 0) {
					threadLock = -1;
					GraphicalView mv = (GraphicalView) v;
					SeriesSelection seriesSelection = mv
							.getCurrentSeriesAndPoint();
					if (seriesSelection == null) {
					} else {
						pos = seriesSelection.getPointIndex();
						// update listview
						adapter.setSelection(adapter.getCount() - 1 - pos);
						listView.setSelection(adapter.getCount() - 1 - pos);
					}
					threadLock = 0;
				}
			}
		};
	}
}
