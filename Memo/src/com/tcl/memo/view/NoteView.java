package com.tcl.memo.view;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.tcl.memo.Constants;
import com.tcl.memo.R;
import com.tcl.memo.data.Note;
import com.tcl.memo.util.BitmapUtils;
import com.tcl.memo.util.SettingUtils.FontSetting;

public class NoteView extends RelativeLayout {
	public final static int NOTE_MODE_PAINT = PaintView.XFERMODE_PAINT;
	public final static int NOTE_MODE_ERASE = PaintView.XFERMODE_ERASE;
	public final static int NOTE_MODE_INSERT = NOTE_MODE_ERASE + 1;

	private final static String TAG = NoteView.class.getSimpleName();
	
	private Note mNote;
	private Note.Paint mPaint;
	private Note.Audio mAudio;
	private Note.Group mGroup;

	public List<Note.Text> mTextList = new LinkedList<Note.Text>();
	public List<Note.Image> mImageList = new LinkedList<Note.Image>();

	private LinkedList<CEditText> mEditTextList = new LinkedList<CEditText>();
	private LinkedList<ImageView> mImageViewList = new LinkedList<ImageView>();
	
	private int mFloatBarLeft;
	private int mFloatBarTop;
	private int mFloatBarRight;
	private int mFloatBarBottom;

	private View mFloatBarText;
	private View mFloatBarImage;

	private int mNoteMode;
	private PaintView mPaintView;
	private RelativeLayout mTextLayout;
	private RelativeLayout mImageLayout;
	private RelativeLayout mContentLayout;
	private RelativeLayout mControllerLayout;
	private RelativeLayout mBlankLayout;
	private DottedRectView mDottedRectView;

	private ZoomController mZoomController;
	private ZoomController.OnZoomListener mOnZoomListener;
	private ZoomController.OnBorderShowListener mOnBorderShowListener;
	private OnTouchListener mOnZoomControllerTouchListener;

	private OnAddPageListener mOnAddPageListener;
	private OnDeleteTextListener mOnDeleteTextListener;
	private OnDeleteImageListener mOnDeleteImageListener;
//	private OnDeleteAudioListener mOnDeleteAudioListener;

	public NoteView(Context context) {
		super(context);
		init();
	}

	public NoteView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public NoteView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		initViews();

		initOnZoomListener();
		initOnBorderShowListener();
		initOnZoomControllerTouchListener();

		mZoomController.setOnZoomListener(mOnZoomListener);
		mZoomController.setOnBorderShowListener(mOnBorderShowListener);
		mZoomController.setOnTouchListener(mOnZoomControllerTouchListener);

		mContentLayout.setBackgroundColor(Color.WHITE);
		
		setNoteMode(NOTE_MODE_PAINT);
	}

	private void initViews() {
		Context context = getContext();

		mBlankLayout = new RelativeLayout(context){
			@Override
			public boolean onInterceptTouchEvent(MotionEvent ev) {
				return true;
			}
			
			@Override
			public boolean onTouchEvent(MotionEvent event) {
				return true;
			}
		};
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		addView(mBlankLayout, layoutParams);
		
		mContentLayout = new RelativeLayout(context);
		layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		addView(mContentLayout, layoutParams);

		mImageLayout = new RelativeLayout(context) {
			@Override
			protected void onLayout(boolean changed, int l, int t, int r, int b) {
				ImageView imageView;
				Note.Image noteImage;
				int childCount = mImageViewList.size();
				for (int i = 0; i < childCount; i++) {
					imageView = mImageViewList.get(i);
					noteImage = mImageList.get(i);
					if (imageView != null && imageView.getVisibility() != GONE
							&& imageView != null) {
						imageView.layout(noteImage.mLeft, noteImage.mTop,
								noteImage.mRight, noteImage.mBottom);
					}
				}
			}
		};
		layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mContentLayout.addView(mImageLayout, layoutParams);

		mPaintView = new PaintView(context);
		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mContentLayout.addView(mPaintView, relativeParams);

		mTextLayout = new RelativeLayout(context) {
			@Override
			protected void onLayout(boolean changed, int l, int t, int r, int b) {
				CEditText editText;
				Note.Text noteText;
				int childCount = mEditTextList.size();
				for (int i = 0; i < childCount; i++) {
					editText = mEditTextList.get(i);
					noteText = mTextList.get(i);
					if (editText != null && editText.getVisibility() != GONE
							&& noteText != null) {
						editText.layout(noteText.mLeft, noteText.mTop,
								noteText.mRight, noteText.mBottom);
					}
				}
			}
		};
		layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mContentLayout.addView(mTextLayout, layoutParams);

		mDottedRectView = new DottedRectView(context);
		layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		addView(mDottedRectView, layoutParams);
		
		mControllerLayout = new RelativeLayout(context);
		layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		addView(mControllerLayout, layoutParams);

		mZoomController = new ZoomController(context);
		layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mControllerLayout.addView(mZoomController, layoutParams);
		
		initFloatBarText();
		initFloatBarImage();
	}

	private void initFloatBarText() {
		Context context = getContext();

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		mFloatBarText = inflater.inflate(R.layout.float_bar_text, null);

		mFloatBarText.findViewById(R.id.delete).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						View zoomView = mZoomController.getZoomView();
						if (zoomView instanceof CEditText) {
							int index = mEditTextList.indexOf(zoomView);
							Note.Text text = mTextList.remove(index);
							mEditTextList.remove(index);
							mTextLayout.removeView(zoomView);
							if (mOnDeleteTextListener != null) {
								//B JiangzhouQ 2012/04/26 For Bug 265146
//								if (index >= 0 && index < mEditTextList.size()) {
								if (index >= 0 && index <= mEditTextList.size()) {
								//E JiangzhouQ 2012/04/26 For Bug 265146
									mOnDeleteTextListener
											.onDeleteText(text);
								}
							}
							mZoomController.setZoomView(null);
							mFloatBarText.setVisibility(View.INVISIBLE);
						}
					}
				});

		mFloatBarText.findViewById(R.id.zoom_out)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						View zoomView = mZoomController.getZoomView();
						if (zoomView instanceof CEditText) {
							int index = mEditTextList.indexOf(zoomView);
							Note.Text text = mTextList.get(index);
							text.mSize += 3;
							((CEditText)zoomView).setTextSize(text.mSize);
						}
					}
				});

		mFloatBarText.findViewById(R.id.zoom_in)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						View zoomView = mZoomController.getZoomView();
						if (zoomView instanceof CEditText) {
							int index = mEditTextList.indexOf(zoomView);
							Note.Text text = mTextList.get(index);
							text.mSize -= 3;
							if(text.mSize <= 0) {
								text.mSize = 1;
							}
							((CEditText)zoomView).setTextSize(text.mSize);
						}
					}
				});

		mFloatBarText.setVisibility(View.INVISIBLE);
		mControllerLayout.addView(mFloatBarText, layoutParams);
	}

	private void initFloatBarImage() {
		Context context = getContext();

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		mFloatBarImage = inflater.inflate(R.layout.float_bar_image, null);

		mFloatBarImage.findViewById(R.id.delete).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						View zoomView = mZoomController.getZoomView();
						if (zoomView instanceof ImageView) {
							int index = mImageViewList.indexOf(zoomView);
							Note.Image image = mImageList.remove(index);
							mImageViewList.remove(index);
							mImageLayout.removeView(zoomView);
							if (mOnDeleteImageListener != null) {
								if (index >= 0 && index <= mImageList.size()) {
									mOnDeleteImageListener
											.onDeleteImage(image);
								}
							}
							mZoomController.setZoomView(null);
							mFloatBarImage.setVisibility(View.INVISIBLE);
						}
					}
				});

		mFloatBarImage.findViewById(R.id.left_rotating)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						View zoomView = mZoomController.getZoomView();
						if (zoomView instanceof ImageView) {
							Note.Image image = mImageList.get(mImageViewList.indexOf(zoomView));
							image.mRotate -= 90;
							image.mRotate %= 360;
							
							ImageView imgView = (ImageView)zoomView;
							BitmapDrawable bmpDrawable = (BitmapDrawable)imgView.getDrawable();
							if(bmpDrawable != null) {
								Bitmap bmp = bmpDrawable.getBitmap();
								if(bmp != null) {
									Bitmap newBmp = BitmapUtils.rotate(bmpDrawable.getBitmap(), -90);
									if(newBmp != null) {
										bmp.recycle();
									} else {
										newBmp = bmp;
									}
									imgView.setImageBitmap(newBmp);
								}
							}
							
							Rect rect = mZoomController.rotateBorderInsideRect();
							mZoomController.invalidate();
							mOnZoomListener.onZoom(zoomView, rect.left, rect.top, rect.right, rect.bottom);
						}
					}
				});

		mFloatBarImage.findViewById(R.id.right_rotating)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						View zoomView = mZoomController.getZoomView();
						if (zoomView instanceof ImageView) {
							Note.Image image = mImageList.get(mImageViewList.indexOf(zoomView));
							image.mRotate += 90;
							image.mRotate %= 360;
							
							ImageView imgView = (ImageView)zoomView;
							BitmapDrawable bmpDrawable = (BitmapDrawable)imgView.getDrawable();
							if(bmpDrawable != null) {
								Bitmap bmp = bmpDrawable.getBitmap();
								if(bmp != null) {
									Bitmap newBmp = BitmapUtils.rotate(bmpDrawable.getBitmap(), 90);
									if(newBmp != null) {
										bmp.recycle();
									} else {
										newBmp = bmp;
									}
									imgView.setImageBitmap(newBmp);
								}
							}
							
							Rect rect = mZoomController.rotateBorderInsideRect();
							mZoomController.invalidate();
							mOnZoomListener.onZoom(zoomView, rect.left, rect.top, rect.right, rect.bottom);
						}
					}
				});

		mFloatBarImage.setVisibility(View.INVISIBLE);
		mControllerLayout.addView(mFloatBarImage, layoutParams);
	}

	private void initOnZoomControllerTouchListener() {
		mOnZoomControllerTouchListener = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mNoteMode == NOTE_MODE_PAINT) {
					mZoomController.setZoomView(null);
					return false;
				}
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					int x = Math.round(event.getX());
					int y = Math.round(event.getY());
					
					if(mZoomController.isShowBorder()
							&& mZoomController.getBorderInsideRect().contains(x, y)) {
						return false;
					}
					
					Rect[] rects = mZoomController.getHandleRects();
					for(int i = 0; i < rects.length; i++) {
						if(rects[i].contains(x, y)) {
							return false;
						}
					}

					View child;
					Rect rectF = new Rect();

					int childCount;
					ViewGroup[] parents = new ViewGroup[] { mTextLayout,
							mImageLayout };
					for (int i = 0; i < parents.length; i++) {
						childCount = parents[i].getChildCount();
						for (int j = childCount - 1; j >= 0; j--) {
							child = parents[i].getChildAt(j);
							rectF.left = child.getLeft();
							rectF.top = child.getTop();
							rectF.right = child.getRight();
							rectF.bottom = child.getBottom();

							if (rectF.contains(x, y)) {
								if (child != mZoomController.getZoomView()) {
									mZoomController.setZoomView(child);
								}
								return false;
							}
						}
					}
				}

				return false;
			}
		};
	}

	private void initOnZoomListener() {
		mOnZoomListener = new ZoomController.OnZoomListener() {
			@Override
			public void onZoom(View zoomView, int left, int top, int right,
					int bottom) {
				
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)zoomView.getLayoutParams();
				params.width = right - left;
				params.height = bottom - top;
				zoomView.setLayoutParams(params);
				
				boolean isText = zoomView instanceof CEditText;
				if (isText) {
					Note.Text text = mTextList.get(mEditTextList
							.indexOf(zoomView));
					text.mLeft = left;
					text.mTop = top;
					text.mRight = right;
					text.mBottom = bottom;
					
					zoomView.layout(left, top, right, bottom);
					adjustFloatBarBaseZoomController(mFloatBarText, left,
							top, right, bottom);
				} else {
					Note.Image image = mImageList.get(mImageViewList
							.indexOf(zoomView));
					image.mLeft = left;
					image.mTop = top;
					image.mRight = right;
					image.mBottom = bottom;
					
					zoomView.layout(left, top, right, bottom);
					adjustFloatBarBaseZoomController(mFloatBarImage, left, top,
							right, bottom);
				}
			}
		};
	}

	private void initOnBorderShowListener() {
		mOnBorderShowListener = new ZoomController.OnBorderShowListener() {
			@Override
			public void onBorderShow(View zoomView, boolean isShow, int left,
					int top, int right, int bottom) {
				if(zoomView != null) {
					boolean isText = zoomView instanceof CEditText;
					if (isText) {
						mFloatBarText.setVisibility(isShow ? View.VISIBLE
								: View.INVISIBLE);
						mFloatBarImage.setVisibility(View.INVISIBLE);
						adjustFloatBarBaseZoomController(mFloatBarText, left, top,
								right, bottom);
					} else {
						mFloatBarImage.setVisibility(isShow ? View.VISIBLE
								: View.INVISIBLE);
						mFloatBarText.setVisibility(View.INVISIBLE);
						adjustFloatBarBaseZoomController(mFloatBarImage, left, top,
								right, bottom);
					}
				}
			}
		};
	}

	private void adjustFloatBarBaseZoomController(View floatBar, int left,
			int top, int right, int bottom) {
		int controllerLayoutWidth = mControllerLayout.getWidth();
		int controllerLayoutHeight = mControllerLayout.getHeight();
		int halfBorderSize = (ZoomController.getBoundarySize() + mZoomController
				.getHandleSize()) / 2;

		if (right > controllerLayoutWidth) {
			right = controllerLayoutWidth;
		}

		if (left < 0) {
			left = 0;
		}

		int floatBarWidth = floatBar.getWidth();
		int floatBarHeight = floatBar.getHeight();

		mFloatBarLeft = left + (right - left - floatBarWidth) / 2;
		if (mFloatBarLeft < 0) {
			mFloatBarLeft = 0;
		}

		mFloatBarRight = mFloatBarLeft + floatBarWidth;
		if (mFloatBarRight > controllerLayoutWidth) {
			mFloatBarLeft -= mFloatBarRight - controllerLayoutWidth;
			mFloatBarRight = mFloatBarLeft + floatBarWidth;
		}

		mFloatBarTop = top - halfBorderSize - floatBarHeight;
		mFloatBarBottom = mFloatBarTop + floatBarHeight;

		int marginTop = mFloatBarBottom;
		int marginBottom = controllerLayoutHeight - (bottom + halfBorderSize);

		if (floatBar.getBottom() < top) {
			if (marginTop < floatBarHeight && marginBottom >= floatBarHeight) {
				mFloatBarTop = controllerLayoutHeight - marginBottom;
				mFloatBarBottom = mFloatBarTop + floatBarHeight;
			}
		} else {
			if (marginBottom < floatBarHeight && marginTop >= floatBarHeight) {
				mFloatBarTop = marginTop - floatBarHeight;
				mFloatBarBottom = marginTop;
			} else {
				mFloatBarTop = bottom + halfBorderSize;
				mFloatBarBottom = mFloatBarTop + floatBarHeight;
			}
		}
		
		int borderTop = mZoomController.getBorderInsideRect().top;
		floatBar.findViewById(R.id.direction_up).setVisibility(mFloatBarTop < borderTop ? View.INVISIBLE : View.VISIBLE);
		floatBar.findViewById(R.id.direction_down).setVisibility(mFloatBarTop > borderTop ? View.INVISIBLE : View.VISIBLE);
		floatBar.layout(mFloatBarLeft, mFloatBarTop, mFloatBarRight,
				mFloatBarBottom);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mFloatBarText != null
				&& mFloatBarText.getVisibility() == View.VISIBLE) {
			mFloatBarText.layout(mFloatBarLeft, mFloatBarTop, mFloatBarRight,
					mFloatBarBottom);
		}
		if (mFloatBarImage != null
				&& mFloatBarImage.getVisibility() == View.VISIBLE) {
			mFloatBarImage.layout(mFloatBarLeft, mFloatBarTop, mFloatBarRight,
					mFloatBarBottom);
		}
	}

	public void addText(String text, FontSetting fontSetting, boolean showBorder) {
		Note.Text noteText = new Note.Text();

		int width = 450, height = 300;
		noteText.mLeft = (getWidth() - width) / 2;
		noteText.mTop = mFloatBarImage.getHeight()
				+ ZoomController.getBoundarySize()
				+ mZoomController.getHandleSize() / 2;
		noteText.mRight = noteText.mLeft + width;
		noteText.mBottom = noteText.mTop + height;

		CEditText editText = getCEditText();
		
		mEditTextList.add(editText);
		
		float density = getResources().getDisplayMetrics().density; 
		noteText.mSize = editText.getTextSize() / density;
		mTextList.add(noteText);
		
		if(fontSetting != null) {
			editText.setTextColor(fontSetting.mFontColor);
			editText.setTypeface(fontSetting.mFontName, fontSetting.mFontStyle);
		}
		editText.setText(text);
		editText.setSingleLine(false);
		editText.setPadding(2, 2, 2, 2);
		editText.setBackgroundDrawable(null);
		editText.setGravity(Gravity.LEFT | Gravity.TOP);
		editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

		editText.setLeft(noteText.mLeft);
		editText.setTop(noteText.mTop);
		editText.setRight(noteText.mRight);
		editText.setBottom(noteText.mBottom);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				noteText.mRight - noteText.mLeft, noteText.mBottom
						- noteText.mTop);
		params.leftMargin = noteText.mLeft;
		params.topMargin = noteText.mTop;

		mTextLayout.addView(editText, params);

		mZoomController.setZoomView(editText);
		mZoomController.setShowBorder(showBorder);
	}

	public void addText(Note.Text text, boolean showBorder) {
		if (text != null) {
			mTextList.add(text);
			
			CEditText editText = getCEditText();
			mEditTextList.add(editText);
			
			editText.setTextSize(text.mSize);
			editText.setTextColor(text.mColor);
			editText.setTypeface(Typeface.create(text.mFont, text.mStyle));
			editText.setText(text.mContent);
			editText.setSingleLine(false);
			editText.setPadding(2, 2, 2, 2);
			editText.setBackgroundDrawable(null);
			editText.setGravity(Gravity.LEFT | Gravity.TOP);
			editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

			editText.setLeft(text.mLeft);
			editText.setTop(text.mTop);
			editText.setRight(text.mRight);
			editText.setBottom(text.mBottom);

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					text.mRight - text.mLeft, text.mBottom - text.mTop);
			params.leftMargin = text.mLeft;
			params.topMargin = text.mTop;

			mTextLayout.addView(editText, params);
			
			mZoomController.setZoomView(editText);
			mZoomController.setShowBorder(showBorder);
		}
	}
	
	private CEditText getCEditText() {
		return new CEditText(getContext()) {

			@Override
			public void setTypeface(String fontName, int fontStyle) {
				super.setTypeface(fontName, fontStyle);
				
				int index = mEditTextList.indexOf(this);
				Note.Text text = mTextList.get(index);
				text.mFont = fontName;
				text.mStyle = fontStyle;
			}
			
			@Override
			public void setTextColor(int color) {
				super.setTextColor(color);
				
				int index = mEditTextList.indexOf(this);
				Note.Text text = mTextList.get(index);
				text.mColor = color;
			}
			
			@Override
			protected void onFocusChanged(boolean focused, int direction,
					Rect previouslyFocusedRect) {
				super.onFocusChanged(focused, direction, previouslyFocusedRect);
				if(focused) {
					mDottedRectView.setDottedRect(mZoomController.getBorderInsideRect());
				} else {
					mDottedRectView.setDottedRect(null);
				}
				mDottedRectView.invalidate();
			}
		};
	}

	public void addImage(File file, boolean showBorder) {
		ImageView imageView = null;
		Note.Image image = null;
		try {
			if (file != null && file.exists() && file.canRead()) {
				int width = getWidth();
				int height = getHeight();
		
				if (width == 0) {
					width = getResources().getDimensionPixelSize(
							R.dimen.note_view_width);
				}
				if (height == 0) {
					height = getResources().getDimensionPixelSize(
							R.dimen.note_view_height);
				}
		
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = BitmapUtils.getSampleSize(file.getAbsolutePath(),
						Math.min(width, height));
				Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
		
				int availWidth = width - 2 * ZoomController.getBoundarySize()
						- mZoomController.getHandleSize();
				int availHeight = height - mFloatBarImage.getHeight() - 2
						* ZoomController.getBoundarySize()
						- mZoomController.getHandleSize();
		
				int bmpWidth = bmp.getWidth();
				int bmpHeight = bmp.getHeight();
		
				float scale = Math.min(availWidth / (float) bmpWidth, availHeight
						/ (float) bmpHeight) - 0.1f;
		
				bmpWidth *= scale;
				bmpHeight *= scale;
		
				image = new Note.Image();
				image.mLeft = (width - bmpWidth) / 2;
				image.mTop = mFloatBarImage.getHeight()
						+ ZoomController.getBoundarySize()
						+ mZoomController.getHandleSize() / 2;
				image.mRight = image.mLeft + bmpWidth;
				image.mBottom = image.mTop + bmpHeight;
		
				image.mUri = file.getAbsolutePath();
		
				imageView = new ImageView(getContext());
				imageView.setScaleType(ScaleType.FIT_XY);
				imageView.setImageBitmap(bmp);
		
				imageView.setLeft(image.mLeft);
				imageView.setTop(image.mTop);
				imageView.setRight(image.mRight);
				imageView.setBottom(image.mBottom);
		
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						image.mRight - image.mLeft, image.mBottom
								- image.mTop);
				params.leftMargin = image.mLeft;
				params.topMargin = image.mTop;
				mImageLayout.addView(imageView, params);
		
				mImageViewList.add(imageView);
				mImageList.add(image);
				mZoomController.setZoomView(imageView);
				mZoomController.setShowBorder(showBorder);
			}
			return;
		} catch(Exception e) {
			Log.d(TAG, e.toString(), e);
		} catch(Throwable e) {
			Log.d(TAG, e.toString(), e);
		}
		if(imageView != null) {
			mImageViewList.remove(imageView);
			mImageLayout.removeView(imageView);
		}
		if(image != null) {
			mImageList.remove(image);
		}
		Toast.makeText(getContext(), R.string.add_or_load_image_error,
				Toast.LENGTH_SHORT).show();
	}
	
	public void addImage(Note.Image image, boolean showBorder) {
		ImageView imageView = null;
		try {
			if (image != null && image.mUri != null) {
				File file = new File(image.mUri);
				if (file.exists() && file.canRead()) {
					int width = getWidth();
					int height = getHeight();
		
					if (width == 0) {
						width = getResources().getDimensionPixelSize(
								R.dimen.note_view_width);
					}
					if (height == 0) {
						height = getResources().getDimensionPixelSize(
								R.dimen.note_view_height);
					}
		
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inSampleSize = BitmapUtils.getSampleSize(image.mUri,
							Math.min(width, height));
					
					Bitmap bmp = BitmapFactory.decodeFile(image.mUri, opts);
					Bitmap newBmp = null;
					if(image.mRotate % 360 != 0) {
						newBmp = BitmapUtils.rotate(bmp, image.mRotate);
					}
					if(newBmp != null) {
						bmp.recycle();
					} else {
						newBmp = bmp;
					}
		
					imageView = new ImageView(getContext());
					imageView.setScaleType(ScaleType.FIT_XY);
					imageView.setImageBitmap(newBmp);
		
					imageView.setLeft(image.mLeft);
					imageView.setTop(image.mTop);
					imageView.setRight(image.mRight);
					imageView.setBottom(image.mBottom);
		
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
							image.mRight - image.mLeft, image.mBottom - image.mTop);
					params.leftMargin = image.mLeft;
					params.topMargin = image.mTop;
					mImageLayout.addView(imageView, params);
		
					mImageViewList.add(imageView);
					mImageList.add(image);
					mZoomController.setZoomView(imageView);
					mZoomController.setShowBorder(showBorder);
				}
			}
			return;
		} catch(Exception e) {
			Log.d(TAG, e.toString(), e);
		} catch(Throwable e) {
			Log.d(TAG, e.toString(), e);
		}
		if(imageView != null) {
			mImageViewList.remove(imageView);
			mImageLayout.removeView(imageView);
		}
		if(image != null) {
			mImageList.remove(image);
		}
		Toast.makeText(getContext(), R.string.add_or_load_image_error,
				Toast.LENGTH_SHORT).show();
	}

	private void bringToFront(int noteMode) {
		switch (noteMode) {
		case NOTE_MODE_INSERT:
			mTextLayout.bringToFront();
			mTextLayout.requestLayout();
			break;
		case NOTE_MODE_PAINT:
			mPaintView.bringToFront();
			mPaintView.requestLayout();
			break;
		}
	}

	public int getNoteMode() {
		return mNoteMode;
	}

	public void setNoteMode(int noteMode) {
		if(noteMode != mNoteMode) {
			mNoteMode = noteMode;
			onNoteModeChanged();
		}
	}

	public PaintView getPaintView() {
		return mPaintView;
	}

	private void onNoteModeChanged() {
		bringToFront(mNoteMode);
		mPaintView.setXfermode(mNoteMode);
		if (mNoteMode == NOTE_MODE_PAINT || mNoteMode == NOTE_MODE_ERASE) {
			mPaintView.setPausePaint(false);
			mZoomController.requestZoomViewFocus(false);
			mZoomController.setShowBorder(false);
			mZoomController.setFocusable(false);
			mZoomController.setFocusableInTouchMode(false);
			mZoomController.clearFocus();
			mZoomController.setVisibility(View.INVISIBLE);
		} else {
			mPaintView.setPausePaint(true);
			mZoomController.requestZoomViewFocus(false);
			mZoomController.setShowBorder(false);
			mZoomController.setFocusable(true);
			mZoomController.setFocusableInTouchMode(true);
			mZoomController.setVisibility(View.VISIBLE);
		}
	}

	public File exportThumbnailToDirection(File dir) {
		mContentLayout.setDrawingCacheEnabled(true);
		mContentLayout.buildDrawingCache();
		Bitmap bmp = mContentLayout.getDrawingCache();

		File file = BitmapUtils.saveToDirectory(bmp, dir, true);

		mContentLayout.destroyDrawingCache();
		mContentLayout.setDrawingCacheEnabled(false);
		return file;
	}

	public void exportThumbnailToFile(File file) {
		mContentLayout.setDrawingCacheEnabled(true);
		mContentLayout.buildDrawingCache();
		Bitmap bmp = mContentLayout.getDrawingCache();

		BitmapUtils.saveToFile(bmp, file);

		mContentLayout.destroyDrawingCache();
		mContentLayout.setDrawingCacheEnabled(false);
	}

	public File exportPaintToDirection(File dir) {
		if (!mPaintView.isBlank()) {
			return mPaintView.exportToDirection(dir);
		}
		return null;
	}

	public void exportPaintToFile(File file) {
		if (!mPaintView.isBlank()) {
			mPaintView.exportToFile(file);
		}
	}
	
	public void clearAll() {
		mPaint = null;
		mAudio = null;
		mGroup = null;
		mNote = null;
		
		mPaintView.clearAll(false);
		mZoomController.setZoomView(null);
		mZoomController.setShowBorder(false);
		
		mFloatBarText.setVisibility(View.INVISIBLE);
		mFloatBarImage.setVisibility(View.INVISIBLE);
		
		mTextList.clear();
		mEditTextList.clear();
		mTextLayout.removeAllViews();
		
		mImageList.clear();
		mImageViewList.clear();
		mImageLayout.removeAllViews();
	}
	
	public boolean isBlank() {
		return mPaintView.isBlank() && mTextList.isEmpty()
				&& mImageList.isEmpty() && mAudio == null;
	}
	
	public void setNoteBg(int resId) {
		if(resId == 0) {
			resId = Constants.DEFAULT_NOTE_BG_RES_ID;
		}
		mContentLayout.setBackgroundResource(resId);
	}
	
	public Note getNote() {
		if (mNote == null) {
			mNote = new Note();
			mNote.mCreateTime = System.currentTimeMillis();
		}
		mNote.mModifyTime = System.currentTimeMillis();
		mNote.mHasImage = !mImageList.isEmpty();
		mNote.mHasText = !mTextList.isEmpty();
		mNote.mHasPaint = mPaint != null;
		mNote.mHasAudio = mAudio != null;
		return mNote;
	}

	public void setNote(Note note) {
		mNote = note;
	}

	public void setPaint(Note.Paint paint) {
		mPaint = paint;
		if (mPaint != null) {
			Bitmap bmp = BitmapFactory.decodeFile(paint.mUri);
			mPaintView.setBgBitmap(bmp);
		}
	}
	
	public Note.Paint getPaint() {
		if (mPaint == null && !mPaintView.isBlank()) {
			mPaint = new Note.Paint();
		}
		return mPaint;
	}

	public Note.Audio getAudio() {
		return mAudio;
	}

	public void setAudio(Note.Audio audio) {
		mAudio = audio;
	}

	public Note.Group getNoteGroup() {
		return mGroup;
	}

	public void setGroup(Note.Group group) {
		mGroup = group;
	}

	public List<Note.Text> getTextList() {
		Typeface typeface;
		CEditText editText;
		Note.Text noteText;
		float density = getResources().getDisplayMetrics().density;
		int size = mTextList.size();
		for (int i = 0; i < size; i++) {
			editText = mEditTextList.get(i);
			noteText = mTextList.get(i);
			typeface = editText.getTypeface();
			if (typeface != null) {
				noteText.mStyle = typeface.getStyle();
			}
			noteText.mLayer = mTextLayout.indexOfChild(editText);
			noteText.mColor = editText.getCurrentTextColor();
			
			noteText.mSize = editText.getTextSize() / density;
			noteText.mContent = editText.getText().toString();
		}
		return mTextList;
	}

	public void setTouchable(boolean touchable) {
		if(touchable) {
			mBlankLayout.setFocusable(false);
			mBlankLayout.setFocusableInTouchMode(false);
			mContentLayout.bringToFront();
			mControllerLayout.bringToFront();
		} else {
			mBlankLayout.setFocusable(true);
			mBlankLayout.setFocusableInTouchMode(true);
			mBlankLayout.bringToFront();
		}
	}

	public void setOnDeleteTextListener(
			OnDeleteTextListener onDeleteTextListener) {
		mOnDeleteTextListener = onDeleteTextListener;
	}

	public void setOnDeleteImageListener(
			OnDeleteImageListener onDeleteImageListener) {
		mOnDeleteImageListener = onDeleteImageListener;
	}

//	public void setOnDeleteAudioListener(
//			OnDeleteAudioListener onDeleteAudioListener) {
//		mOnDeleteAudioListener = onDeleteAudioListener;
//	}

	public OnAddPageListener getOnAddPageListener() {
		return mOnAddPageListener;
	}

	public void setOnAddPageListener(OnAddPageListener onAddPageListener) {
		mOnAddPageListener = onAddPageListener;
	}

	public List<Note.Image> getImageList() {
		return mImageList;
	}
	
	public ZoomController getZoomController() {
		return mZoomController;
	}

	public void setZoomController(ZoomController zoomController) {
		mZoomController = zoomController;
	}

	public static interface OnDeleteTextListener {
		public void onDeleteText(Note.Text text);
	}

	public static interface OnDeleteImageListener {
		public void onDeleteImage(Note.Image image);
	}

	public static interface OnDeleteAudioListener {
		public void onDeleteAudio(Note.Audio audio);
	}
	
	public static interface OnAddPageListener {
		public void onAddPage();
	}
}