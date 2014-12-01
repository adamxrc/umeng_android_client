package com.umeng.android.widget;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.MonthDisplayHelper;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.umeng.client.R;

public class CalendarView extends ImageView {
	// private static int WEEK_LEFT_MARGIN = (int)(AppApplication.width*0.064);
	private static int CELL_WIDTH = 0;
	private static int CELL_HEIGH = 0;
	private static int CELL_MARGIN_TOP = 0;
	private static int CELL_MARGIN_LEFT = 0;
	private static float CELL_TEXT_SIZE;

	public static final int CURRENT_MOUNT = 0;
	public static final int NEXT_MOUNT = 1;
	public static final int PREVIOUS_MOUNT = -1;
	private Calendar mRightNow = null;
	private Cell mToday = null;
	private Cell[][] mCells = new Cell[6][7];
	private OnCellTouchListener mOnCellTouchListener = null;
	MonthDisplayHelper mHelper;
	public Drawable circleDrawable = null;
	private Context context;
	private int width;
	private int height;

	public interface OnCellTouchListener {
		public void onTouch(Cell cell);
	}

	public CalendarView(Context context) {
		this(context, null);
	}

	public CalendarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CalendarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		circleDrawable = context.getResources().getDrawable(
				R.drawable.day_selected);
		initCalendarView();
	}

	private void initCalendarView() {
		mRightNow = Calendar.getInstance();

		// set background
		setImageResource(R.drawable.calendar_bg);
		mHelper = new MonthDisplayHelper(mRightNow.get(Calendar.YEAR),
				mRightNow.get(Calendar.MONTH), mRightNow.getFirstDayOfWeek());
	}

	private void initCells() {
		initCells(mRightNow);
	}

	private void initCells(Calendar calendar) {
		class _calendar {
			public int day;
			public int whichMonth; // -1 Ϊ���� 1Ϊ���� 0Ϊ����

			public _calendar(int d, int b) {
				day = d;
				whichMonth = b;
			}

			public _calendar(int d) { // �ϸ��� Ĭ��Ϊ
				this(d, PREVIOUS_MOUNT);
			}
		}
		;
		_calendar tmp[][] = new _calendar[6][7];

		for (int i = 0; i < tmp.length; i++) {
			int n[] = mHelper.getDigitsForRow(i);
			for (int d = 0; d < n.length; d++) {
				if (mHelper.isWithinCurrentMonth(i, d))
					tmp[i][d] = new _calendar(n[d], CURRENT_MOUNT);
				else if (i == 0) {
					tmp[i][d] = new _calendar(n[d]);
				} else {
					tmp[i][d] = new _calendar(n[d], NEXT_MOUNT);
				}

			}
		}

		int thisDay = 0;
		mToday = null;
		if (mHelper.getYear() == calendar.get(Calendar.YEAR)
				&& mHelper.getMonth() == calendar.get(Calendar.MONTH)) {
			thisDay = calendar.get(Calendar.DAY_OF_MONTH);
		}
		// build cells
		Rect Bound = new Rect(CELL_MARGIN_LEFT, CELL_MARGIN_TOP, CELL_WIDTH
				+ CELL_MARGIN_LEFT, CELL_HEIGH + CELL_MARGIN_TOP);
		for (int week = 0; week < mCells.length; week++) {
			for (int day = 0; day < mCells[week].length; day++) {
				if (tmp[week][day].whichMonth == CURRENT_MOUNT) { // ����
																	// ��ʼ����cell
					if (day == 0 || day == 6)
						mCells[week][day] = new RedCell(tmp[week][day].day,
								new Rect(Bound), CELL_TEXT_SIZE);
					else
						mCells[week][day] = new Cell(tmp[week][day].day,
								new Rect(Bound), CELL_TEXT_SIZE);
				} else if (tmp[week][day].whichMonth == PREVIOUS_MOUNT) { // ����Ϊgray
					mCells[week][day] = new GrayCell(tmp[week][day].day,
							new Rect(Bound), CELL_TEXT_SIZE);
				} else { // ����ΪLTGray
					mCells[week][day] = new LTGrayCell(tmp[week][day].day,
							new Rect(Bound), CELL_TEXT_SIZE);
				}
				Bound.offset(CELL_WIDTH, 0); // move to next column
				// get today
				if (tmp[week][day].day == thisDay
						&& tmp[week][day].whichMonth == 0) {
					mToday = mCells[week][day];
					circleDrawable.setBounds(mToday.getBound());
				}
			}

			Bound.offset(0, CELL_HEIGH); // move to next row and first column
			Bound.left = CELL_MARGIN_LEFT;
			Bound.right = CELL_MARGIN_LEFT + CELL_WIDTH;
		}
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		width = getWidth();
		height = getHeight();

		CELL_WIDTH = (int) (width / 7);
		CELL_HEIGH = (int) (height / 6);
		CELL_MARGIN_TOP = 0;
		CELL_MARGIN_LEFT = 0;

		CELL_TEXT_SIZE = (int) (height * 0.0474);
		initCells();
		super.onLayout(changed, left, top, right, bottom);
	}

	public int getYear() {
		return mHelper.getYear();
	}

	public int getMonth() {
		return mHelper.getMonth();
	}

	public void nextMonth() {
		mHelper.nextMonth();
		initCells();
		invalidate();
	}

	public void previousMonth() {
		mHelper.previousMonth();
		initCells();
		invalidate();
	}

	public boolean firstDay(int day) {
		return day == 1;
	}

	public boolean lastDay(int day) {
		return mHelper.getNumberOfDaysInMonth() == day;
	}

	public void goDate(Calendar calendar) {
		mRightNow = calendar;
		mHelper = new MonthDisplayHelper(calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH), calendar.getFirstDayOfWeek());
		initCells(calendar);
		invalidate();
	}

	public void goToday() {
		Calendar cal = Calendar.getInstance();
		mHelper = new MonthDisplayHelper(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH));
		initCells(Calendar.getInstance());
		invalidate();
	}

	public Calendar getDate() {
		return mRightNow;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mOnCellTouchListener != null) {
			for (Cell[] week : mCells) {
				for (Cell day : week) {
					if (day.hitTest((int) event.getX(), (int) event.getY())) {
						mOnCellTouchListener.onTouch(day);
					}
				}
			}
		}
		return super.onTouchEvent(event);
	}

	public void setOnCellTouchListener(OnCellTouchListener p) {
		mOnCellTouchListener = p;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// draw background
		super.onDraw(canvas);

		// draw cells
		for (Cell[] week : mCells) {
			for (Cell day : week) {
				day.draw(canvas);
			}
		}

		// draw today
		if (circleDrawable != null && mToday != null) {
			circleDrawable.draw(canvas);
		}
		if (circleDrawable.getBounds() != null) {
			circleDrawable.draw(canvas);
			// ���������� Ҫ��취
			circleDrawable = context.getResources().getDrawable(
					R.drawable.day_selected);
			// mDecoraClick.setBounds(null);
		}
	}

	private class GrayCell extends Cell {
		public GrayCell(int dayOfMon, Rect rect, float s) {
			super(dayOfMon, rect, s);
			mPaint.setColor(Color.GRAY);
		}
	}

	private class LTGrayCell extends Cell {
		public LTGrayCell(int dayOfMon, Rect rect, float s) {
			super(dayOfMon, rect, s);
			mPaint.setColor(Color.LTGRAY);
		}
	}

	private class RedCell extends Cell {
		public RedCell(int dayOfMon, Rect rect, float s) {
			super(dayOfMon, rect, s);
			mPaint.setColor(0xdddd0000);
		}
	}
}