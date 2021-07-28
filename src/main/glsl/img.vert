#version 430

#define IMG_VERT_ID 0
#define IMG_UV_ID   1

layout (location = IMG_VERT_ID) in vec2 vertex;
layout (location = IMG_UV_ID)   in vec2 texture_uvVert;
layout (location = IMG_UV_ID)  out vec2 texture_uvFrag;

uniform mat4 ae_Ortho;
uniform mat4 ae_View;

uniform vec2 img_scale;
uniform vec2 img_pos;

void main() {
	vec2 scaled = (vertex * img_scale) + img_pos;
	gl_Position = ae_Ortho * ae_View * vec4(scaled, 1.0, 1.0);
	texture_uvFrag = texture_uvVert;
}
