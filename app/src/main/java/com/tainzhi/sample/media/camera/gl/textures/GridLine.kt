package com.tainzhi.sample.media.camera.gl.textures

import android.graphics.RectF

class GridLine() : Texture() {
    private val components = mutableListOf<Texture>()
    private val type = 3

    fun build(previewRectF: RectF) {
        when (type) {
            // 对角线
            0 -> {
                components.add(
                    LineTexture(
                        Vertex3F(previewRectF.left, previewRectF.top, 0f),
                        Vertex3F(previewRectF.right, previewRectF.bottom, 0f)
                    )
                )
                components.add(
                    LineTexture(
                        Vertex3F(previewRectF.right, previewRectF.top, 0f),
                        Vertex3F(previewRectF.left, previewRectF.bottom, 0f)
                    )
                )
            }
            // 2x2均分线
            1 -> {
                components.add(
                    LineTexture(
                        Vertex3F(previewRectF.centerX(), previewRectF.top, 0f),
                        Vertex3F(previewRectF.centerX(), previewRectF.bottom, 0f)
                    )
                )
                components.add(
                    LineTexture(
                        Vertex3F(previewRectF.left, previewRectF.centerY(), 0f),
                        Vertex3F(previewRectF.right, previewRectF.centerY(), 0f),
                    )
                )
            }
            // 3x3 均分线
            2 -> {
                components.add(
                    LineTexture(
                        Vertex3F(previewRectF.left, previewRectF.top + previewRectF.height() /3f, 0f),
                        Vertex3F(previewRectF.right, previewRectF.top + previewRectF.height() /3f, 0f)
                    )
                )
                components.add(
                    LineTexture(
                        Vertex3F(previewRectF.left, previewRectF.top + previewRectF.height() *2 /3f, 0f),
                        Vertex3F(previewRectF.right, previewRectF.top + previewRectF.height() *2/3f, 0f)
                    )
                )
                components.add(
                    LineTexture(
                        Vertex3F(previewRectF.left + previewRectF.width()/3f, previewRectF.top , 0f),
                        Vertex3F(previewRectF.left + previewRectF.width()/3f, previewRectF.bottom, 0f)
                    )
                )
                components.add(
                    LineTexture(
                        Vertex3F(previewRectF.left + previewRectF.width() * 2/3f, previewRectF.top , 0f),
                        Vertex3F(previewRectF.left + previewRectF.width() * 2/3f, previewRectF.bottom, 0f)
                    )
                )
            }
            // 3x3 golden
            3 -> {
                val widthGolden = previewRectF.width() * GOLDEN_SPLIT_RATIO.toFloat()
                val heightGolden = previewRectF.height() * GOLDEN_SPLIT_RATIO.toFloat()
                components.add(
                    LineTexture(
                        Vertex3F(previewRectF.left + widthGolden, previewRectF.top , 0f),
                        Vertex3F(previewRectF.left + widthGolden, previewRectF.bottom, 0f)
                    )
                )
                components.add(
                    LineTexture(
                        Vertex3F(previewRectF.right - widthGolden, previewRectF.top , 0f),
                        Vertex3F(previewRectF.right - widthGolden, previewRectF.bottom, 0f)
                    )
                )
                components.add(
                    LineTexture(
                        Vertex3F(previewRectF.left, previewRectF.top + heightGolden , 0f),
                        Vertex3F(previewRectF.right, previewRectF.top + heightGolden, 0f)
                    )
                )
                components.add(
                    LineTexture(
                        Vertex3F(previewRectF.left, previewRectF.bottom - heightGolden , 0f),
                        Vertex3F(previewRectF.right,previewRectF.bottom - heightGolden, 0f)
                    )
                )
            }
        }
    }

    override fun onCreate() {
        components.forEach {
            it.create()
            it.setMatrix(modelMatrix, viewMatrix, projectionMatrix)
        }
    }

    override fun onDraw() {
        components.forEach {
            it.draw()
        }
    }

    companion object {
        private val GOLDEN_SPLIT_RATIO = 0.618
    }
}