package com.tcl.memo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CScrollView extends android.widget.ScrollView {
	private boolean mAllowScroll;

	public CScrollView(Context context) {
		super(context);
	}

	public CScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mAllowScroll) {
			return super.onInterceptTouchEvent(ev);
		} else {
			return false;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mAllowScroll) {
			return super.onTouchEvent(ev);
		} else {
			return false;
		}
	}

	public boolean isAllowScroll() {
		return mAllowScroll;
	}

	public void setAllowScroll(boolean allowScroll) {
		mAllowScroll = allowScroll;
	}
}