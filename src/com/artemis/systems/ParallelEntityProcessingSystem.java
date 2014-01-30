package com.artemis.systems;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

/**
 * A parallel EntitySystem implemented with Java 8 parallelStream().
 * 
 * @author The Chubu
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
		final Bag<Entity> bag = (Bag<Entity>) entities;
		
		final Spliterator<Entity> split = Spliterators.spliterator( bag.data(), Spliterator.IMMUTABLE );
		
		final Stream<Entity> stream = StreamSupport.stream( split, true );
		
		stream.limit( bag.size() ).forEach( e -> process( e ) );
	}

	@Override
	protected boolean checkProcessing ()
	{
		return true;
	}

}
