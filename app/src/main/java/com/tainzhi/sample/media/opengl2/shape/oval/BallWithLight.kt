package com.renhui.opengles20study.shape.oval

import android.opengl.GLES20
import android.opengl.Matrix
import com.tainzhi.sample.media.camera.gl.BaseGLSL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

/**
 * 带光源的球体
 */
class BallWithLight : BaseGLSL() {
    private val step = 2f
    private val vertexBuffer: FloatBuffer
    private val vSize: Int
    private var mProgram = 0
    private val mViewMatrix = FloatArray(16)
    private val mProjectMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)
    private fun createBallPos(): FloatArray { //球以(0,0,0)为中心，以R为半径，则球上任意一点的坐标为
// ( R * cos(a) * sin(b),y0 = R * sin(a),R * cos(a) * cos(b))
// 其中，a为圆心到点的线段与xz平面的夹角，b为圆心到点的线段在xz平面的投影与z轴的夹角
        val data = ArrayList<Float>()
        var r1: Float
        var r2: Float
        var h1: Float
        var h2: Float
        var sin: Float
        var cos: Float
        run {
            var i = -90f
            while (i < 90 + step) {
                r1 = Math.cos(i * Math.PI / 180.0).toFloat()
                r2 = Math.cos((i + step) * Math.PI / 180.0).toFloat()
                h1 = Math.sin(i * Math.PI / 180.0).toFloat()
                h2 = Math.sin((i + step) * Math.PI / 180.0).toFloat()
                // 固定纬度, 360 度旋转遍历一条纬线
                val step2 = step * 2
                var j = 0.0f
                while (j < 360.0f + step) {
                    cos = Math.cos(j * Math.PI / 180.0).toFloat()
                    sin = (-Math.sin(j * Math.PI / 180.0)).toFloat()
                    data.add(r2 * cos)
                    data.add(h2)
                    data.add(r2 * sin)
                    data.add(r1 * cos)
                    data.add(h1)
                    data.add(r1 * sin)
                    j += step2
                }
                i += step
            }
        }
        val f = FloatArray(data.size)
        for (i in f.indices) {
            f[i] = data[i]
        }
        return f
    }

    fun onSurfaceCreated() {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        mProgram = createOpenGLProgram(vertexShaderCode, fragmentShaderCode)
    }

    fun onSurfaceChanged(width: Int, height: Int) { //计算宽高比
        val ratio = width.toFloat() / height
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 20f)
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 10.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    fun draw() {
        GLES20.glClearColor(1.0f, 1.0f, 0.6f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)
        val mMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        GLES20.glUniformMatrix4fv(mMatrix, 1, false, mMVPMatrix, 0)
        val mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vSize)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    companion object {
        private const val vertexShaderCode = "uniform mat4 vMatrix;           //总变换矩阵\n" +
                "uniform mat4 uMMatrix;          //变换矩阵\n" +
                "uniform vec3 uLightLocation;    //光源位置\n" +
                "uniform vec3 uCamera;           //摄像机位置\n" +
                "attribute vec3 vPosition;       //顶点位置\n" +
                "attribute vec3 vNormal;         //法向量\n" +
                "varying vec4 vDiffuse;          //用于传递给片元着色器的散射光最终强度\n" +
                "\n" +
                "\n" +
                "//返回散射光强度\n" +
                "vec4 pointLight(vec3 normal,vec3 lightLocation,vec4 lightDiffuse){\n" +
                "    //变换后的法向量\n" +
                "    vec3 newTarget=normalize((vMatrix*vec4(normal+vPosition,1)).xyz-(vMatrix*vec4(vPosition,1)).xyz);\n" +
                "    //表面点与光源的方向向量\n" +
                "    vec3 vp=normalize(lightLocation-(vMatrix*vec4(vPosition,1)).xyz);\n" +
                "    return lightDiffuse*max(0.0,dot(newTarget,vp));\n" +
                "}\n" +
                "\n" +
                "void main(){\n" +
                "   gl_Position = vMatrix * vec4(vPosition,1); //根据总变换矩阵计算此次绘制此顶点位置\n" +
                "\n" +
                "   vec4 at=vec4(1.0,1.0,1.0,1.0);   //光照强度\n" +
                "   vec3 pos=vec3(80.0,80.0,80.0);      //光照位置\n" +
                "   vDiffuse=pointLight(normalize(vPosition),pos,at);\n" +
                "}"
        private const val fragmentShaderCode = "precision mediump float;\n" +
                "varying vec4 vDiffuse;\n" +
                "void main(){\n" +
                "   vec4 finalColor=vec4(1.0);\n" +
                "   gl_FragColor=finalColor*vDiffuse+finalColor*vec4(0.15,0.15,0.15,1.0);\n" +
                "}"
    }

    init {
        val dataPos = createBallPos()
        val buffer = ByteBuffer.allocateDirect(dataPos.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        vertexBuffer = buffer.asFloatBuffer()
        vertexBuffer.put(dataPos)
        vertexBuffer.position(0)
        vSize = dataPos.size / 3
    }
}