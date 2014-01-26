package com.artemis.utils;


/**
 * Free bag has less restrictions than Bag. It doesn't has a minimum capacity,
 * and makes its fields public.
 * 
 * @author TheChubu
 *
 * @param <T>
 */
public class FreeBag<T>
{
	public T[] data;
	public int size = 0;

	/**
	 * Constructs an empty Bag with an initial capacity of {@value #MINIMUM_CAPACITY}
	 * 
	 */
	public FreeBag ()
	{
		this( 8 );
	}

	/**
	 * Constructs an empty Bag with the specified initial capacity.
	 * 
	 * @param capacity of the Bag
	 */
	@SuppressWarnings("unchecked")
	public FreeBag ( final int capacity )
	{
		data = (T[]) new Object[ capacity ];
	}

	/**
	 * Removes the item at the specified position in this Bag. Does this by
	 * overwriting it was last item then removing last item
	 * 
	 * @param index
	 *            the index of item to be removed
	 * @return item that was removed from the Bag
	 */
	public T remove ( final int index )
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
	 * @return the first value in the bag, or null if it has no values.
	 */
	public T removeFirst ()
	{
		if ( size > 0 )
		{
			// Decrement size.
			--size;
			// Save first value.
			final T value = data[0];
			// Replace first value with the last.
			data[0] = data[size];
			// Return saved value.
			return value;
		}
		
		// The Bag is empty.
		return null;
	}
	
	/**
	 * Removes the last object in the bag.
	 * 
	 * @return the last item in the bag, or null if it has no items.
	 */
	public T removeLast ()
	{
		if ( size > 0 )
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
		
		// Bag is empty.
		return null;
	}

	/**
	 * Removes the first occurrence of the specified item from this Bag, if
	 * it is present. Works by overwriting it was last item then removing last item.
	 * 
	 * @param item to be removed from this bag.
	 * @return true if this bag contained the specified item.
	 */
	public boolean remove ( final T item )
	{
		for ( int i = 0; i < size; ++i )
		{
			if ( item == data[i] )
			{
				// Decrement size.
				--size;
				// Overwrite item with last item.
				data[i] = data[size];
				// Null last item.
				data[size] = null;
				// Item has been removed.
				return true;
			}
		}
		// Item not found.
		return false;
	}
	
	/**
	 * Check if the bag contains this item.
	 * 
	 * @param item
	 * @return
	 */
	public boolean contains ( final T item )
	{
		for ( int i = 0; i < size; ++i )
		{
			if ( item == data[i] )
			{
				// Item found.
				return true;
			}
		}
		
		// Item not found.
		return false;
	}
	
	/**
	 * Removes from this Bag all of its items that are contained in the
	 * specified Bag.
	 * 
	 * @param bag containing items to be removed from this Bag
	 * @return {@code true} if this Bag changed as a result of the call
	 */
	public boolean removeAll ( final FreeBag<T> bag )
	{
		boolean modified = false;
		
		final T[] bagData = bag.data;
		
		for ( int i = 0; i < bag.size(); ++i )
		{
			final T item1 = bagData[i];
			
			for ( int j = 0; j < size; ++j )
			{
				if ( item1 == data[j] )
				{
					remove( j );
					--j;
					modified = true;
					break;
				}
			}
		}

		return modified;
	}

	/**
	 * Removes from this Bag all of its items that are contained in the
	 * specified Bag.
	 * 
	 * @param bag containing items to be removed from this Bag
	 * @return {@code true} if this Bag changed as a result of the call
	 */
	public boolean removeAll ( final ImmutableBag<T> bag )
	{
		boolean modified = false;
		
		for ( int i = 0; i < bag.size(); ++i )
		{
			final T item1 = bag.get( i );

			for ( int j = 0; j < size; ++j )
			{
				if ( item1 == data[j] )
				{
					remove( j );
					--j;
					modified = true;
					break;
				}
			}
		}

		return modified;
	}

	/**
	 * Returns the item at the specified position in Bag.
	 * 
	 * @param index of the item to return
	 * @return item at the specified position in bag
	 */
	public T get ( final int index )
	{
		return data[index];
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
	public int getCapacity ()
	{
		return data.length;
	}
	
	/**
	 * Checks if the internal storage supports this index.
	 * 
	 * @param index
	 * @return
	 */
	public boolean isIndexWithinBounds ( final int index )
	{
		return index < data.length;
	}

	/**
	 * Returns true if this list contains no items.
	 * 
	 * @return true if this list contains no items
	 */
	public boolean isEmpty ()
	{
		return size < 1;
	}

	/**
	 * Adds the specified item to the end of this bag. if needed also
	 * increases the capacity of the bag.
	 * 
	 * @param item to be added to this list
	 */
	public void add ( final T item )
	{
		// if size greater than capacity then increase capacity.
		if ( size >= data.length )
		{
			grow();
		}

		data[size] = item;
		// Increment size.
		++size;
	}

	/**
	 * Set item at specified index in the bag.
	 * 
	 * @param index of item
	 * @param item
	 */
	public void set ( final int index, final T item )
	{
		if ( index >= data.length )
		{
			grow( index + data.length );
			size = index + 1;
		}
		else if ( index >= size )
		{
			size = index + 1;
		}
		
		data[index] = item;
	}

	private void grow ()
	{
		final int len = data.length;
		final int newLen = len + ( len >> 1 );
		grow( newLen );
	}
	
	@SuppressWarnings("unchecked")
	private void grow ( final int newCapacity )
	{
		final T[] newArray = (T[]) new Object[newCapacity];
		System.arraycopy( data, 0, newArray, 0, size );
		data = newArray;
	}
	
	public void ensureCapacity ( final int index )
	{
		if ( index >= data.length )
		{
			grow( index + data.length );
		}
	}

	/**
	 * Removes all the items from this bag.
	 */
	public void clear ()
	{
		// Null all items.
		
		// This is OpsArray.fastArrayFill() inlined into the method.
		data[0] = null;

		for ( int i = 1; i < data.length; i += i )
		{
			System.arraycopy( data, 0, data, i, ( ( data.length - i ) < i ) ? ( data.length - i ) : i );
		}

		size = 0;
	}

	/**
	 * Add all items into this bag. 
	 * 
	 * @param items to add.
	 */
	public void addAll ( final FreeBag<T> items )
	{
		ensureCapacity( items.size() + size );
		
		System.arraycopy( items.data, 0, data, size, items.size() );
		
		size += items.size();
	}
	
	/**
	 * Add all items into this bag. 
	 * 
	 * @param items to add.
	 */
	public void addAll ( final ImmutableBag<T> items )
	{
		ensureCapacity( items.size() + size );
		
		for ( int i = 0; i < items.size(); ++i )
		{
			add( items.get( i ) );
		}
	}
	
	@Override
	public String toString ()
	{
		final String newLine = System.lineSeparator();
		final StringBuilder str = new StringBuilder( data.length * 10  );
		
		str.append( super.toString() ).append( newLine );
		str.append( "Capacity " ).append(  this.getCapacity() ).append( newLine );
		str.append( "Size " ).append( this.size );
		
		for ( int i = 0; i < size; ++i )
		{
			str.append( newLine );
			str.append( data[i] );
		}
		
		return str.toString();
	}

}
