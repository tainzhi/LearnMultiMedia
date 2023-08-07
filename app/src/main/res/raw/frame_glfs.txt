precision mediump float;
varying vec4 v_Color;
uniform float u_Opacity;
void main() {
    gl_FragColor = v_Color * u_Opacity;
}