package com.artemis;

import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;

/**
 * Defined to group some shared functionality between {@link EntityWatcher} and
 * {@link EntitySystem}.
 *
 * @author dustContributor
 */
abstract class AbstractEntitySystem extends EntityObserver
{
	private final Aspect aspect;

	protected final IntBag inserted;
	protected final IntBag removed;

	protected final OpenBitSet activeBits;

	/**
	 * Creates an entity observer that uses the specified aspect as a matcher
	 * against entities.
	 *
	 * @param aspect to match against entities.
	 */
	public AbstractEntitySystem ( final Aspect aspect )
	{
		if ( aspect == null || aspect.isEmpty() )
		{
			final String cause = (aspect != null) ? "empty" : "null";
			throw new DustException( AbstractEntitySystem.class,
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
		final long[] cmpBits = world().componentManager().componentBits();
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
		final long[] cmpBits = world().componentManager().componentBits();
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
