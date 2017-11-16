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
	/** */
	private final IntBag addedIds;
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
		this.addedIds = new IntBag( 32 );
	}

	/**
	 * Retrieves the related component from the entity.
	 *
	 * <p>
	 * <b>UNSAFE: Not required to do bounds checks.</b>
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
	public final T getSafe ( final int id )
	{
		return has( id ) ? get( id ) : null;
	}

	/**
	 * Add a component to an entity.
	 *
	 * @param id of the entity to add the component to.
	 * @param component to add to the entity.
	 */
	public final void add ( final int id, final T component )
	{
		if ( id < 0 )
		{
			return;
		}
		addedComponent( id );
		ensureCapacity( id );
		set( id, component );
	}

	/**
	 * Removes the related component of the specified entity.
	 *
	 * It returns <code>null</code> if the index its outside bounds or if the item
	 * at the index was <code>null</code>.
	 *
	 * @param id of the entity to remove the component from.
	 * @return component that was removed from the entity.
	 */
	public final T removeSafe ( final int id )
	{
		return has( id ) ? remove( id ) : null;
	}

	/**
	 * Removes the related component of the specified entity.
	 *
	 * <p>
	 * <b>UNSAFE: Not required to do bounds checks.</b>
	 * </p>
	 *
	 * @param id of the entity to remove the component from.
	 * @return component that was removed from the entity.
	 */
	public final T remove ( final int id )
	{
		// Will remove later.
		removedComponent( id );
		// Return removed component.
		return get( id );
	}

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
	 * Sets a component to an entity.
	 *
	 * <p>
	 * <b>UNSAFE: Not required to do bounds checks.</b>
	 * </p>
	 *
	 * @param id of the entity to set the component to.
	 * @param component to set to the entity.
	 */
	protected abstract void set ( final int id, final T component );

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

	protected abstract void ensureCapacity ( final int id );

	/**
	 * Resizes the handler so it can contain the index provided.
	 *
	 * @param index that is expected the handler can contain.
	 */
	protected final void resize ( final int index )
	{
		final int len = data.length;
		if ( index >= len )
		{
			data = Arrays.copyOf( data, ImmutableBag.getCapacityFor( index, len ) );
		}
	}

	/**
	 * Internal method that marks all the added/removed components from the entity
	 * bits.
	 */
	final void markChanges ()
	{
		final long[] bits = componentBits.getBits();
		final int[] addedIds = this.addedIds.data();
		final int addedSize = this.addedIds.size();

		for ( int i = 0; i < addedSize; ++i )
		{
			BitUtil.setRelative( bits, typeIndex, addedIds[i], wordsPerEntity );
		}

		final int[] removedIds = this.removedIds.data();
		final int removedSize = this.removedIds.size();

		for ( int i = 0; i < removedSize; ++i )
		{
			BitUtil.clearRelative( bits, typeIndex, removedIds[i], wordsPerEntity );
		}
	}

	/**
	 * Internal method to cleanup all removed components and reset added/removed
	 * tracking lists.
	 */
	final void cleanup ()
	{
		final int size = removedIds.size();
		final int[] ids = removedIds.data();

		removedIds.setSize( 0 );
		addedIds.setSize( 0 );

		for ( int i = 0; i < size; ++i )
		{
			delete( ids[i] );
		}
	}

	/**
	 * Sets the appropriate component flag for the entity.
	 *
	 * @param id of the entity.
	 */
	private final void addedComponent ( final int id )
	{
		final int ei = addedIds.binarySearch( id );

		if ( ei < 0 )
		{
			addedIds.insert( -ei - 1, id );
		}
	}

	/**
	 * Clears the appropriate component flag for the entity and queues the
	 * component removal for later.
	 *
	 * @param id of the entity.
	 */
	private final void removedComponent ( final int id )
	{
		final int ei = removedIds.binarySearch( id );

		if ( ei < 0 )
		{
			removedIds.insert( -ei - 1, id );
		}
	}

}
