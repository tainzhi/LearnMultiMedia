package com.tainzhi.sample.media;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tainzhi.sample.media.tool.PcmToWav;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioRecordPlayActivity extends AppCompatActivity implements View.OnClickListener {
	
	public static final String TAG = "AudioRecordPlayActivity";
	
	//采样率 44100HZ
	public static final int SAMPLE_RATE_INHX = 44100;
	//声道数
	public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	//音频数据格式.
	public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	private static final int MY_PERMISSIONS_REQUEST = 1001;
	private String[] permissions = new String[]{
			Manifest.permission.RECORD_AUDIO,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};
	private List<String> unGrantedPermissionList = new ArrayList<>();
	private Button btnRecod;
	private Button btnConvert;
	private Button btnPlay;
	
	private boolean isRecording;
	private AudioRecord audioRecord;
	private AudioTrack audioTrack;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_record_play);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		btnRecod = findViewById(R.id.btn_record);
		btnConvert = findViewById(R.id.btn_convert);
		btnPlay = findViewById(R.id.btn_play);
		btnRecod.setOnClickListener(this);
		btnConvert.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		
		btnRecod.setEnabled(true);
		// record -> convert -> play
		btnConvert.setEnabled(false);
		btnPlay.setEnabled(false);
		
		checkPermissions();
	}
	
	@Override
	public void onClick(View v) {
		Button button = (Button) v;
		switch (v.getId()) {
			case R.id.btn_record:
				if (button.getText().toString().equals(getString(R.string.audio_start_record))) {
					button.setText(getString(R.string.audio_stop_record));
					startRecord();
				} else {
					button.setText(getString(R.string.audio_start_record));
					stopRecord();
					btnConvert.setEnabled(true);
				}
				break;
			case R.id.btn_convert:
				PcmToWav pcmToWav = new PcmToWav(SAMPLE_RATE_INHX, CHANNEL_CONFIG, AUDIO_FORMAT);
				File pcmFile = new File(getExternalCacheDir(), "audio_test.pcm");
				File wavFile = new File(getExternalCacheDir(), "audio_test.wav");
				if (!wavFile.mkdirs()) {
					Log.e(TAG, "wavFile Directory not crated");
					
				}
				if (wavFile.exists()) {
					wavFile.delete();
				}
				pcmToWav.pcmToWav(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath());
				break;
			case R.id.btn_play:
				if (button.getText().toString().equals(R.string.audio_start_play)) {
					button.setText(getString(R.string.audio_stop_play));
					playInModeStream();
				} else {
					button.setText(getString(R.string.audio_stop_play));
					stopPlay();
				}
				break;
			default:
				break;
		}
	}
	
	private void startRecord() {
		final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHX, CHANNEL_CONFIG,
				AUDIO_FORMAT);
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHX,
				CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);
		final byte data[] = new byte[minBufferSize];
		final File file = new File(getExternalCacheDir(), "audio_test.pcm");
		if (!file.mkdir()) {
			Log.e(TAG, "Directory not created");
		}
		if (file.exists()) {
			file.delete();
		}
		
		audioRecord.startRecording();
		isRecording = true;
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				FileOutputStream os = null;
				try {
					os = new FileOutputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
				if (os != null) {
					while (isRecording) {
						int read = audioRecord.read(data, 0, minBufferSize);
						if (AudioRecord.ERROR_INVALID_OPERATION != read) {
							try {
								os.write(data);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					try {
						Log.i(TAG, "run: close file ouput stream!");
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			}
		}).start();
	}
	
	private void stopRecord() {
		isRecording = false;
		if (null != audioRecord) {
			audioRecord.stop();
			audioRecord.release();
			audioRecord = null;
		}
	}
	
	/**
	 * 用 Steam 模式播放
	 */
	private void playInModeStream() {
		int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
		final int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_INHX, channelConfig,
				AUDIO_FORMAT);
		audioTrack = new AudioTrack(new AudioAttributes.Builder()
				                            .setUsage(AudioAttributes.USAGE_MEDIA)
				                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
				                            .build(),
				new AudioFormat.Builder().setSampleRate(SAMPLE_RATE_INHX)
						.setEncoding(AUDIO_FORMAT)
						.setChannelMask(channelConfig)
						.build(),
				minBufferSize,
				AudioTrack.MODE_STREAM,
				AudioManager.AUDIO_SESSION_ID_GENERATE);
		audioTrack.play();
		
		File file = new File(getCacheDir(), "audio_test.pcm");
		try {
			final FileInputStream fileInputStream = new FileInputStream(file);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						byte[] tmpBuffer = new byte[minBufferSize];
						while (fileInputStream.available() > 0) {
							int readCount = fileInputStream.read(tmpBuffer);
							if (readCount == AudioTrack.ERROR_BAD_VALUE ||
									    readCount == AudioTrack.ERROR_INVALID_OPERATION) {
								continue;
							}
							if (readCount != 0 && readCount != -1) {
								audioTrack.write(tmpBuffer, 0, readCount);
							}
							
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			}).start();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void stopPlay() {
		if (audioTrack != null) {
			audioTrack.stop();
			audioTrack.release();
			audioTrack = null;
		}
	}
	
	private void checkPermissions() {
		// Marshmallow开始运行时申请权限
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			for (String permission : permissions) {
				if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
					unGrantedPermissionList.add(permission);
				}
			}
		}
		if (unGrantedPermissionList.isEmpty()) {
			String[] tmpPermissions =
					unGrantedPermissionList.toArray(new String[unGrantedPermissionList.size()]);
			ActivityCompat.requestPermissions(this, tmpPermissions, MY_PERMISSIONS_REQUEST);
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == MY_PERMISSIONS_REQUEST) {
			for (int i = 0; i < grantResults.length; i++) {
				if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
					Log.e(TAG, permissions[i] + " 权限被用户禁止");
				}
			}
		}
		// TODO: 2019-11-22 运行时权限的申请
	}
}
