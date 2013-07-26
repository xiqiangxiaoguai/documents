/*
 * The extrance of the Memo Program
 * @author JiangzhouQ
 * @version 1.0
 * @
 */

package com.tcl.memo.activity;

import java.io.File;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ActionBar.LayoutParams;
import android.app.DownloadManager.Query;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.renderscript.Sampler.Value;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

import com.tcl.memo.Constants;
import com.tcl.memo.R;
import com.tcl.memo.data.Note;
import com.tcl.memo.data.NoteUtils;
import com.tcl.memo.data.Note.Group;
import com.tcl.memo.util.FileUtils;

public class NoteList extends Activity  implements  OnQueryTextListener,
OnCloseListener, OnFocusChangeListener, OnItemClickListener, OnItemLongClickListener {
	
	private static final String TAG = "NoteList";
	
	private final int QUERYID_GRID = 0;
	private final int QUERYID_GRID_BLURRING = 1;
	private final int QUERYID_LIST = 2;
	private final int QUERYID_LIST_BLURRING = 3;
	
	private static boolean SEARCH_VIEW_SHOW = false;
	
	Uri uri = Note.CONTENT_URI;
	Cursor myCursor = null;
	boolean DISPLAY_MODE_GRID = true;
	boolean MULTI_PICK_MODE = false;
	private SearchView mSearchView;
	private String searchText = "";
	private String mOrder = Note.COLUMN_CREATE_TIME + " DESC";
	ActionBar actionBar;
	GridView gridview ;
	ListView listview;
	Menu mMenu = null;
	MultiPickCallback  mMultiPickCallback = new MultiPickCallback();
	private Set<Integer> mCheckedPosition = new HashSet<Integer>();

	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Intent intent = new Intent("com.tcl.memo.EditNote");
		// intent.putExtra("_id", 1L);
		// startActivity(intent);

		actionBar = getActionBar();
		showView();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.note_action_bar_bg));
	}
    
    /*
     * To Show a SearchView in the ActionBar.
     * Called in onCreate()
     */
    private void showSearchView(){
		SEARCH_VIEW_SHOW = true;
		final View searchViewContainer = LayoutInflater.from(actionBar.getThemedContext())
		.inflate(R.layout.custom_action_bar, null);
		if(actionBar.getCustomView() != searchViewContainer){
			mSearchView = (SearchView) searchViewContainer.findViewById(R.id.search_view);
	        mSearchView.setIconifiedByDefault(true);
	        mSearchView.setQueryHint(getString(R.string.hint_findContacts));
	        mSearchView.setIconified(false);
	        mSearchView.setOnQueryTextListener(this);
	        mSearchView.setOnCloseListener(this);
	        mSearchView.setOnQueryTextFocusChangeListener(this);
	        actionBar.setCustomView(searchViewContainer,
	                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
        actionBar.setDisplayShowCustomEnabled(true);

    }
    
    /*
     * Show the view | Refresh the view.
     */
    private void showView(){
		
    	Fragment fg;
    	getFragmentManager().beginTransaction();
    	if(DISPLAY_MODE_GRID){
			showGridView();
		}else{
			showListView();
		}
    	actionBar.setTitle(getString(R.string.app_name) + "(" + getCursor(QUERYID_LIST, mOrder).getCount() + ")");
    	
    	if (mMenu != null) {
			if (myCursor.getCount() == 0) {
				mMenu.findItem(R.id.menu_delete).setEnabled(false);
			} else {
				mMenu.findItem(R.id.menu_delete).setEnabled(true);
			}
		}
    }
    
    /*
     * Switch the view and show it.
     */
    private void switchView(){
    	if(DISPLAY_MODE_GRID){
			showListView();
		}else{
			showGridView();
		}
    }
    
    /*
     * Show the view in list mode.
     */
	private void showListView() {
		DISPLAY_MODE_GRID = false;
		setContentView(R.layout.list_view);
		if (myCursor != null) {
			myCursor.close();
		}
		listview = (ListView) findViewById(R.id.listview);
		listview.setOnItemClickListener(this);
		listview.setOnItemLongClickListener(this);
		listview.setBackgroundDrawable(getResources().getDrawable(R.drawable.note_editor_bg));
		if (searchText.isEmpty()) {
			myCursor = getCursor(QUERYID_LIST, mOrder);
		} else {
			myCursor = getCursor(QUERYID_LIST_BLURRING, mOrder);
		}
		String[] listFromColumns = new String[] { Note.COLUMN_SMALL_THUMBNAIL_URI, Note.COLUMN_TITLE, Note.COLUMN_CREATE_TIME, Note.COLUMN_LABEL  ,Note.COLUMN_HAS_AUDIO , Note.COLUMN_IS_STARRED };
		int[] listToLayoutIDs = new int[] { R.id.list_item_image, R.id.list_item_title, R.id.list_item_date, R.id.list_item_label , R.id.audio_mark , R.id.star_mark};
		MemoCursorAdapterIncludeGroupForList myAdapter = new MemoCursorAdapterIncludeGroupForList(this, R.layout.list_item, myCursor, listFromColumns, listToLayoutIDs ,mCheckedPosition);
		listview.setAdapter(myAdapter);
		
	}
	
	private void showListViewWithCheckbox() {
		DISPLAY_MODE_GRID = false;
		setContentView(R.layout.list_view);
		if (myCursor != null) {
			myCursor.close();
		}
		listview = (ListView) findViewById(R.id.listview);
		listview.setOnItemClickListener(this);
		listview.setBackgroundDrawable(getResources().getDrawable(R.drawable.note_editor_bg));
		if (searchText.isEmpty()) {
			myCursor = getCursor(QUERYID_LIST, mOrder);
		} else {
			myCursor = getCursor(QUERYID_LIST_BLURRING, mOrder);
		}
		String[] listFromColumns = new String[] { Note.COLUMN_SMALL_THUMBNAIL_URI, Note.COLUMN_TITLE, Note.COLUMN_CREATE_TIME, Note.COLUMN_LABEL };
		int[] listToLayoutIDs = new int[] { R.id.list_item_image, R.id.list_item_title, R.id.list_item_date, R.id.list_item_label };
		MemoCursorAdapterIncludeGroupForList myAdapter = new MemoCursorAdapterIncludeGroupForList(this, R.layout.list_item_checkbox, myCursor, listFromColumns, listToLayoutIDs ,mCheckedPosition);
		
		listview.setAdapter(myAdapter);
		listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		
	}
    
    /*
     * Show the view in grid mode.
     */
	private void showGridView() {
		DISPLAY_MODE_GRID = true;
		setContentView(R.layout.grid_view);
		if (myCursor != null) {
			myCursor.close();
		}
		gridview = (GridView) findViewById(R.id.grid);
		gridview.setOnItemClickListener(this);
		gridview.setBackgroundDrawable(getResources().getDrawable(R.drawable.note_editor_bg));
		gridview.setOnItemLongClickListener(this);
		if (searchText.isEmpty()) {
			myCursor = getCursor(QUERYID_GRID, mOrder);
		} else {
			myCursor = getCursor(QUERYID_GRID_BLURRING, mOrder);
		}

		
		
		String[] gridFromColumns = new String[] { Note.COLUMN_SMALL_THUMBNAIL_URI ,Note.COLUMN_HAS_AUDIO , Note.COLUMN_IS_STARRED };
		int[] gridToLayoutIDs = new int[] { R.id.grid_item_image , R.id.audio_mark , R.id.star_mark};
		MemoCursorAdapterIncludeGroup myAdapter = new MemoCursorAdapterIncludeGroup(this, R.layout.grid_item, myCursor, gridFromColumns, gridToLayoutIDs,mCheckedPosition);
		gridview.setAdapter(myAdapter);
	}
	
	private void showGridViewWithCheckbox() {
		DISPLAY_MODE_GRID = true;
		setContentView(R.layout.grid_view);
		if (myCursor != null) {
			myCursor.close();
		}
		gridview = (GridView) findViewById(R.id.grid);
		gridview.setOnItemClickListener(this);
		gridview.setBackgroundDrawable(getResources().getDrawable(R.drawable.note_editor_bg));
		if (searchText.isEmpty()) {
			myCursor = getCursor(QUERYID_GRID, mOrder);
		} else {
			myCursor = getCursor(QUERYID_GRID_BLURRING, mOrder);
		}

		String[] gridFromColumns = new String[] { Note.COLUMN_SMALL_THUMBNAIL_URI };
		int[] gridToLayoutIDs = new int[] { R.id.grid_item_image };
		MemoCursorAdapterIncludeGroup myAdapter = new MemoCursorAdapterIncludeGroup(this, R.layout.grid_item_checkbox, myCursor, gridFromColumns, gridToLayoutIDs,mCheckedPosition);
		gridview.setAdapter(myAdapter);
	}
	/*
	 * Return a Cursor according to the query condition.
	 */
	private final static String SEARCH_BY_KEY_SELECTION = "select * from note where "+
" note.group_id = 0 and (note.label like ? or note.title like ? )"+
"or note._id in (select min(ntt._id) from (select note.* from note, note_group "+
"where note.group_id <> 0 and note.group_id = note_group._id and (note.label like ? or note.title like ? or note_group.name like ?)) "+
"ntt GROUP BY ntt.group_id)";
	private Cursor getCursor(int queryID, String orderStr) {
		String toMarry = "%" + searchText + "%";
		String selection = SEARCH_BY_KEY_SELECTION/*Note.COLUMN_TITLE + " like ? or " + Note.COLUMN_LABEL + " like ?"*/;
		String[] selectionArgs = new String[] { toMarry, toMarry, toMarry ,toMarry };
		String groupQuery = "1 and _id in(select n._id from note n where n.group_id = 0 or n._id in(select min(nn._id) from note nn where nn.group_id <> 0 GROUP BY nn.group_id ))) and (1=1";
		String orderString;
		orderString = orderStr;
		Cursor mCursor = null;
		switch (queryID) {
		case QUERYID_GRID: {
			mCursor = getContentResolver().query(uri, null, groupQuery , null, orderString);
			break;
		}
		case QUERYID_GRID_BLURRING: {
			Log.e("memo", "=======================================");
			selectionArgs = new String[] { toMarry, toMarry, toMarry ,toMarry, toMarry };
			mCursor = getContentResolver().query(Note.CONTENT_URI_SEARCH, null, selection, selectionArgs,
					orderString);
			break;
		}
		case QUERYID_LIST: {
			mCursor = getContentResolver().query(uri, null, groupQuery, null, orderString);
			break;
		}
		case QUERYID_LIST_BLURRING: {
			mCursor = getContentResolver().query(Note.CONTENT_URI_SEARCH, null, selection, selectionArgs,
					orderString);
			break;
		}
		}
		return mCursor;
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	mMenu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.note_list, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	
    	if(DISPLAY_MODE_GRID){
    		menu.findItem(R.id.switch_display_mode).setTitle(R.string.switch_display_mode_list);
    	}else{
    		menu.findItem(R.id.switch_display_mode).setTitle(R.string.switch_display_mode_grid);
    	}
    	
    	return super.onPrepareOptionsMenu(menu);
    }
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add: {
			Intent intent = new Intent(this, NoteEditor.class);
			startActivity(intent);
			break;
		}
		case R.id.switch_display_mode: {
			switchView();
			return true;
		}
		case R.id.switch_order: {
			AlertDialog dlg = new AlertDialog.Builder(this)
					.setTitle(R.string.switch_order)
					.setItems(
							getResources().getStringArray(R.array.order_array),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
										case 0: {
											mOrder = Note.COLUMN_CREATE_TIME + " DESC";
											break;
										}
										case 1: {
											mOrder = Note.COLUMN_TITLE + " DESC" + "," + Note.COLUMN_CREATE_TIME + " DESC";
											break;
										}
										case 2: {
											mOrder = Note.COLUMN_LABEL + " DESC" + "," + Note.COLUMN_CREATE_TIME + " DESC";
											break;
										}
										case 3: {
											mOrder = Note.COLUMN_HAS_AUDIO + " DESC" + "," + Note.COLUMN_CREATE_TIME + " DESC";
											break;
										}
										case 4: {
											mOrder = Note.COLUMN_HAS_TEXT + " DESC" + "," + Note.COLUMN_CREATE_TIME + " DESC";
											break;
										}
										case 5: {
											mOrder = Note.COLUMN_IS_STARRED + " DESC" + "," + Note.COLUMN_CREATE_TIME + " DESC";
											break;
										}
										
									}
								showView();
								}
							}).create();
			dlg.show();
			break;
			
		}
		case R.id.menu_search:{
			showSearchView();
			break;
		}
		case R.id.menu_delete:{
			MULTI_PICK_MODE = true;
			mCheckedPosition.clear();
			if (DISPLAY_MODE_GRID) {
				showGridViewWithCheckbox();
			}else{
				showListViewWithCheckbox();
			}
			startActionMode(mMultiPickCallback);
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}
    
	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		switch (view.getId()) {
		case R.id.search_view: {
//			if (hasFocus) {
//				showInputMethod(mSearchView.findFocus());
//			}
		}
		}
	}
	
//	private void showInputMethod(View view) {
//		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		if (imm != null) {
//			if (!imm.showSoftInput(view, 0)) {
//				Log.w(TAG, "Failed to show soft input method.");
//			}
//		}
//	}
	@Override
	public boolean onClose() {
		if (!TextUtils.isEmpty(mSearchView.getQuery())) {
			mSearchView.setQuery(null, true);
		}
		return true;
	}
	@Override
	public boolean onQueryTextChange(String newText) {
		searchText = newText;
		showView();
		return false;
	}
	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onBackPressed() {
		if (SEARCH_VIEW_SHOW) {
			actionBar.setDisplayShowCustomEnabled(false);
			SEARCH_VIEW_SHOW = false;
			searchText = "";
			showView();
		} else {
			super.onBackPressed();
		}
	}
	
	private class MultiPickCallback implements ActionMode.Callback, OnClickListener {
		public View mMultiSelectActionBarView;

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			 MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.multi_action_mode_delete, menu);

            if (mMultiSelectActionBarView == null) {
                mMultiSelectActionBarView = (ViewGroup)getLayoutInflater()
                    .inflate(R.layout.multi_action_mode, null);
            }
            mMultiSelectActionBarView.findViewById(R.id.multi_checkbox).setOnClickListener(this);
            mode.setCustomView(mMultiSelectActionBarView);
            findViewById(com.android.internal.R.id.action_context_bar).setBackgroundResource(R.drawable.note_action_bar_bg);
            return true; 
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			if (mMultiSelectActionBarView == null) {
                ViewGroup v = (ViewGroup)getLayoutInflater()
                    .inflate(R.layout.multi_action_mode, null);
                mode.setCustomView(v);
            }
            return true; 
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.delete: {
				if(!mCheckedPosition.isEmpty()){
				AlertDialog aDlg = new AlertDialog.Builder(NoteList.this)
				.setIcon(getResources().getDrawable(R.drawable.ic_dialog_alert_holo_light))
						.setTitle(R.string.memo_delete_alert_title)
						.setMessage(R.string.memo_delete_alert_message)
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								})
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										long noteId;
										if (!mCheckedPosition.isEmpty()) {
											Iterator iterator = mCheckedPosition
													.iterator();
											while (iterator.hasNext()) {
												myCursor.moveToPosition((Integer) iterator
														.next());
												noteId = myCursor.getLong(myCursor
														.getColumnIndex(Note.COLUMN_ID));
												long groupToDeleteId = myCursor.getLong(myCursor
														.getColumnIndex(Note.COLUMN_GROUP_ID));
												if (groupToDeleteId == 0) {
													NoteUtils.deleteNote(noteId, NoteList.this);
												} else {
													Cursor groupToDelete = getContentResolver()
															.query(Note.CONTENT_URI,
																	null,
																	Note.COLUMN_GROUP_ID
																			+ " == "
																			+ groupToDeleteId,
																	null, null);
													while (groupToDelete
															.moveToNext()) {
														noteId = groupToDelete
																.getLong(groupToDelete
																		.getColumnIndex(Note.COLUMN_ID));
														NoteUtils.deleteNote(noteId, NoteList.this);
													}
													groupToDelete.close();
												}
											}
										}
										showView();

									}
								}).create();
				aDlg.show();

			}
			}
			}
			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			
			MULTI_PICK_MODE = false;
			((CheckBox)mMultiSelectActionBarView.findViewById(R.id.multi_checkbox)).setChecked(false);
			if(DISPLAY_MODE_GRID){
				showGridView();
			}else{
				showListView();
			}
		}

		@Override
		public void onClick(View v) {
			boolean mAllChecked = ((CheckBox)v).isChecked();
			if (mAllChecked == true) {
				mCheckedPosition.clear();
				if (DISPLAY_MODE_GRID) {
					int mCount = gridview.getAdapter().getCount();
					for (int i = 0; i < mCount; i++) {
							mCheckedPosition.add(i);
					}
					((MemoCursorAdapterIncludeGroup)gridview.getAdapter()).notifyDataSetChanged();
				} else {
					int mCount = listview.getAdapter().getCount();
					for (int i = 0; i <mCount; i++) {
							mCheckedPosition.add(i);
					}
					((MemoCursorAdapterIncludeGroupForList)listview.getAdapter()).notifyDataSetChanged();
				}
			} else {
				mCheckedPosition.clear();
				if (DISPLAY_MODE_GRID) {
					((MemoCursorAdapterIncludeGroup)gridview.getAdapter()).notifyDataSetChanged();
				}else{
					((MemoCursorAdapterIncludeGroupForList)listview.getAdapter()).notifyDataSetChanged();
				}
			}
		}
    //end: added by yunzhou.song
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		myCursor.moveToPosition(arg2);
		final Note mNote = NoteUtils.toNote(myCursor);
		Log.d("^^", "groupId:"+ mNote.mGroupId + " title:" + mNote.mTitle);
		if (!MULTI_PICK_MODE) {
			if (mNote.mGroupId == 0) {
					AlertDialog dlg = new AlertDialog.Builder(this)
							.setTitle(DateFormat.format("yy/MM/dd h:mmaa", mNote.mCreateTime))
							.setItems(
									getResources().getStringArray(
											R.array.note_list_context),
									new DialogInterface.OnClickListener() {
	
										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											switch (which) {
											case 0: {
												Intent intent = new Intent("com.tcl.memo.NoteEditor");
												intent.putExtra(Constants.EXTRA_NOTE_ID, mNote.mId);
												startActivity(intent);
												break;
											}
											case 1: {
												NoteUtils.deleteNote(mNote.mId, NoteList.this);
												showView();
												break;
											}
											case 2: {

												Uri uri = Uri.parse("file://" + mNote.mThumbnailUri);
												Intent intent = new Intent(Intent.ACTION_SEND);
												intent.setType("image/*");
												intent.putExtra(Intent.EXTRA_STREAM, uri);
												startActivity(intent);
												break;
											}
											case 3: {
												Toast.makeText(NoteList.this, R.string.exporting,
														Toast.LENGTH_SHORT).show();
												try {
													File file = FileUtils.copyToDirection(
															new File(mNote.mThumbnailUri),
															Constants.GALLERY_DIR);
													if(file != null) {
														MediaScannerConnection.scanFile(NoteList.this,
																new String[] { file.getAbsolutePath() }, null,
																null);
													}
													Toast.makeText(NoteList.this, R.string.export_complete,
															Toast.LENGTH_SHORT).show();
												} catch (Exception e) {
													Toast.makeText(NoteList.this, R.string.export_fail,
															Toast.LENGTH_SHORT).show();
												}
												break;
											}
											case 4: {
												Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
												intent.setDataAndType(
														Uri.parse("file:/" + mNote.mThumbnailUri),
														"image/*");
												intent.putExtra("mimeType", "image/*");
												intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
												startActivity(Intent.createChooser(intent, getString(R.string.set_as)));
												break;
											}
											}
										}
									}).create();
					dlg.show();
			}
 else {
				AlertDialog dlg = new AlertDialog.Builder(this)
						.setTitle(
								DateFormat.format("yy/MM/dd h:mmaa",
										mNote.mCreateTime))
						.setItems(
								getResources().getStringArray(
										R.array.note_list_context_group),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										switch (which) {
										case 0: {
											final View view = getLayoutInflater()
													.inflate(R.layout.group_name, null);
											
											String[] args = new String[]{mNote.mGroupId + ""};
											Cursor groupCursor = getContentResolver().query(Group.CONTENT_URI, null, Group.COLUMN_ID + " == ?",args, null);
											groupCursor.moveToFirst();
											String groupName = groupCursor.getString(groupCursor.getColumnIndex(Group.COLUMN_NAME));
											groupCursor.close();
											final EditText mEditText = ((EditText)view.findViewById(R.id.group_name));
											mEditText.setText(groupName);

											final AlertDialog.Builder builder = new AlertDialog.Builder(
													new ContextThemeWrapper(NoteList.this, android.R.style.Theme_Holo_Light));
											builder.setTitle(R.string.save_group);
											builder.setView(view);
											builder.setNegativeButton(android.R.string.cancel,
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog, int which) {
															
														}
													});
											builder.setPositiveButton(android.R.string.ok,
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
															Group group = new Group();
															group.mId = mNote.mGroupId;
															group.mName = mEditText.getText().toString();
															getContentResolver()
																	.update(ContentUris
																			.withAppendedId(
																					Group.CONTENT_URI,
																					group.mId),
																					group.toContentValues(),
																			null,
																			null);
															showView();
														}
													});
											builder.create().show();
											break;
										}
										case 1: {
											Cursor childsCursor = getContentResolver()
													.query(uri,
															null,
															Note.COLUMN_GROUP_ID
																	+ " == "
																	+ mNote.mGroupId,
															null, null);
											childsCursor.moveToPosition(-1);
											Note nNote;
											while (childsCursor.moveToNext()) {
												nNote = NoteUtils
														.toNote(childsCursor);
												NoteUtils.deleteNote(nNote.mId, NoteList.this);
											}
											showView();
											childsCursor.close();
											break;
										}
										}
									}
								}).create();
				dlg.show();
			}
		}
		return false;
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (MULTI_PICK_MODE) {
			if (DISPLAY_MODE_GRID) {
				arg1.findViewById(R.id.grid_item_checkbox).performClick();

			} else {
				arg1.findViewById(R.id.list_item_checkbox).performClick();
			}
			if (mCheckedPosition.contains(arg2)) {
				mCheckedPosition.remove(arg2);
			} else {
				mCheckedPosition.add(arg2);
			}
			if (DISPLAY_MODE_GRID) {
				if (mCheckedPosition.size() == gridview.getAdapter().getCount()) {
					((CheckBox) mMultiPickCallback.mMultiSelectActionBarView
							.findViewById(R.id.multi_checkbox))
							.setChecked(true);
				} else {
					((CheckBox) mMultiPickCallback.mMultiSelectActionBarView
							.findViewById(R.id.multi_checkbox))
							.setChecked(false);
				}
			} else {
				if (mCheckedPosition.size() == listview.getAdapter().getCount()) {
					((CheckBox) mMultiPickCallback.mMultiSelectActionBarView
							.findViewById(R.id.multi_checkbox))
							.setChecked(true);
				} else {
					((CheckBox) mMultiPickCallback.mMultiSelectActionBarView
							.findViewById(R.id.multi_checkbox))
							.setChecked(false);
				}
			}
			
			
		}else{
			myCursor.moveToPosition(arg2);
			Long groupId = myCursor.getLong(myCursor.getColumnIndex(Note.COLUMN_GROUP_ID));
			Log.d("^^", "GroupId:" +groupId );
			if( groupId == 0){

				Long noteId = myCursor.getLong(myCursor.getColumnIndex(Note.COLUMN_ID));
				Intent intent = new Intent("com.tcl.memo.NotePreview");
				intent.putExtra(Constants.EXTRA_NOTE_ID, noteId);
				startActivity(intent);
			}else{
				Intent intent = new Intent("com.tcl.memo.ChildNoteEditor");
				intent.putExtra(Constants.EXTRA_NOTE_GROUP_ID, groupId);
				intent.putExtra("displayModeGrid", DISPLAY_MODE_GRID);
				startActivityForResult(intent , 1);
			}
		}
	
}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		showView();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode){
		case 1:{
			DISPLAY_MODE_GRID = data.getBooleanExtra("displayModeGrid", true);
			showView();
		}
		showView();
		}
	}

}