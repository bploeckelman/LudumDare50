#ifdef GL_ES
precision mediump float;
#endif

//input from vertex shader
varying vec2 v_texCoords;
varying vec4 v_color;
varying vec3 v_pos;


void main() {
    gl_FragColor = v_color;
}