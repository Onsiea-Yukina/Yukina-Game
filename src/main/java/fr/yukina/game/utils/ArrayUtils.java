package fr.yukina.game.utils;

import java.util.List;

public class ArrayUtils
{
	public static int[] listIntToArray(List<Integer> list)
	{
		int[] result = list.stream().mapToInt((Integer v) -> v).toArray();
		return result;
	}

	public static float[] listToArray(List<Float> listIn)
	{
		int     size     = listIn != null ? listIn.size() : 0;
		float[] floatArr = new float[size];
		for (int i = 0; i < size; i++)
		{
			floatArr[i] = listIn.get(i);
		}
		return floatArr;
	}
}
