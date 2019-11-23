package com.tainzhi.sample.media.encodemp4;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author: tainzhi
 * @mail: qfq61@qq.com
 * @date: 2019-11-22 15:31
 * @description: Mp4封装混合器
 **/

public class MediaMuxerUtil {
	private static final String TAG = MediaMuxerUtil.class.getSimpleName();
	private final long durationMills;
	private MediaMuxer muxer;
	private int videoTrackIndex = -1;
	private int audioTrackIndex = -1;
	private long beginMills;
	
	public MediaMuxerUtil(String path, long durationMills) {
		this.durationMills = durationMills;
		try {
			muxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void addTrack(MediaFormat format, boolean isVideo) {
		if (muxer == null) {return; }
		
		if (audioTrackIndex != -1 && videoTrackIndex != -1) {
			return;
		}
		
		int track = muxer.addTrack(format);
		Log.i(TAG, String.format("addTrack %s result %d", isVideo ? "video" : "audio", track));
		if (isVideo) {
			videoTrackIndex = track;
			if (audioTrackIndex != -1) {
				Log.i(TAG, "both audio and video added, and muxer is started");
				muxer.start();
				beginMills = System.currentTimeMillis();
			}
		} else {
			audioTrackIndex = track;
			if (videoTrackIndex != -1) {
				muxer.start();
				beginMills = System.currentTimeMillis();
			}
		}
	}
	
	public synchronized void pumpStream(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo
			, boolean isVideo) {
		if (beginMills > 0) {
			try {
				pump(outputBuffer, bufferInfo, isVideo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void pump(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo, boolean isVideo) {
		if (muxer == null) {return;}
		
		if (audioTrackIndex == -1 || videoTrackIndex == -1) {return;}
		
		if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
			// The codec config data was pulled out and fed to the muxer when we got
			// the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
		} else if (bufferInfo.size != 0) {
			outputBuffer.position(bufferInfo.offset);
			outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
			
			muxer.writeSampleData(isVideo ? videoTrackIndex : audioTrackIndex, outputBuffer, bufferInfo);
			Log.d(TAG, String.format("sent %s [" + bufferInfo.size + "] with timestamp:[%d] to " +
					                         "muxer", isVideo ? "video" : "audio",
					bufferInfo.presentationTimeUs / 1000));
		}
		
		if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
			Log.i(TAG, "BUFFER_FLAG_END_OF_STREAM received");
		}
		
		if (System.currentTimeMillis() - beginMills >= durationMills) {
			muxer.stop();
			muxer.release();
			muxer = null;
			videoTrackIndex = audioTrackIndex = -1;
		}
	}
	
	public synchronized void release() {
		if (muxer != null) {
			if (audioTrackIndex != -1 && videoTrackIndex != -1) {
				Log.i(TAG, String.format(("muxer is started. now it will be stoped")));
				try {
					muxer.stop();
					muxer.release();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}
		} else {
			Log.i(TAG, "muxer is failed to be stopped");
		}
	}
}
