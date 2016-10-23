package com.artemis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

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
public final class World
{
	/** Managers for all entities this instance created. */
	private final EntityManager em;
	/** Managers for all the components of the entities of this instance. */
	private final ComponentManager cm;

	/** Arbitrary data field. */
	private final Object data;

	// Various bags to hold entities in 'limbo' before observer processing.

	private final IntBag added = new IntBag();
	private final IntBag changed = new IntBag();
	private final IntBag deleted = new IntBag();
	private final IntBag enabled = new IntBag();
	private final IntBag disabled = new IntBag();

	/** All the observers this instance owns. */
	private final EntityObserver[] observers;
	/** An immutable view of the observers. */
	private final ImmutableBag<EntityObserver> immutableObservers;

	/**
	 * Tiny internal class to pass parameters to the {@link World}'s constructor.
	 * It pre-validates the parameters so {@link World} can use them directly.
	 *
	 * @author dustContributor
	 */
	private static final class WorldParams
	{
		final ComponentManager componentManager;
		final EntityManager entityManager;
		final EntityObserver[] observers;
		final Object data;

		WorldParams (
				final ComponentManager componentManager,
				final EntityManager entityManager,
				final EntityObserver[] observers,
				final Object data )
		{
			super();
			this.componentManager = DustException.enforceNonNull( this, componentManager, "componentManager" );
			this.entityManager = DustException.enforceNonNull( this, entityManager, "entityManager" );
			this.observers = DustException.enforceNonNull( this, observers, "observers" );
			// This one can be null. User-defined.
			this.data = data;
		}

	}

	/**
	 * World instance constructor. Will set itself in all the passed observers.
	 *
	 * @param params to use to initialize the instance, can't be null.
	 */
	World ( final WorldParams params )
	{
		DustException.enforceNonNull( this, params, "params" );

		cm = params.componentManager;
		em = params.entityManager;

		// We don't validate arbitrary data.
		data = params.data;

		// Store observers.
		observers = params.observers;

		// Create immutable view of the observer array.
		final Bag<EntityObserver> obag = new Bag<>( observers );
		obag.setSize( observers.length );
		immutableObservers = obag;

		// World sets itself in all its observers.
		immutableObservers.forEach( o -> o.world( this ) );
	}

	/**
	 * Returns a manager that takes care of all the entities in the world.
	 * entities of this world.
	 *
	 * @return entity manager.
	 */
	public final EntityManager entityManager ()
	{
		return em;
	}

	/**
	 * Returns a manager that takes care of all the components in the world.
	 *
	 * @return component manager.
	 */
	public final ComponentManager componentManager ()
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
	public final <T> T data ()
	{
		return (T) data;
	}

	/**
	 * Gives you all the entity observers in this world instance.
	 *
	 * @return all entity observer in world.
	 */
	public final ImmutableBag<EntityObserver> observers ()
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
	public final <T extends EntityObserver> T observer ( final Class<T> type )
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
	public final void addEntity ( final int eid )
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
	public final boolean isAdded ( final int eid )
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
	public final void changedEntity ( final int eid )
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
	public final boolean isChanged ( final int eid )
	{
		return changed.contains( eid ) > -1;
	}

	/**
	 * Delete the entity from the world.
	 *
	 * @param eid entity id.
	 */
	public final void deleteEntity ( final int eid )
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
	public final boolean isDeleted ( final int eid )
	{
		return deleted.contains( eid ) > -1;
	}

	/**
	 * (Re)enable the entity in the world, after it having being disabled. Won't
	 * do anything unless it was already disabled.
	 *
	 * @param eid entity id.
	 */
	public final void enable ( final int eid )
	{
		enabled.add( eid );
	}

	/**
	 * Disable the entity from being processed. Won't delete it, it will continue
	 * to exist but won't get processed.
	 *
	 * @param eid entity id.
	 */
	public final void disable ( final int eid )
	{
		disabled.add( eid );
	}

	/**
	 * Create and return a new entity. Will NOT add the entity to the world, use
	 * {@link World#addEntity(int)} for that.
	 *
	 * @return entity
	 */
	public final int createEntity ()
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
	public final void process ()
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
	public final <T extends Component> ComponentHandler<T> getHandler ( final Class<T> type )
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
	 * Also can be configured to supply other kinds of {@link World} instances by
	 * providing a custom world supplier.
	 * </p>
	 *
	 *
	 * @author dustContributor
	 *
	 */
	public static final class Builder
	{
		/**
		 * Dummy builder to use for when an observer without aspects is passed.
		 */
		private final Aspect.Builder dummyAspectBuilder = Aspect.builder();
		/**
		 * Types that will be present in all entities of the world.
		 */
		private final HashSet<Class<? extends Component>> componentTypes;
		/**
		 * Observers, their aspect builders and their priorities/orders, matched by
		 * position.
		 */
		private final ArrayList<Function<Aspect, EntityObserver>> observers;
		private final ArrayList<Aspect.Builder> aspects;
		private final IntBag orders;
		/**
		 * Arbitrary data parameter for the world.
		 */
		private Object data;
		/**
		 * Initialize observers in the order specified or not.
		 */
		private boolean initializeByOrder;
		/**
		 * Process observers in the order specified or not.
		 */
		private boolean processByOrder;

		Builder ()
		{
			this.initializeByOrder = false;
			this.processByOrder = false;
			this.componentTypes = new HashSet<>();
			this.observers = new ArrayList<>( 16 );
			this.aspects = new ArrayList<>( 16 );
			this.orders = new IntBag( 16 );
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
		 * Add an observer constructor into this world {@link Builder}. Once
		 * constructed, the observer can be retrieved later via the world instance.
		 * The {@link World} instance constructed with this {@link Builder} will
		 * notify the {@link EntityObserver} of any entity changes. <b>Its order
		 * will be set to 0.</b>
		 *
		 * @param observer to add.
		 * @return this builder instance.
		 */
		public final Builder observer ( final Supplier<EntityObserver> observer )
		{
			return observer( observer, 0 );
		}

		/**
		 * Add an observer constructor into this world {@link Builder}. Once
		 * constructed, the observer can be retrieved later via the world instance.
		 * The {@link World} instance constructed with this {@link Builder} will
		 * notify the {@link EntityObserver} of any entity changes.
		 *
		 * @param observer to add.
		 * @param order of the observer.
		 * @return this builder instance.
		 */
		public final Builder observer ( final Supplier<EntityObserver> observer, final int order )
		{
			return observer( a -> observer.get(), dummyAspectBuilder, order );
		}

		/**
		 * Add an observer constructor into this world {@link Builder}. The
		 * constructor will be called with the {@link Aspect} created with the
		 * passed aspect builder. Once constructed, the observer can be retrieved
		 * later via the world instance. The {@link World} instance constructed with
		 * this {@link Builder} will notify the {@link EntityObserver} of any entity
		 * changes. <b>Its order will be set to 0.</b>
		 *
		 * @param observer to add.
		 * @param aspect builder to use when constructing the observer.
		 * @return this builder instance.
		 */
		public final Builder observer ( final Function<Aspect, EntityObserver> observer, final Aspect.Builder aspect )
		{
			return observer( observer, aspect, 0 );
		}

		/**
		 * Add an observer constructor into this world {@link Builder}. The
		 * constructor will be called with the {@link Aspect} created with the
		 * passed aspect builder. Once constructed, the observer can be retrieved
		 * later via the world instance. The {@link World} instance constructed with
		 * this {@link Builder} will notify the {@link EntityObserver} of any entity
		 * changes.
		 *
		 * @param observer to add.
		 * @param aspect builder to use when constructing the observer.
		 * @param order of the observer.
		 * @return this builder instance.
		 */
		public final Builder observer (
				final Function<Aspect, EntityObserver> observer,
				final Aspect.Builder aspect,
				final int order )
		{
			DustException.enforceNonNull( this, observer, "observer" );
			DustException.enforceNonNull( this, aspect, "aspect" );
			this.observers.add( observer );
			this.aspects.add( aspect );
			this.orders.add( order );
			return this;
		}

		/**
		 * Adds a component type that the {@link World} instance created by this
		 * {@link Builder} will be able to handle.
		 *
		 * @param type of the {@link Component}.
		 * @return this builder instance.
		 */
		public final Builder componentType ( final Class<? extends Component> type )
		{
			componentTypes.add( DustException.enforceNonNull( this, type, "type" ) );
			return this;
		}

		/**
		 * Bulk varargs version of {@link #componentType(Class)}.
		 *
		 * @param types of the {@link Component}s.
		 * @return this builder instance.
		 */
		@SafeVarargs
		public final Builder componentTypes ( final Class<? extends Component>... types )
		{
			for ( final Class<? extends Component> type : types )
			{
				componentType( type );
			}
			return this;
		}

		/**
		 * Bulk {@link Iterable} version of {@link #componentType(Class)}.
		 *
		 * @param types of the {@link Component}s.
		 * @return this builder instance.
		 */
		public final Builder componentTypes ( final Iterable<Class<? extends Component>> types )
		{
			for ( final Class<? extends Component> type : types )
			{
				componentType( type );
			}
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
			/*
			 * Mapping from observer to order, we fill it later by passing it to the
			 * method that creates observers. The usage is a bit messy, maybe it could
			 * be refined.
			 */
			final IdentityHashMap<EntityObserver, Integer> orderMap = new IdentityHashMap<>( observers.size() );
			// Capture the map and use it for sorter by order later.
			final Comparator<EntityObserver> compareByOrder = ( a, b ) -> orderMap.get( a ).compareTo( orderMap.get( b ) );
			/*
			 * Construct component manager, used by world instance and aspect
			 * creation.
			 */
			final ComponentManager cm = new ComponentManager( componentTypes );
			// Use all the constructors to create the observers.
			final EntityObserver[] observers = createObservers( cm, orderMap );
			// Compose world parameters and construct it.
			final World world = new World( composeWorldParams( observers, compareByOrder, cm ) );
			// Do the init step on observers.
			observerInitialization( observers, compareByOrder, world );
			// Now return fully constructed world instance.
			return world;
		}

		private final EntityObserver[] createObservers (
				final ComponentManager cm,
				final IdentityHashMap<EntityObserver, Integer> observerToOrder )
		{
			final int size = observers.size();

			if ( aspects.size() != size )
			{
				// Check if something went wrong, just in case.
				throw new DustException( this, "There are different amounts of observers and aspects in the builder!" );
			}

			// Create empty aspect to pass to observers who don't actually need one.
			final Aspect emptyAspect = dummyAspectBuilder.build( cm );

			final EntityObserver[] result = new EntityObserver[size];

			for ( int i = 0; i < size; ++i )
			{
				// Fetch the observer constructor.
				final Function<Aspect, EntityObserver> ctor = observers.get( i );
				// Fetch the aspect builder for that observer.
				final Aspect.Builder aspectBuilder = aspects.get( i );
				// Fetch the priority/order.
				final int order = orders.getUnsafe( i );
				/*
				 * If its the dummy builder, just use the empty aspect, otherwise build
				 * the new aspect to use.
				 */
				final Aspect aspect = aspectBuilder == dummyAspectBuilder ? emptyAspect : aspectBuilder.build( cm );
				// Construct observer and set it on the same position.
				result[i] = ctor.apply( aspect );
				// Map its order.
				observerToOrder.put( result[i], Integer.valueOf( order ) );
			}

			return result;
		}

		private final WorldParams composeWorldParams (
				final EntityObserver[] observers,
				final Comparator<EntityObserver> comparator,
				final ComponentManager cm )
		{
			// Make a defensive copy.
			final EntityObserver[] processObservers = observers.clone();

			if ( processByOrder )
			{
				// Sort if there was an order specified.
				Arrays.sort( processObservers, comparator );
			}

			// Construct entity manager, used by the world instance.
			final EntityManager em = new EntityManager();

			/*
			 * World will respect the order in which processObservers is for calling
			 * 'process' on each of them.
			 */
			return new WorldParams( cm, em, processObservers, data );
		}

		private final void observerInitialization (
				final EntityObserver[] observers,
				final Comparator<EntityObserver> comparator,
				final World world )
		{
			// Make a defensive copy.
			final EntityObserver[] initializeObservers = observers.clone();

			if ( initializeByOrder )
			{
				// Sort if there was an order specified.
				Arrays.sort( initializeObservers, comparator );
			}

			// First step is to inject the observers.
			Injector.init( world, initializeObservers );

			// Second step is to call 'init' on them.
			for ( int i = 0; i < initializeObservers.length; ++i )
			{
				initializeObservers[i].init();
			}
		}
	}
}
