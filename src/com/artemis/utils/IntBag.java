package com.artemis.utils;

import java.util.Arrays;
import java.util.function.IntPredicate;

public class IntBag extends ImmutableIntBag
{
	/**
	 * Constructs an empty {@link IntBag} with an initial capacity of
	 * {@value #DEFAULT_CAPACITY}.
	 * 
	 */
	public IntBag ()
	{
		super();
	}

	/**
	 * Constructs an empty {@link IntBag} with the defined initial capacity.
	 * 
	 * <p>
	 * NOTE: If capacity is less than {@value #MINIMUM_WORKING_CAPACITY}, the
	 * {@link IntBag} will be created with a capacity of
	 * {@value #MINIMUM_WORKING_CAPACITY} instead.
	 * </p>
	 * 
	 * @param capacity of the {@link IntBag}.
	 */
	public IntBag ( int capacity )
	{
		super( capacity );
	}

	/**
	 * Adds the specified item to the end of this bag.
	 * 
	 * It will increase the size of the bag as required.
	 * 
	 * @param item to be added to this list
	 */
	public void add ( final int item )
	{
		// if size greater than capacity then increase capacity.
		ensureCapacity( size );
		addUnsafe( item );
	}

	/**
	 * Adds the specified item to the end of this bag.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param item to be added to this list
	 */
	public void addUnsafe ( final int item )
	{
		data[size] = item;
		++size;
	}

	/**
	 * Add items into this bag. Does nothing if itemsLength is less than 1.
	 * 
	 * @param items to add.
	 * @param itemsLength of the item array that will be added.
	 */
	public void addAll ( final int[] items, final int itemsLength )
	{
		if ( itemsLength > 0 )
		{
			ensureCapacity( itemsLength + size );
			addAllUnsafe( items, itemsLength );
		}
	}

	/**
	 * Add all items into this bag.
	 * 
	 * @param items to add.
	 */
	public void addAll ( final int[] items )
	{
		ensureCapacity( items.length + size );
		addAllUnsafe( items, items.length );
	}

	/**
	 * Add items into this bag.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param items to add.
	 * @param itemsLength of the item array that will be added.
	 */
	public void addAllUnsafe ( final int[] items, final int itemsLength )
	{
		System.arraycopy( items, 0, data, size, itemsLength );
		size += itemsLength;
	}

	/**
	 * Add all items into this bag.
	 * 
	 * @param items to add.
	 */
	public void addAll ( final IntBag items )
	{
		addAll( items.data, items.size );
	}

	/**
	 * Add all items into this bag.
	 * 
	 * @param items to add.
	 */
	public void addAll ( final ImmutableIntBag items )
	{
		final int itemsSize = items.size();

		ensureCapacity( itemsSize + size );

		for ( int i = 0; i < itemsSize; ++i )
		{
			add( items.getUnsafe( i ) );
		}
	}

	/**
	 * Set item at specified index in the bag.
	 * 
	 * It will increase the size of the bag as required.
	 * 
	 * @param index of item
	 * @param item to be set.
	 */
	public void set ( final int index, final int item )
	{
		if ( index < 0 )
		{
			return;
		}

		ensureCapacity( index );
		setUnsafe( index, item );
	}

	/**
	 * Set item at specified index in the bag.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param index of item
	 * @param item to be set.
	 */
	public void setUnsafe ( final int index, final int item )
	{
		size = Math.max( size, index + 1 );
		data[index] = item;
	}

	/**
	 * Inserts an item into a position of this {@link IntBag}, shifting any
	 * elements remaining to the right, preserving their order.
	 * 
	 * @param index to insert the item at.
	 * @param item to be inserted into the {@link IntBag}.
	 */
	public void insert ( final int index, final int item )
	{
		final int size = this.size;
		/*
		 * Allow to insert at 0 if the bag is empty, or at the end if there is
		 * anything.
		 */
		if ( index >= 0 && index <= size )
		{
			ensureCapacity( size );
			insertUnsafe( index, item );
		}
	}

	/**
	 * Inserts an item into a position of this {@link IntBag}, shifting any
	 * elements remaining to the right, preserving their order.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param index to insert the item at.
	 * @param item to be inserted into the {@link IntBag}.
	 */
	public void insertUnsafe ( final int index, final int item )
	{
		final int[] array = data;
		final int aSize = size;
		// Shift elements to the right.
		System.arraycopy( array, index, array, index + 1, aSize - index );
		// Set element and set new size.
		array[index] = item;
		size = aSize + 1;
	}

	/**
	 * Erases an element of this {@link IntBag}, shifting the remaining elements
	 * to the left, preserving their order.
	 * 
	 * @param index to erase an element at.
	 * @param defaultValue to return in case the index is outside bounds.
	 * @return the element erased from this {@link IntBag}, or defaultValue if the
	 *         index is outside bounds.
	 */
	public int erase ( final int index, final int defaultValue )
	{
		if ( isInSize( index ) )
		{
			return eraseUnsafe( index );
		}

		return defaultValue;
	}

	/**
	 * Erases an element of this {@link IntBag}, shifting the remaining elements
	 * to the left, preserving their order.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param index to erase an element at.
	 * @return the element erased from this {@link IntBag}.
	 */
	public int eraseUnsafe ( final int index )
	{
		final int[] array = data;
		final int item = array[index];
		final int newSize = size - 1;
		// Shift elements to the left.
		System.arraycopy( array, index + 1, array, index, newSize - index );
		size = newSize;
		return item;
	}

	/**
	 * Erases a range of elements in this {@link IntBag}, shifting the remaining
	 * elements to the left, preserving their order.
	 * 
	 * @param index from which the elements will start to be erased.
	 * @param length of the range to be erased.
	 */
	public void eraseRange ( final int index, final int length )
	{
		if ( isInSize( index ) && isInSize( index + length ) )
		{
			eraseRangeUnsafe( index, length );
		}
	}

	/**
	 * Erases a range of elements in this {@link IntBag}, shifting the remaining
	 * elements to the left, preserving their order.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param index from which the elements will start to be erased.
	 * @param length of the range to be erased.
	 */
	public void eraseRangeUnsafe ( final int index, final int length )
	{
		final int[] array = data;
		final int newSize = size - length;
		// Shift elements to the left.
		System.arraycopy( array, index + length, array, index, newSize - index );
		size = newSize;
	}

	/**
	 * Removes the item at the specified position in this {@link IntBag}. Does
	 * this by overwriting it was last item then removing last item.
	 * 
	 * It returns <code>null</code> if the index its outside bounds or if the item
	 * at the index was <code>null</code>.
	 * 
	 * @param index the index of item to be removed
	 * @param defaultValue to return in case the index is outside bounds.
	 * @return item that was removed from the {@link IntBag}, or defaultValue if
	 *         the index is outside bounds.
	 */
	public int remove ( final int index, final int defaultValue )
	{
		if ( isInSize( index ) )
		{
			return removeUnsafe( index );
		}

		return defaultValue;
	}

	/**
	 * Removes the item at the specified position in this {@link IntBag}. Does
	 * this by overwriting it was last item then removing last item.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param index the index of item to be removed
	 * @return item that was removed from the {@link IntBag}
	 */
	public int removeUnsafe ( final int index )
	{
		final int item = data[index];
		// Overwrite item with last item.
		data[index] = data[--size];
		// Return removed item.
		return item;
	}

	/**
	 * Removes the first value in the bag.
	 * 
	 * @param defaultValue to return in case the {@link IntBag} is empty.
	 * 
	 * @return the first value in the bag, or defaultValue if the {@link IntBag}
	 *         is empty.
	 */
	public int removeFirst ( final int defaultValue )
	{
		if ( size > 0 )
		{
			return removeFirstUnsafe();
		}

		// The Bag is empty.
		return defaultValue;
	}

	/**
	 * Removes the first value in the bag.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @return the first value in the bag.
	 */
	public int removeFirstUnsafe ()
	{
		return removeUnsafe( 0 );
	}

	/**
	 * Removes the last object in the bag.
	 * 
	 * @param defaultValue to return in case the {@link IntBag} is empty.
	 * 
	 * @return the last item in the bag, or defaultValue if the {@link IntBag} is
	 *         empty.
	 */
	public int removeLast ( final int defaultValue )
	{
		if ( size > 0 )
		{
			return removeLastUnsafe();
		}

		// Bag is empty.
		return defaultValue;
	}

	/**
	 * Removes the last object in the bag.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @return the last item in the bag.
	 */
	public int removeLastUnsafe ()
	{
		// Get last item.
		final int item = data[--size];
		// Return item.
		return item;
	}

	/**
	 * Removes the first occurrence of the specified item from this
	 * {@link IntBag}, if it is present. Works by overwriting it was last item
	 * then removing last item.
	 * 
	 * @param item to be removed from this bag.
	 * @return <code>true</code> if this bag contained the specified item.
	 */
	public boolean remove ( final int item )
	{
		final int iSize = size;

		for ( int i = 0; i < iSize; ++i )
		{
			if ( item == data[i] )
			{
				// Item found, remove it.
				removeUnsafe( i );
				// Item has been removed.
				return true;
			}
		}

		// Item not found.
		return false;
	}

	/**
	 * Removes the first occurrence of the item that matches the provided
	 * criteria. Works by overwriting it was last item then removing last item.
	 * 
	 * @param criteria to match the items against.
	 * @param defaultValue to return if the item isn't found.
	 * @return the first item that matched the criteria, <code>defaultValue</code>
	 *         if no items matched the criteria.
	 */
	public int remove ( final IntPredicate criteria, final int defaultValue )
	{
		final int iSize = size;

		for ( int i = 0; i < iSize; ++i )
		{
			if ( criteria.test( data[i] ) )
			{
				// Item found. Remove and return it.
				return removeUnsafe( i );
			}
		}

		// Item not found.
		return defaultValue;
	}

	/**
	 * Removes from this {@link IntBag} all of its items that are contained in the
	 * specified {@link IntBag}.
	 * 
	 * @param bag containing items to be removed from this {@link IntBag}
	 */
	public void removeAll ( final IntBag bag )
	{
		final int[] bagData = bag.data;
		final int bagSize = bag.size;

		for ( int i = 0; i < bagSize; ++i )
		{
			remove( bagData[i] );
		}
	}

	/**
	 * Removes from this {@link IntBag} all of its items that are contained in the
	 * specified {@link IntBag}.
	 * 
	 * @param bag containing items to be removed from this {@link IntBag}
	 */
	public void removeAll ( final ImmutableIntBag bag )
	{
		final int bagSize = bag.size();

		for ( int i = 0; i < bagSize; ++i )
		{
			remove( bag.getUnsafe( i ) );
		}
	}

	/**
	 * Performs binary search for the value provided. Won't work if the values
	 * aren't sorted.
	 * 
	 * @param value to search for.
	 * @return the index of the value if found,
	 *         <code>(-wouldBeIndexOfInsertion - 1)</code> if it wasn't.
	 */
	public int binarySearch ( final int value )
	{
		return Arrays.binarySearch( this.data, 0, this.size, value );
	}

	/**
	 * Shrinks the capacity of this {@link IntBag} to the next capacity that could
	 * hold a {@link IntBag} of this size.
	 * 
	 * If the capacities match, then it leaves the {@link IntBag} as it is.
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
	 * Resizes the {@link IntBag} so it can contain the index provided.
	 * 
	 * @param index that is expected the {@link IntBag} can contain.
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
	 * Sets all items in this bag to the provided value.
	 * 
	 * @param value to set the entire bag to.
	 */
	public void clear ( final int value )
	{
		Arrays.fill( data, value );
		size = 0;
	}

	/**
	 * Sets all items in this bag to 0.
	 */
	public void clear ()
	{
		clear( 0 );
	}

	public int[] data ()
	{
		return this.data;
	}

	public void setSize ( final int size )
	{
		this.size = size;
	}

	@Override
	public String toString ()
	{
		final String newLine = System.lineSeparator();
		final StringBuilder str = new StringBuilder( data.length * 10 );

		str.append( super.toString() ).append( newLine );
		str.append( "Capacity " ).append( this.capacity() ).append( newLine );
		str.append( "Size " ).append( this.size );

		for ( int i = 0; i < size; ++i )
		{
			str.append( newLine );
			str.append( data[i] );
		}

		return str.toString();
	}

	private void resize ( final int newCapacity )
	{
		data = Arrays.copyOf( data, newCapacity );
	}

}
