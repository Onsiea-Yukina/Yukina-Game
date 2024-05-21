package fr.yukina;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

@Setter
@Getter
public class Camera
{
	private Vector3f position;
	private Vector3f front;
	private Vector3f up;
	private float    yaw;
	private float    pitch;
	private float    speed;
	private float    sensitivity;

	public Camera(Vector3f position)
	{
		this.position    = position;
		this.front       = new Vector3f(0.0f, 0.0f, -1.0f);
		this.up          = new Vector3f(0.0f, 1.0f, 0.0f);
		this.yaw         = -90.0f;
		this.pitch       = 0.0f;
		this.speed       = 0.5f;
		this.sensitivity = 0.05f;
	}

	public void processInput(long window)
	{
		if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS)
		{
			position.add(new Vector3f(front).mul(speed));
		}
		if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS)
		{
			position.sub(new Vector3f(front).mul(speed));
		}
		if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS)
		{
			position.sub(new Vector3f(front).cross(up).normalize().mul(speed));
		}
		if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS)
		{
			position.add(new Vector3f(front).cross(up).normalize().mul(speed));
		}
		if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS)
		{
			position.y += speed;
		}
		if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS)
		{
			position.y -= speed;
		}
	}

	public void processMouseInput(double xoffset, double yoffset)
	{
		xoffset *= sensitivity;
		yoffset *= sensitivity;

		yaw += xoffset;
		pitch += yoffset;

		if (pitch > 89.0f)
		{
			pitch = 89.0f;
		}
		if (pitch < -89.0f)
		{
			pitch = -89.0f;
		}

		updateCameraVectors();
	}

	private void updateCameraVectors()
	{
		Vector3f front = new Vector3f();
		front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
		front.y = (float) Math.sin(Math.toRadians(pitch));
		front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
		this.front.set(front.normalize());
	}

	public Matrix4f getViewMatrix()
	{
		return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
	}
}