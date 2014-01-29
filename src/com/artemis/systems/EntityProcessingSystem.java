package com.artemis.systems;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

/**
 * A typical entity system. Use this when you need to process entities possessing the
 * provided component types.
 * 
 * @author Arni Arent
 *
 */
public abstract class EntityProcessingSystem extends EntitySystem
{

	public EntityProcessingSystem ( final Aspect aspect )
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
	protected final void processEntities ( final ImmutableBag<Entity> entities )
	{
		final Entity[] entityArray = ( (Bag<Entity>) entities ).getData();
		final int size = entities.size();
		
		for ( int i = 0; i < size; ++i )
		{
			process( entityArray[i] );
		}
	}

	@Override
	protected boolean checkProcessing ()
	{
		return true;
	}

}
