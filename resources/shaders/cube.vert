#version 450 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 uvs;
layout (location = 2) in vec3 normal;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 transformation;

out vec3 passPosition;
out vec3 passNormal;
out vec3 passColor;

void main()
{
	mat4 modelView = view * transformation;
	vec4 modelViewPosition = modelView * vec4(position, 1.0);
	passPosition = modelViewPosition.xyz;
	passNormal = normalize(modelView * vec4(normal, 0.0)).xyz;
	passColor = vec3(0.75, 0.25, 1.25);
	gl_Position = projection * modelView * vec4(position, 1.0);
}