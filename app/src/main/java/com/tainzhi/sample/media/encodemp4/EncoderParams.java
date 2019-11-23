package com.tainzhi.sample.media.encodemp4;

/**
 * @author: tainzhi
 * @mail: qfq61@qq.com
 * @date: 2019-11-22 14:50
 * @description: 音, 视频参数
 **/

public class EncoderParams {
	private String videoPath;
	private int frameWidth;
	private int frameHeight;
	private int bitRate;
	private int frameRate;
	private boolean isVertical;
	
	private String picPath; //图片实拍地址
	
	private int audioBitRate; //音频编码比特率
	private int audioChannelCount; //通道数据
	private int audioSampleRate; //采样率
	private int audioChannelConfig; //单声道或立体声
	private int audioFormat; //采样精度
	private int audioSource; //音频采样
	
	public EncoderParams() {}
	
	public String getVideoPath() {
		return videoPath;
	}
	
	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}
	
	public int getFrameWidth() {
		return frameWidth;
	}
	
	public void setFrameWidth(int frameWidth) {
		this.frameWidth = frameWidth;
	}
	
	public int getFrameHeight() {
		return frameHeight;
	}
	
	public void setFrameHeight(int frameHeight) {
		this.frameHeight = frameHeight;
	}
	
	public int getBitRate() {
		return bitRate;
	}
	
	public void setBitRate(int bitRate) {
		this.bitRate = bitRate;
	}
	
	public int getFrameRate() {
		return frameRate;
	}
	
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}
	
	public boolean isVertical() {
		return isVertical;
	}
	
	public void setVertical(boolean vertical) {
		isVertical = vertical;
	}
	
	public String getPicPath() {
		return picPath;
	}
	
	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}
	
	public int getAudioBitRate() {
		return audioBitRate;
	}
	
	public void setAudioBitRate(int audioBitRate) {
		this.audioBitRate = audioBitRate;
	}
	
	public int getAudioChannelCount() {
		return audioChannelCount;
	}
	
	public void setAudioChannelCount(int audioChannelCount) {
		this.audioChannelCount = audioChannelCount;
	}
	
	public int getAudioSampleRate() {
		return audioSampleRate;
	}
	
	public void setAudioSampleRate(int audioSampleRate) {
		this.audioSampleRate = audioSampleRate;
	}
	
	public int getAudioChannelConfig() {
		return audioChannelConfig;
	}
	
	public void setAudioChannelConfig(int audioChannelConfig) {
		this.audioChannelConfig = audioChannelConfig;
	}
	
	public int getAudioFormat() {
		return audioFormat;
	}
	
	public void setAudioFormat(int audioFormat) {
		this.audioFormat = audioFormat;
	}
	
	public int getAudioSource() {
		return audioSource;
	}
	
	public void setAudioSource(int audioSource) {
		this.audioSource = audioSource;
	}
}
