uniform mat4 u_ModelMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ProjectionMatrix;
attribute vec4 a_Position;
uniform vec4 u_Color;
varying vec4 v_Color;
void main() {
    v_Color = u_Color;
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix  * a_Position;
}