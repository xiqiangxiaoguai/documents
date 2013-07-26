package com.tcl.memo.application;

import java.util.Map;
import java.util.Set;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class MemoApplication extends Application {

	private static final String TAG = "MemoApplication";
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		Log.i(TAG, "onCreate().");
		// Check if data in SharedPreferences are the newest.
		SharedPreferences noteWidgetMap = this.getSharedPreferences("note_widget_map",Context.MODE_PRIVATE);
		SharedPreferences widgetNoteMap = this.getSharedPreferences("widget_note_map",Context.MODE_PRIVATE);
		SharedPreferences widgetDbMap = this.getSharedPreferences("widget_db_map",Context.MODE_PRIVATE);

		Map<String, ?> map = widgetNoteMap.getAll();
		if (map == null) {
			Log.i(TAG, "map is null!");
			return;
		}
		Set<String> keySet = map.keySet();
		if (keySet == null || keySet.isEmpty()) {
			Log.i(TAG, "key is null");
			return;
		}
		
		AppWidgetManager am = AppWidgetManager.getInstance(this);
		int appWidgetId;
		Editor noteWidgEditor = noteWidgetMap.edit();
		Editor widgetNoteEditor = widgetNoteMap.edit();
		Editor widgetDbEditor = widgetDbMap.edit();
		
		for (String s : keySet) {
			appWidgetId = Integer.valueOf(s);
			if (am.getAppWidgetInfo(appWidgetId) == null) {
				Log.w(TAG, "can not found appWidgetId " + appWidgetId + 
						" in AppWidgetManager, so remove it from SharedPreferences");
				String note = widgetNoteMap.getString(s, "");
				noteWidgEditor.remove(note);
				widgetNoteEditor.remove(s);
				widgetDbEditor.remove(s);
			}
		}
		noteWidgEditor.apply();
		widgetNoteEditor.apply();
		widgetDbEditor.apply();
	}

}
