package com.artemis.utils;

import java.lang.reflect.Array;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.artemis.DAConstants;

/**
 * 
 * @author Arni Arent
 *
 * @param <T> type of the elements this ImmutableBag holds.
 */
public abstract class ImmutableBag<T>
{
	public final Class<T> type;

	protected T[] data;
	protected int size;

	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@value #DEFAULT_CAPACITY}. The backing array type will be Object.
	 * 
	 */
	public ImmutableBag ()
	{
		this( DEFAULT_CAPACITY );
	}

	/**
	 * Constructs an empty Bag with the specified initial capacity.
	 * 
	 * <p> NOTE: If capacity is less than {@value #MINIMUM_WORKING_CAPACITY},
	 * the Bag will be created with a capacity of
	 * {@value #MINIMUM_WORKING_CAPACITY} instead. The backing array type will
	 * be Object. </p>
	 * 
	 * @param capacity of the Bag
	 */
	@SuppressWarnings ( "unchecked" )
	public ImmutableBag ( final int capacity )
	{
		this( (Class<T>) Object.class, capacity );
	}

	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@value #DEFAULT_CAPACITY}. Uses Array.newInstance() to instantiate a
	 * backing array of the proper type.
	 * 
	 * @param type of the backing array.
	 */
	public ImmutableBag ( final Class<T> type )
	{
		this( type, DEFAULT_CAPACITY );
	}

	/**
	 * Constructs an empty Bag with the defined initial capacity.
	 * 
	 * <p> NOTE: If capacity is less than {@value #MINIMUM_WORKING_CAPACITY},
	 * the Bag will be created with a capacity of
	 * {@value #MINIMUM_WORKING_CAPACITY} instead. Uses Array.newInstance() to
	 * instantiate a backing array of the proper type. </p>
	 * 
	 * @param type of the backing array.
	 * 
	 * @param capacity of the Bag.
	 */
	@SuppressWarnings ( "unchecked" )
	public ImmutableBag ( final Class<T> type, final int capacity )
	{
		final int newCap = (capacity > MINIMUM_WORKING_CAPACITY) ? capacity
				: MINIMUM_WORKING_CAPACITY;
		this.data = (T[]) Array.newInstance( type, newCap );
		this.type = type;
	}

	/**
	 * Returns the number of items in this bag.
	 * 
	 * @return number of items in this bag.
	 */
	public int size ()
	{
		return size;
	}

	/**
	 * Returns the number of items the bag can hold without growing.
	 * 
	 * @return number of items the bag can hold without growing.
	 */
	public int capacity ()
	{
		return data.length;
	}

	/**
	 * Returns true if this list contains no items.
	 * 
	 * @return true if this list contains no items.
	 */
	public boolean isEmpty ()
	{
		return size < 1;
	}

	/**
	 * Check if the bag contains this item. Uses '==' to compare items.
	 * 
	 * @param item to check if its contained in the bag.
	 * @return the index of the item, -1 if none of such item was found.
	 */
	public int contains ( final T item )
	{
		final int iSize = size;

		for ( int i = 0; i < iSize; ++i )
		{
			if ( data[i] == item )
			{
				// Item found. Return its index.
				return i;
			}
		}

		// Item not found.
		return -1;
	}

	/**
	 * Check if the bag contains this item applying the criteria supplied..
	 * 
	 * @param criteria to be used to find an item.
	 * @return the index of the item that met the criteria, -1 if none of such
	 *         items were found.
	 */
	public int contains ( final Predicate<T> criteria )
	{
		final int iSize = size;

		for ( int i = 0; i < iSize; ++i )
		{
			if ( criteria.test( data[i] ) )
			{
				// Item found. Return its index.
				return i;
			}
		}

		// Item not found.
		return -1;
	}

	/**
	 * Iterates over the items of this Bag applying the criteria supplied.
	 * 
	 * @param criteria to be used to find an item.
	 * @return the item that met the criteria or null if none of such items were
	 *         found.
	 */
	public T find ( final Predicate<T> criteria )
	{
		final int index = contains( criteria );

		if ( index > -1 )
		{
			return data[index];
		}

		return null;
	}

	/**
	 * Iterates over each element of this bag and applying the supplied
	 * operation.
	 * 
	 * @param operation to be performed on each element of this bag.
	 */
	public void forEach ( final Consumer<T> operation )
	{
		final int iSize = size;

		for ( int i = 0; i < iSize; ++i )
		{
			operation.accept( data[i] );
		}
	}

	/**
	 * Returns the item at the specified position in Bag.
	 * 
	 * @param index of the item to return
	 * @return item at the specified position in bag
	 */
	public T get ( final int index )
	{
		if ( isInBounds( index ) )
		{
			return data[index];
		}

		return null;
	}

	/**
	 * Returns the item at the specified position in Bag.
	 * 
	 * <p> <b>UNSAFE: Avoids doing any bounds check.</b> </p>
	 * 
	 * @param index of the item to return
	 * @return item at the specified position in bag
	 */
	public T getUnsafe ( final int index )
	{
		return data[index];
	}

	/**
	 * Checks if the index is within the capacity of the Bag (ie, if its bigger
	 * or equal than 0 and less than the length of the backing array).
	 * 
	 * @param index that needs to be checked.
	 * @return <code>true</code> if the index is within the bounds of the Bag,
	 *         <code>false</code> otherwise.
	 */
	protected boolean isInBounds ( final int index )
	{
		return (index > -1 && index < data.length);
	}

	/**
	 * Checks if the index is within the size of the Bag (ie, if its bigger or
	 * equal than 0 and less than the size of the bag).
	 * 
	 * @param index that needs to be checked.
	 * @return <code>true</code> if the index is within the size of the Bag,
	 *         <code>false</code> otherwise.
	 */
	protected boolean isInSize ( final int index )
	{
		return (index > -1 && index < size);
	}

	/**
	 * Provides a SIZED, SUBSIZED and IMMUTABLE sequential Stream over this bag.
	 *
	 * @return Stream representing this bag.
	 */
	public Stream<T> stream ()
	{
		final Spliterator<T> split = Spliterators.spliterator( data, Spliterator.IMMUTABLE );

		return StreamSupport.stream( split, false ).limit( size );
	}

	/**
	 * Provides a SIZED, SUBSIZED and IMMUTABLE parallel Stream over this bag.
	 *
	 * @return Stream representing this bag.
	 */
	public Stream<T> parallelStream ()
	{
		final Spliterator<T> split = Spliterators.spliterator( data, Spliterator.IMMUTABLE );

		return StreamSupport.stream( split, true ).limit( size );
	}

	/**
	 * This value is fetched from
	 * {@link com.artemis.DAConstants#BAG_DEFAULT_CAPACITY}
	 */
	public static final int DEFAULT_CAPACITY = DAConstants.BAG_DEFAULT_CAPACITY;
	/**
	 * This value is fetched from
	 * {@link com.artemis.DAConstants#BAG_GROW_RATE_THRESHOLD}
	 */
	public static final int GROW_RATE_THRESHOLD = DAConstants.BAG_GROW_RATE_THRESHOLD;

	/** Non-configurable value. */
	public static final int MINIMUM_WORKING_CAPACITY = 4;

	protected static final int nextCapacity ( final int dataLength )
	{
		if ( dataLength < GROW_RATE_THRESHOLD )
		{
			// Exponential 2 growth.
			return (dataLength << 1);
		}

		return dataLength + (dataLength >> 1);
	}

	/**
	 * It finds the next capacity according to the grow strategy that could
	 * contain the supplied index.
	 * 
	 * @param index that needs to be contained.
	 * @param arrayLength of the current backing array.
	 * @return proper capacity given by the grow strategy.
	 */
	protected static final int getCapacityFor ( final int index, final int arrayLength )
	{
		int newSize = arrayLength;

		while ( index >= newSize )
		{
			newSize = nextCapacity( newSize );
		}

		return newSize;
	}

}
