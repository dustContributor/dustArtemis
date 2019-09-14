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
	protected ArrayComponentHandler ( final Class<T> type, final OpenBitSet componentBits, final int wordsPerEntity,
			final int index,
			final int capacity )
	{
		super( type, componentBits, wordsPerEntity, index, capacity );
	}

	@Override
	public final T get ( final int id )
	{
		return data[id];
	}

	@Override
	protected final void delete ( final int id )
	{
		data[id] = null;
	}

	@Override
	protected final void set ( final int id, final T component )
	{
		data[id] = component;
	}

	@Override
	protected final void ensureCapacity ( final int id )
	{
		resize( id );
	}

}
