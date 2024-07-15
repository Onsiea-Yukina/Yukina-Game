#version 450 core

struct DirectionalLight
{
	vec4 color;
	float power;
	vec3 direction;
};

struct Fog
{
	vec4 color;
	float constant;
	float linear;
	float exponent;
	int enabled;
};

struct Grid
{
	vec2 size;
	vec2 offset;
	float lineWidth;
	vec4 color;
	int enabled;
	float gradientWidth;
	float noisePower;
	float weight;
	float minColorThreshold;
};

in vec4 passColor;
in vec2 passGrid;
in vec3 passNormal;

uniform DirectionalLight directionalLight;
uniform Fog fog;
uniform vec3 viewPos; // Position of the viewer (camera)
uniform vec4 ambientLight; // Ambient light color
uniform Grid grid;
uniform float alpha;

out vec4 fragColor;

float random(vec2 co)
{
	return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

void main()
{
	vec4 baseColor = passColor;
	vec4 gridColor = vec4(0.0, 0.0, 0.0, 0.0);

	if (grid.enabled == 1)
	{
		// Calculate the distance from the fragment to the nearest grid line
		float distanceToGridLineX = abs(mod(passGrid.x, grid.size.x) - grid.offset.x);
		float distanceToGridLineZ = abs(mod(passGrid.y, grid.size.y) - grid.offset.y);

		// Calculate the width of the grid line
		float gridLineWidth = grid.lineWidth;

		// Apply gradient effect to the grid lines
		float gradientX = clamp(1.0 - distanceToGridLineX / (gridLineWidth * grid.gradientWidth), 0.0, 1.0);
		float gradientZ = clamp(1.0 - distanceToGridLineZ / (gridLineWidth * grid.gradientWidth), 0.0, 1.0);
		float gradient = max(gradientX, gradientZ);

		// Add slight noise effect to the gradient
		float noise = (random(passGrid.xy) - 0.5) * 2.0 * grid.noisePower; // Adjust noise intensity as needed
		float gridVisibility = clamp(gradient + noise, 0.0, 1.0);

		// Apply the grid color only if it is above the minimum color threshold
		if (gridVisibility > grid.minColorThreshold)
		{
			gridColor = mix(vec4(0.1, 0.1, 0.1, 1.0), grid.color, gridVisibility);
			// Blend the grid color with the base color only where the grid is visible
			baseColor = mix(baseColor, gridColor, gridVisibility * grid.weight);
		}
	}

	// Add ambient light to the base color
	vec4 ambient = ambientLight * baseColor;

	// Calculate the directional light
	vec3 norm = normalize(passNormal);
	vec3 lightDir = normalize(directionalLight.direction);
	float diff = max(dot(norm, lightDir), 0.0);
	vec4 diffuse = directionalLight.color * diff * directionalLight.power;

	vec4 litColor = ambient + baseColor * diffuse;

	// Calculate the fog factor
	if (fog.enabled == 1)
	{
		float distance = length(viewPos - gl_FragCoord.xyz);
		float fogFactor = fog.constant + fog.linear * distance + fog.exponent * distance * distance;
		fogFactor = clamp(fogFactor, 0.0, 1.0);

		// Apply fog to the final color
		vec4 foggedColor = vec4(mix(litColor, fog.color, fogFactor).xyz, alpha);

		// Output the final fragment color
		fragColor = foggedColor;
	}
	else
	{
		fragColor = vec4(litColor.xyz, alpha);
	}
}