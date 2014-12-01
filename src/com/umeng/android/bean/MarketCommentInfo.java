package com.umeng.android.bean;

import org.json.JSONObject;

public class MarketCommentInfo {

	private String author_name;
	private String content;
	private String time;
	private String market_id;
	private String pn;
	private String market_url;
	private String market;

	public MarketCommentInfo() {

	}

	public MarketCommentInfo(JSONObject object) {

		author_name = object.optString("author");
		content = object.optString("content");
		time = object.optString("date");
		market_id = object.optString("market_id");
		market_url = object.optString("market_url");
		pn = object.optString("pn");
		market = object.optString("market");

	}

	public String getAuthor_name() {
		return author_name;
	}

	public void setAuthor_name(String author_name) {
		this.author_name = author_name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getMarket_id() {
		return market_id;
	}

	public void setMarket_id(String market_id) {
		this.market_id = market_id;
	}

	public String getPn() {
		return pn;
	}

	public void setPn(String pn) {
		this.pn = pn;
	}

	public String getMarket_url() {
		return market_url;
	}

	public void setMarket_url(String market_url) {
		this.market_url = market_url;
	}

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

}
