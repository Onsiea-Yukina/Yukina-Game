#version 410 core

in vec2 TexCoord;
in float TextureIndex;
in vec3 DebugColor; // Debug color input

uniform sampler2DArray textures;

out vec4 FragColor;

void main() {
    // Use debug color for visualization
    FragColor = vec4(DebugColor, 1.0);
}