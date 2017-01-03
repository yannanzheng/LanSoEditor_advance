uniform mat4 uMVPMatrix;
uniform mat4 uTexMatrix;

attribute vec4 aPosition;

attribute vec4 aTextureCoord;
attribute vec4 aExtraTextureCoord;
attribute vec4 aExtraVideoTextureCoord;

varying vec2 vTextureCoord;
varying vec2 vExtraTextureCoord;
varying vec2 vExtraVideoTextureCoord;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vExtraTextureCoord = vec2(aExtraTextureCoord.x, 1.0 - aExtraTextureCoord.y);  //OpenGL纹理系统坐标 与 Android图像坐标 Y轴是颠倒的。这里旋转过来
    vExtraVideoTextureCoord = vec2(aExtraVideoTextureCoord.x, 1.0 - aExtraVideoTextureCoord.y);//aExtraVideoTextureCoord.xy;
    vTextureCoord = (uTexMatrix * aTextureCoord).xy;
}