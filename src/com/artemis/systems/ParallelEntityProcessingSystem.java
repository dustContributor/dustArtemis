package com.artemis.systems;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;

/**
 * A parallel EntitySystem implemented with Java 8 parallelStream().
 * 
 * @author dustContributor
 *
 */
public abstract class ParallelEntityProcessingSystem extends EntitySystem
{

	public ParallelEntityProcessingSystem ( final Aspect aspect )
	{
		super( aspect );
	}

	/**
	 * Process a entity this system is interested in.
	 * 
	 * @param e
	 *            the entity to process.
	 */
	protected abstract void process ( final Entity e );

	@Override
	protected void processEntities ( final ImmutableBag<Entity> entities )
	{
		entities.parallelStream().forEach( e -> process( e ) );
	}

	@Override
	protected boolean checkProcessing ()
	{
		return true;
	}

}
