package com.umeng.android.activity;

import java.util.HashMap;
import java.util.Map;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.logic.DataParseManager;
import com.umeng.android.util.NetManager;
import com.umeng.client.R;
import com.umeng.common.message.DeviceConfig;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class QrcodeRegisterActivity extends ActionBarActivity {
	private static final String TAG = QrcodeRegisterActivity.class.getName();

	private final static int SCANNIN_GREQUEST_CODE = 1;
	private String result;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_qrcode_register);

		View qrcodeBtn = (View) this.findViewById(R.id.qrcode_register_btn);
		qrcodeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(QrcodeRegisterActivity.this,
						MipcaCaptureActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
			}

		});

		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.drawable.actionbar_bg));
		getSupportActionBar().setTitle(R.string.add_test_device);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		context = this;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * integrate umeng analytics
	 */
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			this.finish();
			overridePendingTransition(R.anim.translate_activityin_return,
					R.anim.translate_activityout_return);
			break;

		default:
			super.onKeyDown(keyCode, event);
			break;

		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SCANNIN_GREQUEST_CODE:
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				result = bundle.getString("result");
				Log.d(TAG, "qrcode:" + result);
				new Thread(runnable).start();
			}
			break;
		}
	}

	Runnable runnable = new Runnable() {

		@Override
		public void run() {

			// TODO Auto-generated method stub
			if (TextUtils.isEmpty(result)) {
				handler.sendEmptyMessage(2);
				return;
			}
			int pos = result.indexOf("?");
			if (pos < 0 || pos + 1 >= result.length()) {
				handler.sendEmptyMessage(2);
				return;
			}
			String str = result.substring(pos + 1);
			String[] split = str.split(",");
			if (split == null || split.length != 3) {
				handler.sendEmptyMessage(2);
				return;
			}
			String uid = split[0];
			String token = split[1];
			String url = split[2];

			// extract the account from ur

			Map<String, String> maps = new HashMap<String, String>();
			maps.put("uid", uid);
			maps.put("qrcode_token", token);
			maps.put("sdk_version", "5.3.0");
			maps.put("test_device[name]", android.os.Build.MODEL);
			maps.put("test_device[platform]", "android");
			maps.put("test_device[info]",
					getDeviceInfo(QrcodeRegisterActivity.this));

			boolean isResult = false;
			try {
				String json = NetManager.getString(NetManager
						.getHttpClientInputStream(url, maps));
				isResult = DataParseManager.getQrcodeRegisterReplay(json);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (isResult)
				handler.sendEmptyMessage(0);
			else
				handler.sendEmptyMessage(1);

		}

	};

	Handler handler = new Handler() {

		@SuppressLint("ShowToast")
		public void handleMessage(Message msg) {
			Log.d(TAG, "msg.what=" + msg.what);
			switch (msg.what) {
			case 0:
				Toast.makeText(context, "数据读取成功，请在网站上完成注册！", Toast.LENGTH_LONG).show();
				break;
			case 1:
				Toast.makeText(context, "数据读取失败，请重试！", Toast.LENGTH_LONG).show();
				break;
			case 2:
				Toast.makeText(context, "二维码解析出错！", Toast.LENGTH_LONG).show();
				break;
			}
		}

	};

	public static String getDeviceInfo(Context context) {
		try {
			org.json.JSONObject json = new org.json.JSONObject();
			android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			String device_id = tm.getDeviceId();

			android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);

			String mac = wifi.getConnectionInfo().getMacAddress();
			json.put("mac", mac);

			if (TextUtils.isEmpty(device_id)) {
				device_id = mac;
			}

			if (TextUtils.isEmpty(device_id)) {
				device_id = android.provider.Settings.Secure.getString(
						context.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);
			}

			json.put("device_id", device_id);

			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
