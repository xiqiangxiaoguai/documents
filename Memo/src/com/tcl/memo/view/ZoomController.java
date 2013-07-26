package com.tcl.memo.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.tcl.memo.R;

public class ZoomController extends View {
	private static final int INVALID_VALUE = -1;
	private static final int DIRECTION_UP = 1;
	private static final int DIRECTION_DOWN = 2;
	private static final int DIRECTION_LEFT = 3;
	private static final int DIRECTION_RIGHT = 4;
	private static final int DIRECTION_LEFT_UP = 5;
	private static final int DIRECTION_RIGHT_DOWN = 6;
	private static final int DIRECTION_RIGHT_UP = 7;
	private static final int DIRECTION_LEFT_DOWN = 8;
	private static final int DIRECTION_CENTER = 9;

	private static final int BOUNDARY_SIZE = 5;
	private static final int MIN_MOVE_DISTANCE = 5;

	private final int HANDLE_SIZE;
	private final int MIN_BORDER_SIZE;
	private final Bitmap HANDLE_VERTICAL;
	private final Bitmap HANDLE_HORIZONTAL;
	private final Bitmap HANDLE_LEFT_DOWN;
	private final Bitmap HANDLE_RIGHT_UP;
	private final Rect BORDER_INSIDE_RECT = new Rect();
	// private final Rect BORDER_OUTSIDE_RECT = new Rect();
	private final Rect[] HANDLE_RECTS = new Rect[8];
	private final Paint BORDER_PAINT = new Paint();

	private float mX;
	private float mY;
	private int mTouchMoveX;
	private int mTouchMoveY;
	private int mMoveDistanceX;
	private int mMoveDistanceY;
	private int mTouchDirection;
	private boolean mAllowMove;

	private boolean mShowBorder;
	private boolean mZoomViewCanFocus;

	private View mZoomView;
	private OnZoomListener mOnZoomListener;
	private OnBorderShowListener mOnBorderShowListener;

	private int mTouchCenterCount;

	private final InputMethodManager mInputManager;

	public ZoomController(Context context) {
		this(context, null);
	}

	public ZoomController(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ZoomController(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		Resources res = getResources();

		HANDLE_VERTICAL = BitmapFactory.decodeResource(res,
				R.drawable.handle_vertical);
		HANDLE_HORIZONTAL = BitmapFactory.decodeResource(res,
				R.drawable.handle_horizontal);
		HANDLE_LEFT_DOWN = BitmapFactory.decodeResource(res,
				R.drawable.handle_left_down);
		HANDLE_RIGHT_UP = BitmapFactory.decodeResource(res,
				R.drawable.handle_right_up);

		HANDLE_SIZE = HANDLE_VERTICAL.getWidth();

		MIN_BORDER_SIZE = 2 * HANDLE_SIZE;

		BORDER_PAINT.setColor(Color.parseColor("#600000ff"));
		BORDER_PAINT.setStrokeWidth(0);

		for (int i = 0; i < HANDLE_RECTS.length; i++) {
			HANDLE_RECTS[i] = new Rect();
		}

		mInputManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mX = event.getX();
		mY = event.getY();
		if (mZoomView != null && mX >= 0 && mX < getWidth() && mY >= 0
				&& mY < getHeight()) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (mZoomView.isFocused()) {
					if (mX >= mZoomView.getLeft() && mX < mZoomView.getRight()
							&& mY >= mZoomView.getTop()
							&& mY <= mZoomView.getBottom()) {
						mZoomView.onTouchEvent(event);
						return false;
					}
				}
				mTouchMoveX = Math.round(mX);
				mTouchMoveY = Math.round(mY);

				mAllowMove = false;
				mTouchDirection = getDirection(mTouchMoveX, mTouchMoveY);
				if (mZoomViewCanFocus) {
					if (mTouchDirection != DIRECTION_CENTER) {
						requestZoomViewFocus(false);
					} else {
						mTouchCenterCount++;
						if (!mShowBorder && !mZoomView.isFocused()) {
							if (mTouchCenterCount >= 2) {
								requestZoomViewFocus(true);
								return false;
							}
							mShowBorder = true;
							invalidate();
							if (mOnBorderShowListener != null) {
								mOnBorderShowListener.onBorderShow(mZoomView,
										mShowBorder, BORDER_INSIDE_RECT.left,
										BORDER_INSIDE_RECT.top,
										BORDER_INSIDE_RECT.right,
										BORDER_INSIDE_RECT.bottom);
							}
						}
					}
				} else {
					if (mTouchDirection == DIRECTION_CENTER && !mShowBorder) {
						mShowBorder = true;
						invalidate();
						if (mOnBorderShowListener != null) {
							mOnBorderShowListener.onBorderShow(mZoomView,
									mShowBorder, BORDER_INSIDE_RECT.left,
									BORDER_INSIDE_RECT.top,
									BORDER_INSIDE_RECT.right,
									BORDER_INSIDE_RECT.bottom);
						}
					}
				}
				if (mTouchDirection == INVALID_VALUE && mShowBorder) {
					mShowBorder = false;
					mTouchCenterCount = 0;
					invalidate();
					if (mOnBorderShowListener != null) {
						mOnBorderShowListener.onBorderShow(mZoomView,
								mShowBorder, BORDER_INSIDE_RECT.left,
								BORDER_INSIDE_RECT.top,
								BORDER_INSIDE_RECT.right,
								BORDER_INSIDE_RECT.bottom);
					}
					return false;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (mShowBorder) {
					mMoveDistanceX = Math.round(mX - mTouchMoveX);
					mMoveDistanceY = Math.round(mY - mTouchMoveY);

					if (!mAllowMove) {
						if (Math.abs(mMoveDistanceX) >= MIN_MOVE_DISTANCE
								|| Math.abs(mMoveDistanceY) >= MIN_MOVE_DISTANCE) {
							mMoveDistanceX = 0;
							mMoveDistanceY = 0;
							mAllowMove = true;
						}
					}

					if (mAllowMove
							&& (Math.abs(mMoveDistanceX) >= 1 || Math
									.abs(mMoveDistanceY) >= 1)) {
						adjustBorderRect();
						mTouchMoveX = Math.round(mX);
						mTouchMoveY = Math.round(mY);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mZoomViewCanFocus) {
					if (!mAllowMove && mTouchCenterCount >= 2
							&& mTouchDirection == DIRECTION_CENTER) {
						requestZoomViewFocus(true);
					} else if(mTouchDirection != DIRECTION_CENTER) {
						requestZoomViewFocus(false);
					}
				}
				break;
			}
		}

		return true;
	}

	private int getDirection(int x, int y) {
		if (BORDER_INSIDE_RECT.contains(x, y)) {
			return DIRECTION_CENTER;
		}

		int index = INVALID_VALUE;
		for (int i = 0; i < HANDLE_RECTS.length; i++) {
			if (HANDLE_RECTS[i].contains(x, y)) {
				index = i;
				break;
			}
		}

		switch (index) {
		case INVALID_VALUE:
			return INVALID_VALUE;
		case 0:
			return DIRECTION_UP;
		case 1:
			return DIRECTION_DOWN;
		case 2:
			return DIRECTION_LEFT;
		case 3:
			return DIRECTION_RIGHT;
		case 4:
			return DIRECTION_LEFT_UP;
		case 5:
			return DIRECTION_RIGHT_DOWN;
		case 6:
			return DIRECTION_RIGHT_UP;
		case 7:
			return DIRECTION_LEFT_DOWN;
		}

		return INVALID_VALUE;
	}

	public void requestZoomViewFocus(boolean requestFocus) {
		if (mZoomView != null) {
			if (requestFocus && mZoomViewCanFocus) {
				if (!mZoomView.isFocused()) {
					mTouchCenterCount = 0;
					mZoomView.setFocusable(true);
					mZoomView.setFocusableInTouchMode(true);
					mZoomView.requestFocus();
					mInputManager.showSoftInput(mZoomView, 0);
					if (mShowBorder) {
						mShowBorder = false;
						invalidate();
						if (mOnBorderShowListener != null) {
							mOnBorderShowListener.onBorderShow(mZoomView,
									mShowBorder, BORDER_INSIDE_RECT.left,
									BORDER_INSIDE_RECT.top,
									BORDER_INSIDE_RECT.right,
									BORDER_INSIDE_RECT.bottom);
						}
					}
				}
			} else {
				if (mZoomView.isFocused()) {
					mTouchCenterCount = 0;
					mZoomView.setFocusable(false);
					mZoomView.setFocusableInTouchMode(false);
					mZoomView.clearFocus();
					mInputManager.hideSoftInputFromWindow(
							mZoomView.getWindowToken(), 0);
				}
			}
		}
	}

	private void adjustBorderRect() {
		int width = BORDER_INSIDE_RECT.width();
		int height = BORDER_INSIDE_RECT.height();

		float borderScale;
		if (width == height) {
			borderScale = 1;
		} else if (width != 0 && height != 0) {
			borderScale = width / (float) height;
		} else {
			borderScale = INVALID_VALUE;
		}

		switch (mTouchDirection) {
		case DIRECTION_UP:
			BORDER_INSIDE_RECT.top += mMoveDistanceY;
			if (BORDER_INSIDE_RECT.height() < MIN_BORDER_SIZE) {
				BORDER_INSIDE_RECT.top = BORDER_INSIDE_RECT.bottom
						- MIN_BORDER_SIZE;
			}
			break;
		case DIRECTION_DOWN:
			BORDER_INSIDE_RECT.bottom += mMoveDistanceY;
			if (BORDER_INSIDE_RECT.height() < MIN_BORDER_SIZE) {
				BORDER_INSIDE_RECT.bottom = BORDER_INSIDE_RECT.top
						+ MIN_BORDER_SIZE;
			}
			break;
		case DIRECTION_LEFT:
			BORDER_INSIDE_RECT.left += mMoveDistanceX;
			if (BORDER_INSIDE_RECT.width() < MIN_BORDER_SIZE) {
				BORDER_INSIDE_RECT.left = BORDER_INSIDE_RECT.right
						- MIN_BORDER_SIZE;
			}
			break;
		case DIRECTION_RIGHT:
			BORDER_INSIDE_RECT.right += mMoveDistanceX;
			if (BORDER_INSIDE_RECT.width() < MIN_BORDER_SIZE) {
				BORDER_INSIDE_RECT.right = BORDER_INSIDE_RECT.left
						+ MIN_BORDER_SIZE;
			}
			break;
		case DIRECTION_LEFT_UP:
			if (borderScale != INVALID_VALUE) {
				BORDER_INSIDE_RECT.left += mMoveDistanceX;
				BORDER_INSIDE_RECT.top += mMoveDistanceY;
				if (BORDER_INSIDE_RECT.width() < MIN_BORDER_SIZE) {
					BORDER_INSIDE_RECT.left = BORDER_INSIDE_RECT.right
							- MIN_BORDER_SIZE;
				}
				if (BORDER_INSIDE_RECT.height() < MIN_BORDER_SIZE) {
					BORDER_INSIDE_RECT.top = BORDER_INSIDE_RECT.bottom
							- MIN_BORDER_SIZE;
				}
			}
			break;
		case DIRECTION_RIGHT_DOWN:
			if (borderScale != INVALID_VALUE) {
				BORDER_INSIDE_RECT.right += mMoveDistanceX;
				BORDER_INSIDE_RECT.bottom += mMoveDistanceY;
				if (BORDER_INSIDE_RECT.width() < MIN_BORDER_SIZE) {
					BORDER_INSIDE_RECT.right = BORDER_INSIDE_RECT.left
							+ MIN_BORDER_SIZE;
				}
				if (BORDER_INSIDE_RECT.height() < MIN_BORDER_SIZE) {
					BORDER_INSIDE_RECT.bottom = BORDER_INSIDE_RECT.top
							+ MIN_BORDER_SIZE;
				}
			}
			break;
		case DIRECTION_RIGHT_UP:
			if (borderScale != INVALID_VALUE) {
				BORDER_INSIDE_RECT.right += mMoveDistanceX;
				BORDER_INSIDE_RECT.top += mMoveDistanceY;
				if (BORDER_INSIDE_RECT.width() < MIN_BORDER_SIZE) {
					BORDER_INSIDE_RECT.right = BORDER_INSIDE_RECT.left
							+ MIN_BORDER_SIZE;
				}
				if (BORDER_INSIDE_RECT.height() < MIN_BORDER_SIZE) {
					BORDER_INSIDE_RECT.top = BORDER_INSIDE_RECT.bottom
							- MIN_BORDER_SIZE;
				}
			}
			break;
		case DIRECTION_LEFT_DOWN:
			if (borderScale != INVALID_VALUE) {
				BORDER_INSIDE_RECT.left += mMoveDistanceX;
				BORDER_INSIDE_RECT.bottom += mMoveDistanceY;
				if (BORDER_INSIDE_RECT.width() < MIN_BORDER_SIZE) {
					BORDER_INSIDE_RECT.left = BORDER_INSIDE_RECT.right
							- MIN_BORDER_SIZE;
				}
				if (BORDER_INSIDE_RECT.height() < MIN_BORDER_SIZE) {
					BORDER_INSIDE_RECT.bottom = BORDER_INSIDE_RECT.top
							+ MIN_BORDER_SIZE;
				}
			}
			break;
		case DIRECTION_CENTER:
			BORDER_INSIDE_RECT.left += mMoveDistanceX;
			BORDER_INSIDE_RECT.right += mMoveDistanceX;
			BORDER_INSIDE_RECT.top += mMoveDistanceY;
			BORDER_INSIDE_RECT.bottom += mMoveDistanceY;
			break;
		}

		if (mTouchDirection != INVALID_VALUE) {
			if (borderScale != INVALID_VALUE
					&& (mTouchDirection == DIRECTION_LEFT_UP
							|| mTouchDirection == DIRECTION_RIGHT_DOWN
							|| mTouchDirection == DIRECTION_RIGHT_UP || mTouchDirection == DIRECTION_LEFT_DOWN)) {

				int newWidth = BORDER_INSIDE_RECT.width();
				int newHeight = BORDER_INSIDE_RECT.height();
				float tempBorderScale = newWidth / (float) newHeight;

				if (tempBorderScale < borderScale) {
					newHeight = Math.round(newWidth / borderScale);
				} else if (tempBorderScale > borderScale) {
					newWidth = Math.round(newHeight * borderScale);
				}

				switch (mTouchDirection) {
				case DIRECTION_LEFT_UP:
					BORDER_INSIDE_RECT.left = BORDER_INSIDE_RECT.right
							- newWidth;
					BORDER_INSIDE_RECT.top = BORDER_INSIDE_RECT.bottom
							- newHeight;
					break;
				case DIRECTION_RIGHT_DOWN:
					BORDER_INSIDE_RECT.right = BORDER_INSIDE_RECT.left
							+ newWidth;
					BORDER_INSIDE_RECT.bottom = BORDER_INSIDE_RECT.top
							+ newHeight;
					break;
				case DIRECTION_RIGHT_UP:
					BORDER_INSIDE_RECT.right = BORDER_INSIDE_RECT.left
							+ newWidth;
					BORDER_INSIDE_RECT.top = BORDER_INSIDE_RECT.bottom
							- newHeight;
					break;
				case DIRECTION_LEFT_DOWN:
					BORDER_INSIDE_RECT.left = BORDER_INSIDE_RECT.right
							- newWidth;
					BORDER_INSIDE_RECT.bottom = BORDER_INSIDE_RECT.top
							+ newHeight;
					break;
				}
			}

			// int halfBorderSize = (HANDLE_SIZE + BOUNDARY_SIZE) / 2;
			// BORDER_OUTSIDE_RECT.left = BORDER_INSIDE_RECT.left -
			// halfBorderSize;
			// BORDER_OUTSIDE_RECT.top = BORDER_INSIDE_RECT.top -
			// halfBorderSize;
			// BORDER_OUTSIDE_RECT.right = BORDER_INSIDE_RECT.right +
			// halfBorderSize;
			// BORDER_OUTSIDE_RECT.bottom = BORDER_INSIDE_RECT.bottom +
			// halfBorderSize;

			invalidate();
			if (mOnZoomListener != null && mZoomView != null) {
				mOnZoomListener.onZoom(mZoomView, BORDER_INSIDE_RECT.left,
						BORDER_INSIDE_RECT.top, BORDER_INSIDE_RECT.right,
						BORDER_INSIDE_RECT.bottom);
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mShowBorder) {
			drawBorder(canvas);
			drawHandles(canvas);
		}
	}

	private void drawBorder(Canvas canvas) {
		canvas.drawRect(BORDER_INSIDE_RECT.left - BOUNDARY_SIZE,
				BORDER_INSIDE_RECT.top - BOUNDARY_SIZE,
				BORDER_INSIDE_RECT.right + BOUNDARY_SIZE,
				BORDER_INSIDE_RECT.top, BORDER_PAINT);
		canvas.drawRect(BORDER_INSIDE_RECT.left - BOUNDARY_SIZE,
				BORDER_INSIDE_RECT.bottom, BORDER_INSIDE_RECT.right
						+ BOUNDARY_SIZE, BORDER_INSIDE_RECT.bottom
						+ BOUNDARY_SIZE, BORDER_PAINT);
		canvas.drawRect(BORDER_INSIDE_RECT.left - BOUNDARY_SIZE,
				BORDER_INSIDE_RECT.top, BORDER_INSIDE_RECT.left,
				BORDER_INSIDE_RECT.bottom, BORDER_PAINT);
		canvas.drawRect(BORDER_INSIDE_RECT.right, BORDER_INSIDE_RECT.top,
				BORDER_INSIDE_RECT.right + BOUNDARY_SIZE,
				BORDER_INSIDE_RECT.bottom, BORDER_PAINT);
	}

	private void drawHandles(Canvas canvas) {
		computeHandleRects();
		canvas.drawBitmap(HANDLE_VERTICAL, HANDLE_RECTS[0].left,
				HANDLE_RECTS[0].top, null);
		canvas.drawBitmap(HANDLE_VERTICAL, HANDLE_RECTS[1].left,
				HANDLE_RECTS[1].top, null);

		canvas.drawBitmap(HANDLE_HORIZONTAL, HANDLE_RECTS[2].left,
				HANDLE_RECTS[2].top, null);
		canvas.drawBitmap(HANDLE_HORIZONTAL, HANDLE_RECTS[3].left,
				HANDLE_RECTS[3].top, null);

		canvas.drawBitmap(HANDLE_LEFT_DOWN, HANDLE_RECTS[4].left,
				HANDLE_RECTS[4].top, null);
		canvas.drawBitmap(HANDLE_LEFT_DOWN, HANDLE_RECTS[5].left,
				HANDLE_RECTS[5].top, null);

		canvas.drawBitmap(HANDLE_RIGHT_UP, HANDLE_RECTS[6].left,
				HANDLE_RECTS[6].top, null);
		canvas.drawBitmap(HANDLE_RIGHT_UP, HANDLE_RECTS[7].left,
				HANDLE_RECTS[7].top, null);
	}

	private void computeHandleRects() {
		HANDLE_RECTS[0].left = BORDER_INSIDE_RECT.left
				+ (BORDER_INSIDE_RECT.width() - HANDLE_SIZE) / 2;
		HANDLE_RECTS[0].top = BORDER_INSIDE_RECT.top
				- (HANDLE_SIZE + BOUNDARY_SIZE) / 2;

		HANDLE_RECTS[1].left = HANDLE_RECTS[0].left;
		HANDLE_RECTS[1].top = BORDER_INSIDE_RECT.bottom
				- (HANDLE_SIZE - BOUNDARY_SIZE) / 2;

		HANDLE_RECTS[2].left = BORDER_INSIDE_RECT.left
				- (HANDLE_SIZE + BOUNDARY_SIZE) / 2;
		HANDLE_RECTS[2].top = BORDER_INSIDE_RECT.top
				+ (BORDER_INSIDE_RECT.height() - HANDLE_SIZE) / 2;

		HANDLE_RECTS[3].left = BORDER_INSIDE_RECT.right
				- (HANDLE_SIZE - BOUNDARY_SIZE) / 2;
		HANDLE_RECTS[3].top = HANDLE_RECTS[2].top;

		HANDLE_RECTS[4].left = HANDLE_RECTS[2].left;
		HANDLE_RECTS[4].top = HANDLE_RECTS[0].top;

		HANDLE_RECTS[5].left = HANDLE_RECTS[3].left;
		HANDLE_RECTS[5].top = HANDLE_RECTS[1].top;

		HANDLE_RECTS[6].left = HANDLE_RECTS[3].left;
		HANDLE_RECTS[6].top = HANDLE_RECTS[0].top;

		HANDLE_RECTS[7].left = HANDLE_RECTS[2].left;
		HANDLE_RECTS[7].top = HANDLE_RECTS[1].top;

		for (int i = 0; i < HANDLE_RECTS.length; i++) {
			HANDLE_RECTS[i].right = HANDLE_RECTS[i].left + HANDLE_SIZE;
			HANDLE_RECTS[i].bottom = HANDLE_RECTS[i].top + HANDLE_SIZE;
		}
	}

	private void initBorderRect(int left, int top, int right, int bottom) {
		BORDER_INSIDE_RECT.left = left;
		BORDER_INSIDE_RECT.top = top;
		BORDER_INSIDE_RECT.right = right;
		BORDER_INSIDE_RECT.bottom = bottom;
	}

	public View getZoomView() {
		return mZoomView;
	}

	public void setZoomView(View zoomView) {
		if (mZoomView != null) {
			if (mZoomViewCanFocus) {
				requestZoomViewFocus(false);
			}
		}

		mTouchCenterCount = 0;

		mZoomView = zoomView;
		if (mZoomView != null) {
			mShowBorder = true;
			mZoomViewCanFocus = mZoomView instanceof EditText;

			int left = zoomView.getLeft();
			int top = zoomView.getTop();
			int right = zoomView.getRight();
			int bottom = zoomView.getBottom();

			initBorderRect(left, top, right, bottom);

			mZoomView.bringToFront();
			mZoomView.requestLayout();
			invalidate();
		} else {
			mShowBorder = false;
			mZoomViewCanFocus = false;
			BORDER_INSIDE_RECT.left = 0;
			BORDER_INSIDE_RECT.top = 0;
			BORDER_INSIDE_RECT.right = 0;
			BORDER_INSIDE_RECT.bottom = 0;
			invalidate();
		}
		
		if (mOnBorderShowListener != null) {
			mOnBorderShowListener.onBorderShow(mZoomView, mShowBorder,
					BORDER_INSIDE_RECT.left, BORDER_INSIDE_RECT.top,
					BORDER_INSIDE_RECT.right, BORDER_INSIDE_RECT.bottom);
		}
	}

	public boolean isShowBorder() {
		return mShowBorder;
	}

	public void setShowBorder(boolean showBorder) {
		mShowBorder = showBorder;
		invalidate();
		if (mOnBorderShowListener != null) {
			mOnBorderShowListener.onBorderShow(mZoomView, mShowBorder,
					BORDER_INSIDE_RECT.left, BORDER_INSIDE_RECT.top,
					BORDER_INSIDE_RECT.right, BORDER_INSIDE_RECT.bottom);
		}
	}

	public OnZoomListener getOnZoomListener() {
		return mOnZoomListener;
	}

	public void setOnZoomListener(OnZoomListener onZoomListener) {
		mOnZoomListener = onZoomListener;
	}

	public OnBorderShowListener getOnBorderShowListener() {
		return mOnBorderShowListener;
	}

	public void setOnBorderShowListener(
			OnBorderShowListener onBorderShowListener) {
		mOnBorderShowListener = onBorderShowListener;
	}

	public static int getBoundarySize() {
		return BOUNDARY_SIZE;
	}

	public int getHandleSize() {
		return HANDLE_SIZE;
	}

	public Rect getBorderInsideRect() {
		return BORDER_INSIDE_RECT;
	}
	
	public Rect[] getHandleRects() {
		return HANDLE_RECTS;
	}

	public Rect rotateBorderInsideRect() {
		int width = BORDER_INSIDE_RECT.width();
		int height = BORDER_INSIDE_RECT.height();

		float centreX = BORDER_INSIDE_RECT.left + width / 2.0f;
		float centreY = BORDER_INSIDE_RECT.top + height / 2.0f;

		BORDER_INSIDE_RECT.left = Math.round(centreX - height / 2);
		BORDER_INSIDE_RECT.right = Math.round(centreX + height / 2);

		BORDER_INSIDE_RECT.top = Math.round(centreY - width / 2);
		BORDER_INSIDE_RECT.bottom = Math.round(centreY + width / 2);
		return BORDER_INSIDE_RECT;
	}

	public static interface OnZoomListener {
		public void onZoom(View zoomView, int left, int top, int right,
				int bottom);
	}

	public static interface OnBorderShowListener {
		public void onBorderShow(View zoomView, boolean isShow, int left,
				int top, int right, int bottom);
	}
}