package com.umeng.android.logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.gson.Gson;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.AppVersion;
import com.umeng.android.bean.BasicDayData;
import com.umeng.android.bean.ChannelBean;
import com.umeng.android.bean.ChartDataBean;
import com.umeng.android.bean.DurationTimeBean;
import com.umeng.android.bean.EventBean;
import com.umeng.android.bean.FeedbackBean;
import com.umeng.android.bean.GroupBean;
import com.umeng.android.bean.LabelEventBean;
import com.umeng.android.bean.MarketCommentInfo;
import com.umeng.android.bean.RetentionBean;
import com.umeng.android.bean.TotalDayData;
import com.umeng.android.util.JsonKey;
import com.umeng.android.util.StringUtil;

public class DataParseManager {
	
	private final static String TAG = DataParseManager.class.getName();
	private static boolean finish = false;


	@SuppressLint("SimpleDateFormat")
	public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * transform from json to app number
	 * 
	 * @param json
	 * @return app number or -1 for exception
	 */
	public static int getAppNum(String json) {
		int num = -1;
		try {
			JSONObject jsonObject = new JSONObject(json);
			num = jsonObject.getInt("count");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return num;
	}

	/**
	 * transform from json to app datas
	 * 
	 * @param json
	 * @return
	 */
	public static List<AppInformation> getApps(String json) {
		List<AppInformation> list = new LinkedList<AppInformation>();
		Gson gson = new Gson();
		AppInformation[] arrays = gson.fromJson(json, AppInformation[].class);
		list = Arrays.asList(arrays);
		return list;
	}

	/**
	 * transform from json to basic day data(we get tody data and yesterday data
	 * from it)
	 * 
	 * @param json
	 * @return
	 */
	public static BasicDayData getTodayData(String json) {
		BasicDayData daydata = null;
		try {
			JSONObject jsonObject = new JSONObject(json);
			daydata = new BasicDayData(jsonObject.getString("launches"),
					jsonObject.getString("active_users"),
					jsonObject.getString("new_users"),
					jsonObject.getString("installations"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return daydata;
	}

	/**
	 * get all apps total data from json
	 * 
	 * @param json
	 * @return
	 */
	public static TotalDayData getTotalData(String json) {
		Gson gson = new Gson();
		TotalDayData daydata = gson.fromJson(json, TotalDayData.class);
		return daydata;
	}

	/**
	 * get top 10 channels from json
	 * 
	 * @param json
	 * @return
	 */
	public static List<ChannelBean> getChannelBeans(String json) {
		List<ChannelBean> list = new LinkedList<ChannelBean>();
		try {
			JSONArray jsonArray = new JSONArray(json);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
				ChannelBean channel = new ChannelBean(
						jsonObject.getString("total_install"),
						jsonObject.getString("channel"),
						jsonObject.getString("active_user"),
						jsonObject.getString("install"),
						jsonObject.getString("total_install_rate"),
						jsonObject.getString("id"));
				list.add(channel);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * get top 10 versions from json
	 * 
	 * @param json
	 * @return
	 */
	public static List<AppVersion> getVersionBeans(String json) {
		List<AppVersion> list = new LinkedList<AppVersion>();
		try {
			JSONArray jsonArray = new JSONArray(json);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
				AppVersion version = new AppVersion(
						jsonObject.getString("total_install"),
						jsonObject.getString("version"),
						jsonObject.getString("active_user"),
						jsonObject.getString("install"),
						jsonObject.getString("total_install_rate"));
				list.add(version);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * get chart datas from json
	 * 
	 * @param json
	 * @param discription
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static ChartDataBean getChartDataBeans(String json,
			String discription) {
		ChartDataBean chartDataBean = null;
		try {
			JSONObject jsonObject = new JSONObject(json);
			JSONArray jsonArray = jsonObject.getJSONArray("dates");
			Date[] dates = new Date[jsonArray.length()];
			Date date = null;
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					date = format.parse(jsonArray.optString(i));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				dates[i] = date;
			}

			JSONArray jsonArray2 = jsonObject.getJSONObject("data")
					.getJSONArray(discription);
			double[] datas = new double[jsonArray2.length()];
			if (json.indexOf("unknown") != -1) {
				for (int i = 0; i < jsonArray2.length(); i++) {
					try {
						datas[i] = jsonArray2.getJSONObject(i).getDouble(
								"unknown");
					} catch (Exception e) {
						datas[i] = 0;
					}
				}

			} else {
				for (int i = 0; i < jsonArray2.length(); i++) {
					datas[i] = jsonArray2.optDouble(i);
				}
			}
			chartDataBean = new ChartDataBean(datas, dates);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return chartDataBean;
	}

	/**
	 * get chart datas from json
	 * 
	 * @param json
	 * @param discription
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("SimpleDateFormat")
	public static ChartDataBean getChartDataBeansToday(String json,
			String discription, boolean current) {
		ChartDataBean chartDataBean = null;
		try {
			JSONArray jsonArray = (new JSONObject(json)).getJSONArray("dates");
			Date[] dates = new Date[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				if (current) {
					Date date = new Date();
					date.setHours(Integer.parseInt(jsonArray.optString(i)));
					date.setMinutes(0);
					date.setSeconds(0);
					dates[i] = date;
				} else {
					Date date = new Date();
					date.setDate(date.getDate());
					date.setHours(Integer.parseInt(jsonArray.optString(i)));
					date.setMinutes(0);
					date.setSeconds(0);
					dates[i] = date;
				}
			}

			JSONArray jsonArray2 = (new JSONObject(json)).getJSONObject("data")
					.getJSONArray(discription);
			double[] datas = new double[jsonArray2.length()];
			for (int i = 0; i < jsonArray2.length(); i++) {
				datas[i] = jsonArray2.optDouble(i);
			}

			chartDataBean = new ChartDataBean(datas, dates);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return chartDataBean;
	}

	/**
	 * get chart version datas from json
	 * 
	 * @param json
	 * @param version
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static ChartDataBean getChartDataBeansVersion(String json,
			String version) {
		ChartDataBean chartDataBean = null;
		try {
			JSONArray jsonArray = (new JSONObject(json)).getJSONArray("dates");
			Date[] dates = new Date[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {

				Date date = null;
				try {
					date = format.parse(jsonArray.optString(i));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				dates[i] = date;
			}

			JSONArray jsonArray2 = (new JSONObject(json)).getJSONObject("data")
					.getJSONArray(version);
			double[] datas = new double[jsonArray2.length()];
			for (int i = 0; i < jsonArray2.length(); i++) {
				datas[i] = jsonArray2.optDouble(i);
			}

			chartDataBean = new ChartDataBean(datas, dates);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return chartDataBean;
	}

	/**
	 * parse durtion time data
	 * 
	 * @param json
	 * @return
	 */
	public static DurationTimeBean getDurationTimeBean(String json) {
		if (json == null || json.trim().equals("")) {
			return null;
		}
		DurationTimeBean durationTimeBean = null;
		try {
			JSONObject jsonObject = new JSONObject(json);
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			durationTimeBean = new DurationTimeBean();
			DurationTimeBean.Data[] datas = durationTimeBean
					.constructArrays(jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject js = jsonArray.getJSONObject(i);
				datas[i] = durationTimeBean.new Data(js.optString("key"),
						js.optInt("num"), js.optDouble("percent"));
			}
			durationTimeBean.setDatas(datas);
			durationTimeBean.setAverage(jsonObject.optString("average"));
		} catch (Exception e) {
			e.printStackTrace();
			return durationTimeBean;
		}
		return durationTimeBean;
	}

	/**
	 * @param json
	 * @return
	 */
	public static List<RetentionBean> getRetentionBean(String json) {
		if (json == null || json.trim().equals("")) {
			return null;
		}
		List<RetentionBean> lists = new ArrayList<RetentionBean>();
		try {
			JSONArray jArray = new JSONArray(json);
			RetentionBean retentionBean = null;
			for (int j = 0; j < jArray.length(); j++) {
				JSONObject jsonObject = jArray.getJSONObject(j);
				retentionBean = new RetentionBean();
				retentionBean.setInstall_period(jsonObject
						.optString("install_period"));
				retentionBean.setTotal_install(jsonObject
						.optString("total_install"));
				JSONArray jsonArray = jsonObject.getJSONArray("retention_rate");
				double[] retention_rates = new double[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					retention_rates[i] = jsonArray.getDouble(i);
				}
				retentionBean.setRetention_rate(retention_rates);
				lists.add(retentionBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return lists;
	}

	/**
	 * parse groupBean data
	 * 
	 * @param json
	 * @return
	 */
	public static List<GroupBean> getGroupBeans(String json) {
		if (StringUtil.isEmpty(json)) {
			return null;
		}
		List<GroupBean> lists;
		try {
			lists = new ArrayList<GroupBean>();
			JSONArray jsonArray = new JSONArray(json);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				GroupBean groupBean = new GroupBean();
				groupBean.setCount(jsonObject.optInt("count"));
				groupBean.setDisplay_name(jsonObject.getString("display_name"));
				groupBean.setGroup_id(jsonObject.optString("group_id"));
				groupBean.setName(jsonObject.optString("name"));
				lists.add(groupBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return lists;
	}

	/**
	 * get event list
	 * 
	 * @param json
	 * @return
	 */
	public static List<EventBean> getEventBeans(String json) {
		if (StringUtil.isEmpty(json)) {
			return null;
		}
		List<EventBean> lists = null;
		try {
			JSONArray jsonArray = new JSONArray(json);
			lists = new ArrayList<EventBean>();
			int len = jsonArray.length();
			if (len <= 0) {
				return lists;
			}
			EventBean eventBean = null;
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				eventBean = new EventBean();
				eventBean.setDisplay_name(jsonObject.optString("display_name"));
				eventBean.setEvent_id(jsonObject.optString("event_id"));
				eventBean.setName(jsonObject.optString("name"));
				lists.add(eventBean);
			}
		} catch (Exception e) {
			return lists;
		}
		return lists;
	}

	/**
	 * get label event list
	 * 
	 * @param json
	 * @return
	 */
	public static List<LabelEventBean> getLabelEventBeans(String json) {
		if (StringUtil.isEmpty(json)) {
			return null;
		}
		List<LabelEventBean> lists = null;
		try {
			JSONArray jsonArray = new JSONArray(json);
			lists = new ArrayList<LabelEventBean>();
			int len = jsonArray.length();
			if (len <= 0) {
				return lists;
			}
			LabelEventBean labelEventBean = null;
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				labelEventBean = new LabelEventBean();
				labelEventBean.setLabel(jsonObject.optString("label"));
				labelEventBean.setNum(jsonObject.optString("num"));
				labelEventBean.setPercent(jsonObject.optString("percent"));
				lists.add(labelEventBean);
			}
		} catch (Exception e) {
			return lists;
		}
		return lists;
	}

	/**
	 * @param json
	 * @return
	 * @throws Exception
	 */

	public static ArrayList<FeedbackBean> getFeedbackBean(String json) throws Exception{	
		if (StringUtil.isEmpty(json)) {
			return null;
		}
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		
		ArrayList<FeedbackBean> lists = new ArrayList<FeedbackBean>();
		JSONObject jsonObject = new JSONObject(json);
		JSONObject listJson = jsonObject.getJSONObject("data");
		JSONArray jsonArray = listJson.getJSONArray("result");
		finish = listJson.optBoolean("finish");
		FeedbackBean feedbackBean = null;
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsObject = jsonArray.getJSONObject(i);
			feedbackBean = new FeedbackBean();
			feedbackBean.setApp_version(jsObject.optString("app_version"));			
			feedbackBean.setCreated_at(jsObject.optLong("created_at"));
			feedbackBean.setDevice_model(jsObject.optString("device_model"));
			feedbackBean.setFeedback_id(jsObject.optString("feedback_id"));
			feedbackBean.setIsreplied(jsObject.optBoolean("isreplied"));			
			feedbackBean.setOs(jsObject.optString("os"));
			feedbackBean.setOs_version(jsObject.optString("os_version"));
			feedbackBean.setResolution(jsObject.optString("resolution"));
			feedbackBean.setSdk_type(jsObject.optString("sdk_type"));
			feedbackBean.setSdk_version(jsObject.optString("sdk_version"));
			feedbackBean.setThread(jsObject.optString("content"));			
			feedbackBean.setType(jsObject.optString("type"));
			feedbackBean.setUpdated_at(jsObject.optLong("updated_at"));						
			feedbackBean.setDatetime(sdf.format(new Date(feedbackBean.getUpdated_at())));
			//feedbackBean.setTimestamp(jsObject.optString("timestamp"));
			//feedbackBean.setAppkey(jsObject.optString("appkey"));
			//feedbackBean.setIsviewed(jsObject.optBoolean("isviewed"));
			//feedbackBean.setLastreplydate(jsObject.optString("lastreplydate"));
			//feedbackBean.setUser_id(jsObject.optString("user_id"));
				
			lists.add(feedbackBean);
		}
		return lists;
	}
	/*
	 * 返回是否可以翻页
	 */
    public static boolean isFinish(){
	      return finish;
      }
	/**
	 * @param json
	 * @return
	 * @throws Exception
	 */
	public static FeedbackBean getFeedbackBeanDetail(String json) throws Exception {
		if (StringUtil.isEmpty(json)) {
			return null;
		}

		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		Log.d(TAG, "json====" + json);

		JSONObject jsonObject = new JSONObject(json);
		JSONObject listJson = jsonObject.getJSONObject("data");
		JSONArray jsonArray = listJson.getJSONArray("result");

		FeedbackBean feedbackBean = new FeedbackBean();
		List<FeedbackBean.ReplyItem> replyItems = new ArrayList<FeedbackBean.ReplyItem>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsObject = jsonArray.getJSONObject(i);

			FeedbackBean.ReplyItem replyItem = feedbackBean.new ReplyItem();
			
			replyItem.setContent(jsObject.optString("content"));
			replyItem.setDatetime(sdf.format(new Date(jsObject.optLong("created_at"))));
			replyItem.setFeedback_id(jsObject.optString("feedback_id"));
			replyItem.setType(jsObject.optString("type"));
			//replyItem.setAppkey(jsObject.optString("appkey"));
			//replyItem.setUser_id(jsObject.optString("user_id"));
			//replyItem.setUser_name(jsObject.optString("user_name"));
			replyItems.add(replyItem);		
		}
		feedbackBean.setLists(replyItems);
		
		for(FeedbackBean.ReplyItem replyItem : feedbackBean.getLists()){
			Log.d(TAG, "replyItem.getContent() --" + replyItem.getContent());
			Log.d(TAG, "replyItem.getType() --" + replyItem.getType());
			Log.d(TAG, "replyItem.getDatatime() --" + replyItem.getDatetime());			
		}
		return feedbackBean;
	}

	public static boolean getQrcodeRegisterReplay(String json){
		if(StringUtil.isEmpty(json)){

			return false;
		}
		try {
			JSONObject jsonObject = new JSONObject(json);
			return jsonObject.optBoolean("success");
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/*
	 * 解析json数据，获得回复发送状态  "successful" ：发送成功
	 * @param json String
	 * @return status boolean
	 */
    public static boolean getFeedBackReplay(String json){	
		if (StringUtil.isEmpty(json)) {
			return false;
		}
		
		try {
			JSONObject jsonObject = new JSONObject(json);
            String error_msg = jsonObject.optString("error_msg");
			return "successful".equals(error_msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}			
		return false;		
	}
     
    /*
	 * 解析json数据，返回用户评论数据
	 * @param json String
	 * @return list ArrayList<MarketCommentInfo>
	 */
    public static ArrayList<MarketCommentInfo> getMarketCommentInfoList(String json){
    	ArrayList<MarketCommentInfo> tempList = new ArrayList<MarketCommentInfo>();
    	JSONArray array = null;
		try {
			JSONObject jsonObject = new JSONObject(json);
			 array = jsonObject.optJSONArray(JsonKey.DATA);
		} catch (JSONException e) {
			e.printStackTrace();
		}
			
		for (int i = 0; i < array.length(); i++) {
			MarketCommentInfo info = new MarketCommentInfo(array.optJSONObject(i));
			tempList.add(info);			
			Log.d(TAG, "info.getContent() ---" +info.getContent());
		} 	
    	return tempList;  	
    }
}
