package com.tcl.memo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RegionIterator;
import android.util.AttributeSet;
import android.view.View;

public class MapCropView extends View {

	public MapCropView(Context context) {
		this(context, null);
	}

	public MapCropView(Context context, AttributeSet attrs) {
		this(context, null, 0);
	}

	public MapCropView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setBackgroundColor(Color.TRANSPARENT);
		mPaint.setColor(Color.parseColor("#50000000"));
	}

	private Paint mPaint = new Paint();

	@Override
	protected void onDraw(Canvas canvas) {
		Region region = new Region();
		int w = 100;
		int width = getWidth();
		int height = getHeight();
		region.set(new Rect(0, 0, width, height));
		region.op(new Rect(width/2 - 300, height/2 - 250, width/2 + 300, height/2 +250), Region.Op.DIFFERENCE);
		RegionIterator regIter = new RegionIterator(region);
		Rect rect = new Rect();
		while (regIter.next(rect)) {
			canvas.drawRect(rect, mPaint);
		}
	}

}