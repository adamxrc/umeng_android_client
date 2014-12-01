package com.umeng.android.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.adapter.AppAdapter;
import com.umeng.android.bean.AppInformation;
import com.umeng.android.bean.SettingInfo;
import com.umeng.android.bean.TotalDayData;
import com.umeng.android.common.AppApplication;
import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.android.exception.AppException;
import com.umeng.android.logic.DataParseManager;
import com.umeng.android.util.DataStorageManager;
import com.umeng.android.util.NetManager;
import com.umeng.android.util.StringUtil;
import com.umeng.android.util.ToastUtils;
import com.umeng.android.widget.PullToRefreshListView;
import com.umeng.android.widget.PullToRefreshListView.PullDownStateListener;
import com.umeng.client.R;
import com.umeng.fb.FeedbackAgent;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;
import com.umeng.update.UmengUpdateAgent;

/**
 * app list activity
 */
@SuppressLint("UseValueOf")
public class ProductsActivity extends BaseActivity {
	// Executor
	private ExecutorService pool;
	// search task is running
	private volatile boolean isrunning = false;
	// running search task list
	private List<Runnable> taskList;
	public static final String APP_CLICK_NUMBER = "click_nums";
	public static final String SEARCH_APP_NUMBER = "search_nums";
	// a dialog to promot whether exit
	private Dialog loadingDialog;
	// handle message constant
	private static final int EXCEPTION = 0;
	private static final int refresh = 1;
	private static final int seeMoreApps = 3;
	private static final int search = 4;
	private static final int setTop = 5;
	private static final int setTopFail = 6;
	private static final int refreshFail = 7;
	private static final int setTopFailInSearch = 8;
	private static final int setTopSuccessInSearch = 10;
	private static final int initData = 11;
	private static final int no_app = 12;
	private static final int searchRefresh = 13;
	// app number
	private int appNum = 0;
	// app list and search app list
	private List<AppInformation> apps = new ArrayList<AppInformation>();
	private List<AppInformation> allApps = null;
	private List<AppInformation> searchApps = new ArrayList<AppInformation>();
	// all apps total data
	private TotalDayData totalDayData = null;
	// popupwindow for total data
	private PopupWindow mPopupWindowTotalData;
	// to mark wether the popupwindow is showing
	private boolean isMPopupWindowTotalDataShowing = true;
	// applist list
	private PullToRefreshListView appsListview;
	// see more apps
	private LinearLayout appsListFooter;
	private LinearLayout appsListFooterProgress;
	private LinearLayout appsListFooterEmpty;
	// applist page
	private int page = 1;
	private int selectIndex = 0;
	private int showPageSize = 0;
	// how manny apps in a page
	private static final int per_pg = 20;

	// layout for pull-down refresh
	// a dialog to set app to top
	private Dialog setTopDialog;
	// search layout
	private LinearLayout searchLayout;
	private EditText editSearch;
	private LinearLayout noResult;
	private SharedPreferences seeAppNumbers;
	private SharedPreferences searchAppNumbers;
	private AppAdapter appAdapter;
	private boolean refersh = false;
	private boolean isExit = false;
	public static boolean isActive;

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_products);
		isActive = true;
		// 反馈相关设置?
		FeedbackAgent agent = new FeedbackAgent(this);
		agent.sync();
		// initFrameView();
		initFrameData();
		getSupportActionBar().setTitle(R.string.myproduct);
		UmengUpdateAgent.setUpdateAutoPopup(true);
		UmengUpdateAgent.setUpdateListener(null);
		UmengUpdateAgent.update(this);
		PushAgent mPushAgent = PushAgent.getInstance(this);
		mPushAgent.onAppStart();
		if (SettingPushActivity.isEnabled(getApplicationContext())) {
			mPushAgent.enable();
			SettingInfo info = SettingPushActivity.getPushSetting(this);
			if (info != null) {
				mPushAgent.setNoDisturbMode(info.getStart_time_hour(),
						info.getStart_time_minute(), info.getEnd_time_hour(),
						info.getEnd_time_minute());
			}
		} else {

			mPushAgent.disable();
		}

		mPushAgent.getRegistrationId();


		new AddAliasThread().start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.memu_expand, menu);
		MenuItem refreshItem = menu.findItem(R.id.expand);
		MenuItemCompat.setShowAsAction(refreshItem,
				MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.expand:
			createTotalDataPopupwindow(item);
			int xy[] = new int[2];
			searchLayout.getLocationOnScreen(xy);
			if (mPopupWindowTotalData != null) {
				mPopupWindowTotalData.showAtLocation(ProductsActivity.this
						.findViewById(R.id.myproduct_liner_n), Gravity.LEFT
						| Gravity.TOP, 0, xy[1]);
			}
			item.setIcon(R.drawable.up);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * init relation view
	 */
	private void initFrameView() {
		// init serch view
		initSerchView();
		// init refersh view
		initRefershView();
		// init apps listView
		initListView();
	}

	/**
	 * init apps listView
	 */
	private void initListView() {
		appsListview = (PullToRefreshListView) findViewById(R.id.myproduct_listview_1);
		// appsListview.addHeaderView(refreshEmpty);
		if ((apps.size() >= per_pg) && (apps.size() < appNum)) {
			appsListview.addFooterView(appsListFooterEmpty);
			appsListFooterEmpty.addView(appsListFooter);
		}
		appAdapter = new AppAdapter(ProductsActivity.this, apps);
		appsListview.setAdapter(appAdapter);
		appsListview.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				setTopDialog = DialogManager.getTopDialog(
						ProductsActivity.this, position);
				if (setTopDialog != null) {
					setTopDialog.show();
				}
				return true;
			}
		});

		appsListview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				if (appAdapter.getLists() == null
						|| appAdapter.getLists().size() == 0) {
					return;
				}
				if (position == (appAdapter.getLists().size() + 1)) {
					appsListFooterEmpty.removeAllViews();
					appsListFooterEmpty.addView(appsListFooterProgress);
					new Thread(new Runnable() {
						@Override
						public void run() {
							Message message = new Message();
							page = apps.size() / per_pg + 1;
							try {
								if (showPageSize == 0 && appsListview != null) {
									showPageSize = appsListview
											.getLastVisiblePosition()
											- appsListview
													.getFirstVisiblePosition();
								}
								loadAppsFromServer(page);
								message.arg1 = Constants.MSG_SUCCESS;
								message.arg2 = seeMoreApps;
							} catch (Exception e) {
								message.arg1 = Constants.MSG_FAIL;
								message.arg2 = setTopFailInSearch;
								e.printStackTrace();
							} finally {
								handler.sendMessage(message);
							}
						}
					}).start();
				} else {
					Intent intent = new Intent(ProductsActivity.this,
							ProductDetailActivity.class);
					if (position >= appAdapter.getLists().size()) {
						position = appAdapter.getLists().size();
					} else if (position < 1) {
						return;
					}
					seeAppNumbers = ProductsActivity.this
							.getPreferences(MODE_PRIVATE);
					int appNums = seeAppNumbers.getInt(
							ProductsActivity.APP_CLICK_NUMBER, 0);
					appNums++;
					seeAppNumbers.edit()
							.putInt(ProductsActivity.APP_CLICK_NUMBER, appNums)
							.commit();
					intent.putExtra(Constants.APP,
							appAdapter.getLists().get(position - 1));
					ProductsActivity.this.startActivity(intent);
					overridePendingTransition(R.anim.translate_activityin,
							R.anim.translate_activityout);
				}
			}
		});

		appsListview.setPullDownStateListener(new PullDownStateListener() {

			@Override
			public void onRefresh(PullToRefreshListView listView) {
				// TODO
				appsListview.setRefreshing(true);
				if (allApps != null) {
					if (isrunning) {
						taskList.add(new SearchTask(editSearch.getText()
								.toString(), true));
					} else {
						pool.submit(new SearchTask(editSearch.getText()
								.toString(), true));
					}
				} else {
					new Thread(new Runnable() {
						@Override
						public void run() {
							Message message = new Message();
							page = 1;
							try {
								refersh = true;
								loadAppsNum();
								getBaseDataFromService();
								loadAppsFromServer(page);
								message.arg1 = Constants.MSG_SUCCESS;
								message.arg2 = refresh;
								handler.sendMessage(message);
							} catch (Exception e1) {
								e1.printStackTrace();
								message.arg1 = Constants.MSG_FAIL;
								message.arg2 = refreshFail;
								handler.sendMessage(message);
							}
						}
					}).start();
				}

			}

			@Override
			public void onPullDownStarted(PullToRefreshListView listView) {

			}

			@Override
			public void onBouncingEnd(PullToRefreshListView listView) {

			}
		});
	}

	/**
	 * init refersh view
	 */
	private void initRefershView() {
		LayoutInflater inflater = getLayoutInflater();
		appsListFooterEmpty = (LinearLayout) inflater.inflate(
				R.layout.loading_more_empty, null);
		appsListFooter = (LinearLayout) inflater.inflate(
				R.layout.applist_hide_button_myproduct, null);
		appsListFooterProgress = (LinearLayout) inflater.inflate(
				R.layout.load_more_progress, null);
		noResult = (LinearLayout) inflater.inflate(R.layout.search_no_data,
				null);
	}

	/**
	 * init serch view
	 */
	private void initSerchView() {
		searchLayout = (LinearLayout) findViewById(R.id.myproduct_gesture_chooice);
		editSearch = (EditText) findViewById(R.id.search_edit);
		editSearch.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus) {
					if ((!isrunning)
							&& (allApps == null || allApps.size() == 0)) {
						pool.submit(new Thread(new SearchTask("", false)));
					}
				}
			}
		});
		editSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// is there any search task are executing
				if (isrunning) {
					taskList.add(new SearchTask(
							editSearch.getText().toString(), false));
				} else {
					pool.submit(new SearchTask(editSearch.getText().toString(),
							false));
				}
			}
		});
	}

	/**
	 * init relation data
	 */
	private void initFrameData() {
		taskList = new LinkedList<Runnable>();
		// executer init
		pool = Executors.newCachedThreadPool();
		// register exit
		pool.execute(new Runnable() {
			@Override
			public void run() {
				seeAppNumbers = ProductsActivity.this
						.getPreferences(MODE_PRIVATE);
				int AppNums = seeAppNumbers.getInt(APP_CLICK_NUMBER, -1);
				if (AppNums >= 0) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("__ct__", String.valueOf(AppNums));
					MobclickAgent.onEvent(ProductsActivity.this,
							"see_app_numbers", map);
				}
				seeAppNumbers.edit().putInt(APP_CLICK_NUMBER, 0).commit();
				AppNums = seeAppNumbers.getInt(SEARCH_APP_NUMBER, -1);
				if (AppNums >= 0) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("__ct__", String.valueOf(AppNums));
					MobclickAgent.onEvent(ProductsActivity.this,
							"search_appsnum", map);
					seeAppNumbers.edit().putInt(SEARCH_APP_NUMBER, -1).commit();
				}
			}
		});
		// loading dialog
		if (loadingDialog == null) {
			loadingDialog = DialogManager
					.getLoadingDialog(ProductsActivity.this);
		}
		if (loadingDialog != null && !loadingDialog.isShowing()) {
			loadingDialog.show();
		}
		pool.execute(new Runnable() {
			Message message = new Message();

			@Override
			public void run() {
				try {
					loadAppsData();
					loadTotalDayData();
					message.arg1 = Constants.MSG_SUCCESS;
					message.arg2 = initData;
				} catch (Exception e) {
					e.printStackTrace();
					message.arg1 = Constants.MSG_FAIL;
					if (e instanceof AppException) {
						message.arg2 = no_app;
						message.obj = e;
					} else {
						message.obj = AppException.makeException(
								AppException.TYPE_NETWORK, e);
					}
				} finally {
					handler.sendMessage(message);
				}
			}

		});
	}

	// be used to deal with message
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (loadingDialog != null && loadingDialog.isShowing()
					&& !ProductsActivity.this.isFinishing()) {
				loadingDialog.dismiss();
			}
			if (msg.arg1 == Constants.MSG_SUCCESS) {
				switch (msg.arg2) {
				case refresh:
					appAdapter.setLists(apps);
					if (appsListview.getFooterViewsCount() == 0
							&& apps.size() < appNum) {
						appsListview.addFooterView(appsListFooterEmpty);
						appsListFooterEmpty.removeAllViews();
						appsListFooterEmpty.addView(appsListFooter);
					}
					appsListview.setRefreshing(false);
					break;
				// see more apps
				case seeMoreApps:
					if (apps.size() < appNum && apps.size() > 0) {
						appsListFooterEmpty.removeAllViews();
						appsListFooterEmpty.addView(appsListFooter);
					} else {
						appsListview.removeFooterView(appsListFooterEmpty);
					}
					appAdapter.setLists(apps);
					appsListview.setSelection(selectIndex);
					break;
				case searchRefresh:
					appsListview.setRefreshing(false);
				case search:
					appsListview.removeFooterView(appsListFooterEmpty);
					if (searchApps.size() == 0) {
						appsListview.addFooterView(appsListFooterEmpty);
						appsListFooterEmpty.removeAllViews();
						appsListFooterEmpty.addView(noResult);
					} else {
						appsListview.removeFooterView(appsListFooterEmpty);
					}
					appAdapter.setLists(searchApps);
					break;
				case setTop:
					appAdapter.setLists(appAdapter.getLists());
					if (setTopDialog != null) {
						setTopDialog.dismiss();
					}
					break;
				case setTopSuccessInSearch:
					// set top success in search view
					AppAdapter settoplistadapter3 = new AppAdapter(
							ProductsActivity.this, appAdapter.getLists());
					appsListview.setAdapter(settoplistadapter3);
					Toast.makeText(ProductsActivity.this,
							R.string.set_top_success, Toast.LENGTH_SHORT)
							.show();
					break;
				case initData:
					initFrameView();
					break;
				default:
					break;
				}
			} else {
				switch (msg.arg2) {
				case setTopFail:
					if (msg.obj != null) {
						AppException appException = (AppException) msg.obj;
						appException.makeToast(ProductsActivity.this);
					}
					// ToastUtils.showMessageShort(ProductsActivity.this,R.string.set_top_fail);
					break;
				case refreshFail:
				case searchRefresh:
					// refreshEmpty.removeAllViews();
					appsListview.setRefreshing(false);
					ToastUtils.showMessageShort(ProductsActivity.this,
							R.string.refresh_fail);
					break;
				case setTopFailInSearch:
					appsListFooterEmpty.removeAllViews();
					appsListFooterEmpty.addView(appsListFooter);
					ToastUtils.showMessageShort(ProductsActivity.this,
							R.string.load_fail);
					break;
				case no_app:
					AppException appException = (AppException) msg.obj;
					appException.makeToast(ProductsActivity.this);
				default:
					break;
				}
			}
		}
	};

	/**
	 * get apps from service through search
	 */
	private void loadApps() throws Exception {
		String json = null;
		List<AppInformation> appstemp = null;
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("per_page", appNum + "");
		maps.put("auth_token", AppApplication.getInstance().getToken());
		json = NetManager.getStingWithGet(Constants.APPS, maps);
		appstemp = DataParseManager.getApps(json);
		if (allApps == null) {
			allApps = new ArrayList<AppInformation>();
		}
		if (allApps.size() != 0 && appstemp != null) {
			allApps.clear();
		}
		allApps.addAll(appstemp);
		if (searchApps == null) {
			searchApps = new ArrayList<AppInformation>();
		}
	}

	private void loadAppsNum() throws Exception {
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("auth_token", AppApplication.getInstance().getToken());
		String json = NetManager.getStingWithGet(Constants.APPS_COUNT, maps);
		appNum = DataParseManager.getAppNum(json);
	}

	@SuppressWarnings("unchecked")
	private void loadAppsData() throws Exception {
		loadAppsNum();
		// TODO if appNum == -1
		Object object = DataStorageManager.readDataFromPhone(
				Constants.PRODUCT_INFO_DIRECTOR,
				Constants.PRODUCT
						+ StringUtil.getMD5(AppApplication.getInstance()
								.getUser().getUsername()));
		if (object != null && object instanceof List
				&& ((List<AppInformation>) object).size() > 0) {
			apps = (List<AppInformation>) object;
		} else {
			loadAppsFromServer(page);
		}
	}

	/**
	 * get apps from service
	 */
	private void loadAppsFromServer(int pagetmp) throws Exception {
		String json;
		List<AppInformation> appstemp;
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("page", pagetmp + "");
		if (refersh && apps.size() > per_pg) {
			maps.put("per_page", apps.size() + "");
		} else {
			maps.put("per_page", per_pg + "");
		}
		maps.put("auth_token", AppApplication.getInstance().getToken());
		json = NetManager.getStingWithGet(Constants.APPS, maps);
		appstemp = DataParseManager.getApps(json);
		// boolean isRefersh =! apps.containsAll(appstemp);
		// if(apps != null && appstemp!=null&&isRefersh){
		// appstemp.removeAll(apps);
		// apps.addAll(0,appstemp);
		// apps.clear();
		if (refersh) {
			// apps = appstemp;
			deleteRepeatApps(appstemp);
		} else {
			selectIndex = apps.size() - showPageSize;
			if (selectIndex < 0) {
				selectIndex = 0;
			}
			deleteRepeatApps(appstemp);
		}
		// apps.addAll(appstemp);
		// for(AppInformation a:apps){
		// System.out.println(a.getAppkey());
		// }
		DataStorageManager.saveDataToPhone(
				Constants.PRODUCT_INFO_DIRECTOR,
				Constants.PRODUCT
						+ StringUtil.getMD5(AppApplication.getInstance()
								.getUser().getUsername()), apps);
		// }
	}

	/**
	 * get all apps basic statistics information from service
	 */
	private void getBaseDataFromService() throws Exception {
		String json = null;
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("auth_token", AppApplication.getInstance().getToken());
		if (!NetManager.isOnline(this)) {
			ToastUtils.showMessageShort(this, R.string.net_error);
			return;
		}
		TotalDayData tmpData = null;
		try {
			json = NetManager.getStingWithGet(Constants.BASE_DATA, maps);
			tmpData = DataParseManager.getTotalData(json);
			System.out.println(tmpData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (tmpData != null) {
			totalDayData = tmpData;
			//TODO bug ?
			String dir = Constants.TODAY_DATA_DIRECTOR;
			String file = Constants.TODAY_DATA_INFO + StringUtil.getMD5(AppApplication.getInstance().getUser().getUsername());

			DataStorageManager.saveDataToPhone( dir , file , totalDayData);
		}
	}

	/**
	 * load total data
	 * 
	 * @throws Exception
	 */
	private void loadTotalDayData() throws Exception {
		String dir = Constants.TODAY_DATA_DIRECTOR + StringUtil.getMD5(AppApplication.getInstance().getUser().getUsername());
		String file = Constants.TODAY_DATA_INFO;

		Object object = DataStorageManager.readDataFromPhone( dir, file);

		if (object != null && object instanceof TotalDayData) {
			totalDayData = (TotalDayData) object;
		} else {
			getBaseDataFromService();
		}
	}

	/**
	 * create popupwindow to show statistics information for all apps
	 * 
	 * @param item
	 */
	private void createTotalDataPopupwindow(final MenuItem item) {
		try {
			LayoutInflater inflate = this.getLayoutInflater();
			View view = (LinearLayout) inflate.inflate(R.layout.all_data, null);
			TextView textView = (TextView) view
					.findViewById(R.id.applist_text_1);
			textView.setText(totalDayData.getToday_new_users());
			textView = (TextView) view.findViewById(R.id.applist_text_3);
			textView.setText(totalDayData.getToday_active_users());
			textView = (TextView) view.findViewById(R.id.applist_text_5);
			textView.setText(totalDayData.getToday_launches());
			textView = (TextView) view.findViewById(R.id.applist_text_6);
			textView.setText(totalDayData.getInstallations());

			mPopupWindowTotalData = new PopupWindow(view,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			mPopupWindowTotalData.setOutsideTouchable(true);
			mPopupWindowTotalData.setBackgroundDrawable(new ColorDrawable(
					Color.TRANSPARENT));
			mPopupWindowTotalData
					.setAnimationStyle(R.style.mypopwindow_anim_style);
			mPopupWindowTotalData.setFocusable(true);
			mPopupWindowTotalData.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					item.setIcon(R.drawable.main_title_menu_normal);
					if (!isMPopupWindowTotalDataShowing) {
						isMPopupWindowTotalDataShowing = true;
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * intercept the return key
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Timer exitTimer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				isExit = false;
			}
		};
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isExit == false) {
				isExit = true;
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				exitTimer.schedule(task, 2000);
				isActive = false;
			} else {
				exit();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * integrate umeng analytics
	 */
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		if (AppApplication.getInstance().getToken() == null) {
			finish();
			return;
		}
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);

	}

	/**
	 * enter today data activity through a dialog witch is created on app list
	 * long click
	 * 
	 * @author Administrator
	 * 
	 */
	public void getAddStarData(final int pos) {
		// update at local firstly
		new Thread(new Runnable() {
			@Override
			public void run() {
				Message message = new Message();
				message.arg1 = Constants.MSG_SUCCESS;
				message.arg2 = setTop;
				String appKey = appAdapter.getLists().get(pos - 1).getAppkey();
				if (allApps != null) {
					allApps.remove(appAdapter.getLists().get(pos - 1));
					allApps.add(0, appAdapter.getLists().get(pos - 1));
				}
				appAdapter.getLists().add(0,
						appAdapter.getLists().remove(pos - 1));
				handler.sendMessage(message);

				String json = null;
				Map<String, String> maps = new HashMap<String, String>();
				maps.put("appkey", appKey);
				maps.put("auth_token", AppApplication.getInstance().getToken());
				try {
					json = NetManager.getStingWithGet(Constants.ADD_STAR, maps);
					JSONObject jsonObject = new JSONObject(json);
					if (jsonObject.getString("result").equals("success")) {
						// update success
					}
				} catch (Exception e) {
					e.printStackTrace();
					// it doesn't send message when updating server datas
					// message.arg1 = Constants.MSG_FAIL;
					// message.arg2 = setTopFail;
					// message.obj =
					// AppException.makeException(AppException.TYPE_NETWORK, e);
					// handler.sendMessage(message);
				}
			}
		}).start();
	}

	/**
	 * @return the apps
	 */
	public List<AppInformation> getApps() {
		return appAdapter.getLists();
	}

	private class SearchTask implements Runnable {
		String keyWords;
		boolean isRefresh;

		public SearchTask(String keyWords, boolean refresh) {
			this.keyWords = keyWords;
			this.isRefresh = refresh;
		}

		@Override
		public void run() {
			Message message = new Message();
			try {
				isrunning = true;
				if (isRefresh) {
					loadAppsNum();
				}
				if (allApps == null || allApps.size() == 0 || isRefresh) {
					loadApps();
					searchAppNumbers = ProductsActivity.this
							.getPreferences(MODE_PRIVATE);
					searchAppNumbers.edit()
							.putInt(ProductsActivity.SEARCH_APP_NUMBER, appNum)
							.commit();
				}
				if (keyWords.equals("")) {
					searchApps.clear();
					searchApps.addAll(allApps);
				} else {
					searchApps.clear();
					for (AppInformation info : allApps) {
						if (info.getName().toLowerCase()
								.contains(keyWords.toLowerCase())) {
							searchApps.add(info);
						}
					}
				}
				message.arg1 = Constants.MSG_SUCCESS;
				if (isRefresh) {
					message.arg2 = searchRefresh;
				} else {
					message.arg2 = search;
				}
				handler.sendMessage(message);
				isrunning = false;
				int len = taskList.size();
				if (len > 0) {
					Runnable task = taskList.remove(len - 1);
					taskList.clear();
					pool.submit(task);
				}
			} catch (Exception e) {
				isrunning = false;
				message.arg1 = Constants.MSG_FAIL;
				if (isRefresh) {
					message.arg2 = searchRefresh;
				} else {
					message.arg2 = EXCEPTION;
				}
				handler.sendMessage(message);
			}
		}
	}

	private void deleteRepeatApps(List<AppInformation> tmpApps) {
		if (tmpApps == null || tmpApps.size() == 0) {
			return;
		}
		if (apps == null) {
			apps = new ArrayList<AppInformation>();
			apps.addAll(tmpApps);
			return;
		}
		if (apps != null && apps.size() == 0) {
			apps.addAll(tmpApps);
			return;
		}
		ArrayList<AppInformation> lists = new ArrayList<AppInformation>();
		for (AppInformation appInformation : tmpApps) {
			int i = 0;
			for (; i < apps.size(); i++) {
				AppInformation app = apps.get(i);
				if (appInformation.getAppkey().equals(app.getAppkey())) {
					break;
				}
			}
			if (i >= apps.size()) {
				lists.add(appInformation);
			}
		}
		if (refersh) {
			apps.clear();
			apps.addAll(tmpApps);
			refersh = false;
		} else {
			apps.addAll(lists);
		}
	}


	public class AddAliasThread extends Thread{
		@Override
		public void run() {
			int retryTime = 3;
			boolean needAddAlias = true;
			while(needAddAlias && retryTime>0){
				try {
					retryTime--;
					Thread.sleep(2000);
					needAddAlias = !PushAgent.getInstance(ProductsActivity.this).addAlias(AppApplication.getInstance().getUser().getUsername(), "Umeng");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}