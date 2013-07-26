package com.tcl.memo.data;

import java.io.File;
import java.util.ArrayList;

import com.tcl.memo.Constants;

import android.appwidget.AppWidgetManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class NoteUtils {
	private static final String TAG = NoteUtils.class.getSimpleName();

	public static Note toNote(Cursor cursor) {
		Note note = new Note();

		int columnIndex;
		columnIndex = cursor.getColumnIndex(Note.COLUMN_ID);
		if (columnIndex != -1) {
			note.mId = cursor.getLong(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.COLUMN_GROUP_ID);
		if (columnIndex != -1) {
			note.mGroupId = cursor.getLong(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.COLUMN_TITLE);
		if (columnIndex != -1) {
			note.mTitle = cursor.getString(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.COLUMN_LABEL);
		if (columnIndex != -1) {
			note.mLabel = cursor.getString(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.COLUMN_HAS_TEXT);
		if (columnIndex != -1) {
			note.mHasText = cursor.getInt(columnIndex) == 1;
		}
		columnIndex = cursor.getColumnIndex(Note.COLUMN_HAS_IMAGE);
		if (columnIndex != -1) {
			note.mHasImage = cursor.getInt(columnIndex) == 1;
		}
		columnIndex = cursor.getColumnIndex(Note.COLUMN_HAS_PAINT);
		if (columnIndex != -1) {
			note.mHasPaint = cursor.getInt(columnIndex) == 1;
		}
		columnIndex = cursor.getColumnIndex(Note.COLUMN_HAS_AUDIO);
		if (columnIndex != -1) {
			note.mHasAudio = cursor.getInt(columnIndex) == 1;
		}
		columnIndex = cursor.getColumnIndex(Note.COLUMN_THUMBNAIL_URI);
		if (columnIndex != -1) {
			note.mThumbnailUri = cursor.getString(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.COLUMN_SMALL_THUMBNAIL_URI);
		if (columnIndex != -1) {
			note.mSmallThumbnailUri = cursor.getString(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.COLUMN_BG_IMAGE_RES_ID);
		if (columnIndex != -1) {
			note.mBgImageResId = cursor.getInt(columnIndex);
			if(note.mBgImageResId == 0) {
				note.mBgImageResId = Constants.DEFAULT_NOTE_BG_RES_ID;
			}
		}
		columnIndex = cursor.getColumnIndex(Note.COLUMN_IS_STARRED);
		if (columnIndex != -1) {
			note.mIsStarred = cursor.getInt(columnIndex) == 1;
		}
		columnIndex = cursor.getColumnIndex(Note.COLUMN_CREATE_TIME);
		if (columnIndex != -1) {
			note.mCreateTime = cursor.getLong(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.COLUMN_MODIFY_TIME);
		if (columnIndex != -1) {
			note.mModifyTime = cursor.getLong(columnIndex);
		}

		return note;
	}

	public static Note.Text toText(Cursor cursor) {
		Note.Text text = new Note.Text();

		int columnIndex;
		columnIndex = cursor.getColumnIndex(Note.Text.COLUMN_ID);
		if (columnIndex != -1) {
			text.mId = cursor.getLong(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Text.COLUMN_NOTE_ID);
		if (columnIndex != -1) {
			text.mNoteId = cursor.getLong(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Text.COLUMN_LEFT);
		if (columnIndex != -1) {
			text.mLeft = cursor.getInt(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Text.COLUMN_TOP);
		if (columnIndex != -1) {
			text.mTop = cursor.getInt(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Text.COLUMN_RIGHT);
		if (columnIndex != -1) {
			text.mRight = cursor.getInt(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Text.COLUMN_BOTTOM);
		if (columnIndex != -1) {
			text.mBottom = cursor.getInt(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Text.COLUMN_LAYER);
		if (columnIndex != -1) {
			text.mLayer = cursor.getInt(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Text.COLUMN_COLOR);
		if (columnIndex != -1) {
			text.mColor = cursor.getInt(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Text.COLUMN_SIZE);
		if (columnIndex != -1) {
			text.mSize = cursor.getFloat(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Text.COLUMN_STYLE);
		if (columnIndex != -1) {
			text.mStyle = cursor.getInt(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Text.COLUMN_FONT);
		if (columnIndex != -1) {
			text.mFont = cursor.getString(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Text.COLUMN_CONTENT);
		if (columnIndex != -1) {
			text.mContent = cursor.getString(columnIndex);
		}

		return text;
	}

	public static Note.Image toImage(Cursor cursor) {
		Note.Image image = new Note.Image();

		int columnIndex;
		columnIndex = cursor.getColumnIndex(Note.Image.COLUMN_ID);
		if (columnIndex != -1) {
			image.mId = cursor.getLong(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Image.COLUMN_NOTE_ID);
		if (columnIndex != -1) {
			image.mNoteId = cursor.getLong(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Image.COLUMN_URI);
		if (columnIndex != -1) {
			image.mUri = cursor.getString(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Image.COLUMN_LEFT);
		if (columnIndex != -1) {
			image.mLeft = cursor.getInt(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Image.COLUMN_TOP);
		if (columnIndex != -1) {
			image.mTop = cursor.getInt(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Image.COLUMN_RIGHT);
		if (columnIndex != -1) {
			image.mRight = cursor.getInt(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Image.COLUMN_BOTTOM);
		if (columnIndex != -1) {
			image.mBottom = cursor.getInt(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Image.COLUMN_LAYER);
		if (columnIndex != -1) {
			image.mLayer = cursor.getInt(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Image.COLUMN_ROTATE);
		if (columnIndex != -1) {
			image.mRotate = cursor.getInt(columnIndex);
		}

		return image;
	}

	public static Note.Paint toPaint(Cursor cursor) {
		Note.Paint paint = new Note.Paint();

		int columnIndex;
		columnIndex = cursor.getColumnIndex(Note.Paint.COLUMN_ID);
		if (columnIndex != -1) {
			paint.mId = cursor.getLong(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Paint.COLUMN_NOTE_ID);
		if (columnIndex != -1) {
			paint.mNoteId = cursor.getLong(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Paint.COLUMN_URI);
		if (columnIndex != -1) {
			paint.mUri = cursor.getString(columnIndex);
		}

		return paint;
	}
	
	public static Note.Audio toAudio(Cursor cursor) {
		Note.Audio audio = new Note.Audio();

		int columnIndex;
		columnIndex = cursor.getColumnIndex(Note.Audio.COLUMN_ID);
		if (columnIndex != -1) {
			audio.mId = cursor.getLong(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Audio.COLUMN_NOTE_ID);
		if (columnIndex != -1) {
			audio.mNoteId = cursor.getLong(columnIndex);
		}
		columnIndex = cursor.getColumnIndex(Note.Audio.COLUMN_URI);
		if (columnIndex != -1) {
			audio.mUri = cursor.getString(columnIndex);
		}
		
		columnIndex = cursor.getColumnIndex(Note.Audio.COLUMN_CREATE_TIME);
		if (columnIndex != -1) {
			audio.mCreateTime = cursor.getLong(columnIndex);
		}

		return audio;
	}

	public static int deletePaint(Uri uri, String where,
			String[] selectionArgs, ContentResolver resolver) {
		Cursor cursor = resolver.query(uri, new String[] {
				Note.Paint.COLUMN_ID, Note.Paint.COLUMN_URI }, where,
				selectionArgs, null);
		if (cursor != null) {
			String path;
			File file;
			if (cursor.moveToNext()) {
				path = cursor.getString(cursor
						.getColumnIndex(Note.Paint.COLUMN_URI));
				if (path != null) {
					file = new File(path);
					if (file.exists()) {
						file.delete();
					}
				}
			}
			cursor.close();

			return resolver.delete(uri, where, selectionArgs);
		}
		return 0;
	}

	public static int deleteImage(Uri uri, String where,
			String[] selectionArgs, ContentResolver resolver) {
		Cursor cursor = resolver.query(uri, new String[] {
				Note.Image.COLUMN_ID, Note.Image.COLUMN_URI }, where,
				selectionArgs, null);
		if (cursor != null) {
			String path;
			File file;
			if (cursor.moveToNext()) {
				path = cursor.getString(cursor
						.getColumnIndex(Note.Image.COLUMN_URI));
				if (path != null) {
					file = new File(path);
					if (file.exists()) {
						file.delete();
					}
				}
			}
			cursor.close();

			return resolver.delete(uri, where, selectionArgs);
		}
		return 0;
	}
	
	public static void deleteNote(long noteId, Context context) {
		Note note = null;
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(Note.CONTENT_URI, null, Note.COLUMN_ID + "=" + noteId, null, null);
		if(cursor != null) {
			if(cursor.moveToNext()) {
				note = NoteUtils.toNote(cursor);
			}
			cursor.close();
		}
		if(note == null) {
			return;
		}
		//Added by yongan.qiu on 2012.3.21 begin.(for delete widget from launcher)
		SharedPreferences noteWidgetMap = context.getSharedPreferences("note_widget_map",Context.MODE_PRIVATE);
		int appWidgetId = noteWidgetMap.getInt(note.mThumbnailUri, -1);
		Log.i(TAG, "deleteNote(): appWidgetId = " + appWidgetId);
		if (appWidgetId < 0) {
			//have not added to launcher, so do nothing.
		} else {
			//firstly, remove data from SharedPreferences.
			noteWidgetMap.edit().remove(note.mThumbnailUri).commit();
			SharedPreferences widgetNoteMap = context.getSharedPreferences("widget_note_map",Context.MODE_PRIVATE);
			widgetNoteMap.edit().remove(String.valueOf(appWidgetId)).commit();
			SharedPreferences widgetDbMap = context.getSharedPreferences("widget_db_map",Context.MODE_PRIVATE);
			widgetDbMap.edit().remove(String.valueOf(appWidgetId)).commit();

			//secondly, send broadcast to delete widget from launcher.
			Intent widgetIntent = new Intent("com.android.launcher.action.REMOVE_WIDGET");
			widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			Log.i(TAG, "" + widgetIntent);
			context.sendBroadcast(widgetIntent);
		}
		//Added by yongan.qiu on 2012.3.21 end.
		File file;
		String path;
		cursor = resolver.query(Note.Paint.CONTENT_URI,
				new String[] { Note.Paint.COLUMN_URI },
				Note.Paint.COLUMN_NOTE_ID + "=" + noteId, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				path = cursor.getString(cursor
						.getColumnIndex(Note.Paint.COLUMN_URI));
				if (path != null) {
					file = new File(path);
					if (file.exists()) {
						file.delete();
					}
				}
			}
			cursor.close();
		}

		cursor = resolver.query(Note.Image.CONTENT_URI,
				new String[] { Note.Image.COLUMN_URI },
				Note.Image.COLUMN_NOTE_ID + "=" + noteId, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				path = cursor.getString(cursor
						.getColumnIndex(Note.Image.COLUMN_URI));
				if (path != null) {
					file = new File(path);
					if (file.exists()) {
						file.delete();
					}
				}
			}
			cursor.close();
		}

		cursor = resolver.query(Note.Audio.CONTENT_URI,
				new String[] { Note.Audio.COLUMN_URI },
				Note.Audio.COLUMN_NOTE_ID + "=" + noteId, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				path = cursor.getString(cursor
						.getColumnIndex(Note.Audio.COLUMN_URI));
				if (path != null) {
					file = new File(path);
					if (file.exists()) {
						file.delete();
					}
				}
			}
			cursor.close();
		}

		if (note.mThumbnailUri != null) {
			file = new File(note.mThumbnailUri);
			if (file.exists()) {
				file.delete();
			}
		}
		
		if (note.mSmallThumbnailUri != null) {
			file = new File(note.mSmallThumbnailUri);
			if (file.exists()) {
				file.delete();
			}
		}

		ContentProviderOperation.Builder builder;
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		builder = ContentProviderOperation.newDelete(Note.Text.CONTENT_URI);
		builder.withSelection(Note.Text.COLUMN_NOTE_ID + "=" + noteId,
				null);
		operations.add(builder.build());

		builder = ContentProviderOperation
				.newDelete(Note.Image.CONTENT_URI);
		builder.withSelection(Note.Image.COLUMN_NOTE_ID + "=" + noteId,
				null);
		operations.add(builder.build());

		builder = ContentProviderOperation
				.newDelete(Note.Paint.CONTENT_URI);
		builder.withSelection(Note.Paint.COLUMN_NOTE_ID + "=" + noteId,
				null);
		operations.add(builder.build());

		builder = ContentProviderOperation
				.newDelete(Note.Audio.CONTENT_URI);
		builder.withSelection(Note.Audio.COLUMN_NOTE_ID + "=" + noteId,
				null);
		operations.add(builder.build());

		builder = ContentProviderOperation.newDelete(Note.CONTENT_URI);
		builder.withSelection(Note.COLUMN_ID + "=" + noteId, null);
		operations.add(builder.build());

		try {
			resolver.applyBatch(MemoProvider.URI_AUTHORITY, operations);
		} catch (Exception e) {
			Log.e(TAG, e.toString(), e);
		}
	}
}