package com.tainzhi.sample.media.opengl2.rotate

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/26 下午2:07
 * @description:
 **/

class RotateTriangleRenderer : GLSurfaceView.Renderer {
    private var mTriangle: RotateTriangle? = null
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private val mMVPMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mRotationMatrix = FloatArray(16)
    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    var angle = 0f

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) { // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        mTriangle = RotateTriangle()
    }

    override fun onDrawFrame(unused: GL10) {
        val scratch = FloatArray(16)
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
        // Create a rotation for the triangle
// Use the following code to generate constant rotation.
// Leave this code out when using TouchEvents.
// long time = SystemClock.uptimeMillis() % 4000L;
// float angle = 0.090f * ((int) time);
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0f, 0f, 1.0f)
        // Combine the rotation matrix with the projection and camera view
// Note that the mMVPMatrix factor *must be first* in order
// for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0)
        // Draw triangle
        mTriangle!!.draw(scratch)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) { // Adjust the viewport based on geometry changes,
// such as screen rotation
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        // this projection matrix is applied to object coordinates
// in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }
}