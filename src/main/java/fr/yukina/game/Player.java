package fr.yukina.game;

import fr.yukina.game.window.IWindow;
import lombok.Getter;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Player implements IGameLogic
{
	private final @Getter FPSCamera   camera;
	private               float       speed            = 0.5f;
	private               float       mouseSensitivity = 0.1f;
	private final @Getter UpdateState updateState;

	public Player(FPSCamera cameraIn)
	{
		this.camera      = cameraIn;
		this.updateState = new UpdateState(this);
	}

	@Override
	public void initialize()
	{
		this.camera.initialize();
	}

	@Override
	public void input(IWindow windowIn)
	{
		this.camera.projectionViewMatrixHasChanged(false);

		glfwSetInputMode(windowIn.handle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		float cameraSpeed = speed;

		// Forward and backward movement
		if (glfwGetKey(windowIn.handle(), GLFW_KEY_W) == GLFW_PRESS)
		{
			this.camera.position()
			           .add(new Vector3f((float) Math.sin(Math.toRadians(this.camera.rotation().y)) * cameraSpeed, 0,
			                             (float) -Math.cos(Math.toRadians(this.camera.rotation().y)) * cameraSpeed));
		}
		if (glfwGetKey(windowIn.handle(), GLFW_KEY_S) == GLFW_PRESS)
		{
			this.camera.position()
			           .sub(new Vector3f((float) Math.sin(Math.toRadians(this.camera.rotation().y)) * cameraSpeed, 0,
			                             (float) -Math.cos(Math.toRadians(this.camera.rotation().y)) * cameraSpeed));
		}

		// Left and right movement
		if (glfwGetKey(windowIn.handle(), GLFW_KEY_A) == GLFW_PRESS)
		{
			this.camera.position()
			           .sub(new Vector3f((float) Math.cos(Math.toRadians(this.camera.rotation().y)) * cameraSpeed, 0,
			                             (float) Math.sin(Math.toRadians(this.camera.rotation().y)) * cameraSpeed));
		}
		if (glfwGetKey(windowIn.handle(), GLFW_KEY_D) == GLFW_PRESS)
		{
			this.camera.position()
			           .add(new Vector3f((float) Math.cos(Math.toRadians(this.camera.rotation().y)) * cameraSpeed, 0,
			                             (float) Math.sin(Math.toRadians(this.camera.rotation().y)) * cameraSpeed));
		}

		// Upward movement
		if (glfwGetKey(windowIn.handle(), GLFW_KEY_SPACE) == GLFW_PRESS)
		{
			this.camera.position().y += cameraSpeed;
		}

		// Downward movement
		if (glfwGetKey(windowIn.handle(), GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS)
		{
			this.camera.position().y -= cameraSpeed;
		}

		// Mouse movement for camera rotation
		double[] mouseX = new double[1];
		double[] mouseY = new double[1];
		glfwGetCursorPos(windowIn.handle(), mouseX, mouseY);
		glfwSetCursorPos(windowIn.handle(), windowIn.width() / 2.0D, windowIn.height() / 2.0D); // Reset cursor
		// position to
		// the center

		this.camera.rotation().y += (float) (mouseX[0] - windowIn.width() / 2.0D) * mouseSensitivity;
		this.camera.rotation().x += (float) (mouseY[0] - windowIn.height() / 2.0D) * mouseSensitivity;

		// Clamp the pitch (x rotation) to prevent flipping
		if (this.camera.rotation().x > 90.0f)
		{
			this.camera.rotation().x = 90.0f;
		}
		else if (this.camera.rotation().x < -90.0f)
		{
			this.camera.rotation().x = -90.0f;
		}

		this.updateState.update();
		if (this.updateState.hasChanged)
		{
			this.camera.updateViewMatrix();
		}
	}

	@Override
	public void update()
	{

	}

	@Override
	public void cleanup()
	{
		this.camera.cleanup();
	}

	@Getter
	public final static class UpdateState
	{
		private final Player player;

		private boolean hasChanged;

		private final Vector3f lastPosition;
		private       boolean  hasMove;
		private       boolean  hasMoveOneBlock;
		private       boolean  hasMoveOneChunk;
		private       float    movementDistanceX;
		private       float    movementDistanceY;
		private       float    movementDistanceZ;
		private       float    squareMovementDistance;
		private       float    squareMovementDistanceBlock;
		private       float    squareMovementDistanceChunk;

		private final Vector3f lastOrientation;
		private       boolean  hasRotated;
		private       float    rotationDistanceX;
		private       float    rotationDistanceY;
		private       float    rotationDistanceZ;
		private       float    squareRotationDistance;

		public UpdateState(Player playerIn)
		{
			this.player          = playerIn;
			this.lastPosition    = new Vector3f(playerIn.camera.position());
			this.lastOrientation = new Vector3f(playerIn.camera.rotation());
		}

		public void update()
		{
			this.hasChanged = false;
			this.checkMovements();
			if (this.hasMove)
			{
				if (this.squareMovementDistanceBlock > 1.0f)
				{
					this.hasMoveOneBlock             = true;
					this.squareMovementDistanceBlock = 0.0f;
				}

				if (this.squareMovementDistanceChunk > 1.0f)
				{
					this.hasMoveOneChunk             = true;
					this.squareMovementDistanceChunk = 0.0f;
				}
			}
			this.checkRotations();
		}

		private void checkMovements()
		{
			this.hasMove         = false;
			this.hasMoveOneBlock = false;
			this.hasMoveOneChunk = false;

			this.movementDistanceX = this.lastPosition.x - this.player.camera.position().x;
			this.movementDistanceY = this.lastPosition.y - this.player.camera.position().y;
			this.movementDistanceZ = this.lastPosition.z - this.player.camera.position().z;
			this.lastPosition.set(this.player.camera.position());
			this.squareMovementDistance = movementDistanceX * movementDistanceX + movementDistanceY * movementDistanceY
			                              + movementDistanceZ * movementDistanceZ;
			this.squareMovementDistanceBlock += this.squareMovementDistance;
			this.squareMovementDistanceChunk += this.squareMovementDistance;

			if (this.squareMovementDistance <= 0)
			{
				return;
			}

			this.hasChanged = true;
			this.hasMove    = true;
		}

		private void checkRotations()
		{
			this.hasRotated = false;

			this.rotationDistanceX = this.lastOrientation.x - this.player.camera.rotation().x;
			this.rotationDistanceY = this.lastOrientation.y - this.player.camera.rotation().y;
			this.rotationDistanceZ = this.lastOrientation.z - this.player.camera.rotation().z;
			this.lastOrientation.set(this.player.camera.rotation());
			this.squareRotationDistance =
					this.rotationDistanceX * this.rotationDistanceX + this.rotationDistanceY * this.rotationDistanceY
					+ this.rotationDistanceZ * this.rotationDistanceZ;

			if (this.squareRotationDistance <= 0)
			{
				return;
			}

			this.hasChanged = true;
			this.hasRotated = true;
		}
	}
}