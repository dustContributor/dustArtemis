package com.artemis;

import java.util.Arrays;

import org.apache.lucene.util.BitUtil;
import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;
import com.artemis.utils.MutableBitIterator;

/**
 * This entity group will track an "active" list of entities that match a
 * certain {@link EntityFilter} and when they get added/removed into/from the
 * group.
 *
 * @author dustContributor
 */
public final class EntityGroup
{
	private final ComponentManager cm;

	private final int wordCount;
	// Bit sets marking the components this filter is interested in.
	private final long[] all;
	private final long[] none;
	private final long[] any;

	private final IntBag addedEntities;
	private final IntBag removedEntities;
	private final IntBag activeEntities;

	private final OpenBitSet activeEntityFlags;

	/**
	 * Creates an entity observer that uses the specified aspect as a matcher
	 * against entities.
	 *
	 * @param filter to match against entities.
	 * @param cm component manager, used for entity matching operations.
	 */
	EntityGroup ( final EntityFilter filter, final ComponentManager cm )
	{
		DustException.enforceNonNull( this, filter, "filter" );
		DustException.enforceNonNull( this, cm, "cm" );

		if ( filter.isEmpty() )
		{
			throw new DustException( this, "Can't use an empty filter!" );
		}

		final long[] all = filter.allBits( cm );
		final long[] none = filter.noneBits( cm );
		final long[] one = filter.anyBits( cm );

		// If any of the bit sets is empty, do not store them.
		this.all = BitUtil.isEmpty( all ) ? null : all;
		this.none = BitUtil.isEmpty( none ) ? null : none;
		this.any = BitUtil.isEmpty( one ) ? null : one;

		this.wordCount = cm.wordsPerEntity();

		this.cm = cm;

		// Fetch entity amount per observer.
		final int actSize = DAConstants.APPROX_ENTITIES_PER_SYSTEM;
		this.activeEntityFlags = new OpenBitSet( actSize );
		this.addedEntities = new IntBag( actSize / 2 );
		this.removedEntities = new IntBag( actSize / 2 );
		this.activeEntities = new IntBag( actSize );
	}

	/**
	 * Returns list of entities that matched the filter in this group since last
	 * update. This list will be maintained internally so you can keep a reference
	 * to it directly.
	 *
	 * @return entities that were added into this filter.
	 */
	public final ImmutableIntBag added ()
	{
		return this.addedEntities;
	}

	/**
	 * Returns a list of entities that didn't match the filter in this group since
	 * last update. This list will be maintained internally so you can keep a
	 * reference to it directly.
	 *
	 * @return entities that were removed from this group.
	 */
	public final ImmutableIntBag removed ()
	{
		return this.removedEntities;
	}

	/**
	 * Returns a list of all the currently active matching entities in this group.
	 * This list will be maintained internally so you can keep a reference to it
	 * directly.
	 *
	 * @return entities that are active in this group.
	 */
	public final ImmutableIntBag active ()
	{
		return this.activeEntities;
	}

	final void added ( final ImmutableIntBag entities )
	{
		final OpenBitSet acBits = activeEntityFlags;
		final IntBag insrts = addedEntities;
		final long[] cmpBits = cm.componentBits();
		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final int eid = array[i];

			if ( isInteresting( eid, cmpBits ) )
			{
				insrts.add( eid );
				acBits.set( eid );
			}
		}
	}

	final void mutated ( final ImmutableIntBag entities )
	{
		final OpenBitSet acBits = activeEntityFlags;
		final IntBag insrts = addedEntities;
		final IntBag removs = removedEntities;
		final long[] cmpBits = cm.componentBits();
		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final int eid = array[i];
			// First bit for 'contains'.
			final int contains = acBits.getBit( eid );
			// Second bit for 'interesting'.
			final int interesting = isInteresting( eid, cmpBits ) ? 0b10 : 0b0;

			switch ( contains | interesting )
			{
				case 0b01:
				{
					// Not interesting and observer does contains. Remove.
					removs.add( eid );
					acBits.fastClear( eid );
					continue;
				}
				case 0b10:
				{
					// Interesting and observer doesn't contains. Add.
					insrts.add( eid );
					acBits.set( eid );
					continue;
				}
				default:
					// Otherwise do nothing.
					continue;
			}
		}
	}

	final void removed ( final ImmutableIntBag entities )
	{
		// Fetch bits of active entities.
		final OpenBitSet acBits = activeEntityFlags;
		final IntBag removs = removedEntities;
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

	/**
	 * Rebuilds active entity list if something changed.
	 */
	final void updateActiveList ()
	{
		// Now start checking of something actually changed.
		final int minIdRemoved = minimumIdOf( removedEntities );
		final int minIdInserted = minimumIdOf( addedEntities );
		// Compute real min modified entity id.
		final int minId = Math.min( minIdRemoved, minIdInserted );
		// Using max value as flag for no changes.
		if ( minId < Integer.MAX_VALUE )
		{
			// And rebuild this observer's entity list.
			rebuildEntityList( minId );
		}
	}

	/**
	 * Clears the added/removed entity lists in this group.
	 */
	final void clear ()
	{
		this.addedEntities.setSize( 0 );
		this.removedEntities.setSize( 0 );
	}

	/**
	 * Returns the minimum affected entity id found.
	 *
	 * @param entities to process.
	 * @return minimum affected entity id, or {@link Integer#MAX_VALUE} if there
	 *         was nothing in the bag.
	 */
	private static final int minimumIdOf ( final IntBag entities )
	{
		// Using max value as flag for no changes.
		int minId = Integer.MAX_VALUE;

		final int[] data = entities.data();
		// Check changes in removed entities.
		for ( int i = entities.size(); i-- > 0; )
		{
			final int eid = data[i];
			if ( eid < minId )
			{
				minId = eid;
			}
		}
		// Return min entity id that was modified.
		return minId;
	}

	private final void rebuildEntityList ( final int startId )
	{
		final int newSize = activeEntityFlags.cardinality();
		final int oldSize = activeEntities.size();
		activeEntities.setSize( newSize );
		activeEntities.ensureCapacity( newSize );

		final int[] ids = activeEntities.data();

		final MutableBitIterator mbi = new MutableBitIterator( activeEntityFlags.getBits() );
		mbi.selectIndex( startId );

		int idi = Arrays.binarySearch( ids, 0, oldSize, startId );
		// Fix index if Entity ID isn't on the list yet.
		idi = Math.max( idi, -idi - 1 );

		/*
		 * From the found position, rebuild the entity ID list.
		 *
		 * NOTE: It seems explicitly checking for j < ids.length helps the JIT a
		 * bit, j wont ever be bigger than ids.length, but probably the JIT can't
		 * infer that and checks every loop if it has to raise an out of bounds
		 * exception.
		 */
		int id;

		while ( idi < ids.length && (id = mbi.nextSetBit()) > -1 )
		{
			ids[idi++] = id;
		}
	}

	/**
	 * Checks if the components bits are interesting to this {@link EntityGroup}.
	 *
	 * @param id of the entity to check
	 *
	 * @param bits to check
	 * @return 'true' if it's interesting, 'false' otherwise.
	 */
	private final boolean isInteresting ( final int id, final long[] bits )
	{
		final int start = id * wordCount;

		return checkNone( bits, start )
				& checkAny( bits, start )
				& checkAll( bits, start );
	}

	/**
	 * Checks if the provided bit set has at none of the bits set in none bits.
	 *
	 * @param bits to check.
	 * @return true if the two bit sets don't intersect, false otherwise.
	 */
	private final boolean checkNone ( final long[] bits, final int offset )
	{
		if ( none == null )
		{
			return true;
		}
		// Reject entity if it has any of the bits.
		return !BitUtil.intersects( none, 0, bits, offset, wordCount );
	}

	/**
	 * Checks if the provided bit set has at least one of the bits set in any
	 * bits.
	 *
	 * @param bits to check.
	 * @return true if the two bit sets intersect, false otherwise.
	 */
	private final boolean checkAny ( final long[] bits, final int offset )
	{
		if ( any == null )
		{
			return true;
		}
		// Reject entity if it has none the bits.
		return BitUtil.intersects( any, 0, bits, offset, wordCount );
	}

	/**
	 * Checks if the provided bit set has all of the specified bits in all bits.
	 *
	 * @param bits to check.
	 * @return true if it has all the bits, false otherwise.
	 */
	private final boolean checkAll ( final long[] bits, final int offset )
	{
		if ( all == null )
		{
			return true;
		}
		/*
		 * If the intersection of the two bit sets is equal to the all bits, it
		 * means the passed bit set has all of all bits set. Otherwise, reject the
		 * entity.
		 */
		return BitUtil.intersectionEqual( all, 0, bits, offset, wordCount );
	}
}
