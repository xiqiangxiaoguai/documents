package com.tcl.memo;

import java.io.File;

import android.os.Environment;

public class Constants {
	public static final String EXTRA_NOTE_ID = "note_id";
	public static final String EXTRA_RES_ID = "res_id";
	public static final String EXTRA_NOTE_INDEX = "note_index";
	public static final String EXTRA_NOTE_AMOUNT = "note_amount";
	public static final String EXTRA_NOTE_GROUP_ID = "note_group_id";
	public static final String EXTRA_NOTE_SORT_ORDER = "sort_order";
	
	public static final String SHARED_PREFS_NAME = "com.tcl.memo";
	public static final String KEY_NOTE_BG_RES_ID = "note_bg_res_id";
	
	public static final int DEFAULT_NOTE_BG_RES_ID = R.drawable.note_bg_1;

	public static final File THUMBNAIL_DIR = new File(
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/.Memo/Thumbnail");

	public static final File IMAGE_DIR = new File(
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/.Memo/Image");

	public static final File PAINT_DIR = new File(
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/.Memo/Paint");
	
	public static final File AUDIO_DIR = new File(
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/.Memo/Audio");
	
	public static final File GALLERY_DIR = new File(
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures");
}