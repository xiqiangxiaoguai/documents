package com.tcl.memo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class ShowNumberView extends View 
{
	
	private float mX;
	private float my;
	
	private int count;
	
	private int position;
	
	private Paint mPaint;
	
	public ShowNumberView(Context context) 
	{
		super(context);
		
		initPaint();
	}

	public ShowNumberView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		initPaint();
	}

	public ShowNumberView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		
		initPaint();
	}
	
	private void initPaint()
	{
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTypeface(Typeface.SERIF);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setTextSize(25f);
		mPaint.setColor(Color.WHITE);
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{
		canvas.drawText((position + 1) + "/" + count, mX, my, mPaint);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		super.onSizeChanged(w, h, oldw, oldh);
		mX = w * 0.5f;
		my = h * 0.5f;
	}
	
	public void setPosition(int position) 
	{
		this.position = position;
	}
	
	public void setCount(int count)
	{
		this.count = count;
	}
}
