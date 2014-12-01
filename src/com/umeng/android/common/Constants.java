package com.umeng.android.common;

import org.achartengine.chart.PointStyle;

import android.graphics.Color;

/**
 * final url
 */
public class Constants {

	// ######################menu id##################################
	public static final int ABOUT = 0x00;
	public static final int CHANGE_USER = 0x01;
	public static final int FEED_BACK = 0x02;
	public static final int SEETING = 0x03;
	public static final int EXIT = 0x04;
	// ######################request path##############################
	public static final String AUTHORIZE = "http://api.umeng.com/authorize";
	public static final String APPS = "http://api.umeng.com/apps";
	public static final String APPS_COUNT = "http://api.umeng.com/apps/count";
	public static final String BASE_DATA = "http://api.umeng.com/apps/base_data";
	public static final String ADD_STAR = "http://api.umeng.com/apps/add_star";
	public static final String NEW_USER = "http://api.umeng.com/new_users";
	public static final String ACTIVE_USER = "http://api.umeng.com/active_users";
	public static final String LAUNCHES = "http://api.umeng.com/launches";
	public static final String TODAY_DATA = "http://api.umeng.com/today_data";
	public static final String YESTERDY_DATA = "http://api.umeng.com/yesterday_data";
	public static final String CHANNELS = "http://api.umeng.com/channels";
	public static final String VERSIONS = "http://api.umeng.com/versions";
	public static final String DURATION_TIME = "http://api.umeng.com/durations";
	public static final String RETENTION = "http://api.umeng.com/retentions";
	public static final String EVENT_GROUP_LIST = "http://api.umeng.com/events/group_list";
	public static final String EVENT_EVENT_LIST = "http://api.umeng.com/events/event_list";
	public static final String EVENT_DAILY = "http://api.umeng.com/events/daily_data";
	public static final String PARAMTER_LIST = "http://api.umeng.com/events/parameter_list";
	//public static final String FEEDBACK_LIST = "http://api.umeng.com/feedbacks";
	public static final String FEEDBACK_PROXY = "http://api.umeng.com/feedbacks/proxy";
	public static final String FEEDBACK_SHOW = "http://fb.umeng.com/api/v2/feedback/show";
	public static final String FEEDBACK_REPLY_SHOW = "http://fb.umeng.com/api/v2/feedback/reply/full_show";
	public static final String FEEDBACK_NEW = "http://fb.umeng.com/api/v2/feedback/reply/new";
	public static final String PARAMTER_DATA = "http://api.umeng.com/events/parameter_data";
	public static final String FEEDBACK_REPLY = "http://api-test.umeng.com/feedbacks/reply";
	public static final String MESSAGE = "http://api.umeng.com/messages";
	public static final String MESSAGE_DETAIL = "http://api.umeng.com/messages/detail";
	public static final String MESSAGE_READ = "http://api.umeng.com/messages/read";
	public static final String MESSAGE_READALL = "http://api.umeng.com/messages/readall";
	public static final String MESSAGE_DELETE = "http://api.umeng.com/messages/delete";
	public static final String COOLCHUAN_COMMENTS = "http://fb.umeng.com/api/v2/coolchuan/comments";
	public static final String UID = "http://fb.umeng.com/api/v2/user/getuid/";
	// ######################bowser parms#################################
	public static final int TIME_OUT = 6 * 1000;
	public static final int APP_NO_EXIST = 403;
	public static final int DATA_TIME_OUT = 7 * 24 * 60 * 60 * 1000;
	// ######################USER_INFO####################################
	public static final String LOGIN_CHANGE = "login_change";
	public static final String USER_NAME = "user_name";
	public static final String USER_PWD = "user_password";
	public static final String USER_INFO = "user_info";
	public static final String USERS_INFO = "users_info";
	public static final String USER_INFO_DIRECTOR = "user";
	public static final String PRODUCT_INFO_DIRECTOR = "product_info";
	public static final String PRODUCT = "product";
	public static final String TODAY_DATA_DIRECTOR = "today_data";
	public static final String TODAY_DATA_INFO = "today";

	// ######################MESSAGE_TYPE####################################
	public static final byte MSG_SUCCESS = 0x01;
	public static final byte MSG_FAIL = 0x02;
	public static final byte MSG_NO_DATA = 0x03;
	// ####################AppInformation###################################
	public static final String APP = "app";
	public static final String TYPE = "type";
	public static final String PAGE = "page";
	public static final String DATA_TYPE = "data_page";
	public static final String TOTAL_INSTALL = "totalInstall";
	// ##################### file name###################################
	public static final String FILE_NEW_USER = "file_new_user";
	public static final String FILE_ACTIVE_USER = "file_active_user";
	public static final String FILE_CHANNEL_USER = "file_channel_user";
	public static final String FILE_TODAY_USER = "file_today_user";
	public static final String FILE_YESTERDAY_USER = "file_yesterday_user";
	public static final String FILE_VERSION = "file_version";
	public static final String FILE_LAUNCHEL = "file_launchs";
	public static final String FILE_TODAY_NEW_ADD = "file_today_new_add";
	public static final String FILE_TODAY_LAUNCH = "file_today_launch";
	public static final String FILE_DURTION_TIME = "file_durtion_time";
	public static final String FILE_DAY_DURTION_TIME = "file_last_durtion_time";
	public static final String FILE_USER_ACCOUNT = "account";
	public static final String FILE_USER_FILENAME = "user";
	public static final String[] FILE_NAMES = new String[] { FILE_TODAY_USER,
			FILE_YESTERDAY_USER, FILE_CHANNEL_USER, FILE_VERSION,
			FILE_NEW_USER, FILE_ACTIVE_USER, FILE_LAUNCHEL, FILE_TODAY_NEW_ADD,
			FILE_TODAY_LAUNCH, FILE_DURTION_TIME, FILE_DAY_DURTION_TIME };
	// #################### clolors #####################################
	public static final int[] COLORS = new int[] { Color.parseColor("#4ec0c3"),
			Color.parseColor("#dee2e5"), Color.parseColor("#ab497f"),
			Color.parseColor("#ef8032"), Color.parseColor("#fed246"),
			Color.parseColor("#393b38"), Color.parseColor("#75d3ff"),
			Color.parseColor("#ffffff"), };
	public static final int[] ORETENTION_COLORS = new int[] {
			Color.parseColor("#0f7aba"), Color.parseColor("#a7e8fb"),
			Color.parseColor("#cff4ff"), Color.parseColor("#f3f6f6"), };
	public static final int[] COMPARE_COLORS = new int[] {
			Color.parseColor("#45b7ff"), Color.parseColor("#51bcbf") };

	public static final int[] SINGLE_COLORS = new int[] { Color
			.parseColor("#45b7ff") };
	public static final PointStyle[] STYLES = new PointStyle[] {
			PointStyle.CIRCLE, PointStyle.DIAMOND };
	public static final String[] BARCHARTLEBLES = new String[] { "1-3s",
			"3-10s", "10-30s", "30-60s", "1-3m", "3-10m", "10-30m", "30m以上" };
	// ##################### type name###################################
	public static final String TYPE_NEW_ADD = "today_new_add";
	public static final String TYPE_LAUNCH = "today_launch";
	public static final String TYPE_DATA = "data_type";
	public static final String TYPE_TODAY = "type_today";
	public static final String TYPE_YESTERDAY = "type_yesterday";
	public static final String TYPE_LAST_WEEK = "type_last_week";
	public static final String TYPE_LAST_MONTH = "type_last_month";
	public static final String TYPE_ANY = "type_any";
	public static final String TYPE_HALF_MONTH = "type_half_month";
	// ##################### format type####################################
	public static final String FORMAT_MM_DD = "MM-dd";
	public static final String FORMAT_HH_MM = "HH";
	public static final String FORMAT_YYYY_MM_DD = "YYYY-MM-DD";
	// ####################### event ##########################################
	public static final String SIMPLE_EVENT = "simple_event";
	public static final String EKV_EVENT = "ekv_event";
	public static final String LABEL_EVENT = "label_event";
	public static final String GROUP_ID = "group_id";
	// ###################### feedback #######################################
	public static final String REPLAY_DEV = "dev_reply";
	public static final String REPLAY_USER = "user_reply";
}
