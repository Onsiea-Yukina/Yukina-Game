#version 450 core

in vec2 passUVS;

uniform sampler2D textureSampler;

out vec4 fragColor;

void main()
{
	fragColor = texture(textureSampler, passUVS);
}