#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2 vTextureCoord;

uniform sampler2D sTexture;
void main() {
    vec4 base;

    if (vTextureCoord.x < 0.333) {
        base = texture2D(sTexture, vec2(vTextureCoord.x, vTextureCoord.y));
    }else if(vTextureCoord.x < 0.667 && vTextureCoord.x>0.333) {
        base = texture2D(sTexture, vec2((vTextureCoord.x-0.333), vTextureCoord.y));
    }else{
        base = texture2D(sTexture, vec2((vTextureCoord.x-0.667), vTextureCoord.y));
    }

    gl_FragColor = base;
}