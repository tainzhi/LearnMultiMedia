package com.tainzhi.sample.media.tool;

import android.media.AudioFormat;
import android.media.AudioRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author: tainzhi
 * @mail: qfq61@qq.com
 * @date: 2019-11-22 06:26
 * @description: 将音频数据 pcm 转换成 wav 格式
 **/

public class PcmToWav {
	private int bufferSize;
	private int sampleRate;
	private int channel;
	
	public PcmToWav(int sampleRate, int channel, int encodeFormat) {
		this.sampleRate = sampleRate;
		this.channel = channel;
		this.bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encodeFormat);
	}
	
	public boolean pcmToWav(String inFileName, String outFileName) {
		FileInputStream in;
		FileOutputStream out;
		long totalAudioLen;
		long totalDataLen;
		long longSampleRate = sampleRate;
		int _channel = channel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
		long byteRate = 16 * sampleRate * _channel / 8;
		byte[] data = new byte[bufferSize];
		try {
			File file;
			in = new FileInputStream(inFileName);
			out = new FileOutputStream(outFileName);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			
			addWavFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, _channel, byteRate);
			while (in.read(data) != -1) {
				out.write(data);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private void addWavFileHeader(FileOutputStream out, long totalAudioLen,
	                              long totalDataLen, long longSampleRate, int channel,
	                              long byteRate) {
		try {
			byte[] header = new byte[44];
			// RIFF/WAVE header
			header[0] = 'R';
			header[1] = 'I';
			header[2] = 'F';
			header[3] = 'F';
			header[4] = (byte) (totalDataLen & 0xff);
			header[5] = (byte) ((totalDataLen >> 8) & 0xff);
			header[6] = (byte) ((totalDataLen >> 16) & 0xff);
			header[7] = (byte) ((totalDataLen >> 24) & 0xff);
			//WAVE
			header[8] = 'W';
			header[9] = 'A';
			header[10] = 'V';
			header[11] = 'E';
			// 'fmt ' chunk
			header[12] = 'f';
			header[13] = 'm';
			header[14] = 't';
			header[15] = ' ';
			// 4 bytes: size of 'fmt ' chunk
			header[16] = 16;
			header[17] = 0;
			header[18] = 0;
			header[19] = 0;
			// format = 1
			header[20] = 1;
			header[21] = 0;
			header[22] = (byte) channel;
			header[23] = 0;
			header[24] = (byte) (longSampleRate & 0xff);
			header[25] = (byte) ((longSampleRate >> 8) & 0xff);
			header[26] = (byte) ((longSampleRate >> 16) & 0xff);
			header[27] = (byte) ((longSampleRate >> 24) & 0xff);
			header[28] = (byte) (byteRate & 0xff);
			header[29] = (byte) ((byteRate >> 8) & 0xff);
			header[30] = (byte) ((byteRate >> 16) & 0xff);
			header[31] = (byte) ((byteRate >> 24) & 0xff);
			// block align
			header[32] = (byte) (2 * 16 / 8);
			header[33] = 0;
			// bits per sample
			header[34] = 16;
			header[35] = 0;
			//data
			header[36] = 'd';
			header[37] = 'a';
			header[38] = 't';
			header[39] = 'a';
			header[40] = (byte) (totalAudioLen & 0xff);
			header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
			header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
			header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
			out.write(header, 0, 44);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
