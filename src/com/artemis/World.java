package com.artemis;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import com.artemis.annotations.Mapper;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

/**
 * The primary instance for the framework. It contains all the managers.
 * 
 * You must use this to create, delete and retrieve entities.
 * 
 * It is also important to set the delta each game loop iteration, and initialize before game loop.
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

	private final Map<Class<? extends Manager>, Manager> managers;
	private final Bag<Manager> managersBag;

	private final Map<Class<?>, EntitySystem> systems;
	private final Bag<EntitySystem> systemsBag;

	public World ()
	{
		managers = new HashMap<>();
		managersBag = new Bag<>();

		systems = new HashMap<>();
		systemsBag = new Bag<>();

		added = new Bag<>();
		changed = new Bag<>();
		deleted = new Bag<>();
		enable = new Bag<>();
		disable = new Bag<>();

		cm = new ComponentManager();
		setManager( cm );

		em = new EntityManager();
		setManager( em );
	}

	
	/**
	 * Makes sure all managers systems are initialized in the order they were added.
	 */
	public void initialize ()
	{
		for ( int i = 0; i < managersBag.size(); ++i )
		{
			managersBag.get( i ).initialize();
		}

		for ( int i = 0; i < systemsBag.size(); ++i )
		{
			final EntitySystem eSys = systemsBag.get( i );
			
			ComponentMapperInitHelper.config( eSys , this );
			eSys.initialize();
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
	 * Add a manager into this world. It can be retrieved later.
	 * World will notify this manager of changes to entity.
	 * 
	 * @param manager to be added
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
	 * @param manager to delete.
	 */
	public void deleteManager ( final Manager manager )
	{
		managers.remove( manager );
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
	 * @param delta time since last game loop.
	 */
	public void setDelta ( final float delta )
	{
		this.delta = delta;
	}
	
	/**
	 * Adds a entity to this world.
	 * 
	 * @param e entity
	 */
	public void addEntity ( final Entity e )
	{
		added.add( e );
	}
	
	/**
	 * Ensure all systems are notified of changes to this entity.
	 * If you're adding a component to an entity after it's been
	 * added to the world, then you need to invoke this method.
	 * 
	 * @param e entity
	 */
	public void changedEntity ( final Entity e )
	{
		changed.add( e );
	}
	
	/**
	 * Delete the entity from the world.
	 * 
	 * @param e entity
	 */
	public void deleteEntity ( final Entity e )
	{
		if ( !deleted.contains( e ) )
		{
			deleted.add( e );
		}
	}

	/**
	 * (Re)enable the entity in the world, after it having being disabled.
	 * Won't do anything unless it was already disabled.
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
	 * Create and return a new or reused entity instance.
	 * Will NOT add the entity to the world, use World.addEntity(Entity) for that.
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
	 * @param system the system to add.
	 * @return the added system.
	 */
	public <T extends EntitySystem> T setSystem ( final T system )
	{
		return setSystem( system, false );
	}

	/**
	 * Will add a system to this world.
	 *  
	 * @param system the system to add.
	 * @param passive wether or not this system will be processed by World.process()
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
	 * @param system to be deleted from world.
	 */
	public void deleteSystem ( final EntitySystem system )
	{
		systems.remove( system.getClass() );
		systemsBag.remove( system );
	}
	
	private void notifySystems ( final Performer performer, final Entity e )
	{
		for ( int i = 0; i < systemsBag.size(); ++i )
		{
			performer.perform( systemsBag.get( i ), e );
		}
	}

	private void notifyManagers ( final Performer performer, final Entity e )
	{
		for ( int i = 0; i < managersBag.size(); ++i )
		{
			performer.perform( managersBag.get( i ), e );
		}
	}
	
	/**
	 * Retrieve a system for specified system type.
	 * 
	 * @param type type of system.
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
		if ( !entities.isEmpty() )
		{
			for ( int i = 0; i < entities.size(); ++i )
			{
				final Entity e = entities.get( i );
				notifyManagers( performer, e );
				notifySystems( performer, e );
			}
			
			entities.clear();
		}
	}

	
	/**
	 * Process all non-passive systems.
	 */
	public void process ()
	{
		check( added, new Performer()
		{
			@Override
			public void perform ( EntityObserver observer, Entity e )
			{
				observer.added( e );
			}
		} );

		check( changed, new Performer()
		{
			@Override
			public void perform ( EntityObserver observer, Entity e )
			{
				observer.changed( e );
			}
		} );

		check( disable, new Performer()
		{
			@Override
			public void perform ( EntityObserver observer, Entity e )
			{
				observer.disabled( e );
			}
		} );

		check( enable, new Performer()
		{
			@Override
			public void perform ( EntityObserver observer, Entity e )
			{
				observer.enabled( e );
			}
		} );

		check( deleted, new Performer()
		{
			@Override
			public void perform ( EntityObserver observer, Entity e )
			{
				observer.deleted( e );
			}
		} );

		cm.clean();

		for ( int i = 0; i < systemsBag.size(); ++i )
		{
			final EntitySystem system = systemsBag.get( i );
			
			if ( !system.isPassive() )
			{
				system.process();
			}
		}
	}
	

	/**
	 * Retrieves a ComponentMapper instance for fast retrieval of components from entities.
	 * 
	 * @param type of component to get mapper for.
	 * @return mapper for specified component type.
	 */
	public <T extends Component> ComponentMapper<T> getMapper ( final Class<T> type )
	{
		return ComponentMapper.getFor( type, this );
	}
	

	/*
	 * Only used internally to maintain clean code.
	 */
	private interface Performer
	{
		void perform ( EntityObserver observer, Entity e );
	}
	
	private static final class ComponentMapperInitHelper
	{
		@SuppressWarnings("unchecked")
		public static void config ( final Object target, final World world )
		{
			final Class<?> clazz = target.getClass();

			for ( final Field field : clazz.getDeclaredFields() )
			{
				final Mapper annotation = field.getAnnotation( Mapper.class );

				if ( annotation != null && Mapper.class.isAssignableFrom( Mapper.class ) )
				{
					final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
					final Class<? extends Component> componentType = (Class<? extends Component>) genericType.getActualTypeArguments()[0];

					field.setAccessible( true );

					try
					{
						field.set( target, world.getMapper( componentType ) );
					}
					catch ( IllegalArgumentException | IllegalAccessException e )
					{
						throw new RuntimeException( "Error while setting component mappers", e );
					}

				}
			}
		}

	}

}
