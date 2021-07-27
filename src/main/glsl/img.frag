#version 430

#define IMG_UV_ID 1

layout (location = IMG_UV_ID) in vec2 texture_uvFrag;

uniform sampler2D img_sampler;
uniform vec4 img_color;

void main() {
	vec4 pixel = texture(img_sampler, texture_uvFrag);
	gl_FragColor = pixel * img_color;
}
