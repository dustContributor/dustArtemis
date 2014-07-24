package com.artemis.utils;

import java.lang.reflect.Array;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BoundedBag<T> implements ImmutableBag<T>
{
	private T[] data;
	private int size;
	private int offset;

	public final Class<T> type;

	public static final int DEFAULT_CAPACITY = 16;
	public static final int MINIMUM_WORKING_CAPACITY = 4;
	public static final int GROW_RATE_THRESHOLD = 2048;

	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@value #DEFAULT_CAPACITY}. The backing array type will be Object.
	 * 
	 */
	public BoundedBag ()
	{
		this( DEFAULT_CAPACITY );
	}

	/**
	 * Constructs an empty Bag with the specified initial capacity.
	 * 
	 * <p>
	 * NOTE: If capacity is less than {@value #MINIMUM_WORKING_CAPACITY}, the
	 * Bag will be created with a capacity of {@value #MINIMUM_WORKING_CAPACITY}
	 * instead. The backing array type will be Object.
	 * </p>
	 * 
	 * @param capacity
	 *            of the Bag
	 */
	@SuppressWarnings ( "unchecked" )
	public BoundedBag ( final int capacity )
	{
		this( (Class<T>) Object.class, capacity );
	}

	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@value #DEFAULT_CAPACITY}. Uses Array.newInstance() to instantiate a
	 * backing array of the proper type.
	 * 
	 * @param type
	 *            of the backing array.
	 */
	public BoundedBag ( final Class<T> type )
	{
		this( type, DEFAULT_CAPACITY );
	}

	/**
	 * Constructs an empty Bag with the defined initial capacity.
	 * 
	 * <p>
	 * NOTE: If capacity is less than {@value #MINIMUM_WORKING_CAPACITY}, the
	 * Bag will be created with a capacity of {@value #MINIMUM_WORKING_CAPACITY}
	 * instead. Uses Array.newInstance() to instantiate a backing array of the
	 * proper type.
	 * </p>
	 * 
	 * @param type
	 *            of the backing array.
	 * 
	 * @param capacity
	 *            of the Bag.
	 */
	public BoundedBag ( final Class<T> type, final int capacity )
	{
		this.data = newArray( type, (capacity > MINIMUM_WORKING_CAPACITY) ? capacity
				: MINIMUM_WORKING_CAPACITY );
		this.type = type;
	}

	/**
	 * Add item at specified index in the bag.
	 * 
	 * It will increase the size of the bag as required.
	 * 
	 * @param index
	 *            of item
	 * @param item
	 *            to be set.
	 */
	public void add ( int index, final T item )
	{
		// Just use the index, don't check for negative.
		index = Math.abs( index );
		// If its the first element to be added.
		if ( isEmpty() )
		{
			offset = index;
			data[0] = item;
			size = 1;
			return;
		}
		
		int diff = index - offset;
		// If the element has a lower index than the offset.
		if ( diff < 0 )
		{
			diff = -diff;
			int newSize = size + diff;
			ensureCapacityDirect( newSize );
			shiftRight( diff );
			size = newSize;
			offset = index;
			data[0] = item;
			return;
		}
		// Element is in the middle of the array.
		ensureCapacityDirect( diff );
		data[diff] = item;
		size = Math.max( size, diff + 1 );
	}

	/**
	 * Removes the item at the specified position in this Bag.
	 * 
	 * @param index
	 *            the index of item to be removed
	 */
	public void remove ( final int index )
	{
		if ( isInBounds( index - offset ) )
		{
			removeUnsafe( index );
		}
	}

	/**
	 * Removes the item at the specified position in this Bag.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param index
	 *            the index of item to be removed
	 */
	public void removeUnsafe ( final int index )
	{
		int diff = index - offset;
		data[diff] = null;

		// If its the first one.
		if ( diff == 0 )
		{
			int nextNonNull = nextNonNull( 1 );
			// Its the last one.
			if ( nextNonNull < 0 )
			{
				offset = 0;
				size = 0;
				return;
			}
			// It isn't the last one.
			shiftLeft( nextNonNull );
			offset += nextNonNull;
			size -= nextNonNull;
			return;
		}
		// If its the last one.
		if ( diff == (size - 1) )
		{
			size = prevNonNull( diff ) + 1;
		}
	}

//	private void shrink ()
//	{
//		final int cap = Math.max( nextCapacity( size ), MINIMUM_WORKING_CAPACITY );
//
//		if ( cap < data.length )
//		{
//			grow( cap );
//		}
//	}

	@Override
	public T get ( int index )
	{
		index -= offset;
		if ( isInBounds( index ) )
		{
			return data[index];
		}

		return null;
	}

	@Override
	public T getUnsafe ( int index )
	{
		return data[index - offset];
	}

	@Override
	public int size ()
	{
		return size;
	}

	@Override
	public int capacity ()
	{
		return data.length;
	}

	public T[] data ()
	{
		return this.data;
	}

	public int offset ()
	{
		return offset;
	}

	@Override
	public boolean isEmpty ()
	{
		return size < 1;
	}

	@Override
	public int contains ( final T item )
	{
		final int iSize = size;

		for ( int i = 0; i < iSize; ++i )
		{
			if ( data[i] == item )
			{
				// Item found. Return its index.
				return i + offset;
			}
		}

		// Item not found.
		return -1;
	}

	@Override
	public int contains ( final Predicate<T> criteria )
	{
		final int iSize = size;

		for ( int i = 0; i < iSize; ++i )
		{
			if ( criteria.test( data[i] ) )
			{
				// Item found. Return its index.
				return i + offset;
			}
		}

		// Item not found.
		return -1;
	}

	@Override
	public T find ( final Predicate<T> criteria )
	{
		return get( contains( criteria ) );
	}

	@Override
	public void forEach ( final Consumer<T> operation )
	{
		final int iSize = size;

		for ( int i = 0; i < iSize; ++i )
		{
			operation.accept( data[i] );
		}
	}

	private void grow ( final int newCapacity )
	{
		final T[] newArray = newArray( type, newCapacity );
		System.arraycopy( data, 0, newArray, 0, size );
		data = newArray;
	}

	/**
	 * Resizes the Bag so it can contain the index provided.
	 * 
	 * @param index
	 *            that is expected the Bag can contain.
	 */
	public void ensureCapacity ( int index )
	{
		ensureCapacityDirect( index - offset );
	}

	/**
	 * Resizes the Bag so it can contain the index provided without applying an
	 * offset to it.
	 * 
	 * @param index
	 *            that is expected the Bag can contain.
	 */
	private void ensureCapacityDirect ( int index )
	{
		if ( index >= data.length )
		{
			grow( getCapacityFor( index ) );
		}
	}

	/**
	 * It finds the next capacity according to the grow strategy that could
	 * contain the supplied index.
	 * 
	 * @param index
	 *            that needs to be contained.
	 * @return proper capacity given by the grow strategy.
	 */
	private int getCapacityFor ( final int index )
	{
		int newSize = nextCapacity( data.length );

		while ( index >= newSize )
		{
			newSize = nextCapacity( newSize );
		}

		return newSize;
	}

	private static final int nextCapacity ( final int dataLength )
	{
		if ( dataLength < GROW_RATE_THRESHOLD )
		{
			// Exponential 2 growth.
			return (dataLength << 1);
		}

		return dataLength + (dataLength >> 1);
	}

	/**
	 * Removes all the items from this bag.
	 */
	public void clear ()
	{
		/*
		 * Fastest way to fill an array of reference types that I know of.
		 * 
		 * It isn't recommended for primitive types though. See IntStack
		 * grow(newCapacity) method.
		 */

		// Nulls all items.
		data[0] = null;
		for ( int i = 1; i < data.length; i += i )
		{
			System.arraycopy( data, 0, data, i, ((data.length - i) < i) ? (data.length - i) : i );
		}
		size = 0;
		offset = 0;
	}

	/**
	 * Checks if the index is within the capacity of the Bag (ie, if its bigger
	 * or equal than 0 and less than the length of the backing array).
	 * 
	 * @param index
	 *            that needs to be checked.
	 * @return <code>true</code> if the index is within the bounds of the Bag,
	 *         <code>false</code> otherwise.
	 */
	private boolean isInBounds ( int index )
	{
		return (index > -1 && index < data.length);
	}

	private void shiftLeft ( int shOffset )
	{
		T[] array = data;
		int cSize = size;
		int nLim = cSize - shOffset;

		System.arraycopy( array, shOffset, array, 0, nLim );

		for ( int i = nLim; i < cSize; ++i )
		{
			array[i] = null;
		}
	}

	private void shiftRight ( int shOffset )
	{
		T[] array = data;

		System.arraycopy( array, 0, array, shOffset, size );

		for ( int i = shOffset; i-- > 0; )
		{
			array[i] = null;
		}
	}

	private int nextNonNull ( int start )
	{
		T[] array = data;
		int cSize = size;

		for ( int i = start; i < cSize; ++i )
		{
			if ( array[i] != null )
			{
				return i;
			}
		}

		return -1;
	}

	private int prevNonNull ( int start )
	{
		T[] array = data;

		for ( int i = start; i-- > 0; )
		{
			if ( array[i] != null )
			{
				return i;
			}
		}

		return -1;
	}

	@SuppressWarnings ( "unchecked" )
	private static final <T> T[] newArray ( Class<T> type, int capacity )
	{
		return (T[]) Array.newInstance( type, capacity );
	}

	@Override
	public Stream<T> stream ()
	{
		final Spliterator<T> split = Spliterators.spliterator( data, Spliterator.IMMUTABLE );

		return StreamSupport.stream( split, false ).limit( size );
	}

	@Override
	public Stream<T> parallelStream ()
	{
		final Spliterator<T> split = Spliterators.spliterator( data, Spliterator.IMMUTABLE );

		return StreamSupport.stream( split, true ).limit( size );
	}

	@Override
	public String toString ()
	{
		final String newLine = System.lineSeparator();
		final StringBuilder str = new StringBuilder( data.length * 10 );

		str.append( super.toString() ).append( newLine );
		str.append( "Capacity " ).append( this.capacity() ).append( newLine );
		str.append( "Size " ).append( this.size ).append( newLine );
		str.append( "Offset " ).append( this.offset );

		for ( int i = 0; i < size; ++i )
		{
			str.append( newLine );
			str.append( data[i] );
		}

		return str.toString();
	}
}
