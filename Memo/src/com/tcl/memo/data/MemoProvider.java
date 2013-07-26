package com.tcl.memo.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.tcl.memo.data.Note.Audio;
import com.tcl.memo.data.Note.Group;
import com.tcl.memo.data.Note.Image;
import com.tcl.memo.data.Note.Paint;
import com.tcl.memo.data.Note.Text;

public class MemoProvider extends ContentProvider {
	public static final String DB_NAME = "memo.db";

    public static final String URI_AUTHORITY = "com.tcl.memo";
    
    private static final int URI_CODE_TEXT = 1;
    private static final int URI_CODE_TEXT_ID = 2;
    private static final int URI_CODE_IMAGE = 3;
    private static final int URI_CODE_IMAGE_ID = 4;
    private static final int URI_CODE_PAINT = 5;
    private static final int URI_CODE_PAINT_ID = 6;
    private static final int URI_CODE_AUDIO = 7;
    private static final int URI_CODE_AUDIO_ID = 8;
    private static final int URI_CODE_NOTE = 9;
    private static final int URI_CODE_NOTE_ID = 10;
    private static final int URI_CODE_GROUP = 11;
    private static final int URI_CODE_GROUP_ID = 12;
    private static final int URI_CODE_MEMO = 13;
    private static final int URI_CODE_MEMO_ID = 14;
    private static final int URI_SEARCH_BY_KEY_ID = 15;
    
    public static final String URI_MIME_TEXT
        = "vnd.android.cursor.dir/vnd.memo.text";
    public static final String URI_ITEM_MIME_TEXT
        = "vnd.android.cursor.item/vnd.memo.text";
    public static final String URI_MIME_IMAGE
    	= "vnd.android.cursor.dir/vnd.memo.image";
    public static final String URI_ITEM_MIME_IMAGE
    	= "vnd.android.cursor.item/vnd.memo.image";
    public static final String URI_MIME_PAINT
    	= "vnd.android.cursor.dir/vnd.memo.paint";
    public static final String URI_ITEM_MIME_PAINT
    	= "vnd.android.cursor.item/vnd.memo.paint";
    public static final String URI_MIME_AUDIO
    	= "vnd.android.cursor.dir/vnd.memo.audio";
    public static final String URI_ITEM_MIME_AUDIO
    	= "vnd.android.cursor.item/vnd.memo.audio";
    public static final String URI_MIME_NOTE
    	= "vnd.android.cursor.dir/vnd.memo.note";
    public static final String URI_ITEM_MIME_NOTE
    	= "vnd.android.cursor.item/vnd.memo.note";
    public static final String URI_MIME_GROUP
    	= "vnd.android.cursor.dir/vnd.memo.group";
    public static final String URI_ITEM_MIME_GROUP
    	= "vnd.android.cursor.item/vnd.memo.group";
    public static final String URI_MIME_MEMO
    	= "vnd.android.cursor.dir/vnd.memo.memo";
    public static final String URI_ITEM_MIME_MEMO
    	= "vnd.android.cursor.item/vnd.memo.memo";
    public static final String URI_MIME_NOTE_SEARCH
	   = "vnd.android.cursor.dir/vnd.memo.note_temp";

    private static final String TAG = MemoProvider.class.getSimpleName();

    private static UriMatcher mUriMatcher;
    private MemoDbHelper mMemoDbHelper;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        
        mUriMatcher.addURI(URI_AUTHORITY, Text.TABLE_NAME, URI_CODE_TEXT);
        mUriMatcher.addURI(URI_AUTHORITY, Text.TABLE_NAME + "/#", URI_CODE_TEXT_ID);
        mUriMatcher.addURI(URI_AUTHORITY, Image.TABLE_NAME, URI_CODE_IMAGE);
        mUriMatcher.addURI(URI_AUTHORITY, Image.TABLE_NAME + "/#", URI_CODE_IMAGE_ID);
        mUriMatcher.addURI(URI_AUTHORITY, Paint.TABLE_NAME, URI_CODE_PAINT);
        mUriMatcher.addURI(URI_AUTHORITY, Paint.TABLE_NAME + "/#", URI_CODE_PAINT_ID);
        mUriMatcher.addURI(URI_AUTHORITY, Audio.TABLE_NAME, URI_CODE_AUDIO);
        mUriMatcher.addURI(URI_AUTHORITY, Audio.TABLE_NAME + "/#", URI_CODE_AUDIO_ID);
        mUriMatcher.addURI(URI_AUTHORITY, Group.TABLE_NAME, URI_CODE_GROUP);
        mUriMatcher.addURI(URI_AUTHORITY, Group.TABLE_NAME + "/#", URI_CODE_GROUP_ID);
        mUriMatcher.addURI(URI_AUTHORITY, Note.TABLE_NAME, URI_CODE_NOTE);
        mUriMatcher.addURI(URI_AUTHORITY, Note.TABLE_NAME + "/#", URI_CODE_NOTE_ID);
        mUriMatcher.addURI(URI_AUTHORITY, Memo.TABLE_NAME, URI_CODE_MEMO);
        mUriMatcher.addURI(URI_AUTHORITY, Memo.TABLE_NAME + "/#", URI_CODE_MEMO_ID);
        mUriMatcher.addURI(URI_AUTHORITY, Note.TABLE_NAME_SEARCH, URI_SEARCH_BY_KEY_ID);
    }

	@Override
	public boolean onCreate() {
		mMemoDbHelper = new MemoDbHelper(getContext(), DB_NAME, 1);
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		Log.e("memo", "=======================================uri:" + uri);
		switch (mUriMatcher.match(uri)) {
        case URI_CODE_TEXT:
            return URI_MIME_TEXT;
        case URI_CODE_TEXT_ID:
            return URI_ITEM_MIME_TEXT;
        case URI_CODE_IMAGE:
            return URI_MIME_IMAGE;
        case URI_CODE_IMAGE_ID:
            return URI_ITEM_MIME_IMAGE;
        case URI_CODE_PAINT:
            return URI_MIME_PAINT;
        case URI_CODE_PAINT_ID:
            return URI_ITEM_MIME_PAINT;
        case URI_CODE_AUDIO:
            return URI_MIME_AUDIO;
        case URI_CODE_AUDIO_ID:
            return URI_ITEM_MIME_AUDIO;
        case URI_CODE_NOTE:
            return URI_MIME_NOTE;
        case URI_CODE_NOTE_ID:
            return URI_ITEM_MIME_NOTE;
        case URI_CODE_GROUP:
            return URI_MIME_GROUP;
        case URI_CODE_GROUP_ID:
            return URI_ITEM_MIME_GROUP;
        case URI_CODE_MEMO:
            return URI_MIME_MEMO;
        case URI_CODE_MEMO_ID:
            return URI_ITEM_MIME_MEMO;
        case URI_SEARCH_BY_KEY_ID:
            return URI_MIME_NOTE_SEARCH;
        default:
            Log.e(TAG, "Unknown URI:" + uri);
            throw new IllegalArgumentException("Unknown URI:" + uri);
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId;
		Uri rowUri = null;
		SQLiteDatabase db = mMemoDbHelper.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case URI_CODE_TEXT:
			rowId = db.insert(Text.TABLE_NAME, null, values);
			if (rowId != -1) {
				rowUri = ContentUris.withAppendedId(Text.CONTENT_URI, rowId);
			}
			break;
		case URI_CODE_IMAGE:
			rowId = db.insert(Image.TABLE_NAME, null, values);
			if (rowId != -1) {
				rowUri = ContentUris.withAppendedId(Image.CONTENT_URI, rowId);
			}
			break;
		case URI_CODE_PAINT:
			rowId = db.insert(Paint.TABLE_NAME, null, values);
			if (rowId != -1) {
				rowUri = ContentUris.withAppendedId(Paint.CONTENT_URI, rowId);
			}
			break;
		case URI_CODE_AUDIO:
			rowId = db.insert(Audio.TABLE_NAME, null, values);
			if (rowId != -1) {
				rowUri = ContentUris.withAppendedId(Audio.CONTENT_URI, rowId);
			}
			break;
		case URI_CODE_NOTE:
			rowId = db.insert(Note.TABLE_NAME, null, values);
			if (rowId != -1) {
				rowUri = ContentUris.withAppendedId(Note.CONTENT_URI, rowId);
			}
			break;
			
		case URI_CODE_GROUP:
			rowId = db.insert(Group.TABLE_NAME, null, values);
			if (rowId != -1) {
				rowUri = ContentUris.withAppendedId(Group.CONTENT_URI, rowId);
			}
			break;
			
		case URI_CODE_MEMO:
			rowId = db.insert(Memo.TABLE_NAME, null, values);
			if (rowId != -1) {
				rowUri = ContentUris.withAppendedId(Memo.CONTENT_URI, rowId);
			}
			break;
			
		}
		db.close();
		return rowUri;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		SQLiteDatabase db = mMemoDbHelper.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case URI_CODE_TEXT:
			count = db.delete(Text.TABLE_NAME, selection, selectionArgs);
			break;
		case URI_CODE_TEXT_ID:
			count = db.delete(Text.TABLE_NAME,
					Text.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
			
		case URI_CODE_IMAGE:
			count = db.delete(Image.TABLE_NAME, selection, selectionArgs);
			break;
		case URI_CODE_IMAGE_ID:
			count = db.delete(Image.TABLE_NAME,
					Image.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
			
		case URI_CODE_PAINT:
			count = db.delete(Paint.TABLE_NAME, selection, selectionArgs);
			break;
		case URI_CODE_PAINT_ID:
			count = db.delete(Paint.TABLE_NAME,
					Paint.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
			
		case URI_CODE_AUDIO:
			count = db.delete(Audio.TABLE_NAME, selection, selectionArgs);
			break;
		case URI_CODE_AUDIO_ID:
			count = db.delete(Audio.TABLE_NAME,
					Audio.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
			
		case URI_CODE_NOTE:
			count = db.delete(Note.TABLE_NAME, selection, selectionArgs);
			break;
		case URI_CODE_NOTE_ID:
			count = db.delete(Note.TABLE_NAME,
					Note.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
			
		case URI_CODE_GROUP:
			count = db.delete(Group.TABLE_NAME, selection, selectionArgs);
			break;
		case URI_CODE_GROUP_ID:
			count = db.delete(Group.TABLE_NAME,
					Group.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
			
		case URI_CODE_MEMO:
			count = db.delete(Memo.TABLE_NAME, selection, selectionArgs);
			break;
		case URI_CODE_MEMO_ID:
			count = db.delete(Memo.TABLE_NAME,
					Memo.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
			
		default:
			Log.e(TAG, "Unknown URI:" + uri);
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		db.close();
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mMemoDbHelper.getReadableDatabase();
		int count;
		switch (mUriMatcher.match(uri)) {
		case URI_CODE_TEXT:
			count = db.update(Text.TABLE_NAME, values, selection, selectionArgs);
			break;
		case URI_CODE_TEXT_ID:
			count = db.update(Text.TABLE_NAME, values,
					Text.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
			
		case URI_CODE_IMAGE:
			count = db.update(Image.TABLE_NAME, values, selection, selectionArgs);
			break;
		case URI_CODE_IMAGE_ID:
			count = db.update(Image.TABLE_NAME, values,
					Image.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
			
		case URI_CODE_PAINT:
			count = db.update(Paint.TABLE_NAME, values, selection, selectionArgs);
			break;
		case URI_CODE_PAINT_ID:
			count = db.update(Paint.TABLE_NAME, values,
					Paint.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
			
		case URI_CODE_AUDIO:
			count = db.update(Audio.TABLE_NAME, values, selection, selectionArgs);
			break;
		case URI_CODE_AUDIO_ID:
			count = db.update(Audio.TABLE_NAME, values,
					Audio.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
			
		case URI_CODE_NOTE:
			count = db.update(Note.TABLE_NAME, values, selection, selectionArgs);
			break;
		case URI_CODE_NOTE_ID:
			count = db.update(Note.TABLE_NAME, values,
					Note.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
			
		case URI_CODE_GROUP:
			count = db.update(Group.TABLE_NAME, values, selection, selectionArgs);
			break;
		case URI_CODE_GROUP_ID:
			count = db.update(Group.TABLE_NAME, values,
					Group.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
			
		case URI_CODE_MEMO:
			count = db.update(Memo.TABLE_NAME, values, selection, selectionArgs);
			break;
		case URI_CODE_MEMO_ID:
			count = db.update(Memo.TABLE_NAME, values,
					Memo.COLUMN_ID
							+ "="
							+ uri.getLastPathSegment()
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ")" : ""), selectionArgs);
			break;
		default:
			Log.e(TAG, "Unknown URI:" + uri);
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		db.close();
		return count;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		Log.e("memo", "=======================================uri:" + uri);
		Log.e("memo", "=======================================mUriMatcher.match(uri):" + mUriMatcher.match(uri));
		switch (mUriMatcher.match(uri)) {
		case URI_CODE_TEXT:
			sqlBuilder.setTables(Text.TABLE_NAME);
			break;
		case URI_CODE_TEXT_ID:
			sqlBuilder.setTables(Text.TABLE_NAME);
			sqlBuilder.appendWhere(Text.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
			
		case URI_CODE_IMAGE:
			sqlBuilder.setTables(Image.TABLE_NAME);
			break;
		case URI_CODE_IMAGE_ID:
			sqlBuilder.setTables(Image.TABLE_NAME);
			sqlBuilder.appendWhere(Image.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
			
		case URI_CODE_PAINT:
			sqlBuilder.setTables(Paint.TABLE_NAME);
			break;
		case URI_CODE_PAINT_ID:
			sqlBuilder.setTables(Paint.TABLE_NAME);
			sqlBuilder.appendWhere(Paint.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
			
		case URI_CODE_AUDIO:
			sqlBuilder.setTables(Audio.TABLE_NAME);
			break;
		case URI_CODE_AUDIO_ID:
			sqlBuilder.setTables(Audio.TABLE_NAME);
			sqlBuilder.appendWhere(Audio.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
			
		case URI_CODE_NOTE:
			sqlBuilder.setTables(Note.TABLE_NAME);
			break;
		case URI_CODE_NOTE_ID:
			sqlBuilder.setTables(Note.TABLE_NAME);
			sqlBuilder.appendWhere(Note.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
			
		case URI_CODE_GROUP:
			sqlBuilder.setTables(Group.TABLE_NAME);
			break;
		case URI_CODE_GROUP_ID:
			sqlBuilder.setTables(Group.TABLE_NAME);
			sqlBuilder.appendWhere(Group.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
			
		case URI_CODE_MEMO:
			sqlBuilder.setTables(Memo.TABLE_NAME);
			break;
		case URI_CODE_MEMO_ID:
			sqlBuilder.setTables(Memo.TABLE_NAME);
			sqlBuilder.appendWhere(Memo.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		case URI_SEARCH_BY_KEY_ID:
			final SQLiteDatabase db = mMemoDbHelper.getWritableDatabase();
			cursor = db.rawQuery(selection, selectionArgs);
			return cursor;
		default:
			Log.e(TAG, "Unknown URI:" + uri);
			throw new IllegalArgumentException("Unknown URI:" + uri);
		}
		cursor = sqlBuilder.query(mMemoDbHelper.getReadableDatabase(), projection,
				selection, selectionArgs, null, null, sortOrder);
		return cursor;
	}
	
}