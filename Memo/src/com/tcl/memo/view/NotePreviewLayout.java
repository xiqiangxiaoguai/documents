
package com.tcl.memo.view;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.tcl.memo.R;
import com.tcl.memo.activity.NotePreview.PreviewNote;
import com.tcl.memo.data.NoteUtils;

/**
 * @author jingjiang.yu
 */
public class NotePreviewLayout extends LinearLayout {
    private static final String TAG = "NotandumSwitcher";

    public static final int INVALID_SCREEN = -1;

    private int mCurrentScreen = INVALID_SCREEN;

    private int mNextScreen = INVALID_SCREEN;

    private ArrayList<PreviewNote> mNoteList = new ArrayList<PreviewNote>();


    private Scroller mScroller;

    private VelocityTracker mVelocityTracker;

    private static final int TOUCH_STATE_REST = 0;

    private static final int TOUCH_STATE_SCROLLING = 1;

    private static final int TOUCH_STATE_Y_SCROLL = 2;

    private int mCurrWidth = -1;
	private int mCurrHeight = -1;
	
    private int mTouchState = TOUCH_STATE_REST;

    /**
     * The velocity at which a fling gesture will cause us to snap to the next
     * screen
     */
    private static final int SNAP_VELOCITY = 600;

    private final int TOUCH_SLOP;

    private final int MAX_VELOCITY;

    private float mDownX;

    private float mDownY;

    private float mLastMotionX;

    private float mTouchX;
    
    public  View view;

    private boolean mFirstLayout = true;

    private static final float BASELINE_FLING_VELOCITY = 2500.f;

    private static final float FLING_VELOCITY_INFLUENCE = 0.4f;
    
    private int velocityInComputeScroll = 0;
    
    private int mOrientation = Configuration.ORIENTATION_UNDEFINED;
    
    private OnDismissAudioViewListener mOnDismissAudioViewListener;
    private OvershootInterpolator mScrollInterpolator;
    private OnLayoutCompleteListener mOnLayoutCompleteListener;
    private static class OvershootInterpolator implements Interpolator {
        private static final float DEFAULT_TENSION = 1.3f;

        private float mTension;

        public OvershootInterpolator() {
            mTension = DEFAULT_TENSION;
        }

        public void setDistance(int distance) {
            mTension = distance > 0 ? DEFAULT_TENSION / distance : DEFAULT_TENSION;

        }

        public void disableSettle() {
            mTension = 0.f;
        }

        public float getInterpolation(float t) {
            // _o(t) = t * t * ((tension + 1) * t + tension)
            // o(t) = _o(t - 1) + 1
            t -= 1.0f;
            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
        }
    }

    public NotePreviewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TOUCH_SLOP = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        MAX_VELOCITY = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
        
        mScrollInterpolator = new OvershootInterpolator();
        mScroller = new Scroller(context, mScrollInterpolator);

    }

   
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int childWidth = width - getPaddingLeft() - getPaddingRight();
        int childHeight = height - getPaddingTop() - getPaddingBottom();
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, widthMode);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, heightMode);

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }

//        if (mFirstLayout) {
//            scrollTo(mCurrentScreen * (width - getPaddingRight()), 0);
//            mFirstLayout = false;
//        }
    	if (mCurrWidth != width || mCurrHeight != height) {
				scrollTo(mCurrentScreen * width, 0);
			mCurrWidth = width;
			mCurrHeight = height;
		}
    	
    	
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        int childLeft = getPaddingLeft();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.layout(childLeft, getPaddingTop(), child.getMeasuredWidth() + childLeft,
                    child.getMeasuredHeight() + getPaddingTop());

            childLeft += child.getMeasuredWidth() + getPaddingRight();
        }
        if(changed && mOnLayoutCompleteListener != null) {
			mOnLayoutCompleteListener.onLayoutComplete(this);
		}
        
    }

	public OnLayoutCompleteListener getOnLayoutCompleteListener() {
		return mOnLayoutCompleteListener;
	}

	public void setOnLayoutCompleteListener(
			OnLayoutCompleteListener onLayoutCompleteListener) {
		mOnLayoutCompleteListener = onLayoutCompleteListener;
	}

	public static interface OnLayoutCompleteListener {
		void onLayoutComplete(NotePreviewLayout layout);
	}
	
    public void setNoteList(ArrayList<PreviewNote> noteList) {
        if (noteList == null || noteList.size() == 0) {
            Activity mACtivity = (Activity) getContext();
            mACtivity.finish();
            return;
        }

        mNoteList = noteList;

        removeAllViews();

        Log.d("^^", "mNoteList.size():" + mNoteList.size());
        for (int i = 0; i < mNoteList.size(); i++) {
            FrameLayout frame = new FrameLayout(getContext());
            addView(frame);
        }
    }

    private void initNoteList(int notePosition) {

       view = View.inflate(getContext(), R.layout.note_preview_single_view, null);
        PreviewNote mPreviewNote = mNoteList.get(notePosition);
        
		((ImageView) view.findViewById(R.id.note_preview)).setImageURI(Uri.parse(mPreviewNote.thumbUri));
		((TextView)view.findViewById(R.id.one_in_some)).setText(notePosition + 1 +"/"+mNoteList.size());
		
		if(mNoteList.size() == 1){
			view.findViewById(R.id.one_in_some).setVisibility(View.GONE);
		}else{
			view.findViewById(R.id.one_in_some).setVisibility(View.VISIBLE);
		}
		
		((CheckBox)view.findViewById(R.id.add_star)).setChecked(mPreviewNote.isStar);
		
		if(mPreviewNote.hasAudio){
			((ImageView)view.findViewById(R.id.play_audio)).setVisibility(View.VISIBLE);
		}else{
			((ImageView)view.findViewById(R.id.play_audio)).setVisibility(View.GONE);
		}
		((TextView) view.findViewById(R.id.note_title)).setText(mPreviewNote.title);
		
		((TextView) view.findViewById(R.id.label)).setText(mPreviewNote.label);
		 
	
		 
		switch(mPreviewNote.mBackGround){
		case R.drawable.note_bg_1:{
			((RelativeLayout) view.findViewById(R.id.preview)).setBackgroundDrawable(getResources().getDrawable(R.drawable.note_bg_1_top));
			break;
		}
		case R.drawable.note_bg_2:{
			((RelativeLayout) view.findViewById(R.id.preview)).setBackgroundDrawable(getResources().getDrawable(R.drawable.note_bg_2_top));
			break;
		}
		case R.drawable.note_bg_3:{
			((RelativeLayout) view.findViewById(R.id.preview)).setBackgroundDrawable(getResources().getDrawable(R.drawable.note_bg_3_top));
			break;
		}
		case R.drawable.note_bg_4:{
			((RelativeLayout) view.findViewById(R.id.preview)).setBackgroundDrawable(getResources().getDrawable(R.drawable.note_bg_4_top));
			break;
		}
		case R.drawable.note_bg_5:{
			((RelativeLayout) view.findViewById(R.id.preview)).setBackgroundDrawable(getResources().getDrawable(R.drawable.note_bg_5_top));
			break;
		}
		case R.drawable.note_bg_6:{
			((RelativeLayout) view.findViewById(R.id.preview)).setBackgroundDrawable(getResources().getDrawable(R.drawable.note_bg_6_top));
			break;
		}
		
		}
		
		 ((FrameLayout) getChildAt(notePosition)).addView(view);
		 adjustForOrientationInOne(notePosition);
    }

	public void adjustForOrientation(int orientation) {
		mOrientation = orientation;
		 for (int i = mCurrentScreen -1; i <= mCurrentScreen + 1; i++) 
 {
			if (i >= 0 && i < getChildCount()) {
				adjustForOrientationInOne(i);
			}
		}
	}
	
	private void adjustForOrientationInOne(int i){
		FrameLayout frame = (FrameLayout) getChildAt(i);
		if(frame.getChildCount() != 0){
		ImageView preView = (ImageView) frame.getChildAt(0).findViewById(R.id.note_preview);
		preView.setPivotX(0);
		preView.setPivotY(0);

		float scale = 1.0f;
		if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			scale = Float.valueOf(getResources().getString(
					R.string.note_view_scale));
		}
		Resources res = getResources();
		LinearLayout fillLayout = (LinearLayout) view.findViewById(R.id.fill_layout);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fillLayout
				.getLayoutParams();
		params.width = Math.round(res
				.getDimensionPixelSize(R.dimen.note_view_width) * scale);
		params.height = Math.round(res
				.getDimensionPixelSize(R.dimen.note_view_height) * scale);
		fillLayout.setLayoutParams(params);

		preView.setScaleX(scale);
		preView.setScaleY(scale);
		
		requestLayout();
		invalidate();
		}
	}
    public ArrayList<PreviewNote> getNoteList() {
        return mNoteList;
    }

    public void setCurrentPosition(int current) {
        if (mNoteList == null || mNoteList.size() == 0) {
            Activity mActivity = (Activity) getContext();
            mActivity.finish();
            return;
        }

        mCurrentScreen = Math.max(0, Math.min(current, mNoteList.size() - 1));

        
        for(int i = mCurrentScreen -1 ; i <= mCurrentScreen + 1 ; i++){
        	if(i >=0 && i <= mNoteList.size()-1)
        	{
        		initNoteList(i);
        	}
        }
        
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }

        if (!mFirstLayout) {
            scrollTo(mCurrentScreen * (getWidth() - getPaddingRight()), 0);
            invalidate();
        }

//        showCurrentPosition(mCurrentScreen, mNoteList.size());
    }

    public int getCurrentPosition() {
        return mCurrentScreen;
    }

//    private void showCurrentPosition(int currPosition, int count) {
//        Activity activity = (Activity) getContext();
//        TextView positionView = (TextView) activity.findViewById(R.id.title_right_text);
//        if (positionView == null) {
//            Log.e(TAG, "positionView is null");
//        }
//        positionView.setText((currPosition + 1) + "/" + count);
//    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
    	Log.d("^^", "onInterceptTouchEvent");
        if (mNoteList.size() <= 1) {
            return false;
        }

        final int actionMasked = event.getActionMasked();
        final float x = event.getX();
        final float y = event.getY();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                mDownX = x;
                mDownY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveDistanceX = x - mDownX;
                float moveDistanceY = y - mDownY;
                if (mTouchState == TOUCH_STATE_REST) {
                    if (Math.abs(moveDistanceX) > TOUCH_SLOP) {
                        mTouchState = TOUCH_STATE_SCROLLING;
                        mLastMotionX = x;
                    } else if (Math.abs(moveDistanceY) > TOUCH_SLOP) {
                        mTouchState = TOUCH_STATE_Y_SCROLL;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        
        boolean returnValue = mTouchState == TOUCH_STATE_SCROLLING;
        return returnValue;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	
        final int actionMasked = event.getActionMasked();
        final float x = event.getX();
        final float y = event.getY();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        
        if(mTouchState != TOUCH_STATE_SCROLLING){
            return false;
        }

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mDownX = x;
                mDownY = y;
                mLastMotionX = x;
                break;
            case MotionEvent.ACTION_MOVE:
            	if(mOnDismissAudioViewListener != null) {
            		mOnDismissAudioViewListener.onXX();
            	}
            	
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    float deltaX = mLastMotionX - x;
                    mLastMotionX = x;

                    if (deltaX < 0) {
                        int leftmost = -(int) (getWidth() * 0.4);
                        if (mTouchX > leftmost && mTouchX < 0) {
                            mTouchX += Math.max(leftmost - mTouchX,  (float) (getWidth() * 0.15 * Math
        							.sin((deltaX / getWidth()) * (Math.PI / 2))));
                            scrollTo((int) mTouchX, 0);
                            invalidate();
                        }else{
                        	    mTouchX += Math.max(leftmost - mTouchX,deltaX);
                             scrollTo((int) mTouchX, 0);
                             invalidate();
                        }
                    } else if (deltaX > 0) {

                        final float availableToScroll = (float) (getChildAt(getChildCount() - 1)
                                .getRight() - mTouchX - getWidth());

                        if (availableToScroll > 0 ) {
                            mTouchX += Math.min(availableToScroll, deltaX);
                            scrollTo((int) mTouchX, 0);
                            invalidate();
                        }else{
                        	mTouchX += (float) (getWidth() * 0.15 * Math
        							.sin((deltaX / getWidth()) * (Math.PI / 2)));
                            scrollTo((int) mTouchX, 0);
                            invalidate();
                           }
//                        float availableToScroll = (getChildCount() - 1)
//                                * (getWidth() - getPaddingLeft()) - mTouchX;
//                        if (availableToScroll > 0) {
//                            mTouchX += Math.min(availableToScroll, deltaX);
//                            scrollTo((int) mTouchX, 0);
//                            invalidate();
//                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    mVelocityTracker.computeCurrentVelocity(1000, MAX_VELOCITY);
                    final int velocityX = (int) mVelocityTracker.getXVelocity();
                    velocityInComputeScroll = velocityX;
                    final int screenWidth = getWidth();
                    final int whichScreen = (mScrollX + (screenWidth / 2)) / screenWidth;

                    if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                        snapToScreen(mCurrentScreen - 1, velocityX);
                    } else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
                        snapToScreen(mCurrentScreen + 1, velocityX);
                    } else {
                        snapToScreen(whichScreen, 0);
                    }
                }

                mTouchState = TOUCH_STATE_REST;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    final int screenWidth = getWidth();
                    final int whichScreen = (mScrollX + (screenWidth / 2)) / screenWidth;
                    snapToScreen(whichScreen, 0);
                }
                mTouchState = TOUCH_STATE_REST;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;

        }

        return true;
    }

    private void snapToScreen(int whichScreen, int velocity) {

        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));

        
        final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
        final int newX = whichScreen * getWidth();
        final int delta = newX - mScrollX;
        int duration = (screenDelta + 1) * 100;
        
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        
        mScrollInterpolator.disableSettle();
        
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration += (duration / (velocity / BASELINE_FLING_VELOCITY))
                    * FLING_VELOCITY_INFLUENCE;
        } else {
            duration += 100;
        }

        awakenScrollBars(duration);
        mScroller.startScroll(mScrollX, 0, delta, 0, duration);
        	
        mCurrentScreen = whichScreen;
        mNextScreen = whichScreen;

        Log.d("^^", "whichScreen" + whichScreen);
//        showCurrentPosition(mCurrentScreen, mFilePathList.size());

        invalidate();
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        mTouchX = x;
    }

    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mTouchX = mScrollX = mScroller.getCurrX();
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
        	if(Math.abs(velocityInComputeScroll) > SNAP_VELOCITY){
           int which = mCurrentScreen;
				if (mScroller.getFinalX() < mScroller.getStartX()) {
					if (mCurrentScreen != mNoteList.size() - 1) {
						if (mCurrentScreen - 1 >= 0) {
							which = mCurrentScreen - 1;
							initNoteList(which);

							Log.d("^^", "to left initNoteList(" + which + ");");
						}
						if (mCurrentScreen + 2 < mNoteList.size()) {
							((FrameLayout) getChildAt(mCurrentScreen + 2))
									.removeAllViews();
							int j = mCurrentScreen + 2;
							Log.d("^^", "to left remove:" + j);
						}
					}
				}else if (mScroller.getFinalX() > mScroller.getStartX()){
					if (mCurrentScreen != 0) {
						if (mCurrentScreen + 1 < mNoteList.size()) {
							which = mCurrentScreen + 1;
							initNoteList(which);

							Log.d("^^", "to right initNoteList(" + which + ");");
						}
						if (mCurrentScreen - 2 >= 0) {
							((FrameLayout) getChildAt(mCurrentScreen - 2))
									.removeAllViews();
							int k = mCurrentScreen - 2;
							Log.d("^^", "to right remove:" + k);
						}
					}
        		}
        	}
        	if(velocityInComputeScroll != 0){
        		velocityInComputeScroll = 0;
        	}
            mNextScreen = INVALID_SCREEN;
            
        }
    }

    protected void dispatchDraw(Canvas canvas) {

        boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING && mNextScreen == INVALID_SCREEN;

        if (fastDraw) {
            drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());
            return;
        }

        final float scrollPos = (float) getScrollX() / (getWidth() - getPaddingLeft());
        final int leftScreen = (int) scrollPos;
        final int rightScreen = leftScreen + 1;

        if (leftScreen >= 0) {
            drawChild(canvas, getChildAt(leftScreen), getDrawingTime());
        }
        if (scrollPos != leftScreen && rightScreen < getChildCount()) {
            drawChild(canvas, getChildAt(rightScreen), getDrawingTime());
        }
    }
    
    
    
    public OnDismissAudioViewListener getOnXXListener() {
		return mOnDismissAudioViewListener;
	}


	public void setOnXXListener(OnDismissAudioViewListener onDismissAudioViewListener) {
		mOnDismissAudioViewListener = onDismissAudioViewListener;
	}

	public static interface OnDismissAudioViewListener {
    	void onXX();
    }

}
