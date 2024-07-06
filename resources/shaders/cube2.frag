#version 330 core

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
	float intensity;
	Attenuation attenuation;
};

in vec3 passPosition;
in vec3 passNormal;
in vec3 passToCameraVector;
in vec3 passLightPosition;

uniform vec3 ambientLight;
uniform float specularPower;
uniform Material material;
uniform PointLight pointLight;

out vec4 fragColor;

vec3 calcPointLight(PointLight light, vec3 fragPos, vec3 normal)
{
	vec3 lightDir = normalize(passLightPosition - fragPos);

	// Diffuse shading
	float diff = max(dot(normal, lightDir), 0.0);
	vec3 diffuse = light.color * diff * vec3(material.diffuseColor);

	// Specular shading
	vec3 unitCameraVector = normalize(passToCameraVector);
	vec3 reflectDir = reflect(-lightDir, normal);
	float spec = pow(max(dot(reflectDir, unitCameraVector), 0.0), specularPower) * material.reflectance;
	vec3 specular = max(light.color * spec * vec3(material.specularColor), 0.0);

	// Attenuation
	float distance = length(passLightPosition - fragPos);
	float attenuation = light.attenuation.constant +
	light.attenuation.linear * distance +
	light.attenuation.exponent * (distance * distance);

	diffuse /= attenuation;
	specular /= attenuation;

	return (diffuse + specular) * light.intensity;
}

void main()
{
	vec3 lightContribution = calcPointLight(pointLight, normalize(passPosition), normalize(passNormal));

	// Combine with ambient lighting
	vec3 ambient = ambientLight * vec3(material.color);
	vec3 result = ambient + lightContribution;

	fragColor = vec4(result, material.color.a);
}
