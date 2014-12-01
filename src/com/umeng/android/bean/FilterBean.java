package com.umeng.android.bean;

/**
 * Filter condition
 */
public class FilterBean {
	private String name;
	private String value;
	public FilterBean(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof FilterBean)){
			return false;
		}
		FilterBean filterBean = (FilterBean) o;
		if(this.name.equals(filterBean.name)&&this.value.equals(filterBean.value)){
			return true;
		}
		return false;
	}
}
