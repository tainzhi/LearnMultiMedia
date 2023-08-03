package com.tainzhi.sample.media.camera.gl.filter

open class NoFilter : BaseFilter() {
    var vertexCode = "attribute vec4 vPosition;\n" +
            "attribute vec2 vCoord;\n" +
            "uniform mat4 vMatrix;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main(){\n" +
            "    gl_Position = vMatrix*vPosition;\n" +
            "    textureCoordinate = vCoord;\n" +
            "}"
    var fragmentCode = "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D vTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D( vTexture, textureCoordinate );\n" +
            "}"

    override fun onCreate() {
        createProgram(vertexCode, fragmentCode)
    }

    override fun onSizeChanged(width: Int, height: Int) {}
}