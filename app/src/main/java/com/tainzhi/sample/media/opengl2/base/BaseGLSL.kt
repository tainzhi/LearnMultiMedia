package com.tainzhi.sample.media.opengl2.base

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
        private const val TAG: String = "BaseGLSL"
        // 每个顶点的坐标数
        const val COORDS_PER_VERTEX: Int = 3
        // 每个顶点4个字节
        const val vertexStride: Int = COORDS_PER_VERTEX * 4

        /**
         * 加载着色器
         *
         * @param type       加载着色器类型
         * @param shaderCode 加载着色器的代码
         */
        fun loadShader(type: Int, shaderCode: String): Int {
            var shader: Int = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }

        /**
         * 生成OpenGL Program
         *
         * @param vertexSource   顶点着色器代码
         * @param fragmentSource 片元着色器代码
         * @return 生成的OpenGL Program，如果为0，则表示创建失败
         */
        fun createOpenGLProgram(vertexSource: String, fragmentSource: String): Int {
            var vertex: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
            if (vertex == 0) {
                Log.e(TAG, "load shader vertex failed")
                return 0
            }
            var fragment: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
            if (fragment == 0) {
                Log.e(TAG, "load shader fragment failed")
                return 0
            }

            var program: Int = GLES20.glCreateProgram();
            if (program != 0) {
                GLES20.glAttachShader(program, vertex)
                GLES20.glAttachShader(program, fragment)
                GLES20.glLinkProgram(program)
                var linkedStatus: IntArray = IntArray(1)
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