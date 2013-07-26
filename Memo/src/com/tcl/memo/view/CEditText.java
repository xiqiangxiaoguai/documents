package com.tcl.memo.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class CEditText extends EditText {
	public CEditText(Context context) {
		super(context);
	}

	public CEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setTypeface(String fontName, int fontStyle) {
		setTypeface(Typeface.DEFAULT);
		setTypeface(Typeface.create(fontName, fontStyle));
	}
}