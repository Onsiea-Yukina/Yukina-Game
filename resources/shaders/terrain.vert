#version 450 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;

uniform mat4 model;
uniform mat4 projection;
uniform mat4 view;

out vec4 passColor;
out vec2 passGrid;
out vec3 passNormal;
out vec3 passToCamera;

void main()
{
	// Transform the normal to view space
	passNormal = normalize(transpose(inverse(mat3(model))) * normal).xyz;
	passToCamera = (inverse(view) * vec4(0.0, 0.0, 0.0, 1.0)).xyz;

	// Transform the position to clip space
	gl_Position = projection * view * model * vec4(position, 1.0);

	passColor = vec4(1.0, 1.0, 1.0, 1.0); //vec4(position / 256.0, 1.0);
	passGrid = position.xz;
}
