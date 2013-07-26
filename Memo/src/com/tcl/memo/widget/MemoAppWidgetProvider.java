package com.tcl.memo.widget;

import java.io.File;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.tcl.memo.R;

public class MemoAppWidgetProvider extends AppWidgetProvider {
	
    public static final ComponentName COMPONENT =
            new ComponentName("com.tcl.memo",
                    "com.tcl.memo.widget.MemoAppWidgetProvider");
    private static final String TAG = "MemoAppWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	Log.i(TAG, "onUpdate().");
    	int count = appWidgetIds.length;
		SharedPreferences widgetNoteMap = context.getSharedPreferences("widget_note_map",Context.MODE_PRIVATE);
		SharedPreferences widgetDbMap = context.getSharedPreferences("widget_db_map",Context.MODE_PRIVATE);
		final AppWidgetManager gm = AppWidgetManager.getInstance(context);
		for (int i = 0; i < count; i++) {
			String path =  widgetNoteMap.getString(String.valueOf(appWidgetIds[i]), "");
			long noteId = widgetDbMap.getLong(String.valueOf(appWidgetIds[i]), -1);
			Log.i(TAG, "onUpdate(), appWidgetId " + i + " " + appWidgetIds[i] + ", path = " + path + ", note = " + noteId);
			RemoteViews views;
			File file = new File(path);
			if (!path.equals("") && (file.exists())) {
				views = new RemoteViews(context.getPackageName(),
				R.layout.widget);
				Bitmap bitmap = BitmapFactory.decodeFile(path);
				views.setImageViewBitmap(R.id.content, bitmap);
				
				//set onclick action.
				views.setOnClickPendingIntent(R.id.content, getLaunchPendingIntent(context, noteId, Uri.fromFile(file)));
				
			} else {
				//no such appwidget id or image file.
				Log.e(TAG, "onUpdate(). no such appwidget id or image file.");
				views = new RemoteViews(context.getPackageName(),
						R.layout.widget_error);
			}
	        
	        gm.updateAppWidget(appWidgetIds[i], views);
		}
    }
    
    private static PendingIntent getLaunchPendingIntent(Context context, long noteId, Uri file) {
        Intent launchIntent = new Intent("com.tcl.memo.action.VIEW");
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        launchIntent.setData(file);
        launchIntent.putExtra("note_id", noteId);

        PendingIntent pi = PendingIntent.getActivity(context, 0, launchIntent, 0);
        return pi;
    }


}
