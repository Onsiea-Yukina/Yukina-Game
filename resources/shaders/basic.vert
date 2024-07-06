#version 450 core

layout (location = 0) in vec2 position;

out vec2 passUVS;

void main()
{
	gl_Position = vec4(position, 0.0, 1.0);
	passUVS = (position + 1.0) / 2.0;
}