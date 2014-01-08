package com.artemis.utils;


/**
 * Customized unordered Bag of stuff.
 * 
 * Warnings removed, changed array storing methods,
 * added more comments, established minimum capacity for the bag,
 * changed various methods (specially grow related ones),
 * added 'final' keyword where it was possible, fixed warnings, etc.
 * 
 * Hopefully some operations will be faster.
 * 
 * @author TheChubu
 *
 * @param <T>
 */
public class Bag<T> implements ImmutableBag<T>
{
	private T[] array;
	private int size = 0;
	
	private static final int MINIMUM_CAPACITY = 32;

	/**
	 * Constructs an empty Bag with an initial capacity of {@literal 32}
	 * 
	 */
	public Bag ()
	{
		this( MINIMUM_CAPACITY );
	}

	/**
	 * Constructs an empty Bag with the specified initial capacity.
	 * 
	 * NOTE: Minimum capacity is {@literal 32}.
	 * 
	 * @param capacity of the Bag
	 */
	@SuppressWarnings("unchecked")
	public Bag ( final int capacity )
	{
		array = (T[]) new Object[ ( capacity > MINIMUM_CAPACITY ) ? capacity : MINIMUM_CAPACITY];
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
		final T item = array[index]; 
		// Overwrite item with last item.
		array[index] = array[--size]; 
		// Null last item.
		array[size] = null;
		// Return removed item.
		return item;
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
			// Reduce size by one and get last item.
			final T item = array[--size];
			// Null last position.
			array[size] = null;
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
			if ( item == array[i] )
			{
				// Overwrite item with last item.
				array[i] = array[--size];
				// Null last item.
				array[size] = null;
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
	@Override
	public boolean contains ( final T item )
	{
		for ( int i = 0; size > i; ++i )
		{
			if ( item == array[i] )
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
	public boolean removeAll ( final ImmutableBag<T> bag )
	{
		boolean modified = false;

		for ( int i = 0; i < bag.size(); ++i )
		{
			final T item1 = bag.get( i );

			for ( int j = 0; j < size; ++j )
			{
				final T item2 = array[j];

				if ( item1 == item2 )
				{
					remove( j );
					j--;
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
	@Override
	public T get ( final int index )
	{
		return array[index];
	}

	/**
	 * Returns the number of items in this bag.
	 * 
	 * @return number of items in this bag.
	 */
	@Override
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
		return array.length;
	}
	
	/**
	 * Checks if the internal storage supports this index.
	 * 
	 * @param index
	 * @return
	 */
	public boolean isIndexWithinBounds ( final int index )
	{
		return index < array.length;
	}

	/**
	 * Returns true if this list contains no items.
	 * 
	 * @return true if this list contains no items
	 */
	@Override
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
		if ( size >= array.length )
		{
			grow();
		}

		array[size++] = item;
	}

	/**
	 * Set item at specified index in the bag.
	 * 
	 * @param index of item
	 * @param item
	 */
	public void set ( final int index, final T item )
	{
		if ( index >= array.length )
		{
			grow( index + array.length );
		}
		
		array[index] = item;
		
		size = index + 1;
	}

	private void grow ()
	{
		final int len = array.length;
		final int newLen = len + ( len >> 1 );
		grow( newLen );
	}
	
	@SuppressWarnings("unchecked")
	private void grow ( final int newCapacity )
	{
		final T[] newArray = (T[]) new Object[newCapacity];
		System.arraycopy( array, 0, newArray, 0, size );
		array = newArray;
	}
	
	public void ensureCapacity ( final int index )
	{
		if ( index >= array.length )
		{
			grow( index + array.length );
		}
	}

	/**
	 * Removes all the items from this bag.
	 */
	public void clear ()
	{
		// Null all items.
		
		// This is OpsArray.fastArrayFill() inlined into the method.
		array[0] = null;

		for ( int i = 1; i < array.length; i += i )
		{
			System.arraycopy( array, 0, array, i, ( ( array.length - i ) < i ) ? ( array.length - i ) : i );
		}

		size = 0;
	}

	/**
	 * Add all items into this bag. 
	 * 
	 * @param items to add.
	 */
	public void addAll ( final Bag<T> items )
	{
		ensureCapacity( items.size() + size );
		
		System.arraycopy( items.array, 0, array, size, items.size() );
		
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

}
