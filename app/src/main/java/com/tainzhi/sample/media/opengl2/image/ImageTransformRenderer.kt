package com.tainzhi.sample.media.opengl2.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import com.tainzhi.sample.media.opengl2.base.BaseGLSL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 图片处理渲染器
 */
class ImageTransformRenderer(private val mContext: Context, private val mFilter: Filter) : BaseGLSL(), GLSurfaceView.Renderer {
    private var hChangeType = 0
    private var hChangeColor = 0
    private var glHPosition = 0
    private var glHTexture = 0
    private var glHCoordinate = 0
    private var glHMatrix = 0
    private var glHUxy = 0
    private var textureId = 0
    private val mViewMatrix = FloatArray(16)
    private val mProjectMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)
    private var mProgram: Int
    private var uXY = 0f
    private val mBitmap: Bitmap?
    private val bPos: FloatBuffer
    private val bCoord: FloatBuffer
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_TEXTURE_2D)
        mProgram = createOpenGLProgram(vertexMatrixShaderCode, fragmentShaderCode)
        glHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition")
        glHCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate")
        glHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture")
        glHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        hChangeType = GLES20.glGetUniformLocation(mProgram, "vChangeType")
        hChangeColor = GLES20.glGetUniformLocation(mProgram, "vChangeColor")
        glHUxy = GLES20.glGetUniformLocation(mProgram, "uXY")
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val w = mBitmap!!.width
        val h = mBitmap.height
        val sWH = w / h.toFloat()
        val sWidthHeight = width / height.toFloat()
        uXY = sWidthHeight
        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1f, 1f, 3f, 7f)
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1f, 1f, 3f, 7f)
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3f, 7f)
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -sWH / sWidthHeight, sWH / sWidthHeight, 3f, 7f)
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)
        GLES20.glUniform1i(hChangeType, mFilter.type)
        GLES20.glUniform3fv(hChangeColor, 1, mFilter.data, 0)
        GLES20.glUniform1f(glHUxy, uXY)
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
        if (mBitmap != null && !mBitmap.isRecycled) { //生成纹理
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

    // 定义过滤器，用于对图片操作
    enum class Filter(// 放大镜
            val type: Int, val data: FloatArray) {
        NONE(0, floatArrayOf(0.0f, 0.0f, 0.0f)),  // 不做处理
        GRAY(1, floatArrayOf(0.299f, 0.587f, 0.114f)),  // 黑白效果
        COOL(2, floatArrayOf(0.0f, 0.0f, 0.1f)),  // 冷色调
        WARM(2, floatArrayOf(0.1f, 0.1f, 0.0f)),  // 暖色调
        BLUR(3, floatArrayOf(0.006f, 0.004f, 0.002f)),  // 模糊（毛玻璃效果）
        MAGN(4, floatArrayOf(0.1f, 0.3f, 0.4f));

        fun data(): FloatArray {
            return data
        }

    }

    companion object {
        private const val vertexMatrixShaderCode = "attribute vec4 vPosition;\n" +
                "attribute vec2 vCoordinate;\n" +
                "uniform mat4 vMatrix;\n" +
                "\n" +
                "varying vec2 aCoordinate;\n" +
                "varying vec4 aPos;\n" +
                "varying vec4 gPosition;\n" +
                "\n" +
                "void main(){\n" +
                "    gl_Position=vMatrix*vPosition;\n" +
                "    aPos=vPosition;\n" +
                "    aCoordinate=vCoordinate;\n" +
                "    gPosition=vMatrix*vPosition;\n" +
                "}"
        private const val fragmentShaderCode = "precision mediump float;\n" +
                "uniform sampler2D vTexture;\n" +
                "uniform int vChangeType;\n" +
                "uniform vec3 vChangeColor;\n" +
                "uniform int vIsHalf;\n" +
                "uniform float uXY;      //屏幕宽高比\n" +
                "varying vec4 gPosition;\n" +
                "varying vec2 aCoordinate;\n" +
                "varying vec4 aPos;\n" +
                "void modifyColor(vec4 color){\n" +
                "    color.r=max(min(color.r,1.0),0.0);\n" +
                "    color.g=max(min(color.g,1.0),0.0);\n" +
                "    color.b=max(min(color.b,1.0),0.0);\n" +
                "    color.a=max(min(color.a,1.0),0.0);\n" +
                "}\n" +
                "void main(){\n" +
                "    vec4 nColor=texture2D(vTexture,aCoordinate);\n" +
                "    if(aPos.x>0.0||vIsHalf==0){\n" +
                "        if(vChangeType==1){    //黑白图片\n" +
                "            float c=nColor.r*vChangeColor.r+nColor.g*vChangeColor.g+nColor.b*vChangeColor.b;\n" +
                "            gl_FragColor=vec4(c,c,c,nColor.a);\n" +
                "        }else if(vChangeType==2){    //简单色彩处理，冷暖色调、增加亮度、降低亮度等\n" +
                "            vec4 deltaColor=nColor+vec4(vChangeColor,0.0);\n" +
                "            modifyColor(deltaColor);\n" +
                "            gl_FragColor=deltaColor;\n" +
                "        }else if(vChangeType==3){    //模糊处理\n" +
                "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.r,aCoordinate.y-vChangeColor.r));\n" +
                "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.r,aCoordinate.y+vChangeColor.r));\n" +
                "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.r,aCoordinate.y-vChangeColor.r));\n" +
                "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.r,aCoordinate.y+vChangeColor.r));\n" +
                "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.g,aCoordinate.y-vChangeColor.g));\n" +
                "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.g,aCoordinate.y+vChangeColor.g));\n" +
                "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.g,aCoordinate.y-vChangeColor.g));\n" +
                "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.g,aCoordinate.y+vChangeColor.g));\n" +
                "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.b,aCoordinate.y-vChangeColor.b));\n" +
                "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.b,aCoordinate.y+vChangeColor.b));\n" +
                "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.b,aCoordinate.y-vChangeColor.b));\n" +
                "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.b,aCoordinate.y+vChangeColor.b));\n" +
                "            nColor/=13.0;\n" +
                "            gl_FragColor=nColor;\n" +
                "        }else if(vChangeType==4){  //放大镜效果\n" +
                "            float dis=distance(vec2(gPosition.x,gPosition.y/uXY),vec2(vChangeColor.r,vChangeColor.g));\n" +
                "            if(dis<vChangeColor.b){\n" +
                "                nColor=texture2D(vTexture,vec2(aCoordinate.x/2.0+0.25,aCoordinate.y/2.0+0.25));\n" +
                "            }\n" +
                "            gl_FragColor=nColor;\n" +
                "        }else{\n" +
                "            gl_FragColor=nColor;\n" +
                "        }\n" +
                "    }else{\n" +
                "        gl_FragColor=nColor;\n" +
                "    }\n" +
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
    }

    init {
        mBitmap = BitmapFactory.decodeStream(mContext.resources.assets.open("one_piece.jpg"))
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
        mProgram = createOpenGLProgram(vertexMatrixShaderCode, fragmentShaderCode)
    }
}