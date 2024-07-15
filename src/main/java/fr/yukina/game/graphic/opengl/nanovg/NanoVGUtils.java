package fr.yukina.game.graphic.opengl.nanovg;

import lombok.Getter;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import static org.lwjgl.nanovg.NanoVG.*;

public class NanoVGUtils
{
	private final         NanoVGContext context;
	private final @Getter NVGColor      color;
	private final @Getter NVGPaint      paint;
	private final @Getter float[]       bounds;

	NanoVGUtils(NanoVGContext contextIn)
	{
		this.context = contextIn;
		this.color   = NVGColor.create();
		this.paint   = NVGPaint.create();
		this.bounds  = new float[4];
	}

	public final void begin(float widthIn, float heightIn, float pixelRatioIn)
	{
		nvgBeginFrame(this.context.handle(), widthIn, heightIn, pixelRatioIn);
	}

	public final NVGColor createColor()
	{
		return NVGColor.create();
	}

	public final void font(String nameIn, String pathIn)
	{
		int font = nvgCreateFont(this.context.handle(), nameIn, pathIn);
		if (font == -1)
		{
			throw new RuntimeException("Could not add font");
		}
	}

	public final float[] text(String textIn, String fontNameIn, float fontSizeIn, float xIn, float yIn, int rIn,
	                          int gIn, int bIn, int aIn)
	{
		var fontFaceId = nvgFindFont(this.context.handle(), fontNameIn);
		if (fontFaceId < 0)
		{
			throw new RuntimeException("Could not find font");
		}
		nvgFontFaceId(this.context.handle(), fontFaceId);
		nvgFontSize(this.context.handle(), fontSizeIn);
		nvgFillColor(this.context.handle(), rgba(rIn, gIn, bIn, aIn));

		/*ByteBuffer byteText = memUTF8(textIn);
		long       start    = memAddress(byteText);
		long       end      = start + byteText.remaining();
		nnvgText(this.context.handle(), xIn, yIn, start, end);
		memFree(byteText);*/

		nvgText(this.context.handle(), xIn, yIn, textIn);

		nvgTextBounds(this.context.handle(), xIn, yIn, textIn, bounds);

		return this.bounds;
	}

	public final NVGColor rgba(int rIn, int gIn, int bIn, int aIn)
	{
		color.r(rIn / 255f);
		color.g(gIn / 255f);
		color.b(bIn / 255f);
		color.a(aIn / 255f);

		return color;
	}

	public final void end()
	{
		nvgEndFrame(this.context.handle());
	}
}
