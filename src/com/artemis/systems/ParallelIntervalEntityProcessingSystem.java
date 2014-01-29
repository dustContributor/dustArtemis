package com.artemis.systems;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.utils.Bag;
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
		final Bag<Entity> bag = (Bag<Entity>) entities;
		
		final Spliterator<Entity> split = Spliterators.spliterator( bag.getData(), Spliterator.IMMUTABLE );
		
		final Stream<Entity> stream = StreamSupport.stream( split, true );
		
		stream.limit( bag.size() ).forEach( e -> process( e ) );
	}

}
