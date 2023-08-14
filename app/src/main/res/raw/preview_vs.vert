attribute vec4 a_Position;
attribute vec2 a_TexturePosition;
uniform mat4 u_Matrix;
uniform mat4 u_TextureMatrix;
varying vec2 v_texturePostion;
void main(){
    gl_Position = u_Matrix*a_Position;
    v_texturePostion = (u_TextureMatrix*vec4(a_TexturePosition,0,1)).xy;
}
