package com.tainzhi.sample.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tainzhi.sample.media.widget.CustomImageView;

import java.io.IOException;
import java.io.InputStream;

public class DrawImageActivity extends AppCompatActivity implements SurfaceHolder.Callback {
	
	ImageView imageView;
	SurfaceView surfaceView;
	CustomImageView customImageView;
	
	Bitmap bitmap;
	Paint paint = new Paint();
	
	private int imageWidth;
	private int imageHeight;
	
	private int surfaceViewWidth;
	private int surfaceViewHeight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_draw_image);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		imageView = findViewById(R.id.imageview);
		surfaceView = findViewById(R.id.surfaceView);
		customImageView = findViewById(R.id.customview);
		
		surfaceView.getHolder().addCallback(this);
		
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		try {
			InputStream inputStream = getAssets().open("one_piece.jpg");
			bitmap = BitmapFactory.decodeStream(inputStream);
			imageWidth = bitmap.getWidth();
			imageHeight = bitmap.getHeight();
			imageView.setImageBitmap(bitmap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			return;
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
		surfaceViewWidth = i1;
		surfaceViewHeight = i2;
		
		Matrix matrix = new Matrix();
		matrix.setScale((float)surfaceViewWidth/imageWidth, (float)surfaceViewHeight/imageHeight);
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		Canvas canvas = surfaceHolder.lockCanvas();  // 先锁定当前surfaceView的画布
		canvas.drawBitmap(bitmap, matrix, paint); //执行绘制操作
		surfaceHolder.unlockCanvasAndPost(canvas); // 解除锁定并显示在界面上
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
	
	}
}
