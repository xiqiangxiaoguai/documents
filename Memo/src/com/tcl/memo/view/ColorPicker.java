package com.tcl.memo.view;

import com.tcl.memo.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ColorPicker extends View {

	private Bitmap color_picker;

	private Paint mPaint;

	private Paint paint;

	private Matrix matrix;

	public float mCurrentX;

	public float mCurrentY;

	private OnColorChangedListener OnCCL;

	public ColorPicker(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	private void init() {
		BitmapFactory.Options op = new BitmapFactory.Options();

		color_picker = BitmapFactory.decodeResource(getResources(),
				R.drawable.color_picker);
		paint = new Paint();
		matrix = new Matrix();
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStrokeWidth(2);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		canvas.drawBitmap(color_picker, matrix, paint);

		if (mCurrentX != 0 && mCurrentY != 0) {
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setColor(Color.WHITE);
			canvas.drawCircle(mCurrentX, mCurrentY, 5, mPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		float x = event.getX();
		float y = event.getY();

		if (x > 0 && x < color_picker.getWidth() && y > 0
				&& y < color_picker.getHeight()) {
			mCurrentX = x;
			mCurrentY = y;
			if (OnCCL != null)
				OnCCL.onColorChanged(color_picker.getPixel((int) mCurrentX,
						(int) mCurrentY));

			invalidate();
		}

		return true;
	}

	public void SetOnColorChangedListener(OnColorChangedListener l) {

		OnCCL = l;
	}

	public static interface OnColorChangedListener {

		public void onColorChanged(int color);

	}

	public void SetSelectCirclePosition(float x, float y) {

		mCurrentX = x;
		mCurrentY = y;

		OnCCL.onColorChanged(color_picker.getPixel((int) mCurrentX,
				(int) mCurrentY));
		invalidate();
	}
	
	public void SetCirclePosition(float x, float y) {

		mCurrentX = x;
		mCurrentY = y;

		invalidate();
	}
	
	public int GetColorByXY (float x, float y){
		return(color_picker.getPixel((int)x, (int)y));
	}
}
