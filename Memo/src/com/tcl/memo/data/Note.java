package com.tcl.memo.data;

import com.tcl.memo.Constants;

import android.content.ContentValues;
import android.net.Uri;

public class Note {
	public final static String TABLE_NAME = "note";
	public final static String TABLE_NAME_SEARCH = "note_temp";

	public final static String COLUMN_ID = "_id";
	public final static String COLUMN_GROUP_ID = "group_id";
	public final static String COLUMN_TITLE = "title";
	public final static String COLUMN_LABEL = "label";
	public final static String COLUMN_HAS_TEXT = "has_text";
	public final static String COLUMN_HAS_IMAGE = "has_image";
	public final static String COLUMN_HAS_PAINT = "has_paint";
	public final static String COLUMN_HAS_AUDIO = "has_audio";
	public final static String COLUMN_THUMBNAIL_URI = "thumbnail_uri";
	public final static String COLUMN_SMALL_THUMBNAIL_URI = "small_thumbnail_uri";
	public final static String COLUMN_BG_IMAGE_RES_ID = "bg_image_res_id";
	public final static String COLUMN_IS_STARRED = "is_starred";
	public final static String COLUMN_CREATE_TIME = "create_time";
	public final static String COLUMN_MODIFY_TIME = "modify_time";
	
	public final static Uri CONTENT_URI = Uri.parse("content://"
			+ MemoProvider.URI_AUTHORITY + "/" + TABLE_NAME);
	public final static Uri CONTENT_URI_SEARCH = Uri.parse("content://"
			+ MemoProvider.URI_AUTHORITY + "/" + TABLE_NAME_SEARCH);

	public long mId;
	public long mGroupId;
	public String mTitle;
	public String mLabel;
	public boolean mHasText;
	public boolean mHasImage;
	public boolean mHasPaint;
	public boolean mHasAudio;
	public String mThumbnailUri;
	public String mSmallThumbnailUri;
	public int mBgImageResId = Constants.DEFAULT_NOTE_BG_RES_ID;
	public boolean mIsStarred;
	public long mCreateTime = System.currentTimeMillis();
	public long mModifyTime = System.currentTimeMillis();
	
	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		if(mId != 0) {
			values.put(COLUMN_ID, mId);
		}
		values.put(COLUMN_GROUP_ID, mGroupId);
		values.put(COLUMN_TITLE, mTitle);
		values.put(COLUMN_LABEL, mLabel);
		values.put(COLUMN_HAS_TEXT, mHasText);
		values.put(COLUMN_HAS_IMAGE, mHasImage);
		values.put(COLUMN_HAS_PAINT, mHasPaint);
		values.put(COLUMN_HAS_AUDIO, mHasAudio);
		values.put(COLUMN_THUMBNAIL_URI, mThumbnailUri);
		values.put(COLUMN_SMALL_THUMBNAIL_URI, mSmallThumbnailUri);
		if(mBgImageResId != 0) {
			values.put(COLUMN_BG_IMAGE_RES_ID, mBgImageResId);
		}
		values.put(COLUMN_IS_STARRED, mIsStarred);
		values.put(COLUMN_CREATE_TIME, mCreateTime);
		values.put(COLUMN_MODIFY_TIME, mModifyTime);
		return values;
	}
	
	public static class Text {
		public final static String TABLE_NAME = "note_text";
		
		public final static String COLUMN_ID = "_id";
		public final static String COLUMN_NOTE_ID = "note_id";
		public final static String COLUMN_LEFT = "left";
		public final static String COLUMN_TOP = "top";
		public final static String COLUMN_RIGHT = "right";
		public final static String COLUMN_BOTTOM = "bottom";
		public final static String COLUMN_LAYER = "layer";
		public final static String COLUMN_COLOR = "color";
		public final static String COLUMN_SIZE = "size";
		public final static String COLUMN_STYLE = "style";
		public final static String COLUMN_FONT = "font";
		public final static String COLUMN_CONTENT = "content";
		
		public final static Uri CONTENT_URI = Uri.parse("content://" + MemoProvider.URI_AUTHORITY + "/" + TABLE_NAME);
		
		public long mId;
		public long mNoteId;
		public int mLeft;
		public int mTop;
		public int mRight;
		public int mBottom;
		public int mLayer;
		public int mColor;
		public float mSize;
		public int mStyle;
		public String mFont;
		public String mContent;
		
		public ContentValues toContentValues() {
			ContentValues values = new ContentValues();
			if(mId != 0) {
				values.put(COLUMN_ID, mId);
			}
			values.put(COLUMN_NOTE_ID, mNoteId);
			values.put(COLUMN_LEFT, mLeft);
			values.put(COLUMN_TOP, mTop);
			values.put(COLUMN_RIGHT, mRight);
			values.put(COLUMN_BOTTOM, mBottom);
			values.put(COLUMN_LAYER, mLayer);
			values.put(COLUMN_COLOR, mColor);
			values.put(COLUMN_SIZE, mSize);
			values.put(COLUMN_STYLE, mStyle);
			values.put(COLUMN_FONT, mFont);
			values.put(COLUMN_CONTENT, mContent);
			return values;
		}
	}
	
	public static class Image {
		public final static String TABLE_NAME = "note_image";

		public final static String COLUMN_ID = "_id";
		public final static String COLUMN_NOTE_ID = "note_id";
		public final static String COLUMN_URI = "uri";
		public final static String COLUMN_LEFT = "left";
		public final static String COLUMN_TOP = "top";
		public final static String COLUMN_RIGHT = "right";
		public final static String COLUMN_BOTTOM = "bottom";
		public final static String COLUMN_LAYER = "layer";
		public final static String COLUMN_ROTATE = "rotate";
		
		public final static Uri CONTENT_URI = Uri.parse("content://"
				+ MemoProvider.URI_AUTHORITY + "/" + TABLE_NAME);

		public long mId;
		public long mNoteId;
		public String mUri;
		public int mLeft;
		public int mTop;
		public int mRight;
		public int mBottom;
		public int mLayer;
		public int mRotate;
		
		public ContentValues toContentValues() {
			ContentValues values = new ContentValues();
			if(mId != 0) {
				values.put(COLUMN_ID, mId);
			}
			values.put(COLUMN_NOTE_ID, mNoteId);
			values.put(COLUMN_URI, mUri);
			values.put(COLUMN_LEFT, mLeft);
			values.put(COLUMN_TOP, mTop);
			values.put(COLUMN_RIGHT, mRight);
			values.put(COLUMN_BOTTOM, mBottom);
			values.put(COLUMN_LAYER, mLayer);
			values.put(COLUMN_ROTATE, mRotate);
			return values;
		}
	}
	
	public static class Paint {
		public final static String TABLE_NAME = "note_paint";

		public final static String COLUMN_ID = "_id";
		public final static String COLUMN_NOTE_ID = "note_id";
		public final static String COLUMN_URI = "uri";
		
		public final static Uri CONTENT_URI = Uri.parse("content://"
				+ MemoProvider.URI_AUTHORITY + "/" + TABLE_NAME);

		public long mId;
		public long mNoteId;
		public String mUri;
		public int mLayer;

		public ContentValues toContentValues() {
			ContentValues values = new ContentValues();
			if(mId != 0) {
				values.put(COLUMN_ID, mId);
			}
			values.put(COLUMN_NOTE_ID, mNoteId);
			values.put(COLUMN_URI, mUri);
			return values;
		}
	}
	
	public static class Audio {
		public final static String TABLE_NAME = "note_audio";

		public final static String COLUMN_ID = "_id";
		public final static String COLUMN_NOTE_ID = "note_id";
		public final static String COLUMN_URI = "uri";
		public final static String COLUMN_CREATE_TIME = "create_time";
		
		public final static Uri CONTENT_URI = Uri.parse("content://"
				+ MemoProvider.URI_AUTHORITY + "/" + TABLE_NAME);

		public long mId;
		public long mNoteId;
		public String mUri;
		public long mCreateTime = System.currentTimeMillis();
		
		public ContentValues toContentValues() {
			ContentValues values = new ContentValues();
			if(mId != 0) {
				values.put(COLUMN_ID, mId);
			}
			values.put(COLUMN_NOTE_ID, mNoteId);
			values.put(COLUMN_URI, mUri);
			values.put(COLUMN_CREATE_TIME, mCreateTime);
			return values;
		}
	}
	
	public static class Group {
		public final static String TABLE_NAME = "note_group";

		public final static String COLUMN_ID = "_id";
		public final static String COLUMN_NAME = "name";
		public final static Uri CONTENT_URI = Uri.parse("content://"
				+ MemoProvider.URI_AUTHORITY + "/" + TABLE_NAME);

		public long mId;
		public String mName;

		public ContentValues toContentValues() {
			ContentValues values = new ContentValues();
			if(mId != 0) {
				values.put(COLUMN_ID, mId);
			}
			values.put(COLUMN_NAME, mName);
			return values;
		}
	}
}