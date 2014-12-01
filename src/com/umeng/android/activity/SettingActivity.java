package com.umeng.android.activity;

import java.io.InputStream;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.bean.User;
import com.umeng.android.common.AppApplication;
import com.umeng.android.util.DataStorageManager;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.ToastUtils;
import com.umeng.client.R;
import com.umeng.fb.FeedbackAgent;
import com.umeng.message.PushAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

public class SettingActivity extends BaseActivity implements OnClickListener {

	private TextView accountTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		accountTextView = (TextView) findViewById(R.id.account);
		if (AppApplication.getInstance().getUser() != null) {
			accountTextView.setText(AppApplication.getInstance().getUser()
					.getUsername());
		}
		findViewById(R.id.account_line).setOnClickListener(this);
		findViewById(R.id.suggestion_feedback).setOnClickListener(this);
		findViewById(R.id.about).setOnClickListener(this);
		findViewById(R.id.update).setOnClickListener(this);
		findViewById(R.id.open_source_license).setOnClickListener(this);
		findViewById(R.id.quit).setOnClickListener(this);
		findViewById(R.id.message_linearlayout).setOnClickListener(this);
		findViewById(R.id.push_setting_linearlayout).setOnClickListener(this);
		findViewById(R.id.test_device).setOnClickListener(this);

		getSupportActionBar().setTitle(R.string.setting);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 4, 4, R.string.menu_exit).setIcon(R.drawable.menu_exit);
		return true;
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
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.account_line:
			Intent accountIntent = new Intent(SettingActivity.this,
					AccountActivity.class);
			SettingActivity.this.startActivity(accountIntent);
			break;
		case R.id.suggestion_feedback:
			FeedbackAgent agent = new FeedbackAgent(this);
		    agent.startFeedbackActivity();
			break;
		case R.id.about:
			Intent intent = new Intent(SettingActivity.this,
					AboutActivity.class);
			SettingActivity.this.startActivity(intent);
			break;
		case R.id.update:
			UmengUpdateAgent.setUpdateAutoPopup(false);
			UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
				@Override
				public void onUpdateReturned(int updateStatus,
						UpdateResponse updateInfo) {
					switch (updateStatus) {
					case 0: // has update
						UmengUpdateAgent.showUpdateDialog(SettingActivity.this,
								updateInfo);
						break;
					case 1: // has no update
						ToastUtils.showMessageShort(SettingActivity.this,
								R.string.now_id_new_version);
						break;
					case 2: // none wifi
						ToastUtils.showMessageShort(SettingActivity.this,
								R.string.no_wifi_only_update_in_wifi);
						break;
					case 3: // time out
						ToastUtils.showMessageShort(SettingActivity.this,
								R.string.time_out);
						break;
					}
				}
			});
			UmengUpdateAgent.forceUpdate(SettingActivity.this);
			break;
		case R.id.quit:
			MobclickAgent.onEvent(SettingActivity.this, "change_account");
			final Intent logoutIntent = new Intent(this, LoginActivity.class);
			final List<User> users = AppApplication.getInstance().getUsers();
			final List<User> rememberUsers = AppApplication.getInstance()
					.getRememberUsers();
			if (users.size() <= 1) {
				User user = AppApplication.getInstance().getUser();
				new RemoveAliasThread(user).start();
				users.remove(user);
				rememberUsers.remove(user);
				DataStorageManager.saveUsers(SettingActivity.this,
						rememberUsers);
				AppApplication.getInstance().setUser(null);
				// logoutIntent.putExtra(Constants.LOGIN_CHANGE, true);
				startActivity(logoutIntent);
			} else {
				Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("")
						.setMessage(R.string.logout_change)
						.setPositiveButton(R.string.confirm,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										users.remove(AppApplication
												.getInstance().getUser());
										rememberUsers.remove(AppApplication
												.getInstance().getUser());
										DataStorageManager.saveUsers(
												SettingActivity.this,
												rememberUsers);
										AppApplication.getInstance().setUser(
												null);
										startActivity(logoutIntent);
									}
								});
				builder.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}

						});
				builder.create().show();
			}
			break;
		case R.id.open_source_license:
			Dialog dialog = createDialog();
			dialog.show();
			break;
		case R.id.message_linearlayout:
			Intent message = new Intent(SettingActivity.this,
					MessageCenterActivity.class);
			SettingActivity.this.startActivity(message);
			break;
		case R.id.push_setting_linearlayout:
			Intent pushSetingIntent = new Intent(SettingActivity.this,
					SettingPushActivity.class);
			SettingActivity.this.startActivity(pushSetingIntent);
			break;
		case R.id.test_device:
			Intent testDeviceIntent = new Intent(SettingActivity.this,
					QrcodeRegisterActivity.class);
			startActivity(testDeviceIntent);
			break;
		}
	}

	private Dialog createDialog() {
		final Dialog dialog = new Dialog(this, R.style.licenseDialog);
		View view = getLayoutInflater().inflate(R.layout.open_source_license,
				null);
		TextView contentTextView = (TextView) view.findViewById(R.id.content);
		contentTextView.setText(getLicenseContent());
		view.findViewById(R.id.confirm).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
		dialog.setContentView(view);
		dialog.setTitle(R.string.open_source_license);
		return dialog;
	}

	/**
	 * obtain open source license content
	 * 
	 * @return
	 */
	private String getLicenseContent() {
		String result = "";
		try {
			InputStream inputStream = getAssets().open("license.txt");
			result = NetManager.readFromStream(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			// System.out.println("menu key...");
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// System.out.println("back key...");
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

	public class RemoveAliasThread extends Thread {
		private User user;

		public RemoveAliasThread(User user) {
			// TODO Auto-generated constructor stub
			this.user = user;
		}

		@Override
		public void run() {
			int retryTime = 3;
			boolean needRemoveAlias = true;
			while (needRemoveAlias && retryTime > 0) {
				try {
					Thread.sleep(2000);
					retryTime--;
					needRemoveAlias = !PushAgent.getInstance(
							SettingActivity.this).removeAlias(
							user.getUsername(), "Umeng");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
