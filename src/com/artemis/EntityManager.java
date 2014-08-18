package com.artemis;

import java.util.BitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.IdAllocator;
import com.artemis.utils.ImmutableBag;

/**
 * @author Arni Arent
 */
@SuppressWarnings ( "hiding" )
public final class EntityManager extends Manager
{
	private final Bag<Entity> entities;
	private final BitSet disabled;

	private int active;
	private long added;
	private long created;
	private long deleted;

	private final IdAllocator idStore;

	EntityManager ()
	{
		entities = new Bag<>( Entity.class );
		disabled = new BitSet();
		idStore = new IdAllocator();
	}

	public Entity createEntityInstance ()
	{
		++created;
		return new Entity( world, idStore.alloc() );
	}

	@Override
	public void added ( final ImmutableBag<Entity> entities )
	{
		final int size = entities.size();
		int maxID = 0;
		
		for ( int i = 0; i < size; ++i )
		{
			final int eid = entities.getUnsafe( i ).id;
			maxID = ( maxID > eid ) ? maxID : eid;
		}
		
		this.entities.ensureCapacity( maxID + 1 );
		final Entity[] array = ((Bag<Entity>) entities).data();
		
		active += size;
		added += size;
		
		for ( int i = 0; i < size; ++i )
		{
			final Entity e = array[i];
			this.entities.setUnsafe( e.id, e );
		}
	}
	
	@Override
	public void enabled ( final ImmutableBag<Entity> entities )
	{
		final Entity[] array = ((Bag<Entity>) entities).data();
		final int size = entities.size();
		
		for ( int i = 0; i < size; ++i )
		{
			disabled.clear( array[i].id );
		}
	}
	
	@Override
	public void disabled ( final ImmutableBag<Entity> entities )
	{
		final Entity[] array = ((Bag<Entity>) entities).data();
		final int size = entities.size();
		
		for ( int i = 0; i < size; ++i )
		{
			disabled.set( array[i].id );
		}
	}
	
	@Override
	public void deleted ( final ImmutableBag<Entity> entities )
	{
		final int size = entities.size();
		int maxID = 0;
		
		for ( int i = 0; i < size; ++i )
		{
			final int eid = entities.getUnsafe( i ).id;
			maxID = ( maxID > eid ) ? maxID : eid;
		}
		
		this.entities.ensureCapacity( maxID + 1 );
		final Entity[] array = ((Bag<Entity>) entities).data();
		
		active -= size;
		deleted += size;
		
		for ( int i = 0; i < size; ++i )
		{
			final Entity e = array[i];
			final int eid = e.id;
			this.entities.setUnsafe( eid, null );
			disabled.clear( eid );
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
		return !disabled.get( entityId );
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
	public int getActiveEntityCount ()
	{
		return active;
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
		return created;
	}

	/**
	 * Get how many entities have been added to the world since start.
	 * 
	 * @return how many entities have been added.
	 */
	public long getTotalAdded ()
	{
		return added;
	}

	/**
	 * Get how many entities have been deleted from the world since start.
	 * 
	 * @return how many entities have been deleted since start.
	 */
	public long getTotalDeleted ()
	{
		return deleted;
	}

}
