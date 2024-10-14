package com.artemis;

import java.util.*;
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
public final class DustContext {
	/** Managers for all entities this instance created. */
	private final EntityManager entityManager;
	/** Managers for all the components of the entities of this instance. */
	private final ComponentManager componentManager;

	/** Arbitrary data field. */
	private final Object data;

	// Various bags to hold entities in 'limbo' before step processing.

	private final IntBag added = new IntBag();
	private final IntBag changed = new IntBag();
	private final IntBag deleted = new IntBag();
	private final IntBag enabled = new IntBag();
	private final IntBag disabled = new IntBag();

	/** These are the entity group filters to match against the entities. */
	private final EntityGroup[] entityGroups;
	/** All the steps this instance owns. */
	private final DustStep[] steps;
	/** An immutable view of the steps. */
	private final ImmutableBag<DustStep> immutableSteps;

	/**
	 * Tiny internal class to pass parameters to the {@link DustContext}'s
	 * constructor. It pre-validates the parameters so {@link DustContext} can use
	 * them directly.
	 *
	 * @author dustContributor
	 */
	private static record ContextParams(ComponentManager componentManager, EntityManager entityManager,
			DustStep[] steps, EntityGroup[] entityFilters,
			// This one can be null. User-defined.
			Object data) {
		ContextParams {
			DustException.enforceNonNull(this, componentManager, "componentManager");
			DustException.enforceNonNull(this, entityManager, "entityManager");
			DustException.enforceNonNull(this, steps, "steps");
			DustException.enforceNonNull(this, entityFilters, "entityFilters");
		}
	}

	/**
	 * World instance constructor. Will set itself in all the passed
	 * {@link DustStep}s.
	 *
	 * @param params to use to initialize the instance, can't be null.
	 */
	DustContext(final ContextParams params) {
		DustException.enforceNonNull(this, params, "params");

		componentManager = params.componentManager();
		entityManager = params.entityManager();

		// We don't validate arbitrary data.
		data = params.data();

		// Store steps.
		steps = params.steps();

		// Store entity filters.
		entityGroups = params.entityFilters();

		// Create immutable view of the step array.
		var stepsBag = new Bag<DustStep>(steps);
		stepsBag.setSize(steps.length);
		immutableSteps = stepsBag;
	}

	/**
	 * Returns a manager that takes care of all the entities in the
	 * {@link DustContext}. entities of this {@link DustContext}.
	 *
	 * @return entity manager.
	 */
	public final EntityManager entityManager() {
		return entityManager;
	}

	/**
	 * Returns a manager that takes care of all the components in the
	 * {@link DustContext}.
	 *
	 * @return component manager.
	 */
	public final ComponentManager componentManager() {
		return componentManager;
	}

	/**
	 * With this method you can retrieve the arbitrary data you placed in this
	 * instance. This is a generic method so it does the cast for you. But you need
	 * to be sure you're using the relevant generic type, otherwise the cast will
	 * fail. Used like:
	 *
	 * <pre>
	 * MySharedData mySharedData = context.data();
	 * </pre>
	 *
	 * @throws ClassCastException if tries to cast the data field to a wrong type.
	 *
	 * @return the arbitrary data object you placed, or null if there isn't any.
	 * @see Builder#data(Object)
	 */
	@SuppressWarnings("unchecked")
	public final <T> T data() {
		return (T) data;
	}

	/**
	 * Gives you all the entity {@link DustStep}s in this {@link DustContext}
	 * instance.
	 *
	 * @return all {@link DustStep}s in {@link DustContext}.
	 */
	public final ImmutableBag<DustStep> steps() {
		return immutableSteps;
	}

	/**
	 * Retrieve a {@link DustStep} of the specified type.
	 *
	 * @param type of {@link DustStep}.
	 * @return instance of the {@link DustStep} of the specified type in this
	 *         {@link DustContext}, {@code null} if there is none.
	 */
	@SuppressWarnings("unchecked")
	public final <T extends DustStep> T step(final Class<T> type) {
		for (int i = 0; i < steps.length; ++i) {
			var step = steps[i];
			if (step.getClass() == type) {
				return (T) step;
			}
		}
		return null;
	}

	/**
	 * Queue entity for addition in the next {@link DustContext#process()} call.
	 * This will be notified to the {@link EntityGroup}s and internal managers. Only
	 * one occurrence of the entity will be present in the queue.
	 *
	 * @param eid entity id.
	 */
	public final void addEntity(final int eid) {
		if (eid < EntityManager.MIN_ENTITY_ID) {
			throw new IllegalArgumentException("Entity id %s is out of range!".formatted(eid));
		}
		final int ei = added.binarySearch(eid);
		if (ei < 0) {
			added.insert(-ei - 1, eid);
		}
	}

	/**
	 * Checks if the entity has been added to the {@link DustContext} since the last
	 * {@link #process()} call.
	 *
	 * @param eid entity id to look for.
	 * @return true if it was added recently, false otherwise.
	 */
	public final boolean isAdded(final int eid) {
		return added.binarySearch(eid) > -1;
	}

	/**
	 * Queue entity for marking as "changed" in the next
	 * {@link DustContext#process()} call. This will be notified to the
	 * {@link EntityGroup}s and internal managers. Only one occurrence of the entity
	 * will be present in the queue.
	 *
	 * @param eid entity id.
	 */
	public final void changedEntity(final int eid) {
		if (eid < EntityManager.MIN_ENTITY_ID) {
			throw new IllegalArgumentException("Entity id %s is out of range!".formatted(eid));
		}
		final int ei = changed.binarySearch(eid);
		if (ei < 0) {
			changed.insert(-ei - 1, eid);
		}
	}

	/**
	 * Checks if the entity has been changed in the {@link DustContext} since the
	 * last {@link #process()} call.
	 *
	 * @param eid entity id to look for.
	 * @return true if it was changed recently, false otherwise.
	 */
	public final boolean isChanged(final int eid) {
		return changed.binarySearch(eid) > -1;
	}

	/**
	 * Queue entity for deletion in the next {@link DustContext#process()} call.
	 * This will be notified to the {@link EntityGroup}s and internal managers. Only
	 * one occurrence of the entity will be present in the queue.
	 *
	 * @param eid entity id.
	 */
	public final void deleteEntity(final int eid) {
		if (eid < EntityManager.MIN_ENTITY_ID) {
			throw new IllegalArgumentException("Entity id %s is out of range!".formatted(eid));
		}
		final int ei = deleted.binarySearch(eid);
		if (ei < 0) {
			deleted.insert(-ei - 1, eid);
		}
	}

	/**
	 * Checks if the entity has been deleted in the {@link DustContext} since the
	 * last {@link #process()} call.
	 *
	 * @param eid entity id to look for.
	 * @return true if it was deleted recently, false otherwise.
	 */
	public final boolean isDeleted(final int eid) {
		return deleted.binarySearch(eid) > -1;
	}

	/**
	 * Queue entity for enabling in the next {@link DustContext#process()} call.
	 * This will be notified to the {@link EntityGroup}s and internal managers. Only
	 * one occurrence of the entity will be present in the queue.
	 *
	 * @param eid entity id.
	 */
	public final void enable(final int eid) {
		final int ei = enabled.binarySearch(eid);
		if (ei < 0) {
			enabled.insert(-ei - 1, eid);
		}
	}

	/**
	 * Checks if the entity has been enabled in the {@link DustContext} since the
	 * last {@link #process()} call.
	 *
	 * @param eid entity id to look for.
	 * @return true if it was enabled recently, false otherwise.
	 */
	public final boolean isEnabled(final int eid) {
		return enabled.binarySearch(eid) > -1;
	}

	/**
	 * Queue entity for disabling in the next {@link DustContext#process()} call.
	 * This will be notified to the {@link EntityGroup}s and internal managers. Only
	 * one occurrence of the entity will be present in the queue.
	 *
	 * @param eid entity id.
	 */
	public final void disable(final int eid) {
		final int ei = disabled.binarySearch(eid);
		if (ei < 0) {
			disabled.insert(-ei - 1, eid);
		}
	}

	/**
	 * Checks if the entity has been disabled in the {@link DustContext} since the
	 * last {@link #process()} call.
	 *
	 * @param eid entity id to look for.
	 * @return true if it was enabled recently, false otherwise.
	 */
	public final boolean isDisabled(final int eid) {
		return disabled.binarySearch(eid) > -1;
	}

	/**
	 * Create and return a new entity. Will NOT add the entity to the
	 * {@link DustContext}, use {@link DustContext#addEntity(int)} for that.
	 *
	 * @return new entity id.
	 */
	public final int createEntity() {
		final int eid = entityManager.createEntityInstance();
		componentManager.registerEntity(eid);
		return eid;
	}

	private final void markComponentChanges() {
		componentManager.markChanges();
	}

	private final void notifyFilters() {
		for (int i = 0; i < entityGroups.length; ++i) {
			var group = entityGroups[i];
			group.clear();
			group.added(added);
			group.added(enabled);
			group.removed(disabled);
			group.removed(deleted);
			group.mutated(changed);
			group.updateActiveList();
		}
	}

	private final void cleanupComponentManager() {
		componentManager.cleanup(deleted);
	}

	private final void notifyEntityManager() {
		entityManager.added(added);
		entityManager.disabled(disabled);
		entityManager.enabled(enabled);
		entityManager.deleted(deleted);
	}

	private final void cleanupSteps() {
		for (int i = 0; i < steps.length; ++i) {
			var step = steps[i];
			if (step.isActive()) {
				/*
				 * TODO: What if component cleanup or initialization is still needed even if
				 * step is disabled? This way the step will never know since changes will be
				 * cleared in the next tick.
				 */
				step.cleanup();
			}
		}
	}

	private final void runSteps() {
		for (int i = 0; i < steps.length; ++i) {
			var step = steps[i];
			if (step.isActive()) {
				step.run();
			}
		}
	}

	private final void clearLists() {
		added.setSize(0);
		changed.setSize(0);
		disabled.setSize(0);
		enabled.setSize(0);
		deleted.setSize(0);
	}

	/**
	 * Process all active {@link DustStep}s.
	 */
	public final void process() {
		// Mark added/removed components in the entity bits.
		markComponentChanges();
		// Let the entity group filters update their internal lists.
		notifyFilters();
		/*
		 * Let the steps have the opportunity of clean up after removed entities from
		 * last tick.
		 */
		cleanupSteps();
		// Clean components from deleted entities.
		cleanupComponentManager();
		// Notify entity manager of all entity changes.
		notifyEntityManager();
		// Clearing all the affected entities before next update.
		clearLists();
		// Execute all steps of the context.
		runSteps();
	}

	/**
	 * Retrieves a {@link ComponentHandler} instance for fast retrieval of
	 * components from entities.
	 *
	 * @param type of component to get handler for.
	 * @return handler for specified component type.
	 */
	public final <T extends Component> ComponentHandler<T> getHandler(final Class<T> type) {
		return componentManager.getHandlerFor(type);
	}

	/**
	 * Returns a new {@link DustContext} {@link Builder} instance. By default its
	 * configured to not respect each {@link DustStep}'s order.
	 *
	 * @return new {@link Builder} instance.
	 */
	public static final Builder builder() {
		return new Builder();
	}

	/**
	 * <p>
	 * {@link Builder} class to configure and construct {@link DustContext}
	 * instances from.
	 * </p>
	 *
	 * <p>
	 * Can be configured with {@link DustStep}s, configure if the {@link DustStep}
	 * initialization and/or processing should be done in a specific order.
	 * </p>
	 *
	 * @author dustContributor
	 *
	 */
	public static final class Builder {
		/**
		 * Types that will be present in all entities of the {@link DustContext}.
		 */
		private final HashSet<Class<? extends Component>> componentTypes;
		/**
		 * Steps, their filter builders and their priorities/orders, matched by
		 * position.
		 */
		private final ArrayList<Function<EntityGroups, DustStep>> steps;
		private final IntBag orders;
		/**
		 * Arbitrary data parameter for the {@link DustContext}.
		 */
		private Object data;
		/**
		 * Initialize {@link DustStep}s in the order specified or not.
		 */
		private boolean initializeByOrder;
		/**
		 * Process {@link DustStep}s in the order specified or not.
		 */
		private boolean processByOrder;

		Builder() {
			this.initializeByOrder = false;
			this.processByOrder = false;
			this.componentTypes = new HashSet<>();
			this.steps = new ArrayList<>(16);
			this.orders = new IntBag(16);
		}

		/**
		 * This allows you to place an arbitrary object as field of your
		 * {@link DustContext} instance. This way if you need any sort of shared
		 * resources between your systems, you can place them in a data structure of
		 * your choice here.
		 *
		 * @param data to set in the {@link DustContext} instance.
		 * @return this {@link Builder} instance.
		 */
		public final Builder data(final Object data) {
			this.data = data;
			return this;
		}

		/**
		 * Configures this {@link Builder} response to each of the {@link DustStep}'s
		 * order.
		 *
		 * <p>
		 * If set to 'true', the produced {@link DustContext} will initialize
		 * {@link DustStep} instances respecting the passed order (from lower to upper).
		 * Otherwise if set to 'false' it will ignore it and initialize {@link DustStep}
		 * instances in the order they were added to this {@link Builder}.
		 * </p>
		 *
		 * <p>
		 * 'false' by default.
		 * </p>
		 *
		 * @param initializeByOrder value.
		 * @return this {@link Builder} instance.
		 */
		public final Builder initializeByOrder(final boolean initializeByOrder) {
			this.initializeByOrder = initializeByOrder;
			return this;
		}

		/**
		 * Configures this {@link Builder} response to each of the {@link DustStep}'s
		 * order.
		 * <p>
		 * If set to 'true', the produced {@link DustContext} will process
		 * {@link DustStep} instances respecting the passed order (from lower to upper).
		 * Otherwise if set to 'false' it will ignore it and process {@link DustStep}
		 * instances in the order they were added to this {@link Builder}.
		 * </p>
		 * <p>
		 * 'false' by default.
		 * </p>
		 *
		 * @param processByOrder value.
		 * @return this {@link Builder} instance.
		 */
		public final Builder processByOrder(final boolean processByOrder) {
			this.processByOrder = processByOrder;
			return this;
		}

		/**
		 * Convenience overload for {@link DustStep}s that need no filtered entity
		 * lists.
		 *
		 * @param step to add.
		 * @return this builder instance.
		 *
		 * @see Builder#step(Function)
		 */
		public final Builder step(final Supplier<DustStep> step) {
			return step(step, 0);
		}

		/**
		 * Convenience overload for {@link DustStep}s that need no filtered entity
		 * lists.
		 *
		 * @param step  to add.
		 * @param order of the instance.
		 * @return this builder instance.
		 *
		 * @see Builder#step(Function, int)
		 */
		public final Builder step(final Supplier<DustStep> step, final int order) {
			DustException.enforceNonNull(this, step, "step");
			return step(g -> step.get(), order);
		}

		/**
		 * Add an {@link DustStep} constructor into this {@link DustContext}
		 * {@link Builder}. Once constructed, the {@link DustStep} can be retrieved
		 * later via the {@link DustContext} instance. The {@link DustContext} instance
		 * constructed with this {@link Builder} will notify the {@link DustStep} of any
		 * entity changes. <b>Its order will be set to 0.</b>
		 *
		 * @param step to add.
		 * @return this builder instance.
		 */
		public final Builder step(final Function<EntityGroups, DustStep> step) {
			return step(step, 0);
		}

		/**
		 * Add an {@link DustStep} constructor into this {@link DustContext}
		 * {@link Builder}. Once constructed, the {@link DustStep} can be retrieved
		 * later via the {@link DustContext} instance. The {@link DustContext} instance
		 * constructed with this {@link Builder} will notify the {@link DustStep} of any
		 * entity changes.
		 *
		 * @param step  to add.
		 * @param order of the instance.
		 * @return this {@link Builder} instance.
		 */
		public final Builder step(final Function<EntityGroups, DustStep> step, final int order) {
			DustException.enforceNonNull(this, step, "step");
			this.steps.add(step);
			this.orders.add(order);
			return this;
		}

		/**
		 * Adds a component type that the {@link DustContext} instance created by this
		 * {@link Builder} will be able to handle.
		 *
		 * @param type of the {@link Component}.
		 * @return this {@link Builder} instance.
		 */
		public final Builder componentType(final Class<? extends Component> type) {
			componentTypes.add(DustException.enforceNonNull(this, type, "type"));
			return this;
		}

		/**
		 * Bulk varargs version of {@link #componentType(Class)}.
		 *
		 * @param types of the {@link Component}s.
		 * @return this {@link Builder} instance.
		 */
		@SafeVarargs
		public final Builder componentTypes(final Class<? extends Component>... types) {
			for (final Class<? extends Component> type : types) {
				componentType(type);
			}
			return this;
		}

		/**
		 * Bulk {@link Iterable} version of {@link #componentType(Class)}.
		 *
		 * @param types of the {@link Component}s.
		 * @return this {@link Builder} instance.
		 */
		public final Builder componentTypes(final Iterable<Class<? extends Component>> types) {
			for (final Class<? extends Component> type : types) {
				componentType(type);
			}
			return this;
		}

		/**
		 * Builds a {@link DustContext} instance with this {@link Builder}'s
		 * {@link DustStep} configuration.
		 *
		 * @return new {@link DustContext} instance.
		 */
		public final DustContext build() {
			/*
			 * Mapping from step to order, we fill it later by passing it to the method that
			 * creates steps. The usage is a bit messy, maybe it could be refined.
			 */
			var orderMap = new IdentityHashMap<DustStep, Integer>(this.steps.size());
			// Capture the map and use it for sorter by order later.
			final Comparator<DustStep> compareByOrder = (a, b) -> orderMap.get(a).compareTo(orderMap.get(b));
			/*
			 * Construct component manager, used by context instance and filter creation.
			 */
			var cm = new ComponentManager(componentTypes);
			// Make the filter manager which will initialize all the filter groups.
			var filterManager = new EntityGroups(cm);
			// Use all the constructors to create the steps.
			DustStep[] steps = createSteps(orderMap, filterManager);

			// Compose context parameters and construct it.
			var context = new DustContext(composeWorldParams(steps, filterManager, compareByOrder, cm));
			// Set the owner for all steps.
			for (int s = steps.length; s-- > 0;) {
				steps[s].context(context);
			}
			// Do the init step on steps.
			stepInitialization(steps, compareByOrder, context);
			// Now return fully constructed context instance.
			return context;
		}

		private final DustStep[] createSteps(final IdentityHashMap<DustStep, Integer> stepToOrder,
				final EntityGroups groups) {
			int size = steps.size();
			var result = new DustStep[size];
			for (int i = 0; i < size; ++i) {
				// Fetch the step constructor.
				Function<EntityGroups, DustStep> ctor = steps.get(i);
				// Fetch the priority/order.
				int order = orders.getUnsafe(i);
				/*
				 * If its the dummy builder, just use the empty filter, otherwise build the new
				 * filter to use.
				 */
				// Construct step and set it on the same position.
				result[i] = ctor.apply(groups);
				// Map its order.
				stepToOrder.put(result[i], Integer.valueOf(order));
			}

			return result;
		}

		private final ContextParams composeWorldParams(final DustStep[] steps, final EntityGroups filterManager,
				final Comparator<DustStep> comparator, final ComponentManager cm) {
			// Make a defensive copy.
			var processSteps = steps.clone();

			if (processByOrder) {
				// Sort if there was an order specified.
				Arrays.sort(processSteps, comparator);
			}

			// Construct entity manager, used by the context instance.
			var em = new EntityManager();

			/*
			 * World will respect the order in which processSteps is for calli@ng 'process'
			 * on each of them.
			 */
			return new ContextParams(cm, em, processSteps, filterManager.groups(), data);
		}

		private final void stepInitialization(final DustStep[] steps, final Comparator<DustStep> comparator,
				final DustContext context) {
			// Make a defensive copy.
			var initializeSteps = steps.clone();

			if (initializeByOrder) {
				// Sort if there was an order specified.
				Arrays.sort(initializeSteps, comparator);
			}

			// First step is to inject the steps.
			Injector.init(context, initializeSteps);

			// Second step is to call 'init' on them.
			for (int i = 0; i < initializeSteps.length; ++i) {
				initializeSteps[i].init();
			}
		}
	}
}
