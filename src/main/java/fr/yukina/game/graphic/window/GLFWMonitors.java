package fr.yukina.game.graphic.window;

import java.util.Stack;
import java.util.TreeMap;

import static org.lwjgl.glfw.GLFW.glfwGetMonitors;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;

public class GLFWMonitors
{
	GLFWMonitors()
	{

	}

	public final Stack<Long> sortLargestMonitors(boolean primaryFirstIn, int numberIn)
	{
		var monitorsMap = new TreeMap<Double, Long>();
		if (primaryFirstIn)
		{
			monitorsMap.put(Double.POSITIVE_INFINITY, glfwGetPrimaryMonitor());
		}
		var width  = new int[1];
		var height = new int[1];

		var monitorsList = glfwGetMonitors();
		for (int i = 0; i < monitorsList.capacity(); i++)
		{
			var monitor = monitorsList.get(i);
			var space   = width[0] * height[0];
			monitorsMap.put((double) space, monitor);
		}

		var monitorsStack = new Stack<Long>();
		for (var entry : monitorsMap.descendingMap().entrySet())
		{
			if (numberIn >= 0 && monitorsStack.size() >= numberIn)
			{
				monitorsMap.clear();
				break;
			}
			monitorsStack.addFirst(entry.getValue());
		}

		monitorsMap.clear();

		while (monitorsStack.size() < numberIn)
		{
			monitorsStack.push(glfwGetPrimaryMonitor());
		}

		return monitorsStack;
	}
}