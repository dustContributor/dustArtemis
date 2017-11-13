package com.artemis;

import org.apache.lucene.util.OpenBitSet;

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
	protected ArrayComponentHandler ( Class<T> type, OpenBitSet componentBits, int wordsPerEntity, int index,
			int capacity )
	{
		super( type, componentBits, wordsPerEntity, index, capacity );
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
		// Mark as added.
		addedComponent( id );
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
		// Queue removal for later.
		enqueueRemoval( id );
		// Return removed item.
		return data[id];
	}

	@Override
	protected final void delete ( final int id )
	{
		data[id] = null;
	}

	private final boolean isInBounds ( final int index )
	{
		return (index > -1 && index < data.length);
	}

}
