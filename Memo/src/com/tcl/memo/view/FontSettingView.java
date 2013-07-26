package com.tcl.memo.view;

import com.tcl.memo.R;
import com.tcl.memo.view.PaintSettingView.OnAlphaChangeListener;
import com.tcl.memo.view.PaintSettingView.OnClickCloseListener;
import com.tcl.memo.view.PaintSettingView.OnColorChangeListener;
import com.tcl.memo.view.PaintSettingView.OnSizeChangeListener;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class FontSettingView extends LinearLayout {

	private OnFontChangeListener mOnFontChangeListener;
	private OnColorChangeListener mOnColorChangeListener;
	private OnClickCloseListener mOnClickCloseListener;

	private RadioButton RB_font1;
	private RadioButton RB_font2;
	private RadioButton RB_font3;
	private RadioButton RB_font4;

	private RadioButton RB_style1;
	private RadioButton RB_style2;
	private RadioButton RB_style3;
	private RadioButton RB_style4;

	private String COLOR_PICKER_TAG = "colorpicker";
	private String colorpicker_select = null;

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

	private TextView PreviewText;

	private String LastFontViewTag = null;
	private int LastFontStyleViewTag = 0;
	private int LastColorViewTag = -1;

	private String mCurrentFont;
	private int mCurrentStyle;
	private int mCurrentColor;
	private ColorPicker colorPicker;
	
	private int EXIT_FLAG;
	private final int EXIT_C0LORS = 1;
	private final int EXIT_COLORPICKER = 2;

	final String DEFAULT_FONT = "default";

	public FontSettingView(Context context) {
		this(context, null);
	}

	public FontSettingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FontSettingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		addView(inflater.inflate(R.layout.fontsetting, null));
		
		ScrollView scrollview = (ScrollView)findViewById(R.id.fontsetting_scroll);
		scrollview.setOverScrollMode(View.OVER_SCROLL_NEVER);

		PreviewText = (TextView) findViewById(R.id.preview_text);

		PreviewText.setTextSize(20);

		RB_font1 = (RadioButton) findViewById(R.id.font1);
		RB_font2 = (RadioButton) findViewById(R.id.font2);
		RB_font3 = (RadioButton) findViewById(R.id.font3);
		RB_font4 = (RadioButton) findViewById(R.id.font4);

		RB_style1 = (RadioButton) findViewById(R.id.style1);
		RB_style2 = (RadioButton) findViewById(R.id.style2);
		RB_style3 = (RadioButton) findViewById(R.id.style3);
		RB_style4 = (RadioButton) findViewById(R.id.style4);

		RB_font1.setTag(DEFAULT_FONT);
		RB_font2.setTag("sans-serif");
		RB_font3.setTag("monospace");
		RB_font4.setTag("serif");

		RB_style1.setTag(Typeface.NORMAL);
		RB_style2.setTag(Typeface.BOLD);
		RB_style3.setTag(Typeface.ITALIC);
		RB_style4.setTag(Typeface.BOLD_ITALIC);

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

		colorPicker = (ColorPicker) findViewById(R.id.colorpicker);
		colorPicker
				.SetOnColorChangedListener(new ColorPicker.OnColorChangedListener() {

					@Override
					public void onColorChanged(int color) {
						mCurrentColor = color;
						V14.setBackgroundColor(color);
						PreviewText.setTextColor(color);
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

		RB_font1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onFontClick(v);
			}
		});

		RB_font2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onFontClick(v);
			}
		});

		RB_font3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onFontClick(v);
			}
		});

		RB_font4.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onFontClick(v);
			}
		});

		RB_style1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onFontStyleClick(v);
			}
		});

		RB_style2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onFontStyleClick(v);
			}
		});

		RB_style3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onFontStyleClick(v);
			}
		});

		RB_style4.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onFontStyleClick(v);
			}
		});

		findViewById(R.id.closefontsetting).setOnClickListener(
			new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mOnClickCloseListener != null) {
						mOnClickCloseListener.onClickClose(v);
					}
				}
			});
		
	}

	public void onFontClick(View view) {

		RadioButton B = (RadioButton) view;

		RadioButton rb = (RadioButton) findViewWithTag(LastFontViewTag);
		rb.setChecked(false);

		B.setChecked(true);
		if (view.getTag() == DEFAULT_FONT) {
			Typeface tf = Typeface.create((String) null, mCurrentStyle);
			PreviewText.setTypeface(tf);
			mCurrentFont = (String) null;
			LastFontViewTag = DEFAULT_FONT;
		}

		else {
			Typeface tf = Typeface
					.create((String) view.getTag(), mCurrentStyle);
			PreviewText.setTypeface(tf);
			mCurrentFont = (String) view.getTag();
			LastFontViewTag = (String) view.getTag();
		}
		if (mOnFontChangeListener != null) {
			mOnFontChangeListener.onFontChange(mCurrentFont, mCurrentStyle);
		}

	}

	public void onFontStyleClick(View view) {
		RadioButton B = (RadioButton) view;

		if (LastFontStyleViewTag != -1) {
			RadioButton rb = (RadioButton) findViewWithTag(LastFontStyleViewTag);

			rb.setChecked(false);
		}

		B.setChecked(true);
		Typeface tf = Typeface.create(mCurrentFont, (Integer) view.getTag());
		PreviewText.setTypeface(tf);
		mCurrentStyle = (Integer) view.getTag();
		LastFontStyleViewTag = (Integer) view.getTag();

		if (mOnFontChangeListener != null) {
			mOnFontChangeListener.onFontChange(mCurrentFont, mCurrentStyle);
		}

	}

	public void onClickView(View view) {

		ImageView V = (ImageView) view;
		
		EXIT_FLAG = EXIT_C0LORS;

		if (colorpicker_select != null) {
			V14_1.setVisibility(INVISIBLE);
			colorpicker_select = null;
		} else if (LastColorViewTag != -1) {
			ImageView iv = (ImageView) findViewWithTag(LastColorViewTag);

			iv.setVisibility(INVISIBLE);
		}

		switch (view.getId()) {
		case R.id.colorview1:

			V1_1.setVisibility(View.VISIBLE);
			PreviewText.setTextColor(0xffffffff);
			mCurrentColor = 0xffffffff;
			LastColorViewTag = 0xffffffff;

			break;
		case R.id.colorview2:
			V2_1.setVisibility(View.VISIBLE);
			PreviewText.setTextColor(0xffffff00);
			mCurrentColor = 0xffffff00;
			LastColorViewTag = 0xffffff00;

			break;
		case R.id.colorview3:

			V3_1.setVisibility(View.VISIBLE);

			PreviewText.setTextColor(0xffff0000);
			mCurrentColor = 0xffff0000;
			LastColorViewTag = 0xffff0000;

			break;
		case R.id.colorview4:

			V4_1.setVisibility(View.VISIBLE);

			PreviewText.setTextColor(0xFFFF1493);
			mCurrentColor = 0xFFFF1493;
			LastColorViewTag = 0xFFFF1493;

			break;
		case R.id.colorview5:

			V5_1.setVisibility(View.VISIBLE);

			PreviewText.setTextColor(0xFFDA70D6);
			mCurrentColor = 0xFFDA70D6;
			LastColorViewTag = 0xFFDA70D6;

			break;
		case R.id.colorview6:

			V6_1.setVisibility(View.VISIBLE);

			PreviewText.setTextColor(0xFF1E90FF);
			mCurrentColor = 0xFF1E90FF;
			LastColorViewTag = 0xFF1E90FF;

			break;
		case R.id.colorview7:

			V7_1.setVisibility(View.VISIBLE);

			PreviewText.setTextColor(0xFF8470FF);
			mCurrentColor = 0xFF8470FF;
			LastColorViewTag = 0xFF8470FF;

			break;
		case R.id.colorview8:

			V8_1.setVisibility(View.VISIBLE);

			PreviewText.setTextColor(0xFF3CB371);
			mCurrentColor = 0xFF3CB371;
			LastColorViewTag = 0xFF3CB371;

			break;

		case R.id.colorview9:

			V9_1.setVisibility(View.VISIBLE);

			PreviewText.setTextColor(0xFF008000);
			mCurrentColor = 0xFF008000;
			LastColorViewTag = 0xFF008000;

			break;
		case R.id.colorview10:

			V10_1.setVisibility(View.VISIBLE);

			PreviewText.setTextColor(0xFFD3D3D3);
			mCurrentColor = 0xFFD3D3D3;
			LastColorViewTag = 0xFFD3D3D3;

			break;
		case R.id.colorview11:

			V11_1.setVisibility(View.VISIBLE);

			PreviewText.setTextColor(0xFF808080);
			mCurrentColor = 0xFF808080;
			LastColorViewTag = 0xFF808080;

			break;
		case R.id.colorview12:

			V12_1.setVisibility(View.VISIBLE);
			PreviewText.setTextColor(0xFF4B0082);
			mCurrentColor = 0xFF4B0082;
			LastColorViewTag = 0xFF4B0082;

			break;
		case R.id.colorview13:

			V13_1.setVisibility(View.VISIBLE);

			PreviewText.setTextColor(0xFF000000);
			mCurrentColor = 0xFF000000;
			LastColorViewTag = 0xFF000000;

			break;
		}

		if (mOnColorChangeListener != null) {
			mOnColorChangeListener.onColorChange(mCurrentColor);
		}

	}

	public OnFontChangeListener getOnSizeChangeListener() {
		return mOnFontChangeListener;
	}

	public void setOnFontChangeListener(
			OnFontChangeListener onFontChangeListener) {
		mOnFontChangeListener = onFontChangeListener;
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

	public static interface OnFontChangeListener {
		void onFontChange(String fontName, int fontStyle);
	}
	
	public static interface OnColorChangeListener {
		void onColorChange(int fontColor);
	}

	public static interface OnClickCloseListener {
		void onClickClose(View v);
	}

	public void setFontColor(int color, float x, float y, int Flag) {
		
		mCurrentColor = color;
		
		if (Flag == EXIT_C0LORS)
		{
			ImageView iv = (ImageView) findViewWithTag(color);
			if (iv == null) {
				colorPicker.SetSelectCirclePosition(x, y);
			}
			else {
			iv.setVisibility(VISIBLE);
			PreviewText.setTextColor(color);
			LastColorViewTag = color;
			
			colorPicker.SetCirclePosition(x, y);
			V14.setBackgroundColor(colorPicker.GetColorByXY(x,y));
			EXIT_FLAG = EXIT_C0LORS;
			}
			
		} else if (Flag == EXIT_COLORPICKER){
			colorPicker.SetSelectCirclePosition(x, y);
			
			EXIT_FLAG = EXIT_COLORPICKER;
		}

	}

	public void setFontNameAndStyle(String name, int style) {
		if (name == null)

		{
			RB_font1.setChecked(true);
			LastFontViewTag = DEFAULT_FONT;
			Typeface tf = Typeface.create((String) null, style);
			PreviewText.setTypeface(tf);
			mCurrentFont = (String) null;
		} else {
			RadioButton rb_font = (RadioButton) findViewWithTag(name);
			rb_font.setChecked(true);
			LastFontViewTag = name;
			Typeface tf = Typeface.create(name, style);
			PreviewText.setTypeface(tf);
			mCurrentFont = name;
		}

		RadioButton rb_style = (RadioButton) findViewWithTag(style);
		rb_style.setChecked(true);
		LastFontStyleViewTag = style;
		mCurrentStyle = style;

	}
	
	public void setExitFlag(int flag) {
		EXIT_FLAG = flag;
	}

	public String getFontName() {
		return mCurrentFont;
	}
	
	public int getFontStyle() {
		return mCurrentStyle;
	}
	
	public int getFontColor() {
		return mCurrentColor;
	}
	
	public int getFontColorX() {
		return (int)colorPicker.mCurrentX;
	}

	public int getFontColorY() {
		return (int)colorPicker.mCurrentY;
	}
	
	public void adjustScrollViewSize(int width, int height) {
		ScrollView scrollView = (ScrollView)findViewById(R.id.fontsetting_scroll);
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)scrollView.getLayoutParams();
		params.width = width;
		params.height = height;
		scrollView.setLayoutParams(params);
	}
	
	public int getExitFlag() {
		return EXIT_FLAG;
	}
}
