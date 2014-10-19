package com.artemis;

import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

/**
 * This kind of entity manager will pool entity instances instead of
 * deleting/creating them as needed.
 * 
 * @author Dust Contributor
 */
public final class PooledEntityManager extends EntityManager
{
	/** Store of unused entities. */
	private final Bag<Entity> unusedStore;

	PooledEntityManager ()
	{
		super();
		unusedStore = new Bag<>( Entity.class, 16 );
	}

	@Override
	public Entity createEntityInstance ()
	{
		final int eid = idStore.alloc();

		if ( unusedStore.size() > 0 )
		{
			Entity e = unusedStore.removeLastUnsafe();
			e.id = eid;
			return e;
		}

		return newEntityInstance( eid );
	}

	@Override
	public void deleted ( final ImmutableBag<Entity> entities )
	{
		super.deleted( entities );

		final int eSize = entities.size();
		final int newSize = unusedStore.size() + eSize;
		final int poolSize = Math.min( newSize, DAConstants.MAX_POOLED_ENTITIES );

		final Entity[] eArray = ((Bag<Entity>) entities).data();
		unusedStore.addAll( eArray, eSize - (newSize - poolSize) );
	}

	/**
	 * Returns the total of entities that are retained in the pool in this
	 * moment.
	 * 
	 * @return count of entities that are retained in the pool.
	 */
	public int getTotalPooled ()
	{
		return unusedStore.size();
	}

}
