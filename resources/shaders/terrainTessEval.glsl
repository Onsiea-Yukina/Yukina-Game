#version 410 core

layout(triangles, equal_spacing, cw) in;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

in vec3 tcPosition[];
in vec2 tcTexCoord[];
in float tcTextureIndex[];

out vec2 TexCoord;
out float TextureIndex;
out vec3 DebugColor; // Debug color output

void main() {
    vec3 p0 = tcPosition[0];
    vec3 p1 = tcPosition[1];
    vec3 p2 = tcPosition[2];

    vec3 pos = (1.0 - gl_TessCoord.x - gl_TessCoord.y) * p0 +
               gl_TessCoord.x * p1 +
               gl_TessCoord.y * p2;

    gl_Position = projectionMatrix * viewMatrix * vec4(pos, 1.0);
    TexCoord = (1.0 - gl_TessCoord.x - gl_TessCoord.y) * tcTexCoord[0] +
               gl_TessCoord.x * tcTexCoord[1] +
               gl_TessCoord.y * tcTexCoord[2];
    TextureIndex = tcTextureIndex[0]; // Assuming all vertices share the same texture index

    // Set debug color based on tessellation coordinates
    DebugColor = vec3(gl_TessCoord.x, gl_TessCoord.y, 1.0 - gl_TessCoord.x - gl_TessCoord.y);
}