package com.artemis.utils;

import java.lang.reflect.Array;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Collection type a bit like ArrayList but does not preserve the order of its
 * entities, speedwise it is very good, especially suited for games.
 *
 * <p>
 * dustArtemis: It has been spiced up considerably. It supports Java 8 stream()
 * and parallelStream(), allows direct backing array access with data(), uses
 * Array.newInstance() to provide arrays with proper size (to work easily with
 * direct backing array access). Its set(index) method has been fixed, uses a
 * default minimum capacity, implements grow strategies so it doesn't grows
 * madly after a few thousand adds, clearly defines different 'safe' and
 * 'unsafe' methods, and probably more stuff I'm missing.
 * </p>
 *
 * @author Arni Arent
 *
 * @param <T>
 */
public class Bag<T> implements ImmutableBag<T>
{
	private T[] data;
	private int size;

	public final Class<?> type;

	private static final int 	DEFAULT_CAPACITY = 16, 
								MINIMUM_WORKING_CAPACITY = 8, 
								GROW_RATE_THRESHOLD = 2048;

	private IntSupplier growStrategy;

	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@value #DEFAULT_CAPACITY}. The backing array type will be Object.
	 * 
	 */
	public Bag ()
	{
		this( DEFAULT_CAPACITY );
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
	@SuppressWarnings ( "unchecked" )
	public Bag ( final int capacity )
	{
		this( (Class<T>) Object.class, capacity );
	}

	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@value #DEFAULT_CAPACITY}. Uses Array.newInstance() to instantiate a
	 * backing array of the proper type.
	 * 
	 * @param capacity
	 *            of the Bag
	 */
	public Bag ( final Class<T> type )
	{
		this( type, DEFAULT_CAPACITY );
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
	 * @param capacity
	 *            of the Bag
	 */
	@SuppressWarnings ( "unchecked" )
	public Bag ( final Class<T> type, final int capacity )
	{
		this.data = (T[]) Array.newInstance( type, (capacity > MINIMUM_WORKING_CAPACITY) ? capacity : MINIMUM_WORKING_CAPACITY );
		this.type = type;
		this.growStrategy = ( ) ->
		{
			if ( size < GROW_RATE_THRESHOLD )
			{
				// Exponential 2 growth.
				return (data.length << 1);
			}

			growStrategy = ( ) ->
			{
				// Exponential 1.5 growth.
				return data.length + (data.length >> 1);
			};

			return growStrategy.getAsInt();
		};
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
		// if size greater than capacity then increase capacity.
		if ( size >= data.length )
		{
			grow( growStrategy.getAsInt() );
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
	 * Set item at specified index in the bag.
	 * 
	 * It will increase the size of the bag as required.
	 * 
	 * @param index
	 *            of item
	 * @param item
	 */
	public void set ( final int index, final T item )
	{
		if ( index < 0 )
		{
			return;
		}

		if ( index >= data.length )
		{
			grow( data.length + index );
		}

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
	 * @param item
	 */
	public void setUnsafe ( final int index, final T item )
	{
		size = Math.max( size, index + 1 );
		data[index] = item;
	}

	/**
	 * Removes the item at the specified position in this Bag. Does this by
	 * overwriting it was last item then removing last item.
	 * 
	 * It returns null if the index its outside bounds or if the item at the
	 * index was null.
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
			// Null last value.
			data[size] = null;
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
	 * Removes the first occurrence of the specified item from this Bag, if it
	 * is present. Works by overwriting it was last item then removing last
	 * item.
	 * 
	 * @param item
	 *            to be removed from this bag.
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

	@Override
	public int contains ( final T item )
	{
		return find( ( i ) -> i == item );
	}

	@Override
	public int find ( final Predicate<T> criteria )
	{
		for ( int i = 0; i < size; ++i )
		{
			if ( criteria.test( data[i] ) )
			{
				// Item found. Return its index.
				return i;
			}
		}

		// Item not found.
		return -1;
	}

	@Override
	public T findAndGet ( final Predicate<T> criteria )
	{
		for ( int i = 0; i < size; ++i )
		{
			if ( criteria.test( data[i] ) )
			{
				// Item found. Return its index.
				return getUnsafe( i );
			}
		}

		// Item not found.
		return null;
	}
	
	@Override
	public void forEach ( final Consumer<T> operation )
	{
		for ( int i = 0; i < size; ++i )
		{
			operation.accept( data[i] );
		}
	}

	/**
	 * Removes from this Bag all of its items that are contained in the
	 * specified Bag.
	 * 
	 * @param bag
	 *            containing items to be removed from this Bag
	 * @return {@code true} if this Bag changed as a result of the call
	 */
	public boolean removeAll ( final Bag<T> bag )
	{
		boolean modified = false;

		final T[] bagData = bag.data;
		final int bagSize = bag.size;

		for ( int i = 0; i < bagSize; ++i )
		{
			final T item1 = bagData[i];

			for ( int j = 0; j < size; ++j )
			{
				if ( item1 == data[j] )
				{
					removeUnsafe( j );
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
	 * @param bag
	 *            containing items to be removed from this Bag
	 * @return {@code true} if this Bag changed as a result of the call
	 */
	public boolean removeAll ( final ImmutableBag<T> bag )
	{
		boolean modified = false;

		final int bagSize = bag.size();

		for ( int i = 0; i < bagSize; ++i )
		{
			final T item1 = bag.getUnsafe( i );

			for ( int j = 0; j < size; ++j )
			{
				if ( item1 == data[j] )
				{
					removeUnsafe( j );
					--j;
					modified = true;
					break;
				}
			}
		}

		return modified;
	}

	@Override
	public int size ()
	{
		return size;
	}

	@Override
	public int capacity ()
	{
		return data.length;
	}

	@Override
	public boolean isEmpty ()
	{
		return size < 1;
	}

	@SuppressWarnings ( "unchecked" )
	private void grow ( final int newCapacity )
	{
		final T[] newArray = (T[]) Array.newInstance( type, newCapacity );
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

	/**
	 * Add all items into this bag.
	 * 
	 * @param items
	 *            to add.
	 */
	public void addAll ( final Bag<T> items )
	{
		final int itemsSize = items.size();

		ensureCapacity( itemsSize + size );
		System.arraycopy( items.data, 0, data, size, itemsSize );

		size += itemsSize;
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

	public T[] data ()
	{
		return this.data;
	}

	@Override
	public Stream<T> stream ()
	{
		final Spliterator<T> split = Spliterators.spliterator( data, Spliterator.IMMUTABLE );

		return StreamSupport.stream( split, false ).limit( size );
	}

	@Override
	public Stream<T> parallelStream ()
	{
		final Spliterator<T> split = Spliterators.spliterator( data, Spliterator.IMMUTABLE );

		return StreamSupport.stream( split, true ).limit( size );
	}

	private boolean isInBounds ( final int index )
	{
		return (index > -1 && index < data.length);
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
