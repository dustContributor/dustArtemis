package com.artemis;

import static com.artemis.DustException.enforceNonNull;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.apache.lucene.util.BitUtil;
import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.ImmutableBag;
import com.artemis.utils.IntBag;

/**
 * This class serves to manage a particular component type. Its used for both
 * retrieval and modification of components in entities.
 *
 * @author Arni Arent
 * @author dustContributor
 *
 * @param <T> type of component this handler will manage.
 */
public abstract class ComponentHandler<T extends Component>
{
	/** Bit flags indicating component ownership of entities. */
	private final OpenBitSet componentBits;
	/* How many 64 bit words are reserved for each entity. */
	private final int wordsPerEntity;
	/** Index of the component type that this handler uses. */
	private final int typeIndex;
	/** */
	private final IntBag removedIds;
	/** Storage for components. */
	protected T[] data;

	@SuppressWarnings( "unchecked" )
	protected ComponentHandler ( final Class<T> type, final OpenBitSet componentBits, final int wordsPerEntity,
			final int index, final int capacity )
	{
		if ( index < 0 )
		{
			throw new DustException( this, "index can't be negative!" );
		}

		if ( wordsPerEntity < 1 )
		{
			throw new DustException( this, "wordsPerEntity has to be positive!" );
		}

		this.data = (T[]) Array.newInstance( enforceNonNull( this, type, "type" ), capacity );
		this.componentBits = enforceNonNull( this, componentBits, "componentBits" );
		this.typeIndex = index;
		this.wordsPerEntity = wordsPerEntity;
		this.removedIds = new IntBag( 32 );
	}

	/**
	 * Retrieves the related component from the entity.
	 *
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 *
	 * @param id of the entity that has the component.
	 * @return the component that the entity has.
	 */
	public abstract T get ( final int id );

	/**
	 * Retrieves the related component from the entity.
	 *
	 * @param id of the entity that could have the component.
	 * @return the component that the entity has, or <code>null</code> if the
	 *         entity its outside the bounds of this handler.
	 */
	public abstract T getSafe ( final int id );

	/**
	 * Add a component to an entity.
	 *
	 * @param id of the entity to add the component to.
	 * @param component to add to the entity.
	 */
	public abstract void add ( final int id, final T component );

	/**
	 * Add a component to an entity.
	 *
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 *
	 * @param id of the entity to add the component to.
	 * @param component to add to the entity.
	 */
	public abstract void addUnsafe ( final int id, final T component );

	/**
	 * Removes the related component of the specified entity.
	 *
	 * It returns <code>null</code> if the index its outside bounds or if the item
	 * at the index was <code>null</code>.
	 *
	 * @param id of the entity to remove the component from.
	 * @return component that was removed from the entity.
	 */
	public abstract T removeSafe ( final int id );

	/**
	 * Removes the related component of the specified entity.
	 *
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 *
	 * @param id of the entity to remove the component from.
	 * @return component that was removed from the entity.
	 */
	public abstract T remove ( final int id );

	/**
	 * Checks if the entity has this type of component.
	 *
	 * @param id of the entity that could have the component.
	 * @return true if the entity has this component type, false if it doesn't.
	 */
	public final boolean has ( final int id )
	{
		return BitUtil.getRelative( componentBits.getBits(), typeIndex, id, wordsPerEntity );
	}

	/**
	 * Returns the backing array of components for this handler. This can be used
	 * for fast direct array indexing when doing bulk operations on entities. Just
	 * remember to NEVER modify the array directly, unless you know exactly what
	 * you're doing.
	 *
	 * @return array of components backing this handler.
	 */
	public final T[] data ()
	{
		return this.data;
	}

	/**
	 * Deletes the component of the specified entity. Doesn't notifies the
	 * {@link ComponentManager} of the change. Used internally for component
	 * cleanup of deleted entities.
	 *
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 *
	 * @param id of the entity with the component.
	 */
	protected abstract void delete ( final int id );

	/**
	 * Sets the appropriate component flag for the entity.
	 * 
	 * @param id of the entity.
	 */
	protected final void addedComponent ( final int id )
	{
		BitUtil.setRelative( componentBits.getBits(), typeIndex, id, wordsPerEntity );
	}

	/**
	 * Clears the appropriate component flag for the entity and queues the
	 * component removal for later.
	 * 
	 * @param id of the entity.
	 */
	protected final void enqueueRemoval ( final int id )
	{
		BitUtil.clearRelative( componentBits.getBits(), typeIndex, id, wordsPerEntity );

		final int ei = removedIds.binarySearch( id );

		if ( ei < 0 )
		{
			removedIds.insert( -ei - 1, id );
		}
	}

	/**
	 * Resizes the handler so it can contain the index provided.
	 *
	 * @param index that is expected the handler can contain.
	 */
	protected final void ensureCapacity ( final int index )
	{
		final int dataLen = data.length;

		if ( index >= dataLen )
		{
			data = Arrays.copyOf( data, ImmutableBag.getCapacityFor( index, dataLen ) );
		}
	}

	/**
	 * Internal method to cleanup any components removed since the last to this
	 * method.
	 */
	final void clean ()
	{
		final int size = removedIds.size();
		final int[] ids = removedIds.data();
		removedIds.setSize( 0 );

		for ( int i = 0; i < size; ++i )
		{
			delete( ids[i] );
		}
	}
}
