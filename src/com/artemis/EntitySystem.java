package com.artemis;

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
	
	protected World world;
	
	private boolean active;

	/**
	 * Creates an entity system that uses the specified aspect as a matcher
	 * against entities.
	 * 
	 * @param aspect
	 *            to match against entities
	 */
	public EntitySystem ( final Aspect aspect )
	{
		this.aspect = aspect;
		this.active = false;
		this.actives = new Bag<>( Entity.class );
		
		this.index = ClassIndexer.getIndexFor ( this.getClass(), EntitySystem.class );
	}

	/**
	 * Called before processing of entities begins.
	 */
	protected void begin ()
	{
		// Empty method.
	}

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
	 * @param entities
	 *            the entities this system contains.
	 */
	protected abstract void processEntities ( ImmutableBag<Entity> entities );

	/**
	 * Override to implement code that gets executed when systems are
	 * initialized.
	 */
	protected void initialize ()
	{
		// Empty method.
	}

	/**
	 * Called if the system has received a entity it is interested in, e.g.
	 * created or a component was added to it.
	 * 
	 * @param e
	 *            the entity that was added to this system.
	 */
	protected void inserted ( final Entity e )
	{
		// Empty method.
	}

	/**
	 * Called if a entity was removed from this system, e.g. deleted or had one
	 * of it's components removed.
	 * 
	 * @param e
	 *            the entity that was removed from this system.
	 */
	protected void removed ( final Entity e )
	{
		// Empty method.
	}
	
	private final void removeFromSystem ( final Entity e )
	{
		final int ei = e.removedInSystem( this );
		e.systemBits.clear( index );
		actives.removeUnsafe( ei );
		
		if ( (actives.size() - ei) > 0 )
		{
			final Entity tmp = actives.getUnsafe( ei );
			tmp.updateInSystem( this, ei );
		}
		
		removed( e );
	}

	private final void insertToSystem ( final Entity e )
	{
		e.addedInSystem( this, actives.size() );
		e.systemBits.set( index );
		actives.add( e );
		
		inserted( e );
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
	 * Adds entity if the system is interested in it
	 * and hasn't been added before.
	 * 
	 * Removes entity from system if its not interesting
	 * and it has been added before.
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
			final int interesting = aspect.isInteresting( e ) ? 0b01 : 0b00;
			final int contains = e.systemBits.get( index ) ? 0b10 : 0b00;

			switch (interesting | contains)
			{
				case 1:
				{
					insertToSystem( e );
					continue;
				}
				case 2:
				{
					removeFromSystem( e );
					continue;
				}
				default:
					continue;
			}
		}
	}
	
	/**
	 * 
	 * @return true if the system should be processed, false if not.
	 */
	public boolean isActive ()
	{
		return active;
	}

	public void setActive ( final boolean active )
	{
		this.active = active;
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
