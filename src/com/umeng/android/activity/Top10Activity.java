package com.umeng.android.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.adapter.Top10ListAdapter;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.AppVersion;
import com.umeng.android.bean.ChannelBean;
import com.umeng.android.bean.VesinAndChannelBean;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.util.StringUtil;
import com.umeng.android.util.ToastUtils;
import com.umeng.client.R;

public class Top10Activity extends BaseActivity {
	private TextView teViewline;
	private String dataType = "channel";
	private TextView markTextView;
	private TextView typeTextView;
	private ImageView titleImageView;
	// pie chart
	private CategorySeries pieChartSeries = new CategorySeries("");
	private DefaultRenderer pieChartRenderer = new DefaultRenderer();
	private GraphicalView mChartView;
	private AppInformation app = null;
	// top 10 datas for version and channel
	private List<AppVersion> versionsBean = null;
	private List<ChannelBean> channelsBean = null;
	private List<AppVersion> versionsTop7 = new ArrayList<AppVersion>();
	private List<ChannelBean> channelsTop7 = new ArrayList<ChannelBean>();
	private List<AppVersion> versionsTop10 = new ArrayList<AppVersion>();
	private List<ChannelBean> channelsTop10 = new ArrayList<ChannelBean>();
	// top10 listview
	private ListView listviewTop10;
	private int totalInstall;

	@SuppressLint("UseValueOf")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_top10);
		initFrameData();
		initFrameView();
		getSupportActionBar()
				.setTitle(StringUtil.cutString(app.getName(), 120));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

	/**
	 * init relation view
	 */
	private void initFrameView() {

		teViewline = new TextView(Top10Activity.this);
		teViewline.setHeight(1);
		teViewline.setBackgroundColor(Color.parseColor("#33e0e0e0"));

		markTextView = (TextView) this
				.findViewById(R.id.top10_title_mark_text_1);
		typeTextView = (TextView) this.findViewById(R.id.top10_type_2);
		titleImageView = (ImageView) this
				.findViewById(R.id.top10_title_mark_type);

		listviewTop10 = (ListView) this
				.findViewById(R.id.top10_channel_listview_1);
		listviewTop10.addFooterView(teViewline);

		setListview();

		if (dataType.equals("channel")) {
			// channel
			markTextView.setText(R.string.top10_channel);
			typeTextView.setText(R.string.top10_channel);
			titleImageView.setImageResource(R.drawable.home_icon_channel);
		} else {
			// version
			markTextView.setText(R.string.top10_version);
			typeTextView.setText(R.string.top10_version);
			titleImageView.setImageResource(R.drawable.home_icon_ver);
		}

		createPieChart();

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
		dataType = intent.getStringExtra(Constants.TYPE);
		Bundle mybundle = intent.getBundleExtra("mybundle");
		totalInstall = Integer.valueOf(intent
				.getStringExtra(Constants.TOTAL_INSTALL));
		@SuppressWarnings("rawtypes")
		ArrayList list = mybundle.getParcelableArrayList("list");
		channelsBean = (List<ChannelBean>) list.get(0);
		versionsBean = (List<AppVersion>) list.get(1);
		int limit = (channelsBean.size() > 7) ? 6 : channelsBean.size();
		int topTotalInstall = 0;
		for (int i = 0; i < limit; i++) {
			channelsTop7.add(channelsBean.get(i));
			topTotalInstall += Integer.valueOf(channelsBean.get(i)
					.getTotalInstall());
		}
		if (channelsBean.size() > 7) {
			channelsTop7.add(new ChannelBean(String.valueOf(totalInstall
					- topTotalInstall), getString(R.string.other), null, null,
					null, null));
		}
		limit = (channelsBean.size() > 10) ? 10 : channelsBean.size();
		for (int i = 0; i < limit; i++) {
			channelsTop10.add(channelsBean.get(i));
		}

		limit = (versionsBean.size() > 7) ? 6 : versionsBean.size();
		topTotalInstall = 0;
		for (int j = 0; j < limit; j++) {
			versionsTop7.add(versionsBean.get(j));
			topTotalInstall += Integer.valueOf(versionsBean.get(j)
					.getTotalInstall());
		}
		if (versionsBean.size() > 7) {
			versionsTop7.add(new AppVersion(String.valueOf(totalInstall
					- topTotalInstall), getString(R.string.other), null, null,
					null));
		}
		limit = (versionsBean.size() > 10) ? 10 : versionsBean.size();
		for (int i = 0; i < limit; i++) {
			versionsTop10.add(versionsBean.get(i));
		}
	}

	/**
	 * create pie chart
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("UseValueOf")
	private void createPieChart() {
		LinearLayout linearLayout = (LinearLayout) this
				.findViewById(R.id.top10_pie);
		// pie chart for channel
		pieChartRenderer.setApplyBackgroundColor(true);
		pieChartRenderer.setChartTitleTextSize(25);
		pieChartRenderer.setLabelsTextSize(AppApplication.width / 30);
		pieChartRenderer.setLabelsColor(Color.parseColor("#9da1a3"));
		pieChartRenderer.setLegendTextSize(10);
		pieChartRenderer.setMargins(new int[] { 0, 30, 0, 30 });
		pieChartRenderer.setStartAngle(270);
		pieChartRenderer.setBackgroundColor(Color.TRANSPARENT);
		pieChartRenderer.setLegendHeight(1);
		pieChartRenderer.setClickEnabled(true);
		pieChartRenderer.setPanEnabled(false);
		SimpleSeriesRenderer renderer = null;
		if (dataType.equals("version")) {
			for (AppVersion version : versionsTop7) {
				pieChartSeries.add(version.getVersion(),
						new Double(version.getTotalInstall()));
				renderer = new SimpleSeriesRenderer();
				renderer.setColor(Constants.COLORS[(pieChartSeries
						.getItemCount() - 1) % Constants.COLORS.length]);
				pieChartRenderer.addSeriesRenderer(renderer);
			}
		} else {
			for (ChannelBean channelbean : channelsTop7) {
				pieChartSeries.add(channelbean.getChannel(), new Double(
						channelbean.getTotalInstall()));
				renderer = new SimpleSeriesRenderer();
				renderer.setColor(Constants.COLORS[(pieChartSeries
						.getItemCount() - 1) % Constants.COLORS.length]);
				pieChartRenderer.addSeriesRenderer(renderer);
			}
		}
		mChartView = ChartFactory.getPieChartView(Top10Activity.this,
				pieChartSeries, pieChartRenderer);
		mChartView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SeriesSelection seriesSelection = mChartView
						.getCurrentSeriesAndPoint();
				if (seriesSelection != null) {
					int pos = seriesSelection.getPointIndex();
					if (dataType.equals("channel")) {
						if (pos > channelsTop7.size() - 1) {
							pos = channelsTop7.size() - 1;
						}
						ToastUtils.showMessageShort(
								Top10Activity.this,
								channelsTop7.get(pos).getChannel()
										+ getResources().getString(
												R.string.account_users)
										+ channelsTop7.get(pos)
												.getTotalInstall());
					} else {
						if (pos > versionsTop7.size() - 1) {
							pos = versionsTop7.size() - 1;
						}
						ToastUtils.showMessageShort(
								Top10Activity.this,
								versionsTop7.get(pos).getVersion()
										+ getResources().getString(
												R.string.account_users)
										+ versionsTop7.get(pos)
												.getTotalInstall());
					}
				}
			}
		});
		linearLayout.addView(mChartView, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	public void setListview() {
		List<VesinAndChannelBean> lists = new ArrayList<VesinAndChannelBean>();
		if (dataType.equals("version")) {
			VesinAndChannelBean versinbean = null;
			for (AppVersion appVersion : versionsTop10) {
				versinbean = new VesinAndChannelBean(appVersion.getVersion(),
						Integer.valueOf(appVersion.getTodayInstall()),
						Integer.valueOf(appVersion.getActiveUser()), Float
								.valueOf(appVersion.getTotalInstallRate())
								.floatValue());
				lists.add(versinbean);
			}
		} else {
			VesinAndChannelBean channelBean = null;
			for (ChannelBean channeltemp : channelsTop10) {
				channelBean = new VesinAndChannelBean(channeltemp.getChannel(),
						Integer.valueOf(channeltemp.getInstall()),
						Integer.valueOf(channeltemp.getActiveUser()), Float
								.valueOf(channeltemp.getTotalInstallRate())
								.floatValue());
				lists.add(channelBean);
			}
		}
		Top10ListAdapter top10Adapter = new Top10ListAdapter(
				Top10Activity.this, lists);
		listviewTop10.setAdapter(top10Adapter);
		listviewTop10.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				HashMap<String, String> top10_to_trend = new HashMap<String, String>();
				top10_to_trend.put("sift_type", "channels");
				MobclickAgent.onEvent(Top10Activity.this, "top10_to_trend",
						top10_to_trend);
				Intent intent = new Intent(Top10Activity.this,
						TrendActivity.class);
				intent.putExtra(Constants.APP, app);
				intent.putExtra("page", 0);
				if (dataType.equals("version")) {
					intent.putExtra("channelstate", "all");
					intent.putExtra("channelstateid", "");
					// sometimes versionsTop10.size()=1,but occour position=1 on
					// os3.1 ,
					// maybe it's difference all verson of os
					if (position >= versionsTop10.size()) {
						intent.putExtra("versionstate",
								versionsTop10.get(versionsTop10.size() - 1)
										.getVersion());
					} else {
						intent.putExtra("versionstate",
								versionsTop10.get(position).getVersion());
					}
				} else {
					if (position >= channelsTop10.size()) {
						intent.putExtra("channelstate",
								channelsTop10.get(channelsTop10.size() - 1)
										.getChannel());
						intent.putExtra("channelstateid",
								channelsTop10.get(channelsTop10.size() - 1)
										.getId());
					} else {
						intent.putExtra("channelstate",
								channelsTop10.get(position).getChannel());
						intent.putExtra("channelstateid",
								channelsTop10.get(position).getId());
					}
					intent.putExtra("versionstate", "all");
				}
				Bundle bundle = new Bundle();
				@SuppressWarnings("rawtypes")
				ArrayList list = new ArrayList();
				ArrayList<String> tmpsAppVersions = new ArrayList<String>();
				for (AppVersion version : versionsBean) {
					tmpsAppVersions.add(version.getVersion());
				}
				list.add(channelsBean);
				list.add(tmpsAppVersions);
				bundle.putParcelableArrayList("list", list);
				intent.putExtra("mybundle", bundle);
				Top10Activity.this.startActivity(intent);
				overridePendingTransition(R.anim.translate_activityin,
						R.anim.translate_activityout);
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			// if(activitys.get(activitys.size()-1) instanceof Top10Activity){
			// activitys.remove(activitys.size()-1);
			// }
			// this.finish();
			overridePendingTransition(R.anim.translate_activityin_return,
					R.anim.translate_activityout_return);
			break;
		}
		return super.onKeyDown(keyCode, event);
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

}
