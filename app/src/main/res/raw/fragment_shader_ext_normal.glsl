#extension GL_OES_EGL_image_external : require
precision mediump float; //指定默认精度

uniform float uTexelWidthOffset;

varying vec2 textureCoordinate;
uniform sampler2D uTexture;

void main() {
    gl_FragColor = texture2D(uTexture, vec2(textureCoordinate.x, textureCoordinate.y * uTexelWidthOffset));
}