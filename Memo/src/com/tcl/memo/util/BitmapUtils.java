package com.tcl.memo.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapUtils {
	private final static String TAG = BitmapUtils.class.getSimpleName();

	public static File saveToDirectory(Bitmap bmp, File dir, boolean needExtension) {
		File file = null;
		if (bmp != null && !bmp.isRecycled() && dir != null) {
			FileOutputStream outputStream = null;

			while (file == null || file.exists()) {
				file = new File(dir.getAbsolutePath() + File.separatorChar
						+ System.currentTimeMillis() + (needExtension ? ".png" : ""));
			}

			try {
				dir.mkdirs();
				file.createNewFile();
				outputStream = new FileOutputStream(file);
				bmp.compress(CompressFormat.PNG, 100, outputStream);
				outputStream.flush();
			} catch (IOException e) {
				Log.w(TAG, e.toString(), e);
				if (file != null && file.exists()) {
					if (file.delete()) {
						file = null;
					}
				}
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						Log.w(TAG, e.toString(), e);
					}
				}
			}
		}

		return file;
	}

	public static void saveToFile(Bitmap bmp, File file) {
		if (bmp != null && !bmp.isRecycled() && file != null) {
			FileOutputStream outputStream = null;

			try {
				if (file.exists()) {
					file.delete();
				} else {
					new File(file.getParent()).mkdirs();
				}
				file.createNewFile();
				outputStream = new FileOutputStream(file);
				bmp.compress(CompressFormat.PNG, 100, outputStream);
				outputStream.flush();
			} catch (IOException e) {
				Log.w(TAG, e.toString(), e);
				if (file != null && file.exists()) {
					file.delete();
				}
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						Log.w(TAG, e.toString(), e);
					}
				}
			}
		}
	}

	public static int getSampleSize(String pathName, int maxSize) {
		int sampleSize = 1;
		if (pathName != null) {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			opts.inSampleSize = 1;
			BitmapFactory.decodeFile(pathName, opts);

			sampleSize = Math.max(opts.outWidth / maxSize, opts.outHeight
					/ maxSize);
			
			int size;
			for(int i = 0;;i++) {
				size = (int)Math.pow(2, i);
				if(size >= sampleSize) {
					sampleSize = size;
					break;
				}
			}
		}

		return sampleSize;
	}

	public static Bitmap rotate(Bitmap bmp, int angle) {
		Bitmap newBmp = null;
		if (bmp != null && angle % 360 != 0) {
			Matrix matrix = new Matrix();
			matrix.setRotate(angle);
			try {
				newBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
						bmp.getHeight(), matrix, false);
			} catch (Throwable t) {
				Log.e(TAG, t.toString(), t);
			}
		}
		return newBmp;
	}
}