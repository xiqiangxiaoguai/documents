//package com.tcl.memo.activity;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Rect;
//import android.graphics.drawable.Drawable;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//
//import com.google.android.maps.GeoPoint;
//import com.google.android.maps.ItemizedOverlay;
//import com.google.android.maps.MapActivity;
//import com.google.android.maps.MapController;
//import com.google.android.maps.MapView;
//import com.google.android.maps.MyLocationOverlay;
//import com.google.android.maps.Overlay;
//import com.google.android.maps.OverlayItem;
//import com.tcl.memo.R;
//import com.tcl.memo.util.BitmapUtils;
///**
// * @author UPPower Studio
// *
// */
//public class GoogleMapActivity extends MapActivity {
//
//	private MapView mapView;
//	private MyLocationOverlay myLocationOverlay;
//	private MapController mapController;
//	private Drawable pin;
//	private static final File IMAGE_DIR = new File(
//			Environment.getExternalStorageDirectory() + "/Memo/Image");
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.map_layout);
//		mapView = (MapView) findViewById(R.id.mapview);
//		myLocationOverlay = new MyLocationOverlay(GoogleMapActivity.this, mapView);
//		myLocationOverlay.enableCompass();
//		myLocationOverlay.enableMyLocation();
//
//		myLocationOverlay.runOnFirstFix(new Runnable() {
//			public void run() {
//				mapController.animateTo(myLocationOverlay.getMyLocation());
//			}
//		});
//		mapView.getOverlays().add(myLocationOverlay);
//		mapView.setBuiltInZoomControls(true);
//    	mapController = mapView.getController();
//		mapController.setZoom(15);
//	}
//
//	@Override
//	protected boolean isRouteDisplayed() {
//		return false;
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.map_controller, menu);
//		return super.onCreateOptionsMenu(menu);
//	}
//	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.crop_map:{
//			mapView.setDrawingCacheEnabled(true);
//			mapView.buildDrawingCache();
//			int width = mapView.getWidth();
//			int height = mapView.getHeight();
//			Bitmap mapCropBmp = Bitmap.createBitmap(mapView.getDrawingCache(),width/2 - 300, height/2 - 250, width/2 + 300, height/2 +250);
//			File filename = BitmapUtils.saveToDirectory(mapCropBmp, IMAGE_DIR);
//			Intent data = new Intent();
//			data.putExtra("filename",filename.toString());
//			setResult(RESULT_OK, data);
//			Log.d("^^", "set result");
//			finish();
//			mapView.destroyDrawingCache();
//			mapView.setDrawingCacheEnabled(false);
//			break;
//		}
//		case R.id.start_pin:{
//			pin = getResources().getDrawable(R.drawable.red_pin);
//			pin.setBounds(0, 0, pin.getMinimumWidth(), pin.getMinimumHeight());
//			MyItemOverlay startOverlay = new MyItemOverlay(pin);
//			GeoPoint pt = startOverlay.getCenter();
//			mapController.setCenter(pt);
//			break;
//		}
//		case R.id.overlay: {
//			myLocationOverlay.runOnFirstFix(new Runnable() {
//				public void run() {
//					mapController.animateTo(myLocationOverlay.getMyLocation());
//				}
//			});
//			break;
//		}
//		case R.id.button_normal: {
//			if (mapView.isSatellite()) {
//				mapView.setSatellite(false);
//			}
//			if (mapView.isTraffic()) {
//				mapView.setTraffic(false);
//			}
//			if (mapView.isStreetView()) {
//				mapView.setStreetView(false);
//			}
//			break;
//		}
//		case R.id.button_satellite:{
//			mapView.setSatellite(true);
//			break;
//		}
//		case R.id.button_traffic:{
//			if (mapView.isSatellite()) {
//				mapView.setSatellite(false);
//			}
//			mapView.setTraffic(true);
//			break;
//		}
//		}
//		return super.onOptionsItemSelected(item);
//	}
//	private class MyItemOverlay extends ItemizedOverlay<OverlayItem> {
//		private static final int LAT = (int) (39.90960456049752 * 1E6);
//		private static final int LNG = (int) (116.3972282409668 * 1E6);
//		private List<OverlayItem> items = new ArrayList<OverlayItem>();
//
//		public MyItemOverlay(Drawable defaultMarker) {
//			super(defaultMarker);
//
//			GeoPoint point = new GeoPoint(LAT, LNG);
//
//			items.add(new OverlayItem(point, "Point A", "Snippet Point A"));
//
//			populate();
//		}
//		@Override
//		protected OverlayItem createItem(int i) {
//			return items.get(i);
//		}
//		@Override
//		public int size() {
//			return items.size();
//		}
//	}
//
//}