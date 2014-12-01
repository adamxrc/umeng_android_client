package com.umeng.android.activity;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.bean.User;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.util.DataStorageManager;
import com.umeng.client.R;

public class AccountActivity extends ActionBarActivity {

	private List<User> users;
	private List<User> rememberUsers;
	private static final byte deleteView = 0x03;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == Constants.MSG_SUCCESS) {
				if (msg.obj != null) {
					User user = (User) msg.obj;
					users.add(user);
					initFrameView();
				}
			} else if (msg.what == deleteView) {
				initFrameView();
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		users = AppApplication.getInstance().getUsers();
		rememberUsers = AppApplication.getInstance().getRememberUsers();
		// if (users == null) {
		// users = new ArrayList<User>();
		// users.add(0, AppApplication.getInstance().getUser());
		// }
		// int i = 0;
		// for (; i < users.size(); i++) {
		// User user = users.get(i);
		// if (user.getUsername().equals(
		// AppApplication.getInstance().getUser().getUsername())) {
		// break;
		// }
		// }
		// if (i >= users.size()) {
		// users.add(AppApplication.getInstance().getUser());
		// }
		initFrameView();
		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.drawable.actionbar_bg));
		getSupportActionBar().setTitle(R.string.change_user);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.memu_add, menu);
		MenuItem refreshItem = menu.findItem(R.id.add);
		MenuItemCompat.setShowAsAction(refreshItem,
				MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.add:
			// Intent intent = new Intent(AccountActivity.this,
			// AddAccountActivity.class);
			// ArrayList<String> userNames = new ArrayList<String>();
			// for (User user : users) {
			// userNames.add(user.getUsername());
			// }
			// intent.putStringArrayListExtra("users", userNames);
			// startActivityForResult(intent, 0);
			MobclickAgent.onEvent(AccountActivity.this, "add_account");
			Intent intent = new Intent(AccountActivity.this,
					LoginActivity.class);
			intent.putExtra(Constants.LOGIN_CHANGE, true);
			AccountActivity.this.startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * init relation view
	 */
	private void initFrameView() {
		setContentView(R.layout.activity_account);
		LinearLayout accountLinearLayout = (LinearLayout) findViewById(R.id.account_layout);
		addView(accountLinearLayout);
	}

	private void addView(LinearLayout linearLayout) {
		if (users != null && users.size() > 0) {
			int len = users.size();
			if (len == 1) {
				RelativeLayout view = (RelativeLayout) getLayoutInflater()
						.inflate(R.layout.account_listitem, null);
				TextView userNameTextView = (TextView) view
						.findViewById(R.id.userName);
				userNameTextView.setText(users.get(0).getUsername());
				view.setTag(users.get(0));
				if (users
						.get(0)
						.getUsername()
						.equals(AppApplication.getInstance().getUser()
								.getUsername())) {
					((ImageView) view.findViewById(R.id.icon))
							.setBackgroundResource(R.drawable.unread_badge);
				}
				linearLayout.addView(view);
			} else {
				for (int i = 0; i < len; i++) {
					RelativeLayout view = (RelativeLayout) getLayoutInflater()
							.inflate(R.layout.account_listitem, null);
					TextView userNameTextView = (TextView) view
							.findViewById(R.id.userName);
					if (i == 0) {
						view.setBackgroundResource(R.drawable.cell_bg_top);
					} else if (i == len - 1) {
						view.setBackgroundResource(R.drawable.cell_bg_bottom);
					} else {
						view.setBackgroundResource(R.drawable.cell_bg_middle);
					}
					if (users
							.get(i)
							.getUsername()
							.equals(AppApplication.getInstance().getUser()
									.getUsername())) {
						((ImageView) view.findViewById(R.id.icon))
								.setBackgroundResource(R.drawable.unread_badge);
					}
					userNameTextView.setText(users.get(i).getUsername());
					if (!users
							.get(i)
							.getUsername()
							.equals(AppApplication.getInstance().getUser()
									.getUsername())) {
						// change user
						view.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								MobclickAgent.onEvent(AccountActivity.this,
										"add_account");
								User user = (User) v.getTag();
								Intent intent = new Intent(
										AccountActivity.this,
										LoginActivity.class);
								intent.putExtra(Constants.LOGIN_CHANGE, true);
								intent.putExtra(Constants.FILE_USER_FILENAME,
										user);
								AccountActivity.this.startActivity(intent);
								finish();
							}
						});
						// delete user
						view.setOnLongClickListener(new OnLongClickListener() {

							@Override
							public boolean onLongClick(final View v) {
								Builder builder = new AlertDialog.Builder(
										AccountActivity.this);
								builder.setTitle("")
										.setMessage(R.string.delete_user)
										.setPositiveButton(
												R.string.confirm,
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														User user = (User) v
																.getTag();
														users.remove(user);
														rememberUsers
																.remove(user);
														Message message = handler
																.obtainMessage();
														message.what = deleteView;
														handler.sendMessage(message);
													}
												});
								builder.setNegativeButton(R.string.cancel,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.dismiss();
											}

										});
								builder.create().show();
								return true;
							}
						});
					}
					view.setTag(users.get(i));
					linearLayout.addView(view);
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			User user = (User) data.getSerializableExtra("account");
			Message message = handler.obtainMessage();
			message.what = Constants.MSG_SUCCESS;
			message.obj = user;
			handler.sendMessage(message);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		if (rememberUsers != null) {
			DataStorageManager.saveUsers(this, rememberUsers);
		}
	}
}
