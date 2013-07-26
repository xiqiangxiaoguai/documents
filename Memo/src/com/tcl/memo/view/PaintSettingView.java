package com.tcl.memo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.tcl.memo.R;

public class PaintSettingView extends LinearLayout {

	private OnSizeChangeListener mOnSizeChangeListener;
	private OnAlphaChangeListener mOnAlphaChangeListener;
	private OnColorChangeListener mOnColorChangeListener;
	private OnClickCloseListener mOnClickCloseListener;

	private PreviewCircle sampleView;

	private int CurrentColor;

	private PreviewCircle sampleView2;

	private PaintPreview paintPreview;

	private int LastColorViewTag = -1;
	
	private String colorpicker_select = null;
	
	private String COLOR_PICKER_TAG = "colorpicker";
	
	private int EXIT_FLAG;
	private final int EXIT_C0LORS = 1;
	private final int EXIT_COLORPICKER = 2;

	private ImageView V1;
	private ImageView V2;
	private ImageView V3;
	private ImageView V4;
	private ImageView V5;
	private ImageView V6;
	private ImageView V7;
	private ImageView V8;
	private ImageView V9;
	private ImageView V10;
	private ImageView V11;
	private ImageView V12;
	private ImageView V13;
	private ImageView V14;

	private ImageView V1_1;
	private ImageView V2_1;
	private ImageView V3_1;
	private ImageView V4_1;
	private ImageView V5_1;
	private ImageView V6_1;
	private ImageView V7_1;
	private ImageView V8_1;
	private ImageView V9_1;
	private ImageView V10_1;
	private ImageView V11_1;
	private ImageView V12_1;
	private ImageView V13_1;
	private ImageView V14_1;
	private ColorPicker colorPicker;

	public PaintSettingView(Context context) {
		this(context, null);
	}

	public PaintSettingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PaintSettingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		addView(inflater.inflate(R.layout.paintbrush_settings, null));

		ScrollView scrollview = (ScrollView)findViewById(R.id.paintsetting_scroll);
		scrollview.setOverScrollMode(View.OVER_SCROLL_NEVER);

		colorPicker = (ColorPicker) findViewById(R.id.colorpicker_paint);
		colorPicker
				.SetOnColorChangedListener(new ColorPicker.OnColorChangedListener() {

					@Override
					public void onColorChanged(int color) {

						V14.setBackgroundColor(color);
						sampleView.setColor(color);
						sampleView2.setColor(color);
						paintPreview.setColor(color);
						if (LastColorViewTag != -1) {
						ImageView iv = (ImageView) findViewWithTag(LastColorViewTag);
						iv.setVisibility(INVISIBLE);
						}
						V14_1.setVisibility(VISIBLE);
						colorpicker_select = COLOR_PICKER_TAG;
						LastColorViewTag = -1;

						if (mOnColorChangeListener != null) {
							mOnColorChangeListener.onColorChange(color);

						}
						
						EXIT_FLAG = EXIT_COLORPICKER;
					}
				});

		findViewById(R.id.closepaitsetting).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mOnClickCloseListener != null) {
							mOnClickCloseListener.onClickClose(v);
						}
					}
				});

		sampleView = (PreviewCircle) findViewById(R.id.paint1_sample);

		sampleView2 = (PreviewCircle) findViewById(R.id.paint2_sample);

		paintPreview = (PaintPreview) findViewById(R.id.preview);

		((SeekBar) findViewById(R.id.paint1))
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (progress == 0) {
							progress = 1;
						}

						sampleView.setR(progress/2);
						paintPreview.setBond(progress);

						if (mOnSizeChangeListener != null) {
							mOnSizeChangeListener.onSizeChange(progress);
						}
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}

				});

		((SeekBar) findViewById(R.id.paint2))
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (progress == 0) {
							progress = 1;
						}

						sampleView2.setAlpha(progress);

						paintPreview.setAlpha(progress);

						if (mOnAlphaChangeListener != null) {
							mOnAlphaChangeListener.onAlphaChange(progress);
						}
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}

				});

		V1 = (ImageView) findViewById(R.id.colorview1);
		V2 = (ImageView) findViewById(R.id.colorview2);
		V3 = (ImageView) findViewById(R.id.colorview3);
		V4 = (ImageView) findViewById(R.id.colorview4);
		V5 = (ImageView) findViewById(R.id.colorview5);
		V6 = (ImageView) findViewById(R.id.colorview6);
		V7 = (ImageView) findViewById(R.id.colorview7);
		V8 = (ImageView) findViewById(R.id.colorview8);
		V9 = (ImageView) findViewById(R.id.colorview9);
		V10 = (ImageView) findViewById(R.id.colorview10);
		V11 = (ImageView) findViewById(R.id.colorview11);
		V12 = (ImageView) findViewById(R.id.colorview12);
		V13 = (ImageView) findViewById(R.id.colorview13);
		V14 = (ImageView) findViewById(R.id.colorview14);

		V1_1 = (ImageView) findViewById(R.id.colorview1_1);
		V2_1 = (ImageView) findViewById(R.id.colorview2_1);
		V3_1 = (ImageView) findViewById(R.id.colorview3_1);
		V4_1 = (ImageView) findViewById(R.id.colorview4_1);
		V5_1 = (ImageView) findViewById(R.id.colorview5_1);
		V6_1 = (ImageView) findViewById(R.id.colorview6_1);
		V7_1 = (ImageView) findViewById(R.id.colorview7_1);
		V8_1 = (ImageView) findViewById(R.id.colorview8_1);
		V9_1 = (ImageView) findViewById(R.id.colorview9_1);
		V10_1 = (ImageView) findViewById(R.id.colorview10_1);
		V11_1 = (ImageView) findViewById(R.id.colorview11_1);
		V12_1 = (ImageView) findViewById(R.id.colorview12_1);
		V13_1 = (ImageView) findViewById(R.id.colorview13_1);
		V14_1 = (ImageView) findViewById(R.id.colorview14_1);
		V1_1.setTag(0xffffffff);
		V2_1.setTag(0xffffff00);
		V3_1.setTag(0xffff0000);
		V4_1.setTag(0xFFFF1493);
		V5_1.setTag(0xFFDA70D6);
		V6_1.setTag(0xFF1E90FF);
		V7_1.setTag(0xFF8470FF);
		V8_1.setTag(0xFF3CB371);
		V9_1.setTag(0xFF008000);
		V10_1.setTag(0xFFD3D3D3);
		V11_1.setTag(0xFF808080);
		V12_1.setTag(0xFF4B0082);
		V13_1.setTag(0xFF000000);
		V14_1.setTag(COLOR_PICKER_TAG);

		V1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickView(v);
			}
		});

		V2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickView(v);
			}
		});

		V3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickView(v);
			}
		});

		V4.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickView(v);
			}
		});

		V5.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickView(v);
			}
		});

		V6.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickView(v);
			}
		});

		V7.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickView(v);
			}
		});

		V8.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickView(v);
			}
		});

		V9.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickView(v);
			}
		});

		V10.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickView(v);
			}
		});

		V11.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickView(v);
			}
		});

		V12.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickView(v);
			}
		});

		V13.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickView(v);
			}
		});

	}

	public OnSizeChangeListener getOnSizeChangeListener() {
		return mOnSizeChangeListener;
	}

	public void setOnSizeChangeListener(
			OnSizeChangeListener onSizeChangeListener) {
		mOnSizeChangeListener = onSizeChangeListener;
	}

	public OnAlphaChangeListener getOnAlphaChangeListener() {
		return mOnAlphaChangeListener;
	}

	public void setOnAlphaChangeListener(
			OnAlphaChangeListener onAlphaChangeListener) {
		mOnAlphaChangeListener = onAlphaChangeListener;
	}

	public OnColorChangeListener getOnColorChangeListener() {
		return mOnColorChangeListener;
	}

	public void setOnColorChangeListener(
			OnColorChangeListener onColorChangeListener) {
		mOnColorChangeListener = onColorChangeListener;
	}

	public OnClickCloseListener getOnClickCloseListener() {
		return mOnClickCloseListener;
	}

	public void setOnClickCloseListener(
			OnClickCloseListener onClickCloseListener) {
		mOnClickCloseListener = onClickCloseListener;
	}

	public static interface OnSizeChangeListener {
		void onSizeChange(int size);
	}

	public static interface OnAlphaChangeListener {
		void onAlphaChange(int alpha);
	}

	public static interface OnColorChangeListener {
		void onColorChange(int color);
	}

	public static interface OnClickCloseListener {
		void onClickClose(View v);
	}

	public void onClickView(View view) {

		ImageView V = (ImageView) view;
		
		EXIT_FLAG = EXIT_C0LORS;

		if (colorpicker_select != null) {
			V14_1.setVisibility(INVISIBLE);
			colorpicker_select = null;
		}
		else if (LastColorViewTag != -1) {
			ImageView iv = (ImageView) findViewWithTag(LastColorViewTag);

			iv.setVisibility(INVISIBLE);
		}
		switch (view.getId()) {
		case R.id.colorview1:

			V1_1.setVisibility(View.VISIBLE);
			paintPreview.setColor(0xffffffff);
			sampleView.setColor(0xffffffff);
			sampleView2.setColor(0xffffffff);
			CurrentColor = 0xffffffff;
			LastColorViewTag = 0xffffffff;
			break;
		case R.id.colorview2:
			V2_1.setVisibility(View.VISIBLE);
			paintPreview.setColor(0xffffff00);
			sampleView.setColor(0xffffff00);
			sampleView2.setColor(0xffffff00);
			CurrentColor = 0xffffff00;
			LastColorViewTag = 0xffffff00;
			break;
		case R.id.colorview3:

			V3_1.setVisibility(View.VISIBLE);

			paintPreview.setColor(0xffff0000);
			sampleView.setColor(0xffff0000);
			sampleView2.setColor(0xffff0000);
			CurrentColor = 0xffff0000;
			LastColorViewTag = 0xffff0000;

			break;
		case R.id.colorview4:

			V4_1.setVisibility(View.VISIBLE);

			paintPreview.setColor(0xFFFF1493);
			sampleView.setColor(0xFFFF1493);
			sampleView2.setColor(0xFFFF1493);
			CurrentColor = 0xFFFF1493;
			LastColorViewTag = 0xFFFF1493;

			break;
		case R.id.colorview5:

			V5_1.setVisibility(View.VISIBLE);

			paintPreview.setColor(0xFFDA70D6);
			sampleView.setColor(0xFFDA70D6);
			sampleView2.setColor(0xFFDA70D6);
			CurrentColor = 0xFFDA70D6;
			LastColorViewTag = 0xFFDA70D6;

			break;
		case R.id.colorview6:

			V6_1.setVisibility(View.VISIBLE);

			paintPreview.setColor(0xFF1E90FF);
			sampleView.setColor(0xFF1E90FF);
			sampleView2.setColor(0xFF1E90FF);
			CurrentColor = 0xFF1E90FF;
			LastColorViewTag = 0xFF1E90FF;

			break;
		case R.id.colorview7:

			V7_1.setVisibility(View.VISIBLE);

			paintPreview.setColor(0xFF8470FF);
			sampleView.setColor(0xFF8470FF);
			sampleView2.setColor(0xFF8470FF);
			CurrentColor = 0xFF8470FF;
			LastColorViewTag = 0xFF8470FF;

			break;
		case R.id.colorview8:

			V8_1.setVisibility(View.VISIBLE);

			paintPreview.setColor(0xFF3CB371);
			sampleView.setColor(0xFF3CB371);
			sampleView2.setColor(0xFF3CB371);
			CurrentColor = 0xFF3CB371;
			LastColorViewTag = 0xFF3CB371;

			break;

		case R.id.colorview9:

			V9_1.setVisibility(View.VISIBLE);

			paintPreview.setColor(0xFF008000);
			sampleView.setColor(0xFF008000);
			sampleView2.setColor(0xFF008000);
			CurrentColor = 0xFF008000;
			LastColorViewTag = 0xFF008000;

			break;
		case R.id.colorview10:

			V10_1.setVisibility(View.VISIBLE);

			paintPreview.setColor(0xFFD3D3D3);
			sampleView.setColor(0xFFD3D3D3);
			sampleView2.setColor(0xFFD3D3D3);
			CurrentColor = 0xFFD3D3D3;
			LastColorViewTag = 0xFFD3D3D3;

			break;
		case R.id.colorview11:

			V11_1.setVisibility(View.VISIBLE);

			paintPreview.setColor(0xFF808080);
			sampleView.setColor(0xFF808080);
			sampleView2.setColor(0xFF808080);
			CurrentColor = 0xFF808080;
			LastColorViewTag = 0xFF808080;

			break;
		case R.id.colorview12:

			V12_1.setVisibility(View.VISIBLE);
			paintPreview.setColor(0xFF4B0082);
			sampleView.setColor(0xFF4B0082);
			sampleView2.setColor(0xFF4B0082);
			CurrentColor = 0xFF4B0082;
			LastColorViewTag = 0xFF4B0082;

			break;
		case R.id.colorview13:

			V13_1.setVisibility(View.VISIBLE);

			paintPreview.setColor(0xFF000000);
			sampleView.setColor(0xFF000000);
			sampleView2.setColor(0xFF000000);
			CurrentColor = 0xFF000000;
			LastColorViewTag = 0xFF000000;

			break;
		}

		if (mOnColorChangeListener != null) {
			mOnColorChangeListener.onColorChange(CurrentColor);

		}

	}

	public ColorPicker GetColorPickerView() {
		return colorPicker;
	}
	
	public void adjustScrollViewSize(int width, int height) {
		ScrollView scrollView = (ScrollView)findViewById(R.id.paintsetting_scroll);
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)scrollView.getLayoutParams();
		params.width = width;
		params.height = height;
		scrollView.setLayoutParams(params);
	}
	
	public int getPaintSize() {
		return ((SeekBar) findViewById(R.id.paint1)).getProgress();
	}
	
	public void setPaintSize(int size) {
		((SeekBar) findViewById(R.id.paint1)).setProgress(size);
	}
	
	public int getPaintAlpha() {
		return ((SeekBar) findViewById(R.id.paint2)).getProgress();
	}
	
	public void setPaintAlpha(int alpha) {
		((SeekBar) findViewById(R.id.paint2)).setProgress(alpha);
	}

	public void setPaintColor(int color, float x, float y,  int Flag) {
		
		CurrentColor = color;
		
		if (Flag == EXIT_C0LORS) {
			ImageView iv = (ImageView) findViewWithTag(color);
			if (iv == null) {
				colorPicker.SetSelectCirclePosition(x, y);
			}	
			else {
			sampleView.setColor(color);
			sampleView2.setColor(color);
			paintPreview.setColor(color);
			
			colorPicker.SetCirclePosition(x, y);
			iv.setVisibility(VISIBLE);
			LastColorViewTag = color;
			V14.setBackgroundColor(colorPicker.GetColorByXY(x, y));
			EXIT_FLAG = EXIT_C0LORS;
			}
		}
		
		else {
			colorPicker.SetSelectCirclePosition(x,y);
			
			EXIT_FLAG = EXIT_COLORPICKER;
		}

	}
	
	public int getPaintColor() {
		return CurrentColor;
	}
	
	public int getPaintColorX() {
		return Math.round(colorPicker.mCurrentX);
	}
	
	public int getPaintColorY() {
		return Math.round(colorPicker.mCurrentY);
	}
	
	public int getExitFlag() {
		return EXIT_FLAG;
	}
	
	public void setExitFlag(int flag) {
		EXIT_FLAG = flag;
	}
}