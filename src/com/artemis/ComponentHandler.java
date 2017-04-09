package com.artemis;

import java.lang.reflect.Array;
import java.util.Arrays;

import com.artemis.utils.ImmutableBag;

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
	protected T[] data;
	/** Index of the component type that this handler uses. */
	protected final int typeIndex;
	/** Component manager owner of this handler. */
	protected final ComponentManager cm;

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
	protected ComponentHandler ( final Class<T> type, final ComponentManager owner, final int index )
	{
		this( type, owner, index, ImmutableBag.DEFAULT_CAPACITY );
	}

	@SuppressWarnings( "unchecked" )
	protected ComponentHandler ( final Class<T> type, final ComponentManager owner, final int index, final int capacity )
	{
		DustException.enforceNonNull( this, type, "type" );

		if ( index < 0 )
		{
			throw new DustException( this, "index can't be negative!" );
		}

		// this.cm = DustException.enforceNonNull( this, owner, "owner" );
		this.cm = owner;
		this.data = (T[]) Array.newInstance( type, capacity );
		this.typeIndex = index;
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
	public abstract boolean has ( final int id );

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

}
