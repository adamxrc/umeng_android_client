package com.umeng.android.bean;

import org.json.JSONObject;

import com.umeng.android.util.JsonKey;

public class MessageInfo {

	private String title;
	private String created_at;
	private String content;
	private String id;
	private boolean readed;

	public MessageInfo() {

	}

	public MessageInfo(JSONObject data) {

		if (data != null) {

			title = data.optString(JsonKey.TITLE);
			created_at = data.optString(JsonKey.CREATED_AT);
			content = data.optString(JsonKey.CONTENT);
			JSONObject ID = data.optJSONObject(JsonKey.ID);
			if (ID != null) {
				id = ID.optString("$oid");
			}
			readed = data.optBoolean(JsonKey.readed);
		}

	}

	public MessageInfo(String data) {

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isReaded() {
		return readed;
	}

	public void setReaded(boolean readed) {
		this.readed = readed;
	}

}
