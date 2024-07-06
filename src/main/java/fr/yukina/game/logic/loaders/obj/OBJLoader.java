package fr.yukina.game.logic.loaders.obj;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class OBJLoader
{
	public static OBJData loadOBJ(String filePathIn)
	{
		var file = new File(filePathIn);
		try (var bufferedReader = new BufferedReader(new FileReader(file)))
		{
			String          line;
			final var vertices = new ArrayList<OBJVertex>();
			final var  textures = new ArrayList<Vector2f>();
			final var normals  = new ArrayList<Vector3f>();
			final var   indices  = new ArrayList<Integer>();

			while (true)
			{
				line = bufferedReader.readLine();
				if (line.startsWith("v "))
				{
					String[] currentLine = line.split(" ");
					Vector3f vertex = new Vector3f(Float.valueOf(currentLine[1]), Float.valueOf(currentLine[2]),
					                               Float.valueOf(currentLine[3]));
					OBJVertex newVertex = new OBJVertex(vertices.size(), vertex);
					vertices.add(newVertex);

				}
				else if (line.startsWith("vt "))
				{
					String[] currentLine = line.split(" ");
					Vector2f texture = new Vector2f(Float.valueOf(currentLine[1]), Float.valueOf(currentLine[2]));
					textures.add(texture);
				}
				else if (line.startsWith("vn "))
				{
					String[] currentLine = line.split(" ");
					Vector3f normal = new Vector3f(Float.valueOf(currentLine[1]), Float.valueOf(currentLine[2]),
					                               Float.valueOf(currentLine[3]));
					normals.add(normal);
				}
				else if (line.startsWith("f "))
				{
					break;
				}
			}

			while (line != null && line.startsWith("f "))
			{
				String[] currentLine = line.split(" ");
				String[] vertex1     = currentLine[1].split("/");
				String[] vertex2     = currentLine[2].split("/");
				String[] vertex3     = currentLine[3].split("/");
				processVertex(vertex1, vertices, indices);
				processVertex(vertex2, vertices, indices);
				processVertex(vertex3, vertices, indices);
				line = bufferedReader.readLine();
			}
			bufferedReader.close();

			removeUnusedVertices(vertices);
			float[] verticesArray = new float[vertices.size() * 3];
			float[] texturesArray = new float[vertices.size() * 2];
			float[] normalsArray  = new float[vertices.size() * 3];
			float furthest = convertDataToArrays(vertices, textures, normals, verticesArray, texturesArray,
			                                     normalsArray);
			int[]   indicesArray = convertIndicesListToArray(indices);
			OBJData data         = new OBJData(verticesArray, texturesArray, normalsArray, indicesArray, furthest);
			return data;
		}
		catch (FileNotFoundException eIn)
		{
			System.err.println("Model file not found : \"" + file.getAbsolutePath() + "\" for \"" + filePathIn + "\"");
			throw new RuntimeException(eIn);
		}
		catch (IOException eIn)
		{
			System.err.println("Cannot read model file \"" + file.getAbsolutePath() + "\" for \"" + filePathIn + "\"");
			throw new RuntimeException(eIn);
		}
	}

	private static void processVertex(String[] vertexIn, List<OBJVertex> verticesIn, List<Integer> indicesIn)
	{
		int       index         = Integer.parseInt(vertexIn[0]) - 1;
		OBJVertex currentVertex = verticesIn.get(index);
		int       textureIndex  = Integer.parseInt(vertexIn[1]) - 1;
		int       normalIndex   = Integer.parseInt(vertexIn[2]) - 1;
		if (!currentVertex.isSet())
		{
			currentVertex.textureIndex(textureIndex);
			currentVertex.normalIndex(normalIndex);
			indicesIn.add(index);
		}
		else
		{
			dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indicesIn, verticesIn);
		}
	}

	private static int[] convertIndicesListToArray(List<Integer> indicesIn)
	{
		int[] indicesArray = new int[indicesIn.size()];
		for (int i = 0; i < indicesArray.length; i++)
		{
			indicesArray[i] = indicesIn.get(i);
		}
		return indicesArray;
	}

	private static float convertDataToArrays(List<OBJVertex> verticesIn, List<Vector2f> texturesIn,
	                                         List<Vector3f> normalsIn, float[] verticesArrayIn,
	                                         float[] texturesArrayIn,
	                                         float[] normalsArrayIn)
	{
		float furthestPoint = 0;
		for (int i = 0; i < verticesIn.size(); i++)
		{
			OBJVertex currentVertex = verticesIn.get(i);
			if (currentVertex.length() > furthestPoint)
			{
				furthestPoint = currentVertex.length();
			}
			Vector3f position     = currentVertex.position();
			Vector2f textureCoord = texturesIn.get(currentVertex.textureIndex());
			Vector3f normalVector = normalsIn.get(currentVertex.normalIndex());
			verticesArrayIn[i * 3]     = position.x;
			verticesArrayIn[i * 3 + 1] = position.y;
			verticesArrayIn[i * 3 + 2] = position.z;
			texturesArrayIn[i * 2]     = textureCoord.x;
			texturesArrayIn[i * 2 + 1] = 1 - textureCoord.y;
			normalsArrayIn[i * 3]      = normalVector.x;
			normalsArrayIn[i * 3 + 1]  = normalVector.y;
			normalsArrayIn[i * 3 + 2]  = normalVector.z;
		}
		return furthestPoint;
	}

	private static void dealWithAlreadyProcessedVertex(OBJVertex previousVertexIn, int newTextureIndexIn,
	                                                   int newNormalIndexIn, List<Integer> indicesIn,
	                                                   List<OBJVertex> verticesIn)
	{
		if (previousVertexIn.hasSameTextureAndNormal(newTextureIndexIn, newNormalIndexIn))
		{
			indicesIn.add(previousVertexIn.index());
		}
		else
		{
			OBJVertex anotherVertex = previousVertexIn.duplicateVertex();
			if (anotherVertex != null)
			{
				dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndexIn, newNormalIndexIn, indicesIn,
				                               verticesIn);
			}
			else
			{
				OBJVertex duplicateVertex = new OBJVertex(verticesIn.size(), previousVertexIn.position());
				duplicateVertex.textureIndex(newTextureIndexIn);
				duplicateVertex.normalIndex(newNormalIndexIn);
				previousVertexIn.duplicateVertex(duplicateVertex);
				verticesIn.add(duplicateVertex);
				indicesIn.add(duplicateVertex.index());
			}

		}
	}

	private static void removeUnusedVertices(List<OBJVertex> verticesIn)
	{
		for (OBJVertex vertex : verticesIn)
		{
			if (!vertex.isSet())
			{
				vertex.textureIndex(0);
				vertex.normalIndex(0);
			}
		}
	}
}