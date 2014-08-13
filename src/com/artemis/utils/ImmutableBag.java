package com.artemis.utils;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 
 * @author Arni Arent
 *
 * @param <T> type of the elements this ImmutableBag holds.
 */
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
	 * @return the index of the item, -1 if none of such item was found.
	 */
	int contains ( final T item );

	/**
	 * Check if the bag contains this item applying the criteria supplied..
	 * 
	 * @param criteria
	 *            to be used to find an item.
	 * @return the index of the item that met the criteria, -1 if none of such
	 *         items were found.
	 */
	int contains ( final Predicate<T> criteria );
	
	/**
	 * Iterates over the items of this Bag applying the criteria supplied.
	 * 
	 * @param criteria
	 *            to be used to find an item.
	 * @return the item that met the criteria or null if none of such
	 *         items were found.
	 */
	T find ( final Predicate<T> criteria );
	
	/**
	 * Iterates over each element of this bag and applying the supplied
	 * operation.
	 * 
	 * @param operation
	 *            to be performed on each element of this bag.
	 */
	void forEach ( final Consumer<T> operation );

	/**
	 * Returns the item at the specified position in Bag.
	 * 
	 * @param index
	 *            of the item to return
	 * @return item at the specified position in bag
	 */
	T get ( int index );

	/**
	 * Returns the item at the specified position in Bag.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param index
	 *            of the item to return
	 * @return item at the specified position in bag
	 */
	T getUnsafe ( final int index );

	/**
	 * Provides a SIZED, SUBSIZED and IMMUTABLE sequential Stream over this bag.
	 *
	 * @return Stream representing this bag.
	 */
	Stream<T> stream ();

	/**
	 * Provides a SIZED, SUBSIZED and IMMUTABLE parallel Stream over this bag.
	 *
	 * @return Stream representing this bag.
	 */
	Stream<T> parallelStream ();
}
