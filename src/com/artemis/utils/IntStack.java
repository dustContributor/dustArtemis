package com.artemis.utils;

/**
 * Stack of integers.
 * 
 * @author dustContributor
 */
public final class IntStack
{
	private int[] data;
	private int size = 0;

	public static final int MINIMUM_CAPACITY = 16,
							EMPTY_STACK = Integer.MIN_VALUE;

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
	 * @return the top of the stack, or {@value #EMPTY_STACK} if it has no
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
		return EMPTY_STACK;
	}

	/**
	 * Removes the value in the top of the stack.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @return the top of the stack, or {@value #EMPTY_STACK} if it has no
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
	 * 
	 * @return top of the stack.
	 */
	public final int peek ()
	{
		if ( size > 0 )
		{
			 return data[size - 1];
		}
		// Stack is empty.
		return EMPTY_STACK;
	}

	/**
	 * Returns the top value of the stack without removing it.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @return top of the stack.
	 */
	public final int unsafePeek ()
	{
		return data[size - 1];
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

		// Put value on the top.
		data[size] = value;
		// Increment size.
		++size;
	}

	/**
	 * Pushes the value into the top of the stack.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param value
	 *            to be added to this stack.
	 */
	public final void unsafePush ( final int value )
	{
		// Put value on the top.
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
		/*
		 * Iterating over the array turns out to be faster for primitive types
		 * than System.arraycopy(). Specially after a few runs.
		 */
		for ( int i = 0; i < data.length; ++i )
		{
			newArray[i] = data[i];
		}

		data = newArray;
	}

	public final void ensureCapacity ( final int count )
	{
		if ( count >= data.length )
		{
			int newSize = data.length << 1;
			
			while ( newSize <= count )
			{
				newSize <<= 1;
			}
			
			grow( newSize );
		}
	}

	/**
	 * Marks the stack as empty.
	 */
	public final void clear ()
	{
		size = 0;
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
