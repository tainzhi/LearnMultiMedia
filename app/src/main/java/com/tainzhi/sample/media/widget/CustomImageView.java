package com.tainzhi.sample.media.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author: tainzhi
 * @mail: qfq61@qq.com
 * @date: 2019-11-21 18:31
 * @description:
 **/

public class CustomImageView extends View {
	
	private Paint paint = new Paint();
	private Bitmap bitmap;
	private Rect srcRect, destRect;
	
	public CustomImageView(Context context) {
		super(context);
		initView(context);
	}
	
	public CustomImageView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	public CustomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}
	
	public CustomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initView(context);
	}
	
	private void initView(Context context) {
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		try {
			InputStream inputStream = context.getAssets().open("one_piece.jpg");
			bitmap = BitmapFactory.decodeStream(inputStream);
			int bitmapWidth = bitmap.getWidth();
			int bitmapHeight = bitmap.getHeight();
			srcRect = new Rect(0, 0, bitmapWidth, bitmapHeight);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		destRect = new Rect(0, 0, width, height);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (bitmap != null) {
			canvas.drawBitmap(bitmap, srcRect, destRect, paint);
		}
	}
}
