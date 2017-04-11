package com.artemis.utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.artemis.DAConstants;

/**
 * 
 * @author Arni Arent
 * @author dustContributor
 *
 * @param <T> type of the elements it holds.
 */
public abstract class ImmutableBag<T> implements Iterable<T>
{
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
	 * <p>
	 * <b>NOTE</b>: If capacity is less than {@value #MINIMUM_WORKING_CAPACITY},
	 * the Bag will be created with a capacity of
	 * {@value #MINIMUM_WORKING_CAPACITY} instead. The backing array type will be
	 * Object.
	 * </p>
	 * 
	 * @param capacity of the Bag
	 */
	@SuppressWarnings( "unchecked" )
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
	 * <p>
	 * <b>NOTE</b>: If capacity is less than {@value #MINIMUM_WORKING_CAPACITY},
	 * the Bag will be created with a capacity of
	 * {@value #MINIMUM_WORKING_CAPACITY} instead. Uses Array.newInstance() to
	 * instantiate a backing array of the proper type.
	 * </p>
	 * 
	 * @param type of the backing array.
	 * 
	 * @param capacity of the Bag.
	 */
	@SuppressWarnings( "unchecked" )
	public ImmutableBag ( final Class<T> type, final int capacity )
	{
		this( (T[]) Array.newInstance( type, fixInitialCapacity( capacity ) ) );
	}

	/**
	 * Constructs an empty Bag with the defined data array as backing storage.
	 * 
	 * <p>
	 * <b>NOTE</b>: Wont do any length/null checks on the passed array.
	 * </p>
	 * 
	 * @param data array to use as backing storage.
	 */
	public ImmutableBag ( final T[] data )
	{
		this.data = data;
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
		final int size = this.size;
		final T[] data = this.data;

		for ( int i = 0; i < size; ++i )
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
		Objects.requireNonNull( criteria, "criteria" );

		final int size = this.size;
		final T[] data = this.data;

		for ( int i = 0; i < size; ++i )
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
	 * Iterates over each element of this bag and applying the supplied operation
	 * on each element.
	 * 
	 * @param operation to be performed on each element of this bag.
	 */
	@Override
	public void forEach ( final Consumer<? super T> operation )
	{
		Objects.requireNonNull( operation, "operation" );

		final int size = this.size;
		final T[] data = this.data;

		for ( int i = 0; i < size; ++i )
		{
			operation.accept( data[i] );
		}
	}

	/**
	 * Iterates over each element of this bag and applying the supplied operation
	 * on each element while passing the index of the element being operated on.
	 * 
	 * @param operation to be performed on each element of this bag.
	 */
	public void forEach ( final ObjIntConsumer<T> operation )
	{
		Objects.requireNonNull( operation, "operation" );

		final int size = this.size;
		final T[] data = this.data;

		for ( int i = 0; i < size; ++i )
		{
			operation.accept( data[i], i );
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
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param index of the item to return
	 * @return item at the specified position in bag
	 */
	public T getUnsafe ( final int index )
	{
		return data[index];
	}

	/**
	 * Checks if the index is within the capacity of the Bag (ie, if its bigger or
	 * equal than 0 and less than the length of the backing array).
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
	 * Provides a {@link Spliterator#SIZED}, {@link Spliterator#SUBSIZED},
	 * {@link Spliterator#ORDERED} and {@link Spliterator#IMMUTABLE}
	 * {@link java.util.stream.Stream} over this bag.
	 *
	 * @return {@link Stream} representing this bag.
	 */
	public Stream<T> stream ()
	{
		return StreamSupport.stream( Arrays.spliterator( data, 0, size ), false );
	}

	/**
	 * Provides a {@link Spliterator#SIZED}, {@link Spliterator#SUBSIZED},
	 * {@link Spliterator#ORDERED} and {@link Spliterator#IMMUTABLE}
	 * <b>parallel</b> {@link java.util.stream.Stream} over this bag.
	 *
	 * @return parallel {@link Stream} representing this bag.
	 */
	public Stream<T> parallelStream ()
	{
		return StreamSupport.stream( Arrays.spliterator( data, 0, size ), true );
	}

	/**
	 * Returns an iterator over this bag. Doesn't supports modification of the bag
	 * while in use.
	 */
	@Override
	public final Iterator<T> iterator ()
	{
		return new ArrayIterator<>( this.data, this.size );
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

	/**
	 * Ensures the capacity parameter is bigger than
	 * {@link ImmutableBag#MINIMUM_WORKING_CAPACITY}.
	 * 
	 * @param capacity to check.
	 * @return capacity if its valid,
	 *         {@link ImmutableBag#MINIMUM_WORKING_CAPACITY} otherwise.
	 */
	public static final int fixInitialCapacity ( final int capacity )
	{
		return (capacity > MINIMUM_WORKING_CAPACITY) ? capacity : MINIMUM_WORKING_CAPACITY;
	}

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
	public static final int getCapacityFor ( final int index, final int arrayLength )
	{
		int newSize = arrayLength;

		while ( index >= newSize )
		{
			newSize = nextCapacity( newSize );
		}

		return newSize;
	}

	/**
	 * Fills the passed array with the passed value up to the specified limit.
	 * Doesn't does bound checks.
	 * 
	 * @param value to set in the array.
	 * @param dest array to fill with the value.
	 * @param limit up to which the value will be set in the array.
	 * @return the passed array.
	 */
	protected static final <T> T[] fillWith ( final T value, final T[] dest, final int limit )
	{
		// Sets all elements to the specified value.
		dest[0] = value;

		for ( int i = 1; i < limit; i += i )
		{
			System.arraycopy( dest, 0, dest, i, Math.min( limit - i, i ) );
		}
		return dest;
	}

}
