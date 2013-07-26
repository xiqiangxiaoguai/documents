package com.tcl.memo.view;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tcl.memo.R;
import com.tcl.memo.util.BitmapUtils;
import com.tcl.memo.util.SettingUtils;

public class PaintView extends View {
	public static final int XFERMODE_PAINT = 1;
	public static final int XFERMODE_ERASE = XFERMODE_PAINT + 1;

	private static final float TOUCH_TOLERANCE = 5.0f;

	private int mXfermode = XFERMODE_PAINT;
	private float mX = -1, mY = -1;
	private Path mPath;
	private LinkedList<MyPath> mPaths = new LinkedList<MyPath>() {

		private static final long serialVersionUID = 6147543460800037941L;

		public boolean add(MyPath object) {
			boolean bool = super.add(object);
			boolean canUndo = !isEmpty();
			if (canUndo != mCanUndo) {
				mCanUndo = canUndo;
				if (mOnUndoRedoStateListener != null) {
					mOnUndoRedoStateListener.OnUndoStateChanged(mCanUndo);
				}
			}
			return bool;
		};

		public MyPath remove(int location) {
			MyPath myPath = super.remove(location);
			boolean canUndo = !isEmpty();
			if (canUndo != mCanUndo) {
				mCanUndo = canUndo;
				if (mOnUndoRedoStateListener != null) {
					mOnUndoRedoStateListener.OnUndoStateChanged(mCanUndo);
				}
			}
			return myPath;
		};

		public void clear() {
			super.clear();
			if (mCanUndo != false) {
				mCanUndo = false;
				if (mOnUndoRedoStateListener != null) {
					mOnUndoRedoStateListener.OnUndoStateChanged(false);
				}
			}
		};
	};
	private Stack<MyPath> mPathBuf = new Stack<MyPath>() {

		private static final long serialVersionUID = -2151699557240405299L;

		public MyPath push(MyPath object) {
			MyPath myPath = super.push(object);
			boolean canRedo = !isEmpty();
			if (canRedo != mCanRedo) {
				mCanRedo = canRedo;
				if (mOnUndoRedoStateListener != null) {
					mOnUndoRedoStateListener.OnRedoStateChanged(mCanRedo);
				}
			}
			return myPath;
		};

		public synchronized MyPath pop() {
			MyPath myPath = super.pop();
			boolean canRedo = !isEmpty();
			if (canRedo != mCanRedo) {
				mCanRedo = canRedo;
				if (mOnUndoRedoStateListener != null) {
					mOnUndoRedoStateListener.OnRedoStateChanged(mCanRedo);
				}
			}
			return myPath;
		};

		public void clear() {
			super.clear();
			if (mCanRedo != false) {
				mCanRedo = false;
				if (mOnUndoRedoStateListener != null) {
					mOnUndoRedoStateListener.OnRedoStateChanged(false);
				}
			}
		};
	};
	private Paint mPaint;
	private Bitmap mBitmap;
	private Bitmap mUndoBitmap;
	private Bitmap mBgBitmap;
	private Canvas mCanvas;
	private RectF mRectF;

	private boolean mCanUndo;
	private boolean mCanRedo;
	private boolean mPausePaint;

	private int mEraserSize = SettingUtils.DEFAULT_ERASER_SIZE;
	private int mStrokeSize = SettingUtils.DEFAULT_PAINT_SIZE;
	private int mStrokeAlpha = SettingUtils.DEFAULT_PAINT_ALPHA;
	private int mStrokeColor = SettingUtils.DEFAULT_PAINT_COLOR;

	private OnUndoRedoStateChangeListener mOnUndoRedoStateListener;

	public PaintView(Context context) {
		this(context, null);
	}

	public PaintView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PaintView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setBackgroundColor(Color.TRANSPARENT);

		mXfermode = XFERMODE_PAINT;

		mPath = new Path();

		mPaint = new Paint();
		mPaint.setColor(mStrokeColor);
		mPaint.setAlpha(mStrokeAlpha);
		mPaint.setStrokeWidth(mStrokeSize);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setPathEffect(new CornerPathEffect(10.0f));

		Resources res = context.getResources();
		mBitmap = Bitmap.createBitmap(
				res.getDimensionPixelSize(R.dimen.note_view_width),
				res.getDimensionPixelSize(R.dimen.note_view_height),
				Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);

		if (mBgBitmap != null) {
			mCanvas.drawBitmap(mBgBitmap, 0, 0, null);
		}

		mRectF = new RectF();
	}

	public void setXfermode(int Xfermode) {
		mXfermode = Xfermode;
		switch (mXfermode) {
		case XFERMODE_ERASE: {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			break;
		}
		default: {
			mPaint.setXfermode(null);
			break;
		}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(mBitmap, 0, 0, null);
		switch (mXfermode) {
		case XFERMODE_ERASE: {
			if (mX > 0 || mY > 0) {
				mPaint.setColor(Color.BLACK);
				mPaint.setAlpha(255);
				mPaint.setStrokeWidth(1);
				setXfermode(PaintView.XFERMODE_PAINT);
				canvas.drawCircle(mX, mY, mEraserSize / 2, mPaint);
			}

			setXfermode(PaintView.XFERMODE_ERASE);
			mPaint.setColor(Color.BLACK);
			mPaint.setAlpha(255);
			mPaint.setStrokeWidth(mEraserSize);
			mCanvas.drawPath(mPath, mPaint);
			break;
		}
		default: {
			setXfermode(PaintView.XFERMODE_PAINT);
			mPaint.setColor(mStrokeColor);
			mPaint.setAlpha(mStrokeAlpha);
			mPaint.setStrokeWidth(mStrokeSize);
			canvas.drawPath(mPath, mPaint);
			break;
		}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mPausePaint) {
			return false;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			touchDown(event.getX(), event.getY());
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			touchMove(event.getX(), event.getY());
			break;
		}
		case MotionEvent.ACTION_UP: {
			touchUp(event.getX(), event.getY());
			break;
		}
		}
		mPath.computeBounds(mRectF, false);
		int stokeWidth = (int) mPaint.getStrokeWidth() + 1;
		invalidate((int) mRectF.left - stokeWidth, (int) mRectF.top
				- stokeWidth, (int) mRectF.right + stokeWidth,
				(int) mRectF.bottom + stokeWidth);
		return true;
	}

	private void touchDown(float x, float y) {
		mPath.reset();
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touchMove(float x, float y) {
		if (Math.abs(x - mX) >= TOUCH_TOLERANCE
				|| Math.abs(y - mY) >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
	}

	private void touchUp(float x, float y) {
		mPath.lineTo(mX, mY);
		mPaths.add(new MyPath(mStrokeColor, mStrokeAlpha, mStrokeSize, mXfermode, new Path(mPath)));
		
		mPaint.setColor(mXfermode == XFERMODE_PAINT ? mStrokeColor : Color.BLACK);
		mPaint.setAlpha(mXfermode == XFERMODE_PAINT ? mStrokeAlpha : 255);
		mPaint.setStrokeWidth(mXfermode == XFERMODE_PAINT ? mStrokeSize : mEraserSize);
		
		mCanvas.drawPath(mPath, mPaint);
		mPath.reset();

		mX = -1;
		mY = -1;
		invalidate();
	}

	public void clearAll(boolean canUndo) {
		mPath.reset();
		mPaths.clear();
		mPathBuf.clear();
		
		mPausePaint = false;
		
		if(mBgBitmap != null) {
			mBgBitmap.recycle();
			mBgBitmap = null;
		}

		if (canUndo) {
			if (mUndoBitmap != null) {
				mUndoBitmap.recycle();
			}
			mUndoBitmap = mBitmap;
		}

		mBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(),
				Bitmap.Config.ARGB_8888);
		mCanvas.setBitmap(mBitmap);

		if (canUndo != mCanUndo) {
			mCanUndo = canUndo;
			if (mOnUndoRedoStateListener != null) {
				mOnUndoRedoStateListener.OnUndoStateChanged(mCanUndo);
			}
			mCanUndo = false;
		}
		
		invalidate();
	}

	public boolean isBlank() {
		if (mBgBitmap == null && mPaths.isEmpty()) {
			return true;
		}
		return false;
	}

	public void setBgBitmap(Bitmap bmp) {
		if (bmp != null) {
			mBgBitmap = bmp;

			if (mBgBitmap != null) {
				mCanvas.drawBitmap(mBgBitmap, 0, 0, null);
			}

			MyPath myPath;
			int xfermode = mXfermode;
			Iterator<MyPath> iterator = mPaths.iterator();
			while (iterator.hasNext()) {
				myPath = iterator.next();
				setXfermode(myPath.mXfermode);
				mPaint.setColor(mXfermode == XFERMODE_PAINT ? myPath.mColor : Color.BLACK);
				mPaint.setAlpha(mXfermode == XFERMODE_PAINT ? myPath.mAlpha : 255);
				mPaint.setStrokeWidth(mXfermode == XFERMODE_PAINT ? myPath.mSize : mEraserSize);
				mCanvas.drawPath(myPath.mPath, mPaint);
			}
			setXfermode(xfermode);
			invalidate();
		}
	}

	// public void clear() {
	// if(mBgBitmap != null) {
	// mBgBitmap.recycle();
	// mBgBitmap = null;
	// }
	//
	// mBitmap.recycle();
	// mBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(),
	// Bitmap.Config.ARGB_8888);
	// mCanvas.setBitmap(mBitmap);
	//
	// mPath.reset();
	// mPaths.clear();
	// mPathBuf.clear();
	// invalidate();
	// }

	public void redo() {
		if (!mPathBuf.isEmpty()) {
			mPaths.add(mPathBuf.pop());
			mBitmap.recycle();
			mBitmap = Bitmap.createBitmap(mBitmap.getWidth(),
					mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBitmap);

			if(mBgBitmap != null) {
				mCanvas.drawBitmap(mBgBitmap, 0, 0, null);
			}

			int xfermode = mXfermode;
			MyPath myPath;
			Iterator<MyPath> iterator = mPaths.iterator();
			while (iterator.hasNext()) {
				myPath = iterator.next();
				setXfermode(myPath.mXfermode);
				mPaint.setColor(mXfermode == XFERMODE_PAINT ? myPath.mColor : Color.BLACK);
				mPaint.setAlpha(mXfermode == XFERMODE_PAINT ? myPath.mAlpha : 255);
				mPaint.setStrokeWidth(mXfermode == XFERMODE_PAINT ? myPath.mSize : mEraserSize);
				mCanvas.drawPath(myPath.mPath, mPaint);
			}
			setXfermode(xfermode);
		} else {
			if (mUndoBitmap != null) {
				mBgBitmap = mUndoBitmap;
			}
			if(mBgBitmap != null) {
				mCanvas.drawBitmap(mBgBitmap, 0, 0, null);
			}
		}
		invalidate();
	}

	public void undo() {
		if (!mPaths.isEmpty()) {
			mBitmap.recycle();
			mBitmap = Bitmap.createBitmap(mBitmap.getWidth(),
					mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
			mCanvas.setBitmap(mBitmap);

			if(mBgBitmap != null) {
				mCanvas.drawBitmap(mBgBitmap, 0, 0, null);
			}

			MyPath myPath = mPaths.remove(mPaths.size() - 1);
			mPathBuf.push(myPath);
			int xfermode = mXfermode;
			Iterator<MyPath> iterator = mPaths.iterator();
			while (iterator.hasNext()) {
				myPath = iterator.next();
				setXfermode(myPath.mXfermode);
				mPaint.setColor(mXfermode == XFERMODE_PAINT ? myPath.mColor : Color.BLACK);
				mPaint.setAlpha(mXfermode == XFERMODE_PAINT ? myPath.mAlpha : 255);
				mPaint.setStrokeWidth(mXfermode == XFERMODE_PAINT ? myPath.mSize : mEraserSize);
				mCanvas.drawPath(myPath.mPath, mPaint);
			}
			setXfermode(xfermode);
		} else {
			if (mUndoBitmap != null) {
				mBgBitmap = mUndoBitmap;
			}
			if(mBgBitmap != null) {
				mCanvas.drawBitmap(mBgBitmap, 0, 0, null);
			}
		}
		invalidate();
	}

	public File exportToDirection(File dir) {
		return BitmapUtils.saveToDirectory(mBitmap, dir, false);
	}

	public void exportToFile(File file) {
		BitmapUtils.saveToFile(mBitmap, file);
	}

	public boolean canUndo() {
		return mCanUndo;
	}

	public boolean canRedo() {
		return mCanRedo;
	}
	
	public int getEraserSize() {
		return mEraserSize;
	}

	public void setEraserSize(int eraserSize) {
		mEraserSize = eraserSize;
	}
	
	public int getStrokeSize() {
		return mStrokeSize;
	}

	public void setStrokeSize(int size) {
		mStrokeSize = size;
	}

	public int getStrokeColor() {
		return mPaint.getColor();
	}

	public void setStrokeColor(int color) {
		mStrokeColor = color;
	}
	
	public int getStrokeAlpha() {
		return mStrokeColor;
	}
	
	public void setStrokeAlpha(int alpha) {
		mStrokeAlpha = alpha;
	}

	public boolean isPausePaint() {
		return mPausePaint;
	}

	public void setPausePaint(boolean pausePaint) {
		mPausePaint = pausePaint;
	}

	public OnUndoRedoStateChangeListener getOnUndoRedoStateListener() {
		return mOnUndoRedoStateListener;
	}

	public void setOnUndoRedoStateListener(
			OnUndoRedoStateChangeListener onUndoRedoStateChangeListener) {
		mOnUndoRedoStateListener = onUndoRedoStateChangeListener;
	}

	private class MyPath {
		public Path mPath;
		public int mColor;
		public int mAlpha;
		public int mSize;
		public int mXfermode;
		

		public MyPath(int color, int alpha, int size, int xfermode, Path path) {
			mColor = color;
			mAlpha = alpha;
			mSize = size;
			mXfermode = xfermode;
			mPath = path;
		}
	}

	public static interface OnUndoRedoStateChangeListener {
		public void OnUndoStateChanged(boolean canUndo);

		public void OnRedoStateChanged(boolean canRedo);
	}
}