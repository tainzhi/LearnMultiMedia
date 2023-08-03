package com.tainzhi.sample.media.camera.gl

import android.opengl.GLES20
import android.util.Log

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-11-23 13:17
 * @description:  shape base
 **/

open class BaseGLSL {

    companion object {
        private val TAG: String = BaseGLSL::class.java.simpleName
        const val COORDS_PER_VERTEX: Int = 3
        const val vertexStride: Int = COORDS_PER_VERTEX * 4

        private fun loadShader(type: Int, shaderCode: String): Int {
            val shader: Int = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }

        fun createOpenGLProgram(vertexSource: String, fragmentSource: String): Int {
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
    }
}