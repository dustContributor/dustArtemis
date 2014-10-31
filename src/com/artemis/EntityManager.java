package com.artemis;

import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.IdAllocator;
import com.artemis.utils.ImmutableBag;

/**
 * @author Arni Arent
 */
@SuppressWarnings ( "hiding" )
public class EntityManager extends EntityObserver
{
	/** Bit set with the 'disabled' status of all entities. */
	private final OpenBitSet disabled;

	/* Various counters of entity state in this manager. */
	private long activeCount;
	private long addedCount;
	private long createdCount;
	private long deletedCount;

	/** Store for allocating entity IDs. */
	final IdAllocator idStore;
	/** Bag of all the entities. */
	final Bag<Entity> entities;

	EntityManager ()
	{
		// Fetch approximate live entities.
		int eSize = DAConstants.APPROX_LIVE_ENTITIES;

		this.entities = new Bag<>( Entity.class, eSize );
		this.disabled = new OpenBitSet( eSize );
		this.idStore = new IdAllocator();
	}

	public Entity createEntityInstance ()
	{
		return newEntityInstance( idStore.alloc() );
	}

	final Entity newEntityInstance ( final int eid )
	{
		++createdCount;
		/*
		 * Guarantee 'entities' and 'disabled' can hold the Entity. This way we
		 * can avoid doing bound checks in other methods later.
		 */
		entities.ensureCapacity( eid + 1 );
		disabled.ensureCapacity( eid + 1 );

		return new Entity( world, eid );
	}

	@Override
	public void added ( final ImmutableBag<Entity> entities )
	{
		final int eSize = entities.size();
		final Entity[] eArray = ((Bag<Entity>) entities).data();
		final Entity[] meArray = this.entities.data();

		activeCount += eSize;
		addedCount += eSize;

		for ( int i = eSize; i-- > 0; )
		{
			final Entity e = eArray[i];
			meArray[e.id] = e;
		}
	}

	@Override
	public void enabled ( final ImmutableBag<Entity> entities )
	{
		final Entity[] array = ((Bag<Entity>) entities).data();

		for ( int i = entities.size(); i-- > 0; )
		{
			disabled.fastClear( array[i].id );
		}
	}

	@Override
	public void disabled ( final ImmutableBag<Entity> entities )
	{
		final Entity[] array = ((Bag<Entity>) entities).data();

		for ( int i = entities.size(); i-- > 0; )
		{
			disabled.fastSet( array[i].id );
		}
	}

	@Override
	public void deleted ( final ImmutableBag<Entity> entities )
	{
		final int eSize = entities.size();
		final Entity[] eArray = ((Bag<Entity>) entities).data();
		final Entity[] meArray = this.entities.data();

		activeCount -= eSize;
		deletedCount += eSize;

		for ( int i = eSize; i-- > 0; )
		{
			final int eid = eArray[i].id;

			meArray[eid] = null;
			disabled.fastClear( eid );
			idStore.free( eid );
		}
	}

	/**
	 * Check if this entity is active. Active means the entity is being actively
	 * processed.
	 * 
	 * @param entityId of the entity that will be checked.
	 * @return true if active, false if not.
	 */
	public boolean isActive ( final int entityId )
	{
		return entities.getUnsafe( entityId ) != null;
	}

	/**
	 * Check if the specified entityId is enabled.
	 * 
	 * @param entityId of the entity that will be checked.
	 * @return true if the entity is enabled, false if it is disabled.
	 */
	public boolean isEnabled ( final int entityId )
	{
		return !disabled.fastGet( entityId );
	}

	/**
	 * Get a entity with this id.
	 * 
	 * @param entityId of the entity to retrieve.
	 * @return the retrieved entity.
	 */
	public Entity getEntity ( final int entityId )
	{
		return entities.getUnsafe( entityId );
	}

	/**
	 * Get how many entities are active in this world.
	 * 
	 * @return how many entities are currently active.
	 */
	public long getActiveEntityCount ()
	{
		return activeCount;
	}

	/**
	 * Get how many entities have been created in the world since start. Note: A
	 * created entity may not have been added to the world, thus created count
	 * is always equal or larger than added count.
	 * 
	 * @return how many entities have been created since start.
	 */
	public long getTotalCreated ()
	{
		return createdCount;
	}

	/**
	 * Get how many entities have been added to the world since start.
	 * 
	 * @return how many entities have been added.
	 */
	public long getTotalAdded ()
	{
		return addedCount;
	}

	/**
	 * Get how many entities have been deleted from the world since start.
	 * 
	 * @return how many entities have been deleted since start.
	 */
	public long getTotalDeleted ()
	{
		return deletedCount;
	}

}
