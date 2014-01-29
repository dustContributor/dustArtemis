package com.artemis.utils;

/**
 * Stack of integers.
 * 
 * @author TheChubu
 */
public final class IntStack
{
	private int[] data;
	private int size = 0;

	private static final int MINIMUM_CAPACITY = 8;
	public static final int NOT_RETURN = Integer.MIN_VALUE;

	/**
	 * Constructs an empty IntStack with an initial capacity of
	 * {@value #MINIMUM_CAPACITY}
	 * 
	 */
	public IntStack ()
	{
		this( MINIMUM_CAPACITY );
	}

	/**
	 * Constructs an empty IntStack with the specified initial capacity.
	 * 
	 * <p>
	 * NOTE: If capacity is less than {@value #MINIMUM_CAPACITY}, the IntStack
	 * will be created with a capacity of {@value #MINIMUM_CAPACITY} instead.
	 * </p>
	 * 
	 * @param capacity
	 *            of the IntStack.
	 */
	public IntStack ( final int capacity )
	{
		data = new int[ ( capacity > MINIMUM_CAPACITY ) ? capacity : MINIMUM_CAPACITY];
	}

	/**
	 * Removes the value in the top of the stack.
	 * 
	 * @return the top of the stack, or {@value #NOT_RETURN} if it has no
	 *         values.
	 */
	public final int pop ()
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
	 * Removes the value in the top of the stack.
	 * 
	 * Doesn't checks bounds.
	 * 
	 * @return the top of the stack, or {@value #NOT_RETURN} if it has no
	 *         values.
	 */
	public final int unsafePop ()
	{
		// Decrement size.
		--size;
		// Return last value.
		return data[size];
	}

	/**
	 * Returns the top value of the stack without removing it.
	 * 
	 * @return top of the stack.
	 */
	public final int peek ()
	{
		return data[size - 1];
	}

	/**
	 * Returns the number of values in this bag.
	 * 
	 * @return number of values in this bag.
	 */
	public final int size ()
	{
		return size;
	}

	/**
	 * Returns the number of values the bag can hold without growing.
	 * 
	 * @return number of values the bag can hold without growing.
	 */
	public final int getCapacity ()
	{
		return data.length;
	}

	/**
	 * Returns true if this list contains no values.
	 * 
	 * @return true if this list contains no values
	 */
	public final boolean isEmpty ()
	{
		return size < 1;
	}

	/**
	 * Pushes the value into the top of the stack.
	 * 
	 * @param value
	 *            to be added to this stack.
	 */
	public final void push ( final int value )
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
	 * Pushes the value into the top of the stack.
	 * 
	 * Doesn't checks bounds.
	 * 
	 * @param value
	 *            to be added to this stack.
	 */
	public final void unsafePush ( final int value )
	{
		data[size] = value;
		// Increment size.
		++size;
	}

	private final void grow ()
	{
		grow( data.length << 1 );
	}

	private final void grow ( final int newCapacity )
	{
		final int[] newArray = new int[newCapacity];
		System.arraycopy( data, 0, newArray, 0, size );
		data = newArray;
	}

	public final void ensureCapacity ( final int count )
	{
		if ( count >= data.length )
		{
			grow( count + data.length );
		}
	}

	/**
	 * Set stack size to 0.
	 */
	public final void clear ()
	{
		size = 0;
	}

	@Override
	public String toString ()
	{
		final String newLine = System.lineSeparator();
		final StringBuilder str = new StringBuilder( data.length * 10 );

		str.append( super.toString() ).append( newLine );
		str.append( "Capacity " ).append( this.getCapacity() ).append( newLine );
		str.append( "Size " ).append( this.size );

		for ( int i = 0; i < size; ++i )
		{
			str.append( newLine );
			str.append( data[i] );
		}

		return str.toString();
	}

}
