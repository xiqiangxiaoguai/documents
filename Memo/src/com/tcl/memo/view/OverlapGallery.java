package com.tcl.memo.view;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

public class OverlapGallery extends Gallery 
{
	// mCamera是用来做类3D效果处理，比如Z轴方向上的平移，绕Y轴的旋转等
	private Camera mCamera = new Camera();
	// mMaxRotationAngle是图片绕Y轴最大旋转角度，也就是屏幕最边上那两张图片的旋转角度
	private int mMaxRotationAngle = 100;
	// mMaxZoom是图片在Z轴平移的距离，视觉上看上进心来就是放大缩小的效果
	private int mMaxZoom = -200;
	private int mCoveflowCenter;
	private boolean mAlphaMode = true;
	private boolean mCircleMode = false;
	
	private NumberObserver mObserver;
	
	public OverlapGallery(Context context) 
	{
		super(context);
		this.setStaticTransformationsEnabled(true);
	}

	public OverlapGallery(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		this.setStaticTransformationsEnabled(true);
	}

	public OverlapGallery(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		this.setStaticTransformationsEnabled(true);
	}

	public int getMaxRotationAngle() 
	{
		return mMaxRotationAngle;
	}

	public void setMaxRotationAngle(int maxRotationAngle) 
	{
		mMaxRotationAngle = maxRotationAngle;
	}

	public boolean getCircleMode() 
	{
		return mCircleMode;
	}

	public void setCircleMode(boolean isCircle)
	{
		mCircleMode = isCircle;
	}

	public boolean getAlphaMode() 
	{
		return mAlphaMode;
	}

	public void setAlphaMode(boolean isAlpha) 
	{
		mAlphaMode = isAlpha;
	}

	public int getMaxZoom() 
	{
		return mMaxZoom;
	}

	public void setMaxZoom(int maxZoom) 
	{
		mMaxZoom = maxZoom;
	}
	
	public void setNumberObserver(NumberObserver observer)
	{
		mObserver = observer;
	}

	private int getCenterOfCoverflow() 
	{
		return (getWidth() - getPaddingLeft() - getPaddingRight())/2	+ getPaddingLeft();
	}

	// 获取视图中心
	private static int getCenterOfView(View view) 
	{
		return view.getLeft() + view.getWidth() / 2;
	}

	// 重写Garray方法 ，产生层叠和放大效果
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) 
	{
		final int childCenter = getCenterOfView(child);
		final int childWidth = child.getWidth();

		final int position = (Integer) child.getTag();

		// 旋转角度
		int rotationAngle = 0;
		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);
		if (childCenter == mCoveflowCenter) 
		{
			transformImageBitmap(child, t, 0, 0);

			mObserver.numberNotice(position);
		} 
		else 
		{
			rotationAngle = (int) (((float) (mCoveflowCenter - childCenter) / childWidth) * mMaxRotationAngle);
			// Log.d("test", "recanglenum:"+Math.floor ((mCoveflowCenter - childCenter)/childWidth));

			/*
			if (Math.abs(rotationAngle) > mMaxRotationAngle) 
			{
				rotationAngle = (rotationAngle < 0) ? -mMaxRotationAngle : mMaxRotationAngle;
			}
			*/

			transformImageBitmap(child, t, rotationAngle, (int) Math.floor((mCoveflowCenter - childCenter) / (childWidth == 0 ? 1 : childWidth)));
		}
		
		return true;
	}

	/**
	 * This is called during layout when the size of this view has changed. If
	 * you were just added to the view hierarchy, you're called with the old
	 * values of 0.
	 * 
	 * @param w
	 *            Current width of this view.
	 * @param h
	 *            Current height of this view.
	 * @param oldw
	 *            Old width of this view.
	 * @param oldh
	 *            Old height of this view.
	 */
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		mCoveflowCenter = getCenterOfCoverflow();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	/**
	 * Transform the Image Bitmap by the Angle passed
	 * 
	 * @param imageView
	 *            ImageView the ImageView whose bitmap we want to rotate
	 * @param t
	 *            transformation
	 * @param rotationAngle
	 *            the Angle by which to rotate the Bitmap
	 */
	private void transformImageBitmap(View child, Transformation t, int rotationAngle, int d) 
	{
		mCamera.save();
		final Matrix imageMatrix = t.getMatrix();
		final int imageHeight = child.getLayoutParams().height;
		final int imageWidth = child.getLayoutParams().width;
		final int rotation = Math.abs(rotationAngle);
		
		// mCamera.translate(0.0f, 0.0f, 100.0f);
		// As the angle of the view gets less, zoom in
		// if (rotation <= mMaxRotationAngle) {
		float zoomAmount = (float) (-180 + rotation);
		if (rotationAngle < 0)
		{
			//mCamera.translate((float) (-rotation * 0.5), (float) (-rotation * 0.3) + 5, zoomAmount);
			mCamera.translate((float) (-rotation), (float) (-rotation * 0.3) + 5, zoomAmount);
		} 
		else 
		{
			mCamera.translate((float) rotation, (float) (-rotation * 0.3) + 5, zoomAmount);
		}
		
		if (mCircleMode) 
		{
			if (rotation < 40) 
			{
				mCamera.translate(0.0f, 155, 0.0f);
			}
			else 
			{
				mCamera.translate(0.0f, (255 - rotation * 2.5f), 0.0f);
			}
		}
		if (mAlphaMode) 
		{
			// child.setBackgroundDrawable(getResources().getDrawable(R.drawable.app_bg));
			// child.setBackgroundColor(255);
		}
		// }
		// mCamera.rotateY(rotationAngle);
		mCamera.getMatrix(imageMatrix);
		imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
		imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
		mCamera.restore();
	}

	// 重载视图显示顺序让左到中间显示，再到右到中间显示
	protected int getChildDrawingOrder(int childCount, int i) 
	{
		int selectedIndex = getSelectedItemPosition() - getFirstVisiblePosition();
		
		// Just to be safe
		if (selectedIndex < 0)
		{
			return i;
		}

		if (i == childCount - 1) 
		{
			// Draw the selected child last
			return selectedIndex;
		}
		else if (i >= selectedIndex) 
		{
			return childCount - 1 - (i - selectedIndex);
		}
		else 
		{
			return i;
		}
	}
}
