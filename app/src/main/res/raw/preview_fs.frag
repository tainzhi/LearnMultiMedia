#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;

in vec2 v_FragTexCoord;
out vec4 outColor;
uniform vec2 u_TextureSize;
uniform samplerExternalOES u_TextureSampler;
//uniform sampler2D u_TextureLUT;
uniform int u_IsTrueAspectRatio;
uniform int u_IsLUT;

// from https://iquilezles.org/articles/distfunctions
// reference: https://www.shadertoy.com/view/WtdSDs
float roundedBoxSDF(vec2 CenterPosition, vec2 Size, float Radius) {
    return length(max(abs(CenterPosition) - Size + Radius, 0.0)) - Radius;
}

//vec4 lookupTable(vec4 color) {
//    float blueColor = color.b * 63.0;
//
//    vec2 quad1;
//    quad1.y = floor(floor(blueColor) / 8.0);
//    quad1.x = floor(blueColor) - (quad1.y * 8.0);
//    vec2 quad2;
//    quad2.y = floor(ceil(blueColor) / 8.0);
//    quad2.x = ceil(blueColor) - (quad2.y * 8.0);
//
//    vec2 texPos1;
//    texPos1.x = (quad1.x * 0.125) + 0.5 / 512.0 + ((0.125 - 1.0 / 512.0) * color.r);
//    texPos1.y = (quad1.y * 0.125) + 0.5 / 512.0 + ((0.125 - 1.0 / 512.0) * color.g);
//    vec2 texPos2;
//    texPos2.x = (quad2.x * 0.125) + 0.5 / 512.0 + ((0.125 - 1.0 / 512.0) * color.r);
//    texPos2.y = (quad2.y * 0.125) + 0.5 / 512.0 + ((0.125 - 1.0 / 512.0) * color.g);
//    vec4 newColor1 = texture(u_textureLUT, texPos1);
//    vec4 newColor2 = texture(u_textureLUT, texPos2);
//    vec4 newColor = mix(newColor1, newColor2, fract(blueColor));
//    return vec4(newColor.rgb, color.w);
//}

void main() {
    vec4 mixColor;
    if (u_IsTrueAspectRatio == 1)
    {
        // make round corner
        vec2 size = vec2(u_TextureSize.x, u_TextureSize.y);
        // bottom left as the original postion
        vec2 location = vec2(0.0, 0.0);
        float radius = 0.05 * u_TextureSize.x;
        float distance = roundedBoxSDF(v_FragTexCoord.xy * size - location - size / 2.0, size / 2.0, radius);
        float smoothedAlpha = smoothstep(0.0, 1.0, distance);
        mixColor = mix(texture(u_TextureSampler, v_FragTexCoord), vec4(0.0, 0.0, 0, smoothedAlpha), smoothedAlpha);
    }
    else {
        // todo: make round corner
        // 对某些机型需要输出1:1的texture，但是camera engine没有该比例的输出，只能通过shader截取，
        // 1:1 采样长方形image的正中心的正方形，其边长为长方形的短边
        float shortLength = min(u_TextureSize.x, u_TextureSize.y);
        // 计算采样区域左下角的坐标
        vec2 offset = (u_TextureSize - vec2(shortLength)) / 2.0;
        // 计算归一化纹理坐标
        vec2 normalizedTexCoord = (v_FragTexCoord * vec2(shortLength) + offset) / u_TextureSize;
        mixColor = texture(u_TextureSampler, normalizedTexCoord);
    }
//    if (u_IsLUT == 1) {
//        outColor = lookupTable(mixColor);
//    } else {
//        outColor = mixColor;
//    }
    outColor = mixColor;

}
