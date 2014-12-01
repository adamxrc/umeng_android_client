
package com.umeng.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.android.util.FrameAnimationController;
import com.umeng.android.util.PullToRefreshUtils;
import com.umeng.client.R;

public class PullToRefreshListView extends ListView {

    private static final int MSG_BUNCING_BACK = 0;

    private float mMaximumVelocity;

    private final float MAXIMUM_VELOCITY = 1.5f;

    private int mTriggerRefreshHeight;

    private boolean mTracking = false;

    private float mStartY = 0;

    private int mCurOffsetY = 0;

    private boolean mIsRefreshing = false;

    private Animation mAnimRotate, mAnimRotateBack;

    private boolean mCanRefresh = false;

    private PullDownStateListener mRefreshListener;

    private View mHeader = null;

    private View mContainer = null;

    private ImageView mBackgroundImageView = null;

    private int mMaxHeaderHeight = Integer.MAX_VALUE;

    private boolean mPullDownEnabled = true;

    private boolean mCanPullDown = true;

    private boolean mAnimating;

    private String mPullString;

    private String mRefreshingString;

    private String mReleaseString;

    private TextView mMajorText;

    private TextView mMinorText;

    private View mIndicator;

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullToRefreshListView(Context context) {
        super(context);
        init();
    }

    private void init() {
        Resources resources = getResources();
        mMaximumVelocity = PullToRefreshUtils.convertDimenToPix(getContext(), MAXIMUM_VELOCITY);
        mTriggerRefreshHeight = resources
                .getDimensionPixelSize(R.dimen.pulldown_trigger_refresh_height);
        mHeader = LayoutInflater.from(getContext()).inflate(R.layout.pulldown_header, null);
        mContainer = mHeader.findViewById(R.id.pull_header);
        mBackgroundImageView = (ImageView) mHeader.findViewById(R.id.img_bkg);
        mMajorText = (TextView) mHeader.findViewById(R.id.pull_header_major_text);
        mMinorText = (TextView) mHeader.findViewById(R.id.pull_header_minor_text);
        mIndicator = mHeader.findViewById(R.id.pullheader_indicator);

        addHeaderView(mHeader);

        mPullString = resources.getString(R.string.pulldown_pull);
        mRefreshingString = resources.getString(R.string.pulldown_refreshing);
        mReleaseString = resources.getString(R.string.pulldown_release);

        mAnimRotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_180);
        mAnimRotate.setFillAfter(true);

        mAnimRotateBack = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_back_180);
        mAnimRotateBack.setFillAfter(true);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!mCanPullDown) {
            return super.dispatchTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mPullDownEnabled) {
                    mCanRefresh = false;
                    // 当处于列表最顶端时，记录下当前位置
                    if (!mIsRefreshing && (getFirstVisiblePosition() <= 0)) {
                        prepareTracking(ev);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTracking) {
                    // 如果已经处于下拉状态，记录下当前的偏移
                    float curY = ev.getY();

                    // 点击时有可能会滑动一点，选择大于10来防止把这种操作误判成滑动操作
                    if ((curY - mStartY) > 10) {
                        requestDisallowInterceptTouchEvent(true);
                        ViewGroup.LayoutParams params = mContainer.getLayoutParams();
                        // 偏移位置以手在Y轴滑动的距离的一半为准
                        mCurOffsetY = (int) ((curY - mStartY) / 2);
                        if ((mCurOffsetY) < mMaxHeaderHeight) {
                            params.height = mCurOffsetY;
                            mContainer.setLayoutParams(params);
                            if (mCurOffsetY >= mTriggerRefreshHeight) {
                                if (!mCanRefresh) {
                                    mMajorText.setText(mReleaseString);
                                    mIndicator.startAnimation(mAnimRotate);
                                    mCanRefresh = true;
                                }
                            } else {
                                if (mCanRefresh) {
                                    mMajorText.setText(mPullString);
                                    mIndicator.startAnimation(mAnimRotateBack);
                                    mCanRefresh = false;
                                }
                            }
                        } else {
                            mCurOffsetY = Math.max(0, mMaxHeaderHeight);
                        }
                        // 进入下拉刷新状态，需要disable掉listview的上下滚动，因此需要吃掉move消息
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
                        return true;
                    }
                } else if (mPullDownEnabled && !mTracking && !mIsRefreshing
                        && (getFirstVisiblePosition() <= 0) && (mHeader.getTop() >= 0)) {
                    // 当处于列表最顶端时，记录下当前位置
                    prepareTracking(ev);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                startBuncingBack();
                if (mTracking) {
                    // 手指松开，如果滑动距离超过阈值，则触发刷新
                    if (mCanRefresh) {
                        if (mRefreshListener != null) {
                            mRefreshListener.onRefresh(PullToRefreshListView.this);
                        }
                    }
                    stopTracking();
                }
                break;
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore;
            return false;
        } catch (IndexOutOfBoundsException e) {
            // ignore;
            return false;
        }
    }

    private void prepareTracking(MotionEvent ev) {
        mMajorText.setText(mPullString);
        mTracking = true;
        mStartY = ev.getY();
        if (mRefreshListener != null) {
            mRefreshListener.onPullDownStarted(PullToRefreshListView.this);
        }
    }

    private void stopTracking() {
        mTracking = false;
        requestDisallowInterceptTouchEvent(false);
    }

    public void setBackgroundImage(Bitmap bitmap) {
        mBackgroundImageView.setImageBitmap(bitmap);
    }

    public TextView getMajorTextView() {
        return mMajorText;
    }

    public void setMajorText(String text) {
        mMajorText.setText(text);
    }

    public void setMinorText(String text) {
        mMinorText.setText(text);
    }

    public void setPullString(String mPullString) {
        this.mPullString = mPullString;
    }

    public void setRefreshingString(String mRefreshingString) {
        this.mRefreshingString = mRefreshingString;
    }

    public void setReleaseString(String mReleaseString) {
        this.mReleaseString = mReleaseString;
    }

    public void setPullDownStateListener(PullDownStateListener listener) {
        mRefreshListener = listener;
    }

    public void setRefreshing(boolean refreshing) {
        if (mIsRefreshing == refreshing) {
            return;
        }
        mIsRefreshing = refreshing;
        View prog = findViewById(R.id.pull_header_prog);
        if (mIsRefreshing) {
            mIndicator.clearAnimation();
            mIndicator.setVisibility(View.INVISIBLE);
            prog.setVisibility(View.VISIBLE);
            mMajorText.setText(mRefreshingString);
        } else {
            mIndicator.setVisibility(View.VISIBLE);
            mMajorText.setText(mPullString);
            prog.setVisibility(View.INVISIBLE);
            if (!mAnimating) {
                startBuncingBack();
            }
        }
    }

    public void stopBuncingBack() {
        mAnimating = false;
        FrameAnimationController.removeMessages(MSG_BUNCING_BACK);
    }

    /**
     * 开始回弹
     */
    public void startBuncingBack() {
        mAnimating = true;
        new BuncingBackAnimation().run();
    }

    private final class BuncingBackAnimation implements Runnable {

        @Override
        public void run() {
            final ViewGroup.LayoutParams params = mContainer.getLayoutParams();
            if (mCurOffsetY >= 0) {
                // 动画分为两部分，一部分是超过刷新距离后，回弹速度为2.0f
                // 距离小于刷新距离后，回弹速度为0.5f
                float velocity = mIsRefreshing ? mMaximumVelocity : mMaximumVelocity / 2;
                mCurOffsetY -= velocity * FrameAnimationController.ANIMATION_FRAME_DURATION;
                if (mIsRefreshing && mCurOffsetY <= mTriggerRefreshHeight) {
                    mCurOffsetY = mTriggerRefreshHeight;
                    params.height = mCurOffsetY;
                    mContainer.setLayoutParams(params);
                    stopBuncingBack();
                    return;
                }

                if (mCurOffsetY <= 0) {
                    mCurOffsetY = 0;
                    params.height = mCurOffsetY;
                    mContainer.setLayoutParams(params);
                    stopBuncingBack();
                    return;
                }
                params.height = mCurOffsetY;
                mContainer.setLayoutParams(params);
            }
            FrameAnimationController.requestAnimationFrame(MSG_BUNCING_BACK, this);
        }
    }

    public static interface PullDownStateListener {
        public void onPullDownStarted(final PullToRefreshListView listView);

        public void onRefresh(final PullToRefreshListView listView);

        public void onBouncingEnd(final PullToRefreshListView listView);
    }
}
