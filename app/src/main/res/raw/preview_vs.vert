attribute vec4 a_Position;
attribute vec2 a_TexturePosition;
uniform mat4 u_ModelMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ProjectionMatrix;
uniform mat4 u_TextureMatrix;
varying vec2 v_FragTexCoord;
void main(){
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix *a_Position;
    v_FragTexCoord = (u_TextureMatrix*vec4(a_TexturePosition,0,1)).xy;
}
