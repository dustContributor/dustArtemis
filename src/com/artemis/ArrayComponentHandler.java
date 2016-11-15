package com.artemis;

import com.artemis.utils.ImmutableBag;

/**
 * This component handlers is implemented with a single element array, indexed
 * directly.
 *
 * @author dustContributor
 *
 * @param <T> type of component this handler will manage.
 */
public final class ArrayComponentHandler<T extends Component> extends ComponentHandler<T>
{
	/**
	 * Constructs an empty handler with an initial capacity of
	 * {@link ImmutableBag#DEFAULT_CAPACITY}. Uses Array.newInstance() to
	 * instantiate a backing array of the proper type.
	 *
	 * @param type of the backing array.
	 * @param owner of this component handler.
	 * @param index of the component type this handler will manage in the
	 *          component manager.
	 */
	ArrayComponentHandler ( final Class<T> type, final ComponentManager owner, final int index )
	{
		super( type, owner, index, ImmutableBag.DEFAULT_CAPACITY );
	}

	ArrayComponentHandler ( final Class<T> type, final ComponentManager owner, final int index, final int capacity )
	{
		super( type, owner, index, capacity );
	}

	@Override
	public final T get ( final int id )
	{
		return data[id];
	}

	@Override
	public final T getSafe ( final int id )
	{
		if ( isInBounds( id ) )
		{
			return get( id );
		}
		return null;
	}

	@Override
	public final void add ( final int id, final T component )
	{
		if ( id < 0 )
		{
			return;
		}
		ensureCapacity( id );
		addUnsafe( id, component );
	}

	@Override
	public final void addUnsafe ( final int id, final T component )
	{
		data[id] = component;
		cm.notifyAddedComponent( id, typeIndex );
	}

	@Override
	public final T removeSafe ( final int id )
	{
		if ( isInBounds( id ) )
		{
			return remove( id );
		}

		return null;
	}

	@Override
	public final T remove ( final int id )
	{
		cm.notifyRemovedComponent( id, typeIndex );
		// Item ref copy.
		final T item = data[id];
		// Null item.
		data[id] = null;
		// Return removed item.
		return item;
	}

	@Override
	public final boolean has ( final int id )
	{
		return isInBounds( id ) && data[id] != null;
	}

	private final boolean isInBounds ( final int index )
	{
		return (index > -1 && index < data.length);
	}
}
