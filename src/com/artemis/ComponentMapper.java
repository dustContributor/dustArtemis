package com.artemis;

import java.util.Arrays;

import com.artemis.utils.ImmutableBag;

/**
 * High performance component retrieval from entities. Use this wherever you
 * need to retrieve components from entities often and fast.
 * 
 * @author Arni Arent
 * 
 * @param <T> the class type of the component
 */
public final class ComponentMapper<T extends Component> extends ImmutableBag<T>
{
	ComponentMapper ()
	{
		super();
	}

	ComponentMapper ( final int capacity )
	{
		super( capacity );
	}

	/**
	 * Constructs an empty mapper with an initial capacity of
	 * {@value #DEFAULT_CAPACITY}. Uses Array.newInstance() to instantiate a
	 * backing array of the proper type.
	 * 
	 * @param type of the backing array.
	 */
	ComponentMapper ( final Class<T> type )
	{
		super( type );
	}

	ComponentMapper ( final Class<T> type, final int capacity )
	{
		super( type, capacity );
	}

	/**
	 * Fast and safe retrieval of a component for this entity. If the entity
	 * does not have this component then null is returned.
	 * 
	 * @param entity that could have the component.
	 * @return the instance of the component
	 */
	public final T get ( final Entity entity )
	{
		return get( entity.id );
	}

	/**
	 * Fast but unsafe retrieval of a component for this entity. In most
	 * scenarios you already know the entity possesses this component.
	 * 
	 * <p> <b>UNSAFE: Avoids doing any bounds check. Could throw an
	 * ArrayIndexOutOfBoundsExeption. </b> </p>
	 * 
	 * @param entity that should have the component.
	 * @return the instance of the component
	 */
	public final T getUnsafe ( final Entity entity )
	{
		return getUnsafe( entity.id );
	}

	/**
	 * Checks if the entity has this type of component.
	 * 
	 * @param entity that could have the component.
	 * @return true if the entity has this component type, false if it doesn't.
	 */
	public final boolean has ( final Entity entity )
	{
		return has( entity.id );
	}

	/**
	 * Checks if the entity has this type of component.
	 * 
	 * @param entityID of the entity that could have the component.
	 * @return true if the entity has this component type, false if it doesn't.
	 */
	public final boolean has ( final int entityID )
	{
		return get( entityID ) != null;
	}

	/**
	 * Returns the backing array of components for this mapper. This can be used
	 * for fast direct array indexing when doing bulk operations on entities.
	 * Just remember to NEVER modify the array directly, unless you know exactly
	 * what you're doing.
	 * 
	 * @return array of components backing this mapper.
	 */
	public final T[] data ()
	{
		return this.data;
	}

	/**
	 * Removes the item at the specified position in this mapper.
	 * 
	 * It returns <code>null</code> if the index its outside bounds or if the
	 * item at the index was <code>null</code>.
	 * 
	 * @param index the index of item to be removed
	 * @return item that was removed from the mapper.
	 */
	final T remove ( final int index )
	{
		if ( isInBounds( index ) )
		{
			return removeUnsafe( index );
		}

		return null;
	}

	/**
	 * Removes the item at the specified position in this mapper.
	 * 
	 * <p> <b>UNSAFE: Avoids doing any bounds check.</b> </p>
	 * 
	 * @param index the index of item to be removed
	 * @return item that was removed from the mapper
	 */
	final T removeUnsafe ( final int index )
	{
		// Item ref copy.
		final T item = data[index];
		// Null item.
		data[index] = null;
		// Return removed item.
		return item;
	}

	/**
	 * Set item at specified index in the mapper.
	 * 
	 * @param index of item
	 * @param item to be set.
	 */
	final void set ( final int index, final T item )
	{
		if ( index < 0 )
		{
			return;
		}

		ensureCapacity( index );
		setUnsafe( index, item );
	}

	/**
	 * Set item at specified index in the mapper.
	 * 
	 * <p> <b>UNSAFE: Avoids doing any bounds check.</b> </p>
	 * 
	 * @param index of item
	 * @param item to be set.
	 */
	final void setUnsafe ( final int index, final T item )
	{
		data[index] = item;
	}

	/**
	 * Resizes the mapper so it can contain the index provided.
	 * 
	 * @param index that is expected the mapper could contain.
	 */
	final void ensureCapacity ( final int index )
	{
		if ( index >= data.length )
		{
			data = Arrays.copyOf( data, getCapacityFor( index ) );
		}
	}

	/**
	 * It finds the next capacity according to the grow strategy that could
	 * contain the supplied index.
	 * 
	 * @param index that needs to be contained.
	 * @return proper capacity given by the grow strategy.
	 */
	private final int getCapacityFor ( final int index )
	{
		int newSize = nextCapacity( data.length );

		while ( index >= newSize )
		{
			newSize = nextCapacity( newSize );
		}

		return newSize;
	}

}
