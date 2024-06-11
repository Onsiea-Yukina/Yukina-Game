package fr.yukina.game.render;

import fr.yukina.game.IGame;
import fr.yukina.game.YukinaGame;
import fr.yukina.game.render.opengl.GraphicObject;
import fr.yukina.game.window.IWindow;
import fr.yukina.game.world.World;
import org.lwjgl.opengl.*;
import org.lwjgl.system.Callback;

public class YukinaGameRenderer implements IGameRenderer
{
	private       IWindow       window;
	private       IGame         game;
	private       WorldRenderer worldRenderer;
	private       Callback      debugCallback;
	private       GraphicObject graphicObject;
	private       boolean       wireframeMode       = false; // Toggle this to switch between wireframe and fill modes
	private final float[]       viewProjectionArray = new float[16];
	private       int           projectionViewLocation;

	@Override
	public void initialize(final IWindow windowIn, final IGame gameIn)
	{
		this.window = windowIn;
		this.game   = gameIn;

		GL.createCapabilities();
		/*var debugCallback = new GLDebugMessageCallback()
		{
			@Override
			public void invoke(int source, int type, int id, int severity, int length, long message, long userParam)
			{
				System.err.println("OpenGL Debug Message:");
				System.err.println("    Source: " + source(source));
				System.err.println("    Type: " + type(type));
				System.err.println("    ID: " + id);
				System.err.println("    Severity: " + severity(severity));
				System.err.println("    Message: " + getMessage(length, message));
				System.err.println();
			}
		};
		GL43.glDebugMessageCallback(debugCallback, 0);
		this.debugCallback = debugCallback;

		GL43.glEnable(GL43.GL_DEBUG_OUTPUT);
		GL43.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);*/

		var world = (World) ((YukinaGame) this.game).world();
		this.worldRenderer = new WorldRenderer(world);
		this.worldRenderer.initialize();

		this.graphicObject = new GraphicObject();
		this.graphicObject.attach();
		var vbo = this.graphicObject.bufferObject(GL32.GL_ARRAY_BUFFER, 3, 0, 0);
		// 3D rectangle
		vbo.data(new float[] { 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f });
		vbo.detach();
		this.graphicObject.indexBufferObject().data(new int[] { 0, 1, 2, 2, 3, 0 });
		this.graphicObject.detach();

		this.worldRenderer.shaderManager().attach();
		var program = this.worldRenderer.shaderManager().programId();
		this.projectionViewLocation = GL20.glGetUniformLocation(program, "projectionView");
		this.worldRenderer.shaderManager().detach();
		this.wireframeMode(this.wireframeMode);
		this.defaultState();
	}

	public void wireframeMode(boolean wireframeModeIn)
	{
		if (wireframeModeIn)
		{
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			return;
		}
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}

	public void defaultState()
	{
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glViewport(0, 0, this.window.width(), this.window.height());
	}

	@Override
	public boolean render()
	{
		this.worldRenderer.update();
		if (this.worldRenderer.chunkRenderers().size() == 0 || (!this.worldRenderer.hasChanged() && !this.game.player()
		                                                                                                      .camera()
		                                                                                                      .projectionViewMatrixHasChanged()))
		{
			return false;
		}
		System.out.println("Rendering...");

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		this.worldRenderer.shaderManager().attach();
		if (this.game.player().camera().projectionViewMatrixHasChanged())
		{
			this.game.player().camera().projectionViewMatrix().get(this.viewProjectionArray);
			GL20.glUniformMatrix4fv(this.projectionViewLocation, false, this.viewProjectionArray);
		}

		this.worldRenderer.render(this.game.player().camera().projectionMatrix(),
		                          this.game.player().camera().viewMatrix());

		return true;
	}

	@Override
	public void cleanup()
	{
		if (this.worldRenderer != null)
		{
			this.worldRenderer.cleanup();
		}
		if (this.debugCallback != null)
		{
			this.debugCallback.free();
			GL43.glDebugMessageCallback(null, 0);
		}
	}

	// Utility methods to convert debug constants to string
	private static String source(int sourceIn)
	{
		switch (sourceIn)
		{
			case GL43.GL_DEBUG_SOURCE_API:
				return "API";
			case GL43.GL_DEBUG_SOURCE_WINDOW_SYSTEM:
				return "Window System";
			case GL43.GL_DEBUG_SOURCE_SHADER_COMPILER:
				return "Shader Compiler";
			case GL43.GL_DEBUG_SOURCE_THIRD_PARTY:
				return "Third Party";
			case GL43.GL_DEBUG_SOURCE_APPLICATION:
				return "Application";
			case GL43.GL_DEBUG_SOURCE_OTHER:
				return "Other";
			default:
				return "Unknown";
		}
	}

	private static String type(int typeIn)
	{
		switch (typeIn)
		{
			case GL43.GL_DEBUG_TYPE_ERROR:
				return "Error";
			case GL43.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR:
				return "Deprecated Behavior";
			case GL43.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR:
				return "Undefined Behavior";
			case GL43.GL_DEBUG_TYPE_PORTABILITY:
				return "Portability";
			case GL43.GL_DEBUG_TYPE_PERFORMANCE:
				return "Performance";
			case GL43.GL_DEBUG_TYPE_MARKER:
				return "Marker";
			case GL43.GL_DEBUG_TYPE_PUSH_GROUP:
				return "Push Group";
			case GL43.GL_DEBUG_TYPE_POP_GROUP:
				return "Pop Group";
			case GL43.GL_DEBUG_TYPE_OTHER:
				return "Other";
			default:
				return "Unknown";
		}
	}

	private static String severity(int severityIn)
	{
		switch (severityIn)
		{
			case GL43.GL_DEBUG_SEVERITY_HIGH:
				return "High";
			case GL43.GL_DEBUG_SEVERITY_MEDIUM:
				return "Medium";
			case GL43.GL_DEBUG_SEVERITY_LOW:
				return "Low";
			case GL43.GL_DEBUG_SEVERITY_NOTIFICATION:
				return "Notification";
			default:
				return "Unknown";
		}
	}

	private static String message(int lengthIn, long messageIn)
	{
		return GLDebugMessageCallback.getMessage(lengthIn, messageIn);
	}
}