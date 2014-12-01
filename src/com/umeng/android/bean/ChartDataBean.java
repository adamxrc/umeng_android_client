package com.umeng.android.bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
/**
 * Provide data for trend chart
 */
public class ChartDataBean implements Serializable{
	private static final long serialVersionUID = 7753605229333843344L;
	private double [] data;
	private Date [] dates;
	
	public ChartDataBean(double[] data, Date[] dates) {
		super();
		this.data = data;
		this.dates = dates;
	}
	public double[] getData() {
		return data;
	}
	public void setData(double[] data) {
		this.data = data;
	}
	public Date[] getDates() {
		return dates;
	}
	public void setDates(Date[] dates) {
		this.dates = dates;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null ||!(o instanceof ChartDataBean)){
			return false;
		}
		ChartDataBean chartDataBean = (ChartDataBean) o;
		if(Arrays.equals(this.data, chartDataBean.data)){
			if(Arrays.equals(dates, chartDataBean.dates)){
				return true;
			}
		}
		return false;
	}
	/**
	 * reverse this object
	 */
	public void reverse(){
		double[] times = new double[data.length];
		Date[] das = new Date[dates.length];
		int length = times.length;
		for(int i=0;i<length;i++){
			times[i] = data[length-i-1];
			das[i] = dates[length-i-1];
		}
		this.data = times;
		this.dates = das;
	}
}
