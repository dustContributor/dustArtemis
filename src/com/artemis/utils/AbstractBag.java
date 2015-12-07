package com.artemis.utils;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * 
 * @author dustContributor
 *
 * @param <T> type of the elements it holds.
 */
abstract class AbstractBag<T> extends ImmutableBag<T>
{
	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@link ImmutableBag#DEFAULT_CAPACITY}. The backing array type will be
	 * Object.
	 * 
	 */
	public AbstractBag ()
	{
		super();
	}

	/**
	 * Constructs an empty Bag with the specified initial capacity.
	 * 
	 * <p>
	 * NOTE: If capacity is less than
	 * {@link ImmutableBag#MINIMUM_WORKING_CAPACITY}, the Bag will be created
	 * with a capacity of {@link ImmutableBag#MINIMUM_WORKING_CAPACITY} instead.
	 * The backing array type will be Object.
	 * </p>
	 * 
	 * @param capacity of the Bag
	 */
	public AbstractBag ( final int capacity )
	{
		super( capacity );
	}

	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@link ImmutableBag#DEFAULT_CAPACITY}. Uses {@link Array#newInstance } to
	 * instantiate a backing array of the proper type.
	 * 
	 * @param type of the backing array.
	 */
	public AbstractBag ( final Class<T> type )
	{
		super( type );
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
	public AbstractBag ( final T[] data )
	{
		super( data );
	}

	/**
	 * Constructs an empty Bag with the defined initial capacity.
	 * 
	 * <p>
	 * NOTE: If capacity is less than
	 * {@link ImmutableBag#MINIMUM_WORKING_CAPACITY}, the Bag will be created
	 * with a capacity of {@link ImmutableBag#MINIMUM_WORKING_CAPACITY} instead.
	 * Uses Array.newInstance() to instantiate a backing array of the proper
	 * type.
	 * </p>
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
	 * Removes all the items from this bag. This versions erases up to the
	 * {@link ImmutableBag#size()} of the Bag.
	 */
	public void clear ()
	{
		fillWith( null, data, size );
		size = 0;
	}

	/**
	 * Removes all the items from this bag. This version erases up to the
	 * {@link ImmutableBag#capacity()} of the Bag.
	 */
	public void totalClear ()
	{
		fillWith( null, data, data.length );
		size = 0;
	}

	public final T[] data ()
	{
		return this.data;
	}

	public final void setSize ( final int size )
	{
		this.size = size;
	}

}
