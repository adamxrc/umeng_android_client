package com.umeng.android.activity;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.adapter.MessageCenterAdapter;
import com.umeng.android.bean.MessageInfo;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.android.util.JsonKey;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.ToastUtils;
import com.umeng.android.widget.XListView;
import com.umeng.client.R;

public class MessageCenterActivity extends ActionBarActivity implements
		OnClickListener, XListView.IXListViewListener {

	private Context mContext;
	private XListView mListView;
	private MessageCenterAdapter adapter;
	private static String PAGE_SIZE = "20";// 刷新更多数据的数量
	private int pageCount = 1;
	private ArrayList<MessageInfo> list;
	private ArrayList<MessageInfo> tempList;
	private boolean isRefresh;
	private final int UP = 1;
	private final int DOWN = 2;
	private int actionType = UP;
	private boolean isDestroy;
	private Dialog loadingDialog;
	protected PopupWindow popupWindowopupWindowLoadingFial;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_center);
		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.drawable.actionbar_bg));
		getSupportActionBar().setTitle(R.string.message_center);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		mContext = this;
		mListView = (XListView) findViewById(R.id.list_view);
		mListView.setPullRefreshEnable(true);
		mListView.setXListViewListener(this);
		mListView.setRefreshTime(getTime());
		list = new ArrayList<MessageInfo>();
		tempList = new ArrayList<MessageInfo>();
		loadingDialog = DialogManager.getLoadingDialog(mContext,
				R.string.loading);
		if (loadingDialog != null) {
			loadingDialog.show();
		}
		new LoadMessage().execute("");
	}

	class LoadMessage extends AsyncTask<String, String, ArrayList<MessageInfo>> {

		@Override
		protected ArrayList<MessageInfo> doInBackground(String... params) {
			// TODO Auto-generated method stub

			return getMessage();
		}

		protected void onPostExecute(ArrayList<MessageInfo> result) {
			// TODO Auto-generated method stub

			if (list != null && list.size() > 0) {
				refreshAdapter();

			} else {
				closeFlushView();
				createPopupWindowLoadingFail(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onRefresh();
						if (loadingDialog != null) {
							loadingDialog.show();
						}
						popupWindowopupWindowLoadingFial.dismiss();
					}
				});
				showLoadFailPopupWindow(findViewById(R.id.list_view));
			}
		}
	}

	/**
	 * 获得消息列表
	 * 
	 * @return
	 */
	private ArrayList<MessageInfo> getMessage() {
		// TODO Auto-generated method stub

		Map<String, String> maps = new HashMap<String, String>();
		maps.put("auth_token", AppApplication.getInstance().getToken());
		maps.put("page", String.valueOf(pageCount));
		maps.put("per_page", PAGE_SIZE);
		try {
			String json = NetManager.getStingWithGet(Constants.MESSAGE, maps);
			JSONArray array = new JSONArray(json);
			int length = array.length() - 1;
			MessageInfo info;
			for (int i = length; i >= 0; i--) {

				info = new MessageInfo(array.getJSONObject(i));
				tempList.add(info);
			}

			switch (actionType) {
			case UP:
				list.clear();
				list.addAll(tempList);
				break;
			case DOWN:
				list.addAll(tempList);
				break;

			}

			tempList.clear();
			return list;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	public void deleteMessage(String id) {
		new DeleteMessageTask().execute(id);
	}

	class DeleteMessageTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			// TODO Auto-generated method stub
			if (!isDestroy) {

				Map<String, String> maps = new HashMap<String, String>();
				maps.put("auth_token", AppApplication.getInstance().getToken());
				maps.put("id", params[0]);
				InputStream stream = null;
				try {
					stream = NetManager.getHttpClientInputStream(
							Constants.MESSAGE_DELETE, maps);
					String result = NetManager.readFromStream(stream);
					return result;
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

			return null;
		}

		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub

			if (!TextUtils.isEmpty(result) && result.equals(JsonKey.SUCCESS)) {
				onRefresh();
				ToastUtils.showMessageShort(mContext,
						getString(R.string.delete_success_message));
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
		isRefresh = false;
		isDestroy = false;

		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isDestroy = true;
	}

	@Override
	public void onClick(View v) {
	}

	private String getTime() {
		return new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
				.format(new Date());
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

		if (!isRefresh) {
			isRefresh = true;
			actionType = UP;
			pageCount = 1;
			new LoadMessage().execute("");
		}

	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		if (!isRefresh) {
			isRefresh = true;
			actionType = DOWN;
			pageCount++;
			new LoadMessage().execute("");
		}
	}

	/**
	 * 刷新adatpter
	 */
	private void refreshAdapter() {
		if (adapter == null) {

			adapter = new MessageCenterAdapter(mContext, list);
		} else {
			adapter.notifyChanged(list);
		}
		if (list.size() >= Integer.valueOf(PAGE_SIZE)) {
			mListView.setPullLoadEnable(true);
		} else {
			mListView.setPullLoadEnable(false);

		}
		mListView.setAdapter(adapter);
		closeFlushView();
	}

	/**
	 * 关闭刷新界面
	 */
	private void closeFlushView() {
		mListView.stopRefresh();
		mListView.stopLoadMore();
		mListView.setRefreshTime(getTime());
		isRefresh = false;
		if (loadingDialog != null) {
			try {
				loadingDialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
