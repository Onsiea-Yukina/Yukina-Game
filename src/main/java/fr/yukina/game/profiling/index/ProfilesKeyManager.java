package fr.yukina.game.profiling.index;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ProfilesKeyManager
{
	private final Map<String, Integer> indexMap;

	public ProfilesKeyManager()
	{
		this.indexMap = new ConcurrentHashMap<>();
	}

	public final String key(boolean isIndexedIn, String nameIn)
	{
		if (!isIndexedIn)
		{
			return nameIn;
		}

		Integer index = this.indexMap.get(nameIn);
		if (index == null)
		{
			index = this.indexMap.size() + 1;
			this.indexMap.put(nameIn, index);
		}

		return index.toString();
	}

	public void remove(String nameIn)
	{
		this.indexMap.remove(nameIn);
	}

	public final void clear()
	{
		this.indexMap.clear();
	}
}