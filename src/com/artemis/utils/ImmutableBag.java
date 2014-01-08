package com.artemis.utils;

public interface ImmutableBag<T>
{
	int size ();
	
	boolean isEmpty ();
	boolean contains ( final T item );
	
	T get ( final int index );
}
