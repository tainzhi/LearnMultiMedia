package com.tainzhi.sample.media;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener {
	// map keys
	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String CLASS_NAME = "class_name";
	
	/**
	 * Each entry has three strings: the test title, the test description, and the name of
	 * the activity class.
	 */
	private static final String[][] TESTS = {
			{ "通过三种方式绘制图片",
					"ImageView, SurfaceView, 自定义图片",
					"DrawImageActivity"},
			{"录制音频, 播放音频",
					"AudioRecord采集音频PCM, AudioTrack播放",
					"AudioRecordPlayActivity"},
			{"kotlin用Camera2拍照", "kotlin用Camera2拍照", "camera.CameraActivity"},
			{"视频播放器", "MediaCodec实现的播放器", "player.PlayActivity"},
			{"录制camera, 转码264, 混合音频",
					"camera预览数据,转码成h264,再混合音频",
					"VideoRecordActivity"
			},
			{"OpenGLES draw triangle", "绘制基本三角形", "opengl2.TriangleActivity"},
			{"OpenGLES 绘制立方体", "绘制立方体， 并缩放，位移，旋转等", "opengl2.SquareActivity"},
			{"OpenGLES draw oval", "绘制基本圆形", "opengl2.OvalActivity"},
			{"OpenGLES 绘制圆锥", "绘制基本圆锥体", "opengl2.ConeActivity"},
			{"OpenGLES 绘制圆柱体", "绘制基本圆柱体", "opengl2.CylinderActivity"},
			{"OpenGLES 绘制球体", "绘制发光小球", "opengl2.BallActivity"},
			{"OpenGLES 绘制画笔点", "响应触摸事件，并在触摸出绘制点", "opengl2.PaintPointActivity"},
			{"OpenGLES 旋转三角形", "响应触摸事件，旋转三角形", "opengl2.RotateTriangleActivity"},
			{"OpenGLES 渲染图片", "渲染效果: 黑白, 冷色, 暖色, 模糊, 放大", "opengl2.ImageGLRendererActivity"},
			{"OpenGLES 预览相机", "预览相机", "opengl2.camera.CameraPreviewActivity"},
			{"OpenGLES 相机拍照", "相机拍照", "opengl2.camera.TakePictureActivity"},
	};
	
	private ListView listView;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		listView = findViewById(R.id.listview);
		
		listView.setAdapter(new SimpleAdapter(this, createActivityList(),
				android.R.layout.two_line_list_item, new String[] { TITLE, DESCRIPTION },
				new int[] { android.R.id.text1, android.R.id.text2 } ));
		listView.setOnItemClickListener(this);
	}
	
	/**
	 * Creates the list of activities from the string arrays.
	 */
	private List<Map<String, Object>> createActivityList() {
		List<Map<String, Object>> testList = new ArrayList<>();
		
		for (String[] test : TESTS) {
			Map<String, Object> tmp = new HashMap<>();
			tmp.put(TITLE, test[0]);
			tmp.put(DESCRIPTION, test[1]);
			Intent intent = new Intent();
			// Do the class name resolution here, so we crash up front rather than when the
			// activity list item is selected if the class name is wrong.
			try {
				Class cls = Class.forName("com.tainzhi.sample.media." + test[2]);
				intent.setClass(this, cls);
				tmp.put(CLASS_NAME, intent);
			} catch (ClassNotFoundException cnfe) {
				throw new RuntimeException("Unable to find " + test[2], cnfe);
			}
			testList.add(tmp);
		}
		
		// Collections.sort(testList, TEST_LIST_COMPARATOR);
		
		return testList;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Map<String, Object> map = (Map<String, Object>)listView.getItemAtPosition(position);
		Intent intent = (Intent) map.get(CLASS_NAME);
		startActivity(intent);
	}
}
