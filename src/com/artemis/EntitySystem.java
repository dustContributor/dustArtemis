package com.artemis;

import java.util.Arrays;

import com.artemis.utils.Bag;
import com.artemis.utils.ClassIndexer;
import com.artemis.utils.ImmutableBag;

/**
 * The most raw entity system. It should not typically be used, but you can
 * create your own entity system handling by extending this. It is recommended
 * that you use the other provided entity system implementations.
 * 
 * @author Arni Arent
 * 
 */
public abstract class EntitySystem extends EntityObserver
{
	private final int index;
	private final Bag<Entity> actives;
	private final Aspect aspect;

	/**
	 * Creates an entity system that uses the specified aspect as a matcher
	 * against entities.
	 * 
	 * @param aspect to match against entities
	 */
	public EntitySystem ( final Aspect aspect )
	{
		this.setActive( false );

		this.aspect = aspect;
		this.index = ClassIndexer.getIndexFor( this.getClass(), EntitySystem.class );

		// Fetch entity amount per system.
		int actSize = DAConstants.APPROX_ENTITIES_PER_SYSTEM;

		this.actives = new Bag<>( Entity.class, actSize );
	}

	/**
	 * Called before processing of entities begins.
	 */
	protected void begin ()
	{
		// Empty method.
	}

	@Override
	public final void process ()
	{
		begin();
		processEntities( actives );
		end();
	}

	/**
	 * Called after the processing of entities ends.
	 */
	protected void end ()
	{
		// Empty method.
	}

	/**
	 * Any implementing entity system must implement this method and the logic
	 * to process the given entities of the system.
	 * 
	 * @param entities the entities this system contains.
	 */
	protected abstract void processEntities ( ImmutableBag<Entity> entities );

	/**
	 * Called if the system has received a entity it is interested in, e.g.
	 * created or a component was added to it.
	 * 
	 * @param e the entity that was added to this system.
	 */
	protected void inserted ( final Entity e )
	{
		// Empty method.
	}

	/**
	 * Called if a entity was removed from this system, e.g. deleted or had one
	 * of it's components removed.
	 * 
	 * @param e the entity that was removed from this system.
	 */
	protected void removed ( final Entity e )
	{
		// Empty method.
	}

	private final void removeFromSystem ( final Entity e )
	{
		final int ei = searchFor( e );
		actives.eraseUnsafe( ei );

		e.systemBits.fastClear( index );
		removed( e );
	}

	private final void insertToSystem ( final Entity e )
	{
		final int ei = -(searchFor( e ) + 1);

		actives.ensureCapacity( actives.size() + 1 );
		actives.insertUnsafe( ei, e );

		e.systemBits.set( index );
		inserted( e );
	}

	private final int searchFor ( final Entity e )
	{
		return Arrays.binarySearch( actives.data(), 0, actives.size(), e, ( a, b ) -> a.id - b.id );
	}

	@Override
	public void added ( final ImmutableBag<Entity> entities )
	{
		checkAll( entities );
	}

	@Override
	public void changed ( final ImmutableBag<Entity> entities )
	{
		checkAll( entities );
	}

	@Override
	public void deleted ( final ImmutableBag<Entity> entities )
	{
		removeAll( entities );
	}

	@Override
	public void disabled ( final ImmutableBag<Entity> entities )
	{
		removeAll( entities );
	}

	private final void removeAll ( final ImmutableBag<Entity> entities )
	{
		final Entity[] array = ((Bag<Entity>) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final Entity e = array[i];

			if ( e.systemBits.get( index ) )
			{
				removeFromSystem( e );
			}
		}
	}

	@Override
	public void enabled ( final ImmutableBag<Entity> entities )
	{
		checkAll( entities );
	}

	/**
	 * Adds entity if the system is interested in it and hasn't been added
	 * before.
	 * 
	 * Removes entity from system if its not interesting and it has been added
	 * before.
	 * 
	 * @param e entities to check.
	 */
	private final void checkAll ( final ImmutableBag<Entity> entities )
	{
		final Entity[] array = ((Bag<Entity>) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final Entity e = array[i];
			// Second bit for 'contains'.
			int flags = e.systemBits.getBit( index ) << 1;
			// First bit for 'interesting'.
			flags |= aspect.isInteresting( e ) ? 0b1 : 0b0;

			switch (flags)
			{
			// Interesting and system doesn't contains.
				case 0b01:
				{
					insertToSystem( e );
					continue;
				}
				// Not interesting and system does contains.
				case 0b10:
				{
					removeFromSystem( e );
					continue;
				}
				// Otherwise do nothing.
				default:
					continue;
			}
		}
	}

	public int getIndex ()
	{
		return index;
	}

	public ImmutableBag<Entity> getActives ()
	{
		return actives;
	}

}
