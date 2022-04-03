#ifdef GL_ES
precision highp float;
#endif

uniform sampler2D u_texture;
uniform float x;
uniform float z;

//input from vertex shader
varying vec2 v_texCoords;
varying vec4 v_color;
varying vec3 v_pos;

void main() {
    gl_FragColor = vec4(x/256., z/256., 1. , 1.);

}