package com.artemis;

import java.util.HashMap;

import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

/**
 * The primary instance for the framework. It contains all the managers.
 * 
 * You must use this to create, delete and retrieve entities.
 * 
 * It is also important to set the delta each game loop iteration, and
 * initialize before game loop.
 * 
 * @author Arni Arent
 * 
 */
public class World
{
	private final EntityManager em;
	private final ComponentManager cm;

	public float delta;
	private final Bag<Entity> added;
	private final Bag<Entity> changed;
	private final Bag<Entity> deleted;
	private final Bag<Entity> enable;
	private final Bag<Entity> disable;

	private final HashMap<Class<? extends Manager>, Manager> managers;
	private final Bag<Manager> managersBag;

	private final HashMap<Class<? extends EntitySystem>, EntitySystem> systems;
	private final Bag<EntitySystem> systemsBag;

	public World ()
	{
		managers = new HashMap<>();
		managersBag = new Bag<>( Manager.class );

		systems = new HashMap<>();
		systemsBag = new Bag<>( EntitySystem.class );

		added = new Bag<>( Entity.class );
		changed = new Bag<>( Entity.class );
		deleted = new Bag<>( Entity.class );
		enable = new Bag<>( Entity.class );
		disable = new Bag<>( Entity.class );

		cm = new ComponentManager();
		setManager( cm );

		em = new EntityManager();
		setManager( em );
	}

	/**
	 * Makes sure all managers systems are initialized in the order they were
	 * added.
	 */
	public void initialize ()
	{
		{
			final Manager[] mArray = managersBag.getData();
			final int size = managersBag.size();

			for ( int i = 0; i < size; ++i )
			{
				mArray[i].initialize();
			}
		}

		final EntitySystem[] sArray = systemsBag.getData();
		final int size = systemsBag.size();

		// Injecting all ComponentMappers into the systems.
		for ( int i = 0; i < size; ++i )
		{
			MapperImplementor.initFor( sArray[i], this );
		}
		
		// Now initializing the systems.
		for ( int i = 0; i < size; ++i )
		{
			sArray[i].initialize();
		}
	}

	/**
	 * Returns a manager that takes care of all the entities in the world.
	 * entities of this world.
	 * 
	 * @return entity manager.
	 */
	public EntityManager getEntityManager ()
	{
		return em;
	}

	/**
	 * Returns a manager that takes care of all the components in the world.
	 * 
	 * @return component manager.
	 */
	public ComponentManager getComponentManager ()
	{
		return cm;
	}

	/**
	 * Add a manager into this world. It can be retrieved later. World will
	 * notify this manager of changes to entity.
	 * 
	 * @param manager
	 *            to be added
	 */
	public <T extends Manager> T setManager ( final T manager )
	{
		managers.put( manager.getClass(), manager );
		managersBag.add( manager );
		manager.setWorld( this );
		return manager;
	}

	/**
	 * Returns a manager of the specified type.
	 * 
	 * @param <T>
	 * @param managerType
	 *            class type of the manager
	 * @return the manager
	 */
	@SuppressWarnings("unchecked")
	public <T extends Manager> T getManager ( final Class<T> managerType )
	{
		return (T) managers.get( managerType );
	}

	/**
	 * Deletes the manager from this world.
	 * 
	 * @param manager
	 *            to delete.
	 */
	public void deleteManager ( final Manager manager )
	{
		managers.remove( manager.getClass() );
		managersBag.remove( manager );
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
	 * @param delta
	 *            time since last game loop.
	 */
	public void setDelta ( final float delta )
	{
		this.delta = delta;
	}

	/**
	 * Adds a entity to this world.
	 * 
	 * @param e
	 *            entity
	 */
	public void addEntity ( final Entity e )
	{
		added.add( e );
	}

	/**
	 * Ensure all systems are notified of changes to this entity. If you're
	 * adding a component to an entity after it's been added to the world, then
	 * you need to invoke this method.
	 * 
	 * @param e
	 *            entity
	 */
	public void changedEntity ( final Entity e )
	{
		changed.add( e );
	}

	/**
	 * Delete the entity from the world.
	 * 
	 * @param e
	 *            entity
	 */
	public void deleteEntity ( final Entity e )
	{
		if ( !deleted.contains( e ) )
		{
			deleted.add( e );
		}
	}

	/**
	 * (Re)enable the entity in the world, after it having being disabled. Won't
	 * do anything unless it was already disabled.
	 */
	public void enable ( final Entity e )
	{
		enable.add( e );
	}

	/**
	 * Disable the entity from being processed. Won't delete it, it will
	 * continue to exist but won't get processed.
	 */
	public void disable ( final Entity e )
	{
		disable.add( e );
	}

	/**
	 * Create and return a new or reused entity instance. Will NOT add the
	 * entity to the world, use World.addEntity(Entity) for that.
	 * 
	 * @return entity
	 */
	public Entity createEntity ()
	{
		return em.createEntityInstance();
	}

	/**
	 * Get a entity having the specified id.
	 * 
	 * @param entityId
	 * @return entity
	 */
	public Entity getEntity ( final int entityId )
	{
		return em.getEntity( entityId );
	}

	/**
	 * Gives you all the systems in this world for possible iteration.
	 * 
	 * @return all entity systems in world.
	 */
	public ImmutableBag<EntitySystem> getSystems ()
	{
		return systemsBag;
	}

	/**
	 * Adds a system to this world that will be processed by World.process()
	 * 
	 * @param system
	 *            the system to add.
	 * @return the added system.
	 */
	public <T extends EntitySystem> T setSystem ( final T system )
	{
		return setSystem( system, false );
	}

	/**
	 * Will add a system to this world.
	 * 
	 * @param system
	 *            the system to add.
	 * @param passive
	 *            wether or not this system will be processed by World.process()
	 * @return the added system.
	 */
	public <T extends EntitySystem> T setSystem ( final T system, final boolean passive )
	{
		system.setWorld( this );
		system.setPassive( passive );

		systems.put( system.getClass(), system );
		systemsBag.add( system );

		return system;
	}

	/**
	 * Removed the specified system from the world.
	 * 
	 * @param system
	 *            to be deleted from world.
	 */
	public void deleteSystem ( final EntitySystem system )
	{
		systems.remove( system.getClass() );
		systemsBag.remove( system );
	}

	private void notifySystems ( final Performer performer, final Entity e )
	{
		final EntitySystem[] sArray = systemsBag.getData();
		final int size = systemsBag.size();

		for ( int i = 0; i < size; ++i )
		{
			performer.perform( sArray[i], e );
		}
	}

	private void notifyManagers ( final Performer performer, final Entity e )
	{
		final Manager[] mArray = managersBag.getData();
		final int size = managersBag.size();

		for ( int i = 0; i < size; ++i )
		{
			performer.perform( mArray[i], e );
		}
	}

	/**
	 * Retrieve a system for specified system type.
	 * 
	 * @param type
	 *            type of system.
	 * @return instance of the system in this world.
	 */
	@SuppressWarnings("unchecked")
	public <T extends EntitySystem> T getSystem ( final Class<T> type )
	{
		return (T) systems.get( type );
	}

	/**
	 * Performs an action on each entity.
	 * 
	 * @param entities
	 * @param performer
	 */
	private void check ( final Bag<Entity> entities, final Performer performer )
	{
		final Entity[] eArray = entities.getData();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final Entity e = eArray[i];
			notifyManagers( performer, e );
			notifySystems( performer, e );
		}

		entities.clear();
	}

	private final Performer addedPerformer = ( o, e ) -> o.added( e ), 
							changedPerformer = ( o, e ) -> o.changed( e ), 
							disabledPerformer = ( o, e ) -> o.disabled( e ), 
							enabledPerformer = ( o, e ) -> o.enabled( e ), 
							deletedPerformer = ( o, e ) -> o.deleted( e );

	/**
	 * Process all non-passive systems.
	 */
	public void process ()
	{
		check( added, addedPerformer );
		check( changed, changedPerformer );
		check( disable, disabledPerformer );
		check( enable, enabledPerformer );
		check( deleted, deletedPerformer );

		cm.clean();

		final EntitySystem[] sArray = systemsBag.getData();
		final int size = systemsBag.size();

		for ( int i = 0; i < size; ++i )
		{
			final EntitySystem system = sArray[i];

			if ( !system.isPassive() )
			{
				system.process();
			}
		}
	}

	/**
	 * Retrieves a ComponentMapper instance for fast retrieval of components
	 * from entities.
	 * 
	 * @param type
	 *            of component to get mapper for.
	 * @return mapper for specified component type.
	 */
	public <T extends Component> ComponentMapper<T> getMapper ( final Class<T> type )
	{
		return ComponentMapper.getFor( type, this );
	}

	/*
	 * Only used internally to maintain clean code.
	 */
	private static interface Performer
	{
		void perform ( final EntityObserver observer, final Entity e );
	}

}
