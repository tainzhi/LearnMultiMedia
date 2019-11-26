package com.tainzhi.sample.media.opengl2.paint

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-11-25 23:20
 * @description:
 **/

interface PPgles {
    fun init(program: Int, vertexShader: Int, fragmentShader: Int)
    val vertexShader:String
    val fragmentShader: String
    fun draw()
}
