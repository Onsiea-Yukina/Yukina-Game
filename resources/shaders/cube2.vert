#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 uvs;
layout (location = 2) in vec3 normal;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 transformation;
uniform vec3 pointLightPosition;

out vec3 passPosition;
out vec3 passNormal;
out vec3 passToCameraVector;
out vec3 passLightPosition;

void main()
{
	mat4 modelView = transformation;
	vec4 worldPosition = transformation * vec4(position, 1.0);
	passPosition = vec3(worldPosition);

	// Transform the normal
	passNormal = (transformation * vec4(normal, 0.0)).xyz;
	passToCameraVector = (inverse(view) * vec4(0.0, 0.0, 0.0, 1.0)).xyz;
	passLightPosition = pointLightPosition;

	gl_Position = projection * view * worldPosition;
}