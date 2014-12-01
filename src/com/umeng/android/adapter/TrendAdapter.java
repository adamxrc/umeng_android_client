package com.umeng.android.adapter;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.android.bean.ChartDataBean;
import com.umeng.android.common.Constants;
import com.umeng.android.util.StringUtil;
import com.umeng.client.R;

public class TrendAdapter extends BaseAdapter {

	private Context context;
	private ChartDataBean chartDataBean;
	private int selection = -1;
	// private LinearLayout[] linearLayouts;
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"MM-dd");
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(
			"HH");
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(
			"yyyy-MM-dd");
	private SimpleDateFormat currentSimpleDateFormat;

	public TrendAdapter(Context c, ChartDataBean chartDataBean,
			String format_type) {
		this.context = c;
		this.chartDataBean = chartDataBean;
		// if(chartDataBean!=null&&chartDataBean.getData()!=null&&chartDataBean.getData().length
		// > 0){
		// linearLayouts = new LinearLayout[chartDataBean.getData().length];
		// }
		if (format_type.equals(Constants.FORMAT_HH_MM)) {
			currentSimpleDateFormat = simpleDateFormat1;
		} else if (format_type.equals(Constants.FORMAT_MM_DD)) {
			currentSimpleDateFormat = simpleDateFormat;
		} else {
			currentSimpleDateFormat = simpleDateFormat2;
		}
	}

	public void setSelection(int selection) {
		this.selection = selection;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return (chartDataBean == null) ? 0 : chartDataBean.getData().length;
	}

	@Override
	public Object getItem(int position) {
		return (chartDataBean == null || chartDataBean.getData() == null || position >= chartDataBean
				.getData().length) ? null : chartDataBean.getData()[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder = null;
		if (view == null) {
			LayoutInflater factory = LayoutInflater.from(context);
			view = (LinearLayout) factory.inflate(R.layout.trend_listview_item,
					null);
			holder = new ViewHolder();
			holder.dateText = (TextView) view
					.findViewById(R.id.trend_single_listview_text_1);
			holder.numberText = (TextView) view
					.findViewById(R.id.trend_single_listview_text_2);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		holder.dateText.setText(currentSimpleDateFormat.format(chartDataBean
				.getDates()[chartDataBean.getDates().length - 1 - position]));
		holder.numberText
				.setText(StringUtil.cutInteger((int) chartDataBean.getData()[chartDataBean
						.getData().length - 1 - position])
						+ "");
		// linearLayouts[position] = (LinearLayout) view;
		if (position == selection) {
			view.setBackgroundColor(Color.parseColor("#E7E7E7"));
		} else {
			view.setBackgroundColor(Color.TRANSPARENT);
		}

		return view;
	}

	/**
	 * @param chartDataBean
	 *            the chartDataBean to set
	 */
	public void setChartDataBean(ChartDataBean chartDataBean) {
		this.chartDataBean = chartDataBean;
		notifyDataSetChanged();
	}

	// /**
	// * @return the linearLayouts
	// */
	// public LinearLayout[] getLinearLayouts() {
	// return linearLayouts;
	// }

	private static class ViewHolder {
		TextView dateText;
		TextView numberText;
	}
}
