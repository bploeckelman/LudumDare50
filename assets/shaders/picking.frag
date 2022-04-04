#ifdef GL_ES
precision highp float;
#endif

uniform sampler2D u_texture;

//input from vertex shader
varying vec2 v_texCoords;
varying vec4 v_color;
varying vec3 v_pos;

void main() {
    gl_FragColor = vec4(v_color.g/256., v_color.b/256., v_color.a , 1.);

}