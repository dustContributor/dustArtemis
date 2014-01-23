package com.artemis.utils;



/**
 * Customized unordered Bag for primitive ints.
 * 
 * @author TheChubu
 */
public class IntBag
{
	private int[] data;
	private int size = 0;
	
	private static final int MINIMUM_CAPACITY = 8;
	public static final int NOT_RETURN = Integer.MIN_VALUE;

	/**
	 * Constructs an empty IntBag with an initial capacity of {@value #MINIMUM_CAPACITY}
	 * 
	 */
	public IntBag ()
	{
		this( MINIMUM_CAPACITY );
	}

	/**
	 * Constructs an empty IntBag with the specified initial capacity.
	 * 
	 * <p>NOTE: If capacity is less than {@value #MINIMUM_CAPACITY}, the IntBag will be created with a capacity of {@value #MINIMUM_CAPACITY} instead.</p>
	 * 
	 * @param capacity of the IntBag
	 */
	public IntBag ( final int capacity )
	{
		data = new int[ ( capacity > MINIMUM_CAPACITY ) ? capacity : MINIMUM_CAPACITY];
	}

	/**
	 * Removes the value at the specified position in this IntBag. Does this by
	 * overwriting it was last value then removing last value
	 * 
	 * @param index of the value to be removed
	 * @return value that was removed from the IntBag
	 */
	public int remove ( final int index )
	{
		// value copy.
		final int value = data[index]; 
		// Decrement size.
		--size;
		// Overwrite value with last value.
		data[index] = data[size]; 
		// Return removed value.
		return value;
	}
	
	/**
	 * Removes the first value in the bag.
	 * 
	 * @return the first value in the bag, or {@value #NOT_RETURN} if it has no values.
	 */
	public int removeFirst ()
	{
		if ( size > 0 )
		{
			// Decrement size.
			--size;
			// Save first value.
			final int value = data[0];
			// Replace first value with the last.
			data[0] = data[size];
			// Return saved value.
			return value;
		}
		
		// The Bag is empty.
		return NOT_RETURN;
	}
	
	
	/**
	 * Removes the last value in the bag.
	 * 
	 * @return the last value in the bag, or {@value #NOT_RETURN} if it has no values.
	 */
	public int removeLast ()
	{
		if ( size > 0 )
		{
			// Decrement size.
			--size;
			// Return last value.
			return data[size];
		}
		
		// IntBag is empty.
		return NOT_RETURN;
	}

	/**
	 * Removes the first occurrence of the specified value from this IntBag, if
	 * it is present. Works by overwriting it was last value then removing last value.
	 * 
	 * @param value to be removed from this bag.
	 * @return true if this bag contained the specified value.
	 */
	public boolean removeValue ( final int value )
	{
		for ( int i = 0; i < size; ++i )
		{
			if ( value == data[i] )
			{
				// Decrement size.
				--size;
				// Overwrite value with last value.
				data[i] = data[size];
				// value has been removed.
				return true;
			}
		}
		
		// value not found.
		return false;
	}
	
	/**
	 * Check if the bag contains this value.
	 * 
	 * @param value
	 * @return
	 */
	public boolean contains ( final int value )
	{
		for ( int i = 0; size > i; ++i )
		{
			if ( value == data[i] )
			{
				// value found.
				return true;
			}
		}
		
		// value not found.
		return false;
	}

	/**
	 * Removes from this IntBag all of its values that are contained in the
	 * specified IntBag.
	 * 
	 * @param bag containing values to be removed from this IntBag
	 * @return {@code true} if this IntBag changed as a result of the call
	 */
	public boolean removeAll ( final IntBag bag )
	{
		boolean modified = false;
		
		final int[] bagData = bag.data;
		
		for ( int i = 0; i < bag.size(); ++i )
		{
			final int item1 = bagData[i];
			
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
	 * Returns the value at the specified position in IntBag.
	 * 
	 * @param index of the value to return
	 * @return value at the specified position in bag
	 */
	public int get ( final int index )
	{
		return data[index];
	}

	/**
	 * Returns the number of values in this bag.
	 * 
	 * @return number of values in this bag.
	 */
	public int size ()
	{
		return size;
	}
	
	/**
	 * Returns the number of values the bag can hold without growing.
	 * 
	 * @return number of values the bag can hold without growing.
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
	 * Returns true if this list contains no values.
	 * 
	 * @return true if this list contains no values
	 */
	public boolean isEmpty ()
	{
		return size < 1;
	}

	/**
	 * Adds the specified value to the end of this bag. if needed also
	 * increases the capacity of the bag.
	 * 
	 * @param value to be added to this list
	 */
	public void add ( final int value )
	{
		// if size greater than capacity then increase capacity.
		if ( size >= data.length )
		{
			grow();
		}

		data[size] = value;
		// Increment size.
		++size;
	}

	/**
	 * Set value at specified index in the bag.
	 * 
	 * @param index of value
	 * @param value
	 */
	public void set ( final int index, final int value )
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
		
		data[index] = value;
	}

	private void grow ()
	{
		final int len = data.length;
		final int newLen = len + ( len >> 1 );
		grow( newLen );
	}
	
	private void grow ( final int newCapacity )
	{
		final int[] newArray = new int[newCapacity];
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
	 * Zeroes all the values from this bag.
	 */
	public void clear ()
	{
		for ( int i = 0; i < data.length; ++i )
		{
			data[i] = 0;
		}

		size = 0;
	}

	/**
	 * Add all values into this bag. 
	 * 
	 * @param values to add.
	 */
	public void addAll ( final IntBag values )
	{
		ensureCapacity( values.size() + size );
		
		System.arraycopy( values.data, 0, data, size, values.size() );
		
		size += values.size();
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
