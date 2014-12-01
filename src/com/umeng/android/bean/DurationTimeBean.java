package com.umeng.android.bean;

import java.io.Serializable;

public class DurationTimeBean implements Serializable {

	private static final long serialVersionUID = 2L;
	private Data[] datas;
	private String average;
	
	
	/**
	 * @return the datas
	 */
	public Data[] getDatas() {
		return datas;
	}


	/**
	 * @param datas the datas to set
	 */
	public void setDatas(Data[] datas) {
		this.datas = datas;
	}


	/**
	 * @return the average
	 */
	public String getAverage() {
		return average;
	}


	/**
	 * @param average the average to set
	 */
	public void setAverage(String average) {
		this.average = average;
	}


	public class Data implements Serializable{
		
		private static final long serialVersionUID = 2L;
		public String key;
		public int num;
		public double percent;
		
		/**
		 * @param key
		 * @param num
		 * @param percent
		 */
		public Data() {}
		public Data(String key, int num, double percent) {
			super();
			this.key = key;
			this.num = num;
			this.percent = percent;
		}
		
	}
	/**
	 * @return
	 */
	public double[] convertValues(){
		double[] values = new double[this.datas.length];
		for(int i=0;i<values.length;i++){
			values[i] = this.datas[i].num;
		}
		return values;
	}
	/**
	 * @return
	 */
	public String[] convertKey(){
		String[] keys = new String[this.datas.length];
		for(int i=0;i<keys.length;i++){
			keys[i] = this.datas[i].key;
		}
		return keys;
	}
	
	public double[] convertPercent(){
		double[] values = new double[this.datas.length];
		for(int i=0;i<values.length;i++){
			values[i] = this.datas[i].percent;
		}
		return values;
	}
	
	/**
	 * @param len
	 * @return
	 */
	public Data[] constructArrays(int len){
		if(len<=0){
			return null;
		}
		Data[] datas = new Data[len];
		return datas;
	}
}
