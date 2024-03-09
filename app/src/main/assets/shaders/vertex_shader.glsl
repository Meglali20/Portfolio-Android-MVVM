uniform mat4 uMVPMatrix;
attribute vec2 vPosition;
attribute vec2 aTextureCoord;

void main() {
    gl_Position = vec4(vPosition, 0, 1);
}