package com.umeng.android.bean;

import java.io.Serializable;
import java.util.List;

public class FeedbackBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private String sdk_type;
	private long created_at;
	private String thread;
	private String resolution;
	private String sdk_version;
	private String timestamp;
	private long updated_at;
	private String device_model;
	private String lastreplydate;
	private String app_version;
	private String user_id;
	private String type;
	private boolean isviewed;
	private String os_version;
	private String datetime;
	private boolean isreplied;
	private String feedback_id;
	private String appkey;
	private String os;
	private List<ReplyItem> lists ;
	public class ReplyItem implements Serializable{
		private static final long serialVersionUID = 2L;
		private String type;
		private String user_id;
		private String reply_id;
		private String user_name;
		private String content;
		private String datetime;
		private String feedback_id;
		private String appkey;
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getUser_id() {
			return user_id;
		}
		public void setUser_id(String user_id) {
			this.user_id = user_id;
		}

		public String getReply_id() {
			return reply_id;
		}

		public void setReply_id(String reply_id) {
			this.reply_id = reply_id;
		}

		public String getUser_name() {
			return user_name;
		}

		public void setUser_name(String user_name) {
			this.user_name = user_name;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getDatetime() {
			return datetime;
		}

		public void setDatetime(String datetime) {
			this.datetime = datetime;
		}

		public String getFeedback_id() {
			return feedback_id;
		}

		public void setFeedback_id(String feedback_id) {
			this.feedback_id = feedback_id;
		}

		public String getAppkey() {
			return appkey;
		}

		public void setAppkey(String appkey) {
			this.appkey = appkey;
		}
		
	}

	public String getSdk_type() {
		return sdk_type;
	}

	public void setSdk_type(String sdk_type) {
		this.sdk_type = sdk_type;
	}

	public long getCreated_at() {
		return created_at;
	}

	public void setCreated_at(long created_at) {
		this.created_at = created_at;
	}

	public String getThread() {
		return thread;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getSdk_version() {
		return sdk_version;
	}

	public void setSdk_version(String sdk_version) {
		this.sdk_version = sdk_version;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public long getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(long updated_at) {
		this.updated_at = updated_at;
	}

	public String getDevice_model() {
		return device_model;
	}

	public void setDevice_model(String device_model) {
		this.device_model = device_model;
	}

	public String getLastreplydate() {
		return lastreplydate;
	}

	public void setLastreplydate(String lastreplydate) {
		this.lastreplydate = lastreplydate;
	}

	public String getApp_version() {
		return app_version;
	}

	public void setApp_version(String app_version) {
		this.app_version = app_version;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean getIsviewed() {
		return isviewed;
	}

	public void setIsviewed(boolean isviewed) {
		this.isviewed = isviewed;
	}
	public String getOs_version() {
		return os_version;
	}
	public void setOs_version(String os_version) {
		this.os_version = os_version;
	}
	public String getDatetime() {
		return datetime;
	}
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
	public boolean isIsreplied() {
		return isreplied;
	}
	public void setIsreplied(boolean isreplied) {
		this.isreplied = isreplied;
	}
	public String getFeedback_id() {
		return feedback_id;
	}
	public void setFeedback_id(String feedback_id) {
		this.feedback_id = feedback_id;
	}
	public String getAppkey() {
		return appkey;
	}
	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}
	public List<ReplyItem> getLists() {
		return lists;
	}
	public void setLists(List<ReplyItem> lists) {
		this.lists = lists;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	
}
