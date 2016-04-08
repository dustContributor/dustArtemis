package com.artemis;

import java.util.function.BiConsumer;

import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.ImmutableIntBag;
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
public abstract class EntityWatcher extends EntityObserver
{
	private final Aspect aspect;

	private final IntBag inserted;
	private final IntBag removed;

	private final OpenBitSet activeBits;

	/**
	 * Creates an entity observer that builds an {@link Aspect} instance using the
	 * passed {@link Aspect.Builder}, and uses that Aspect as a matcher against
	 * entities.
	 *
	 * @param builder to create an {@link Aspect} to match against entities.
	 */
	public EntityWatcher ( final Aspect.Builder builder )
	{
		this( DustException.enforceNonNull( EntityWatcher.class, builder, "builder" ).build() );
	}

	/**
	 * Creates an entity observer that uses the specified aspect as a matcher
	 * against entities.
	 *
	 * @param aspect to match against entities.
	 */
	public EntityWatcher ( final Aspect aspect )
	{
		if ( aspect == null || aspect.isEmpty() )
		{
			final String cause = (aspect != null) ? "empty" : "null";
			throw new DustException( EntityWatcher.class,
					"Cant pass an Aspect that is " + cause + '!' +
							" Extend EntityObserver if you want an observer" +
							" that doesn't processes any entity!" );
		}
		// Fetch entity amount per observer.
		final int actSize = DAConstants.APPROX_ENTITIES_PER_SYSTEM;
		this.aspect = aspect;
		this.activeBits = new OpenBitSet( actSize );
		this.inserted = new IntBag( actSize / 2 );
		this.removed = new IntBag( actSize / 2 );
	}

	/**
	 * Called only if the observer received matching entities, e.g. created or a
	 * component was added to it.
	 *
	 * @param entities that were inserted into this observer.
	 */
	protected void inserted ( final ImmutableIntBag entities )
	{
		// Empty method.
	}

	/**
	 * Called only if the observer got any entity removed from itself, e.g. entity
	 * deleted or had one of it's components removed.
	 *
	 * @param entities that were removed from this observer.
	 */
	protected void removed ( final ImmutableIntBag entities )
	{
		// Empty method.
	}

	@Override
	public final void added ( final ImmutableIntBag entities )
	{
		addAll( entities );
	}

	@Override
	public final void changed ( final ImmutableIntBag entities )
	{
		checkAll( entities );
	}

	@Override
	public final void deleted ( final ImmutableIntBag entities )
	{
		removeAll( entities );
	}

	@Override
	public final void disabled ( final ImmutableIntBag entities )
	{
		removeAll( entities );
	}

	@Override
	public final void enabled ( final ImmutableIntBag entities )
	{
		addAll( entities );
	}

	@Override
	final void processModifiedEntities ()
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

	private final void removeAll ( final ImmutableIntBag entities )
	{
		// Fetch bits of active entities.
		final OpenBitSet acBits = activeBits;
		final IntBag removs = removed;
		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final int eid = array[i];

			if ( acBits.get( eid ) )
			{
				removs.add( eid );
				acBits.fastClear( eid );
			}
		}
	}

	private final void addAll ( final ImmutableIntBag entities )
	{
		final Aspect asp = aspect;
		final OpenBitSet acBits = activeBits;
		final IntBag insrts = inserted;
		final long[] cmpBits = world.componentManager().componentBits();
		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final int eid = array[i];

			if ( asp.isInteresting( eid, cmpBits ) )
			{
				insrts.add( eid );
				acBits.set( eid );
			}
		}
	}

	/**
	 * Adds entity if the observer is interested in it and hasn't been added
	 * before.
	 *
	 * Removes entity from observer if its not interesting and it has been added
	 * before.
	 *
	 * @param entities to check.
	 */
	private final void checkAll ( final ImmutableIntBag entities )
	{
		final Aspect asp = aspect;
		// Fetch bits of active entities.
		final OpenBitSet acBits = activeBits;
		final IntBag insrts = inserted;
		final IntBag removs = removed;
		final long[] cmpBits = world.componentManager().componentBits();
		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final int eid = array[i];
			// Second bit for 'contains'.
			int flags = acBits.getBit( eid ) << 1;
			// First bit for 'interesting'.
			flags |= asp.isInteresting( eid, cmpBits ) ? 0b1 : 0b0;

			switch ( flags )
			{
				case 0b01:
				{
					// Interesting and observer doesn't contains. Insert.
					insrts.add( eid );
					acBits.set( eid );
					continue;
				}
				case 0b10:
				{
					// Not interesting and observer does contains. Remove.
					removs.add( eid );
					acBits.fastClear( eid );
					continue;
				}
				default:
					// Otherwise do nothing.
					continue;
			}
		}
	}

}
