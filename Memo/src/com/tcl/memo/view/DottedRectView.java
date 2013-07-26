package com.tcl.memo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class DottedRectView extends View {

	private int mLineWidth = 5;
	private Rect mDottedRect;
	private Paint mDottedPaint = new Paint();

	public DottedRectView(Context context) {
		this(context, null);
	}

	public DottedRectView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DottedRectView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mDottedPaint.setStrokeWidth(1);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mDottedRect != null) {
			int i;
			int temp;

			for (i = mDottedRect.left; i < mDottedRect.right;) {
				temp = i + mLineWidth;
				if (temp >= mDottedRect.right) {
					temp = mDottedRect.right - 1;
				}
				canvas.drawLine(i, mDottedRect.top, temp, mDottedRect.top,
						mDottedPaint);

				i += 2 * mLineWidth;
			}

			for (i = mDottedRect.top; i < mDottedRect.bottom;) {
				temp = i + mLineWidth;
				if (temp >= mDottedRect.bottom) {
					temp = mDottedRect.bottom - 1;
				}
				canvas.drawLine(mDottedRect.right - 1, i,
						mDottedRect.right - 1, temp, mDottedPaint);
				i += 2 * mLineWidth;
			}

			for (i = mDottedRect.right - 1; i >= mDottedRect.left;) {
				temp = i - mLineWidth;
				if (temp < mDottedRect.left) {
					temp = mDottedRect.left;
				}
				canvas.drawLine(i, mDottedRect.bottom - 1, temp,
						mDottedRect.bottom - 1, mDottedPaint);
				i -= 2 * mLineWidth;
			}

			for (i = mDottedRect.bottom - 1; i >= mDottedRect.top;) {
				temp = i - mLineWidth;
				if (temp < mDottedRect.top) {
					temp = mDottedRect.top;
				}
				canvas.drawLine(mDottedRect.left, i, mDottedRect.left, temp,
						mDottedPaint);
				i -= 2 * mLineWidth;
			}
		}
	}

	public Rect getDottedRect() {
		return mDottedRect;
	}

	public void setDottedRect(Rect dottedRect) {
		mDottedRect = dottedRect;
	}
}