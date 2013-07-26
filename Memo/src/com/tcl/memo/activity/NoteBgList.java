package com.tcl.memo.activity;

import java.util.ArrayList;

import com.tcl.memo.Constants;
import com.tcl.memo.R;
import com.tcl.memo.view.NumberObserver;
import com.tcl.memo.view.OverlapGallery;
import com.tcl.memo.view.ShowNumberView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class NoteBgList extends Activity implements NumberObserver 
{
	private OverlapGallery mGallery;
	
	private ShowNumberView mShowPageNumberView;
	
	private NoteBgAdapter mAdapter;
	
	private int position = -1;
	
	private void initVar()
	{
		mAdapter = new NoteBgAdapter(this);
		mAdapter.createReflectedImages();
		
		mGallery = (OverlapGallery) findViewById(R.id.gallery);
		mGallery.setNumberObserver(this);
		
		mShowPageNumberView = (ShowNumberView)findViewById(R.id.number_view);
		mShowPageNumberView.setCount(mAdapter.getCount());
		
		findViewById(R.id.set_as_background).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				selectNoteBg(mAdapter.getSelectedImage(position));
			}
		});

	}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.note_bg_list);
		
		initVar();
		
		mGallery.setAdapter(mAdapter);
		mGallery.setFadingEdgeLength(0);
		mGallery.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				if(position == NoteBgList.this.position)
				{
					selectNoteBg(mAdapter.getSelectedImage(position));
				}
			}
		});
	}

	@Override
	public void numberNotice(int number) 
	{
		position = number;

		mShowPageNumberView.setPosition(position);
		mShowPageNumberView.invalidate();
	}
	
	private void selectNoteBg(int resId) 
	{
		SharedPreferences sharedPrefs = getSharedPreferences(
				Constants.SHARED_PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putInt(Constants.KEY_NOTE_BG_RES_ID, resId);
		editor.commit();
		
		Intent intent = new Intent();
		intent.putExtra(Constants.EXTRA_RES_ID, resId);
		setResult(RESULT_OK, intent);
		finish();
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if(requestCode == 100 && resultCode == RESULT_OK)
		{
			finish();
		}
	}
}

class NoteBgAdapter extends BaseAdapter 
{
	private Context mContext;
	private ArrayList<Integer> mNoteBgList;
	private ArrayList<Integer> mNoteSmallBgList;
	
	private ImageView[] mImageView;
	
	
	public NoteBgAdapter(Context context) 
	{
		mContext = context;
		mNoteBgList = new ArrayList<Integer>(6);
		mNoteSmallBgList = new ArrayList<Integer>(6);
		
		Resources resources = context.getResources();
		String packageName = resources.getResourcePackageName(R.array.note_bg_big);
		addNoteBgs(resources, packageName, R.array.note_bg_big);
		
		mImageView = new ImageView[mNoteBgList.size()];
	}

	@Override
	public int getCount() 
	{
		return mNoteBgList.size();
	}

	@Override
	public Object getItem(int position) 
	{
		return position;
	}

	@Override
	public long getItemId(int position) 
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		position = position % mNoteBgList.size();
		
		ImageView imageView = mImageView[position];
		imageView.setTag(new Integer(position));
		return imageView;
	}
	
	public int getSelectedImage(int position)
	{
		if(position < 0 || position >= mNoteBgList.size())
		{
			return mNoteBgList.get(0);
		}
		return mNoteBgList.get(position);
	}
	
	private void addNoteBgs(Resources resources, String packageName, int list) 
	{
		String[] extras = resources.getStringArray(list);
		for (String extra : extras) 
		{
			int wallpaperId = resources.getIdentifier(extra, "drawable", packageName);
			if (wallpaperId != 0) 
			{
				int wallPaperSmall = resources.getIdentifier(extra + "_small", "drawable", packageName);

				if (wallPaperSmall != 0) 
				{
					mNoteSmallBgList.add(wallPaperSmall);
					mNoteBgList.add(wallpaperId);
				}
			}
		}
	}
	
	public boolean createReflectedImages() 
	{
		// 倒影图和原图之间的距离
		final int reflectionGap = 0;
		int index = 0;
		for (int imageResId : mNoteSmallBgList) 
		{
			// 返回原图解码之后的bitmap对象
			Bitmap originalImage = BitmapFactory.decodeResource(mContext.getResources(), imageResId);
			int width = originalImage.getWidth();
			int height = originalImage.getHeight();
			// create Matrix object
			Matrix matrix = new Matrix();

			// 指定一个角度以0,0为坐标进行旋转
			// matrix.setRotate(30);

			// 指定矩阵(x轴不变，y轴相反)
			matrix.preScale(1, -1);

			// 将矩阵应用到该原图之中，返回一个宽度不变，高度为原图1/2的倒影位图
			Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height - 50, width, 50, matrix, false);
			
			//yang.wu add begin
//			Canvas canvas = new Canvas(reflectionImage);
//			Paint paint = new Paint();
//			paint.setAntiAlias(false);
//			
//			LinearGradient shader = new LinearGradient(0,
//															 0, 
//															 0,
//															 reflectionImage.getHeight(),
//					                                    0x70ffffff, 
//					                                    0x00ffffff, 
//					                                    TileMode.MIRROR);
//			paint.setShader(shader);
//			paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
//			canvas.drawRect(0, 0, width, reflectionImage.getHeight(), paint);
			//yang.wu add end

			// 创建一个宽度不变，高度为原图+倒影图高度的位图
			Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + 50), Config.ARGB_8888);

			// 将上面创建的位图初始化到画布
			Canvas canvas = new Canvas(bitmapWithReflection);
			canvas.drawBitmap(originalImage, 0, 0, null);

			Paint deafaultPaint = new Paint();
			deafaultPaint.setAntiAlias(false);
			// canvas.drawRect(0, height, width, height + reflectionGap,deafaultPaint);
			canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
			Paint paint = new Paint();
			paint.setAntiAlias(false);

			/**
			 * 参数一:为渐变起初点坐标x位置， 参数二:为y轴位置， 参数三和四:分辨对应渐变终点， 最后参数为平铺方式，
			 * 这里设置为镜像Gradient是基于Shader类，所以我们通过Paint的setShader方法来设置这个渐变
			 */
			LinearGradient shader = new LinearGradient(0, 
					                                    originalImage.getHeight(), 
					                                         0,
					                                    bitmapWithReflection.getHeight() + reflectionGap,
															 0x70ffffff, 
															 0x00ffffff, 
															 TileMode.MIRROR);
			// 设置阴影
			paint.setShader(shader);
			paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
			// 用已经定义好的画笔构建一个矩形阴影渐变效果
			canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);
			// 创建一个ImageView用来显示已经画好的bitmapWithReflection
			ImageView imageView = new ImageView(mContext);
			imageView.setImageBitmap(bitmapWithReflection);
			imageView.setScaleType(ScaleType.FIT_XY);
			// 设置imageView大小 ，也就是最终显示的图片大小
			imageView.setLayoutParams(new OverlapGallery.LayoutParams(280, 600));
			mImageView[index++] = imageView;
			
			//yang.wu add begin
//			LinearLayout layout = (LinearLayout)mWallpaper.getLayoutInflater().inflate(R.layout.grallery_item, null);
//			layout.setLayoutParams(new OverlapGallery.LayoutParams(240, 620));
//			
//			ImageView wallPaperImageView = (ImageView)layout.findViewById(R.id.wallpaper_imageview);
//			wallPaperImageView.setImageResource(imageResId);
//			wallPaperImageView.setLayoutParams(new LinearLayout.LayoutParams(240, 500));
//			wallPaperImageView.setScaleType(ScaleType.FIT_XY);
//			
//			ImageView wallPaperReflectionImageView = (ImageView)layout.findViewById(R.id.wallpaper_reflection);
//			wallPaperReflectionImageView.setImageBitmap(reflectionImage);
//			wallPaperReflectionImageView.setLayoutParams(new LinearLayout.LayoutParams(240, 120));
//			wallPaperReflectionImageView.setScaleType(ScaleType.FIT_CENTER);
//			mLinearLayout[index++] = layout;
			//yang.wu add end
		}
		
		return true;
	}
}