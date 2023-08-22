package com.tainzhi.sample.media.camera.gl

import android.opengl.GLES20
import android.opengl.GLES30
import android.util.SparseArray
import androidx.core.util.isNotEmpty
import com.tainzhi.sample.media.R
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer

class Shader(val vsId: Int, val fsId: Int) {

    enum class ShaderType(val vsId: Int, val fsId: Int) {
        CAMERA_PREVIEW(R.raw.preview_vs, R.raw.preview_fs),
        FRAME(R.raw.frame_glvs, R.raw.frame_glfs)
    }
    class Factory {
        private val shaders = SparseArray<Shader>(ShaderType.values().size)

        fun loadShaders() {
            ShaderType.values().forEach {
                shaders.append(it.ordinal, Shader(it.vsId, it.fsId))
            }
        }

        fun clearShaders() {
            shaders.clear()
        }

        fun isLoaded() = shaders.isNotEmpty()

        fun getShader(shaderType: ShaderType): Shader = shaders[shaderType.ordinal]
    }

    class Cache {

        inner class ProgramBinary {
            private var format: Int = 0
            private var binary: ByteBuffer
            constructor(format: Int, binary: ByteBuffer) {
                this.format = format
                this.binary = binary
            }
            constructor(dis: DataInputStream) {
                this.format = dis.readInt()
                this.binary = ByteBuffer.allocate(dis.readInt())
                dis.readFully(binary.array(), 0, binary.limit())
            }

            private fun createProgram() {
                val program = GLES20.glCreateProgram()
                if (program != 0) {
                    GLES30.glProgramBinary(program, format,binary.rewind(), binary.limit())
                    val linkedStatus: IntArray = IntArray(1)
                    GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkedStatus, 0)
                    if (linkedStatus[0] != GLES20.GL_TRUE) {
                        GLES30.glDeleteProgram(program)
                    }
                }
            }

            private fun write(dos: DataOutputStream) {
                dos.writeInt(format)
                dos.writeInt(binary.limit())
                dos.write(binary.array(), 0, binary.limit())
            }
        }
    }
}