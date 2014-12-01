package com.umeng.android.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.umeng.android.common.Constants;
import com.umeng.android.dialog.DialogManager;
import com.umeng.client.R;

@SuppressLint("Registered")
public class BaseActivity extends ActionBarActivity {

	protected PopupWindow popupWindowopupWindowLoadingFial;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.drawable.actionbar_bg));
	}

	/**
	 * exit application
	 */
	public void exit() {
		Intent intent = new Intent(this, ExitActivity.class);
		intent.putExtra("exit", true);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, 3, 3, R.string.setting).setIcon(R.drawable.setting);
		menu.add(0, 4, 4, R.string.menu_exit).setIcon(R.drawable.menu_exit);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int item_id = item.getItemId();

		switch (item_id) {

		case Constants.EXIT:
			// exit
			DialogManager.getExitDialog(BaseActivity.this).show();
			break;

		case Constants.SEETING:
			Intent setingIntent = new Intent(BaseActivity.this,
					SettingActivity.class);
			BaseActivity.this.startActivity(setingIntent);
		}
		return true;
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.translate_activityin_return,
				R.anim.translate_activityout_return);
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
		// if (!firstStart) {
		popupWindowopupWindowLoadingFial.setOutsideTouchable(true);
		popupWindowopupWindowLoadingFial
				.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		// }
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
