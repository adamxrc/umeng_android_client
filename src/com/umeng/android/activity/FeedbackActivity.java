package com.umeng.android.activity;

import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.anim;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings.Secure;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.activity.FeedbackDetailActivity.GetListThread;
import com.umeng.android.adapter.FeedbackAdapter;
import com.umeng.android.adapter.MarketCommentAdapter;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.FeedbackBean;
import com.umeng.android.bean.MarketCommentInfo;
import com.umeng.android.bean.User;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.android.exception.AppException;
import com.umeng.android.logic.DataParseManager;
import com.umeng.android.util.DeviceUtil;
import com.umeng.android.util.JsonKey;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.StringUtil;
import com.umeng.android.util.ToastUtils;
import com.umeng.android.widget.FeedbackListView;
import com.umeng.android.widget.XListView;
import com.umeng.client.R;
import com.umeng.common.Log;

public class FeedbackActivity extends BaseActivity implements
		XListView.IXListViewListener {

	private final String TAG = FeedbackActivity.class.getName();
	private AppInformation app;
	private List<FeedbackBean> feedbackBeans = new ArrayList<FeedbackBean>();
	private XListView fbListView;
	private FeedbackAdapter feedbackAdapter;
	private Dialog loadingDialog;
	private Context mContext;
	private static String PAGECOUNT = "20";// 一次请求获取的最大数据条数
	private final int UP = 1;
	private final int DOWN = 2;
	private int actionType = UP;
	private boolean isRefreshing;
	int screenWidth;
	private int titlebarHigh;
    public final int Feedback = 0;
	public final int MarketComment = 1;
	private boolean isFeedback = true;//默认Feedback界面 false：应用市场评论页面
	private long last_updated_at;
	
	private final int FBREFERSH = 3;//刷新
	private final int FBLOADMORE = 4;//加载更多
	private int fb_tag = FBLOADMORE; //加载更多或刷新标签
	private boolean isGetError = false;//获取失败标签
	public int code;
	/** 页面title list **/
	List<String> titleList = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_back_main);
		mContext = this;
		screenWidth = getWindowManager().getDefaultDisplay().getWidth() / 2;
		titlebarHigh = DeviceUtil.dip2px(mContext, 4);
				
		initImageView();
		initTextView();
		initViewPager();
		initFrameView();
		initFrameData(0);
		
		getSupportActionBar()
				.setTitle(StringUtil.cutString(app.getName(), 120));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		initMarketContentView();
	}

	private String getTime() {
		return new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
				.format(new Date());
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
	 * init relation view
	 */
	private void initFrameView() {
		fbListView = (XListView) feedbackView.findViewById(R.id.listView);				
		fbListView.setPullRefreshEnable(true);
		fbListView.setXListViewListener(this);
		fbListView.setPullLoadEnable(true);
		fbListView.setRefreshTime(getTime());
				
		fbListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int location, long arg3) {
			
					Intent intent = new Intent(FeedbackActivity.this,
							FeedbackDetailActivity.class);
					intent.putExtra("feedback", feedbackBeans.get(location - 1));
				
					feedbackBeans.get(location -1).setIsreplied(true);
					intent.putExtra(Constants.APP, app);
					FeedbackActivity.this.startActivityForResult(intent, 0);
			}
		});
		
	}

	/**
	 * init relation data
	 */
	private void initFrameData(int tag) {
		app = (AppInformation) getIntent().getSerializableExtra(Constants.APP);
		if (app == null) {
			return;
		}
		fb_tag = tag;
		showLoadingDialog();
		new LoadFeedbacks().execute("");
	}

	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent");
		setIntent(intent);
		fbListView.setRefreshTime(getTime());
        initFrameData(FBREFERSH);		
		getSupportActionBar()
				.setTitle(StringUtil.cutString(app.getName(), 120));
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
		MobclickAgent.onPause(this);
	}

	private ViewPager mPager;// 页卡内容
	private PagerTabStrip view_pager_title;// 页卡内容
	private List<View> listViews; // Tab页面列表
	private ImageView cursor;// 动画图片
	private TextView feedback_btton, comment_button;// 页卡头标
	private View feedbackView;
	private View marketView;
	private XListView market_list_view;
	private MarketCommentAdapter markeAdapter;
	private ArrayList<MarketCommentInfo> marketList;
	private ArrayList<MarketCommentInfo> tempList;
	private ArrayList<FeedbackBean> fbBeanList = new ArrayList<FeedbackBean>();
	private User user;
	private int page = 1;

	private void initMarketContentView() {
		// TODO Auto-generated method stub
		market_list_view = (XListView) marketView
				.findViewById(R.id.market_list_view);
		market_list_view.setPullRefreshEnable(true);
		market_list_view.setPullLoadEnable(true);
		market_list_view.setXListViewListener(this);
		market_list_view.setRefreshTime(getTime());
		marketList = new ArrayList<MarketCommentInfo>();
		tempList = new ArrayList<MarketCommentInfo>();
	}

	/**
	 * 初始化头标
	 */
	private void initTextView() {
		feedback_btton = (TextView) findViewById(R.id.feedback_btton);
		comment_button = (TextView) findViewById(R.id.comment_button);
		feedback_btton.setOnClickListener(new TabOnClickListener(0));
		comment_button.setOnClickListener(new TabOnClickListener(1));
	}

	/**
	 * 初始化ViewPager
	 */
	private void initViewPager() {
		mPager = (ViewPager) findViewById(R.id.vPager);
		view_pager_title = (PagerTabStrip) findViewById(R.id.view_pager_title);
		view_pager_title.setTabIndicatorColor(getResources().getColor(
				R.color.feed_back_tab_selected_color));
		listViews = new ArrayList<View>();
		LayoutInflater mInflater = getLayoutInflater();
		feedbackView = mInflater.inflate(R.layout.activity_feedback, null);
		marketView = mInflater.inflate(R.layout.market_comment, null);
		listViews.add(feedbackView);
		listViews.add(marketView);
		titleList.add(getString(R.string.feed_back));
		titleList.add(getString(R.string.market_evaluate));
		mPager.setAdapter(new FBPagerAdapter(listViews, titleList));
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(new FBOnPageChangeListener());
	}

	/**
	 * 初始化动画
	 */
	private void initImageView() {
		cursor = (ImageView) findViewById(R.id.cursor);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
	}

	private void changeTabBackground(int index) {
		AbsoluteLayout.LayoutParams layoutParams = null;
		switch (index) {
		case Feedback:
			layoutParams = new AbsoluteLayout.LayoutParams(screenWidth,
					titlebarHigh, 0, 0);
			break;
		case MarketComment:
			layoutParams = new AbsoluteLayout.LayoutParams(screenWidth,
					titlebarHigh, screenWidth * 2, 0);
			break;
		}
		cursor.setLayoutParams(layoutParams);
	}

	/*
	 * 刷新
	 * @see com.umeng.android.widget.XListView.IXListViewListener#onRefresh()
	 */
	@Override
	public void onRefresh() {
	    Log.d(TAG, "onRefresh");
		if(isFeedback){//Feedback 刷新
			fb_tag = FBREFERSH;
			fbListView.setRefreshTime(getTime());
			new LoadFeedbacks().execute("");
		}else{//MarketComment 刷新
			if (!isRefreshing) {
				isRefreshing = true;
				actionType = DOWN;
				tempList.clear();
				page = 1;
				new LoadComments().execute("");
			}
			market_list_view.setRefreshTime(getTime());
		}
		
	}

	/*
	 * 加载更多
	 * @see com.umeng.android.widget.XListView.IXListViewListener#onLoadMore()
	 */
	@Override
	public void onLoadMore() {
         Log.d(TAG, "onLoadMore");
         if(isFeedback){//Feedback 加载更多数据
        	 fb_tag = FBLOADMORE;
        	 new LoadFeedbacks().execute("");
         }else{//MarketComment 加载更多数据
        	 if (!isRefreshing) {
     			isRefreshing = true;
     			actionType = UP;
     			if (!tempList.isEmpty()) {
     				page++;
     			}
     			tempList.clear();
     			new LoadComments().execute("");
     		}      	 
         }		
	}

	
	/**
	 * 获得消息列表 market_id
	 * 360，wandoujia，jifeng，baidu，anzhi，anzhuoshichang，xiaomi，UC，yingyonghui
	 * @return tempList ArrayList<MarketCommentInfo>
	 */
	private ArrayList<MarketCommentInfo> getComments() {
		// TODO Auto-generated method stub

		Map<String, String> maps = new HashMap<String, String>();
		maps.put("tag", "getCommentList");
		maps.put("appkey", app.getAppkey());
		maps.put("page", String.valueOf(page));
		//maps.put("pagecount", PAGECOUNT);
		maps.put("auth_token", AppApplication.getInstance().getToken());
		try {
			String json = NetManager.getStingWithGet(
					Constants.FEEDBACK_PROXY, maps);
			Log.d(TAG, "Comment json ----" + json);
			tempList = DataParseManager.getMarketCommentInfoList(json);
			return tempList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
		
	/**
	 * 头标点击监听
	 */
	public class TabOnClickListener implements View.OnClickListener {
		private int index = 0;

		public TabOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			changeTabBackground(index);
			mPager.setCurrentItem(index);
		}
	};
	
	/**
	 * ViewPager适配器
	 */
	public class FBPagerAdapter extends PagerAdapter {
		public List<View> mListViews;
		public List<String> titleList;

		public FBPagerAdapter(List<View> mListViews, List<String> titleList) {
			this.mListViews = mListViews;
			this.titleList = titleList;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// TODO Auto-generated method stub
			return (titleList.size() > position) ? titleList.get(position) : "";
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}
	
	/**
	 * 页卡切换监听
	 */
	public class FBOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int arg0) {
			Log.d(TAG, "in FBOnPageChangeListener arg0=  " +arg0);
			switch (arg0) {
			case 0:
				isFeedback = true;
				break;
			case 1:
				if(tempList.size()==0){
					new LoadComments().execute("");					
					showLoadingDialog();
					isFeedback = false;
					break;
				}
				
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

			if (arg0 == 1) {
				arg2 = screenWidth * 2;
			}
			AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(
					screenWidth, titlebarHigh, (int) (arg2 * 0.5), 0);
			cursor.setLayoutParams(layoutParams);
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}
    

	/*
	 * 显示加载中...
	 */
	private void showLoadingDialog(){
		if (loadingDialog == null) {
			loadingDialog = DialogManager
					.getLoadingDialog(FeedbackActivity.this);
		}
		if ((loadingDialog != null && !loadingDialog.isShowing() && !FeedbackActivity.this
				.isFinishing())) {
			loadingDialog.show();
		}
	}
	
	/*
	 * dismiss加载中...
	 */
	private void dismissLoadingDialog(){
		if (loadingDialog != null && loadingDialog.isShowing()) {
			loadingDialog.dismiss();
		}
	}
	
	/*
	 * 异步加载应用市场评论
	 */
	class LoadComments extends AsyncTask<String, String, ArrayList<MarketCommentInfo>> {

		@Override
		protected ArrayList<MarketCommentInfo> doInBackground(String... params) {
			if (!FeedbackActivity.this.isFinishing()) {
		
				return getComments();
			}
			// TODO Auto-generated method stub
			return null;
		}
		
		protected void onPostExecute(ArrayList<MarketCommentInfo> result) {
			// TODO Auto-generated method stub
			if (result != null && !result.isEmpty()) {
		
				if (actionType == DOWN) {
					marketList.clear();
				}
				marketList.addAll(tempList);
				if (markeAdapter == null) {
		
					markeAdapter = new MarketCommentAdapter(mContext,
							marketList);
					market_list_view.setAdapter(markeAdapter);
				} else {
					markeAdapter.notifyChanged(marketList);
				}
			}
			if (marketList.size() >= Integer.valueOf(PAGECOUNT)) {
				market_list_view.setPullLoadEnable(true);
			} else {
				market_list_view.setPullLoadEnable(false);
			}
			
			market_list_view.stopRefresh();
			market_list_view.stopLoadMore();
			dismissLoadingDialog();
			isRefreshing = false;
		}
	}	
	
	/**
	 * 获得反馈列表
	 * @return loadData ArrayList<FeedbackBean>
	 */
	private ArrayList<FeedbackBean> getFeedbacks(int tag) {
		
		
		if (!TextUtils.isEmpty(AppApplication.getInstance().getToken())) {	
			return loadData(tag);
		} else {
			List<User> users = AppApplication.getInstance().getUsers();
			if (users != null && users.size() > 0) {
				user = users.get(0);				
				if(login(user)){
					AppApplication.getInstance().setUser(user);		
					return loadData(tag);
				}				
			} else {
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		}
		return null;		
	}
	
	/**
	 * 拉取数据获得反馈列表
	 * @return fbBeanList ArrayList<FeedbackBean>
	 */	
	private ArrayList<FeedbackBean> loadData(int tag){
		// TODO Auto-generated method stub
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("tag", "getFeedbackList");
		maps.put("appkey", app.getAppkey());
		maps.put("auth_token", AppApplication.getInstance().getToken());
		maps.put("count", PAGECOUNT);
		Log.d(TAG, "tag========== " +tag);
		if(tag == FBLOADMORE && last_updated_at!=0){//加载更多
			maps.put("updated_at", last_updated_at+"");
			maps.put("st", "lt");
		}									
		try {						
			String json = NetManager.getStingWithGet(
					Constants.FEEDBACK_PROXY, maps);			
			Log.d(TAG, "FeedbackBean list json---" + json);
		    fbBeanList = DataParseManager.getFeedbackBean(json);
			    
		    if(fbBeanList != null && fbBeanList.size() > 0){
		    	 last_updated_at = fbBeanList.get(fbBeanList.size() - 1).getUpdated_at();
		    }
		    isGetError = false;
			return fbBeanList;
		} catch (Exception e) {
			isGetError = true;
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * 异步加载反馈数据
	 */
	class LoadFeedbacks extends AsyncTask<String, String, ArrayList<FeedbackBean>> {

		@Override
		protected ArrayList<FeedbackBean> doInBackground(String... params) {
			if (!FeedbackActivity.this.isFinishing()) {
		
				return getFeedbacks(fb_tag);
			}
			// TODO Auto-generated method stub
			return null;
		}
		
		protected void onPostExecute(ArrayList<FeedbackBean> result) {
						
			if (loadingDialog != null && loadingDialog.isShowing()) {
				try {
					loadingDialog.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
									
			if (result != null && !result.isEmpty()) {		
				if(fb_tag == FBREFERSH){//刷新
					feedbackBeans.clear();
					if(result.size() == Integer.valueOf(PAGECOUNT)){
						fbListView.setPullLoadEnable(true);
					}
				}
				feedbackBeans.addAll(fbBeanList);
				
				if (feedbackAdapter == null) {					
					feedbackAdapter = new FeedbackAdapter(mContext, feedbackBeans);					
					fbListView.setAdapter(feedbackAdapter);
				} else {
					feedbackAdapter.setLists(feedbackBeans);
				}				
			}
							
			fbListView.stopRefresh();
			fbListView.stopLoadMore();
			
			//获取到的数据为空 或者数据量小于20 则不显示查看更多
			if(result == null || result.size() < Integer.valueOf(PAGECOUNT)){
				if(!isGetError){
					fbListView.setPullLoadEnable(false);
				}				
			}	
			
					
			if(isGetError){
				createPopupWindowLoadingFail(new OnClickListener() {
					@Override
					public void onClick(View v) {
						fb_tag = FBREFERSH;
						showLoadingDialog();
			        	new LoadFeedbacks().execute("");
						popupWindowopupWindowLoadingFial.dismiss();
					}
				});
				showLoadFailPopupWindow(findViewById(R.id.listView));
			}
			
			dismissLoadingDialog();			
		}
	}
	
	/*
	 * 用户登录
	 */
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
//					loadData(fb_tag);
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
}

