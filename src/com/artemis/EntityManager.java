package com.artemis;

import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.IdAllocator;
import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;

/**
 * @author Arni Arent
 * @author dustContributor
 */
public final class EntityManager
{
	/* Various counters of entity state in this manager. */
	private long activeCount;
	private long addedCount;
	private long createdCount;
	private long deletedCount;

	/** Store for allocating entity IDs. */
	private final IdAllocator idStore;

	/** Bit set of all the alive entity IDs */
	private final OpenBitSet entities;
	/** Bit set with the 'disabled' status of all entities. */
	private final OpenBitSet disabled;

	EntityManager ()
	{
		// Fetch approximate live entities.
		int eSize = DAConstants.APPROX_LIVE_ENTITIES;

		this.entities = new OpenBitSet( eSize );
		this.disabled = new OpenBitSet( eSize );
		this.idStore = new IdAllocator();
	}

	final int createEntityInstance ()
	{
		final int eid = idStore.alloc();

		++createdCount;
		/*
		 * Guarantee 'entities' and 'disabled' can hold the Entity. This way we can
		 * avoid doing bound checks in other methods later.
		 */
		entities.ensureCapacity( eid + 1 );
		disabled.ensureCapacity( eid + 1 );
		// Track allocated ID.
		entities.fastSet( eid );

		return eid;
	}

	final void added ( final ImmutableIntBag entities )
	{
		final int eSize = entities.size();

		activeCount += eSize;
		addedCount += eSize;
	}

	final void enabled ( final ImmutableIntBag entities )
	{
		final int[] array = ((IntBag) entities).data();

		for ( int i = entities.size(); i-- > 0; )
		{
			disabled.fastClear( array[i] );
		}
	}

	final void disabled ( final ImmutableIntBag entities )
	{
		final int[] array = ((IntBag) entities).data();

		for ( int i = entities.size(); i-- > 0; )
		{
			disabled.fastSet( array[i] );
		}
	}

	final void deleted ( final ImmutableIntBag entities )
	{
		final int eSize = entities.size();
		final int[] eArray = ((IntBag) entities).data();

		activeCount -= eSize;
		deletedCount += eSize;

		for ( int i = eSize; i-- > 0; )
		{
			final int eid = eArray[i];

			this.entities.fastClear( eid );
			disabled.fastClear( eid );
			idStore.free( eid );
		}
	}

	/**
	 * Returns the destination bag with all the current alive entities that exist.
	 * 
	 * @param dest bag to hold the values.
	 * @return the bag passed as parameter holding all the entities that are
	 *         alive.
	 */
	public final IntBag activeEntities ( final IntBag dest )
	{
		entities.forEachSetBit( dest::add );
		return dest;
	}

	/**
	 * Check if this entity is active. Active means the entity is being actively
	 * processed.
	 * 
	 * @param entityId of the entity that will be checked.
	 * @return true if active, false if not.
	 */
	public final boolean isActive ( final int entityId )
	{
		return entities.fastGet( entityId );
	}

	/**
	 * Check if the specified entity is enabled.
	 * 
	 * @param entityId of the entity that will be checked.
	 * @return true if the entity is enabled, false if it is disabled.
	 */
	public final boolean isEnabled ( final int entityId )
	{
		return !disabled.fastGet( entityId );
	}

	/**
	 * Get how many entities are active in this world.
	 * 
	 * @return how many entities are currently active.
	 */
	public final long totalActive ()
	{
		return activeCount;
	}

	/**
	 * Get how many entities have been created in the world since start. Note: A
	 * created entity may not have been added to the world, thus created count is
	 * always equal or larger than added count.
	 * 
	 * @return how many entities have been created since start.
	 */
	public final long totalCreated ()
	{
		return createdCount;
	}

	/**
	 * Get how many entities have been added to the world since start.
	 * 
	 * @return how many entities have been added.
	 */
	public final long totalAdded ()
	{
		return addedCount;
	}

	/**
	 * Get how many entities have been deleted from the world since start.
	 * 
	 * @return how many entities have been deleted since start.
	 */
	public final long totalDeleted ()
	{
		return deletedCount;
	}

}
