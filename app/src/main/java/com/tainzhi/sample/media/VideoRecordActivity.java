package com.tainzhi.sample.media;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tainzhi.sample.media.encodemp4.VideoRecorder;

public class VideoRecordActivity extends AppCompatActivity {
	
	private VideoRecorder mRecorder;
	private String path;
	private Thread recordThread;
	private boolean isStart = false;
	private int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
	private AudioRecord mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2);
	/**
	 * 录音线程
	 */
	Runnable recordRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				int bytesRecord;
				//int bufferSize = 320;
				byte[] tempBuffer = new byte[bufferSize];
				if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
					stopRecord();
					return;
				}
				mAudioRecord.startRecording();
				//writeToFileHead();
				while (isStart) {
					if (null != mAudioRecord) {
						bytesRecord = mAudioRecord.read(tempBuffer, 0, bufferSize);
						if (bytesRecord == AudioRecord.ERROR_INVALID_OPERATION || bytesRecord == AudioRecord.ERROR_BAD_VALUE) {
							continue;
						}
						if (bytesRecord != 0 && bytesRecord != -1) {
							if (mRecorder != null) {
								mRecorder.addAudioData(tempBuffer);
							}
						} else {
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_record);
		if (null == savedInstanceState) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, Camera2BasicFragment.newInstance())
					.commit();
		}
	}
	
	public void click() {
		if (mRecorder == null) {
			Toast.makeText(this, "start record...", Toast.LENGTH_SHORT).show();
			startRecord();
			path =
					Environment.getExternalStorageDirectory() +
							"/record-" + System.currentTimeMillis() +
							".mp4";
			mRecorder = new VideoRecorder(path);
			mRecorder.start();
		} else {
			Toast.makeText(this, "saved: " + path, Toast.LENGTH_LONG).show();
			stopRecord();
			mRecorder.stop();
			mRecorder = null;
		}
	}
	
	public void addVideoData(byte[] data) {
		if (mRecorder != null) {
			mRecorder.addVideoData(data);
		}
	}
	
	/**
	 * 销毁线程方法
	 */
	private void destroyThread() {
		try {
			isStart = false;
			if (null != recordThread && Thread.State.RUNNABLE == recordThread.getState()) {
				try {
					Thread.sleep(500);
					recordThread.interrupt();
				} catch (Exception e) {
					recordThread = null;
				}
			}
			recordThread = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			recordThread = null;
		}
	}
	
	/**
	 * 启动录音线程
	 */
	private void startThread() {
		destroyThread();
		isStart = true;
		if (recordThread == null) {
			recordThread = new Thread(recordRunnable);
			recordThread.start();
		}
	}
	
	/**
	 * 启动录音
	 */
	public void startRecord() {
		try {
			startThread();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 停止录音
	 */
	public void stopRecord() {
		try {
			destroyThread();
			if (mAudioRecord != null) {
				if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
					mAudioRecord.stop();
				}
				if (mAudioRecord != null) {
					mAudioRecord.release();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
