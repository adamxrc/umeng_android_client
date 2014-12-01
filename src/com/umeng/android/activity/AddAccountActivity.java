package com.umeng.android.activity;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.umeng.analytics.MobclickAgent;
import com.umeng.android.bean.User;
import com.umeng.android.util.StringUtil;
import com.umeng.android.util.ToastUtils;
import com.umeng.client.R;

public class AddAccountActivity extends ActionBarActivity implements OnClickListener{
	
	private EditText userNamEditText;
	private EditText passwordEditText;
	private List<String> userNames;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_account);
		userNamEditText = (EditText) findViewById(R.id.userName);
		passwordEditText = (EditText) findViewById(R.id.userPwd);
		findViewById(R.id.add_account).setOnClickListener(this);
		userNames = getIntent().getStringArrayListExtra("users");
		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.drawable.actionbar_bg));
        getSupportActionBar().setTitle(R.string.add_new_user);
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
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_account:
			String userName = userNamEditText.getText().toString();
			String password = passwordEditText.getText().toString();
			if(!StringUtil.isEmpty(userName)&&StringUtil.isEmail(userName)&&!StringUtil.isEmpty(password)){
				if (!isExistUser(userName)) {
					Intent intent = new Intent(AddAccountActivity.this,
							AccountActivity.class);
					intent.putExtra("account", new User(userName, password));
					setResult(RESULT_OK, intent);
					finish();
				}else{
					ToastUtils.showMessageShort(AddAccountActivity.this, R.string.user_exist);
				}
			}else if(StringUtil.isEmpty(userName)||!StringUtil.isEmail(userName)){
				ToastUtils.showMessageShort(AddAccountActivity.this, R.string.user_name_error);
			}
			break;
		}
	}
	private boolean isExistUser(String userName){
		if(userNames == null||userNames.size() == 0){
			return false;
		}
		boolean isExist = false;
		for(String name:userNames){
			if(name.equals(userName)){
				isExist = true;
				break;
			}
		}
		return isExist;
	}
}
