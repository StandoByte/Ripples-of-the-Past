#version 110

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;

uniform vec2 InSize;

void main() {
    vec4 rgb = texture2D(DiffuseSampler, texCoord);
    float rTmp = rgb.r;
    rgb.r = rgb.g;
    rgb.g = rTmp;
    gl_FragColor = vec4(rgb.rgb, 1.0);
}
