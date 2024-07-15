#version 450 core

layout (location = 0) in vec3 passPosition;
layout (location = 1) in float passTier;

layout (location = 0) out vec4 fragColor;

struct Fog {
	int activated;
	vec3 colour;
	float density;
};

vec4 calcFog(vec3 pos, vec4 colour, Fog fog) {
	float distance = length(pos);
	float fogFactor = 1.0 / exp((distance * fog.density) * (distance * fog.density));
	fogFactor = clamp(fogFactor, 0.0, 1.0);

	vec3 resultColour = mix(fog.colour, colour.xyz, fogFactor);
	return vec4(resultColour.xyz, colour.w);
}

// Place the Fog uniform inside a uniform block
layout (set = 0, binding = 0) uniform FogBlock {
	Fog fog;
};

void main() {
	// Normalize the passTier value to the range [0, 1]
	float tierNormalized = mod(passTier, 4.0) / 4.0;

	// Use tierNormalized to influence the color
	fragColor = vec4(passPosition.x / (32 * 16), passPosition.y / 16, passPosition.z / (32 * 16), 1.0);
	if (fog.activated == 1) {
		fragColor = calcFog(passPosition, fragColor, fog);
	}
}