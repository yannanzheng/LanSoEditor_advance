#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
varying vec2 vExtraTextureCoord;

uniform float uTexelWidthOffset;


uniform sampler2D sTexture;
uniform sampler2D uExtraTexture;

uniform vec2 uPosition;
void main() {
    vec4 base;
    vec4 overlay = texture2D(uExtraTexture, vExtraTextureCoord);
    vec4 outputColor;


    vec2 texCoord = vec2(vTextureCoord.x, vTextureCoord.y * uTexelWidthOffset);
    vec2 normCoord = 2.0 * texCoord - 1.0;
    normCoord.x = normCoord.x * sign(normCoord.x + uPosition.x);
    texCoord = normCoord / 2.0 + 0.5;

    base = texture2D(sTexture, texCoord);

    outputColor.r = overlay.r + base.r * base.a * (1.0 - overlay.a);
    outputColor.g = overlay.g + base.g * base.a * (1.0 - overlay.a);
    outputColor.b = overlay.b + base.b * base.a * (1.0 - overlay.a);
    outputColor.a = overlay.a + base.a * (1.0 - overlay.a);

    gl_FragColor = outputColor;
}