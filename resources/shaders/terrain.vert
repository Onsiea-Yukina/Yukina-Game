#version 450 core

layout (location = 0) in vec3 position;

layout (location = 0) out vec3 passPosition;
layout (location = 1) out float passTier;

layout (set = 0, binding = 0) uniform UniformBlock {
	mat4 projectionView;
};

void main()
{
	gl_Position = projectionView * vec4(position, 1.0);
	passPosition = position;

	// Calculate tier based on some criteria, for example using the y position
	passTier = floor(position.y / 10.0); // Each tier is 10 units in height
}