#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform float uTexelWidthOffset;

varying vec2 vTextureCoord;
varying vec2 vExtraTextureCoord;
varying vec2 vExtraVideoTextureCoord;

uniform samplerExternalOES uTexture;
uniform samplerExternalOES uExtraVideoTexture;
uniform sampler2D uExtraTexture;

void main() {
    vec4 base = texture2D(uTexture, vec2(vTextureCoord.x, vTextureCoord.y));
    vec4 overlay = texture2D(uExtraTexture, vExtraTextureCoord);
    vec4 video = texture2D(uExtraVideoTexture,vExtraVideoTextureCoord);

    vec4 outputColor =vec4(video.xyz,overlay.a);
//    outputColor.r = overlay.r + base.r * base.a * (1.0 - overlay.a);
//    outputColor.g = overlay.g + base.g * base.a * (1.0 - overlay.a);
//    outputColor.b = overlay.b + base.b * base.a * (1.0 - overlay.a);
//    outputColor.a = overlay.a + base.a * (1.0 - overlay.a);

    vec4 outputVideoColor =(1.0-outputColor.a)*base + outputColor*outputColor.a;
//    outputVideoColor.r = outputColor.r + base.r * base.a * (1.0 - outputColor.a);
//    outputVideoColor.g = outputColor.g + base.g * base.a * (1.0 - outputColor.a);
//    outputVideoColor.b = outputColor.b + base.b * base.a * (1.0 - outputColor.a);
//    outputVideoColor.a = outputColor.a + base.a * (1.0 - outputColor.a);

    gl_FragColor = outputVideoColor;
}