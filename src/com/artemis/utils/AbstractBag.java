package com.artemis.utils;

import java.util.Arrays;

class AbstractBag<T> extends ImmutableBag<T>
{
	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@value #DEFAULT_CAPACITY}. The backing array type will be Object.
	 * 
	 */
	public AbstractBag ()
	{
		super();
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
	public AbstractBag ( final int capacity )
	{
		super( capacity );
	}

	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@value #DEFAULT_CAPACITY}. Uses Array.newInstance() to instantiate a
	 * backing array of the proper type.
	 * 
	 * @param type of the backing array.
	 */
	public AbstractBag ( final Class<T> type )
	{
		super( type );
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
	public AbstractBag ( final Class<T> type, final int capacity )
	{
		super( type, capacity );
	}

	private void resize ( final int newCapacity )
	{
		data = Arrays.copyOf( data, newCapacity );
	}

	/**
	 * Shrinks the capacity of this Bag to the next capacity that could hold a
	 * Bag of this size.
	 * 
	 * If the capacities match, then it leaves the Bag as it is.
	 */
	public void shrink ()
	{
		final int nextCap = nextCapacity( size );
		final int newCap = Math.max( nextCap, MINIMUM_WORKING_CAPACITY );

		if ( newCap < data.length )
		{
			resize( newCap );
		}
	}

	/**
	 * Resizes the backing array so its length matches the current size of the
	 * bag.
	 */
	public void trim ()
	{
		resize( size );
	}

	/**
	 * Resizes the Bag so it can contain the index provided.
	 * 
	 * @param index that is expected the Bag can contain.
	 */
	public void ensureCapacity ( final int index )
	{
		final int dataLen = data.length;

		if ( index >= dataLen )
		{
			resize( getCapacityFor( index, dataLen ) );
		}
	}

	/**
	 * Removes all the items from this bag.
	 */
	public void clear ()
	{
		// Nulls all items.
		final T[] dst = data;
		final int limit = dst.length;

		dst[0] = null;
		for ( int i = 1; i < limit; i += i )
		{
			System.arraycopy( dst, 0, dst, i, ((limit - i) < i) ? (limit - i) : i );
		}
		size = 0;
	}

	public T[] data ()
	{
		return this.data;
	}

	public void setSize ( final int size )
	{
		this.size = size;
	}
}
