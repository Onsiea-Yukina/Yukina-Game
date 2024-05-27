#version 330 core

layout (location = 0) in vec3 position;

out vec3 passPosition;
out float tier; // New output for tier information

uniform mat4 projectionView;

void main()
{
	gl_Position = projectionView * vec4(position, 1.0);
	passPosition = position;

	// Calculate tier based on some criteria, for example using the y position
	tier = floor(position.y / 10.0); // Each tier is 5 units in height
}