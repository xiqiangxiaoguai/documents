package com.tcl.memo.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tcl.memo.Constants;
import com.tcl.memo.R;
import com.tcl.memo.data.MemoProvider;
import com.tcl.memo.data.Note;
import com.tcl.memo.data.NoteUtils;
import com.tcl.memo.util.BitmapUtils;
import com.tcl.memo.util.DateUtils;
import com.tcl.memo.util.FileUtils;
import com.tcl.memo.util.SettingUtils;
import com.tcl.memo.util.SettingUtils.EraserSetting;
import com.tcl.memo.util.SettingUtils.FontSetting;
import com.tcl.memo.util.SettingUtils.PaintSetting;
import com.tcl.memo.view.AudioView;
import com.tcl.memo.view.CEditText;
import com.tcl.memo.view.CRelativeLayout;
import com.tcl.memo.view.CRelativeLayout.OnLayoutCompleteListener;
import com.tcl.memo.view.CScrollView;
import com.tcl.memo.view.EraserSettingView;
import com.tcl.memo.view.FontSettingView;
import com.tcl.memo.view.NoteView;
import com.tcl.memo.view.NoteView.OnDeleteImageListener;
import com.tcl.memo.view.NoteView.OnDeleteTextListener;
import com.tcl.memo.view.PaintSettingView;
import com.tcl.memo.view.PaintView;
import com.tcl.memo.view.ZoomController;
import com.tcl.memo.widget.MemoAppWidgetProvider;

public class NoteEditor extends Activity {
	private static final String TAG = NoteEditor.class.getSimpleName();

	private static final int REQUEST_CODE_TAKE_IMAGE = 1;
	private static final int REQUEST_CODE_PICK_IMAGE = 2;
	private static final int REQUEST_CODE_CHOOSE_BG = 3;
	private static final int REQUEST_CODE_CROP_MAP = 4;

	private static final int DIALOG_SAVE_NOTE = 1;
	private static final int DIALOG_DISCARD_NOTE = 2;
	private static final int DIALOG_CREATE_GROUP = 3;
	private static final int DIALOG_SAVE_GROUP = 4;
	private static final int DIALOG_ADD_IMAGE = 5;
	private static final int DIALOG_UPDATE_NOTE_GROUP = 6;
	private static final int DIALOG_LOAD_NOTE = 7;
	private static final int DIALOG_ADD_LABEL = 8;

	private int mNoteBgResId = Constants.DEFAULT_NOTE_BG_RES_ID;

	private NoteView mNoteView;

	public List<Note.Text> mDeletedTextList = new LinkedList<Note.Text>();
	public List<Note.Image> mDeletedImageList = new LinkedList<Note.Image>();

	public List<Long> mNewInsertNoteIdList = new LinkedList<Long>();

	private PopupWindow mAudioWin;
	private PopupWindow mFontSettingWin;
	private PopupWindow mPaintSettingWin;
	private PopupWindow mEraserSettingWin;
	
	private CScrollView mScrollView;

	private int mNoteIndex;
	private int mNoteAmount;
	
	private boolean mReturnNotePreview;
	
	private LoadNoteAsyncTask mLoadNoteAsyncTask;
	private UpdateNoteGroupAsyncTask mUpdateNoteGroupAsyncTask;
	private CreateGroupAsyncTask mCreateGroupAsyncTask;
	private SaveNoteAsyncTask mSaveNoteAsyncTask;

	//Added by yongan.qiu on 2012.3.20 begin.
	private BroadcastReceiver mAddWidgetResultReceiver;
	//Added by yongan.qiu on 2012.3.20 end.
	
	//Added by yongan.qiu on 2012.3.24 begin.
	private int mAppWidgetId;
	private boolean mEnterFromWidget;
	//Added by yongan.qiu on 2012.3.24 end.
	//Added by gangzhou.qi at 2012-8-3 Begin
	private ArrayList<Integer> mCacheImageList = new ArrayList<Integer>();
	private int mCacheImageListLimited = 100;
	private int mCurrentCacheImageIndex = 0;
	//Added by gangzhou.qi at 2012-8-3 End
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_editor);

		Intent intent = getIntent();
		//Added by yongan.qiu on 2012.3.24 begin.
		if(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(intent.getAction())) {
			mEnterFromWidget = true;
			mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		}
		//Added by yongan.qiu on 2012.3.24 end.

		mNoteIndex = intent.getIntExtra(Constants.EXTRA_NOTE_INDEX, 0);
		mNoteAmount = intent.getIntExtra(Constants.EXTRA_NOTE_AMOUNT, 1);

		initActionBar();
		
		CRelativeLayout layout = (CRelativeLayout)findViewById(R.id.layout);
		layout.setOnLayoutCompleteListener(new OnLayoutCompleteListener(){
			@Override
			public void onLayoutComplete(CRelativeLayout layout) {
				if (mFontSettingWin != null && mFontSettingWin.isShowing()) {
					int width = 530;
					int height = isLandscape() ? 590 : 750;
					((FontSettingView) mFontSettingWin.getContentView())
							.adjustScrollViewSize(width, isLandscape() ? 488
									: LinearLayout.LayoutParams.WRAP_CONTENT);
					mFontSettingWin.update(getActionBar().getCustomView()
							.findViewById(R.id.insert_mode), 4, -4, width,
							height);
				}
				if (mPaintSettingWin != null && mPaintSettingWin.isShowing()) {
					int width = 530;
					int height = isLandscape() ? 590 : 681;
					((PaintSettingView) mPaintSettingWin.getContentView())
							.adjustScrollViewSize(width, isLandscape() ? 488
									: LinearLayout.LayoutParams.WRAP_CONTENT);
					mPaintSettingWin.update(getActionBar().getCustomView()
							.findViewById(R.id.paint_mode), -73, -4, width,
							height);
				}
				if (mEraserSettingWin != null && mEraserSettingWin.isShowing()) {
					mEraserSettingWin.update(getActionBar().getCustomView()
							.findViewById(R.id.eraser), -150, -4, -1, -1);
				}
				if (mAudioWin != null && mAudioWin.isShowing()) {
					mAudioWin.update(findViewById(R.id.add_audio), -330, 0, -1,
							-1);
				}
			}});

		mNoteView = (NoteView) findViewById(R.id.note_view);
		mNoteView.setNoteMode(NoteView.NOTE_MODE_PAINT);
		
		PaintView paintView = mNoteView.getPaintView();
		PaintSetting paintSetting = SettingUtils.getPaintSetting(this);
		paintView.setStrokeColor(paintSetting.mPaintColor);
		paintView.setStrokeAlpha(paintSetting.mPaintAlpha);
		paintView.setStrokeSize(paintSetting.mPaintSize);
		
		EraserSetting eraserSetting = SettingUtils.getEraserSetting(this);
		paintView.setEraserSize(eraserSetting.mEraserSize);

		mScrollView = (CScrollView) findViewById(R.id.scroll);
		mScrollView.setVerticalFadingEdgeEnabled(false);
		mScrollView.setHorizontalFadingEdgeEnabled(false);
		mScrollView.setOverScrollMode(CScrollView.OVER_SCROLL_NEVER);

		findViewById(R.id.forward_backward_layout).setVisibility(View.INVISIBLE);
		((CheckBox) findViewById(R.id.move))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						mScrollView.setAllowScroll(isChecked);
						mNoteView.setTouchable(!isChecked);
					}
				});

		findViewById(R.id.add_text).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RadioButton rb = (RadioButton) getActionBar().getCustomView()
						.findViewById(R.id.insert_mode);
				if (!rb.isChecked()) {
					rb.performClick();
				}
				mNoteView.setNoteMode(NoteView.NOTE_MODE_INSERT);
				mNoteView.addText("", SettingUtils.getFontSetting(NoteEditor.this), true);
			}
		});

		findViewById(R.id.add_image).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RadioButton rb = (RadioButton) getActionBar().getCustomView()
						.findViewById(R.id.insert_mode);
				if (!rb.isChecked()) {
					rb.performClick();
				}
				showDialog(DIALOG_ADD_IMAGE);
			}
		});

		findViewById(R.id.add_audio).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAudioWin == null || !mAudioWin.isShowing()) {
					showAudioWin();
				} else {
					dismissAudioWin();
				}
			}
		});

		findViewById(R.id.add_star).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				mNoteView.getNote().mIsStarred = cb.isChecked();
			}
		});
		
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				final int viewId = v.getId();
				if(!isNoteBlank()) {
					mSaveNoteAsyncTask = new SaveNoteAsyncTask() {
						@Override
						protected void onPostExecute(Long result) {
							super.onPostExecute(result);
							if (needCreateGroup()) {
								mReturnNotePreview = true;
								showDialog(DIALOG_CREATE_GROUP);
							} else {
								Intent intent = new Intent(NoteEditor.this, NotePreview.class);
								intent.putExtra(Constants.EXTRA_NOTE_ID, result.longValue());
								switch (viewId) {
								case R.id.forward:
									intent.putExtra(Constants.EXTRA_NOTE_INDEX, mNoteIndex - 1);
									break;
								case R.id.backward:
									intent.putExtra(Constants.EXTRA_NOTE_INDEX, mNoteIndex + 1);
									break;
								}
								startActivity(intent);
								finish();
							}
						}
					};
					mSaveNoteAsyncTask.execute();
				} else if(needCreateGroup()) {
					mReturnNotePreview = true;
					showDialog(DIALOG_CREATE_GROUP);
				} else {
					long noteId = 0;
					if(mNewInsertNoteIdList.isEmpty()) {
						ContentResolver resolver = getContentResolver();
						Cursor cursor = resolver.query(Note.CONTENT_URI, null,
								Note.COLUMN_GROUP_ID + "="
										+ mNoteView.getNote().mGroupId, null,
								Note.COLUMN_ID);
						if(cursor != null) {
							if(cursor.moveToLast()) {
								noteId = cursor.getLong(cursor.getColumnIndex(Note.COLUMN_ID));
							}
							cursor.close();
						}
					} else {
						noteId = mNewInsertNoteIdList.get(mNewInsertNoteIdList.size() - 1);
					}
					if(noteId != 0) {
						Intent intent = new Intent(NoteEditor.this, NotePreview.class);
						intent.putExtra(Constants.EXTRA_NOTE_ID, noteId);
						intent.putExtra(Constants.EXTRA_NOTE_INDEX, mNoteIndex - 1);
						startActivity(intent);
						finish();
					}
				}
			}};
			

		findViewById(R.id.forward).setOnClickListener(onClickListener);
		findViewById(R.id.backward).setOnClickListener(onClickListener);
		
		if(mEnterFromWidget) {
			findViewById(R.id.add_page).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.add_page).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					NoteView.OnAddPageListener onAddPageListener = mNoteView
							.getOnAddPageListener();
					if (onAddPageListener != null) {
						onAddPageListener.onAddPage();
					}
				}
			});
		}

		mNoteView.setOnDeleteTextListener(new OnDeleteTextListener() {
			@Override
			public void onDeleteText(Note.Text text) {
				mDeletedTextList.add(text);
			}
		});

		mNoteView.setOnDeleteImageListener(new OnDeleteImageListener() {
			@Override
			public void onDeleteImage(Note.Image image) {
				mDeletedImageList.add(image);
			}
		});

		mNoteView.getPaintView().setOnUndoRedoStateListener(
				new PaintView.OnUndoRedoStateChangeListener() {
					@Override
					public void OnUndoStateChanged(boolean canUndo) {
						getActionBar().getCustomView().findViewById(R.id.undo)
								.setEnabled(canUndo);
					}

					@Override
					public void OnRedoStateChanged(boolean canRedo) {
						getActionBar().getCustomView().findViewById(R.id.redo)
								.setEnabled(canRedo);
					}
				});

		mNoteView.setOnAddPageListener(new NoteView.OnAddPageListener() {
			@Override
			public void onAddPage() {
				if (isNoteBlank()) {
					Toast.makeText(NoteEditor.this, R.string.note_is_blank,
							Toast.LENGTH_SHORT).show();
				} else {
					dismissAllWin();
					mSaveNoteAsyncTask = new SaveNoteAsyncTask() {
						@Override
						protected void onPostExecute(Long result) {
							mNoteIndex++;
							mNoteAmount++;
							super.onPostExecute(result);
							Note note = mNoteView.getNote();
							long groupId = note.mGroupId;
							mNoteView.clearAll();
							
							note = mNoteView.getNote();
							note.mGroupId = groupId;
							note.mBgImageResId = mNoteBgResId;
							((EditText) findViewById(R.id.note_title)).setText(null);
							((CheckBox) findViewById(R.id.add_star)).setChecked(false);
							((TextView) findViewById(R.id.label)).setText(null);
							
							if (mNoteAmount > 1) {
								findViewById(R.id.forward_backward_layout).setVisibility(View.VISIBLE);
								findViewById(R.id.backward).setVisibility(View.INVISIBLE);
								((TextView)findViewById(R.id.page_number)).setText(mNoteAmount + "/" + mNoteAmount);
							} else {
								findViewById(R.id.forward_backward_layout).setVisibility(View.INVISIBLE);
							}
							((ImageButton) findViewById(R.id.add_audio)).setImageResource(R.drawable.add_audio);
						}
					};
					mSaveNoteAsyncTask.execute();
				}
			}
		});
		adjustForOrientation(getResources().getConfiguration().orientation);

		long noteId = intent.getLongExtra(Constants.EXTRA_NOTE_ID, 0);
		if (noteId > 0) {
			mLoadNoteAsyncTask = new LoadNoteAsyncTask();
			mLoadNoteAsyncTask.execute(noteId);
		} else {
			SharedPreferences sharedPrefs = getSharedPreferences(
					Constants.SHARED_PREFS_NAME, Context.MODE_WORLD_READABLE);
			mNoteBgResId = sharedPrefs.getInt(Constants.KEY_NOTE_BG_RES_ID, Constants.DEFAULT_NOTE_BG_RES_ID);
			
			mNoteView.getNote().mGroupId = intent.getLongExtra(Constants.EXTRA_NOTE_GROUP_ID, 0);
			mNoteView.getNote().mBgImageResId = mNoteBgResId;
			mNoteView.setNoteBg(mNoteBgResId);
			setNoteBgTopBg(mNoteBgResId);
			
			if(mNoteAmount > 1) {
				findViewById(R.id.forward_backward_layout).setVisibility(View.VISIBLE);
				findViewById(R.id.forward).setVisibility(View.VISIBLE);
				findViewById(R.id.backward).setVisibility(View.INVISIBLE);
				((TextView)findViewById(R.id.page_number)).setText(mNoteAmount + "/" + mNoteAmount);
			} else {
				findViewById(R.id.forward_backward_layout).setVisibility(View.INVISIBLE);
			}
		}
		
		//Added by yongan.qiu on 2012.3.20 begin.
		mAddWidgetResultReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				onReceiveAddWidgetResult(context,intent);
			}
		};
		IntentFilter intentFilter = new IntentFilter("com.android.launcher.action.ADD_WIDGET_RESULT");
		registerReceiver(mAddWidgetResultReceiver, intentFilter);	
		//Added by yongan.qiu on 2012.3.20 end.
	}
	
	private void setNoteBgTopBg(int noteBgResId) {
		int noteBgTopResId = R.drawable.note_bg_1_top;
		switch(noteBgResId) {
		case R.drawable.note_bg_1:
			noteBgTopResId = R.drawable.note_bg_1_top;
			break;
		case R.drawable.note_bg_2:
			noteBgTopResId = R.drawable.note_bg_2_top;
			break;
		case R.drawable.note_bg_3:
			noteBgTopResId = R.drawable.note_bg_3_top;
			break;
		case R.drawable.note_bg_4:
			noteBgTopResId = R.drawable.note_bg_4_top;
			break;
		case R.drawable.note_bg_5:
			noteBgTopResId = R.drawable.note_bg_5_top;
			break;
		case R.drawable.note_bg_6:
			noteBgTopResId = R.drawable.note_bg_6_top;
			break;
		}
		findViewById(R.id.tool_bar).setBackgroundResource(noteBgTopResId);
	}

	//Added by yongan.qiu on 2012.3.20 begin.
	private void onReceiveAddWidgetResult(Context context, Intent intent) {
		Log.i(TAG, "onReceiveAddWidgetResult.onReceive(). " + intent);
		String path = intent.getStringExtra("note_path");
		long id = intent.getLongExtra("note_id", -1);
		boolean success = intent.getBooleanExtra("success", false);
		if (success) {
			int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if (appWidgetId >= 0) {
				
				SharedPreferences noteWidgetMap = this.getSharedPreferences("note_widget_map",Context.MODE_PRIVATE);
				noteWidgetMap.edit().putInt(path, appWidgetId).apply(); 
				SharedPreferences widgetNoteMap = this.getSharedPreferences("widget_note_map",Context.MODE_PRIVATE);
				widgetNoteMap.edit().putString(String.valueOf(appWidgetId), path).apply();
				SharedPreferences widgetDbMap = this.getSharedPreferences("widget_db_map", Context.MODE_PRIVATE);
				widgetDbMap.edit().putLong(String.valueOf(appWidgetId), id).apply();
				
				//Toast to tell user that add widget success.
				Toast.makeText(this, R.string.add_widget_success, Toast.LENGTH_SHORT).show();
				
			} else {
				Log.d(TAG, "onReceiveAddWidgetResult(): invalid appwidget id!");
				//Toast to tell user that add widget failed.
				Toast.makeText(this, R.string.add_widget_failed, Toast.LENGTH_SHORT).show();
			}
		} else {
			//Toast to tell user that adding failed.
			Toast.makeText(this, R.string.add_widget_failed, Toast.LENGTH_SHORT).show();
		}

	}
	//Added by yongan.qiu on 2012.3.20 end.
	
	@Override
	public void onBackPressed() {
		if (mEraserSettingWin != null && mEraserSettingWin.isShowing()) {
			mEraserSettingWin.dismiss();
		//add by lijuan.li 2012.03.21 begin
		} else if (mPaintSettingWin != null && mPaintSettingWin.isShowing()) {
			mPaintSettingWin.dismiss();
		} else if (mFontSettingWin != null && mFontSettingWin.isShowing()) {
			mFontSettingWin.dismiss();
		//add by lijuan.li 2012.03.21 end
		} else {
			if(isNoteBlank()) {
				Toast.makeText(NoteEditor.this, R.string.note_is_blank,
						Toast.LENGTH_SHORT).show();
				finish();
			} else {
				showDialog(DIALOG_DISCARD_NOTE);
			}
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean hasLabel = !TextUtils.isEmpty(mNoteView.getNote().mLabel);
		
		MenuItem menuItem = menu.findItem(R.id.menu_add_label);
		menuItem.setVisible(!hasLabel);
		
		menuItem = menu.findItem(R.id.menu_modify_label);
		menuItem.setVisible(hasLabel);
		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_save: {
			final View v = getWindow().peekDecorView();
            if (v != null && v.getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
			
			
			if (isNoteBlank()) {
				if (needCreateGroup()) {
					showDialog(DIALOG_CREATE_GROUP);
				} else {
					dismissAllWin();
					Toast.makeText(this, R.string.note_is_blank,
							Toast.LENGTH_SHORT).show();
					finish();
				}
			} else {
				dismissAllWin();
				mSaveNoteAsyncTask = new SaveNoteAsyncTask() {
					@Override
					protected void onPostExecute(Long result) {
						super.onPostExecute(result);
						//Added by yongan.qiu on 2012.3.24 begin.
						if(mEnterFromWidget) {
							Intent intent = new Intent();
							intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
							setResult(RESULT_OK, intent);
							
							Log.i(TAG, "mAppWidgetId " + mAppWidgetId);
							
							SharedPreferences noteWidgetMap = NoteEditor.this.getSharedPreferences("note_widget_map",Context.MODE_PRIVATE);
							noteWidgetMap.edit().putInt(mNoteView.getNote().mThumbnailUri, mAppWidgetId).apply(); 
							SharedPreferences widgetNoteMap = NoteEditor.this.getSharedPreferences("widget_note_map",Context.MODE_PRIVATE);
							widgetNoteMap.edit().putString(String.valueOf(mAppWidgetId), mNoteView.getNote().mThumbnailUri).apply();
							SharedPreferences widgetDbMap = NoteEditor.this.getSharedPreferences("widget_db_map", Context.MODE_PRIVATE);
							widgetDbMap.edit().putLong(String.valueOf(mAppWidgetId), mNoteView.getNote().mId).apply();

							Intent widgetIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
							widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{mAppWidgetId});
							widgetIntent.setComponent(MemoAppWidgetProvider.COMPONENT);
							sendBroadcast(widgetIntent);

							finish();
							return;
						} else {
						//Added by yongan.qiu on 2012.3.24 end.
							if (needCreateGroup()) {
								showDialog(DIALOG_CREATE_GROUP);
							} else {
								setResult(RESULT_OK);
								finish();
							}
						}
					}
				};
				mSaveNoteAsyncTask.execute();
			}
			break;
		}
		case R.id.menu_cancel: {
			if(isNoteBlank()) {
				if (needCreateGroup()) {
					showDialog(DIALOG_CREATE_GROUP);
				} else {
					Toast.makeText(NoteEditor.this, R.string.note_is_blank,
							Toast.LENGTH_SHORT).show();
					finish();
				}
			} else {
				showDialog(DIALOG_DISCARD_NOTE);
			}
			break;
		}
		case R.id.menu_add_label:
		case R.id.menu_modify_label: {
			showDialog(DIALOG_ADD_LABEL);
			break;
		}
		case R.id.menu_background: {
			startActivityForResult(new Intent("com.tcl.memo.NoteBgList"),
					REQUEST_CODE_CHOOSE_BG);
			break;
		}
		case R.id.menu_set_as: {
			if (isNoteBlank()) {
				Toast.makeText(this, R.string.note_is_blank,
						Toast.LENGTH_SHORT).show();
			} else {
				mSaveNoteAsyncTask = new SaveNoteAsyncTask() {
					@Override
					protected void onPostExecute(Long result) {
						super.onPostExecute(result);
	
						Note note = mNoteView.getNote();
						Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
						intent.setDataAndType(
								Uri.parse("file:/" + note.mThumbnailUri), "image/*");
						intent.putExtra("mimeType", "image/*");
						intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						startActivity(Intent.createChooser(intent,
								getString(R.string.set_as)));
	
					}
				};
				mSaveNoteAsyncTask.execute();
			}
			break;
		}
		case R.id.menu_share: {
			if (isNoteBlank()) {
				Toast.makeText(this, R.string.note_is_blank,
						Toast.LENGTH_SHORT).show();
			} else {
				mSaveNoteAsyncTask = new SaveNoteAsyncTask() {
					@Override
					protected void onPostExecute(Long result) {
						super.onPostExecute(result);
	
						Note note = mNoteView.getNote();
						Uri uri = Uri.parse("file://" + note.mThumbnailUri);
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("image/*");
						intent.putExtra(Intent.EXTRA_STREAM, uri);
						startActivity(intent);
					}
				};
				mSaveNoteAsyncTask.execute();
			}
			break;
		}
		case R.id.menu_export_to_gallery: {
			if (isNoteBlank()) {
				Toast.makeText(this, R.string.note_is_blank,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, R.string.exporting,
						Toast.LENGTH_SHORT).show();
				mSaveNoteAsyncTask = new SaveNoteAsyncTask() {
					@Override
					protected Long doInBackground(Void... params) {
						long noteId = super.doInBackground(params);
						File file = FileUtils.copyToDirection(
								new File(mNoteView.getNote().mThumbnailUri),
								Constants.GALLERY_DIR);
						if(file != null) {
							MediaScannerConnection.scanFile(NoteEditor.this,
									new String[] { file.getAbsolutePath() }, null,
									null);
						}
						return noteId;
					}
					
					@Override
					protected void onPostExecute(Long result) {
						super.onPostExecute(result);
						Toast.makeText(NoteEditor.this, R.string.export_complete,
								Toast.LENGTH_SHORT).show();
						if(mNoteAmount > 1) {
							findViewById(R.id.forward_backward_layout).setVisibility(View.VISIBLE);
							findViewById(R.id.backward).setVisibility(View.INVISIBLE);
							((TextView)findViewById(R.id.page_number)).setText(mNoteAmount + "/" + mNoteAmount);
						} else {
							findViewById(R.id.forward_backward_layout).setVisibility(View.INVISIBLE);
						}
					}
				};
				mSaveNoteAsyncTask.execute();
			}
			break;
		}
		//Added by yongan.qiu on 2012.3.20 begin.
		case R.id.menu_add_widget: {
			if (isNoteBlank()) {
				Toast.makeText(this, R.string.note_is_blank,
						Toast.LENGTH_SHORT).show();
			} else {
				mSaveNoteAsyncTask = new SaveNoteAsyncTask() {
					@Override
					protected void onPostExecute(Long result) {
						super.onPostExecute(result);
						
						Note note = mNoteView.getNote();
						
						SharedPreferences noteWidgetMap = NoteEditor.this.getSharedPreferences("note_widget_map",Context.MODE_PRIVATE);
						int appWidgetId =  noteWidgetMap.getInt(note.mThumbnailUri, -1);
						boolean shouldAddToHome = false;
						if (appWidgetId >= 0) {
							if (AppWidgetManager.getInstance(NoteEditor.this).getAppWidgetInfo(appWidgetId) != null) {
								Log.w(TAG, "appwidget for this note already exist.");
								Toast.makeText(NoteEditor.this, R.string.widget_already_exists, Toast.LENGTH_SHORT).show();
							} else {//widget not exists indeed.
								//remove from SharedPreferences.
								Log.w(TAG, "saveNote(): can not found appWidgetId " + appWidgetId + 
										" in AppWidgetManager, so remove it from SharedPreferences");
								noteWidgetMap.edit().remove(note.mThumbnailUri).apply();
								SharedPreferences widgetNoteMap = NoteEditor.this.getSharedPreferences("widget_note_map",Context.MODE_PRIVATE);
								widgetNoteMap.edit().remove(String.valueOf(appWidgetId)).apply();
								SharedPreferences widgetDbMap = NoteEditor.this.getSharedPreferences("widget_db_map",Context.MODE_PRIVATE);
								widgetDbMap.edit().remove(String.valueOf(appWidgetId)).apply();
								shouldAddToHome = true;
							}
						} else {
							shouldAddToHome = true;
						}
						if (shouldAddToHome) {
							Intent widgetIntent = new Intent("com.android.launcher.action.ADD_WIDGET");
							widgetIntent.putExtra("appwidget_provider", MemoAppWidgetProvider.COMPONENT);
							widgetIntent.putExtra("note_path", note.mThumbnailUri);
							widgetIntent.putExtra("note_id", note.mId);
							sendBroadcast(widgetIntent);
						}
					}
				};
				mSaveNoteAsyncTask.execute();
			}
			break;
		}
		//Added by yongan.qiu on 2012.3.20 end.
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.note_editor, menu);
		return true;//super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		adjustForOrientation(newConfig.orientation);
	}

	private void adjustForOrientation(int orientation) {
		CheckBox moveCheckBox = (CheckBox) findViewById(R.id.move);

		mNoteView.setPivotX(0);
		mNoteView.setPivotY(0);

		float scale = 1.0f;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			scale = Float.valueOf(getResources().getString(
					R.string.note_view_scale));

			moveCheckBox.setEnabled(true);
		} else {
			mScrollView.setAllowScroll(false);

			moveCheckBox.setChecked(false);
			moveCheckBox.setEnabled(false);
		}

		Resources res = getResources();
		LinearLayout fillLayout = (LinearLayout) findViewById(R.id.fill_layout);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fillLayout
				.getLayoutParams();
		params.width = Math.round(res
				.getDimensionPixelSize(R.dimen.note_view_width) * scale);
		params.height = Math.round(res
				.getDimensionPixelSize(R.dimen.note_view_height) * scale);
		fillLayout.setLayoutParams(params);

		mNoteView.setScaleX(scale);
		mNoteView.setScaleY(scale);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_SAVE_NOTE:
			return createProgressHintDialog(getResources().getString(
					R.string.saving_note));
		case DIALOG_DISCARD_NOTE:
			return createDiscardNoteDialog();
		case DIALOG_CREATE_GROUP:
			return createCreateGroupDialog();
		case DIALOG_SAVE_GROUP:
			return createProgressHintDialog(getResources().getString(
					R.string.saving_group));
		case DIALOG_UPDATE_NOTE_GROUP:
			return createProgressHintDialog(getResources().getString(
					R.string.updating_note_group));
		case DIALOG_ADD_IMAGE:
			return createAddImageDialog();
		case DIALOG_LOAD_NOTE:
			return createProgressHintDialog(getResources().getString(
					R.string.loading_note));
		case DIALOG_ADD_LABEL:
			return createAddLabelDialog();
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_TAKE_IMAGE: {
				//Canceled by gangzhou.qi at 2012-8-3 Begin
				/*
				 * File file = new File(getExternalCacheDir().getAbsolutePath() + "/.temp.jpg");
				 */
				//Canceled by gangzhou.qi at 2012-8-3 End
				//Modified by gangzhou.qi at 2012-8-3 Begin
				File file = new File(getExternalCacheDir().getAbsolutePath() + "/." + mCurrentCacheImageIndex +".jpg");
				//Modified by gangzhou.qi at 2012-8-3 End
				if (data == null) {
					data = new Intent();
				}
				try {
					data.setData(Uri.fromFile(file));
					handleTakeOrPickImage(data);
				} catch(Exception e) {
					Toast.makeText(this, R.string.add_or_load_image_error,
							Toast.LENGTH_SHORT).show();
					Log.d(TAG, e.toString(), e);
				}
				break;
			}
			case REQUEST_CODE_PICK_IMAGE: {
				handleTakeOrPickImage(data);
				break;
			}
			case REQUEST_CODE_CHOOSE_BG: {
				mNoteBgResId = data.getIntExtra(Constants.EXTRA_RES_ID,
						Constants.DEFAULT_NOTE_BG_RES_ID);
				mNoteView.getNote().mBgImageResId = mNoteBgResId;
				mNoteView.setNoteBg(mNoteBgResId);
				setNoteBgTopBg(mNoteBgResId);
				break;
			}
			case REQUEST_CODE_CROP_MAP: {
				if (data == null) {
					data = new Intent();
				}
				Bundle bundle = data.getExtras();
				String filename = bundle.getString("filename");
				File file = new File(filename);
				try {
					data.setData(Uri.fromFile(file));
					handleTakeOrPickImage(data);
				} catch(Exception e) {
					Toast.makeText(this, R.string.add_or_load_image_error,
							Toast.LENGTH_SHORT).show();
					Log.d(TAG, e.toString(), e);
				}
				break;
			}
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		dismissAllWin();

		if (mLoadNoteAsyncTask != null) {
			mLoadNoteAsyncTask.cancel(true);
		}
		if (mUpdateNoteGroupAsyncTask != null) {
			mUpdateNoteGroupAsyncTask.cancel(true);
		}
		if (mCreateGroupAsyncTask != null) {
			mCreateGroupAsyncTask.cancel(true);
		}
		if (mSaveNoteAsyncTask != null) {
			mSaveNoteAsyncTask.cancel(true);
		}
		
		//Added by yongan.qiu on 2012.3.20 begin.
		unregisterReceiver(mAddWidgetResultReceiver);
		//Added by yongan.qiu on 2012.3.20 end.
	}

	private void initActionBar() {
		final View customView = getLayoutInflater().inflate(
				R.layout.note_editor_action_bar_view, null);

		RadioButton radioButton = (RadioButton) customView
				.findViewById(R.id.insert_mode);
		radioButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dismissPaintSettingWin();
				dismissEraserSettingWin();
				if (view.getTag() != null) {
					dismissEraserSettingWin();
					dismissPaintSettingWin();
					if (mFontSettingWin == null || !mFontSettingWin.isShowing()) {
						showFontSettingWin();
					}else {
						dismissFontSettingWin();
					}
				} else {
					view.setTag(0);
				}
				mNoteView.setNoteMode(NoteView.NOTE_MODE_INSERT);

				RadioButton rb = (RadioButton) customView
						.findViewById(R.id.paint_mode);
				rb.setTag(null);
				rb.setChecked(false);

				rb = (RadioButton) customView.findViewById(R.id.eraser);
				rb.setTag(null);
				rb.setChecked(false);
				rb.setEnabled(false);

				customView.findViewById(R.id.undo).setEnabled(false);
				customView.findViewById(R.id.redo).setEnabled(false);
			}
		});

		radioButton = (RadioButton) customView.findViewById(R.id.paint_mode);
		radioButton.setChecked(true);
		radioButton.setTag(0);
		radioButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dismissFontSettingWin();
				dismissEraserSettingWin();
				if (view.getTag() != null) {
					if (mPaintSettingWin == null
							|| !mPaintSettingWin.isShowing()) {
						showPaintSettingWin();
					} else {
						dismissPaintSettingWin();
					}
				} else {
					view.setTag(0);
				}
				mNoteView.setNoteMode(NoteView.NOTE_MODE_PAINT);

				RadioButton rb = (RadioButton) customView
						.findViewById(R.id.insert_mode);
				rb.setTag(null);
				rb.setChecked(false);
				rb = (RadioButton) customView.findViewById(R.id.eraser);
				rb.setTag(null);
				rb.setChecked(false);
				rb.setEnabled(true);

				customView.findViewById(R.id.undo).setEnabled(
						mNoteView.getPaintView().canUndo());
				customView.findViewById(R.id.redo).setEnabled(
						mNoteView.getPaintView().canRedo());
			}
		});

		radioButton = (RadioButton) customView.findViewById(R.id.eraser);
		radioButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dismissFontSettingWin();
				dismissPaintSettingWin();
				if (view.getTag() != null) {
					if (mEraserSettingWin == null
							|| !mEraserSettingWin.isShowing()) {
						showEraserSettingWin();
					} else {
						dismissEraserSettingWin();
					}
				} else {
					view.setTag(0);
				}
				mNoteView.setNoteMode(NoteView.NOTE_MODE_ERASE);

				RadioButton rb = (RadioButton) customView
						.findViewById(R.id.insert_mode);
				rb.setTag(null);
				rb.setChecked(false);
				rb = (RadioButton) customView.findViewById(R.id.paint_mode);
				rb.setTag(null);
				rb.setChecked(false);
			}
		});

		View view = customView.findViewById(R.id.undo);
		view.setEnabled(false);
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PaintView pv = mNoteView.getPaintView();
				pv.undo();
				v.setEnabled(pv.canUndo());
			}
		});

		view = customView.findViewById(R.id.redo);
		view.setEnabled(false);
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PaintView pv = mNoteView.getPaintView();
				pv.redo();
				v.setEnabled(pv.canRedo());
			}
		});

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(customView);
		actionBar.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.note_action_bar_bg));
	}

	public void handleTakeOrPickImage(Intent data) {
		File file = null;
		Bitmap bmp = data.getParcelableExtra("data");
		if (bmp != null) {
			//Canceled by gangzhou.qi at 2012-8-3 Begin
			/*
			 * File file = new File(getExternalCacheDir().getAbsolutePath() + "/.temp.jpg");
			 */
			//Canceled by gangzhou.qi at 2012-8-3 End
			//Modified by gangzhou.qi at 2012-8-3 Begin
			file = new File(getExternalCacheDir().getAbsolutePath() + "/." +mCurrentCacheImageIndex +".jpg");
			//Modified by gangzhou.qi at 2012-8-3 End
			BitmapUtils.saveToFile(bmp, file);
			bmp.recycle();
		} else {
			String path = null;
			Uri uri = data.getData();
			if (uri != null) {
				String scheme = uri.getScheme();
				if ("file".equalsIgnoreCase(scheme)) {
					path = uri.getEncodedPath();
				} else if ("content".equalsIgnoreCase(scheme)) {
					Cursor cursor = getContentResolver().query(uri,
							new String[] { "_data" }, null, null, null);
					if (cursor != null) {
						if (cursor.moveToNext()) {
							path = cursor.getString(0);
						}
						cursor.close();
					}
				}
			}
			if (path != null) {
				file = new File(path);
			}
		}

		if (file != null) {
			mNoteView.setNoteMode(NoteView.NOTE_MODE_INSERT);
			mNoteView.addImage(file, true);
		}
	}

	public Dialog createAddImageDialog() {
		final Context dialogContext = new ContextThemeWrapper(this,
				android.R.style.Theme_Holo_Light);

		String[] choices = new String[2];
		choices[0] = getString(R.string.image);
		choices[1] = getString(R.string.take_image);
//		choices[2] = getString(R.string.map);

		final ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
				android.R.layout.simple_list_item_1, choices);

		final AlertDialog.Builder builder = new AlertDialog.Builder(
				dialogContext);
		builder.setTitle(R.string.add_image);
		builder.setSingleChoiceItems(adapter, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switch (which) {
						case 0:
							pickPhoto();
							break;
						case 1:
							takeImage();
							break;
//						case 2:
//							cropMap();
//							break;
						}
					}
				});
		return builder.create();
	}

	private Dialog createDiscardNoteDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light));
		builder.setTitle(R.string.cancel);
		builder.setMessage(R.string.cancel_note_msg);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Note.Audio audio = mNoteView.getAudio();
						if (audio != null && audio.mId <= 0 && audio.mUri != null) {
							new File(audio.mUri).delete();
						}
						if (needCreateGroup()) {
							showDialog(DIALOG_CREATE_GROUP);
						} else {
							finish();
						}
					}
				});
		return builder.create();
	}

	private Dialog createCreateGroupDialog() {
		final View view = getLayoutInflater()
				.inflate(R.layout.group_name, null);

		((EditText) view.findViewById(R.id.group_name)).setText(DateUtils
				.formatTimeStampString(this, System.currentTimeMillis(), true));

		final AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light));
		builder.setTitle(R.string.save_group);
		builder.setView(view);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismissAllWin();
				finish();
			}
		});
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismissAllWin();
						final EditText groupName = (EditText) view
								.findViewById(R.id.group_name);
						mCreateGroupAsyncTask = new CreateGroupAsyncTask() {
							@Override
							protected void onPostExecute(Long result) {
								super.onPostExecute(result);
								if(!mNewInsertNoteIdList.isEmpty()) {
									mUpdateNoteGroupAsyncTask = new UpdateNoteGroupAsyncTask() {
										@Override
										protected void onPostExecute(Integer result) {
											super.onPostExecute(result);
											if(mReturnNotePreview) {
												Intent intent = new Intent(NoteEditor.this, NotePreview.class);
												intent.putExtra(Constants.EXTRA_NOTE_ID, mNewInsertNoteIdList.get(mNewInsertNoteIdList.size() - 1));
												intent.putExtra(Constants.EXTRA_NOTE_INDEX, mNoteIndex - 1);
												startActivity(intent);
											}
											finish();
										}
									};
									mUpdateNoteGroupAsyncTask.execute(result);
								} else {
									if(mReturnNotePreview) {
										ContentResolver resolver = getContentResolver();
										Cursor cursor = resolver.query(Note.CONTENT_URI, null,
												Note.COLUMN_GROUP_ID + "=" + result.longValue(), null,
												Note.COLUMN_ID);
										if(cursor != null) {
											if(cursor.moveToLast()) {
												Intent intent = new Intent(NoteEditor.this, NotePreview.class);
												intent.putExtra(Constants.EXTRA_NOTE_ID, cursor.getLong(cursor.getColumnIndex(Note.COLUMN_ID)));
												intent.putExtra(Constants.EXTRA_NOTE_INDEX, mNoteIndex - 1);
												startActivity(intent);
											}
											cursor.close();
										}
									}
									finish();
								}
							}
						};
						mCreateGroupAsyncTask.execute(groupName.getText().toString());
					}
				});
		return builder.create();
	}

	private Dialog createAddLabelDialog() {
		final View view = getLayoutInflater().inflate(R.layout.add_label, null);

		final AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light));
		builder.setTitle(TextUtils.isEmpty(mNoteView.getNote().mLabel) ? R.string.add_label
				: R.string.modify_label);
		builder.setView(view);
		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						dialog.cancel();
					}
				});

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText editText = (EditText) view
								.findViewById(R.id.label_name);
						mNoteView.getNote().mLabel = editText.getText()
								.toString();
						((TextView) NoteEditor.this.findViewById(R.id.label))
								.setText(mNoteView.getNote().mLabel);
					}
				});
		
		Dialog dialog = builder.create();
		dialog.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				((Dialog) dialog).setTitle(TextUtils.isEmpty(mNoteView
						.getNote().mLabel) ? R.string.add_label
						: R.string.modify_label);
				((EditText) view.findViewById(R.id.label_name))
						.setText(mNoteView.getNote().mLabel);
			}
		});
		return dialog;
	}

	private Dialog createProgressHintDialog(CharSequence message) {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setMessage(message);
		return dialog;
	}

	protected void takeImage() {
		try {
			//Canceled by gangzhou.qi at 2012-8-3 Begin
			/*
			 * File file = new File(getExternalCacheDir().getAbsolutePath() + "/.temp.jpg");
			 */
			//Canceled by gangzhou.qi at 2012-8-3 End
			//Modified by gangzhou.qi at 2012-8-3 Begin
			File file = new File(getExternalCacheDir().getAbsolutePath() + "/.0.jpg");
			for(int i = 0 ; i < mCacheImageListLimited ; i ++){
				if(mCacheImageList != null && !mCacheImageList.contains(i)){
					mCacheImageList.add(i);
					file = new File(getExternalCacheDir().getAbsolutePath() + "/." + i + ".jpg");
					mCurrentCacheImageIndex = i;
					break;
				}
			}
			//Modified by gangzhou.qi at 2012-8-3 End
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(intent, REQUEST_CODE_TAKE_IMAGE);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.image_picker_not_found,
					Toast.LENGTH_SHORT).show();
		}
	}

	protected void pickPhoto() {
		try {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
			intent.setType("image/*");
			startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.image_picker_not_found,
					Toast.LENGTH_SHORT).show();
		}
	}

	protected void cropMap() {
		try {
			Intent intent = new Intent("com.tcl.memo.GoogleMap");
			startActivityForResult(intent, REQUEST_CODE_CROP_MAP);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.image_picker_not_found,
					Toast.LENGTH_SHORT).show();
		}
	}

	private boolean needCreateGroup() {
		boolean needCreateGroup = false;
		long groupId = mNoteView.getNote().mGroupId;
		if(groupId == 0) {
			long noteId = getIntent().getLongExtra(Constants.EXTRA_NOTE_ID, 0);
			if(noteId != 0 && !mNewInsertNoteIdList.isEmpty()) {
				needCreateGroup = true;
			} else if(noteId == 0 && mNewInsertNoteIdList.size() > 1) {
				needCreateGroup = true;
			}
		}
		return needCreateGroup;
	}
	
	private boolean isNoteBlank() {
		boolean isBlank = mNoteView.isBlank();
//		if(isBlank) {
//			isBlank = TextUtils.isEmpty(((EditText)findViewById(R.id.note_title)).getText());
//		}
//		if(isBlank) {
//			isBlank = !((CheckBox) findViewById(R.id.add_star)).isChecked();
//		}
//		if(isBlank) {
//			isBlank = TextUtils.isEmpty(((TextView) findViewById(R.id.label)).getText());
//		}
		return isBlank;
	}
	
	//add by lijuan.li at 2012.03.21 begin
	private void showFontSettingWin() {
		final FontSettingView fontSettingView = new FontSettingView(this);
		fontSettingView
				.setOnClickCloseListener(new FontSettingView.OnClickCloseListener() {
					@Override
					public void onClickClose(View v) {
						dismissFontSettingWin();
					}
				});
		fontSettingView.setOnFontChangeListener(new FontSettingView.OnFontChangeListener() {
			@Override
			public void onFontChange(String fontName, int fontStyle) {
				ZoomController zoomController = mNoteView.getZoomController();
				if(zoomController.isShowBorder()) {
					View zoomView = zoomController.getZoomView();
					if(zoomView != null && (zoomView instanceof CEditText)) {
						((CEditText)zoomView).setTypeface(fontName, fontStyle);
					}
				}
			}
		});
		fontSettingView.setOnColorChangeListener(new FontSettingView.OnColorChangeListener(){
			@Override
			public void onColorChange(int fontColor) {
				ZoomController zoomController = mNoteView.getZoomController();
				if(zoomController.isShowBorder()) {
					View zoomView = zoomController.getZoomView();
					if(zoomView != null && (zoomView instanceof EditText)) {
						((CEditText)zoomView).setTextColor(fontColor);
					}
				}
			}});
		
		FontSetting fontSetting = SettingUtils.getFontSetting(NoteEditor.this);
		fontSettingView.setFontNameAndStyle(fontSetting.mFontName, fontSetting.mFontStyle);
		fontSettingView.setFontColor(fontSetting.mFontColor, fontSetting.mFontColorX, fontSetting.mFontColorY, fontSetting.mExitFlag);
		fontSettingView.setExitFlag(fontSetting.mExitFlag);
		
		mFontSettingWin = new PopupWindow(this);
		mFontSettingWin.setWidth(530);
		mFontSettingWin.setHeight(isLandscape() ? 590 : 750);
		mFontSettingWin.setBackgroundDrawable(new ColorDrawable(
				Color.TRANSPARENT));
		fontSettingView.adjustScrollViewSize(mFontSettingWin.getWidth(), isLandscape() ? 488 : LinearLayout.LayoutParams.WRAP_CONTENT);
		mFontSettingWin.setContentView(fontSettingView);
		mFontSettingWin.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				ZoomController zoomController = mNoteView.getZoomController();
				if(!zoomController.isShowBorder()) {
					FontSetting fontSetting = new FontSetting();
					fontSetting.mFontName = fontSettingView.getFontName();
					fontSetting.mFontStyle = fontSettingView.getFontStyle();
					fontSetting.mFontColor = fontSettingView.getFontColor();
					fontSetting.mFontColorX = fontSettingView.getFontColorX();
					fontSetting.mFontColorY = fontSettingView.getFontColorY();
					fontSetting.mExitFlag = fontSettingView.getExitFlag();
					SettingUtils.saveFontSetting(NoteEditor.this, fontSetting);
				}
			}
		});
		mFontSettingWin.showAsDropDown(getActionBar().getCustomView()
				.findViewById(R.id.insert_mode), 4, -4);
	}
	
	private void dismissFontSettingWin() {
		if (mFontSettingWin != null && mFontSettingWin.isShowing()) {
			mFontSettingWin.dismiss();
		}
	}

	private void showPaintSettingWin() {
		final PaintSettingView paintSettingView = new PaintSettingView(this);
		paintSettingView
				.setOnClickCloseListener(new PaintSettingView.OnClickCloseListener() {
					@Override
					public void onClickClose(View v) {
						dismissPaintSettingWin();
					}
				});
		paintSettingView
				.setOnSizeChangeListener(new PaintSettingView.OnSizeChangeListener() {
					@Override
					public void onSizeChange(int size) {
						mNoteView.getPaintView().setStrokeSize(size);
					}
				});
		paintSettingView
				.setOnAlphaChangeListener(new PaintSettingView.OnAlphaChangeListener() {
					@Override
					public void onAlphaChange(int alpha) {
						mNoteView.getPaintView().setStrokeAlpha(alpha);
					}
				});
		paintSettingView
				.setOnColorChangeListener(new PaintSettingView.OnColorChangeListener() {
					@Override
					public void onColorChange(int color) {
						mNoteView.getPaintView().setStrokeColor(color);
					}
				});
		
		PaintSetting paintSetting = SettingUtils.getPaintSetting(NoteEditor.this);
		paintSettingView.setPaintSize(paintSetting.mPaintSize);
		paintSettingView.setPaintAlpha(paintSetting.mPaintAlpha);
		paintSettingView.setPaintColor(paintSetting.mPaintColor, paintSetting.mPaintColorX,paintSetting.mPaintColorY, paintSetting.mExitFlag);
		paintSettingView.setExitFlag(paintSetting.mExitFlag);
		
		mPaintSettingWin = new PopupWindow(this);
		mPaintSettingWin.setWidth(530);
		mPaintSettingWin.setHeight(isLandscape() ? 590 : 720);
		mPaintSettingWin.setBackgroundDrawable(new ColorDrawable(
				Color.TRANSPARENT));
		paintSettingView.adjustScrollViewSize(mPaintSettingWin.getWidth(), isLandscape() ? 488 : LinearLayout.LayoutParams.WRAP_CONTENT);
		mPaintSettingWin.setContentView(paintSettingView);
		mPaintSettingWin.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				PaintSetting paintSetting = new PaintSetting();
				paintSetting.mPaintSize = paintSettingView.getPaintSize();
				paintSetting.mPaintAlpha = paintSettingView.getPaintAlpha();
				paintSetting.mPaintColor = paintSettingView.getPaintColor();
				paintSetting.mPaintColorX = paintSettingView.getPaintColorX();
				paintSetting.mPaintColorY = paintSettingView.getPaintColorY();
				paintSetting.mExitFlag = paintSettingView.getExitFlag();
				SettingUtils.savePaintSetting(NoteEditor.this, paintSetting);
			}
		});
		mPaintSettingWin.showAsDropDown(getActionBar().getCustomView()
				.findViewById(R.id.paint_mode), -73, -4);
	}
	
	
	private void dismissPaintSettingWin() {
		if (mPaintSettingWin != null && mPaintSettingWin.isShowing()) {
			mPaintSettingWin.dismiss();
		}
	}
	//add by lijuan.li at 2012.03.21 end

	private void showEraserSettingWin() {
		final EraserSettingView eraserSettingView = new EraserSettingView(this);
		eraserSettingView
				.setOnClickCloseListener(new EraserSettingView.OnClickCloseListener() {
					@Override
					public void onClickClose(View v) {
						dismissEraserSettingWin();
					}
				});
		eraserSettingView
				.setOnClickClearAllListener(new EraserSettingView.OnClickClearAllListener() {
					@Override
					public void onClickClearAll(View v) {
						mNoteView.getPaintView().clearAll(true);
						dismissEraserSettingWin();
					}
				});
		eraserSettingView
				.setOnEraserSizeChangeListener(new EraserSettingView.OnEraserSizeChangeListener() {
					@Override
					public void onEraserSizeChange(int size) {
						mNoteView.getPaintView().setEraserSize(size);
					}
				});
		
		eraserSettingView.setEraserSize(SettingUtils.getEraserSetting(this).mEraserSize);

		if (mEraserSettingWin != null) {
			mEraserSettingWin.dismiss();
		}
		mEraserSettingWin = new PopupWindow(this);
		mEraserSettingWin.setWidth(477);
		mEraserSettingWin.setHeight(305);
		mEraserSettingWin.setBackgroundDrawable(new ColorDrawable(
				Color.TRANSPARENT));
		mEraserSettingWin.setContentView(eraserSettingView);
		mEraserSettingWin.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				EraserSetting eraserSetting = new EraserSetting();
				eraserSetting.mEraserSize = eraserSettingView.getEraserSize();
				SettingUtils.saveEraserSetting(NoteEditor.this, eraserSetting);
			}
		});
		mEraserSettingWin.showAsDropDown(getActionBar().getCustomView()
				.findViewById(R.id.eraser), -150, -4);
	}

	private void dismissEraserSettingWin() {
		if (mEraserSettingWin != null && mEraserSettingWin.isShowing()) {
			mEraserSettingWin.dismiss();
		}
	}
	
	private void showAudioWin() {
		final AudioView audioView = new AudioView(this) {
			@Override
			protected void onDeleteRecord() {
				super.onDeleteRecord();
				((ImageButton) NoteEditor.this.findViewById(R.id.add_audio))
						.setImageResource(R.drawable.add_audio);
				mNoteView.setAudio(null);
				dismissAudioWin();
			}

			@Override
			protected void onReplaceRecord() {
				super.onReplaceRecord();
				((ImageButton) NoteEditor.this.findViewById(R.id.add_audio))
						.setImageResource(R.drawable.add_audio);
				mNoteView.setAudio(null);
			}

			@Override
			protected void onStopRecord() {
				super.onStopRecord();
				((ImageButton) NoteEditor.this.findViewById(R.id.add_audio))
						.setImageResource(R.drawable.play_audio);
			}
		};
		audioView
				.setOnAudioChangeListener(new AudioView.OnAudioChangeListener() {
					@Override
					public void onAudioChange(Note.Audio audio) {
						mNoteView.setAudio(audio);
					}
				});
		Note.Audio audio = mNoteView.getAudio();
		if (audio != null) {
			audioView.setInitAudio(audio);
			audioView.changeState(AudioView.STATE_START_PLAY_RECORD);
		}

		mAudioWin = new PopupWindow(this);
		mAudioWin.setOnDismissListener(new OnDismissListener(){
			@Override
			public void onDismiss() {
				audioView.stopPlay();
				audioView.stopRecord();
			}});
		mAudioWin.setWidth(480);
		mAudioWin.setHeight(190);
		mAudioWin.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		mAudioWin.setContentView(audioView);
		mAudioWin.showAsDropDown(findViewById(R.id.add_audio), -330, 0);
	}

	private void dismissAudioWin() {
		if (mAudioWin != null && mAudioWin.isShowing()) {
			mAudioWin.dismiss();
		}
	}
	
	private void dismissAllWin() {
		dismissAudioWin();
		dismissFontSettingWin();
		dismissPaintSettingWin();
		dismissEraserSettingWin();
	}

	private class SaveNoteAsyncTask extends AsyncTask<Void, Integer, Long> {
		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_SAVE_NOTE);
		}

		@Override
		protected Long doInBackground(Void... params) {
			return saveMemo();
		}

		@Override
		protected void onPostExecute(Long result) {
			dismissDialog(DIALOG_SAVE_NOTE);
		}
		
		private long saveMemo() {
			ContentResolver resolver = getContentResolver();
			Note note = saveNote(resolver);
			savePaint(note.mId, resolver);
			saveAudio(note.mId, resolver);
			saveText(note.mId, resolver);
			saveImage(note.mId, resolver);
			saveImage(note.mId, resolver);

			//Added by yongan.qiu on 2012.3.21 begin. (send update widget broadcast if needed.)
			SharedPreferences noteWidgetMap = NoteEditor.this.getSharedPreferences("note_widget_map",Context.MODE_PRIVATE);
			int appWidgetId = noteWidgetMap.getInt(note.mThumbnailUri, -1);
			Log.i(TAG, "saveNote(): appWidgetId = " + appWidgetId);
			if (appWidgetId >= 0) {//widget may exist.
				if (AppWidgetManager.getInstance(NoteEditor.this).getAppWidgetInfo(appWidgetId) != null) {
					Intent widgetIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
					widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
					widgetIntent.setComponent(MemoAppWidgetProvider.COMPONENT);
					sendBroadcast(widgetIntent);
				} else {//widget not exists indeed.
					//remove from SharedPreferences.
					Log.w(TAG, "saveNote(): can not found appWidgetId " + appWidgetId + 
							" in AppWidgetManager, so remove it from SharedPreferences");
					noteWidgetMap.edit().remove(note.mThumbnailUri).commit();
					SharedPreferences widgetNoteMap = NoteEditor.this.getSharedPreferences("widget_note_map",Context.MODE_PRIVATE);
					widgetNoteMap.edit().remove(String.valueOf(appWidgetId)).commit();
					SharedPreferences widgetDbMap = NoteEditor.this.getSharedPreferences("widget_db_map",Context.MODE_PRIVATE);
					widgetDbMap.edit().remove(String.valueOf(appWidgetId)).commit();
				}
			}
			//Added by yongan.qiu on 2012.3.21 end.
			
			return note.mId;
		}
		
		private Note saveNote(ContentResolver resolver) {
			Note note = mNoteView.getNote();
			note.mTitle = ((EditText) findViewById(R.id.note_title)).getText()
					.toString();
			if(TextUtils.isEmpty(note.mTitle)) {
				note.mTitle = null;
			}
			if (note.mThumbnailUri == null) {
				File file = mNoteView
						.exportThumbnailToDirection(Constants.THUMBNAIL_DIR);
				if (file != null) {
					note.mThumbnailUri = file.getAbsolutePath();
				}
			} else {
				mNoteView.exportThumbnailToFile(new File(note.mThumbnailUri));
			}
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 4;
			Bitmap bmp = BitmapFactory.decodeFile(note.mThumbnailUri, opts);
			if(note.mSmallThumbnailUri == null) {
				File file = BitmapUtils.saveToDirectory(bmp, Constants.THUMBNAIL_DIR, false);
				if (file != null) {
					note.mSmallThumbnailUri = file.getAbsolutePath();
				}
			} else {
				BitmapUtils.saveToFile(bmp, new File(note.mSmallThumbnailUri));
			}
			bmp.recycle();
			if (note.mId == 0) {
				Uri uri = resolver.insert(Note.CONTENT_URI, note.toContentValues());
				if(uri != null) {
					note.mId = Long.valueOf(uri.getLastPathSegment());
				}
				mNewInsertNoteIdList.add(note.mId);
			} else {
				resolver.update(Note.CONTENT_URI, note.toContentValues(), Note.COLUMN_ID + "=" + note.mId, null);
			}
			return note;
		}
		
		private Note.Paint savePaint(long noteId, ContentResolver resolver) {
			Note.Paint paint = mNoteView.getPaint();
			if (paint != null) {
				if (paint.mUri == null) {
					File file = mNoteView
							.exportPaintToDirection(Constants.PAINT_DIR);
					if (file != null) {
						paint.mUri = file.getAbsolutePath();
					}
				} else {
					mNoteView.exportPaintToFile(new File(paint.mUri));
				}
				paint.mNoteId = noteId;
				if (paint.mId == 0) {
					Uri uri = resolver.insert(Note.Paint.CONTENT_URI, paint.toContentValues());
					if(uri != null) {
						paint.mId = Long.valueOf(uri.getLastPathSegment());
					}
				} else {
					resolver.update(Note.Paint.CONTENT_URI, paint.toContentValues(), Note.Paint.COLUMN_ID + "="
							+ paint.mId, null);
				}
			} else {
				Cursor cursor = resolver.query(Note.Paint.CONTENT_URI,
						new String[] { Note.Paint.COLUMN_URI },
						Note.Paint.COLUMN_NOTE_ID + "=" + noteId, null, null);
				if (cursor != null) {
					File file;
					String path;
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
					resolver.delete(Note.Paint.CONTENT_URI, Note.Paint.COLUMN_NOTE_ID + "="
							+ noteId, null);
				}
			}
			return paint;
		}
		
		private Note.Audio saveAudio(long noteId, ContentResolver resolver) {
			Note.Audio audio = mNoteView.getAudio();
			if (audio != null && audio.mUri != null) {
				Note.Audio savedAudio = null;
				Cursor cursor = resolver.query(Note.Audio.CONTENT_URI, null,
						Note.Audio.COLUMN_NOTE_ID + "=" + noteId, null, null);
				if (cursor != null) {
					if (cursor.moveToNext()) {
						savedAudio = NoteUtils.toAudio(cursor);
					}
					cursor.close();
				}
				if (savedAudio == null || !audio.mUri.equals(savedAudio.mUri)) {
					audio.mNoteId = noteId;
					if (savedAudio != null) {
						new File(savedAudio.mUri).delete();
						audio.mId = savedAudio.mId;
						resolver.update(Note.Audio.CONTENT_URI, audio.toContentValues(), Note.Audio.COLUMN_ID + "="
								+ audio.mId, null);
					} else {
						Uri uri = resolver.insert(Note.Audio.CONTENT_URI, audio.toContentValues());
						if(uri != null) {
							audio.mId = Long.valueOf(uri.getLastPathSegment());
						}
					}
				}
			} else {
				Cursor cursor = resolver.query(Note.Audio.CONTENT_URI,
						new String[] { Note.Audio.COLUMN_URI },
						Note.Audio.COLUMN_NOTE_ID + "=" + noteId, null, null);
				if (cursor != null) {
					File file;
					String path;
					if (cursor.moveToNext()) {
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
					resolver.delete(Note.Audio.CONTENT_URI, Note.Audio.COLUMN_NOTE_ID + "="
							+ noteId, null);
				}
			}
			return audio;
		}
		
		private List<Note.Text> saveText(long noteId, ContentResolver resolver) {
			ContentProviderOperation.Builder builder;
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			
			Note.Text text;
			List<Note.Text> textList = mNoteView.getTextList();
			int size = textList.size();
			for (int i = 0; i < size; i++) {
				builder = null;
				text = textList.get(i);
				text.mNoteId = noteId;
				if (text.mId == 0) {
					if (text.mContent != null
							&& text.mContent.trim().length() != 0) {
						builder = ContentProviderOperation
								.newInsert(Note.Text.CONTENT_URI);
					}
				} else {
					if (text.mContent != null
							&& text.mContent.trim().length() != 0) {
						builder = ContentProviderOperation
								.newUpdate(Note.Text.CONTENT_URI);
					} else {
						builder = ContentProviderOperation
								.newDelete(Note.Text.CONTENT_URI);
					}
					builder.withSelection(Note.Text.COLUMN_ID + "=" + text.mId,
							null);
				}
				if (builder != null) {
					builder.withValues(text.toContentValues());
					operations.add(builder.build());
				}
			}
			
			if (!operations.isEmpty()) {
				try {
					ContentProviderResult[] results = resolver.applyBatch(MemoProvider.URI_AUTHORITY, operations);
					if(results != null) {
						for(int i = 0; i < results.length; i++) {
							if(results[i] != null && results[i].uri != null) {
								textList.get(i).mId = Long.valueOf(results[i].uri.getLastPathSegment());
							}
						}
					}
				} catch (Exception e) {
					Log.d(TAG, e.toString(), e);
				}
			}
			
			size = mDeletedTextList.size();
			if (size > 0) {
				operations.clear();
				for (int i = 0; i < size; i++) {
					//B JiangzhouQ 2012/04/26 For Bug 265140
//					text = mDeletedTextList.remove(i);
					text = mDeletedTextList.remove(0);
					//E JiangzhouQ 2012/04/26 For Bug 265140
					if (text != null && text.mId != 0) {
						builder = ContentProviderOperation
								.newDelete(ContentUris.withAppendedId(
										Note.Text.CONTENT_URI, text.mId));
						operations.add(builder.build());
					}
				}
				if (!operations.isEmpty()) {
					try {
						resolver.applyBatch(MemoProvider.URI_AUTHORITY, operations);
					} catch (Exception e) {
						Log.d(TAG, e.toString(), e);
					}
				}
			}
			
			return textList;
		}
		
		private List<Note.Image> saveImage(long noteId, ContentResolver resolver) {
			ContentProviderOperation.Builder builder;
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			
			Note.Image image;
			List<Note.Image> imageList = mNoteView.getImageList();
			int size = imageList.size();
			for (int i = 0; i < size; i++) {
				image = imageList.get(i);
				image.mNoteId = noteId;
				if (image.mId == 0) {
					if (image.mUri != null
							&& !image.mUri.startsWith(Constants.IMAGE_DIR
									.getAbsolutePath())) {
						File file = FileUtils.copyToDirection(new File(
								image.mUri), Constants.IMAGE_DIR);
						if (file != null) {
							image.mUri = file.getAbsolutePath();
						}
					}
					builder = ContentProviderOperation
							.newInsert(Note.Image.CONTENT_URI);
				} else {
					builder = ContentProviderOperation
							.newUpdate(Note.Image.CONTENT_URI);
					builder.withSelection(Note.Image.COLUMN_ID + "="
							+ image.mId, null);
				}
				builder.withValues(image.toContentValues());
				operations.add(builder.build());
			}

			if (!operations.isEmpty()) {
				try {
					ContentProviderResult[] results = resolver.applyBatch(MemoProvider.URI_AUTHORITY, operations);
					if(results != null) {
						for(int i = 0; i < results.length; i++) {
							if(results[i] != null && results[i].uri != null) {
								imageList.get(i).mId = Long.valueOf(results[i].uri.getLastPathSegment());
							}
						}
					}
				} catch (Exception e) {
					Log.d(TAG, e.toString(), e);
				}
			}
			
			size = mDeletedImageList.size();
			if (size > 0) {
				operations.clear();
				Uri uri;
				File file;
				String path;
				Cursor cursor;
				String[] projection = new String[] { Note.Image.COLUMN_URI };
				for (int i = 0; i < size; i++) {
					image = mDeletedImageList.remove(0);
					if (image != null && image.mId != 0) {
						uri = ContentUris.withAppendedId(
								Note.Image.CONTENT_URI, image.mId);
						cursor = resolver.query(uri, projection, null, null,
								null);
						if (cursor != null) {
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
						}
						builder = ContentProviderOperation.newDelete(uri);
						operations.add(builder.build());
					}
				}
				if (!operations.isEmpty()) {
					try {
						resolver.applyBatch(MemoProvider.URI_AUTHORITY, operations);
					} catch (Exception e) {
						Log.d(TAG, e.toString(), e);
					}
				}
			}
			return imageList;
		}
	}

	private class CreateGroupAsyncTask extends AsyncTask<String, Integer, Long> {

		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_SAVE_GROUP);
		}

		@Override
		protected Long doInBackground(String... params) {
			ContentResolver resolver = getContentResolver();

			Note.Group group = new Note.Group();
			group.mName = params[0];

			Uri uri = resolver.insert(Note.Group.CONTENT_URI,
					group.toContentValues());

			long groupId = Long.valueOf(uri.getLastPathSegment());
			mNoteView.getNote().mGroupId = groupId;
			return groupId;
		}

		@Override
		protected void onPostExecute(Long result) {
			dismissDialog(DIALOG_SAVE_GROUP);
		}
	}

	private class UpdateNoteGroupAsyncTask extends
			AsyncTask<Long, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_UPDATE_NOTE_GROUP);
		}

		@Override
		protected Integer doInBackground(Long... params) {
			if (!mNewInsertNoteIdList.isEmpty()) {
				ContentResolver resolver = getContentResolver();

				StringBuilder selection = new StringBuilder();
				selection.append(Note.COLUMN_ID).append(" IN(");
				
				long noteId = getIntent().getLongExtra(Constants.EXTRA_NOTE_ID, 0);
				if(noteId != 0) {
					selection.append(noteId).append(",");
				}

				Iterator<Long> iterator = mNewInsertNoteIdList.iterator();
				while (iterator.hasNext()) {
					selection.append(iterator.next()).append(",");
				}
				selection.deleteCharAt(selection.length() - 1);
				selection.append(")");

				ContentValues values = new ContentValues();
				values.put(Note.COLUMN_GROUP_ID, params[0]);
				return resolver.update(Note.CONTENT_URI, values,
						selection.toString(), null);
			}

			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			dismissDialog(DIALOG_UPDATE_NOTE_GROUP);
		}
	}

	private class LoadNoteAsyncTask extends AsyncTask<Long, Integer, Void> {

		private Note mNote;
		private Note.Paint mPaint;
		private Note.Audio mAudio;
		private List<Note.Text> mTextList = new LinkedList<Note.Text>();
		private List<Note.Image> mImageList = new LinkedList<Note.Image>();

		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_LOAD_NOTE);
		}

		@Override
		protected Void doInBackground(Long... params) {
			loadNote(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			((EditText) findViewById(R.id.note_title)).setText(mNote.mTitle);
			((TextView) findViewById(R.id.label)).setText(mNote.mLabel);

			((CheckBox) findViewById(R.id.add_star))
					.setChecked(mNote.mIsStarred);

			mNoteView.setPaint(mPaint);
			mNoteView.setAudio(mAudio);
			mNoteView.setNote(mNote);

			int size = mTextList.size();
			for (int i = 0; i < size; i++) {
				mNoteView.addText(mTextList.get(i), false);
			}

			size = mImageList.size();
			for (int i = 0; i < size; i++) {
				mNoteView.addImage(mImageList.get(i), false);
			}

			mNoteBgResId = mNote.mBgImageResId;
			mNoteView.setNoteBg(mNoteBgResId);
			setNoteBgTopBg(mNoteBgResId);

			((ImageButton) findViewById(R.id.add_audio))
					.setImageResource(mAudio == null ? R.drawable.add_audio
							: R.drawable.play_audio);
			
			if(mNoteAmount > 1) {
				findViewById(R.id.forward_backward_layout).setVisibility(View.VISIBLE);
				findViewById(R.id.forward).setVisibility(mNoteIndex > 0 ? View.VISIBLE : View.INVISIBLE);
				((TextView)findViewById(R.id.page_number)).setText((mNoteIndex + 1) + "/" + mNoteAmount);
				findViewById(R.id.backward).setVisibility((mNoteIndex + 1) < mNoteAmount ? View.VISIBLE : View.INVISIBLE);
			} else {
				findViewById(R.id.forward_backward_layout).setVisibility(View.INVISIBLE);
			}

			dismissDialog(DIALOG_LOAD_NOTE);
		}

		private void loadNote(long noteId) {
			ContentResolver resolver = getContentResolver();
			Cursor cursor = resolver.query(
					ContentUris.withAppendedId(Note.CONTENT_URI, noteId), null,
					null, null, null);
			if (cursor != null) {
				if (cursor.moveToNext()) {
					mNote = NoteUtils.toNote(cursor);
				}
				cursor.close();
				
				if (mNote != null) {
					cursor = resolver.query(Note.Paint.CONTENT_URI, null,
							Note.Paint.COLUMN_NOTE_ID + "=" + noteId, null,
							null);
					if (cursor != null) {
						if (cursor.moveToNext()) {
							mPaint = NoteUtils.toPaint(cursor);
						}
						cursor.close();
					}

					cursor = resolver.query(Note.Audio.CONTENT_URI, null,
							Note.Audio.COLUMN_NOTE_ID + "=" + noteId, null,
							null);
					if (cursor != null) {
						if (cursor.moveToNext()) {
							mAudio = NoteUtils.toAudio(cursor);
						}
						cursor.close();
					}

					cursor = resolver.query(Note.Text.CONTENT_URI, null,
							Note.Text.COLUMN_NOTE_ID + "=" + noteId, null,
							Note.Text.COLUMN_LAYER);
					if (cursor != null) {
						while (cursor.moveToNext()) {
							mTextList.add(NoteUtils.toText(cursor));
						}
						cursor.close();
					}

					cursor = resolver.query(Note.Image.CONTENT_URI, null,
							Note.Image.COLUMN_NOTE_ID + "=" + noteId, null,
							Note.Image.COLUMN_LAYER);
					if (cursor != null) {
						while (cursor.moveToNext()) {
							mImageList.add(NoteUtils.toImage(cursor));
						}
						cursor.close();
					}
				}
			}
		}
	}
	
	private boolean isLandscape() {
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
}