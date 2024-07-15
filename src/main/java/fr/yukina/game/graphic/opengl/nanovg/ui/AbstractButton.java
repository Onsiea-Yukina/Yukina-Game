package fr.yukina.game.graphic.opengl.nanovg.ui;

import fr.yukina.game.graphic.opengl.nanovg.Color;
import fr.yukina.game.graphic.opengl.nanovg.NanoVGContext;
import fr.yukina.game.graphic.window.handler.ISubscriptable;
import fr.yukina.game.graphic.window.handler.InputHandler;
import fr.yukina.game.graphic.window.handler.Subscriber;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NanoVG;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class AbstractButton
		implements InputHandler.IMouseButtonFunction, InputHandler.IMousePositionFunction, InputHandler.IButtonFunction
{
	protected final   NanoVGContext context;
	protected @Setter float         x;
	protected @Setter float         y;
	protected @Setter float         width;
	protected @Setter float         height;

	protected @Getter @Setter int texture;

	private final Color color;
	private final Color selectedColor;
	private final Color disabledColor;
	private final Color hoveredColor;

	private boolean selected;
	private boolean disabled;
	private boolean hovered;

	private final List<Integer> shortcuts;

	private final ISubscriptable<InputHandler.IMousePositionFunction> mousePositionSubscriptable;
	private final ISubscriptable<InputHandler.IMouseButtonFunction>   mouseButtonSubscriptable;
	private final ISubscriptable<InputHandler.IButtonFunction>        buttonSubscriptable;

	private final IButtonFunction pressCallback;

	private final Subscriber<IButtonFunction> subscriber;

	public AbstractButton(NanoVGContext contextIn, InputHandler handlerIn, float xIn, float yIn, float widthIn,
	                      float heightIn, int textureIn, IButtonFunction pressCallbackIn)
	{
		this.context = contextIn;
		this.x       = xIn;
		this.y       = yIn;
		this.width   = widthIn;
		this.height  = heightIn;
		this.texture = textureIn;

		this.color         = new Color(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);
		this.selectedColor = new Color(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		this.disabledColor = new Color(0.125f, 0.125f, 0.125f, 0.95f, 0.0f, 0.0f, 0.0f, 0.0f);
		this.hoveredColor  = new Color(0.0f, 0.0f, 0.0f, 1.0f, 0.75f, 0.75f, 0.75f, 1.0f);

		this.selected = false;
		this.disabled = false;
		this.hovered  = false;

		this.shortcuts = new ArrayList<>();

		this.mousePositionSubscriptable = handlerIn.addMousePositionSubscriber(this);
		this.mouseButtonSubscriptable   = handlerIn.addMouseButtonSubscriber(this);
		this.buttonSubscriptable        = handlerIn.addButtonSubscriber(this);

		this.pressCallback = pressCallbackIn;

		this.subscriber = new Subscriber<>();
	}

	public final void draw()
	{
		var color = this.color;

		if (this.disabled)
		{
			color = this.disabledColor;
		}
		else if (this.hovered)
		{
			color = this.hoveredColor;
		}
		else if (this.selected)
		{
			color = this.selectedColor;
		}

		NanoVG.nvgBeginPath(this.context.handle());
		this.context.color().r(color.fill().x);
		this.context.color().g(color.fill().y);
		this.context.color().b(color.fill().z);
		this.context.color().a(color.fill().w);
		NanoVG.nvgFillColor(this.context.handle(), this.context.color());

		this.draw(color);

		if (this.hovered)
		{
			color = this.hoveredColor;
			this.context.color().r(color.stroke().x);
			this.context.color().g(color.stroke().y);
			this.context.color().b(color.stroke().z);
			this.context.color().a(color.stroke().w);
			NanoVG.nvgStrokeWidth(this.context.handle(), this.selected ? 16.0f : 10.0f);
			NanoVG.nvgStrokeColor(this.context.handle(), this.context.color());
			NanoVG.nvgStroke(this.context.handle());
		}
		if (this.selected)
		{
			color = this.selectedColor;
			this.context.color().r(color.stroke().x);
			this.context.color().g(color.stroke().y);
			this.context.color().b(color.stroke().z);
			this.context.color().a(color.stroke().w);
			NanoVG.nvgStrokeWidth(this.context.handle(), this.hovered ? 6.0f : 10.0f);
			NanoVG.nvgStrokeColor(this.context.handle(), this.context.color());
			NanoVG.nvgStroke(this.context.handle());
		}

		NanoVG.nvgClosePath(this.context.handle());
	}

	public abstract void draw(Color colorIn);

	public final void enable()
	{
		this.disabled = false;

		this.mousePositionSubscriptable.resume();
		this.mouseButtonSubscriptable.resume();
		this.buttonSubscriptable.resume();
	}

	public final void disable()
	{
		this.disabled = true;

		this.mousePositionSubscriptable.pause();
		this.mouseButtonSubscriptable.pause();
		this.buttonSubscriptable.pause();
	}

	public final AbstractButton select()
	{
		this.subscriber.forEach(f -> f.execute(this));
		this.selected = true;
		this.pressCallback.execute(this);

		return this;
	}

	public final AbstractButton unselect()
	{
		this.selected = false;

		return this;
	}

	// TODO dissociate hovered and shortcuted
	@Override
	public final boolean onButton(long handleIn, int keyIn, int scancodeIn, int actionIn, int modsIn)
	{
		if (this.disabled || (actionIn != GLFW.GLFW_PRESS && actionIn != GLFW.GLFW_REPEAT))
		{
			var xPos = new double[1];
			var yPos = new double[1];
			GLFW.glfwGetCursorPos(handleIn, xPos, yPos);
			this.hovered = this.isHovered(xPos[0], yPos[0]);
			return false;
		}

		for (var shortcut : this.shortcuts)
		{
			var action = GLFW.glfwGetKey(handleIn, shortcut);
			if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
			{
				this.hovered = true;
				this.select();
				return true;
			}
		}

		var xPos = new double[1];
		var yPos = new double[1];
		GLFW.glfwGetCursorPos(handleIn, xPos, yPos);
		this.hovered = this.isHovered(xPos[0], yPos[0]);

		return false;
	}

	@Override
	public final void onMousePosition(long handleIn, double xIn, double yIn)
	{
		this.hovered = this.isHovered(xIn, yIn);
	}

	public abstract boolean isHovered(double xIn, double yIn);

	@Override
	public final boolean onMouseButton(long handleIn, int buttonIn, int actionIn, int modsIn)
	{
		if (this.disabled || !this.hovered || buttonIn != GLFW.GLFW_MOUSE_BUTTON_1 || actionIn != GLFW.GLFW_PRESS)
		{
			return false;
		}

		this.select();

		return true;
	}

	public final AbstractButton fill(float rIn, float gIn, float bIn)
	{
		this.color.fill(rIn, gIn, bIn);
		return this;
	}

	public final AbstractButton stroke(float rIn, float gIn, float bIn)
	{
		this.color.stroke(rIn, gIn, bIn);
		return this;
	}

	public final AbstractButton selectedFill(float rIn, float gIn, float bIn)
	{
		this.selectedColor.fill(rIn, gIn, bIn);
		return this;
	}

	public final AbstractButton selectedStroke(float rIn, float gIn, float bIn)
	{
		this.selectedColor.stroke(rIn, gIn, bIn);
		return this;
	}

	public final AbstractButton disabledFill(float rIn, float gIn, float bIn)
	{
		this.disabledColor.fill(rIn, gIn, bIn);
		return this;
	}

	public final AbstractButton disabledStroke(float rIn, float gIn, float bIn)
	{
		this.disabledColor.stroke(rIn, gIn, bIn);
		return this;
	}

	public final AbstractButton hoveredFill(float rIn, float gIn, float bIn)
	{
		this.hoveredColor.fill(rIn, gIn, bIn);
		return this;
	}

	public final AbstractButton hoveredStroke(float rIn, float gIn, float bIn)
	{
		this.hoveredColor.stroke(rIn, gIn, bIn);
		return this;
	}

	public final AbstractButton shortcut(int shortcutIn)
	{
		this.shortcuts.add(shortcutIn);

		return this;
	}

	public final ISubscriptable<IButtonFunction> subscribe(IButtonFunction functionIn)
	{
		return this.subscriber.subscribe(functionIn);
	}

	public final void destroy()
	{
		this.mousePositionSubscriptable.unsubscribe();
		this.mouseButtonSubscriptable.unsubscribe();
		this.buttonSubscriptable.unsubscribe();
	}

	public static interface IButtonFunction
	{
		void execute(AbstractButton buttonIn);
	}
}