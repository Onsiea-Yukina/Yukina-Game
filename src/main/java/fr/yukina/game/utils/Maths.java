package fr.yukina.game.utils;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Maths
{
	public final static Vector3f ZERO_VECTOR = new Vector3f(0, 0, 0);

	public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale)
	{
		var matrix = new Matrix4f();
		matrix.identity();
		matrix.translate(translation.x, translation.y, 0);
		matrix.scale(scale.x, scale.y, 1);
		return matrix;
	}

	public final static Matrix4f createTransformationMatrix(Vector3f positionIn, Vector3f rotationIn, Vector3f scaleIn)
	{
		return createTransformationMatrix(positionIn.x, positionIn.y, positionIn.z, rotationIn.x, rotationIn.y,
		                                  rotationIn.z, scaleIn.x, scaleIn.y, scaleIn.z);
	}

	public final static Matrix4f createTransformationMatrix(Vector3f positionIn, float rotXIn, float rotYIn,
	                                                        float rotZIn, float scaleIn)
	{
		return createTransformationMatrix(positionIn.x, positionIn.y, positionIn.z, rotXIn, rotYIn, rotZIn, scaleIn,
		                                  scaleIn, scaleIn);
	}

	public final static Matrix4f createTransformationMatrix(float xIn, float yIn, float zIn, float rotXIn,
	                                                        float rotYIn,
	                                                        float rotZIn, float scaleIn)
	{
		return createTransformationMatrix(xIn, yIn, zIn, rotXIn, rotYIn, rotZIn, scaleIn, scaleIn, scaleIn);
	}

	public final static Matrix4f createTransformationMatrix(float xIn, float yIn, float zIn, float rotXIn,
	                                                        float rotYIn,
	                                                        float rotZIn, float scaleXIn, float scaleYIn,
	                                                        float scaleZIn)
	{
		Matrix4f matrix = new Matrix4f();

		matrix.identity();
		matrix.translate(xIn, yIn, zIn);
		matrix.rotateX((float) Math.toRadians(rotXIn));
		matrix.rotateY((float) Math.toRadians(rotYIn));
		matrix.rotateZ((float) Math.toRadians(rotZIn));
		matrix.scale(scaleXIn, scaleYIn, scaleZIn);

		return matrix;
	}

	public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos)
	{
		float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		float l1  = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
		float l2  = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
		float l3  = 1.0f - l1 - l2;
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}

	public final Matrix4f createProjectionMatrix(float fovIn, float zNearIn, float zFarIn, float widthIn,
	                                             float heightIn)
	{
		float aspectRatio    = widthIn / heightIn;
		float y_scale        = (float) ((1f / Math.tan(Math.toRadians(fovIn / 2f))));
		float x_scale        = y_scale / aspectRatio;
		float frustum_length = zFarIn - zNearIn;

		Matrix4f matrix = new Matrix4f();
		matrix.identity();
		matrix.m00(x_scale);
		matrix.m11(y_scale);
		matrix.m22(-((zFarIn + zNearIn) / frustum_length));
		matrix.m23(-1);
		matrix.m32(-((2 * zNearIn * zFarIn) / frustum_length));
		matrix.m33(0);
		return matrix;
	}
}