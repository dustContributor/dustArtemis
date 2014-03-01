package com.artemis;

import java.util.BitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.IntStack;

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

	private final IntStack idStack;
	private int nextAvailableId;

	public EntityManager ()
	{
		entities = new Bag<>( Entity.class );
		disabled = new BitSet();
		idStack = new IntStack( 512 );
	}

	@Override
	protected void initialize ()
	{
		// Empty method.
	}

	protected Entity createEntityInstance ()
	{
		final Entity e = new Entity( world, checkOutId() );
		created++;
		return e;
	}

	@Override
	public void added ( final Entity e )
	{
		active++;
		added++;
		entities.set( e.id, e );
	}

	@Override
	public void enabled ( final Entity e )
	{
		disabled.clear( e.id );
	}

	@Override
	public void disabled ( final Entity e )
	{
		disabled.set( e.id );
	}

	@Override
	public void deleted ( final Entity e )
	{
		entities.set( e.id, null );

		disabled.clear( e.id );

		checkInId( e.id );

		active--;
		deleted++;
	}

	/**
	 * Check if this entity is active. Active means the entity is being actively
	 * processed.
	 * 
	 * @param entityId
	 * @return true if active, false if not.
	 */
	public boolean isActive ( final int entityId )
	{
		return entities.getUnsafe( entityId ) != null;
	}

	/**
	 * Check if the specified entityId is enabled.
	 * 
	 * @param entityId
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
	
	private final int checkOutId ()
	{
		if ( idStack.size() > 0 )
		{
			return idStack.unsafePop();
		}
		
		return nextAvailableId++;
	}

	private final void checkInId ( final int id )
	{
		idStack.push( id );
	}

}
