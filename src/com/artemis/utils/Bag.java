package com.artemis.utils;

import java.util.function.Predicate;

/**
 * Collection type a bit like ArrayList but does not preserve the order of its
 * entities, speed-wise it is very good, especially suited for games.
 *
 * <p>
 * dustArtemis: It has been spiced up considerably. It supports Java 8 stream()
 * and parallelStream(), allows direct backing array access with data(), uses
 * Array.newInstance() to provide arrays with proper size (to work easily with
 * direct backing array access). Its set(index) method has been fixed, uses a
 * default minimum capacity, implements grow strategies so it doesn't grows
 * madly after a few thousand adds, clearly defines different 'safe' and
 * 'unsafe' methods, and probably more stuff I'm missing.
 * 
 * <p>
 * Growth is predictable. Before set(index,value) method enlarged the array
 * depending on the index that was being set, now it follows the grow strategies
 * always.
 * </p>
 * 
 * <p>These are the grow strategies:</p>
 * <p>capacity*2 when the size is less than {@value #GROW_RATE_THRESHOLD}</p>
 * <p>capacity*1.5 when the size is more or equal than {@value #GROW_RATE_THRESHOLD}</p>
 * 
 * </p>
 *
 * @author Arni Arent
 *
 * @param <T> type of the items that will be stored in this Bag.
 */
public final class Bag<T> extends ImmutableBag<T>
{
	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@value #DEFAULT_CAPACITY}. The backing array type will be Object.
	 * 
	 */
	public Bag ()
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
	public Bag ( final int capacity )
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
	public Bag ( final Class<T> type )
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
	public Bag ( final Class<T> type, final int capacity )
	{
		super( type, capacity );
	}

	/**
	 * Adds the specified item to the end of this bag.
	 * 
	 * It will increase the size of the bag as required.
	 * 
	 * @param item
	 *            to be added to this list
	 */
	public void add ( final T item )
	{
		final int len = data.length;
		// if size greater than capacity then increase capacity.
		if ( size >= len )
		{
			grow( nextCapacity( len ) );
		}

		addUnsafe( item );
	}

	/**
	 * Adds the specified item to the end of this bag.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param item
	 *            to be added to this list
	 */
	public void addUnsafe ( final T item )
	{
		// Set the new item.
		data[size] = item;
		// Increment size.
		++size;
	}
	
	/**
	 * Add items into this bag. Does nothing if itemsLength is less than 1.
	 * 
	 * @param items
	 *            to add.
	 * @param itemsLength
	 *            of the item array that will be added.
	 */
	public void addAll ( final T[] items, final int itemsLength )
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
	 * @param items
	 *            to add.
	 */
	public void addAll ( final T[] items )
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
	 * @param items
	 *            to add.
	 * @param itemsLength
	 *            of the item array that will be added.
	 */
	public void addAllUnsafe ( final T[] items, final int itemsLength )
	{
		System.arraycopy( items, 0, data, size, itemsLength );
		size += itemsLength;
	}

	/**
	 * Add all items into this bag.
	 * 
	 * @param items
	 *            to add.
	 */
	public void addAll ( final Bag<T> items )
	{
		addAll( items.data, items.size );
	}

	/**
	 * Add all items into this bag.
	 * 
	 * @param items
	 *            to add.
	 */
	public void addAll ( final ImmutableBag<T> items )
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
	 * @param index
	 *            of item
	 * @param item to be set.
	 */
	public void set ( final int index, final T item )
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
	 * @param index
	 *            of item
	 * @param item to be set.
	 */
	public void setUnsafe ( final int index, final T item )
	{
		size = Math.max( size, index + 1 );
		data[index] = item;
	}

	@Override
	public T get ( final int index )
	{
		if ( isInBounds( index ) )
		{
			return getUnsafe( index );
		}

		return null;
	}

	@Override
	public T getUnsafe ( final int index )
	{
		return data[index];
	}

	/**
	 * Removes the item at the specified position in this Bag. Does this by
	 * overwriting it was last item then removing last item.
	 * 
	 * It returns <code>null</code> if the index its outside bounds or if the
	 * item at the index was <code>null</code>.
	 * 
	 * @param index
	 *            the index of item to be removed
	 * @return item that was removed from the Bag.
	 */
	public T remove ( final int index )
	{
		if ( isInBounds( index ) )
		{
			return removeUnsafe( index );
		}

		return null;
	}

	/**
	 * Removes the item at the specified position in this Bag. Does this by
	 * overwriting it was last item then removing last item.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param index
	 *            the index of item to be removed
	 * @return item that was removed from the Bag
	 */
	public T removeUnsafe ( final int index )
	{
		// Item ref copy.
		final T item = data[index];
		// Decrement size.
		--size;
		// Overwrite item with last item.
		data[index] = data[size];
		// Null last item.
		data[size] = null;
		// Return removed item.
		return item;
	}

	/**
	 * Removes the first value in the bag.
	 * 
	 * @return the first value in the bag, or <code>null</code> if it has no
	 *         values.
	 */
	public T removeFirst ()
	{
		if ( size > 0 )
		{
			return removeFirstUnsafe();
		}

		// The Bag is empty.
		return null;
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
	public T removeFirstUnsafe ()
	{
		return removeUnsafe( 0 );
	}

	/**
	 * Removes the last object in the bag.
	 * 
	 * @return the last item in the bag, or <code>null</code> if it has no
	 *         items.
	 */
	public T removeLast ()
	{
		if ( size > 0 )
		{
			return removeLastUnsafe();
		}

		// Bag is empty.
		return null;
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
	public T removeLastUnsafe ()
	{
		// Decrement size.
		--size;
		// Get last item.
		final T item = data[size];
		// Null last position.
		data[size] = null;
		// Return item.
		return item;
	}

	/**
	 * Removes the first occurrence of the specified item from this Bag, if it
	 * is present. Works by overwriting it was last item then removing last
	 * item.
	 * 
	 * @param item
	 *            to be removed from this bag.
	 * @return <code>true</code> if this bag contained the specified item.
	 */
	public boolean remove ( final T item )
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
	 * @param criteria
	 *            to match the items against.
	 * 
	 * @return the first item that matched the criteria, <code>null</code> if no
	 *         items matched the criteria.
	 */
	public T remove ( final Predicate<T> criteria )
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
		return null;
	}
	
	/**
	 * Removes from this Bag all of its items that are contained in the
	 * specified Bag.
	 * 
	 * @param bag
	 *            containing items to be removed from this Bag
	 */
	public void removeAll ( final Bag<T> bag )
	{
		final T[] bagData = bag.data;
		final int bagSize = bag.size;
		
		for ( int i = 0; i < bagSize; ++i )
		{
			remove( bagData[i] );
		}
	}

	/**
	 * Removes from this Bag all of its items that are contained in the
	 * specified Bag.
	 * 
	 * @param bag
	 *            containing items to be removed from this Bag
	 */
	public void removeAll ( final ImmutableBag<T> bag )
	{
		final int bagSize = bag.size();
		
		for ( int i = 0; i < bagSize; ++i )
		{
			remove( bag.getUnsafe( i ) );
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
	 * @param index that is expected the Bag can contain.
	 */
	public void ensureCapacity ( final int index )
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
	}

	public void setSize ( final int size )
	{
		this.size = size;
	}

	public T[] data ()
	{
		return this.data;
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

}
