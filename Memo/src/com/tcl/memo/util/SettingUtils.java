package com.tcl.memo.util;

import com.tcl.memo.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

public class SettingUtils {
	public static final String DEFAULT_FONT_NAME = null;
	public static final int DEFAULT_FONT_STYLE = Typeface.NORMAL;
	public static final int DEFAULT_FONT_COLOR = 0xFF000000;
	public static final int DEFAULT_PAINT_SIZE = 5;
	public static final int DEFAULT_PAINT_ALPHA = 255;
	public static final int DEFAULT_PAINT_COLOR = 0xFF000000;
	public static final int DEFAULT_ERASER_SIZE = 5;
	public static final int DEFAULT_EXIT_FLAG = 1;

	public static FontSetting getFontSetting(Context context) {
		FontSetting fontSetting = new FontSetting();
		SharedPreferences sharedPrefs = context.getSharedPreferences(
				Constants.SHARED_PREFS_NAME, Context.MODE_WORLD_READABLE);
		fontSetting.mFontName = sharedPrefs.getString(
				FontSetting.KEY_FONT_NAME, DEFAULT_FONT_NAME);
		fontSetting.mFontStyle = sharedPrefs.getInt(FontSetting.KEY_FONT_STYLE,
				DEFAULT_FONT_STYLE);
		fontSetting.mFontColor = sharedPrefs.getInt(FontSetting.KEY_FONT_COLOR,
				DEFAULT_FONT_COLOR);
		fontSetting.mFontColorX = sharedPrefs.getInt(
				FontSetting.KEY_FONT_COLOR_X, 200);
		fontSetting.mFontColorY = sharedPrefs.getInt(
				FontSetting.KEY_FONT_COLOR_Y, 50);
		fontSetting.mExitFlag = sharedPrefs.getInt(FontSetting.KEY_FONT_EIXT_FLAG, DEFAULT_EXIT_FLAG);
		return fontSetting;
	}

	public static boolean saveFontSetting(Context context,
			FontSetting fontSetting) {
		SharedPreferences sharedPrefs = context.getSharedPreferences(
				Constants.SHARED_PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(FontSetting.KEY_FONT_NAME, fontSetting.mFontName);
		editor.putInt(FontSetting.KEY_FONT_STYLE, fontSetting.mFontStyle);
		editor.putInt(FontSetting.KEY_FONT_COLOR, fontSetting.mFontColor);
		editor.putInt(FontSetting.KEY_FONT_COLOR_X, fontSetting.mFontColorX);
		editor.putInt(FontSetting.KEY_FONT_COLOR_Y, fontSetting.mFontColorY);
		editor.putInt(FontSetting.KEY_FONT_EIXT_FLAG, fontSetting.mExitFlag);
		
		return editor.commit();
	}

	public static PaintSetting getPaintSetting(Context context) {
		PaintSetting paintSetting = new PaintSetting();
		SharedPreferences sharedPrefs = context.getSharedPreferences(
				Constants.SHARED_PREFS_NAME, Context.MODE_WORLD_READABLE);
		paintSetting.mPaintSize = sharedPrefs.getInt(
				PaintSetting.KEY_PAINT_SIZE, DEFAULT_PAINT_SIZE);
		paintSetting.mPaintColor = sharedPrefs.getInt(
				PaintSetting.KEY_PAINT_COLOR, DEFAULT_PAINT_COLOR);
		paintSetting.mPaintAlpha = sharedPrefs.getInt(
				PaintSetting.KEY_PAINT_ALPHA, DEFAULT_PAINT_ALPHA);
		paintSetting.mPaintColorX = sharedPrefs.getInt(
				PaintSetting.KEY_PAINT_COLOR_X, 200);
		paintSetting.mPaintColorY = sharedPrefs.getInt(
				PaintSetting.KEY_PAINT_COLOR_Y, 50);
		paintSetting.mExitFlag = sharedPrefs.getInt(PaintSetting.KEY_PAINT_EIXT_FLAG, DEFAULT_EXIT_FLAG);

		return paintSetting;
	}

	public static boolean savePaintSetting(Context context,
			PaintSetting paintSetting) {
		SharedPreferences sharedPrefs = context.getSharedPreferences(
				Constants.SHARED_PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putInt(PaintSetting.KEY_PAINT_SIZE, paintSetting.mPaintSize);
		editor.putInt(PaintSetting.KEY_PAINT_ALPHA, paintSetting.mPaintAlpha);
		editor.putInt(PaintSetting.KEY_PAINT_COLOR, paintSetting.mPaintColor);
		editor.putInt(PaintSetting.KEY_PAINT_COLOR_X, paintSetting.mPaintColorX);
		editor.putInt(PaintSetting.KEY_PAINT_COLOR_Y, paintSetting.mPaintColorY);
		editor.putInt(PaintSetting.KEY_PAINT_EIXT_FLAG, paintSetting.mExitFlag);
		return editor.commit();
	}

	public static EraserSetting getEraserSetting(Context context) {
		EraserSetting eraserSetting = new EraserSetting();
		SharedPreferences sharedPrefs = context.getSharedPreferences(
				Constants.SHARED_PREFS_NAME, Context.MODE_WORLD_READABLE);
		eraserSetting.mEraserSize = sharedPrefs.getInt(
				EraserSetting.KEY_ERASER_SIZE, DEFAULT_ERASER_SIZE);
		return eraserSetting;
	}

	public static boolean saveEraserSetting(Context context,
			EraserSetting eraserSetting) {
		SharedPreferences sharedPrefs = context.getSharedPreferences(
				Constants.SHARED_PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putInt(EraserSetting.KEY_ERASER_SIZE, eraserSetting.mEraserSize);
		return editor.commit();
	}

	public static class FontSetting {
		public static final String KEY_FONT_NAME = "font_name";
		public static final String KEY_FONT_STYLE = "font_style";
		public static final String KEY_FONT_COLOR = "font_color";
		public static final String KEY_FONT_COLOR_X = "font_color_x";
		public static final String KEY_FONT_COLOR_Y = "font_color_y";
		public static final String KEY_FONT_EIXT_FLAG = "font_exit_flag";

		public String mFontName = DEFAULT_FONT_NAME;
		public int mFontStyle = DEFAULT_FONT_STYLE;
		public int mFontColor = DEFAULT_FONT_COLOR;
		public int mFontColorX;
		public int mFontColorY;
		public int mExitFlag;
	}

	public static class PaintSetting {
		public static final String KEY_PAINT_SIZE = "paint_size";
		public static final String KEY_PAINT_ALPHA = "paint_alpha";
		public static final String KEY_PAINT_COLOR = "paint_color";
		public static final String KEY_PAINT_COLOR_X = "paint_color_x";
		public static final String KEY_PAINT_COLOR_Y = "paint_color_y";
		public static final String KEY_PAINT_EIXT_FLAG = "font_exit_flag";

		public int mPaintSize = DEFAULT_PAINT_SIZE;
		public int mPaintAlpha = DEFAULT_PAINT_ALPHA;
		public int mPaintColor = DEFAULT_PAINT_COLOR;
		public int mPaintColorX;
		public int mPaintColorY;
		public int mExitFlag;
	}

	public static class EraserSetting {
		public static final String KEY_ERASER_SIZE = "eraser_size";
		public int mEraserSize = DEFAULT_ERASER_SIZE;
	}
}