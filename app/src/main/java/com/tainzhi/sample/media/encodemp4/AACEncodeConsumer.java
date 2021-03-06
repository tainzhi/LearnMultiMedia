package com.tainzhi.sample.media.encodemp4;

import android.annotation.SuppressLint;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author: tainzhi
 * @mail: qfq61@qq.com
 * @date: 2019-11-22 15:28
 * @description: 对ACC音频进行编码
 **/

public class AACEncodeConsumer extends Thread {
	
	/**
	 * 音频源为MIC
	 */
	public static final int SOURCE_MIC = MediaRecorder.AudioSource.MIC;
	private static final String TAG = "EncodeAudio";
	private static final String MIME_TYPE = "audio/mp4a-latm";
	private static final int TIMES_OUT = 10000;
	private static final int ACC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
	private static final int AUDIO_BUFFER_SIZE = 1024;
	// 编码器
	private boolean isExit = false;
	private boolean isEncoderStarted = false;
	private WeakReference<MyMediaMuxer> mMuxerRef;
	private MediaCodec mAudioEncoder;
	private MediaFormat newFormat;
	private long prevPresentationTimes = 0;
	private long nanoTime = 0;//System.nanoTime();
	private LinkedBlockingQueue<RawData> queue = new LinkedBlockingQueue<>();
	private RawData bigShip;
	
	private int audioSampleRate;
	private int audioChannelConfig;
	private int audioBitRate;
	private int bufferSize = 3584;//1600;
	
	private AACEncodeConsumer(Builder builder) {
		this.audioSampleRate = builder.audioSampleRate;
		this.audioChannelConfig = builder.audioChannelConfig;
		this.audioBitRate = builder.audioBitRate;
		this.bufferSize = AudioRecord.getMinBufferSize(audioSampleRate, audioChannelConfig, audioBitRate);
	}
	
	
	synchronized void setTmpuMuxer(MyMediaMuxer mMuxer) {
		this.mMuxerRef = new WeakReference<>(mMuxer);
		
		MyMediaMuxer muxer = mMuxerRef.get();
		if (muxer != null && newFormat != null) {
			muxer.addTrack(newFormat, false);
		}
	}
	
	// queue数据没处理完时，先放到bigShip里，确保编码器消费速度
	public void addData(ByteBuffer byteBuffer, int length) {
		if (bigShip == null) {
			bigShip = new RawData();
			bigShip.merge(byteBuffer, length);
			if (queue.isEmpty()) {
				queue.offer(bigShip);
				bigShip = null;
			}
		} else {
			if (bigShip.canMerge(length)) {
				bigShip.merge(byteBuffer, length);
			} else {
				queue.offer(bigShip);
				bigShip = null;
			}
		}
	}
	
	private RawData removeData() {
		return queue.poll();
	}
	
	@Override
	public void run() {
		startCodec();
		while (!isExit) {
			try {
				RawData data = removeData();
				if (data != null) {
					Log.d("encode", "onWebRtcAudioRecording take data");
					encoderBytes(data.buf, data.readBytes, data.timeStamp);
				}
				
				MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
				int outputBufferIndex;
				do {
					outputBufferIndex = mAudioEncoder.dequeueOutputBuffer(mBufferInfo, TIMES_OUT);
					if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
						//                Log.i(TAG, "INFO_TRY_AGAIN_LATER");
					} else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
						Log.i(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
						synchronized (AACEncodeConsumer.this) {
							newFormat = mAudioEncoder.getOutputFormat();
							if (mMuxerRef != null) {
								MyMediaMuxer muxer = mMuxerRef.get();
								if (muxer != null) {
									muxer.addTrack(newFormat, false);
								}
							}
						}
					} else {
						if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
							mBufferInfo.size = 0;
						}
						if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
							
							Log.i(TAG, "数据流结束，退出循环");
							break;
						}
						ByteBuffer outputBuffer = mAudioEncoder.getOutputBuffer(outputBufferIndex);
						if (mBufferInfo.size != 0) {
							if (outputBuffer == null) {
								throw new RuntimeException("encodecOutputBuffer" + outputBufferIndex + "was null");
							}
							if (mMuxerRef != null) {
								MyMediaMuxer muxer = mMuxerRef.get();
								if (muxer != null) {
									Log.i(TAG, "------编码混合音频数据------------" + mBufferInfo.presentationTimeUs / 1000);
									muxer.pumpStream(outputBuffer, mBufferInfo, false);
								}
							}
						}
						mAudioEncoder.releaseOutputBuffer(outputBufferIndex, false);
					}
				} while (outputBufferIndex >= 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		stopCodec();
	}
	
	@SuppressLint("NewApi")
	public void encoderBytes(byte[] audioBuf, int readBytes, long timeStamp) {
		int inputBufferIndex = mAudioEncoder.dequeueInputBuffer(TIMES_OUT);
		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = mAudioEncoder.getInputBuffer(inputBufferIndex);
			
			if (audioBuf == null || readBytes <= 0) {
				mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, 0, System.nanoTime() / 1000, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
			} else {
				if (inputBuffer != null) {
					inputBuffer.clear();
					inputBuffer.put(audioBuf);
				}
				Log.e("chao", "audio set pts-------" + timeStamp / 1000 / 1000);
				mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, readBytes, System.nanoTime() / 1000, 0);
				
			}
		}
		
		
	}
	
	private void startCodec() {
		MediaCodecInfo mCodecInfo = selectSupportCodec(MIME_TYPE);
		if (mCodecInfo == null) {
			return;
		}
		try {
			mAudioEncoder = MediaCodec.createByCodecName(mCodecInfo.getName());
			MediaFormat mediaFormat = new MediaFormat();
			mediaFormat.setString(MediaFormat.KEY_MIME, MIME_TYPE);
			mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, audioBitRate);
			mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, audioSampleRate);
			mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, ACC_PROFILE);
			mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, audioChannelConfig);
			mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize * 2);
			if (mAudioEncoder != null) {
				mAudioEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
				mAudioEncoder.start();
				isEncoderStarted = true;
			}
		} catch (Exception e) {
			Log.e(TAG, "startCodec" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void stopCodec() {
		try {
			if (mAudioEncoder != null) {
				mAudioEncoder.stop();
				mAudioEncoder.release();
				mAudioEncoder = null;
			}
		} catch (Exception e) {
			// 捕获release()方法抛出异常
		}
		isEncoderStarted = false;
	}
	
	public void exit() {
		isExit = true;
	}
	
	/**
	 * 遍历所有编解码器，返回第一个与指定MIME类型匹配的编码器
	 * 判断是否有支持指定mime类型的编码器
	 */
	private MediaCodecInfo selectSupportCodec(String mimeType) {
		int numCodecs = MediaCodecList.getCodecCount();
		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
			if (!codecInfo.isEncoder()) {
				continue;
			}
			String[] types = codecInfo.getSupportedTypes();
			for (int j = 0; j < types.length; j++) {
				if (types[j].equalsIgnoreCase(mimeType)) {
					return codecInfo;
				}
			}
		}
		return null;
	}
	
	static class RawData {
		byte[] buf;
		int readBytes;
		long timeStamp;
		
		RawData() {
			buf = new byte[3584];
		}
		
		void merge(ByteBuffer byteBuffer, int length) {
			System.arraycopy(byteBuffer.array(), byteBuffer.arrayOffset(), buf, readBytes, length);
			readBytes += length;
			timeStamp = System.nanoTime();
		}
		
		boolean canMerge(int length) {
			return readBytes + length < buf.length;
		}
		
	}
	
	public static class Builder {
		private int audioSampleRate;
		private int audioChannelConfig;
		private int audioBitRate;
		
		public Builder audioSampleRate(int audioSampleRate) {
			this.audioSampleRate = audioSampleRate;
			return this;
		}
		
		public Builder audioChannelConfig(int audioChannelCount) {
			this.audioChannelConfig = audioChannelCount;
			return this;
		}
		
		public Builder audioBitRate(int audioBitRate) {
			this.audioBitRate = audioBitRate;
			return this;
		}
		
		public AACEncodeConsumer build() {
			return new AACEncodeConsumer(this);
		}
		
	}
	
}
