package com.artemis.systems;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.utils.ImmutableBag;

/**
 * Parallel version of IntervalEntityProcessingSystem.
 * 
 * @author The Chubu
 * 
 */
public abstract class ParallelIntervalEntityProcessingSystem extends IntervalEntitySystem
{

	public ParallelIntervalEntityProcessingSystem ( final Aspect aspect, final float interval )
	{
		super( aspect, interval );
	}

	/**
	 * Process a entity this system is interested in.
	 * 
	 * @param e
	 *            the entity to process.
	 */
	protected abstract void process ( Entity e );

	@Override
	protected void processEntities ( final ImmutableBag<Entity> entities )
	{
		entities.parallelStream().forEach( e -> process( e ) );
	}

}
