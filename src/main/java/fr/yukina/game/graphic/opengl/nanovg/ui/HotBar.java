package fr.yukina.game.graphic.opengl.nanovg.ui;

import fr.yukina.game.graphic.opengl.nanovg.NanoVGContext;
import fr.yukina.game.graphic.window.handler.InputHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

public class HotBar implements InputHandler.IScrollFunction
{
	private final           NanoVGContext               context;
	private @Getter @Setter float                       x;
	private @Getter @Setter float                       y;
	private @Getter @Setter float                       buttonWidth;
	private @Getter @Setter float                       buttonHeight;
	private @Getter final   Map<String, AbstractButton> buttons;
	private @Getter         String                      selected;
	private @Getter         int                         selectedIndex;
	private @Getter         boolean                     enabled;

	public HotBar(NanoVGContext contextIn, InputHandler inputHandlerIn, float xIn, float yIn, float buttonWidthIn,
	              float buttonHeightIn)
	{
		this.context      = contextIn;
		this.x            = xIn;
		this.y            = yIn;
		this.buttonWidth  = buttonWidthIn;
		this.buttonHeight = buttonHeightIn;
		this.buttons      = new LinkedHashMap<>();
		this.enabled      = true;

		inputHandlerIn.addScrollSubscriber(this);
	}

	public final void enable()
	{
		this.enabled = true;
	}

	public final void disable()
	{
		this.enabled = false;
	}

	public final void draw(DrawFunction drawCallbackIn)
	{
		int i = 0;
		for (var buttonEntry : this.buttons().entrySet())
		{
			var button = buttonEntry.getValue();
			var x      = (this.x - this.buttons().size() * this.buttonWidth) / 2 + i * (this.buttonWidth + 10.0f);
			var y      = this.y - (this.buttonHeight + 10.0f) / 2;
			button.x(x);
			button.y(y);
			button.draw();
			drawCallbackIn.draw(i, x, y, buttonEntry.getKey(), button);
			i++;
		}
	}

	public final boolean hovered()
	{
		for (var button : this.buttons.values())
		{
			if (button.hovered())
			{
				return true;
			}
		}
		return false;
	}

	public final void add(String nameIn, AbstractButton buttonIn)
	{
		buttonIn.subscribe((_buttonIn) ->
		                   {
			                   this.selected = nameIn;
			                   int i = 0;
			                   for (var buttonEntry : this.buttons.entrySet())
			                   {
				                   var name   = buttonEntry.getKey();
				                   var button = buttonEntry.getValue();
				                   if (name.contentEquals(nameIn))
				                   {
					                   this.selectedIndex = i;
				                   }
				                   button.unselect();
				                   i++;
			                   }
		                   });
		this.buttons.put(nameIn, buttonIn);
		if (this.selectedIndex == -1 || this.selected == null)
		{
			buttonIn.select();
		}
	}

	@Override
	public void onScroll(final long windowHandleIn, final double xOffsetIn, final double yOffsetIn)
	{
		if (!this.enabled)
		{
			return;
		}
		this.selectedIndex += yOffsetIn;
		if (this.selectedIndex > this.buttons.size() - 1)
		{
			this.selectedIndex = 0;
		}
		else if (this.selectedIndex < 0)
		{
			this.selectedIndex = this.buttons.size() - 1;
		}
		var entry = (Map.Entry<String, AbstractButton>) this.buttons.entrySet().toArray()[this.selectedIndex];
		this.selected = entry.getKey();
		entry.getValue().select();
	}

	public interface DrawFunction
	{
		void draw(int indexIn, float xIn, float yIn, String buttonNameIn, AbstractButton buttonIn);
	}
}