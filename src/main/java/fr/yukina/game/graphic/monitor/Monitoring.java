package fr.yukina.game.graphic.monitor;

import fr.yukina.game.graphic.opengl.nanovg.NanoVGContext;
import fr.yukina.game.graphic.window.GLFWContext;
import fr.yukina.game.graphic.window.GLFWWindow;
import fr.yukina.game.logic.player.Player;
import fr.yukina.game.utils.Statistic;
import fr.yukina.game.utils.StatisticFormatter;
import lombok.Getter;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.opengl.GL32;

public class Monitoring
{
	private               boolean    antialiasing;
	private final @Getter GLFWWindow window;

	private final Statistic          framerateStatistic;
	private final StatisticFormatter framerateStatisticFormatter;
	private final Player             player;

	private @Getter NanoVGContext nanoVGContext;
	private         int           frame = 0;
	private         long          lastFrameTime;

	public Monitoring(GLFWContext glfwContextIn, boolean antialiasingIn, long monitorIn, Player playerIn)
	{
		this.framerateStatistic          = new Statistic(100);
		this.framerateStatisticFormatter = new StatisticFormatter(framerateStatistic);
		this.player                      = playerIn;

		this.antialiasing = antialiasingIn;
		this.window       = glfwContextIn.create(1920, 1080, "Yukina-Monitoring", 20, 1, this.antialiasing, monitorIn);
		this.window.initialize();

		framerateStatisticFormatter.addPattern("FPS[{iteration}]: {low} < {current}({average}) < {high} ");
		//framerateStatisticFormatter.addPattern(
		//		"[0:{count}]:{range} (.){median} ➚{variation} σ2({variance}) σ({deviation}) Q({iqr}) q1({q1}) q3"
		//		+ "({q3}) ");
		framerateStatisticFormatter.addRule(0, 0, 2, 25000, 3, 3, "iteration", "low", "current", "average", "high",
		                                    "count", "range", "median", "variation", "iqr", "q1", "q3");
		framerateStatisticFormatter.addRule(0, 0, 4, 25000, 4, 4, "variance", "deviation");
	}

	public final void initialize()
	{
		this.window().makeCurrent();
		this.nanoVGContext = new NanoVGContext(antialiasing);

		this.nanoVGContext.font("sans-serif", "resources/fonts/Microsoft Sans Serif.ttf");
		this.nanoVGContext.font("noto-symbols", "resources/fonts/NotoSansSymbols2-Regular.ttf");
		this.nanoVGContext.font("noto", "resources/fonts/NotoSans-VariableFont_wdth,wght.ttf");
		this.nanoVGContext.font("arial", "resources/fonts/arial/arial.ttf");
		NanoVG.nvgAddFallbackFont(this.nanoVGContext.handle(), "sans-serif", "noto-symbols");
		NanoVG.nvgAddFallbackFont(this.nanoVGContext.handle(), "noto-symbols", "noto");
		NanoVG.nvgAddFallbackFont(this.nanoVGContext.handle(), "noto", "arial");

		this.window.show();
		lastFrameTime = System.nanoTime();
	}

	public final void update()
	{
		if (System.nanoTime() - lastFrameTime >= 1_000_000_000)
		{
			framerateStatistic.set(frame);
			frame         = 0;
			lastFrameTime = System.nanoTime();
			System.out.println(framerateStatistic.histogram().size());
		}
		frame++;
	}

	public final void display()
	{
		this.window.makeCurrent();
		GL32.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT | GL32.GL_STENCIL_BUFFER_BIT);
		this.nanoVGContext.begin(this.window.width(), this.window.height(), 1.0f);
		var r = (int) ((60.0f - framerateStatistic.current()) / 60.0f * 255.0f);
		if (framerateStatistic.current() < 30)
		{
			r = (int) ((60.0f - framerateStatistic.current() + 30) / 60.0f * 255.0f);
		}

		var g = (int) (framerateStatistic.current() / 60.0f * 255.0f);
		if (framerateStatistic.current() < 30)
		{
			g = (int) ((framerateStatistic.current() - 30) / 60.0f * 255.0f);
		}

		framerateStatisticFormatter.process();
		this.nanoVGContext.bounds()[3] = 0.0f;
		while (!framerateStatisticFormatter.texts().isEmpty())
		{
			this.nanoVGContext.text(framerateStatisticFormatter.texts().firstElement(), "sans-serif", 24, 20,
			                        this.nanoVGContext.bounds()[3] + +26, r, g, 0, 255);
			framerateStatisticFormatter.texts().remove(0);
		}

		var lastX = 0.0f;
		var lastY = 0.0f;
		for (var i = 0; i < framerateStatistic.histogram().size(); i++)
		{
			var x       = i * 12;
			var value   = framerateStatistic.histogram().get(i);
			var percent = -((int) (value / framerateStatistic.high() * 100));
			var y       = this.nanoVGContext.bounds()[3] + 100;

			r = (int) ((60.0f - value) / 60.0f * 255.0f);
			if (value < 30)
			{
				r = (int) ((60.0f - value + 30) / 60.0f * 255.0f);
			}

			g = (int) (value / 60.0f * 255.0f);
			if (value < 30)
			{
				g = (int) ((value - 30) / 60.0f * 255.0f);
			}

			NanoVG.nvgBeginPath(this.nanoVGContext.handle());
			NanoVG.nvgFillColor(this.nanoVGContext.handle(), this.nanoVGContext.rgba(r, g, 0, 50));
			NanoVG.nvgRect(this.nanoVGContext.handle(), x, y, 6, percent);
			NanoVG.nvgFill(this.nanoVGContext.handle());

			NanoVG.nvgBeginPath(this.nanoVGContext.handle());
			NanoVG.nvgStrokeColor(this.nanoVGContext.handle(), this.nanoVGContext.rgba(r, g, 0, 255));
			NanoVG.nvgMoveTo(this.nanoVGContext.handle(), lastX, lastY);
			NanoVG.nvgBezierTo(this.nanoVGContext.handle(), lastX, lastY, lastX + (x - lastX) / 2,
			                   lastY + ((y + percent) - lastY) / 2, x, (y + percent));
			NanoVG.nvgStroke(this.nanoVGContext.handle());

			lastX = x;
			lastY = y + percent;
		}

		this.nanoVGContext.text(
				"Position: " + player.camera().position().x() + ", " + player.camera().position().y() + ", "
				+ player.camera().position().z(), "sans-serif", 24, 20, this.nanoVGContext.bounds()[3] + 100 + 26, 255,
				255, 255, 255);

		this.nanoVGContext.text(
				"Orientation: " + this.player.camera().orientation().x() + ", " + this.player.camera().orientation().y
				+ ", " + this.player.camera().orientation().z(), "sans-serif", 24, 20,
				this.nanoVGContext.bounds()[3] + 26, 255, 255, 255, 255);
		this.nanoVGContext.text(
				"Velocity:" + this.player.camera().velocity().x + ", " + this.player.camera().velocity().y + ", "
				+ this.player.camera().velocity().z, "sans-serif", 24, 20, this.nanoVGContext.bounds()[3] + 26, 255,
				255, 255, 255);
		this.nanoVGContext.text(
				"Changes: " + this.player.camera().updateState().hasChanged() + "(" + this.player.camera().updateState()
				                                                                                 .hasMoved() + "/"
				+ this.player.camera().updateState().hasRotated() + ")" + " " + "chunk(" + this.player.camera()
				                                                                                      .updateState()
				                                                                                      .hasMoveOneChunk()
				+ ") " + "block(" + this.player.camera().updateState().hasMoveOneBlock() + ")", "sans-serif", 24, 20,
				this.nanoVGContext.bounds()[3] + 26, 255, 255, 255, 255);
		this.nanoVGContext.text("Distances: " + Math.sqrt(this.player.camera().updateState().movementSquare()) + "("
		                        + this.player.camera().updateState().movementX() + ", " + this.player.camera()
		                                                                                             .updateState()
		                                                                                             .movementY() +
		                        ", "
		                        + this.player.camera().updateState().movementZ() + ") / " + Math.sqrt(
				                        this.player.camera().updateState().rotationSquare()) + "(" + this.player.camera().updateState()
		                                                                                                        .rotationX() + ", "
		                        + player.camera().updateState().rotationY() + ", " + player.camera().updateState()
		                                                                                   .rotationZ() + ")",
		                        "sans-serif", 24, 20, this.nanoVGContext.bounds()[3] + 26, 255, 255, 255, 255);
		this.nanoVGContext.text(
				"Last: " + this.player.camera().updateState().lastPosition().x() + ", " + this.player.camera()
				                                                                                     .updateState()
				                                                                                     .lastPosition().y()
				+ ", " + this.player.camera().updateState().lastPosition().z() + " chunk(" + this.player.camera()
				                                                                                        .updateState()
				                                                                                        .lastChunk().x()
				+ ", " + this.player.camera().updateState().lastChunk().y() + ", " + this.player.camera().updateState()
				                                                                                .lastChunk().z()
				+ ") block(" + this.player.camera().updateState().lastBlock().x() + ", " + this.player.camera()
				                                                                                      .updateState()
				                                                                                      .lastBlock().y()
				+ ", " + this.player.camera().updateState().lastBlock().z()

				+ ")", "sans-serif", 24, 20, this.nanoVGContext.bounds()[3] + 26, 255, 255, 255, 255);
		this.nanoVGContext.end();
		this.window.swapBuffers();
	}

	public final boolean shouldClose()
	{
		return this.window.shouldClose();
	}

	public final void destroy()
	{
		this.nanoVGContext.destroy();
	}
}