package com.artemis;

import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

/**
 * @author Dust Contributor
 */
public final class PooledEntityManager extends EntityManager
{
	private final Bag<Entity> entityStore;

	PooledEntityManager ()
	{
		super();
		entityStore = new Bag<>( Entity.class, 16 );
	}

	@Override
	public Entity createEntityInstance ()
	{
		final int eid = idStore.alloc();

		if ( entityStore.size() > 0 )
		{
			Entity e = entityStore.removeLastUnsafe();
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
		final int newSize = entityStore.size() + eSize;
		final int poolSize = Math.min( newSize, DAConstants.MAX_POOLED_ENTITIES );

		final Entity[] eArray = ((Bag<Entity>) entities).data();
		entityStore.addAll( eArray, eSize - (newSize - poolSize) );
	}

	public int getTotalPooled ()
	{
		return entityStore.size();
	}

}
