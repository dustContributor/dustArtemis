package com.artemis.utils;

import java.util.function.Predicate;

/**
 * BoundedBag retains elements by their indices. It tries to save up space by
 * offset the indices by the element with the lowest index in the bag. That
 * way if you only have elements, say, from index 500 to 700, you save up those
 * first 500 slots that wouldn't being used.
 * 
 * @author dustContributor
 *
 * @param <T>
 *            type of elements this BoundedBag will hold.
 */
public final class BoundedBag<T> extends ImmutableBag<T>
{
	private int offset;

	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@value #DEFAULT_CAPACITY}. The backing array type will be Object.
	 * 
	 */
	public BoundedBag ()
	{
		super();
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
	public BoundedBag ( final int capacity )
	{
		super( capacity );
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
		super( type );
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
		super( type, capacity );
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
	public void add ( final int index, final T item )
	{
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

	public T[] data ()
	{
		return this.data;
	}

	public int offset ()
	{
		return offset;
	}

	@Override
	public int contains ( final T item )
	{
		final int i = super.contains( item );
		return ( i < 0 ) ? i : i + offset;
	}

	@Override
	public int contains ( final Predicate<T> criteria )
	{
		final int i = super.contains( criteria );
		return ( i < 0 ) ? i : i + offset;
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
	private void ensureCapacityDirect ( final int index )
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

	private void shiftLeft ( final int shOffset )
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

	private void shiftRight ( final int shOffset )
	{
		T[] array = data;

		System.arraycopy( array, 0, array, shOffset, size );

		for ( int i = shOffset; i-- > 0; )
		{
			array[i] = null;
		}
	}

	private int nextNonNull ( final int start )
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

	private int prevNonNull ( final int start )
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
