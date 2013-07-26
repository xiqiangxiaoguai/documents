package com.tcl.memo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PreviewCircle extends View{
	
	Paint mPaint;
	int mRadios = 25;
	int mR = 25;
	int mColor = Color.WHITE;
	int mAlpha = 255;
	public PreviewCircle(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public PreviewCircle(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init () {
		mPaint = new Paint();
		mPaint.setColor(Color.WHITE);
		mPaint.setAntiAlias(true);
//		mPaint.setStyle(Paint.Style.FILL);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		mPaint.setColor(mColor);
		mPaint.setAlpha(mAlpha);
		canvas.drawCircle(mR, mR, mRadios, mPaint);
	}
	
	public void setR(int R) {
		mRadios = R;
		invalidate();
	}
	
	public void setAlpha(int a) {
		mAlpha = a ;
		invalidate();
	}
	
	public void setColor (int color) {
		mColor = color;
		invalidate();
	}

}
