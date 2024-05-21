#version 410 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in float textureIndex;

uniform mat4 modelMatrix;

out vec3 vPosition;
out vec2 TexCoord;
out float TextureIndex;

void main() {
    vPosition = position;
    TexCoord = texCoord;
    TextureIndex = textureIndex;
    gl_Position = modelMatrix * vec4(position, 1.0);
}