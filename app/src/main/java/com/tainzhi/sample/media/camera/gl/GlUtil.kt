package com.tainzhi.sample.media.camera.gl

import android.opengl.GLES20
import android.util.Log
import com.tainzhi.sample.media.CamApp
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object GlUtil {

    private val TAG = GlUtil::class.java.simpleName
    const val COORDS_PER_VERTEX: Int = 3
    const val VERTEX_STRIDE: Int = COORDS_PER_VERTEX * 4

    var glVersion = 3

    fun getShaderSource(sourceId: Int): String {
        val sb = StringBuilder()
        val inputStream = CamApp.getInstance().resources.openRawResource(sourceId)
        val bufferReader = BufferedReader(InputStreamReader(inputStream))
        try {
            var read = bufferReader.readLine()
            while (read != null) {
                sb.append(read).append("\n")
                read = bufferReader.readLine()
            }
            sb.deleteCharAt(sb.length - 1)
        } catch (ioe: IOException) {
            Log.e(TAG, "error reading sharer: ${ioe}")
        }
        return sb.toString()
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader: Int = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] != GLES20.GL_TRUE) {
            val logLength = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_INFO_LOG_LENGTH, logLength, 0)
            // 输出编译错误日志
            Log.e(TAG, "loadShader($type) failed: ${GLES20.glGetShaderInfoLog(shader)}")
            // 可以进行一些清理工作，如删除着色器、程序等
        }
        return shader
    }

    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertex: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertex == 0) {
            Log.e(TAG, "load shader vertex failed")
            return 0
        }
        val fragment: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragment == 0) {
            Log.e(TAG, "load shader fragment failed")
            return 0
        }

        var program: Int = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, vertex)
            GLES20.glAttachShader(program, fragment)
            GLES20.glLinkProgram(program)
            val linkedStatus: IntArray = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkedStatus, 0)
            if (linkedStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not linked program: " + GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        return program
    }


    private fun getGlErrorName(error: Int): String {
        when (error) {
            GLES20.GL_NO_ERROR -> return "GL_NO_ERROR"
            GLES20.GL_INVALID_ENUM -> return "GL_INVALID_ENUM"
            GLES20.GL_INVALID_VALUE -> return "GL_INVALID_VALUE"
            GLES20.GL_INVALID_OPERATION -> return "GL_INVALID_OPERATION";
            GLES20.GL_INVALID_FRAMEBUFFER_OPERATION -> return "GL_INVALID_FRAMEBUFFER_OPERATION";
            GLES20.GL_OUT_OF_MEMORY -> return "GL_OUT_OF_MEMORY";
            else ->
                return String.format("glGetError(%x)", error);
        }
    }

    fun checkGlError(op: String) {
        val error = GLES20.glGetError();
        if (GLES20.GL_NO_ERROR != error) {
            val message = op + ": " + getGlErrorName(error);
            when (error) {
                GLES20.GL_OUT_OF_MEMORY ->
                    // The state of the GL is undefined after this error occurs.
                    throw OutOfMemoryError(message);
                GLES20.GL_INVALID_ENUM, GLES20.GL_INVALID_VALUE,
                GLES20.GL_INVALID_OPERATION, GLES20.GL_INVALID_FRAMEBUFFER_OPERATION -> {
                    // The offending command is ignored and should have no side effects.
                    Log.e(TAG, message, Throwable());
                    throw RuntimeException(message);
                }

                else -> {
                    // This should never happen.
                    throw RuntimeException(message)
                }
            }
        }
    }

    fun useProgram(program: Int) {
        GLES20.glUseProgram(program)
        checkGlError("glUseProgram")
    }
}