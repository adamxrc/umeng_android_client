package com.umeng.android.activity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.bean.User;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.android.exception.AppException;
import com.umeng.android.util.DataStorageManager;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.StringUtil;
import com.umeng.android.util.ToastUtils;
import com.umeng.client.R;

public class LoginActivity extends Activity {

	// logining from other page .for instance from change user of setting
	private boolean loginChange = false;
	private Dialog loadingDialog;
	private boolean rememberUser = false;
	private boolean loginAuto = true;
	private boolean login_add = false;
	private Button loginButton;
	private EditText userNameEditText;
	private EditText passwordEditText;
	private CheckBox rememberCheckBox;
	private TextView qrcodeRegisterTextView;
	private User user;
	private List<User> users;
	private boolean logining = false;
	private boolean loginOne = false;
	private int code = 0;
	private static final int INIT = 0x03;
	// a handler to deal with message
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (loadingDialog != null) {
				try {
					loadingDialog.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			logining = false;
			switch (msg.what) {
			case Constants.MSG_SUCCESS:
				// check deleting data or no
				boolean deleteData = DataStorageManager
						.isClearData(Constants.DATA_TIME_OUT);
				if (deleteData) {
					DataStorageManager.clearData(DataStorageManager.PATH);
				}
				Intent intent = new Intent(LoginActivity.this,
						ProductsActivity.class);
				startActivity(intent);
				finish();
				overridePendingTransition(R.anim.translate_activityin_return,
						R.anim.translate_activityout_return);
				break;
			case Constants.MSG_FAIL:
				if (code == 403) {
					ToastUtils.showMessageShort(LoginActivity.this,
							R.string.no_oauth);
				} else if (code == 401) {
					ToastUtils.showMessageShort(LoginActivity.this,
							R.string.validate);
				} else {
					ToastUtils.showMessageShort(LoginActivity.this,
							R.string.net_error);
				}
				MobclickAgent.onEvent(LoginActivity.this, "login_fail");
				setContentView(R.layout.activity_login);
				initFrameView();
				if (user != null) {
					userNameEditText.setText(user.getUsername());
					passwordEditText.setText(user.getPassword());
					rememberCheckBox.setChecked(AppApplication.getInstance()
							.isRemembered(user));
				}
				break;
			case INIT:
				login_add = true;
				initFrameView();
				if (user != null) {
					userNameEditText.setText(user.getUsername());
				}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initFrameData();
	}

	/**
	 * init relation data
	 */
	private void initFrameData() {

		loginChange = getIntent()
				.getBooleanExtra(Constants.LOGIN_CHANGE, false);
		users = AppApplication.getInstance().getUsers();
		if (users != null && users.size() > 0) {
			user = users.get(0);
		}
		if (loginChange) {
			loginOne = true;
			DataStorageManager.clearData(DataStorageManager.PATH);
			user = (User) getIntent().getSerializableExtra(
					Constants.FILE_USER_FILENAME);
		}

		if (user != null && user.getPassword() != null) {
			setContentView(R.layout.splash_screen);
			loadingDialog = DialogManager.getLoadingDialog(LoginActivity.this,
					R.string.logining);
			if (loadingDialog != null && !loadingDialog.isShowing()) {
				loadingDialog.show();
			}
			handler.post(new Runnable() {
				@Override
				public void run() {
					new Thread(new LoginRunnable()).start();
				}
			});
		} else {
			Message message = handler.obtainMessage();
			message.what = INIT;
			handler.sendMessage(message);
		}
	}

	/**
	 * init relation view
	 */
	private void initFrameView() {
		loginOne = true;
		loginAuto = false;
		// 登录按钮点击事件
		loginButton = (Button) findViewById(R.id.main_button_1);
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!NetManager.isOnline(LoginActivity.this)) {
					ToastUtils.showMessageShort(LoginActivity.this,
							R.string.net_error);
					return;
				}
				String userName = userNameEditText.getText().toString();
				String password = passwordEditText.getText().toString();
				user = new User(userName, null);
				if (AppApplication.getInstance().isLogin(user) && login_add) {
					ToastUtils.showMessageShort(LoginActivity.this,
							R.string.user_exist);
					return;
				}
				boolean result = validateUserInfo(userName, password);
				if (result && !logining) {
					loadingDialog = DialogManager.getLoadingDialog(
							LoginActivity.this, R.string.logining);
					if (loadingDialog != null) {
						loadingDialog.show();
					}
					logining = true;
					new Thread(new LoginRunnable()).start();
				}
			}
		});

		userNameEditText = (EditText) findViewById(R.id.main_edit_1);
		passwordEditText = (EditText) findViewById(R.id.main_edit_2);

		// 记住用户CheckBox点击事件
		rememberCheckBox = (CheckBox) findViewById(R.id.remember_checkbox);
		rememberCheckBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						rememberUser = isChecked;
					}
				});
		
		qrcodeRegisterTextView = (TextView) findViewById(R.id.tv_add_test_device);
		qrcodeRegisterTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		qrcodeRegisterTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, QrcodeRegisterActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			
		});
	}

	private boolean login() {
		if (loginOne) {
			return login(this.user);
		} else {
			for (User user : users) {
				this.user = user;
				if (login(user)) {
					return true;
				}
			}
		}
		return false;
	}

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
					if (!loginAuto) {
						if (rememberUser) {
							AppApplication.getInstance().saveUser(user);
						} else {
							AppApplication.getInstance().forgetUser(user);
						}
					}
					String token = json.getString("auth_token");
					AppApplication.getInstance().setToken(token);
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

	/**
	 * intercept return key
	 */
	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// switch (keyCode) {
	// case KeyEvent.KEYCODE_BACK:
	// DialogManager.getExitDialog(LoginActivity.this).show();
	// break;
	// }
	// return true;
	// }
	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	/**
	 * integrate umeng_analytics
	 */
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	/**
	 * username and password is illegal
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	private boolean validateUserInfo(String userName, String password) {

		int messageRid = 0;
		if (userName == null || userName.trim().equals("")) {
			messageRid = R.string.username_not_null;
		} else if (password == null || password.trim().equals("")) {
			messageRid = R.string.password_not_null;
		} else if (!StringUtil.isEmail(userName)) {
			messageRid = R.string.user_name_error;
		} else {
			user.setPassword(password);
			return true;
		}
		ToastUtils.showMessageShort(LoginActivity.this, messageRid);
		return false;
	}

	/**
	 * connect the net to login validate
	 */
	private class LoginRunnable implements Runnable {
		@Override
		public void run() {
			Message msg = Message.obtain();
			boolean result = login();
			if (result) {
				AppApplication.getInstance().setUser(user);
				msg.what = Constants.MSG_SUCCESS;
			} else {
				msg.what = Constants.MSG_FAIL;
				msg.obj = AppException.makeException(
						AppException.TYPE_NETWORK,
						new Exception(getResources().getString(
								R.string.net_error)));
			}
			handler.sendMessage(msg);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && !loginChange) {
			Intent intent = new Intent(this, ExitActivity.class);
			intent.putExtra("exit", true);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}