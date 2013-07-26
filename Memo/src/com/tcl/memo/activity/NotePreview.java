package com.tcl.memo.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;

import com.tcl.memo.Constants;
import com.tcl.memo.R;
import com.tcl.memo.data.Note;
import com.tcl.memo.data.Note.Audio;
import com.tcl.memo.data.NoteUtils;
import com.tcl.memo.util.FileUtils;
import com.tcl.memo.view.AudioView;
import com.tcl.memo.view.NotePreviewLayout;
import com.tcl.memo.view.NotePreviewLayout.OnLayoutCompleteListener;

public class NotePreview extends Activity {
	private static final String TAG = NotePreview.class.getSimpleName();
	Cursor myCursor;
	Note mNote;
	Note nNote;
	ActionBar actionBar;
	private static final int EDIT_REQUEST_CODE = 1;
	ImageView preView;
	ImageView mPlayAudio;
	private PopupWindow mAudioWin;
	private NotePreviewLayout mNotePreviewLayout;
	PreviewNote mPreviewNote;
	Cursor groupCursor;
	Long groupId;
	private static final int FROM_NOTE_PREVIEW = 2;
	ArrayList<PreviewNote> noteList = new ArrayList<PreviewNote>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_preview);
		actionBar = getActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.note_action_bar_bg));

		Intent intent = getIntent();
		long noteId = intent.getLongExtra(Constants.EXTRA_NOTE_ID, 0);
		final int position = intent.getIntExtra(Constants.EXTRA_NOTE_INDEX, 0);
		final int count = intent.getIntExtra(Constants.EXTRA_NOTE_AMOUNT, 0);
		final String mOrder = intent
				.getStringExtra(Constants.EXTRA_NOTE_SORT_ORDER);
		Cursor queryCursor = getContentResolver().query(Note.CONTENT_URI, null,
				Note.COLUMN_ID + " == " + noteId, null, null);

		if (queryCursor == null || queryCursor.getCount() == 0) {
			this.finish();
		}

		queryCursor.moveToNext();
		groupId = queryCursor.getLong(queryCursor
				.getColumnIndex(Note.COLUMN_GROUP_ID));

		if (groupId != 0) {
			groupCursor = getContentResolver().query(Note.CONTENT_URI, null,
					Note.COLUMN_GROUP_ID + " == " + groupId, null, mOrder);
		} else {
			groupCursor = queryCursor;
			groupCursor.moveToPosition(-1);
		}
		while (groupCursor.moveToNext()) {
			mNote = NoteUtils.toNote(groupCursor);
			mPreviewNote = new PreviewNote();
			mPreviewNote.thumbUri = mNote.mThumbnailUri;
			mPreviewNote.hasAudio = mNote.mHasAudio;
			mPreviewNote.isStar = mNote.mIsStarred;
			mPreviewNote.noteId = mNote.mId;
			mPreviewNote.title = mNote.mTitle;
			mPreviewNote.label = mNote.mLabel;
			mPreviewNote.mBackGround = mNote.mBgImageResId;
			noteList.add(mPreviewNote);

		}
		mNotePreviewLayout = (NotePreviewLayout) findViewById(R.id.note_preview_layout);

		mNotePreviewLayout
				.setOnXXListener(new NotePreviewLayout.OnDismissAudioViewListener() {

					@Override
					public void onXX() {
						dismissAudioWin();
					}
				});

		mNotePreviewLayout.setNoteList(noteList);
		mNotePreviewLayout.setCurrentPosition(position);
		mNotePreviewLayout
				.setOnLayoutCompleteListener(new OnLayoutCompleteListener() {
					@Override
					public void onLayoutComplete(NotePreviewLayout layout) {
						if (mAudioWin != null && mAudioWin.isShowing()) {
							mAudioWin.update(findViewById(R.id.play_audio),
									 mNotePreviewLayout.getCurrentPosition()*mNotePreviewLayout.getWidth() -330, 0, -1, -1);
						}

					}
				});

		mNotePreviewLayout.adjustForOrientation(getResources()
				.getConfiguration().orientation);

	}

	public void onStarClicked(View v) {
		Cursor mNoteCursor = getContentResolver()
				.query(Note.CONTENT_URI,
						null,
						Note.COLUMN_ID
								+ " == "
								+ noteList.get(mNotePreviewLayout
										.getCurrentPosition()).noteId, null,
						null);
		while (mNoteCursor.moveToNext()) {
			nNote = NoteUtils.toNote(mNoteCursor);
		}
		Log.d("^^", "add star item clicked");
		CheckBox cv = (CheckBox) v;
		nNote.mIsStarred = cv.isChecked();
		getContentResolver().update(
				ContentUris.withAppendedId(Note.CONTENT_URI, nNote.mId),
				nNote.toContentValues(), null, null);
	}

	public void onPlayAudioClicked(View v) {
		if (mAudioWin == null || !mAudioWin.isShowing()) {
			showAudioWin();
		} else {
			dismissAudioWin();
		}
	}

	public void onPreviewClicked(View v) {
		Cursor mNoteCursor = getContentResolver()
				.query(Note.CONTENT_URI,
						null,
						Note.COLUMN_ID
								+ " == "
								+ noteList.get(mNotePreviewLayout
										.getCurrentPosition()).noteId, null,
						null);
		while (mNoteCursor.moveToNext()) {
			nNote = NoteUtils.toNote(mNoteCursor);
		}

		Intent intent = new Intent("com.tcl.memo.NoteEditor");
		intent.putExtra(Constants.EXTRA_NOTE_ID, nNote.mId);
		intent.putExtra(Constants.EXTRA_NOTE_INDEX,
				mNotePreviewLayout.getCurrentPosition());
		intent.putExtra(Constants.EXTRA_NOTE_AMOUNT, noteList.size());
		startActivityForResult(intent, EDIT_REQUEST_CODE);
	}

	public class PreviewNote {
		public Long noteId;
		public boolean hasAudio;
		public String thumbUri;
		public boolean isStar;
		public String title;
		public String label;
		public int mBackGround;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case EDIT_REQUEST_CODE: {
			finish();
			break;
		}
		case FROM_NOTE_PREVIEW: {
			finish();
			break;
		}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add: {
			Intent intent = new Intent(this, NoteEditor.class);
			intent.putExtra(Constants.EXTRA_NOTE_GROUP_ID, groupId);
			startActivityForResult(intent, FROM_NOTE_PREVIEW);
			break;
		}

		case R.id.menu_delete: {
			AlertDialog dlg = new AlertDialog.Builder(this)
			.setTitle(android.R.string.dialog_alert_title)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setMessage(
					getResources().getString(R.string.memo_delete_alert_title))
			.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							
						}
					})
			.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							Cursor mNoteCursor = getContentResolver().query(
									Note.CONTENT_URI,
									null,
									Note.COLUMN_ID
											+ " == "
											+ noteList.get(mNotePreviewLayout
													.getCurrentPosition()).noteId, null, null);
							while (mNoteCursor.moveToNext()) {
								nNote = NoteUtils.toNote(mNoteCursor);
							}
							NoteUtils.deleteNote(nNote.mId, NotePreview.this);
							finish();
						}
					})
			.create();
			dlg.show();
			
			
			
			break;
		}
		case R.id.menu_set_as: {
			Cursor mNoteCursor = getContentResolver().query(
					Note.CONTENT_URI,
					null,
					Note.COLUMN_ID
							+ " == "
							+ noteList.get(mNotePreviewLayout
									.getCurrentPosition()).noteId, null, null);
			while (mNoteCursor.moveToNext()) {
				nNote = NoteUtils.toNote(mNoteCursor);
			}
			Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
			intent.setDataAndType(Uri.parse("file:/" + nNote.mThumbnailUri),
					"image/*");
			intent.putExtra("mimeType", "image/*");
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			startActivity(Intent.createChooser(intent,
					getString(R.string.set_as)));
			break;
		}
		case R.id.menu_share: {
			Cursor mNoteCursor = getContentResolver().query(
					Note.CONTENT_URI,
					null,
					Note.COLUMN_ID
							+ " == "
							+ noteList.get(mNotePreviewLayout
									.getCurrentPosition()).noteId, null, null);
			while (mNoteCursor.moveToNext()) {
				nNote = NoteUtils.toNote(mNoteCursor);
			}
			Uri uri = Uri.parse("file://" + nNote.mThumbnailUri);
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("image/*");
			intent.putExtra(Intent.EXTRA_STREAM, uri);
			startActivity(intent);
			break;
		}
		case R.id.menu_export_to_gallery: {
			Cursor mNoteCursor = getContentResolver().query(
					Note.CONTENT_URI,
					null,
					Note.COLUMN_ID
							+ " == "
							+ noteList.get(mNotePreviewLayout
									.getCurrentPosition()).noteId, null, null);
			while (mNoteCursor.moveToNext()) {
				nNote = NoteUtils.toNote(mNoteCursor);
			}

			Toast.makeText(this, R.string.exporting, Toast.LENGTH_SHORT).show();
			try {
				File file = FileUtils.copyToDirection(new File(nNote.mThumbnailUri),
						Constants.GALLERY_DIR);
				if(file != null) {
					MediaScannerConnection.scanFile(NotePreview.this,
							new String[] { file.getAbsolutePath() }, null,
							null);
				}
				Toast.makeText(NotePreview.this, R.string.export_complete,
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(NotePreview.this, R.string.export_fail,
						Toast.LENGTH_SHORT).show();
			}

			break;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.note_preview, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mNotePreviewLayout.adjustForOrientation(newConfig.orientation);
		if(mAudioWin != null && mAudioWin.isShowing()){
//	    mAudioWin.showAsDropDown(findViewById(R.id.play_audio), mNotePreviewLayout.getCurrentPosition()*mNotePreviewLayout.getWidth() -330, 0);
//		Log.d("^^", "findViewById(R.id.play_audio).getX() : " + findViewById(R.id.play_audio).getX() + " findViewById(R.id.play_audio).getY() : " + findViewById(R.id.play_audio).getY());
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		dismissAudioWin();
	}

	private void showAudioWin() {
		Cursor mNoteCursor = getContentResolver()
				.query(Note.CONTENT_URI,
						null,
						Note.COLUMN_ID
								+ " == "
								+ noteList.get(mNotePreviewLayout
										.getCurrentPosition()).noteId, null,
						null);
		while (mNoteCursor.moveToNext()) {
			nNote = NoteUtils.toNote(mNoteCursor);
		}

		final AudioView audioView = new AudioView(this) {
			@Override
			protected void onAudioStateChanged() {
				super.onAudioStateChanged();
				findViewById(R.id.replace_delete_layout).setVisibility(
						View.GONE);
			}
		};

		Cursor cursor = getContentResolver().query(Note.Audio.CONTENT_URI,
				null, Note.Audio.COLUMN_NOTE_ID + "==" + nNote.mId, null, null);
		if (cursor != null && cursor.moveToNext()) {
			Audio audio = NoteUtils.toAudio(cursor);
			if (audio != null) {
				audioView.setInitAudio(audio);
				audioView.changeState(AudioView.STATE_START_PLAY_RECORD);
				
			}
		}

		mAudioWin = new PopupWindow(this);
		mAudioWin.setWidth(480);
		mAudioWin.setHeight(190);
		mAudioWin.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		mAudioWin.setContentView(audioView);
		mAudioWin.showAsDropDown(findViewById(R.id.play_audio), mNotePreviewLayout.getCurrentPosition()*mNotePreviewLayout.getWidth() -330, 0);
		mAudioWin.setOnDismissListener(new OnDismissListener(){
			@Override
			public void onDismiss() {
				if (audioView != null) {
					audioView.stopPlay();
					audioView.stopRecord();
				}
			}});
	}

	private void dismissAudioWin() {
		if (mAudioWin != null && mAudioWin.isShowing()) {
			mAudioWin.dismiss();
		}
	}

}
