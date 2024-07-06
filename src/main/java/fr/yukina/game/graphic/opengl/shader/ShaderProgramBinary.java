package fr.yukina.game.graphic.opengl.shader;

import java.nio.ByteBuffer;

public record ShaderProgramBinary(ByteBuffer binary, int format)
{
}