package com.umeng.android.activity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.PopupWindow;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.bean.MessageInfo;
import com.umeng.android.bean.User;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.StringUtil;
import com.umeng.client.R;

public class MessagDetailsActivity extends ActionBarActivity {

	private WebView webView;
	private String id;
	private String from = "";
	private boolean isDestroy;
	private int code;
	private User user;
	protected PopupWindow popupWindowopupWindowLoadingFial;
	private Dialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_details);
		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.drawable.actionbar_bg));
		getSupportActionBar().setTitle(R.string.message_details);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		webView = (WebView) findViewById(R.id.webView);
		loadingDialog = DialogManager.getLoadingDialog(this, R.string.loading);
		if (loadingDialog != null) {
			loadingDialog.show();
		}
		getData();
	}

	private void getData() {
		id = getIntent().getStringExtra("id");
		from = getIntent().getStringExtra("from");
		Bundle bun = getIntent().getExtras();
		if (bun != null && from == null) {
			Set<String> keySet = bun.keySet();
			for (String key : keySet) {
				String value = bun.getString(key);
				id = value;
			}
		}

		if (!TextUtils.isEmpty(AppApplication.getInstance().getToken())) {

			new LoadMessageDetail().execute("");
		} else {

			List<User> users = AppApplication.getInstance().getUsers();
			if (users != null && users.size() > 0) {
				user = users.get(0);
				AppApplication.getInstance().setUser(user);
				loginThread.start();
			} else {
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
				finish();
			}

		}
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

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPause(this);
		isDestroy = false;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isDestroy = true;
	}

	/**
	 * 获得信息详情
	 * 
	 * @return
	 */
	private MessageInfo getMessageDetail() {
		// TODO Auto-generated method stub
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("auth_token", AppApplication.getInstance().getToken());
		maps.put("id", id);

		try {
			String json = NetManager.getStingWithGet(Constants.MESSAGE_DETAIL,
					maps);
			MessageInfo info = new MessageInfo(new JSONObject(json));

			return info;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	class LoadMessageDetail extends AsyncTask<String, String, MessageInfo> {

		@Override
		protected MessageInfo doInBackground(String... params) {
			// TODO Auto-generated method stub
			if (!isDestroy) {

				return getMessageDetail();
			}
			return null;

		}

		protected void onPostExecute(MessageInfo result) {
			// TODO Auto-generated method stub

			if (result != null && !TextUtils.isEmpty(result.getContent())
					&& !isDestroy) {

				WebSettings webSettings = webView.getSettings();
				webSettings.setDefaultTextEncodingName("UTF-8");
				webView.loadData(result.getContent(),
						"text/html; charset=utf-8", null);
				myThread.start();
			} else {
				createPopupWindowLoadingFail(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (loadingDialog != null) {
							loadingDialog.show();
						}
						new LoadMessageDetail().execute("");
						popupWindowopupWindowLoadingFial.dismiss();
					}
				});
				showLoadFailPopupWindow(findViewById(R.id.webView));
			}

			if (loadingDialog != null && loadingDialog.isShowing()) {
				try {
					loadingDialog.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	Thread myThread = new Thread(new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (!isDestroy) {

				Map<String, String> maps = new HashMap<String, String>();
				maps.put("auth_token", AppApplication.getInstance().getToken());
				maps.put("id", id);
				InputStream stream = null;
				try {
					stream = NetManager.getHttpClientInputStream(
							Constants.MESSAGE_READ, maps);
					String result = NetManager.readFromStream(stream);
					Log.d("MessagDetailsActivity", result);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		}
	});
	Thread loginThread = new Thread(new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (!isDestroy & user != null) {
				login(user);
			}
		}
	});

	private boolean login(User user) {
		if (!NetManager.isOnline(this)) {
			return false;
		}
		Map<String, String> maps = new LinkedHashMap<String, String>();
		String auth = user.getUsername() + ":" + user.getPassword();
		auth = Base64.encodeToString(auth.getBytes(), Base64.DEFAULT);
		auth = StringUtil.encryptionString(auth);
		maps.put("auth", auth);
		InputStream stream = null;
		try {
			stream = NetManager.getHttpClientInputStream(Constants.AUTHORIZE,
					maps);
			String loginresult = NetManager.readFromStream(stream);
			JSONObject json = new JSONObject(loginresult);

			if (json.getInt("code") == HttpURLConnection.HTTP_OK) {
				if (json.getString("success").equals("ok")) {
					String token = json.getString("auth_token");
					AppApplication.getInstance().setToken(token);
					new LoadMessageDetail().execute("");
					close(stream);
					return true;
				}
			} else if (json.getInt("code") == 403) {
				code = 403;
			} else if (json.getInt("code") == 401) {
				code = 401;
			}
			close(stream);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				close(stream);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * close inputstream
	 * 
	 * @param inputStream
	 * @throws Exception
	 */
	private void close(InputStream inputStream) throws Exception {
		if (inputStream != null) {
			inputStream.close();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && TextUtils.isEmpty(from)
				&& !ProductsActivity.isActive) {
			Intent intent = new Intent(this, ProductsActivity.class);
			startActivity(intent);
			finish();

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * create a pop-up window when loading fail
	 */
	protected void createPopupWindowLoadingFail(OnClickListener onClickListener) {

		View view = this.getLayoutInflater().inflate(R.layout.loading_failed,
				null);
		view.setOnClickListener(onClickListener);
		if (popupWindowopupWindowLoadingFial == null) {
			popupWindowopupWindowLoadingFial = new PopupWindow(view,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
		popupWindowopupWindowLoadingFial.setOutsideTouchable(true);
		popupWindowopupWindowLoadingFial
				.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	}

	/**
	 * show popup window when loading fail...
	 */
	protected void showLoadFailPopupWindow(View view) {
		if (popupWindowopupWindowLoadingFial == null
				|| popupWindowopupWindowLoadingFial.isShowing()) {
			return;
		}
		if (!this.isFinishing()) {
			popupWindowopupWindowLoadingFial.showAtLocation(view,
					Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
		}
	}
}
