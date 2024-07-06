#version 450 core

struct Material
{
	vec4 color;
	vec4 diffuseColor;
	vec4 specularColor;
	float reflectance;
};

struct Attenuation
{
	float constant;
	float linear;
	float exponent;
};

struct PointLight
{
	vec3 color;
	vec3 position; // Light position is assumed to be in view coordinates
	float intensity;
	Attenuation attenuation;
};

struct DirectionalLight {
	vec3 direction;
	vec3 color;
};

in vec3 passPosition;
in vec3 passNormal;
in vec3 passColor;

uniform vec3 ambientLight;
uniform float specularPower;
uniform Material material;
uniform PointLight pointLight;

out vec4 fragColor;

vec4 ambientC;
vec4 diffuseC;
vec4 speculrC;

void setupColors(Material material)
{
	ambientC = material.color;
	diffuseC = material.diffuseColor;
	speculrC = material.specularColor;
}

vec4 calcPointLight(PointLight light, vec3 position, vec3 normal)
{
	vec4 diffuseColor = vec4(0, 0, 0, 0);
	vec4 specColor = vec4(0, 0, 0, 0);

	// Diffuse Light
	vec3 light_direction = normalize(light.position - position);
	float diffuseFactor = max(dot(normal, light_direction), 0.0);
	diffuseColor = diffuseC * vec4(light.color, 1.0) * light.intensity * diffuseFactor;

	// Specular Light
	vec3 view_direction = normalize(-position);
	vec3 reflect_direction = reflect(-light_direction, normal);
	float specularFactor = pow(max(dot(view_direction, reflect_direction), 0.0), specularPower);
	specColor = speculrC * specularFactor * material.reflectance * vec4(light.color, 1.0);

	// Attenuation
	float distance = length(light.position - position);
	float attenuationInv = light.attenuation.constant + light.attenuation.linear * distance +
	light.attenuation.exponent * distance * distance;
	return (diffuseColor + specColor) / attenuationInv;
}

void main()
{
	setupColors(material);

	vec4 diffuseSpecularComp = calcPointLight(pointLight, passPosition, passNormal);

	fragColor = vec4(0.1, 0.1, 0.1, 1.0) + (ambientC * vec4(ambientLight, 1.0) + diffuseSpecularComp);
}
