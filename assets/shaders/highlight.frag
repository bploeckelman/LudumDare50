#ifdef GL_ES
precision highp float;
#endif

//input from vertex shader
varying vec2 v_texCoords;
varying vec4 v_color;
varying vec3 v_pos;

const float borderDist = .1;

void main() {
    vec4 color = vec4(.2, .7, 1., 1.);
    float borderX = max(smoothstep( borderDist, .05, v_texCoords.x), smoothstep(1. - borderDist, .95, v_texCoords.x));
    float borderY = max(smoothstep( borderDist, .05, v_texCoords.y), smoothstep(1. - borderDist, .95, v_texCoords.y));
    gl_FragColor = color * max(borderX, borderY);

}