package com.github.siralpega.util.db;

public interface Callback<T>
{
	public void execute(T response);
}
