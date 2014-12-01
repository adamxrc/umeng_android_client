package com.umeng.android.activity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.FeedbackBean;
import com.umeng.android.bean.User;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.android.logic.DataParseManager;
import com.umeng.android.util.DeviceUtil;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.StringUtil;
import com.umeng.android.util.ToastUtils;
import com.umeng.client.R;

@SuppressLint("SimpleDateFormat")
public class FeedbackDetailActivity extends BaseActivity {

	private final String TAG = FeedbackDetailActivity.class.getName();
	private String feedbackId;

	private AppInformation app;
	private FeedbackBean feedbackBean;
	private Dialog loadingDialog;
	private EditText contenTextView;
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private ScrollView scrollView;
	private boolean isPush = false;//推送反馈
	private LinearLayout linearLayout;//会话内容布局

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				try {
					loadingDialog.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			switch (msg.what) {
			case Constants.MSG_SUCCESS:
				ToastUtils.showMessageShort(FeedbackDetailActivity.this,
						R.string.send_success);
				constructorReplayMessage();
				initFrameView();
				break;
			case Constants.MSG_FAIL:
				ToastUtils.showMessageShort(FeedbackDetailActivity.this,
						R.string.send_fail);
				break;
			case GetListFail:
				ToastUtils.showMessageShort(FeedbackDetailActivity.this,
						R.string.get_feedback_fail);
				break;
			case GetListSuccess:
				initFrameView();
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback_detail);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		Log.d(TAG, "onCreate");
		initFrameData();
		initFrameView();
		getSupportActionBar()
				.setTitle(StringUtil.cutString(app.getName(), 120));
	}
	
	@Override
	protected void onNewIntent(Intent intent){
		Log.d(TAG, "onNewIntent");
		Log.d(TAG, "feedbackId" + intent.getStringExtra("feedbackId"));
		super.onNewIntent(intent);
		setIntent(intent);
					
		initFrameData();
		initFrameView();
					
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

	/*
	 * 设置数据
	 */
	private void initFrameData() {
		linearLayout = (LinearLayout) findViewById(R.id.feedback_line);
		app = (AppInformation) getIntent().getSerializableExtra(Constants.APP);
		
		if(!app.getName().equals(getSupportActionBar().getTitle())){
			getSupportActionBar()
			.setTitle(StringUtil.cutString(app.getName(), 120));
			linearLayout.removeAllViews();
		}
		
		feedbackBean = (FeedbackBean) getIntent().getSerializableExtra(
				"feedback");
		feedbackId = getIntent().getStringExtra("feedbackId");
		Log.d(TAG, "feedbackId" + feedbackId);
		if(feedbackId == null){
			feedbackId = feedbackBean.getFeedback_id();
		}else{
			isPush = true;//推送过来的反馈
		}
		
		if (app == null || (feedbackBean == null) && (feedbackId == null)) {
			return;
		} else if (feedbackId != null) {
			checkAndLoadData();
		}
				
	}

	/*
	 * UI初始化
	 */
	private void initFrameView() {
		if (feedbackBean == null) {
			return;
		}		
		linearLayout.removeAllViews();
		contenTextView = (EditText) findViewById(R.id.content);
		contenTextView.setText("");
		//addQuestionLayout(linearLayout);
		if (feedbackBean != null && feedbackBean.getLists() != null
				&& feedbackBean.getLists().size() > 0) {
			for (FeedbackBean.ReplyItem replyItem : feedbackBean.getLists()) {
				if (StringUtil.isEmpty(replyItem.getContent())) {
					continue;
				}
				View view = null;
				if (replyItem.getType().equals(Constants.REPLAY_DEV)) {
					view = getLayoutInflater().inflate(
							R.layout.message_developer, null);
					String time = replyItem.getDatetime();
					TextView questionTimeTextView = ((TextView) view
							.findViewById(R.id.quesstion_time));
					TextView questionTextView = ((TextView) view
							.findViewById(R.id.quesstion));
					questionTextView.setText(replyItem.getContent());
					if (time.indexOf("-") > 0 && time.lastIndexOf(":") > 0) {
						questionTimeTextView.setText(time.substring(
								time.indexOf("-") + 1, time.lastIndexOf(":")));
					}
				} else {
					view = getLayoutInflater().inflate(R.layout.message_user,
							null);
					String time = replyItem.getDatetime();
					TextView answerTimeTextView = ((TextView) view
							.findViewById(R.id.answer_time));
					TextView answerTextView = ((TextView) view
							.findViewById(R.id.answer));			
					answerTextView.setText(replyItem.getContent());
					if (time.indexOf("-") > 0 && time.lastIndexOf(":") > 0) {
						answerTimeTextView.setText(time.substring(
								time.indexOf("-") + 1, time.lastIndexOf(":")));
					}
				}
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//				params.topMargin = AppApplication.height / 25;
				params.topMargin = getScreenHeight() / 25;
				view.setLayoutParams(params);
				linearLayout.addView(view);
			}
		}

		// scroll down the reply list to the bottom.
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
			
		//发送回复按钮点击事件
		findViewById(R.id.umeng_fb_send_btn).setOnClickListener(new OnClickListener() {					
		     @Override
			 public void onClick(View v) {
				 String content = contenTextView.getText().toString();
				 if (!StringUtil.isEmpty(content)) {
				 sendReply(content);
				 // isReplay = true;
				 } else {
					ToastUtils.showMessageShort(FeedbackDetailActivity.this,
							R.string.please_input_1);
						}						
				 }
			});
	}

	
	/**
	 * add view to show question complant
	 * 
	 * @param linearLayout
	 */
	/*
	private void addQuestionLayout(LinearLayout linearLayout) {
		View view = getLayoutInflater().inflate(R.layout.message_user, null);
		TextView questionTimeTextView = ((TextView) view
				.findViewById(R.id.answer_time));
		TextView questionTextView = ((TextView) view.findViewById(R.id.answer));
		questionTextView.setText(feedbackBean.getThread());
		String time = feedbackBean.getDatetime();
		if (time.indexOf("-") > 0 && time.lastIndexOf(":") > 0) {
			questionTimeTextView.setText(time.substring(time.indexOf("-") + 1,
					time.lastIndexOf(":")));
		}
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.topMargin = getScreenHeight() / 25;
		view.setLayoutParams(params);
		linearLayout.addView(view);
	}
*/
	private void constructorReplayMessage() {
		FeedbackBean.ReplyItem replyItem = feedbackBean.new ReplyItem();
		replyItem.setContent(contenTextView.getText().toString());
		replyItem.setDatetime(simpleDateFormat.format(new Date()));
		replyItem.setType(Constants.REPLAY_DEV);
		if (feedbackBean == null || feedbackBean.getLists() == null) {
			feedbackBean.setLists(new ArrayList<FeedbackBean.ReplyItem>());
		}
		feedbackBean.getLists().add(replyItem);
	}

	/**
	 * send feedback replay
	 * 
	 * @param content
	 */
	private void sendReply(final String content) {
		if (loadingDialog == null) {
			loadingDialog = DialogManager.getLoadingDialog(this);
		}
		if ((loadingDialog != null && !loadingDialog.isShowing() && !FeedbackDetailActivity.this
				.isFinishing())) {
			loadingDialog.show();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				Message message = new Message();
				Map<String, String> maps = new HashMap<String, String>();
				maps.put("tag", "sendReply");
				maps.put("auth_token", AppApplication.getInstance().getToken());
				maps.put("content", content);
				maps.put("appkey", app.getAppkey());
				maps.put("type",Constants.REPLAY_DEV);
				maps.put("feedback_id", feedbackId);
								
				boolean success = false;
				try {
					String json = NetManager.getString(NetManager
							.getHttpClientInputStream(Constants.FEEDBACK_PROXY,maps));	
					//Log.d(TAG, "sendReply json -----" + json);					
					success = DataParseManager.getFeedBackReplay(json);
					if (success) {
						message.what = Constants.MSG_SUCCESS;
					} else {
						message.what = Constants.MSG_FAIL;
					}
				} catch (Exception e) {
					e.printStackTrace();
					message.what = Constants.MSG_FAIL;
				} finally {
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		MobclickAgent.onPause(this);
	}	
	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart();
	}
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}
	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
	}
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}

	// private void returnActivity(){
	// Intent intent = new Intent();
	// intent.putExtra("isreplay", isReplay);
	// setResult(RESULT_OK, intent);
	// finish();
	// }
	// @Override
	// public boolean onKeyUp(int keyCode, KeyEvent event) {
	// return super.onKeyDown(keyCode, event);
	// }

	public void loadData() {
		if (loadingDialog == null)
			loadingDialog = DialogManager.getLoadingDialog(this);

		if ((loadingDialog != null && !loadingDialog.isShowing() && !FeedbackDetailActivity.this
				.isFinishing()))
			loadingDialog.show();

		new GetListThread().start();
	}

	public class GetListThread extends Thread {

		public void run() {
			if (user != null) {
			   if (TextUtils.isEmpty(AppApplication.getInstance().getToken())) {
					login(user);
				}
			} 
			Map<String, String> maps = new HashMap<String, String>();
			maps.put("appkey", app.getAppkey());
			maps.put("feedback_id", feedbackId);
			maps.put("auth_token", AppApplication.getInstance().getToken());
			maps.put("tag", "getFeedbackDetail");
			
			try {
				String json = NetManager.getStingWithGet(
						Constants.FEEDBACK_PROXY, maps);
				//Log.d(TAG, "json-----" + json);
				FeedbackBean mFeedbackBean =  DataParseManager.getFeedbackBeanDetail(json);
				if (mFeedbackBean == null){
					handler.sendEmptyMessageDelayed(GetListFail, 500);					
				}else{						
					if(isPush && feedbackBean == null){//如果是推送过来的反馈
						Log.d(TAG, "isPush && feedbackBean == null");
						feedbackBean = new FeedbackBean();	
					}
					
					feedbackBean.setLists(mFeedbackBean.getLists());
					handler.sendEmptyMessageDelayed(GetListSuccess, 500);
				}
													
			} catch (Exception e) {
				e.printStackTrace();
				handler.sendEmptyMessage(GetListFail);
			}
		}
	}

	public static final int GetListSuccess = 0x1111;
	public static final int GetListFail = 0x2222;
	public int count = -1;
	public User user;
	public int code;

	public void checkAndLoadData() {
		if (!TextUtils.isEmpty(AppApplication.getInstance().getToken())) {
			loadData();
		} else {
			List<User> users = AppApplication.getInstance().getUsers();
			if (users != null && users.size() > 0) {
				user = users.get(0);
				AppApplication.getInstance().setUser(user);
				loadData();
			} else {
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		}
		
	}

	//
	// Thread loginThread = new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// if (!FeedbackDetailActivity.this.isDestroyed() & user != null) {
	// login(user);
	// }
	// }
	// });

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
					new GetListThread().start();
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
	 * @param inputStream
	 * @throws Exception
	 */
	private void close(InputStream inputStream) throws Exception {
		if (inputStream != null) {
			inputStream.close();
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed(); 
		if (isPush){
			Intent intent = new Intent(FeedbackDetailActivity.this, FeedbackActivity.class);
			intent.putExtra(Constants.APP, app);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
	
	/**
	 * 获取手机分辨率
	 */
	private int getScreenHeight(){
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}
}
