package com.umeng.android.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.common.AppApplication;
import com.umeng.client.R;

@SuppressWarnings("deprecation")
public class AboutActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_about);
		// click to copy
		TextView text = (TextView) this.findViewById(R.id.about_qq_text);
		text.setOnClickListener(new MyClickListener(text.getText().toString()));
		text = (TextView) this.findViewById(R.id.about_umeng_text);
		text.setOnClickListener(new MyClickListener(text.getText().toString()));
		text = (TextView) this.findViewById(R.id.about_sina_text);
		text.setOnClickListener(new MyClickListener(text.getText().toString()));
		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.drawable.actionbar_bg));
		getSupportActionBar().setTitle(R.string.aboutus);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

	/**
	 * click listener to copy information about umeng
	 * 
	 * @author Administrator
	 */
	private class MyClickListener implements OnClickListener {
		private String copyString;
		private Dialog dialog;

		private MyClickListener(String copyString) {
			this.copyString = copyString;
		}

		@Override
		public void onClick(View v) {
			dialog = new Dialog(AboutActivity.this, R.style.LodingDialog);
			dialog.setCanceledOnTouchOutside(true);
			LayoutInflater inflater = AboutActivity.this.getLayoutInflater();
			LinearLayout lin = (LinearLayout) inflater.inflate(
					R.layout.copy_dialog, null);
			TextView content = (TextView) lin.findViewById(R.id.copy_content);
			content.setText(copyString);
			LinearLayout copyItem = (LinearLayout) lin
					.findViewById(R.id.copy_button);
			copyItem.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					cmb.setText(copyString);
					dialog.dismiss();
				}
			});
			dialog.setContentView(lin, new LayoutParams(
					(int) (AppApplication.width / 1.3),
					LayoutParams.WRAP_CONTENT));
			dialog.show();
		}
	}

}