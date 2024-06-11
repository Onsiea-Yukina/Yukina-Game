package fr.yukina.game.labs;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CirclePoints
{

	public static class Point
	{
		int x, y;

		public Point(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString()
		{
			return x + "," + y;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null || getClass() != obj.getClass())
			{
				return false;
			}
			Point point = (Point) obj;
			return x == point.x && y == point.y;
		}

		@Override
		public int hashCode()
		{
			return Integer.hashCode(x) * 31 + Integer.hashCode(y);
		}
	}

	public static void main(String[] args)
	{
		double minRenderDistance  = 2.0;
		double maxRenderDistance  = 3.0;
		double renderDistanceStep = 1.0;
		double minChunkSize       = 1.0;
		double maxChunkSize       = 1.0;
		double chunkSizeStep      = 1.0;

		try (FileWriter writer = new FileWriter("P:/results.csv"))
		{
			writer.write("render_distance,chunk_size,x,y,type\n");

			for (double renderDistance = minRenderDistance;
			     renderDistance <= maxRenderDistance; renderDistance += renderDistanceStep)
			{
				for (double chunkSize = minChunkSize; chunkSize <= maxChunkSize; chunkSize += chunkSizeStep)
				{
					Set<Point> circleResult = calculatePointsInCircle(renderDistance);
					Set<Point> chunks       = calculateChunks(renderDistance, chunkSize);

					writePointsToCSV(writer, circleResult, renderDistance, chunkSize, "circle");
					writePointsToCSV(writer, chunks, renderDistance, chunkSize, "chunk");
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Set<Point> calculatePointsInCircle(double renderDistance)
	{
		Set<Point> points = new HashSet<>();
		for (int r = 0; r <= renderDistance; r++)
		{
			for (double angle = 0; angle < 2 * Math.PI; angle += 1.0 / r)
			{
				int x = (int) Math.round(r * Math.cos(angle));
				int y = (int) Math.round(r * Math.sin(angle));
				points.add(new Point(x, y));
			}
		}
		return points;
	}

	public static Set<Point> calculateChunks(double renderDistance, double chunkSize)
	{
		Set<Point> chunks            = new HashSet<>();
		int        renderDistanceInt = (int) renderDistance;

		for (int x = -renderDistanceInt; x <= renderDistanceInt; x++)
		{
			for (int z = -renderDistanceInt; z <= renderDistanceInt; z++)
			{
				double dist = Math.sqrt(x * x + z * z);
				if (dist <= renderDistance)
				{
					chunks.add(new Point(x, z));
				}
			}
		}

		return chunks;
	}

	public static void writePointsToCSV(FileWriter writer, Set<Point> points, double renderDistance, double chunkSize,
	                                    String type) throws IOException
	{
		for (Point point : points)
		{
			writer.write(renderDistance + "," + chunkSize + "," + point.x + "," + point.y + "," + type + "\n");
		}
	}
}
