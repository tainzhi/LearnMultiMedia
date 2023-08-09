#extension GL_OES_EGL_image_external: require
precision mediump float;
varying vec2 textureCoordinate;
uniform samplerExternalOES vTexture;
void main() {
    float radius = 0.05;
    if (textureCoordinate.x > radius && textureCoordinate.x < (1.0 - radius) ||
    textureCoordinate.y > radius && textureCoordinate.y < (1.0 - radius)) {
        gl_FragColor = texture2D(vTexture, textureCoordinate);
    } else {
        if (textureCoordinate.x < radius && textureCoordinate.y < radius) {
            float center_x = radius;
            float center_y = radius;
            if (pow(radius, 2.0) > pow((textureCoordinate.x - center_x), 2.0) + pow((textureCoordinate.y - center_y), 2.0)) {
                gl_FragColor = texture2D(vTexture, textureCoordinate);
            }
        } else {
            discard;
        }
    }
}
