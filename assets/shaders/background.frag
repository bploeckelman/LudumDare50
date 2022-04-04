#ifdef GL_ES
precision highp float;
#endif

uniform sampler2D u_texture;
uniform float u_time; // from 0-12-0

//input from vertex shader
varying vec2 v_texCoord;
varying vec4 v_color;

//const vec3 dayTop = vec3(.95, .95, 1.);
const vec3 dayTop = vec3(.2, .2, .66);
const vec3 dayMid = vec3(.5, .5, 1.);
//const vec3 dayBot = vec3(.2, .2, .6);
const vec3 dayBot = vec3(.96, .96, 1.);

const vec3 duskTop = vec3(.6, .1, .1);
const vec3 duskMid = vec3(.8, .4, .2);
const vec3 duskBot = vec3(.8, .8, .4);



void main() {
    vec4 texColor = texture2D(u_texture, v_texCoord);

    float dayCalc = smoothstep(6.5,8.5, u_time);
    float nightCalc = smoothstep(5.0, 6.5, u_time);

    vec3 color = mix(mix(duskMid, dayMid, dayCalc), mix(duskTop, dayTop, dayCalc), smoothstep(.3, .8, v_texCoord.y));
    color = mix(mix(duskBot, dayBot, dayCalc), color, smoothstep(.1, .3, v_texCoord.y));

    gl_FragColor = vec4(mix(texColor.rgb, color, nightCalc), 1.);

}