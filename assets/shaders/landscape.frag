#ifdef GL_ES
precision highp float;
#endif

uniform sampler2D u_texture;


//input from vertex shader
varying vec2 v_texCoords;
varying vec4 v_color;
varying vec3 v_pos;



void main() {
    vec4 noise1 = texture2D(u_texture, vec2(v_texCoords.x * .4, v_texCoords.y * .4));
    vec4 noise2 = texture2D(u_texture, vec2(v_texCoords.x * 1.3, v_texCoords.y * 1.3));
    vec4 noise3 = texture2D(u_texture, vec2(v_texCoords.x * .7, v_texCoords.y * .7));

    float noise = ( noise1.b *.2 +
                    noise1.g * .1 +
                    noise3.r * .1 +
                    noise3.b * .2 +
                    noise2.b * .25 +
                    noise2.g * .05);
    vec3 grassColor = noise * mix( vec3(.3, .4, .05), vec3(.2, 1., .2), noise);
    vec3 snowColor = ((noise/2.)+.5) * vec3(1., 1., 1.);
    gl_FragColor = vec4(mix(grassColor, snowColor, v_color.r), 1.);
//    gl_FragColor = vec4(v_color.r, 1., 1., 1.);
}