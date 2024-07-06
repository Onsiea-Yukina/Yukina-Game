package fr.yukina.game.logic.player;

import fr.yukina.game.graphic.window.GLFWWindow;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

@Getter
public class Camera
{
	private final Vector3f position;
	private final Vector3f orientation;

	private final Vector3f velocity;
	private final float    friction;  // The rate at which velocity decreases
	private final float    minVelocity;
	private final float    maxVelocity;
	private       boolean  cursorLock;
	private       boolean  cursorLockKeyPressed;

	private final Matrix4f viewMatrix;
	private final Matrix4f projectionMatrix;
	private final Matrix4f projectionViewMatrix;

	private final UpdateState updateState;
	private       double      lastMouseX, lastMouseY;
	private boolean firstMouse = true;

	/**
	 * @param xIn
	 * @param yIn
	 * @param zIn
	 * @param nearIn
	 * @param farIn
	 * @param fovIn    (field of view in radians)
	 * @param aspectIn (width / height)
	 */
	public Camera(float xIn, float yIn, float zIn, float nearIn, float farIn, double fovIn, float aspectIn)
	{
		this.position    = new Vector3f(xIn, yIn, zIn);
		this.orientation = new Vector3f(0, 0, 0);

		this.velocity    = new Vector3f(0, 0, 0);
		this.friction    = 0.9f;
		this.minVelocity = 0.001f;
		this.maxVelocity = 1.0f;
		this.cursorLock  = true;

		this.viewMatrix = new Matrix4f();
		this.updateViewMatrix();
		this.projectionMatrix = new Matrix4f();
		this.updateProjectionMatrix(nearIn, farIn, fovIn, aspectIn);
		this.projectionViewMatrix = new Matrix4f();
		this.preprocessProjectionViewMatrix();
		this.updateState = new UpdateState();
	}

	public void update(GLFWWindow windowIn)
	{
		this.updateOrientation(windowIn);
		this.updatePosition(windowIn);

		this.updateState.update(this.position, this.orientation);
		if (this.updateState.hasChanged)
		{
			this.updateViewMatrix();
			this.preprocessProjectionViewMatrix();
		}
	}

	private void updateOrientation(GLFWWindow windowIn)
	{
		if (windowIn.pressed(GLFW.GLFW_KEY_F1))
		{
			if (!this.cursorLockKeyPressed)
			{
				this.cursorLock           = !this.cursorLock;
				this.cursorLockKeyPressed = true;
			}
		}
		else
		{
			this.cursorLockKeyPressed = false;
		}

		if (!this.cursorLock)
		{
			return;
		}

		var mousePosition = windowIn.mousePosition();

		// Initialize last mouse position on the first frame
		if (this.firstMouse)
		{
			this.lastMouseX = mousePosition[0];
			this.lastMouseY = mousePosition[1];
			this.firstMouse = false;
		}

		// Calculate mouse movement offsets
		double deltaX = mousePosition[0] - this.lastMouseX;
		double deltaY = mousePosition[1] - this.lastMouseY;

		// Update last mouse position
		this.lastMouseX = mousePosition[0];
		this.lastMouseY = mousePosition[1];

		// Sensitivity settings
		float sensitivity = 0.1f;
		deltaX *= sensitivity;
		deltaY *= sensitivity;

		this.orientation.x = (float) Math.clamp(this.orientation.x + deltaY, -89.0f, 89.0f);
		this.orientation.x %= 360;
		this.orientation.y = (float) ((this.orientation.y + deltaX) % 360);

		// Re-center the mouse
		GLFW.glfwSetCursorPos(windowIn.handle(), windowIn.width() / 2.0, windowIn.height() / 2.0);
		this.lastMouseX = windowIn.width() / 2.0;
		this.lastMouseY = windowIn.height() / 2.0;
	}

	private void updatePosition(GLFWWindow windowIn)
	{
		var acceleration = 0.0125f;
		if (windowIn.pressed(GLFW.GLFW_KEY_LEFT_CONTROL))
		{
			acceleration = 0.05f;
		}
		else if (windowIn.pressed(GLFW.GLFW_KEY_LEFT_ALT))
		{
			acceleration = 0.005f;
		}

		Vector3f forward = new Vector3f((float) Math.sin(Math.toRadians(this.orientation.y)), 0,
		                                (float) -Math.cos(Math.toRadians(this.orientation.y)));

		Vector3f right = new Vector3f((float) Math.cos(Math.toRadians(this.orientation.y)), 0,
		                              (float) Math.sin(Math.toRadians(this.orientation.y)));

		if (windowIn.pressed(GLFW.GLFW_KEY_W))
		{
			velocity.x += forward.x * acceleration;
			velocity.z += forward.z * acceleration;
		}

		if (windowIn.pressed(GLFW.GLFW_KEY_S))
		{
			velocity.x -= forward.x * acceleration;
			velocity.z -= forward.z * acceleration;
		}

		if (windowIn.pressed(GLFW.GLFW_KEY_A))
		{
			velocity.x -= right.x * acceleration;
			velocity.z -= right.z * acceleration;
		}

		if (windowIn.pressed(GLFW.GLFW_KEY_D))
		{
			velocity.x += right.x * acceleration;
			velocity.z += right.z * acceleration;
		}

		if (windowIn.pressed(GLFW.GLFW_KEY_LEFT_SHIFT))
		{
			velocity.y -= acceleration;
		}
		if (windowIn.pressed(GLFW.GLFW_KEY_SPACE))
		{
			velocity.y += acceleration;
		}

		// Apply friction to simulate inertia
		velocity.mul(friction);

		// Clamp velocity to maximum value
		if (velocity.length() > maxVelocity)
		{
			velocity.normalize(maxVelocity);
		}

		// Apply minimum velocity threshold
		if (velocity.length() < minVelocity)
		{
			velocity.set(0, 0, 0);
		}

		// Update position with the current velocity
		this.position.x += velocity.x;
		this.position.y += velocity.y;
		this.position.z += velocity.z;
	}

	private void updateViewMatrix()
	{
		this.viewMatrix.rotationX((float) Math.toRadians(this.orientation.x))
		               .rotateY((float) Math.toRadians(this.orientation.y))
		               .rotateZ((float) Math.toRadians(this.orientation.z))
		               .translate(-this.position.x, -this.position.y, -this.position.z);
	}

	private void updateProjectionMatrix(float nearIn, float farIn, double fovIn, float aspectIn)
	{
		this.projectionMatrix.identity();
		this.projectionMatrix.setPerspective((float) fovIn, aspectIn, nearIn, farIn);
	}

	private void preprocessProjectionViewMatrix()
	{
		this.projectionViewMatrix.set(this.projectionMatrix);
		this.projectionViewMatrix.mul(this.viewMatrix);
	}

	@Getter
	public final static class UpdateState
	{
		private final Vector3f lastPosition;
		private final Vector3f lastOrientation;
		private final Vector3i lastBlock;
		private final Vector3i lastChunk;

		private boolean hasChanged;
		private boolean hasRotated;
		private boolean hasMoved;
		private boolean hasMoveOneBlock;
		private boolean hasMoveOneChunk;

		private double movementSquare;
		private double movementX;
		private double movementY;
		private double movementZ;

		private double rotationSquare;
		private double rotationX;
		private double rotationY;
		private double rotationZ;

		public UpdateState()
		{
			this.lastPosition    = new Vector3f();
			this.lastOrientation = new Vector3f();
			this.lastBlock       = new Vector3i();
			this.lastChunk       = new Vector3i();
		}

		public void update(Vector3f positionIn, Vector3f orientationIn)
		{
			this.reset();

			if (positionIn.x != this.lastPosition.x || positionIn.y != this.lastPosition.y
			    || positionIn.z != this.lastPosition.z)
			{
				this.hasChanged = true;
				this.hasMoved   = true;

				var newBlockX = (int) (positionIn.x);
				var newBlockY = (int) (positionIn.y);
				var newBlockZ = (int) (positionIn.z);
				if (newBlockX != this.lastBlock.x || newBlockY != this.lastBlock.y || newBlockZ != this.lastBlock.z)
				{
					this.hasMoveOneBlock = true;
					this.lastBlock.x     = newBlockX;
					this.lastBlock.y     = newBlockY;
					this.lastBlock.z     = newBlockZ;
				}

				// TODO TERRAIN SIZE
				var newChunkX = (int) (positionIn.x / 16.0f);
				var newChunkY = (int) (positionIn.y / 16.0f);
				var newChunkZ = (int) (positionIn.z / 16.0f);
				if (newChunkX != this.lastChunk.x || newChunkY != this.lastChunk.y || newChunkZ != this.lastChunk.z)
				{
					this.hasMoveOneChunk = true;
					this.lastChunk.x     = newChunkX;
					this.lastChunk.y     = newChunkY;
					this.lastChunk.z     = newChunkZ;
				}

				this.movementX      = positionIn.x - this.lastPosition.x;
				this.movementY      = positionIn.y - this.lastPosition.y;
				this.movementZ      = positionIn.z - this.lastPosition.z;
				this.movementSquare = this.movementX * this.movementX + this.movementY * this.movementY
				                      + this.movementZ * this.movementZ;

				this.lastPosition.x = positionIn.x;
				this.lastPosition.y = positionIn.y;
				this.lastPosition.z = positionIn.z;
			}

			if (orientationIn.x != this.lastOrientation.x || orientationIn.y != this.lastOrientation.y
			    || orientationIn.z != this.lastOrientation.z)
			{
				this.hasChanged = true;
				this.hasRotated = true;

				this.rotationX      = orientationIn.x - this.lastOrientation.x;
				this.rotationY      = orientationIn.y - this.lastOrientation.y;
				this.rotationZ      = orientationIn.z - this.lastOrientation.z;
				this.rotationSquare = this.rotationX * this.rotationX + this.rotationY * this.rotationY
				                      + this.rotationZ * this.rotationZ;

				this.lastOrientation.x = orientationIn.x;
				this.lastOrientation.y = orientationIn.y;
				this.lastOrientation.z = orientationIn.z;
			}
		}

		public void reset()
		{
			this.hasChanged      = false;
			this.hasRotated      = false;
			this.hasMoved        = false;
			this.hasMoveOneBlock = false;
			this.hasMoveOneChunk = false;

			this.movementX      = 0.0f;
			this.movementY      = 0.0f;
			this.movementZ      = 0.0f;
			this.movementSquare = 0.0f;

			this.rotationX      = 0.0f;
			this.rotationY      = 0.0f;
			this.rotationZ      = 0.0f;
			this.rotationSquare = 0.0f;
		}
	}
}