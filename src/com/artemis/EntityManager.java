package com.artemis;

import java.util.BitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.IdPool;
import com.artemis.utils.ImmutableBag;

/**
 * @author Arni Arent
 */
public class EntityManager extends Manager
{
	private final Bag<Entity> entities;
	private final BitSet disabled;

	private int active;
	private long added;
	private long created;
	private long deleted;

	private final IdPool idPool;

	public EntityManager ()
	{
		entities = new Bag<>( Entity.class );
		disabled = new BitSet();
		idPool = new IdPool();
	}

	protected Entity createEntityInstance ()
	{
		final Entity e = new Entity( world, idPool.getId() );
		created++;
		return e;
	}

	@Override
	public void added ( ImmutableBag<Entity> entities )
	{
		final int size = entities.size();
		int maxID = 0;
		
		for ( int i = 0; i < size; ++i )
		{
			int eid = entities.getUnsafe( i ).id;
			maxID = ( maxID > eid ) ? maxID : eid;
		}
		
		this.entities.ensureCapacity( maxID + 1 );
		
		active += size;
		added += size;
		
		for ( int i = 0; i < size; ++i )
		{
			Entity e = entities.getUnsafe( i );
			this.entities.setUnsafe( e.id, e );
		}
	}
	
//	@Override
//	public void added ( final Entity e )
//	{
//		active++;
//		added++;
//		entities.set( e.id, e );
//	}
	
	@Override
	public void enabled ( ImmutableBag<Entity> entities )
	{
		for ( int i = entities.size(); i-- > 0; )
		{
			disabled.clear( entities.getUnsafe( i ).id );
		}
	}

//	@Override
//	public void enabled ( final Entity e )
//	{
//		disabled.clear( e.id );
//	}
	
	@Override
	public void disabled ( ImmutableBag<Entity> entities )
	{
		for ( int i = entities.size(); i-- > 0; )
		{
			disabled.set( entities.getUnsafe( i ).id );
		}
	}

//	@Override
//	public void disabled ( final Entity e )
//	{
//		disabled.set( e.id );
//	}
	
	@Override
	public void deleted ( ImmutableBag<Entity> entities )
	{
		final int size = entities.size();
		int maxID = 0;
		
		for ( int i = 0; i < size; ++i )
		{
			int eid = entities.getUnsafe( i ).id;
			maxID = ( maxID > eid ) ? maxID : eid;
		}
		
		this.entities.ensureCapacity( maxID + 1 );
		
		active -= size;
		deleted += size;
		
		for ( int i = 0; i < size; ++i )
		{
			Entity e = entities.getUnsafe( i );
			int eid = e.id;
			this.entities.setUnsafe( eid, null );
			disabled.clear( eid );
			idPool.putId( eid );
		}
	}

//	@Override
//	public void deleted ( final Entity e )
//	{
//		final int id = e.id;
//
//		entities.set( id, null );
//		disabled.clear( id );
//		idPool.putId( id );
//
//		active--;
//		deleted++;
//	}

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
	 * @param entityId
	 * @return the entity
	 */
	protected Entity getEntity ( final int entityId )
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
