package com.tcl.memo.view;

import com.tcl.memo.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class EraserSettingView extends LinearLayout {

	private View mSampleView;
	private SeekBar mEraserSizeSeekBar;

	private OnClickCloseListener mOnClickCloseListener;
	private OnClickClearAllListener mOnClickClearAllListener;
	private OnEraserSizeChangeListener mOnEraserSizeChangeListener;

	public EraserSettingView(Context context) {
		this(context, null);
	}

	public EraserSettingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EraserSettingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		addView(inflater.inflate(R.layout.eraser_setting, null));

		mSampleView = findViewById(R.id.eraser_size_sample);

		mEraserSizeSeekBar = (SeekBar) findViewById(R.id.eraser_size);
		mEraserSizeSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (progress == 0) {
							progress = 1;
						}

						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSampleView
								.getLayoutParams();
						params.width = progress;
						params.height = progress;
						mSampleView.setLayoutParams(params);

						if (mOnEraserSizeChangeListener != null) {
							mOnEraserSizeChangeListener
									.onEraserSizeChange(progress);
						}
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}
				});
		findViewById(R.id.close).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnClickCloseListener != null) {
					mOnClickCloseListener.onClickClose(v);
				}
			}
		});
		findViewById(R.id.clear_all).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnClickClearAllListener != null) {
					mOnClickClearAllListener.onClickClearAll(v);
				}
			}
		});
	}
	
	public int getEraserSize() {
		return mEraserSizeSeekBar.getProgress();
	}
	
	public void setEraserSize(int size) {
		mEraserSizeSeekBar.setProgress(size);
	}

	public OnClickCloseListener getOnClickCloseListener() {
		return mOnClickCloseListener;
	}

	public void setOnClickCloseListener(OnClickCloseListener onClickCloseListener) {
		mOnClickCloseListener = onClickCloseListener;
	}

	public OnClickClearAllListener getOnClickClearAllListener() {
		return mOnClickClearAllListener;
	}

	public void setOnClickClearAllListener(
			OnClickClearAllListener onClickClearAllListener) {
		mOnClickClearAllListener = onClickClearAllListener;
	}

	public OnEraserSizeChangeListener getOnEraserSizeChangeListener() {
		return mOnEraserSizeChangeListener;
	}

	public void setOnEraserSizeChangeListener(
			OnEraserSizeChangeListener onEraserSizeChangeListener) {
		mOnEraserSizeChangeListener = onEraserSizeChangeListener;
	}

	public static interface OnEraserSizeChangeListener {
		void onEraserSizeChange(int size);
	}

	public static interface OnClickCloseListener {
		void onClickClose(View v);
	}

	public static interface OnClickClearAllListener {
		void onClickClearAll(View v);
	}
}