package com.tcl.memo.data;

import android.content.ContentValues;
import android.net.Uri;

public class Memo {

	public final static String TABLE_NAME = "memo";

	public final static String COLUMN_ID = "_id";
	public final static String COLUMN_DEFAULT_SORT = "default_sort";
	public final static String COLUMN_DEFAULT_VIEW = "default_view";
	public final static String COLUMN_DEFAULT_BACKGROUND = "default_background";
	
	public final static Uri CONTENT_URI = Uri.parse("content://"
			+ MemoProvider.URI_AUTHORITY + "/" + TABLE_NAME);

	public long mId;
	public int mDefaultSort;
	public int mDefaultView;
	public String mDefaultBackground;
	
	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		if(mId != 0) {
			values.put(COLUMN_ID, mId);
		}
		values.put(COLUMN_DEFAULT_SORT, mDefaultSort);
		values.put(COLUMN_DEFAULT_VIEW, mDefaultView);
		values.put(COLUMN_DEFAULT_BACKGROUND, mDefaultBackground);
		return values;
	}
}