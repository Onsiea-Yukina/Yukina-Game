package fr.yukina.game.graphic.opengl.nanovg.ui;

import fr.yukina.game.graphic.opengl.nanovg.Color;
import fr.yukina.game.graphic.opengl.nanovg.NanoVGContext;
import fr.yukina.game.graphic.window.handler.InputHandler;
import lombok.Setter;
import org.lwjgl.nanovg.NanoVG;

public class CircularButton extends AbstractButton
{
	private @Setter float radius;

	public CircularButton(final NanoVGContext contextIn, final InputHandler handlerIn, final float xIn,
	                      final float yIn,
	                      final float radiusIn, final int textureIn, final IButtonFunction pressCallbackIn)
	{
		super(contextIn, handlerIn, xIn, yIn, radiusIn * 2.0f, radiusIn * 2.0f, textureIn, pressCallbackIn);

		this.radius = radiusIn;
	}

	@Override
	public boolean isHovered(final double xIn, final double yIn)
	{
		var distX = this.x - xIn;
		var distY = this.y - yIn;
		// TODO optimize, avoid using sqrt
		return Math.sqrt(distX * distX + distY * distY) <= Math.sqrt(this.radius * this.radius);
	}

	@Override
	public void draw(final Color colorIn)
	{
		var imgPaint = NanoVG.nvgImagePattern(this.context.handle(), this.x - this.radius, this.y - this.radius,
		                                      this.width, this.height, 0.0f, this.texture, 1.0f, this.context.paint());
		NanoVG.nvgFillPaint(this.context.handle(), imgPaint);
		NanoVG.nvgCircle(this.context.handle(), this.x, this.y, this.radius);
		NanoVG.nvgFill(this.context.handle());
	}
}