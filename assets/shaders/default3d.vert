attribute vec3 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
attribute vec3 a_normal;

uniform mat4 u_projTrans;

varying vec2 v_texCoords;
varying vec4 v_color;
varying vec3 v_pos;
varying vec3 v_normal;

void main()
{
    v_pos = a_position.xyz;
    v_color = a_color;
    v_texCoords = a_texCoord0;
    v_normal = a_normal;
    gl_Position =  u_projTrans * vec4(a_position, 1.);
}