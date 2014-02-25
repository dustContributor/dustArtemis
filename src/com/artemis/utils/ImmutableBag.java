package com.artemis.utils;

import java.util.stream.Stream;

public interface ImmutableBag<T>
{
	/**
	 * Returns the number of items in this bag.
	 * 
	 * @return number of items in this bag.
	 */
	int size ();
	
	/**
	 * Returns the number of items the bag can hold without growing.
	 * 
	 * @return number of items the bag can hold without growing.
	 */
	int capacity ();
	
	/**
	 * Returns true if this list contains no items.
	 * 
	 * @return true if this list contains no items.
	 */
	boolean isEmpty ();
	
	/**
	 * Check if the bag contains this item.
	 * 
	 * @param item
	 * @return -1 if the item isn't contained in the bag. Otherwise
	 * 			it returns its index.
	 */
	int contains ( final T item );
	
	/**
	 * Returns the item at the specified position in Bag.
	 * 
	 * @param index of the item to return
	 * @return item at the specified position in bag
	 */
	T get ( int index );
	
	/**
	 * Returns the item at the specified position in Bag.
	 * 
	 * @param index of the item to return
	 * @return item at the specified position in bag
	 */
	T getUnsafe ( final int index );
	
	Stream<T> stream();
	Stream<T> parallelStream();
}
