package com.umeng.android.activity;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.android.util.StringUtil;
import com.umeng.android.util.ToastUtils;
import com.umeng.android.widget.CalendarView;
import com.umeng.android.widget.Cell;
import com.umeng.client.R;

public class CalendarActivity extends Activity implements
		CalendarView.OnCellTouchListener, OnClickListener {
	public static final String MIME_TYPE = "vnd.android.cursor.dir/vnd.exina.android.calendar.date";
	private CalendarView mView = null;
	private Handler mHandler = new Handler();
	private Rect ecBounds;
	private ImageView prevMonthImageView;
	private ImageView nextMonthImageView;
	private TextView timeTextView;
	private boolean today;

	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.calender);
        today = getIntent().getBooleanExtra("today", true);
        mView = (CalendarView)findViewById(R.id.calendar);
        mView.setOnCellTouchListener(this);
        String dateString = getIntent().getStringExtra("date");
        if (dateString != null) {
        	Calendar calendar = Calendar.getInstance();
        	calendar.setTime(StringUtil.getDateFromString(dateString));
        	mView.goDate(calendar);
		}
        prevMonthImageView = (ImageView) findViewById(R.id.prev_month);
        prevMonthImageView.setOnClickListener(this);
        nextMonthImageView = (ImageView) findViewById(R.id.next_month);
        nextMonthImageView.setOnClickListener(this);
        timeTextView = (TextView) findViewById(R.id.time);
        if(mView.getMonth()+1>=10){
        	timeTextView.setText(mView.getYear()+"-"+(mView.getMonth()+1));
        }else{
        	timeTextView.setText(mView.getYear()+"-0"+(mView.getMonth()+1));
        }
        
    }

	public void onTouch(Cell cell) {

		Intent intent = getIntent();
		String action = intent.getAction();
		if (cell.mPaint.getColor() == Color.GRAY) {
			mView.previousMonth();
		} else if (cell.mPaint.getColor() == Color.LTGRAY) {
			mView.nextMonth();
		} else {
			if (action.equals(Intent.ACTION_PICK)
					|| action.equals(Intent.ACTION_GET_CONTENT)) {
				ecBounds = cell.getBound();
				mView.getDate();
				mView.circleDrawable.setBounds(ecBounds);
				mView.invalidate();
				String dateString = getDateString(mView.getYear(),
						mView.getMonth() + 1, cell.getDayOfMonth());
				if (!StringUtil.dataIsIlleage(dateString, today)) {
					ToastUtils.showMessageShort(this, R.string.error_date);
					return;
				}
				Intent resultIntent = new Intent(this,
						ProductTrendDetailActivity.class);
				resultIntent.putExtra("result", dateString);
				setResult(RESULT_OK, resultIntent);
				finish();
				return;
			}
		}
		mHandler.post(new Runnable() {
			public void run() {
				if (mView.getMonth() >= 9) {
					timeTextView.setText(mView.getYear() + "-"
							+ (mView.getMonth() + 1));
				} else {
					timeTextView.setText(mView.getYear() + "-0"
							+ (mView.getMonth() + 1));
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.prev_month:
			mView.previousMonth();
			if (mView.getMonth() + 1 >= 10) {
				timeTextView.setText(mView.getYear() + "-"
						+ (mView.getMonth() + 1));
			} else {
				timeTextView.setText(mView.getYear() + "-0"
						+ (mView.getMonth() + 1));
			}
			break;

		case R.id.next_month:
			mView.nextMonth();
			if (mView.getMonth() + 1 >= 10) {
				timeTextView.setText(mView.getYear() + "-"
						+ (mView.getMonth() + 1));
			} else {
				timeTextView.setText(mView.getYear() + "-0"
						+ (mView.getMonth() + 1));
			}
			break;
		}
	}

	/**
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	private String getDateString(int year, int month, int day) {
		StringBuilder builder = new StringBuilder();
		builder.append(year).append("-");
		if (month >= 10) {
			builder.append(month).append("-");
		} else {
			builder.append("0").append(month).append("-");
		}
		if (day >= 10) {
			builder.append(day);
		} else {
			builder.append("0").append(day);
		}
		return builder.toString();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindDrawables(findViewById(R.id.rootView));
		// System.gc();
		Runtime.getRuntime().gc();
	}

	private void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}
}