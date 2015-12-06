package com.artemis;

import java.util.IdentityHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;
import com.artemis.utils.IntBag;

/**
 * The primary instance for the framework. It contains all the managers.
 * 
 * You must use this to create, delete and retrieve entities.
 * 
 * It is also important to set the delta each game loop iteration, and
 * initialize before game loop.
 * 
 * @author Arni Arent
 * @author dustContributor
 * 
 */
public class World
{
	private final EntityManager em;
	private final ComponentManager cm;

	public float delta;

	private final IntBag added;
	private final IntBag changed;
	private final IntBag deleted;
	private final IntBag enabled;
	private final IntBag disabled;

	private final IdentityHashMap<Class<EntityObserver>, EntityObserver> observerMap;
	private final Bag<EntityObserver> observers;

	public World ()
	{
		observerMap = new IdentityHashMap<>();
		observers = new Bag<>( EntityObserver.class );

		added = new IntBag();
		changed = new IntBag();
		deleted = new IntBag();
		enabled = new IntBag();
		disabled = new IntBag();

		cm = new ComponentManager();
		em = new EntityManager();

		setObserver( em );
	}

	/**
	 * Makes sure all managers observers are initialized in the order they were
	 * added.
	 */
	public void initialize ()
	{
		/*
		 * Injecting all ComponentMappers and EntityObservers into the
		 * observers.
		 */
		Injector.init( observers );

		// Initialize all observers.
		{
			final EntityObserver[] data = observers.data();
			final int size = observers.size();

			for ( int i = 0; i < size; ++i )
			{
				data[i].init();
			}
		}
	}

	/**
	 * Returns a manager that takes care of all the entities in the world.
	 * entities of this world.
	 * 
	 * @return entity manager.
	 */
	public EntityManager entityManager ()
	{
		return em;
	}

	/**
	 * Returns a manager that takes care of all the components in the world.
	 * 
	 * @return component manager.
	 */
	public ComponentManager componentManager ()
	{
		return cm;
	}

	/**
	 * Registers a poolable component with its supplier.
	 * 
	 * @param type of the poolable component.
	 * @param supplier of new component instances.
	 */
	public <T extends Component> void registerPoolable ( Class<T> type, Supplier<T> supplier )
	{
		cm.registerPoolable( type, supplier, null );
	}

	/**
	 * Registers a poolable component with its supplier and a resetter for
	 * components that need to be reset in some way before being reused.
	 * 
	 * @param type of the poolable component.
	 * @param supplier of new component instances.
	 * @param resetter of components that will be reused.
	 */
	public <T extends Component> void registerPoolable (
		Class<T> type,
		Supplier<T> supplier,
		Consumer<T> resetter )
	{
		cm.registerPoolable( type, supplier, resetter );
	}

	/**
	 * Gives you all the entity observers in this world instance.
	 * 
	 * @return all entity observer in world.
	 */
	public ImmutableBag<EntityObserver> getObservers ()
	{
		return observers;
	}

	/**
	 * Adds an entity observer to this world that will be processed by
	 * World.process()
	 * 
	 * @param observer the observer to add.
	 * @return the added observer.
	 */
	public <T extends EntityObserver> T setObserver ( final T observer )
	{
		return setObserver( observer, true );
	}

	/**
	 * Add an observer into this world. It can be retrieved later. World will
	 * notify this observer of changes to entities.
	 * 
	 * @param observer the observer to add.
	 * @param active whether or not this observer will be processed by
	 *            World.process()
	 * @return the added observer.
	 */
	@SuppressWarnings ( "unchecked" )
	public <T extends EntityObserver> T setObserver ( final T observer, final boolean active )
	{
		observer.world = this;
		observer.setActive( active );

		observerMap.put( (Class<EntityObserver>) observer.getClass(), observer );
		observers.add( observer );

		return observer;
	}

	/**
	 * Removed the specified entity observer from the world.
	 * 
	 * @param observer to be deleted from world.
	 */
	public void deleteObserver ( final EntityObserver observer )
	{
		observerMap.remove( observer.getClass() );
		observers.remove( observer );
	}

	/**
	 * Retrieve an entity observer of the specified type.
	 * 
	 * @param type of observer.
	 * @return instance of the observer of the specified type in this world,
	 *         {@code null} if there is none.
	 */
	@SuppressWarnings ( "unchecked" )
	public <T extends EntityObserver> T getObserver ( final Class<T> type )
	{
		return (T) observerMap.get( type );
	}

	/**
	 * Time since last game loop.
	 * 
	 * @return delta time since last game loop.
	 */
	public float getDelta ()
	{
		return delta;
	}

	/**
	 * You must specify the delta for the game here.
	 * 
	 * @param delta time since last game loop.
	 */
	public void setDelta ( final float delta )
	{
		this.delta = delta;
	}

	/**
	 * Adds a entity to this world.
	 * 
	 * @param eid entity id.
	 */
	public void addEntity ( final int eid )
	{
		added.add( eid );
	}

	/**
	 * Checks if the entity has been added to the world since the last
	 * {@link #process()} call.
	 * 
	 * @param eid entity id to look for.
	 * @return true if it was added recently, false otherwise.
	 */
	public boolean isAdded ( final int eid )
	{
		return added.contains( eid ) > -1;
	}

	/**
	 * Ensure all observers are notified of changes to this entity. If you're
	 * adding a component to an entity after it's been added to the world, then
	 * you need to invoke this method.
	 * 
	 * @param eid entity id.
	 */
	public void changedEntity ( final int eid )
	{
		changed.add( eid );
	}

	/**
	 * Checks if the entity has been changed in the world since the last
	 * {@link #process()} call.
	 * 
	 * @param eid entity id to look for.
	 * @return true if it was changed recently, false otherwise.
	 */
	public boolean isChanged ( final int eid )
	{
		return changed.contains( eid ) > -1;
	}

	/**
	 * Delete the entity from the world.
	 * 
	 * @param eid entity id.
	 */
	public void deleteEntity ( final int eid )
	{
		deleted.add( eid );
	}

	/**
	 * Checks if the entity has been deleted in the world since the last
	 * {@link #process()} call.
	 * 
	 * @param eid entity id to look for.
	 * @return true if it was deleted recently, false otherwise.
	 */
	public boolean isDeleted ( final int eid )
	{
		return deleted.contains( eid ) > -1;
	}

	/**
	 * (Re)enable the entity in the world, after it having being disabled. Won't
	 * do anything unless it was already disabled.
	 * 
	 * @param eid entity id.
	 */
	public void enable ( final int eid )
	{
		enabled.add( eid );
	}

	/**
	 * Disable the entity from being processed. Won't delete it, it will
	 * continue to exist but won't get processed.
	 * 
	 * @param eid entity id.
	 */
	public void disable ( final int eid )
	{
		disabled.add( eid );
	}

	/**
	 * Create and return a new entity. Will NOT add the entity to the world, use
	 * {@link World#addEntity(int)} for that.
	 * 
	 * @return entity
	 */
	public int createEntity ()
	{
		final int eid = em.createEntityInstance();
		cm.registerEntity( eid );
		return eid;
	}

	/**
	 * Iterates over all entity bags, and which each corresponding action
	 * (added, deleted, etc), it calls it for each entity in that bag, for each
	 * entity observer present in this World instance.
	 */
	private final void checkAll ()
	{
		// Checking all affected entities in all EntityObservers.
		notifyObservers( observers );
		// Clean components from deleted entities.
		cm.clean( deleted );
		// Clearing all the affected entities before next world update.
		clearAllBags();
	}

	private final void clearAllBags ()
	{
		added.setSize( 0 );
		changed.setSize( 0 );
		disabled.setSize( 0 );
		enabled.setSize( 0 );
		deleted.setSize( 0 );
	}

	private final <T extends EntityObserver> void notifyObservers ( final Bag<T> observers )
	{
		final int size = observers.size();
		final T[] obs = observers.data();

		for ( int i = 0; i < size; ++i )
		{
			final T o = obs[i];
			o.added( added );
			o.changed( changed );
			o.disabled( disabled );
			o.enabled( enabled );
			o.deleted( deleted );
			o.processModifiedEntities();
		}
	}

	/**
	 * Process all active observers.
	 */
	public void process ()
	{
		checkAll();

		final EntityObserver[] sArray = observers.data();
		final int sSize = observers.size();

		for ( int i = 0; i < sSize; ++i )
		{
			final EntityObserver obs = sArray[i];

			if ( obs.isActive() )
			{
				obs.process();
			}
		}
	}

	/**
	 * Retrieves a ComponentMapper instance for fast retrieval of components
	 * from entities.
	 * 
	 * @param type of component to get mapper for.
	 * @return mapper for specified component type.
	 */
	public <T extends Component> ComponentMapper<T> getMapper ( final Class<T> type )
	{
		return cm.getMapperFor( type );
	}

}
