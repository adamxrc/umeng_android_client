package com.umeng.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.umeng.android.common.AppApplication;

public class ExitActivity extends Activity {
	boolean exit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadScreenWidthAndHeight();
		exit = getIntent().getBooleanExtra("exit", false);
		if (exit) {
			finish();
			System.exit(0);
		} else {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
		}
	}

	/**
	 * obtain the width and height of screen
	 */
	private void loadScreenWidthAndHeight(){
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		AppApplication.width = dm.widthPixels;
		AppApplication.height = dm.heightPixels;
	}
}
