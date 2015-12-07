package com.artemis.utils;

import java.util.function.Predicate;
import java.util.function.Supplier;

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
 * <p>
 * These are the grow strategies:
 * </p>
 * <p>
 * capacity*2 when the size is less than
 * {@link ImmutableBag#GROW_RATE_THRESHOLD}
 * </p>
 * <p>
 * capacity*1.5 when the size is more or equal than
 * {@link ImmutableBag#GROW_RATE_THRESHOLD}
 * </p>
 * </p>
 * 
 * @author Arni Arent
 * @author dustContributor
 *
 * @param <T> type of the elements it holds.
 */
public final class Bag<T> extends AbstractBag<T>
{
	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@link ImmutableBag#DEFAULT_CAPACITY}. The backing array type will be
	 * Object.
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
	 * NOTE: If capacity is less than
	 * {@link ImmutableBag#MINIMUM_WORKING_CAPACITY}, the Bag will be created
	 * with a capacity of {@link ImmutableBag#MINIMUM_WORKING_CAPACITY} instead.
	 * The backing array type will be Object.
	 * </p>
	 * 
	 * @param capacity of the Bag
	 */
	public Bag ( final int capacity )
	{
		super( capacity );
	}

	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@link ImmutableBag#DEFAULT_CAPACITY}. Uses Array.newInstance() to
	 * instantiate a backing array of the proper type.
	 * 
	 * @param type of the backing array.
	 */
	public Bag ( final Class<T> type )
	{
		super( type );
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
	public Bag ( final Class<T> type, final int capacity )
	{
		super( type, capacity );
	}

	/**
	 * Adds the specified item to the end of this bag.
	 * 
	 * It will increase the size of the bag as required.
	 * 
	 * @param item to be added to this list
	 */
	public void add ( final T item )
	{
		// if size greater than capacity then increase capacity.
		ensureCapacity( size );
		addUnsafe( item );
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
	public Bag ( final T[] data )
	{
		super( data );
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
	 * @param items to add.
	 * @param itemsLength of the item array that will be added.
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
	 * @param items to add.
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
	 * @param items to add.
	 * @param itemsLength of the item array that will be added.
	 */
	public void addAllUnsafe ( final T[] items, final int itemsLength )
	{
		System.arraycopy( items, 0, data, size, itemsLength );
		size += itemsLength;
	}

	/**
	 * Add all items into this bag.
	 * 
	 * @param items to add.
	 */
	public void addAll ( final Bag<T> items )
	{
		addAll( items.data, items.size );
	}

	/**
	 * Add all items into this bag.
	 * 
	 * @param items to add.
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
	 * @param index of item
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
	 * Tries to get the item at the specified index. If it isn't there, it sets
	 * it to whatever the supplier provides, then returns that result. Will
	 * resize the bag as needed.
	 * 
	 * @param index of the item.
	 * @param supplier of items to use if its missing.
	 * @return item at the index, or null if the index is negative.
	 */
	public T getOrSet ( final int index, final Supplier<T> supplier )
	{
		T tmp = null;
		// If its a valid index.
		if ( index > -1 )
		{
			// Resize if needed.
			ensureCapacity( index );
			// Now get the element.
			tmp = getUnsafe( index );
			// If it isn't there, initialize it.
			if ( tmp == null )
			{
				tmp = supplier.get();
				setUnsafe( index, tmp );
			}
		}
		// Returns null if index isn't valid.
		return tmp;
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
	public void setUnsafe ( final int index, final T item )
	{
		size = Math.max( size, index + 1 );
		data[index] = item;
	}

	/**
	 * Inserts an item into a position of this Bag, shifting any elements
	 * remaining to the right, preserving their order.
	 * 
	 * @param index to insert the item at.
	 * @param item to be inserted into the Bag.
	 */
	public void insert ( final int index, final T item )
	{
		if ( isInSize( index ) )
		{
			ensureCapacity( size + 1 );
			insertUnsafe( index, item );
		}
	}

	/**
	 * Inserts an item into a position of this Bag, shifting any elements
	 * remaining to the right, preserving their order.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param index to insert the item at.
	 * @param item to be inserted into the Bag.
	 */
	public void insertUnsafe ( final int index, final T item )
	{
		final T[] array = data;
		final int aSize = size;
		// Shift elements to the right.
		System.arraycopy( array, index, array, index + 1, aSize - index );
		// Set element and set new size.
		array[index] = item;
		size = aSize + 1;
	}

	/**
	 * Erases an element of this Bag, shifting the remaining elements to the
	 * left, preserving their order.
	 * 
	 * @param index to erase an element at.
	 * @return the element erased from this Bag.
	 */
	public T erase ( final int index )
	{
		if ( isInSize( index ) )
		{
			return eraseUnsafe( index );
		}

		return null;
	}

	/**
	 * Erases an element of this Bag, shifting the remaining elements to the
	 * left, preserving their order.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param index to erase an element at.
	 * @return the element erased from this Bag.
	 */
	public T eraseUnsafe ( final int index )
	{
		final T[] array = data;
		final T item = array[index];
		final int newSize = size - 1;
		// Shift elements to the left.
		System.arraycopy( array, index + 1, array, index, newSize - index );
		// Null last element and set new size.
		array[newSize] = null;
		size = newSize;
		// Return erased element.
		return item;
	}

	/**
	 * Removes the item at the specified position in this Bag. Does this by
	 * overwriting it was last item then removing last item.
	 * 
	 * It returns <code>null</code> if the index its outside bounds or if the
	 * item at the index was <code>null</code>.
	 * 
	 * @param index the index of item to be removed
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
	 * @param index the index of item to be removed
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
	 * @param item to be removed from this bag.
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
	 * @param criteria to match the items against.
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
	 * @param bag containing items to be removed from this Bag
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
	 * @param bag containing items to be removed from this Bag
	 */
	public void removeAll ( final ImmutableBag<T> bag )
	{
		final int bagSize = bag.size();

		for ( int i = 0; i < bagSize; ++i )
		{
			remove( bag.getUnsafe( i ) );
		}
	}

	/**
	 * Compacts the backing array of this Bag to the left, leaving all null
	 * values to the right. Preserves ordering.
	 */
	public void compact ()
	{
		final T[] arr = data;
		final int prevSize = size;
		int newSize = prevSize;

		for ( int i = prevSize; i-- > 0; )
		{
			// If there isn't something to compact.
			if ( arr[i] != null )
			{
				continue;
			}
			// Found something.
			int j = i;

			// Find more slots to compact.
			while ( j-- > 0 )
			{
				if ( arr[j] != null )
				{
					break;
				}
			}
			// Copy contents to left.
			System.arraycopy( arr, i + 1, arr, j + 1, newSize - i - 1 );
			// Size + diff between start and finish of null slots.
			newSize += j - i;
			// Jump over recently compacted elements.
			i = j;
		}

		// If something was compacted.
		if ( prevSize != newSize )
		{
			// Nullify everything to right.
			for ( int i = prevSize; i-- > newSize; )
			{
				arr[i] = null;
			}
			// Set new size.
			size = newSize;
		}
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
