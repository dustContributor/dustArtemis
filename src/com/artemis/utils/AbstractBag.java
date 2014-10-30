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
	 * Resizes the Bag so it can contain the index provided.
	 * 
	 * @param index that is expected the Bag can contain.
	 */
	public void ensureCapacity ( final int index )
	{
		if ( index >= data.length )
		{
			resize( getCapacityFor( index ) );
		}
	}

	/**
	 * It finds the next capacity according to the grow strategy that could
	 * contain the supplied index.
	 * 
	 * @param index that needs to be contained.
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
		// Nulls all items.
		Arrays.fill( data, null );
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
