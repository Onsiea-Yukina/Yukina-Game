package fr.yukina.game.utils.thread;

public interface IOIFunction<O, I>
{
	O process(I inputIn);
}