#version 330 core

in vec3 passPosition;
in float tier; // Input for tier information

out vec4 fragColor;

void main()
{
	// Normalize the tier value to the range [0, 1]
	float tierNormalized = mod(tier, 4.0) / 4.0;

	// Use tierNormalized to influence the color
	fragColor = vec4(passPosition.x / (32 * 16), passPosition.y / 16, passPosition.z / (32 * 16), 1.0);
}