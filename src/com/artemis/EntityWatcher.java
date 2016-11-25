package com.artemis;

import java.util.function.BiConsumer;

import com.artemis.utils.IntBag;

/**
 * This kind of observer will notify you when ntities that match a certain
 * {@link Aspect} are inserted or removed. It doesn't tracks a list of all those
 * entities though, use {@link EntitySystem} if you want that.
 *
 * This observer is more lightweight than {@link EntitySystem} so you can use it
 * when you don't need the additional process of keeping track and ordering of
 * the active entity list, which consume time and memory.
 *
 * @author dustContributor
 */
public abstract class EntityWatcher extends AbstractEntitySystem
{
	/**
	 * Creates an entity observer that uses the specified aspect as a matcher
	 * against entities.
	 *
	 * @param aspect to match against entities.
	 */
	public EntityWatcher ( final Aspect aspect )
	{
		super( aspect );
	}

	@Override
	protected final void processModifiedEntities ()
	{
		// Now start checking of something actually changed.
		processIfModified( removed, EntityWatcher::removed );
		processIfModified( inserted, EntityWatcher::inserted );
	}

	/**
	 * If there is something in the passed entities bag, it will call the
	 * operation on the bag then clear it.
	 *
	 * @param entities to process.
	 * @param operation to make on the entities.
	 */
	private final void processIfModified (
			final IntBag entities,
			final BiConsumer<EntityWatcher, IntBag> operation )
	{
		if ( entities.isEmpty() )
		{
			return;
		}
		// Let the observer process the events.
		operation.accept( this, entities );
		// Clear the container.
		entities.setSize( 0 );
	}

}
