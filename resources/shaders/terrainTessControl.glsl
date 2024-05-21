#version 410 core

layout(vertices = 3) out;

in vec3 vPosition[];
in vec2 TexCoord[];
in float TextureIndex[];

out vec3 tcPosition[];
out vec2 tcTexCoord[];
out float tcTextureIndex[];

uniform vec3 cameraPos; // Camera position uniform

void main() {
    // Calculate the centroid of the triangle
    vec3 centroida = (vPosition[0] + vPosition[1] + vPosition[2]) / 3.0;

    // Calculate the distance from the camera to the centroid
    float distance = length(cameraPos - centroida);

    // Adjust tessellation levels based on distance
    float tessLevel = mix(10.0, 2.0, distance / 100.0); // Example values, adjust as needed

    if (gl_InvocationID == 0) {
        gl_TessLevelInner[0] = tessLevel;
        gl_TessLevelOuter[0] = tessLevel;
        gl_TessLevelOuter[1] = tessLevel;
        gl_TessLevelOuter[2] = tessLevel;
    }

    tcPosition[gl_InvocationID] = vPosition[gl_InvocationID];
    tcTexCoord[gl_InvocationID] = TexCoord[gl_InvocationID];
    tcTextureIndex[gl_InvocationID] = TextureIndex[gl_InvocationID];

    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
}