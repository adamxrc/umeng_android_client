package com.umeng.android.bean;

import java.io.Serializable;

public class LabelEventBean implements Serializable{

	private static final long serialVersionUID = 1L;
	private String num;
	private String percent;
	private String label;
	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
	public String getPercent() {
		return percent;
	}
	public void setPercent(String percent) {
		this.percent = percent;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
}
