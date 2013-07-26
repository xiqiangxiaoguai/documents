package com.tcl.memo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.tcl.memo.R;

public class PaintPreview extends View {
	
	Paint mPaint;
	Paint mPaint2;
	RectF oval;
	RectF oval2;
	Path path;
	int mAlpha = 255;
	int mWidth = 5;
	
	public PaintPreview(Context context) {
		super(context);
	}

	public PaintPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

		
		path = new Path();
		oval =  new RectF(20,0,220,100);
		oval2 = new RectF(207,35,407,135);

		path.addArc(oval, 20, 140);		
		path.arcTo(oval2, 200, 140,true);

//		path.lineTo(50, 13);
//		path.lineTo(100, 0);
//		path.lineTo(150, 13);
//		path.lineTo(200, 26);
//		path.lineTo(250, 13);
//		path.lineTo(300, 0);
		
		mPaint = new Paint();
		mPaint.setDither(true);
		mPaint.setStrokeWidth(5);
		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);

//		mPaint.setStrokeCap(Paint.Cap.SQUARE);
//		mPaint.setPathEffect(new CornerPathEffect(10.0f));
		
		
	}
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.drawPath(path, mPaint);

//canvas.drawArc(oval, 0, 180, false, mPaint);
//canvas.drawArc(oval2, 180, 180, false, mPaint2);


	}
	
	public void setColor (int color) {
		mPaint.setColor(color);
		mPaint.setStrokeWidth(mWidth);
		mPaint.setAlpha(mAlpha);
		invalidate();

	}
	
	public void setBond (int width) {
		mPaint.setStrokeWidth(width);
		mWidth = width;
		invalidate();
	}	
	
	public void setAlpha (int a) {
		mPaint.setAlpha(a);
		mAlpha = a;
		invalidate();
	}
}
