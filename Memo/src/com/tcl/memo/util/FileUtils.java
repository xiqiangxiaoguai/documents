package com.tcl.memo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

public class FileUtils {
	private final static String TAG = BitmapUtils.class.getSimpleName();

	public static boolean copyToFile(File srcFile, File desFile) {
		boolean isSuccess = false;
		if (srcFile != null && srcFile.exists() && desFile != null) {
			byte[] buffer = new byte[1024 * 1024];
			FileInputStream inputStream = null;
			FileOutputStream outputStream = null;
			try {
				inputStream = new FileInputStream(srcFile);

				if (!desFile.exists()) {
					desFile.createNewFile();
				}
				outputStream = new FileOutputStream(desFile);

				int length;
				while ((length = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, length);
				}

				outputStream.flush();
			} catch (FileNotFoundException e) {
				Log.w(TAG, e.toString(), e);
			} catch (IOException e) {
				Log.w(TAG, e.toString(), e);
			} finally {
				try {
					if (inputStream != null) {
						inputStream.close();
					}
					if (outputStream != null) {
						outputStream.close();
					}
					isSuccess = true;
				} catch (IOException e) {
					Log.w(TAG, e.toString(), e);
				}
			}
		}
		return isSuccess;
	}

	public static File copyToDirection(File srcFile, File desDir) {
		File desFile = null;
		if (srcFile != null && srcFile.exists()) {
			byte[] buffer = new byte[1024 * 1024];
			FileInputStream inputStream = null;
			FileOutputStream outputStream = null;
			try {
				desDir.mkdirs();

				inputStream = new FileInputStream(srcFile);

				while (desFile == null || desFile.exists()) {
					String srcFileName = srcFile.getName();
					desFile = new File(desDir.getAbsolutePath()
							+ File.separatorChar
							+ System.currentTimeMillis()
							+ srcFileName.substring(srcFileName
									.lastIndexOf(".")));
				}
				outputStream = new FileOutputStream(desFile);

				int length;
				while ((length = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, length);
				}

				outputStream.flush();
			} catch (FileNotFoundException e) {
				Log.w(TAG, e.toString(), e);
				return null;
			} catch (IOException e) {
				Log.w(TAG, e.toString(), e);
				return null;
			} finally {
				try {
					if (inputStream != null) {
						inputStream.close();
					}
					if (outputStream != null) {
						outputStream.close();
					}
				} catch (IOException e) {
					Log.w(TAG, e.toString(), e);
					return null;
				}
			}
		}
		return desFile;
	}
}