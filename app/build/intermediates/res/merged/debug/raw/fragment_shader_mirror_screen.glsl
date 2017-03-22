#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2 vTextureCoord;

uniform sampler2D sTexture;
uniform vec2 uPosition;

void main() {
    vec4 base;

    vec2 texCoord = vTextureCoord.xy;
    vec2 normCoord = 2.0 * texCoord - 1.0;
    normCoord.x = normCoord.x * sign(normCoord.x + uPosition.x);
    texCoord = normCoord / 2.0 + 0.5;

    base = texture2D(sTexture, texCoord);

    gl_FragColor = base;
}