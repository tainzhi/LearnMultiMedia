package com.tainzhi.sample.media

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tainzhi.sample.media.tool.PcmToWav
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


/*
   - AudioRecod录制音频PCM文件，比MediaRecord更底层
   - PCM文件需要添加wav文件头，才能被AudioTrack播放
   - AudioTrack有stream和staic两种播放方式
   - [参考：使用 AudioRecord 采集音频PCM并保存到文件](https://www.cnblogs.com/renhui/p/7457321.html)
   - [参考：使用 AudioTrack 播放PCM音频](https ://www.cnblogs.com/renhui/p/7463287.html)
 */
class AudioRecordPlayActivity : AppCompatActivity(), View.OnClickListener {
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val unGrantedPermissionList: MutableList<String> = ArrayList()
    private var btnRecod: Button? = null
    private var btnConvert: Button? = null
    private var btnPlay: Button? = null
    private var isRecording = false
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var handlerThread: HandlerThread? = null
    private var threadHandler: Handler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlerThread = HandlerThread(TAG)
        handlerThread!!.start()
        threadHandler = Handler(handlerThread!!.looper)
        setContentView(R.layout.activity_audio_record_play)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        btnRecod = findViewById(R.id.btn_record)
        btnConvert = findViewById(R.id.btn_convert)
        btnPlay = findViewById(R.id.btn_play)
        btnRecod?.setOnClickListener(this)
        btnConvert?.setOnClickListener(this)
        btnPlay?.setOnClickListener(this)
        btnRecod?.setEnabled(true)
        // record -> convert -> play
        btnConvert?.setEnabled(false)
        btnPlay?.setEnabled(false)
        checkPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (handlerThread != null) {
            handlerThread!!.quitSafely()
        }
    }

    override fun onClick(v: View) {
        val button = v as Button
        when (v.getId()) {
            R.id.btn_record -> if (button.text.toString() == getString(R.string.audio_start_record)) {
                startRecord()
                btnRecod!!.text = getString(R.string.audio_stop_record)
            } else {
                stopRecord()
            }

            R.id.btn_convert -> convert()
            R.id.btn_play -> if (button.text.toString() == getString(R.string.audio_start_play)) {
                playInModeStream()
                btnPlay!!.text = getString(R.string.audio_stop_play)
            } else {
                stopPlay()
            }

            else -> {}
        }
    }

    private fun stopRecord() {
        isRecording = false
        if (null != audioRecord) {
            audioRecord!!.stop()
            audioRecord!!.release()
            audioRecord = null
        }
    }

    private fun startRecord() {
        threadHandler!!.post {
            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE_INHX, CHANNEL_CONFIG,
                AUDIO_FORMAT
            )
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@post
            }
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHX,
                CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize
            )
            val data = ByteArray(minBufferSize)
            val file = File(filesDir, "audio_test.pcm")
            Log.d(TAG, file.absolutePath + " created")
            // if (!file.mkdir()) {
            // 	Log.e(TAG, "Directory not created");
            // }
            if (file.exists()) {
                file.delete()
            }
            audioRecord!!.startRecording()
            isRecording = true
            var os: FileOutputStream? = null
            try {
                os = FileOutputStream(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            if (os != null) {
                while (isRecording) {
                    val read = audioRecord!!.read(data, 0, minBufferSize)
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            os.write(data)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
                try {
                    Log.i(TAG, "run: close file ouput stream!")
                    os.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                // ui线程更新button
                // 等待convert转码
                runOnUiThread {
                    Log.d(TAG, "stop recording")
                    btnRecod!!.isEnabled = false
                    btnRecod!!.text = getString(R.string.audio_start_play)
                    btnConvert!!.isEnabled = true
                }
            }
        }
    }

    private fun convert() {
        threadHandler!!.post {
            Log.d(TAG, "start convert")
            val pcmToWav = PcmToWav(SAMPLE_RATE_INHX, CHANNEL_CONFIG, AUDIO_FORMAT)
            val pcmFile = File(
                filesDir,
                "audio_test.pcm"
            )
            val wavFile = File(filesDir, "audio_test.wav")
            // if (!wavFile.mkdirs()) {
            // 	Log.e(TAG, "wavFile Directory not crated");
            // }
            if (wavFile.exists()) {
                wavFile.delete()
            }
            val result = pcmToWav.pcmToWav(
                pcmFile.absolutePath,
                wavFile.absolutePath
            )
            if (result) {
                runOnUiThread {
                    Log.d(TAG, "stop convert, converted file=" + wavFile.absolutePath)
                    btnConvert!!.isEnabled = false
                    btnPlay!!.isEnabled = true
                }
            }
        }
    }

    /**
     * 用 stream 模式播放
     */
    private fun playInModeStream() {
        threadHandler!!.post {
            Log.d(TAG, "start play int stream mode")
            val channelConfig = AudioFormat.CHANNEL_OUT_MONO
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE_INHX, channelConfig,
                AUDIO_FORMAT
            )
            audioTrack = AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                AudioFormat.Builder().setSampleRate(SAMPLE_RATE_INHX)
                    .setEncoding(AUDIO_FORMAT)
                    .setChannelMask(channelConfig)
                    .build(),
                minBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            audioTrack!!.play()
            val file = File(filesDir, "audio_test.pcm")
            try {
                val fileInputStream = FileInputStream(file)
                try {
                    val tmpBuffer = ByteArray(minBufferSize)
                    while (fileInputStream.available() > 0) {
                        val readCount = fileInputStream.read(tmpBuffer)
                        if (readCount == AudioTrack.ERROR_BAD_VALUE ||
                            readCount == AudioTrack.ERROR_INVALID_OPERATION
                        ) {
                            continue
                        }
                        if (readCount != 0 && readCount != -1) {
                            audioTrack!!.write(tmpBuffer, 0, readCount)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            runOnUiThread {
                stopPlay()
                Log.d(TAG, "finish play in stream mode")
            }
        }
    }

    private fun checkPermissions() {
        // Marshmallow开始运行时申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    unGrantedPermissionList.add(permission)
                }
            }
        }
        if (!unGrantedPermissionList.isEmpty()) {
            val tmpPermissions = unGrantedPermissionList.toTypedArray()
            ActivityCompat.requestPermissions(this, tmpPermissions, MY_PERMISSIONS_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, permissions[i] + " 权限被用户禁止")
                }
            }
        }
        // TODO: 2019-11-22 运行时权限的申请
    }

    private fun stopPlay() {
        runOnUiThread {
            if (audioTrack != null) {
                audioTrack!!.stop()
                audioTrack!!.release()
                audioTrack = null
            }
            btnPlay!!.isEnabled = false
            btnPlay!!.text = getString(R.string.audio_start_play)
            btnRecod!!.text = getString(R.string.audio_start_record)
            btnRecod!!.isEnabled = true
        }
    }

    companion object {
        const val TAG = "AudioRecordPlayActivity"

        //采样率 44100HZ
        const val SAMPLE_RATE_INHX = 44100

        //声道数
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO

        //音频数据格式.
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val MY_PERMISSIONS_REQUEST = 1001
    }
}