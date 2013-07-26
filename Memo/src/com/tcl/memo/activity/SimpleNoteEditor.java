package com.tcl.memo.activity;

import android.view.Menu;
import com.tcl.memo.R;

public class SimpleNoteEditor extends NoteEditor {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		menu.findItem(R.id.menu_set_as).setVisible(false);
		menu.findItem(R.id.menu_share).setVisible(false);
		menu.findItem(R.id.menu_export_to_gallery).setVisible(false);
		menu.findItem(R.id.menu_add_widget).setVisible(false);
		return true;
	}

}
