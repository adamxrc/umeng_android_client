package com.umeng.android.common;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.umeng.android.activity.FeedbackActivity;
import com.umeng.android.activity.FeedbackDetailActivity;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.User;
import com.umeng.android.util.DataStorageManager;
import com.umeng.android.util.StringUtil;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

public class AppPush {

	private static AppPush mAppPush;

	public static AppPush getInstance(Context context) {
		if (mAppPush == null)
			mAppPush = new AppPush(context);
		return mAppPush;
	}

	public static final String SHARED_NAME = "AppPush";
	public static final String KEY_APP_PREFIX = "KEY_APP_PREFIX_";
	public static final String KEY_NUM_SUFFIX = "NUM";// 当前的消息数目
	public static final String KEY_LAST_FEEDBACK_SUFFIX = "LAST_FEEDBACK";// 最后的feedbackId
	public static final String KEY_SAME_FEEDBACK_SUFFIX = "SAME_FEEDBACK";// 目前的消息是否来自同一个feedback
	public static final String KEY_APP_PUSH_SWITCH_PREFIX = "SWITCH";// App
																		// Push推送开关

	private Context context;
	private SharedPreferences pref;
	public UmengMessageHandler mMessageHandler;
	public UmengNotificationClickHandler mNotificationClickHandler;

	public AppPush(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.pref = context.getSharedPreferences(SHARED_NAME,
				Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
	}

	public void init() {
		Log.d("AppPush", "Push token: "
				+ PushAgent.getInstance(context).getRegistrationId());

		mMessageHandler = new UmengMessageHandler() {
			@Override
			public void dealWithNotificationMessage(Context paramContext,
					UMessage paramUMessage) {
				if (isMsgPushOpen())
					super.dealWithNotificationMessage(paramContext,
							paramUMessage);
			}
			
			@Override
			public void dealWithCustomMessage(Context context, UMessage msg) {
				// TODO Auto-generated method stub
				super.dealWithCustomMessage(context, msg);
				String appkey = null;
				String feedbackId = null;
				String content = null;
                Log.d("AppPush", "msg.alias - " + msg.alias);
				// discard the message with different alias from the current
				// login user.
				User user = AppApplication.getInstance().getUser();
				if (user == null) {
					// there is no login user or the app was shut down.
					List<User> localUsers = AppApplication.getInstance().getUsers();
					if (localUsers != null && localUsers.size() > 0) {
						user = localUsers.get(0);
					}
				}
				
				if (user == null || msg == null) {
					// no local user record.
					return;
				}
				//the user of mesage isn't the current user
				if (!user.getUsername().equals(msg.alias)){
					return;
				}
				
				
				try {
					JSONObject json = new JSONObject(msg.custom);
					appkey = json.optString("appkey");
					feedbackId = json.optString("feedback_id");
					content = json.optString("content");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				AppInformation app = getAppInfoByAppkey(appkey);
				if (app == null || !isAppPushOpen(appkey)) {
					return;
				}
				
				int smallIcon = getSmallIconId(context, msg);
				if (smallIcon < 0) {
					return;
				}
				
				addMsgToSharedPrefs(appkey, feedbackId);

				String title = String.format(NOTIFICATION_TITLE, app.getName(),
						getMsgNumOfApp(appkey));
				String text = content;
				String ticker = content;

				msg.play_lights = msg.play_sound = msg.play_vibrate = true;

				PendingIntent clickPendingIntent = getClickPendingIntent(
						context, msg);
				PendingIntent dismissPendingIntent = getDismissPendingIntent(
						context, msg);

				NotificationCompat.Builder builder = new NotificationCompat.Builder(
						context);
				builder.setDefaults(getNotificationDefaults(context, msg));
				builder.setSmallIcon(smallIcon);

				builder.setContentTitle(title).setContentText(text)
						.setTicker(ticker).setAutoCancel(true)
						.setContentIntent(clickPendingIntent)
						.setDeleteIntent(dismissPendingIntent);

				int id = app.getAppkey().hashCode();
				NotificationManager manager = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);
				Notification notification = builder.build();
				manager.notify(id, notification);
			}
		};

		mNotificationClickHandler = new UmengNotificationClickHandler() {

			@Override
			public void handleMessage(Context context, UMessage msg) {
				Log.d("AppPush", "msg.display_type = " + msg.display_type);
				// TODO Auto-generated method stub
				if (TextUtils.equals(msg.display_type,
						UMessage.DISPLAY_TYPE_NOTIFICATION)) {
					super.handleMessage(context, msg);
				} else if (TextUtils.equals(msg.display_type,
						UMessage.DISPLAY_TYPE_CUSTOM)) {

					String appkey = null;
					String feedbackId = null;

					try {

						JSONObject json = new JSONObject(msg.custom);
						appkey = json.optString("appkey");
						feedbackId = json.optString("feedback_id");
						Log.d("AppPush", "json == " + json);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					AppInformation app = getAppInfoByAppkey(appkey);
										
					if (app == null) {
						return;
					}

					if (isAllMsgFromSameFeedback(appkey)) {
						gotoFeedDetailActivity(context, app, feedbackId);
					} else {
						gotoFeedActivity(context, app);
					}

					resetMsgOfApp(appkey);

				}
			}
		};
		PushAgent.getInstance(context).setDebugMode(true);
		PushAgent.getInstance(context).setMessageHandler(mMessageHandler);
		PushAgent.getInstance(context).setNotificationClickHandler(
				mNotificationClickHandler);
	}

	public void addMsgToSharedPrefs(String appkey, String feedbackId) {
		String keyOfNum = KEY_APP_PREFIX + appkey + KEY_NUM_SUFFIX;
		int num = pref.getInt(keyOfNum, 0) + 1;

		String keyOfLastFeedbackId = KEY_APP_PREFIX + appkey
				+ KEY_LAST_FEEDBACK_SUFFIX;
		String lastFeedbackId = pref.getString(keyOfLastFeedbackId, feedbackId);

		String keyOfSameFeedback = KEY_APP_PREFIX + appkey
				+ KEY_SAME_FEEDBACK_SUFFIX;
		boolean sameFeedback = pref.getBoolean(keyOfSameFeedback, true)
				&& TextUtils.equals(feedbackId, lastFeedbackId);

		pref.edit().putInt(keyOfNum, num)
				.putString(keyOfLastFeedbackId, feedbackId)
				.putBoolean(keyOfSameFeedback, sameFeedback).commit();
	}

	public int getMsgNumOfApp(String appkey) {
		String key = KEY_APP_PREFIX + appkey + KEY_NUM_SUFFIX;
		int num = pref.getInt(key, 0);
		return num;
	}

	public boolean isAllMsgFromSameFeedback(String appkey) {
		String keyOfSameFeedback = KEY_APP_PREFIX + appkey
				+ KEY_SAME_FEEDBACK_SUFFIX;
		return pref.getBoolean(keyOfSameFeedback, true);
	}

	public void resetMsgOfApp(String appkey) {
		String keyPrefix = KEY_APP_PREFIX + appkey;

		Editor editor = pref.edit();
		Map<String, ?> kvs = pref.getAll();
		if (kvs != null && kvs.size() > 0) {
			for (Map.Entry<String, ?> entry : kvs.entrySet()) {
				String key = entry.getKey();
				if (key.startsWith(keyPrefix)) {
					editor.remove(key);
				}
			}
		}
		editor.commit();
	}

	private void gotoFeedActivity(Context context, AppInformation app) {
		Intent feedBackIntent = new Intent(context, FeedbackActivity.class);
		feedBackIntent.putExtra(Constants.APP, app);
		feedBackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(feedBackIntent);
		Log.d("AppPush", "gotoFeedActivity");
	}

	private void gotoFeedDetailActivity(Context context, AppInformation app,
			String feedbackId) {
		Intent intent = new Intent(context, FeedbackDetailActivity.class);
		intent.putExtra("feedbackId", feedbackId);
		intent.putExtra(Constants.APP, app);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public AppInformation getAppInfoByAppkey(String appkey) {
		List<AppInformation> apps = getAppsFromCache();
		AppInformation app = null;

		for (AppInformation appInfo : apps) {
			if (TextUtils.equals(appInfo.getAppkey(), appkey)) {
				app = appInfo;
				break;
			}
		}
		return app;
	}

	public List<AppInformation> getAppsFromCache() {

		if (AppApplication.getInstance().getUser() == null) {
			List<User> users = AppApplication.getInstance().getUsers();
			if (users != null && users.size() > 0) {
				AppApplication.getInstance().setUser(users.get(0));
			}
		}

		List<AppInformation> apps = null;

		Object object = DataStorageManager.readDataFromPhone(
				Constants.PRODUCT_INFO_DIRECTOR,
				Constants.PRODUCT
						+ StringUtil.getMD5(AppApplication.getInstance()
								.getUser().getUsername()));
		if (object != null && object instanceof List
				&& ((List<AppInformation>) object).size() > 0) {
			apps = (List<AppInformation>) object;
		}

		return apps;
	}

	public boolean isAppPushOpen(String appkey) {
		String keyOfPushSwitch = KEY_APP_PUSH_SWITCH_PREFIX + appkey;
		return pref.getBoolean(keyOfPushSwitch, true);
	}

	public void closeAppPush(String appkey) {
		String keyOfPushSwitch = KEY_APP_PUSH_SWITCH_PREFIX + appkey;
		pref.edit().putBoolean(keyOfPushSwitch, false).commit();
	}

	public void openAppPush(String appkey) {
		String keyOfPushSwitch = KEY_APP_PUSH_SWITCH_PREFIX + appkey;
		pref.edit().putBoolean(keyOfPushSwitch, true).commit();
	}

	public boolean isMsgPushOpen() {
		String keyOfPushSwitch = KEY_APP_PUSH_SWITCH_PREFIX + "Message";
		return pref.getBoolean(keyOfPushSwitch, true);
	}

	public void closeMsgPush() {
		String keyOfPushSwitch = KEY_APP_PUSH_SWITCH_PREFIX + "Message";
		pref.edit().putBoolean(keyOfPushSwitch, false).commit();
	}

	public void openMsgPush() {
		String keyOfPushSwitch = KEY_APP_PUSH_SWITCH_PREFIX + "Message";
		pref.edit().putBoolean(keyOfPushSwitch, true).commit();
	}

	public String NOTIFICATION_TITLE = "%s有%s个反馈";
}
