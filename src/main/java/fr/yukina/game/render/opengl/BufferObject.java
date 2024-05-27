package fr.yukina.game.render.opengl;

import lombok.Getter;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryStack;

import java.nio.*;

@Getter
public class BufferObject
{
	private final int target;
	private final int id;
	private       int count;

	public BufferObject(int targetIn)
	{
		this.target = targetIn;
		this.id     = GL15.glGenBuffers();
	}

	public BufferObject()
	{
		this.target = GL15.GL_ARRAY_BUFFER;
		this.id     = GL15.glGenBuffers();
	}

	public void attach()
	{
		GL15.glBindBuffer(this.target, this.id);
	}

	/**
	 * Array methods
	 */

	/**
	 * @param dataIn
	 */
	public final void data(byte[] dataIn)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			this.count = dataIn.length;
			var buffer = stack.malloc(dataIn.length);
			buffer.put(dataIn);
			buffer.flip();
			GL15.glBufferData(this.target, buffer, GL15.GL_DYNAMIC_DRAW);
		}
	}

	/**
	 * @param dataIn
	 */
	public final void data(short[] dataIn)
	{
		this.count = dataIn.length;
		GL15.glBufferData(this.target, dataIn, GL15.GL_DYNAMIC_DRAW);
	}

	/**
	 * @param dataIn
	 */
	public final void data(int[] dataIn)
	{
		this.count = dataIn.length;
		GL15.glBufferData(this.target, dataIn, GL15.GL_DYNAMIC_DRAW);
	}

	/**
	 * @param dataIn
	 */
	public final void data(float[] dataIn)
	{
		this.count = dataIn.length;
		GL15.glBufferData(this.target, dataIn, GL15.GL_DYNAMIC_DRAW);
	}

	/**
	 * @param dataIn
	 */
	public final void data(double[] dataIn)
	{
		this.count = dataIn.length;
		GL15.glBufferData(this.target, dataIn, GL15.GL_DYNAMIC_DRAW);
	}

	/**
	 * @param dataIn
	 */
	public final void data(long[] dataIn)
	{
		this.count = dataIn.length;
		GL15.glBufferData(this.target, dataIn, GL15.GL_DYNAMIC_DRAW);
	}

	/**
	 * Buffer methods
	 */
	public final void data(ByteBuffer dataIn)
	{
		this.count = dataIn.capacity();
		GL15.glBufferData(this.target, dataIn, GL15.GL_DYNAMIC_DRAW);
	}

	/**
	 * @param dataIn
	 */
	public final void data(ShortBuffer dataIn)
	{
		this.count = dataIn.capacity();
		GL15.glBufferData(this.target, dataIn, GL15.GL_DYNAMIC_DRAW);
	}

	/**
	 * @param dataIn
	 */
	public final void data(IntBuffer dataIn)
	{
		this.count = dataIn.capacity();
		GL15.glBufferData(this.target, dataIn, GL15.GL_DYNAMIC_DRAW);
	}

	/**
	 * @param dataIn
	 */
	public final void data(FloatBuffer dataIn)
	{
		this.count = dataIn.capacity();
		GL15.glBufferData(this.target, dataIn, GL15.GL_DYNAMIC_DRAW);
	}

	/**
	 * @param dataIn
	 */
	public final void data(DoubleBuffer dataIn)
	{
		this.count = dataIn.capacity();
		GL15.glBufferData(this.target, dataIn, GL15.GL_DYNAMIC_DRAW);
	}

	/**
	 * @param dataIn
	 */
	public final void data(LongBuffer dataIn)
	{
		this.count = dataIn.capacity();
		GL15.glBufferData(this.target, dataIn, GL15.GL_DYNAMIC_DRAW);
	}

	/**
	 * Size method
	 */

	/**
	 * @param sizeIn
	 */
	public final void size(int sizeIn)
	{
		this.count = sizeIn;
		GL15.glBufferData(this.target, sizeIn, GL15.GL_DYNAMIC_DRAW);
	}

	public void detach()
	{
		GL15.glBindBuffer(this.target, 0);
	}

	public void cleanup()
	{
		this.detach();
		GL15.glDeleteBuffers(this.id);
	}
}