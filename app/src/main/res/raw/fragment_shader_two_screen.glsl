#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2 vTextureCoord;

uniform sampler2D sTexture;
void main() {
    vec4 base;

    if (vTextureCoord.x < 0.5) {
        base = texture2D(sTexture, vec2(vTextureCoord.x*1.5, vTextureCoord.y));
    }else {
        base = texture2D(sTexture, vec2((vTextureCoord.x-0.5)*1.5, vTextureCoord.y));
    }

    gl_FragColor = base;
}