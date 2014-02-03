package com.artemis.utils;

import java.util.stream.Stream;

public interface ImmutableBag<T>
{
	int size ();
	
	boolean isEmpty ();
	boolean contains ( final T item );
	
	T get ( final int index );
	
	Stream<T> stream();
	Stream<T> parallelStream();
}
