package fr.yukina.game.graphic.opengl.nanovg;

import lombok.Getter;
import lombok.experimental.Delegate;

import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class NanoVGContext
{
	private final @Getter   long        handle;
	private final @Delegate NanoVGUtils utils;

	public NanoVGContext(boolean antialiasingIn)
	{
		this.handle = antialiasingIn ? nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES) : nvgCreate(NVG_STENCIL_STROKES);
		if (handle == NULL)
		{
			throw new RuntimeException("Could not init nanovg");
		}
		this.utils = new NanoVGUtils(this);

	}

	public void destroy()
	{
		nvgDelete(this.handle);
	}
}
