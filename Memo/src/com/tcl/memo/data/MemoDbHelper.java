package com.tcl.memo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.tcl.memo.Constants;
import com.tcl.memo.data.Note.Audio;
import com.tcl.memo.data.Note.Group;
import com.tcl.memo.data.Note.Image;
import com.tcl.memo.data.Note.Paint;
import com.tcl.memo.data.Note.Text;

public class MemoDbHelper extends SQLiteOpenHelper {

	public MemoDbHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public MemoDbHelper(Context context, String name, CursorFactory factory,
            int version) {
        super(context, name, factory, version);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Text.TABLE_NAME + "("
				+ Text.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Text.COLUMN_NOTE_ID + " INTEGER,"
				+ Text.COLUMN_LEFT + " INTEGER,"
				+ Text.COLUMN_TOP + " INTEGER,"
				+ Text.COLUMN_RIGHT + " INTEGER,"
				+ Text.COLUMN_BOTTOM + " INTEGER,"
				+ Text.COLUMN_LAYER + " INTEGER,"
				+ Text.COLUMN_COLOR + " INTEGER,"
				+ Text.COLUMN_SIZE + " REAL,"
				+ Text.COLUMN_STYLE + " INTEGER,"
				+ Text.COLUMN_FONT + " TEXT,"
				+ Text.COLUMN_CONTENT + " TEXT"
				+ ")");
		
		db.execSQL("CREATE TABLE " + Image.TABLE_NAME + "("
				+ Image.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Image.COLUMN_NOTE_ID + " INTEGER,"
				+ Image.COLUMN_URI + " TEXT,"
				+ Image.COLUMN_LEFT + " INTEGER,"
				+ Image.COLUMN_TOP + " INTEGER,"
				+ Image.COLUMN_RIGHT + " INTEGER,"
				+ Image.COLUMN_BOTTOM + " INTEGER,"
				+ Image.COLUMN_LAYER + " INTEGER,"
				+ Image.COLUMN_ROTATE + " INTEGER"
				+ ")");
		
		db.execSQL("CREATE TABLE " + Audio.TABLE_NAME + "("
				+ Audio.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Audio.COLUMN_NOTE_ID + " INTEGER,"
				+ Audio.COLUMN_URI + " TEXT,"
				+ Audio.COLUMN_CREATE_TIME + " INTEGER"
				+ ")");
		
		db.execSQL("CREATE TABLE " + Paint.TABLE_NAME + "("
				+ Paint.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Paint.COLUMN_NOTE_ID + " INTEGER,"
				+ Paint.COLUMN_URI + " TEXT"
				+ ")");
		
		db.execSQL("CREATE TABLE " + Note.TABLE_NAME + "("
				+ Note.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Note.COLUMN_IS_STARRED + " INTEGER,"
				+ Note.COLUMN_LABEL + " TEXT,"
				+ Note.COLUMN_TITLE + " TEXT,"
				+ Note.COLUMN_CREATE_TIME + " TEXT,"
				+ Note.COLUMN_MODIFY_TIME + " TEXT,"
				+ Note.COLUMN_HAS_IMAGE + " INTEGER DEFAULT 0,"
				+ Note.COLUMN_HAS_AUDIO + " INTEGER DEFAULT 0,"
				+ Note.COLUMN_HAS_PAINT + " INTEGER DEFAULT 0,"
				+ Note.COLUMN_HAS_TEXT + " INTEGER DEFAULT 0,"
				+ Note.COLUMN_THUMBNAIL_URI + " TEXT,"
				+ Note.COLUMN_SMALL_THUMBNAIL_URI + " TEXT,"
				+ Note.COLUMN_BG_IMAGE_RES_ID + " INTEGER DEFAULT " + Constants.DEFAULT_NOTE_BG_RES_ID + ","
				+ Note.COLUMN_GROUP_ID + " INTEGER"
				+ ")");
		
		db.execSQL("CREATE TABLE " + Group.TABLE_NAME + "("
				+ Group.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Group.COLUMN_NAME + " TEXT"
				+ ")");
		
		db.execSQL("CREATE TABLE " + Memo.TABLE_NAME + "("
				+ Memo.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Memo.COLUMN_DEFAULT_BACKGROUND + "INTEGER,"
				+ Memo.COLUMN_DEFAULT_SORT + " INTEGER DEFAULT 0,"
				+ Memo.COLUMN_DEFAULT_VIEW + " INTEGER DEFAULT 0"
				+ ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
}