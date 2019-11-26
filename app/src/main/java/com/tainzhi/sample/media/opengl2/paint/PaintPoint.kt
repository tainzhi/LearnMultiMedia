package com.tainzhi.sample.media.opengl2.paint

import android.opengl.GLES20

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-11-25 23:22
 * @description:
 **/

class PaintPoint : PPgles{
    private var mAPosition = 0
    private var mUFragmentColor = 0
    private val mPosition = floatArrayOf(0.0f, 0.0f)
    private val mColor = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)
    override val vertexShader = """
            attribute vec4 a_Position;
            void main() {
                gl_Position = a_Position;
                gl_PointSize = 50.0;
            }
            """
    override val fragmentShader = ("precision mediump float;"
            + "uniform vec4 u_FragmentColor;"
            + "void main() {"
            + "    gl_FragColor = u_FragmentColor;"
            + "}")

    override fun init(program: Int, vertexShader: Int, fragmentShader: Int) {
        mAPosition = GLES20.glGetAttribLocation(program, "a_Position")
        mUFragmentColor = GLES20.glGetUniformLocation(program, "u_FragmentColor")
    }

    override fun draw() {
        GLES20.glVertexAttrib3f(mAPosition, mPosition[0], mPosition[1], 0.0f)
        GLES20.glUniform4f(mUFragmentColor, mColor[0], mColor[1], mColor[2], mColor[3])
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
    }

    fun setPosition(x: Float, y: Float) {
        mPosition[0] = x
        mPosition[1] = y
    }

    fun setColor(r: Float, g: Float, b: Float, a: Float) {
        mColor[0] = r
        mColor[1] = g
        mColor[2] = b
        mColor[3] = a
    }


    companion object {
        private const val TAG = "CZPoint"
    }
}

