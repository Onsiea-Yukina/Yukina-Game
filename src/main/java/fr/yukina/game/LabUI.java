package fr.yukina.game;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.util.*;

public class LabUI
{
	private final static int      WIDTH  = 1000;
	private final static int      HEIGHT = 1000;
	private static       long     window;
	private static       long     nanovg;
	private final static NVGColor COLOR  = NVGColor.create();

	public final static void main(String[] args)
	{
		if (!GLFW.glfwInit())
		{
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_TRUE);

		window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "Yukina-LabUI", MemoryUtil.NULL, MemoryUtil.NULL);
		if (window == 0)
		{
			throw new IllegalStateException("Unable to create GLFW window");
		}

		GLFW.glfwSetKeyCallback(window, (windowIn, key, scancode, action, mods) ->
		{
			if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
			{
				GLFW.glfwSetWindowShouldClose(window, true);
			}
		});

		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(1);
		GL.createCapabilities();
		nanovg = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS | NanoVGGL3.NVG_STENCIL_STROKES | NanoVGGL3.NVG_DEBUG);

		var group = new Group();
		var sliderA = group.add("sliderA",
		                        new Slider.Builder().x(50.0f).y(50.0f).width(900).height(80).cursorHeight(90).build());
		var sliderB = group.add("sliderB",
		                        new Slider.Builder().x(50.0f).y(50.0f + 150.0f).width(900).height(80).cursorHeight(90)
		                                            .build());
		var sliderC = group.add("sliderC",
		                        new Slider.Builder().x(50.0f).y(50.0f + 300.0f).width(900).height(80).cursorHeight(90)
		                                            .build());
		sliderA.next(sliderB);
		sliderB.next(sliderC);
		sliderC.next(sliderA);

		InputManager inputManager = new InputManager(window);
		GLFW.glfwShowWindow(window);

		while (!GLFW.glfwWindowShouldClose(window))
		{
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
			NanoVG.nvgBeginFrame(nanovg, WIDTH, HEIGHT, 1.0f);
			group.draw(nanovg);
			NanoVG.nvgEndFrame(nanovg);
			GLFW.glfwSwapBuffers(window);

			inputManager.reset();
			GLFW.glfwPollEvents();
			inputManager.update();
			group.update(inputManager);
		}
	}

	public final static void fillRectangle(long handleIn, float xIn, float yIn, float wIn, float hIn, Color colorIn)
	{
		fillRectangle(handleIn, xIn, yIn, wIn, hIn, colorIn.r, colorIn.g, colorIn.b, colorIn.a);
	}

	public final static void fillRectangle(long handleIn, float xIn, float yIn, float wIn, float hIn, float rIn,
	                                       float gIn, float bIn, float aIn)
	{
		NanoVG.nvgBeginPath(handleIn);
		NanoVG.nvgRect(handleIn, xIn, yIn, wIn, hIn);
		COLOR.r(rIn);
		COLOR.g(gIn);
		COLOR.b(bIn);
		COLOR.a(aIn);
		NanoVG.nvgFillColor(handleIn, COLOR);
		NanoVG.nvgFill(handleIn);
		NanoVG.nvgClosePath(handleIn);
	}

	@Getter
	public abstract static class Toggle
	{
		private final int       key;
		private       boolean   active;
		private       IFunction startFunction;
		private       IFunction endFunction;

		public Toggle(int keyIn, IFunction startFunctionIn, IFunction endFunctionIn)
		{
			this.key           = keyIn;
			this.startFunction = startFunctionIn;
			this.endFunction   = endFunctionIn;
		}

		public void update(long handleIn)
		{
			if (!this.active)
			{
				if (state(handleIn, key) == GLFW.GLFW_PRESS)
				{
					this.active = true;
					this.startFunction.execute();
				}
				return;
			}

			if (state(handleIn, key) == GLFW.GLFW_RELEASE)
			{
				this.active = false;
				this.endFunction.execute();
			}
		}

		public abstract int state(long handleIn, int keyIn);
	}

	public final static class ToggleKey extends Toggle
	{
		public ToggleKey(final int keyIn, final IFunction startFunctionIn, final IFunction endFunctionIn)
		{
			super(keyIn, startFunctionIn, endFunctionIn);
		}

		@Override
		public int state(final long handleIn, final int keyIn)
		{
			return GLFW.glfwGetKey(handleIn, keyIn);
		}
	}

	public final static class ToggleMouse extends Toggle
	{
		public ToggleMouse(final int keyIn, final IFunction startFunctionIn, final IFunction endFunctionIn)
		{
			super(keyIn, startFunctionIn, endFunctionIn);
		}

		@Override
		public int state(final long handleIn, final int keyIn)
		{
			return GLFW.glfwGetMouseButton(handleIn, keyIn);
		}
	}

	public interface IFunction
	{
		void execute();
	}

	@Getter
	public final static class InputManager
	{
		private final static double[] MOUSE_X_ARRAY = new double[1];
		private final static double[] MOUSE_Y_ARRAY = new double[1];

		private final   long              window;
		private         double            mouseX;
		private         double            mouseY;
		private         double            scrollX;
		private         double            scrollY;
		private final   Stack<IHoverable> hovered;
		private final   List<Toggle>      toggleList;
		private @Setter ISelectable       selected;

		public InputManager(long windowIn)
		{
			this.window     = windowIn;
			this.hovered    = new Stack<>();
			this.toggleList = new ArrayList<>();

			GLFW.glfwSetScrollCallback(this.window, (handleIn, x, y) ->
			{
				this.scrollX = x;
				this.scrollY = y;
			});
		}

		public void hovered(IHoverable hoverableIn)
		{
			this.hovered.push(hoverableIn);
		}

		public Toggle registerToggleKey(int keyIn, IFunction startFunctionIn, IFunction endFunctionIn)
		{
			var toggle = new ToggleKey(keyIn, startFunctionIn, endFunctionIn);
			this.toggleList.add(toggle);
			return toggle;
		}

		public Toggle registerToggleMouse(int keyIn, IFunction startFunctionIn, IFunction endFunctionIn)
		{
			var toggle = new ToggleMouse(keyIn, startFunctionIn, endFunctionIn);
			this.toggleList.add(toggle);
			return toggle;
		}

		public boolean pressed(int keyIn)
		{
			return GLFW.glfwGetKey(window, keyIn) == GLFW.GLFW_PRESS;
		}

		public boolean released(int keyIn)
		{
			return GLFW.glfwGetKey(window, keyIn) == GLFW.GLFW_RELEASE;
		}

		public boolean mousePressed(int keyIn)
		{
			return GLFW.glfwGetMouseButton(window, keyIn) == GLFW.GLFW_PRESS;
		}

		public boolean mouseReleased(int keyIn)
		{
			return GLFW.glfwGetMouseButton(window, keyIn) == GLFW.GLFW_RELEASE;
		}

		public void update()
		{
			GLFW.glfwGetCursorPos(this.window, MOUSE_X_ARRAY, MOUSE_Y_ARRAY);
			this.mouseX = MOUSE_X_ARRAY[0];
			this.mouseY = MOUSE_Y_ARRAY[0];

			for (var toggle : this.toggleList)
			{
				toggle.update(this.window);
			}
		}

		public void reset()
		{
			this.scrollX = 0.0D;
			this.scrollY = 0.0D;
			this.hovered.clear();
		}
	}

	public final static class Group implements IDrawable, IUpdatable
	{
		private final Map<String, IDrawable>  drawables;
		private final Map<String, IUpdatable> updatables;
		private final Map<String, IHoverable> hoverables;
		private       ISelectable             selected;
		private       long                    lastSelection;

		public Group()
		{
			this.drawables  = new HashMap<>();
			this.updatables = new HashMap<>();
			this.hoverables = new HashMap<>();
		}

		public <T extends IDrawable> T add(String nameIn, T drawableIn)
		{
			this.drawables.put(nameIn, drawableIn);
			if (drawableIn instanceof IUpdatable)
			{
				this.updatables.put(nameIn, (IUpdatable) drawableIn);
			}

			if (this.selected == null && drawableIn instanceof ISelectable)
			{
				this.select(null, (ISelectable) drawableIn); // TODO add input manager
			}

			if (drawableIn instanceof IHoverable)
			{
				this.hoverables.put(nameIn, (IHoverable) drawableIn);
			}

			return drawableIn;
		}

		private void select(InputManager inputManagerIn, ISelectable selectableIn)
		{
			if (this.selected != null)
			{
				this.selected.unselect();
			}
			this.selected = selectableIn;
			this.selected.select();
			this.lastSelection = System.nanoTime();
			if (inputManagerIn != null)
			{
				inputManagerIn.selected(this.selected);
			}
		}

		public boolean update(InputManager inputManagerIn)
		{
			if (System.nanoTime() - this.lastSelection >= 100_000_000L && this.selected != null
			    && inputManagerIn.pressed(GLFW.GLFW_KEY_TAB))
			{
				this.select(inputManagerIn, selected.next());
				return true;
			}

			for (var hoverable : this.hoverables.values())
			{
				if (hoverable.hovered(inputManagerIn.mouseX(), inputManagerIn.mouseY()))
				{
					inputManagerIn.hovered(hoverable);
				}
			}

			var oneSuccess = false;
			for (var updatable : this.updatables.values())
			{
				if (updatable.update(inputManagerIn))
				{
					oneSuccess = true;
				}
			}

			return oneSuccess;
		}

		public void draw(long handleIn)
		{
			for (var drawable : this.drawables.values())
			{
				drawable.draw(handleIn);
			}
		}
	}

	public interface IUpdatable
	{
		boolean update(InputManager inputManagerIn);
	}

	public interface IHoverable
	{
		boolean hovered(double mouseXIn, double mouseYIn);

		boolean hovered();
	}

	public interface IDrawable
	{
		void draw(long handleIn);
	}

	public interface ISelectable
	{
		void select();

		void unselect();

		ISelectable next();
	}

	@Getter
	public abstract static class UIElement implements IHoverable
	{
		protected @Setter float   x;
		protected @Setter float   y;
		protected @Setter float   width;
		protected @Setter float   height;
		protected         boolean hovered;

		public UIElement(float xIn, float yIn, float widthIn, float heightIn)
		{
			this.x      = xIn;
			this.y      = yIn;
			this.width  = widthIn;
			this.height = heightIn;
		}

		@Override
		public boolean hovered(final double mouseXIn, final double mouseYIn)
		{
			this.hovered = mouseXIn >= this.x && mouseXIn <= this.x + this.width && mouseYIn >= this.y
			               && mouseYIn <= this.y + this.height;

			return this.hovered;
		}
	}

	@Getter
	public final static class TextRecorder extends UIElement implements IDrawable, ISelectable
	{

	}

	@Getter
	public final static class Slider extends UIElement implements IDrawable, IUpdatable, ISelectable
	{
		private final   Color        color;
		private final   Color        selectedColor;
		private final   Color        selectedStrokeColor;
		private final   SliderCursor cursor;
		private         boolean      wantSelected;
		private         boolean      selected;
		private @Setter ISelectable  next;

		public Slider(float xIn, float yIn, float widthIn, float heightIn, float cursorWidthIn, float cursorHeightIn,
		              Color colorIn, Color selectedColorIn, Color selectedStrokeColorIn, Color cursorColorIn,
		              Color cursorHoveredColorIn)
		{
			super(xIn, yIn, widthIn, heightIn);
			this.color               = colorIn;
			this.selectedColor       = selectedColorIn;
			this.selectedStrokeColor = selectedStrokeColorIn;
			this.cursor              = new SliderCursor(this, cursorWidthIn, cursorHeightIn, cursorColorIn,
			                                            cursorHoveredColorIn);
		}

		@Override
		public void select()
		{
			this.wantSelected = true;
		}

		@Override
		public void unselect()
		{
			this.wantSelected = false;
		}

		@Override
		public boolean update(InputManager inputManagerIn)
		{
			this.selected = false;
			if ((this.wantSelected && inputManagerIn.hovered.size() == 0) || this.hovered)
			{
				this.selected = true;
			}

			if (this.cursor.update(inputManagerIn))
			{
				return false;
			}

			if (this.hovered)
			{
				if (inputManagerIn.mousePressed(GLFW.GLFW_MOUSE_BUTTON_LEFT))
				{
					this.cursor.x(inputManagerIn.mouseX());
					return true;
				}
			}

			if (this.hovered || this.selected)
			{
				if (inputManagerIn.pressed(GLFW.GLFW_KEY_KP_ADD) || inputManagerIn.pressed(GLFW.GLFW_KEY_RIGHT))
				{
					this.cursor.x(this.cursor.x() + this.cursor.width() / 2 + 4.0f);
				}
				else if (inputManagerIn.pressed(GLFW.GLFW_KEY_KP_SUBTRACT) || inputManagerIn.pressed(
						GLFW.GLFW_KEY_LEFT))
				{
					this.cursor.x(this.cursor.x() + this.cursor.width() / 2 - 4.0f);
				}
				else
				{
					this.cursor.x(this.cursor.x() + this.cursor.width() / 2 + inputManagerIn.scrollY() * 20.0f);
				}
				return true;
			}

			return false;
		}

		public void draw(long handleIn)
		{
			if (this.selected)
			{
				fillRectangle(handleIn, this.x, this.y, this.width, this.height, this.selectedColor);
				if (this.selectedStrokeColor.a > 0.0f && (this.selectedStrokeColor.r > 0.0f
				                                          || this.selectedStrokeColor.g > 0.0f
				                                          || this.selectedStrokeColor.b > 0.0f))
				{
					NanoVG.nvgStrokeColor(handleIn, selectedStrokeColor.transpose(COLOR));
					NanoVG.nvgStrokeWidth(handleIn, 8.0f);
					NanoVG.nvgStroke(handleIn);
				}
			}
			else
			{
				fillRectangle(handleIn, this.x, this.y, this.width, this.height, this.color);
			}
			this.cursor.draw(handleIn);
		}

		public final static class Builder
		{
			private @Setter       float x;
			private @Setter       float y;
			private @Setter       float width;
			private @Setter       float height;
			private @Setter       float cursorWidth;
			private @Setter       float cursorHeight;
			private final @Getter Color color;
			private final @Getter Color selectedColor;
			private final @Getter Color selectedStrokeColor;
			private final @Getter Color cursorColor;
			private final @Getter Color cursorHoveredColor;

			public Builder()
			{
				this.width               = 100.0f;
				this.height              = 40.0f;
				this.x                   = 0.0f;
				this.y                   = 0.0f;
				this.cursorHeight        = this.height + 8.0f;
				this.cursorWidth         = this.cursorHeight;
				this.color               = new Color(0.5f, 0.5f, 0.5f, 1.0f);
				this.selectedColor       = new Color(0.75f, 0.75f, 0.75f, 1.0f);
				this.selectedStrokeColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
				this.cursorColor         = new Color(1.0f, 0.0f, 0.0f, 1.0f);
				this.cursorHoveredColor  = new Color(1.0f, 1.0f, 0.0f, 1.0f);
			}

			public Slider build()
			{
				return new Slider(this.x, this.y, this.width, this.height, this.cursorWidth, this.cursorHeight,
				                  this.color, this.selectedColor, this.selectedStrokeColor, this.cursorColor,
				                  this.cursorHoveredColor);
			}
		}
	}

	@Getter
	public final static class SliderCursor extends UIElement implements IDrawable, IUpdatable, IHoverable
	{
		private final         Slider  slider;
		private @Setter       float   percent;
		private @Setter       float   width;
		private @Setter       float   height;
		private @Getter final Color   color;
		private @Getter final Color   hoveredColor;
		private               double  useX; // Coordinate when click on cursor
		private               double  useY; // Coordinate when click on cursor
		private               double  offsetX; // Offset between cursor center and click position
		private               double  offsetY; // Offset between cursor center and click position
		private @Setter       int     numValues; // Number of possible values
		private @Setter       float   precision; // Precision factor (step size)
		private               boolean hovered;
		private               boolean used;

		private SliderCursor(Slider sliderIn, float widthIn, float heightIn, Color colorIn, Color hoveredColorIn)
		{
			this.slider       = sliderIn;
			this.width        = widthIn;
			this.height       = heightIn;
			this.color        = colorIn;
			this.hoveredColor = hoveredColorIn;
			this.numValues    = 100;
			this.calculatePrecision();
		}

		public void x(double xIn)
		{
			this.percent = (float) ((Math.clamp(xIn, this.slider.x, this.slider.x + this.slider.width) - this.slider.x)
			                        / this.slider.width);
			this.percent = Math.clamp(this.percent, 0.0f, 1.0f);
			this.snapToPrecision();
		}

		public void use(double xIn, double yIn)
		{
			this.useX    = xIn;
			this.useY    = yIn;
			this.offsetX = xIn - this.x(); // Calculate offset on use
			this.offsetY = yIn - this.y(); // Calculate offset on use
			this.used    = true;
		}

		public boolean update(InputManager inputManagerIn)
		{
			if (!this.used && this.hovered(inputManagerIn.mouseX(), inputManagerIn.mouseY())
			    && inputManagerIn.mousePressed(GLFW.GLFW_MOUSE_BUTTON_LEFT))
			{
				this.use(inputManagerIn.mouseX(), inputManagerIn.mouseY());
				return true;
			}

			if (inputManagerIn.mouseReleased(GLFW.GLFW_MOUSE_BUTTON_LEFT) || !this.used)
			{
				this.unuse();
				return false;
			}

			this.percent = (float) (
					(Math.clamp(inputManagerIn.mouseX() - this.offsetX + this.width / 2.0f, this.slider.x,
					            this.slider.x + this.slider.width) - this.slider.x) / this.slider.width);
			this.percent = Math.clamp(this.percent, 0.0f, 1.0f);
			this.snapToPrecision();

			return true;
		}

		public void unuse()
		{
			this.used = false;
		}

		public boolean hovered(double xIn, double yIn)
		{
			this.hovered =
					xIn >= this.x() && xIn <= this.x() + this.width && yIn >= this.y() && yIn <= this.y() + this.height;

			return this.hovered;
		}

		public void draw(long handleIn)
		{
			if (this.hovered)
			{
				fillRectangle(handleIn, this.x(), this.y(), this.width, this.height(), this.hoveredColor);
			}
			else
			{
				fillRectangle(handleIn, this.x(), this.y(), this.width, this.height(), this.color);
			}
		}

		public float height()
		{
			if (this.slider.selected)
			{
				return this.height + 4.0f;
			}

			return this.height;
		}

		public float x()
		{
			return this.slider.x + this.percent * this.slider.width - this.width / 2.0f;
		}

		public float y()
		{
			return this.slider.y + (this.slider.height - this.height()) / 2.0f;
		}

		private void snapToPrecision()
		{
			if (this.precision > 0)
			{
				this.percent = Math.round(this.percent / this.precision) * this.precision;
				this.percent = Math.clamp(this.percent, 0.0f, 1.0f);
			}
		}

		private void calculatePrecision()
		{
			if (this.numValues > 1)
			{
				this.precision = 1.0f / (this.numValues - 1);
			}
			else
			{
				this.precision = 1.0f;
			}
		}
	}

	@Getter
	@ToString
	@EqualsAndHashCode
	public final static class Color
	{
		private float r;
		private float g;
		private float b;
		private float a;

		public Color()
		{

		}

		public Color(float rIn, float gIn, float bIn, float aIn)
		{
			this.r = rIn;
			this.g = gIn;
			this.b = bIn;
			this.a = aIn;
		}

		public Color(int rIn, int gIn, int bIn, int aIn)
		{
			this.r = rIn / 255.0f;
			this.g = gIn / 255.0f;
			this.b = bIn / 255.0f;
			this.a = aIn / 255.0f;
		}

		public Color(byte rIn, byte gIn, byte bIn, byte aIn)
		{
			this.r = rIn / 255.0f;
			this.g = gIn / 255.0f;
			this.b = bIn / 255.0f;
			this.a = aIn / 255.0f;
		}

		public NVGColor transpose(NVGColor colorIn)
		{
			colorIn.r(this.r);
			colorIn.g(this.g);
			colorIn.b(this.b);
			colorIn.a(this.a);

			return colorIn;
		}

		public Color set(float rIn, float gIn, float bIn, float aIn)
		{
			this.r = rIn;
			this.g = gIn;
			this.b = bIn;
			this.a = aIn;

			return this;
		}

		public Color r(int rIn)
		{
			this.r = rIn / 255.0f;

			return this;
		}

		public Color r(byte rIn)
		{
			this.r = rIn / 255.0f;

			return this;
		}

		public Color r(float rIn)
		{
			this.r = rIn;

			return this;
		}

		public Color g(int gIn)
		{
			this.g = gIn / 255.0f;

			return this;
		}

		public Color g(byte gIn)
		{
			this.g = gIn / 255.0f;

			return this;
		}

		public Color g(float gIn)
		{
			this.g = gIn;

			return this;
		}

		public Color b(int bIn)
		{
			this.b = bIn / 255.0f;

			return this;
		}

		public Color b(byte bIn)
		{
			this.b = bIn / 255.0f;

			return this;
		}

		public Color b(float bIn)
		{
			this.b = bIn;

			return this;
		}

		public Color a(int aIn)
		{
			this.a = aIn / 255.0f;

			return this;
		}

		public Color a(byte aIn)
		{
			this.a = aIn / 255.0f;

			return this;
		}

		public Color a(float aIn)
		{
			this.a = aIn;

			return this;
		}
	}
}