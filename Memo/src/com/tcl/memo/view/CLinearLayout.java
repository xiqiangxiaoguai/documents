package com.tcl.memo.view;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class CLinearLayout extends LinearLayout{
	
	private OnLayoutCompleteListener mOnLayoutCompleteListener;
	
	public CLinearLayout(Context context) {
		super(context);
	}

	public CLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		
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
		void onLayoutComplete(CLinearLayout layout);
	}
}
