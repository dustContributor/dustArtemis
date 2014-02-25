package com.artemis.utils;

import java.util.function.Predicate;
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
	 * Check if the bag contains this item. Uses '==' to compare items.
	 * 
	 * @param item
	 * @return -1 if the item isn't contained in the bag. Otherwise
	 * 			it returns its index.
	 */
	int contains ( final T item );
	
	/**
	 * Iterates over the items of this Bag applying the criteria
	 * supplied.
	 * 
	 * @param criteria to be used to find an item.
	 * @return the index of the item that met the criteria, -1 if none of
	 *         such items were found.
	 */
	int find ( final Predicate<T> criteria );
	
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
