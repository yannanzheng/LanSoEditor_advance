#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform float uTexelWidthOffset;

varying vec2 vTextureCoord;
varying vec2 vExtraTextureCoord;

uniform sampler2D sTexture;
uniform sampler2D uExtraTexture;
void main() {
    vec4 base;
    vec4 overlay = texture2D(uExtraTexture, vExtraTextureCoord);
    vec4 outputColor;

    if (vTextureCoord.x < 0.5) {
        base = texture2D(sTexture, vec2(vTextureCoord.x, vTextureCoord.y * uTexelWidthOffset));
    }
    else {
        base = texture2D(sTexture, vec2((vTextureCoord.x-0.5), vTextureCoord.y * uTexelWidthOffset));
    }

    outputColor.r = overlay.r + base.r * base.a * (1.0 - overlay.a);
    outputColor.g = overlay.g + base.g * base.a * (1.0 - overlay.a);
    outputColor.b = overlay.b + base.b * base.a * (1.0 - overlay.a);
    outputColor.a = overlay.a + base.a * (1.0 - overlay.a);

    gl_FragColor = outputColor;
}