package com.umeng.android.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint.Align;
import android.graphics.drawable.BitmapDrawable;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.umeng.android.bean.ChartDataBean;
import com.umeng.android.bean.DurationTimeBean;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.client.R;

/**
 * a utils to transformation bitmap
 */
public class BitmapManager {

	// private static final String TAG = BitmapManager.class.getName();
	/**
	 * change bitmap to absolute width and height
	 * 
	 * @param bm
	 * @param x
	 * @param y
	 * @return
	 */
	public static Bitmap changeBitmapByHW(Bitmap bm, int x, int y) {
		if (x <= 0 || y <= 0) {
			return null;
		}
		int width = bm.getWidth();
		int height = bm.getHeight();
		float widthchange = ((float) x) / width;
		float heightchange = ((float) y) / height;

		Matrix mt = new Matrix();
		mt.postScale(widthchange, heightchange);
		Bitmap bm2 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
				bm.getHeight(), mt, true);
		return bm2;
	}

	public static Bitmap getBitmapFromDrawable(Context c, int drawable) {
		return ((BitmapDrawable) c.getResources().getDrawable(drawable))
				.getBitmap();
	}

	/**
	 * get line chart view that the type of x axis is date
	 * 
	 * @param c
	 * @param timenumbers
	 * @param ymin
	 * @param ymax
	 * @param titles
	 * @param xValues
	 * @param yValues
	 * @param sx
	 * @param sy
	 * @return
	 */
	public static GraphicalView getTimeChartView(Context c, int timenumbers,
			double ymin, double ymax, String[] titles, List<Date[]> xValues,
			List<double[]> yValues, double sx, double sy, String format) {
		XYMultipleSeriesRenderer renderer = null;
		if (titles.length == 1) {
			renderer = getDemoRenderer(Constants.SINGLE_COLORS,
					new PointStyle[] { Constants.STYLES[0] }, timenumbers,
					ymax, ymin);
		} else {
			renderer = getDemoRenderer(Constants.COMPARE_COLORS,
					Constants.STYLES, timenumbers, ymax, ymin);
		}
		XYMultipleSeriesDataset dataset = buildDateDataset(titles, xValues,
				yValues);
		return ChartFactory.getTimeChartView(c, dataset, renderer, format);
	}

	/**
	 * get renderer
	 * 
	 * @param timenumbers
	 * @param ymin
	 * @param ymax
	 * @param sx
	 * @param sy
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static XYMultipleSeriesRenderer getDemoRenderer(int[] colors,
			PointStyle[] styles, int timenumbers, double ymax, double ymin) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) renderer.getSeriesRendererAt(i))
					.setFillPoints(true);
		}
		renderer.setShowLegend(false);
		renderer.setAxisTitleTextSize((float) (AppApplication.width / 42.667));
		renderer.setChartTitleTextSize((float) (AppApplication.width / 42.667));
		renderer.setLabelsTextSize((float) (AppApplication.width / 42.667));
		renderer.setLegendTextSize((float) (AppApplication.width / 42.667));
		renderer.setXLabelsColor(AppApplication.getInstance().getResources()
				.getColor(R.color.textColor));
		renderer.setYLabelsColor(0, AppApplication.getInstance().getResources()
				.getColor(R.color.textColor));
		renderer.setAxesColor(Color.TRANSPARENT);
		renderer.setBackgroundColor(Color.argb(0, 0xff, 0, 0));
		renderer.setMarginsColor(Color.argb(0, 0xff, 0, 0));
		renderer.setXLabels(timenumbers);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setXLabelsAlign(Align.CENTER);
		renderer.setClickEnabled(true);
		renderer.setPointSize((float) (AppApplication.width / 160 - 0.5));
		renderer.setPanEnabled(false, false);
		int len = colors.length;
		for (int i = 0; i < len; i++) {
			XYSeriesRenderer sRender = new XYSeriesRenderer();
			sRender.setColor(colors[i]);
			if (styles.length > i)
				sRender.setPointStyle(styles[i]);
			sRender.setFillPoints(true);
			sRender.setFillBelowLine(true);
			sRender.setLineWidth(AppApplication.width / 160);
			sRender.setFillBelowLineColor(Color.parseColor("#2245b7ff"));
			renderer.addSeriesRenderer(sRender);
		}
		if (ymax < 5) {
			renderer.setYLabels((int) ymax + 1);
			renderer.setYAxisMax((int) ymax + 1);
			renderer.setYAxisMin(0);
			ymax = ymax + 1;
		} else {
			renderer.setYAxisMax(ymax + (ymax - ymin) / 3);
			renderer.setYAxisMin(((ymin - (ymax - ymin) / 3) > 0) ? ymin
					- (ymax - ymin) / 3 : 0);
			ymax = ymax + (ymax - ymin) / 3;
		}
		renderer.setMargins(new int[] { AppApplication.width / 50,
				AppApplication.width * (StringUtil.getLength(ymax) + 1) / 75,
				2, AppApplication.width / 50 });
		return renderer;
	}

	/**
	 * get renderer without axis
	 * 
	 * @param timenumbers
	 * @param ymin
	 * @param ymax
	 * @param sx
	 * @param sy
	 * @return
	 */
	public static XYMultipleSeriesRenderer getDemoRenderer_noaxis(
			int timenumbers, double ymin, double ymax) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize((float) (AppApplication.width / 42.667));
		renderer.setChartTitleTextSize((float) (AppApplication.width / 42.667));
		renderer.setLabelsTextSize((float) (AppApplication.width / 42.667));
		renderer.setLegendTextSize((float) (AppApplication.width / 42.667));
		renderer.setInScroll(true);
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setPointStyle(PointStyle.CIRCLE);
		r.setColor(android.graphics.Color.parseColor("#45b7ff"));
		r.setLineWidth((float) (AppApplication.width / 160));
		r.setFillPoints(true);
		renderer.addSeriesRenderer(r);
		renderer.setPointSize((float) (AppApplication.width / 160 - 2.0));
		renderer.setXLabelsColor(Color.TRANSPARENT);
		renderer.setYLabelsColor(0, Color.TRANSPARENT);
		renderer.setAxesColor(Color.TRANSPARENT);
		renderer.setBackgroundColor(Color.argb(0, 0xff, 0, 0));
		renderer.setMarginsColor(Color.argb(0, 0xff, 0, 0));
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.setYLabels(5);
		renderer.setMargins(new int[] { 2, 10, 2, 10 });
		renderer.setLegendHeight(1);
		renderer.setClickEnabled(true);
		renderer.setPanEnabled(false, false);
		if (ymax < 5) {
			renderer.setYLabels((int) ymax + 1);
			renderer.setYAxisMax((int) ymax + 1);
			renderer.setYAxisMin(0);
		} else {
			renderer.setYAxisMax(ymax + (ymax - ymin) / 10);
			renderer.setYAxisMin(((ymin - (ymax - ymin) / 10) > 0) ? ymin
					- (ymax - ymin) / 10 : 0);
		}
		return renderer;
	}

	/**
	 * @param titles
	 * @param xValues
	 * @param yValues
	 * @return
	 */
	public static XYMultipleSeriesDataset buildDateDataset(String[] titles,
			List<Date[]> xValues, List<double[]> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length;
		if (titles.length == xValues.size()) {
			for (int i = 0; i < length; i++) {
				TimeSeries series = new TimeSeries(titles[i]);
				Date[] xV = xValues.get(i);
				double[] yV = yValues.get(i);
				int seriesLength = xV.length;
				for (int k = 0; k < seriesLength; k++) {
					series.add(xV[k], yV[k]);
				}
				series.setTitle(titles[i]);
				dataset.addSeries(series);
			}
		}
		return dataset;
	}

	/**
	 * get chart line witch do not show the axis
	 * 
	 * @param c
	 * @param timenumbers
	 * @param ymin
	 * @param ymax
	 * @param titles
	 * @param xValues
	 * @param yValues
	 * @param sx
	 * @param sy
	 * @return
	 */
	public static GraphicalView getTimeChartView_noaxis(Context c,
			int timenumbers, double ymin, double ymax, String[] titles,
			List<Date[]> xValues, List<double[]> yValues, double sx, double sy,
			String format) {
		XYMultipleSeriesRenderer renderer = getDemoRenderer_noaxis(timenumbers,
				ymin, ymax);
		XYMultipleSeriesDataset dataset = buildDateDataset(titles, xValues,
				yValues);
		return ChartFactory.getTimeChartView(c, dataset, renderer, format);
	}

	/**
	 * @param context
	 * @param chartDataBean
	 * @param chartliLayout
	 * @param daytype
	 * @param format
	 * @param isShowTable
	 */
	public static void setChartData(Context context,
			ChartDataBean chartDataBean, LinearLayout chartliLayout,
			int daytype, String format, boolean isShowTable, String title) {
		if (chartDataBean == null) {
			return;
		}
		List<ChartDataBean> lists = new ArrayList<ChartDataBean>();
		lists.add(chartDataBean);
		setChartData(context, lists, chartliLayout, daytype, format,
				isShowTable, new String[] { title }, null);
	}

	/**
	 * @param context
	 * @param chartDataBean
	 * @param chartliLayout
	 * @param daytype
	 * @param format
	 * @param isShowTable
	 */
	public static void setChartData(Context context,
			ChartDataBean chartDataBean, LinearLayout chartliLayout,
			int daytype, String format, boolean isShowTable, String title,
			OnClickListener listener) {
		if (chartDataBean == null) {
			return;
		}
		List<ChartDataBean> lists = new ArrayList<ChartDataBean>();
		lists.add(chartDataBean);
		setChartData(context, lists, chartliLayout, daytype, format,
				isShowTable, new String[] { title }, listener);
	}

	/**
	 * set datas for chart
	 * 
	 * @param chartDataBean
	 * @param imageview
	 */
	@SuppressWarnings("deprecation")
	public static void setChartData(Context context,
			List<ChartDataBean> chartDataBeanList, LinearLayout chartliLayout,
			int daytype, String format, boolean isShowTable, String[] titles,
			OnClickListener listener) {
		int xnumbers = 0;
		if (daytype == 7 || daytype == 4) {
			xnumbers = 1;
		} else if (daytype == 15) {
			xnumbers = 3;
			// xnumbers = 1;
		} else if (daytype == 30) {
			xnumbers = 6;
			// xnumbers = 1;
		} else if (daytype == 90) {
			xnumbers = 15;
			// xnumbers = 1;
		} else if (daytype == 180) {
			xnumbers = 30;
		} else if (daytype == 360) {
			xnumbers = 60;
		} else {
			xnumbers = 360000 / 6;
		}

		// fill datas for chart
		List<Date[]> dates = new ArrayList<Date[]>();
		List<double[]> values = new ArrayList<double[]>();
		for (ChartDataBean chartDataBean : chartDataBeanList) {
			dates.add(chartDataBean.getDates());
			values.add(chartDataBean.getData());
		}
		if (chartDataBeanList.size() != titles.length) {
			return;
		}
		double[] maxmin = StringUtil.getMaxAndMin(chartDataBeanList.get(0)
				.getData());
		if (chartDataBeanList.size() == 2) {
			double[] tmpMaxMin = StringUtil.getMaxAndMin(chartDataBeanList.get(
					1).getData());
			if (tmpMaxMin[0] < maxmin[0]) {
				tmpMaxMin[0] = maxmin[0];
			}
			if (tmpMaxMin[1] > maxmin[1]) {
				maxmin[1] = tmpMaxMin[1];
			}
		}
		if (isShowTable) {
			LinearLayout myLinerlayout = new LinearLayout(context);
			GraphicalView mChartView = BitmapManager
					.getTimeChartView(context, daytype / xnumbers, maxmin[0],
							maxmin[1], titles, dates, values,
							AppApplication.width, AppApplication.height, format);
			if (listener != null) {
				mChartView.setOnClickListener(listener);
			}
			myLinerlayout.addView(mChartView, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			if (chartliLayout.getChildCount() == 0) {
				chartliLayout.addView(myLinerlayout, new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			} else {
				chartliLayout.removeViewAt(0);
				chartliLayout.addView(myLinerlayout, new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			}
		} else {
			GraphicalView mChartView = null;
			mChartView = BitmapManager.getTimeChartView_noaxis(context, daytype
					/ xnumbers, maxmin[0], maxmin[1], new String[] { "" },
					dates, values, AppApplication.width, AppApplication.height,
					format);
			chartliLayout.addView(mChartView, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}
	}

	@SuppressWarnings("deprecation")
	public static void setStackedBarChart(LinearLayout linearLayout,
			DurationTimeBean durationTimeBean, int[] colors, String[] titles) {
		if (durationTimeBean == null) {
			return;
		}
		// construct a new render
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize((float) (AppApplication.width / 42.667));
		renderer.setChartTitleTextSize((float) (AppApplication.width / 42.667));
		renderer.setLabelsTextSize((float) (AppApplication.width / 42.667));
		renderer.setLegendTextSize((float) (AppApplication.width / 42.667));
		renderer.setInScroll(true);
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(colors[i]);
			renderer.addSeriesRenderer(r);
		}
		renderer.setXLabelsColor(Color.TRANSPARENT);
		renderer.setYLabelsColor(0, Color.TRANSPARENT);
		renderer.setAxesColor(Color.TRANSPARENT);
		renderer.setBackgroundColor(Color.argb(0, 0xff, 0, 0));
		renderer.setMarginsColor(Color.argb(0, 0xff, 0, 0));
		renderer.setXLabels(3);
		renderer.setYLabels(10);
		renderer.setXLabelsAlign(Align.CENTER);
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.setPanEnabled(false, false);
		renderer.setClickEnabled(true);
		renderer.setBarSpacing(1.618f);
		renderer.setShowLabels(false);
		renderer.setShowLegend(false);
		GraphicalView mChartView = ChartFactory.getBarChartView(
				linearLayout.getContext(),
				buildStackedDataset(titles, durationTimeBean), renderer,
				BarChart.Type.DEFAULT);
		linearLayout.addView(mChartView, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	/**
	 * @param linearLayout
	 * @param durationTimeBean
	 * @param colors
	 * @param titles
	 */
	@SuppressWarnings("deprecation")
	public static void setBarChart(LinearLayout linearLayout,
			DurationTimeBean durationTimeBean, int[] colors, String[] titles) {
		if (durationTimeBean == null) {
			return;
		}
		// construct a new render
		// durationTimeBean.getDatas();
		double[] maxmin = StringUtil.getMaxAndMin(durationTimeBean
				.convertPercent());
		double ymin = maxmin[0];
		double ymax = maxmin[1];
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize((float) (AppApplication.width / 42.667));
		renderer.setChartTitleTextSize((float) (AppApplication.width / 42.667));
		renderer.setLabelsTextSize((float) (AppApplication.width / 42.667));
		renderer.setLegendTextSize((float) (AppApplication.width / 42.667));
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(colors[i]);
			renderer.addSeriesRenderer(r);
		}
		renderer.setXLabels(0);
		renderer.setXAxisMin(0.45);
		renderer.setXAxisMax(8.5);
		renderer.setYLabels(10);
		renderer.setXLabelsAlign(Align.CENTER);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setXLabelsColor(AppApplication.getInstance().getResources()
				.getColor(R.color.textColor));
		renderer.setYLabelsColor(0, AppApplication.getInstance().getResources()
				.getColor(R.color.textColor));
		renderer.setPanEnabled(false, false);
		renderer.setBackgroundColor(Color.argb(0, 0xff, 0, 0));
		renderer.setMarginsColor(Color.argb(0, 0xff, 0, 0));
		renderer.setBarSpacing(2f);
		renderer.setMargins(new int[] { AppApplication.width / 50,
				AppApplication.width * 3 / 75, 2,
				AppApplication.width / 50 });
		renderer.setShowLabels(true);
		renderer.setClickEnabled(true);
		renderer.setShowLegend(false);
		for (int i = 1; i <= Constants.BARCHARTLEBLES.length; i++) {
			renderer.addXTextLabel(i, Constants.BARCHARTLEBLES[i - 1]);
		}
		renderer.setYAxisMax(110);
		renderer.setYAxisMin(ymin);
		if (ymax < 10) {
			renderer.setYLabels((int) ymax + 1);
			renderer.setYAxisMax((int) ymax + 1);
			renderer.setYAxisMin(0);
		} else {
			renderer.setYAxisMax(ymax + (ymax - ymin) / 10);
			renderer.setYAxisMin(0);
		}
		GraphicalView mChartView = ChartFactory.getBarChartView(
				linearLayout.getContext(),
				buildDataset(titles, durationTimeBean), renderer,
				BarChart.Type.DEFAULT);
		LinearLayout myLinerlayout = new LinearLayout(linearLayout.getContext());
		myLinerlayout.addView(mChartView, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		if (linearLayout.getChildCount() == 0) {
			linearLayout.addView(myLinerlayout, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		} else {
			linearLayout.removeViewAt(0);
			linearLayout.addView(myLinerlayout, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}
	}

	/**
	 * @param titles
	 * @param durationTimeBean
	 * @return
	 */
	private static XYMultipleSeriesDataset buildDataset(String[] titles,
			DurationTimeBean durationTimeBean) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int len = titles.length;
		for (int i = 0; i < len; i++) {
			CategorySeries series = new CategorySeries(titles[i]);
			double[] v = durationTimeBean.convertPercent();
			String[] keys = durationTimeBean.convertKey();
			int seriesLength = v.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(keys[k], v[k]);
			}
			dataset.addSeries(series.toXYSeries());
		}
		return dataset;
	}

	/**
	 * @param titles
	 * @param durationTimeBean
	 * @return
	 */
	private static XYMultipleSeriesDataset buildStackedDataset(String[] titles,
			DurationTimeBean durationTimeBean) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int len = titles.length;
		for (int i = 0; i < len; i++) {
			CategorySeries series = new CategorySeries(titles[i]);
			double[] v = durationTimeBean.convertValues();
			String[] keys = durationTimeBean.convertKey();
			int seriesLength = v.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(keys[k], v[k]);
			}
			dataset.addSeries(series.toXYSeries());
		}
		return dataset;
	}
}