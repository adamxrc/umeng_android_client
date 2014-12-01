package com.umeng.android.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.util.Log;

import com.umeng.android.bean.AppInformation;
import com.umeng.android.common.Constants;
import com.umeng.android.exception.AppException;

public class NetManager {

	private static final String TAG = NetManager.class.getName();

	private static final int TRY_TIME = 3;

	/**
	 * get String from http through get method
	 * 
	 * @param urlpath
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public static String getString(String urlpath) throws Exception {
		InputStream inputStream = getInputStreamStreamThroughGet(urlpath);
		return readFromStream(inputStream);
	}

	public static String getString(InputStream inputStream) throws Exception {
		return readFromStream(inputStream);
	}

	/**
	 * transform from byte array to stream
	 * 
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static String readFromStream(InputStream inputStream)
			throws Exception {
		if (inputStream == null) {
			throw new Exception("inputstream is null");
		}
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while (((len = inputStream.read(buffer)) != -1)) {
			arrayOutputStream.write(buffer, 0, len);
		}
			
		arrayOutputStream.close();
		inputStream.close();
		String json = new String(arrayOutputStream.toByteArray());
		
		return json;
	}

	/**
	 * get String from http through get method with header parameters
	 * 
	 * @param urlpath
	 * @param headerKey
	 * @param headerValue
	 * @return
	 * @throws Exception
	 */
	public static String getStingWithGet(String urlpath,
			Map<String, String> maps) throws Exception {

		if (maps != null && maps.size() > 0) {
			StringBuilder builder = new StringBuilder();
			builder.append(urlpath).append("?");

			try {
				String tag = maps.get("tag");
				if(!StringUtil.isEmpty(tag))
				{
					maps.remove("tag");
					Log.d(TAG, "tag     ----    " + tag);
					String encoded_url = null;
					if ("getFeedbackList".equals(tag)) {//获取反馈列表
						encoded_url = URLEncoder.encode(Constants.FEEDBACK_SHOW);
					} else if ("getFeedbackDetail".equals(tag)) {//获取回复列表
						encoded_url = URLEncoder.encode(Constants.FEEDBACK_REPLY_SHOW);				
					}else if("getUid".equals(tag)){//获取UID
						encoded_url = URLEncoder.encode(Constants.UID);
					}else if("sendReply".equals(tag)){
						encoded_url = URLEncoder.encode(Constants.FEEDBACK_NEW);
					}else if("getCommentList".equals(tag)){
						encoded_url = URLEncoder.encode(Constants.COOLCHUAN_COMMENTS);
					}
					
					builder.append("path=").append(encoded_url).append("&");	
				}
					
			} catch (Exception e) {
                 e.printStackTrace();
			}

			for (String key : maps.keySet()) {
				builder.append(key).append("=").append(maps.get(key))
						.append("&");
			}
			builder.deleteCharAt(builder.length() - 1);
			urlpath = builder.toString();
			
			Log.d(TAG, "url     ----    " + urlpath);
		}
		return getString(urlpath);
	}

	/**
	 * get stream from http through get method
	 * 
	 * @param urlpath
	 * @param encoding
	 *            ll
	 * @return
	 * @throws Exception
	 */
	public static InputStream getInputStreamStreamThroughGet(String urlpath)
			throws Exception {
		int times = 0;
		InputStream inputStream = null;
		HttpURLConnection connection = null;
		do {
			try {
				URL url = new URL(urlpath);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(Constants.TIME_OUT);
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					inputStream = connection.getInputStream();
					break;
				} else if (connection.getResponseCode() == Constants.APP_NO_EXIST) {
					throw new AppException(
							AppException.TYPE_NO_APP,
							new Exception(
									"This app is not allowed to access or exist!"));
				} else {
					times++;
				}
			} catch (Exception e) {
				if (e instanceof AppException) {
					e.printStackTrace();
					throw e;
				} else if (times < TRY_TIME) {
					times++;
					Thread.sleep(1000);
					continue;
				}
			}
		} while (times < TRY_TIME);
		return inputStream;
	}

	/**
	 * use HttpClient to get data(HttpUrlConnect will throws exeption before get
	 * inputstream or responsecode)
	 * 
	 * @param url
	 * @param maps
	 * @return
	 * @throws Exception
	 */
	public static InputStream getHttpClientInputStream(String url,
			Map<String, String> maps) throws Exception {
        /************************ zhaoyang **************************/
		try {		
			String tag = maps.get("tag");
			maps.remove("tag");
			String encoded_url = null;
		    if("sendReply".equals(tag)) {
				encoded_url = URLEncoder.encode(Constants.FEEDBACK_NEW);
				url = url + "?path=" + encoded_url;	
             }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Log.d(TAG, "url----   " + url);
		/***********************************************************/
		// Post包装 (包装发送的东西)
		HttpPost post = new HttpPost(url);
		// 请求参数
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> map : maps.entrySet()) {
			nameValuePairs.add(new BasicNameValuePair(map.getKey(), map
					.getValue()));
			
			Log.d(TAG, map.getKey() +"  ----   " + map
					.getValue());
		}
		// post参数绑定
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		// 用httpclient发送数据
		DefaultHttpClient client = new DefaultHttpClient();
		// 得到response应答
		HttpResponse response = client.execute(post);
		
		Log.d(TAG, "status code---" + response.getStatusLine().getStatusCode());
		// 通过应答得到流
		InputStream inputStream = response.getEntity().getContent();
		
		return inputStream;
	}

	/**
	 * user is online
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isOnline(Context context) {
		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if (ni != null)
				return ni.isConnectedOrConnecting();
		} catch (Exception e) {
			return false;
		}
		return false;
	}
}