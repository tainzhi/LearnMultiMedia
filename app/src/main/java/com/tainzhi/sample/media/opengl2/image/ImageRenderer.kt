package com.tainzhi.sample.media.opengl2.image

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import com.tainzhi.sample.media.opengl2.BaseGLSL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 图片展示渲染器
 */
class ImageRenderer(private val mContext: Context) : BaseGLSL(), GLSurfaceView.Renderer {
    
    var mBitmap: Bitmap? = null
    
    private var mProgram = 0
    private var glHPosition = 0
    private var glHTexture = 0
    private var glHCoordinate = 0
    private var glHMatrix = 0
    private var textureId = 0
    private val bPos: FloatBuffer
    private val bCoord: FloatBuffer
    private val mViewMatrix = FloatArray(16)
    private val mProjectMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_TEXTURE_2D)
        mProgram = createOpenGLProgram(vertexMatrixShaderCode, fragmentShaderCode)
        glHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition")
        glHCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate")
        glHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture")
        glHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glClearColor(1f, 1f, 1f,1f)
        GLES20.glViewport(0, 0, width, height)
        val w = mBitmap!!.width
        val h = mBitmap!!.height
        val sWH = w / h.toFloat()
        val sWidthHeight = width / height.toFloat()
        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        val scale: Float =  width.toFloat()/2f
        Matrix.scaleM(modelMatrix, 0, scale*w/h.toFloat(), scale, 1f)
        // modelMatrix = transMatrix * scalematrix
        // 先scale，再translate使得靠近屏幕下边缘
        val translate = (height - width) / 2f
        val transMatrix = FloatArray(16)
        Matrix.setIdentityM(transMatrix, 0)
        Matrix.translateM(transMatrix, 0,0f, -translate,0f)

        Matrix.multiplyMM(modelMatrix, 0, transMatrix, 0, modelMatrix, 0)
        Log.d(TAG, "onSurfaceChanged: width:$width,height:$height,bitmapW:$w, bitmapH:$h")
        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1f, 1f, 3f, 7f)
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1f, 1f, 3f, 7f)
            }
        } else {
            if (sWH > sWidthHeight) {
                // Matrix.orthoM(mProjectMatrix, 0, -1/2f, 1/2f, -1 / sWidthHeight * sWH * 1/2f, 1 / sWidthHeight * sWH * 1/2f, 3f, 7f)
                Matrix.orthoM(mProjectMatrix, 0, -width.toFloat()/2f, width.toFloat()/2f, -height.toFloat()/2f,height.toFloat()/2f, -1f, 70f)
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -sWH / sWidthHeight, sWH / sWidthHeight, 3f, 7f)
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mMVPMatrix, 0)
        logMatrix("modelMatrix", modelMatrix)
        logMatrix("viewMatrix", mViewMatrix)
        logMatrix("proMatrix", mProjectMatrix)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)
        GLES20.glUniformMatrix4fv(glHMatrix, 1, false, mMVPMatrix, 0)
        GLES20.glEnableVertexAttribArray(glHPosition)
        GLES20.glEnableVertexAttribArray(glHCoordinate)
        GLES20.glUniform1i(glHTexture, 0)
        textureId = createTexture()
        //传入顶点坐标
        GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, bPos)
        //传入纹理坐标
        GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, bCoord)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun createTexture(): Int {
        val texture = IntArray(1)
        if (mBitmap != null && !mBitmap!!.isRecycled) { //生成纹理
            GLES20.glGenTextures(1, texture, 0)
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0])
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)
            return texture[0]
        }
        return 0
    }

    companion object {
        private val TAG = ImageRenderer::class.java.simpleName
        private const val vertexMatrixShaderCode = "attribute vec4 vPosition;\n" +
                "attribute vec2 vCoordinate;\n" +
                "uniform mat4 vMatrix;\n" +
                "varying vec2 aCoordinate;\n" +
                "void main(){\n" +
                "    gl_Position=vMatrix*vPosition;\n" +
                "    aCoordinate=vCoordinate;\n" +
                "}"
        private const val fragmentShaderCode = "precision mediump float;\n" +
                "uniform sampler2D vTexture;\n" +
                "varying vec2 aCoordinate;\n" +
                "void main(){\n" +
                "    gl_FragColor=texture2D(vTexture,aCoordinate);\n" +
                "}"
        private val sPos = floatArrayOf(
                -1.0f, 1.0f,  //左上角
                -1.0f, -1.0f,  //左下角
                1.0f, 1.0f,  //右上角
                1.0f, -1.0f //右下角
        )
        private val sCoord = floatArrayOf(
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f)
        private fun logMatrix(name: String, floatArray: FloatArray) {
            if (floatArray.size == 16) {
                val stringBuilder = StringBuilder()
                for (i in 0 until 4) {
                    for (j in 0 until 4) {
                        val index = i * 4 + j
                        stringBuilder.append(floatArray[index]).append(" ")
                    }
                    stringBuilder.append("\n")
                }
                Log.d(TAG, "Matrix($name):\n$stringBuilder")
            } else {
                Log.e(TAG, "The FloatArray should have exactly 16 elements.")
            }
        }
    }

    init {
        val bb = ByteBuffer.allocateDirect(sPos.size * 4)
        bb.order(ByteOrder.nativeOrder())
        bPos = bb.asFloatBuffer()
        bPos.put(sPos)
        bPos.position(0)
        val cc = ByteBuffer.allocateDirect(sCoord.size * 4)
        cc.order(ByteOrder.nativeOrder())
        bCoord = cc.asFloatBuffer()
        bCoord.put(sCoord)
        bCoord.position(0)
    }
}