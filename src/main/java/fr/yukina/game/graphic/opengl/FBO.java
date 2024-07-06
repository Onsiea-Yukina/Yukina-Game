package fr.yukina.game.graphic.opengl;

import fr.yukina.game.graphic.window.GLFWWindow;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

@Getter
@Setter
public class FBO
{
	public static final int NONE                = 0;
	public static final int DEPTH_TEXTURE       = 1;
	public static final int DEPTH_RENDER_BUFFER = 2;

	private int width;
	private int height;

	private int frameBuffer;

	private int colourTexture;
	private int depthTexture;

	private int depthBuffer;
	private int colourBuffer;

	private boolean multisample;

	/**
	 * Creates an FBO of a specified width and height, with the desired type of
	 * depth buffer attachment.
	 * @param widthIn           - the width of the FBO.
	 * @param heightIn          - the height of the FBO.
	 * @param depthBufferTypeIn - an int indicating the type of depth buffer attachment that
	 *                          this FBO should use.
	 */
	public FBO(int widthIn, int heightIn, int depthBufferTypeIn, GLFWWindow windowIn) throws Exception
	{
		this.width(widthIn);
		this.height(heightIn);
		this.initialiseFrameBuffer(depthBufferTypeIn, windowIn);
		this.multisample(false);
	}

	public FBO(int widthIn, int heightIn, GLFWWindow windowIn) throws Exception
	{
		this.width(widthIn);
		this.height(heightIn);
		this.multisample(false);
		this.initialiseFrameBuffer(FBO.DEPTH_TEXTURE, windowIn);
	}

	public FBO(int widthIn, int heightIn, int depthRenderBufferIn, GLFWWindow windowIn, boolean multisampledIn)
	throws Exception
	{
		this.width(widthIn);
		this.height(heightIn);
		this.multisample(multisampledIn);
		this.initialiseFrameBuffer(FBO.DEPTH_TEXTURE, windowIn);
	}

	// Static methods

	public final static void unbind()
	{
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

	// Methods

	public void bind()
	{
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.frameBuffer());
	}

	/**
	 * Binds the frame buffer, setting it as the current render target. Anything
	 * rendered after this will be rendered to this FBO, and not to the screen.
	 */
	public void start()
	{
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.frameBuffer());
		GL11.glViewport(0, 0, this.width(), this.height());
	}

	/**
	 * Binds the frame buffer, setting it as the current render target. Anything
	 * rendered after this will be rendered to this FBO, and not to the screen.
	 */
	public void start(final int xIn, final int yIn, final int widthIn, final int heightIn)
	{
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.frameBuffer());
		GL11.glViewport(xIn, yIn, widthIn, heightIn);
	}

	/**
	 * Unbinds the frame buffer, setting the default frame buffer as the current
	 * render target. Anything rendered after this will be rendered to the
	 * screen, and not this FBO.
	 */
	public void stop(GLFWWindow windowIn)
	{
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, windowIn.width(), windowIn.height());
	}

	/**
	 * Binds the current FBO to be read from
	 */
	public void bindToRead()
	{
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, this.frameBuffer());
		GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
	}

	public void resolveToFBO(FBO outputFBOIn, GLFWWindow windowIn)
	{
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, outputFBOIn.frameBuffer());
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.frameBuffer());
		GL30.glBlitFramebuffer(0, 0, this.width(), this.height(), 0, 0, outputFBOIn.width(), outputFBOIn.height(),
		                       GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
		this.stop(windowIn);
	}

	public void resolveToScreen(GLFWWindow windowIn)
	{
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.frameBuffer());
		GL11.glDrawBuffer(GL11.GL_BACK);
		GL30.glBlitFramebuffer(0, 0, this.width(), this.height(), 0, 0, windowIn.width(), windowIn.height(),
		                       GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
		this.stop(windowIn);
	}

	/**
	 * Creates the FBO along with a colour buffer texture attachment, and
	 * possibly a depth buffer.
	 * @param typeIn - the type of depth buffer attachment to be attached to the
	 *               FBO.
	 * @throws Exception
	 */
	private void initialiseFrameBuffer(int typeIn, GLFWWindow windowIn) throws Exception
	{
		this.createFrameBuffer();
		if (this.multisample())
		{
			this.createMultisampleColourAttachment();
		}
		else
		{
			this.createTextureAttachment();
		}

		if (typeIn == FBO.DEPTH_RENDER_BUFFER)
		{
			this.createDepthBufferAttachment();
		}
		else if (typeIn == FBO.DEPTH_TEXTURE)
		{
			this.createDepthTextureAttachment();
		}

		if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
		{
			throw new Exception("[ERROR] Failed to create framebuffer !");
		}

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

		this.stop(windowIn);
	}

	/**
	 * Creates a new frame buffer object and sets the buffer to which drawing
	 * will occur - colour attachment 0. This is the attachment where the colour
	 * buffer texture is.
	 */
	private void createFrameBuffer()
	{
		this.frameBuffer(GL30.glGenFramebuffers());
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.frameBuffer());
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
	}

	/**
	 * Creates a texture and sets it as the colour buffer attachment for this
	 * FBO.
	 */
	private void createTextureAttachment()
	{
		this.colourTexture(GL11.glGenTextures());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.colourTexture());
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, this.width(), this.height(), 0, GL11.GL_RGBA,
		                  GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D,
		                            this.colourTexture(), 0);
	}

	private void createMultisampleColourAttachment()
	{
		this.colourBuffer(GL30.glGenRenderbuffers());
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, this.colourBuffer());
		GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, 4, GL11.GL_RGBA8, this.width(), this.height());
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_RENDERBUFFER,
		                               this.depthBuffer());
	}

	/**
	 * Adds a depth buffer to the FBO in the form of a texture, which can later
	 * be sampled.
	 */
	private void createDepthTextureAttachment()
	{
		this.depthTexture(GL11.glGenTextures());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.depthTexture());
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, this.width(), this.height(), 0,
		                  GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D,
		                            this.depthTexture(), 0);
	}

	/**
	 * Adds a depth buffer to the FBO in the form of a render buffer. This can't
	 * be used for sampling in the shaders.
	 */
	private void createDepthBufferAttachment()
	{
		this.depthBuffer(GL30.glGenRenderbuffers());
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, this.depthBuffer());
		if (!this.multisample())
		{
			GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, this.width(), this.height());
		}
		else
		{
			GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, 4, GL14.GL_DEPTH_COMPONENT24, this.width(),
			                                      this.height());
		}
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER,
		                               this.depthBuffer());
	}

	/**
	 * It is necessary to attach the frame buffer beforehand.
	 **/
	public void clear(GLFWWindow windowIn)
	{
		// GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		this.stop(windowIn);
	}

	/**
	 * Deletes the frame buffer and its attachments when the game closes.
	 */
	public void cleanup()
	{
		GL30.glDeleteFramebuffers(this.frameBuffer());
		GL11.glDeleteTextures(this.colourTexture());
		GL11.glDeleteTextures(this.depthTexture());
		GL30.glDeleteRenderbuffers(this.depthBuffer());
		GL30.glDeleteRenderbuffers(this.colourBuffer());
	}
}