package com.artemis;

import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.function.BiFunction;

import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;
import com.artemis.utils.IntBag;

/**
 * The primary instance for the framework. It contains all the managers.
 * 
 * You must use this to create, delete and retrieve entities.
 * 
 * @author Arni Arent
 * @author dustContributor
 * 
 */
public class World
{
	/** Managers for all entities this instance created. */
	private final EntityManager em;
	/** Managers for all the components of the entities of this instance. */
	private final ComponentManager cm;

	/** Arbitrary data field. */
	private final Object data;

	// Various bags to hold entities in 'limbo' before observer processing.

	private final IntBag added;
	private final IntBag changed;
	private final IntBag deleted;
	private final IntBag enabled;
	private final IntBag disabled;

	/** All the observers this instance owns. */
	private final EntityObserver[] observers;
	/** An immutable view of the observers. */
	private final ImmutableBag<EntityObserver> immutableObservers;

	/**
	 * World instance constructor. Will set itself in all the passed observers.
	 * 
	 * @param observers this instance will own, can't be null.
	 * @param data arbitrary data to hold, can be null.
	 */
	protected World ( final EntityObserver[] observers, final Object data )
	{
		added = new IntBag();
		changed = new IntBag();
		deleted = new IntBag();
		enabled = new IntBag();
		disabled = new IntBag();

		cm = new ComponentManager();
		em = new EntityManager();

		// We don't validate arbitrary data.
		this.data = data;

		// Store observers.
		this.observers = DustException.enforceNonNull( this, observers, "observers" );

		// Create immutable view of the observer array.
		final Bag<EntityObserver> obag = new Bag<>( observers );
		obag.setSize( observers.length );
		immutableObservers = obag;

		// World sets itself in all its observers.
		immutableObservers.forEach( o -> o.world = this );
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
	 * With this method you can retrieve the arbitrary data you placed in this
	 * instance. This is a generic method so it does the cast for you. But you
	 * need to be sure you're using the relevant generic type, otherwise the cast
	 * will fail. Used like:
	 * 
	 * <pre>
	 * MySharedData mySharedData = world.data();
	 * </pre>
	 * 
	 * @throws ClassCastException if tries to cast the data field to a wrong type.
	 * 
	 * @return the arbitrary data object you placed, or null if there isn't any.
	 * @see Builder#data(Object)
	 */
	@SuppressWarnings( "unchecked" )
	public <T> T data ()
	{
		return (T) data;
	}

	/**
	 * Gives you all the entity observers in this world instance.
	 * 
	 * @return all entity observer in world.
	 */
	public ImmutableBag<EntityObserver> getObservers ()
	{
		return immutableObservers;
	}

	/**
	 * Retrieve an entity observer of the specified type.
	 * 
	 * @param type of observer.
	 * @return instance of the observer of the specified type in this world,
	 *         {@code null} if there is none.
	 */
	@SuppressWarnings( "unchecked" )
	public <T extends EntityObserver> T getObserver ( final Class<T> type )
	{
		for ( int i = 0; i < observers.length; ++i )
		{
			final EntityObserver obs = observers[i];

			if ( obs.getClass() == type )
			{
				return (T) obs;
			}
		}
		return null;
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
	 * Disable the entity from being processed. Won't delete it, it will continue
	 * to exist but won't get processed.
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
	 * Iterates over all entity bags, and which each corresponding action (added,
	 * deleted, etc), it calls it for each entity in that bag, for each entity
	 * observer present in this World instance.
	 */
	private final void checkAll ()
	{
		// Checking all affected entities in all EntityObservers.
		notifyObservers();

		notifyComponentManager();
		notifyEntityManager();

		// Clearing all the affected entities before next world update.
		added.setSize( 0 );
		changed.setSize( 0 );
		disabled.setSize( 0 );
		enabled.setSize( 0 );
		deleted.setSize( 0 );
	}

	private final void notifyObservers ()
	{
		for ( int i = 0; i < observers.length; ++i )
		{
			final EntityObserver o = observers[i];
			o.added( added );
			o.changed( changed );
			o.disabled( disabled );
			o.enabled( enabled );
			o.deleted( deleted );
			o.processModifiedEntities();
		}
	}

	private final void notifyComponentManager ()
	{
		// Clean components from deleted entities.
		cm.clean( deleted );
	}

	private final void notifyEntityManager ()
	{
		// Notify entity manager of all entity changes.
		em.added( added );
		em.disabled( disabled );
		em.enabled( enabled );
		em.deleted( deleted );
	}

	/**
	 * Process all active observers.
	 */
	public void process ()
	{
		checkAll();

		for ( int i = 0; i < observers.length; ++i )
		{
			final EntityObserver obs = observers[i];

			if ( obs.isActive() )
			{
				obs.process();
			}
		}
	}

	/**
	 * Retrieves a {@link ComponentHandler} instance for fast retrieval of
	 * components from entities.
	 * 
	 * @param type of component to get handler for.
	 * @return handler for specified component type.
	 */
	public <T extends Component> ComponentHandler<T> getHandler ( final Class<T> type )
	{
		return cm.getHandlerFor( type );
	}

	/**
	 * Returns a new {@link World} {@link Builder} instance. By default its
	 * configured to not respect each {@link EntityObserver}'s order.
	 * 
	 * @return new {@link Builder} instance.
	 */
	public static final Builder builder ()
	{
		return new Builder();
	}

	/**
	 * <p>
	 * Builder class to configure and construct {@link World} instances from.
	 * </p>
	 * 
	 * <p>
	 * Can be configured with {@link EntityObserver}s, configure if the
	 * {@link EntityObserver} initialization and/or processing should be done in a
	 * specific order.
	 * </p>
	 * 
	 * <p>
	 * Also can be configured to supply other kinds of {@link World} instaces by
	 * providing a custom world supplier.
	 * </p>
	 * 
	 * 
	 * @author dustContributor
	 *
	 */
	public static final class Builder
	{
		private final Bag<EntityObserver> observers;
		private final IdentityHashMap<EntityObserver, Integer> orders;

		private BiFunction<EntityObserver[], Object, World> constructor;
		private Object data;
		private boolean initializeByOrder;
		private boolean processByOrder;

		Builder ()
		{
			this.constructor = World::new;
			this.initializeByOrder = false;
			this.processByOrder = false;
			this.observers = new Bag<>( EntityObserver.class, 16 );
			this.orders = new IdentityHashMap<>( 16 );
		}

		/**
		 * In case you want to override the default {@link World} constructor call,
		 * useful for when you want to build your subclass of {@link World}.
		 * 
		 * <p>
		 * For example: <br>
		 * 
		 * <pre>
		 * builder.constructor( SomeWorldSubclass::new );
		 * </pre>
		 * </p>
		 * 
		 * @param constructor to construct a {@link World} from.
		 * @return this builder instance.
		 */
		public final Builder constructor ( final BiFunction<EntityObserver[], Object, World> constructor )
		{
			this.constructor = DustException.enforceNonNull( this, constructor, "worldSupplier" );
			return this;
		}

		/**
		 * This allows you to place an arbitrary object as field of your
		 * {@link World} instance. This way if you need any sort of shared resources
		 * between your systems, you can place them in a data structure of your
		 * choice here.
		 * 
		 * @param data to set in the world instance.
		 * @return this builder instance.
		 */
		public final Builder data ( final Object data )
		{
			this.data = data;
			return this;
		}

		/**
		 * Configures this {@link Builder} response to each of the
		 * {@link EntityObserver}'s order.
		 * 
		 * <p>
		 * If set to 'true', the produced {@link World} will initialize
		 * {@link EntityObserver} instances respecting the passed order (from lower
		 * to upper). Otherwise if set to 'false' it will ignore it and initialize
		 * {@link EntityObserver} instances in the order they were added to this
		 * {@link Builder}.
		 * </p>
		 * 
		 * <p>
		 * 'false' by default.
		 * </p>
		 * 
		 * @param initializeByOrder value.
		 * @return this builder instance.
		 */
		public final Builder initializeByOrder ( final boolean initializeByOrder )
		{
			this.initializeByOrder = initializeByOrder;
			return this;
		}

		/**
		 * Configures this {@link Builder} response to each of the
		 * {@link EntityObserver}'s order.
		 * <p>
		 * If set to 'true', the produced {@link World} will process
		 * {@link EntityObserver} instances respecting the passed order (from lower
		 * to upper). Otherwise if set to 'false' it will ignore it and process
		 * {@link EntityObserver} instances in the order they were added to this
		 * {@link Builder}.
		 * </p>
		 * <p>
		 * 'false' by default.
		 * </p>
		 * 
		 * @param processByOrder value.
		 * @return this builder instance.
		 */
		public final Builder processByOrder ( final boolean processByOrder )
		{
			this.processByOrder = processByOrder;
			return this;
		}

		/**
		 * Add an observer into this world {@link Builder}. It can be retrieved
		 * later. Later, the {@link World} instance constructed with this
		 * {@link Builder} will notify the {@link EntityObserver} of any entity
		 * changes. <b>Its order will be set to 0.</b>
		 * 
		 * @param observer to add.
		 * @return this builder instance.
		 */
		public final Builder observer ( final EntityObserver observer )
		{
			return observer( observer, 0 );
		}

		/**
		 * Add an observer into this world {@link Builder}. It can be retrieved
		 * later. Later, the {@link World} instance constructed with this
		 * {@link Builder} will notify the {@link EntityObserver} of any entity
		 * changes.
		 * 
		 * @param observer to add.
		 * @param order of the observer.
		 * @return this builder instance.
		 */
		public final Builder observer ( final EntityObserver observer, final int order )
		{
			DustException.enforceNonNull( this, observer, "observer" );
			this.observers.add( observer );
			this.orders.put( observer, Integer.valueOf( order ) );
			return this;
		}

		/**
		 * Builds a {@link World} instance with this {@link Builder}'s
		 * {@link EntityObserver} configuration.
		 * 
		 * @return new {@link World} instance.
		 */
		public final World build ()
		{
			// EntityObserver comparator by order.
			final Comparator<EntityObserver> orderComparator = ( a, b ) ->
			{
				return orders.get( a ).compareTo( orders.get( b ) );
			};

			final EntityObserver[] processObservers = Arrays.copyOf( observers.data(), observers.size() );
			final EntityObserver[] initializeObservers = Arrays.copyOf( observers.data(), observers.size() );

			if ( processByOrder )
			{
				Arrays.sort( processObservers, orderComparator );
			}
			/*
			 * World will respect the order in which processObservers is for calling
			 * 'process' on each of them.
			 */
			final World world = constructor.apply( processObservers, data );

			if ( initializeByOrder )
			{
				Arrays.sort( initializeObservers, orderComparator );
			}

			// Inject the observers in initialization order.
			Injector.init( world, initializeObservers );

			for ( int i = 0; i < initializeObservers.length; ++i )
			{
				// And call 'init' on them in initialization order.
				initializeObservers[i].init();
			}
			// Now return fully constructed World instance.
			return world;
		}
	}
}
