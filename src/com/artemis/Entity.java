package com.artemis;

import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.Bag;

/**
 * The entity class. Cannot be instantiated outside the framework, you must
 * create new entities using World.
 * 
 * <p>
 * dustArtemis: Instead of an UUID instance, now the unique ID is given by an
 * AtomicInteger that gets incremented each time an entity needs a new unique
 * id. This avoids the not-so-cheap UUID object initialization and its
 * "unique enough" for most cases. ie, you'll need to cover all the entire 32
 * bit range of an integer for it to start overlapping ids.
 * </p>
 * 
 * @author Arni Arent
 * 
 */
public final class Entity
{
	/**
	 * The internal id for this entity within the framework. No other entity
	 * will have the same ID, but ID's are however reused so another entity may
	 * acquire this ID if the previous entity was deleted.
	 */
	public final int id;

	/**
	 * BitSet instance containing bits of the components the entity possesses.
	 */
	final OpenBitSet componentBits;

	/**
	 * BitSet instance containing bits signaling which systems this entity is
	 * active in.
	 */
	final OpenBitSet systemBits;

	/** World instance where this Entity is in. */
	private final World world;

	Entity ( final World world, final int id )
	{
		this.world = world;
		this.id = id;
		this.systemBits = new OpenBitSet();
		this.componentBits = new OpenBitSet();
	}

	@Override
	public String toString ()
	{
		return "Entity[" + id + "]";
	}

	/**
	 * Add a component to this entity.
	 * 
	 * @param component
	 *            to add to this entity
	 * 
	 * @return this entity for chaining.
	 */
	public Entity addComponent ( final Component component )
	{
		world.getComponentManager().addComponent( this, component );
		return this;
	}

	/**
	 * Remove component by its type.
	 * 
	 * @param type
	 *            of the component to be removed.
	 * 
	 * @return this entity for chaining.
	 */
	public Entity removeComponent ( final Class<? extends Component> type )
	{
		world.getComponentManager().removeComponent( this, type );
		return this;
	}

	/**
	 * Checks if the entity has been added to the world and has not been deleted
	 * from it. If the entity has been disabled this will still return true.
	 * 
	 * @return if it's active.
	 */
	public boolean isActive ()
	{
		return world.getEntityManager().isActive( id );
	}

	/**
	 * Will check if the entity is enabled in the world. By default all entities
	 * that are added to world are enabled, this will only return false if an
	 * entity has been explicitly disabled.
	 * 
	 * @return if it's enabled
	 */
	public boolean isEnabled ()
	{
		return world.getEntityManager().isEnabled( id );
	}

	/**
	 * Slower retrieval of components from this entity. The recommended way to
	 * retrieve components from an entity is using the ComponentMapper. Is fine
	 * to use e.g. when creating new entities and setting data in components.
	 * 
	 * @param <T>
	 *            the expected return component type.
	 * @param type
	 *            the expected return component type.
	 * @return component that matches, or null if none is found.
	 */
	@SuppressWarnings ( "unchecked" )
	public <T extends Component> T getComponent ( final Class<T> type )
	{
		return (T) world.getComponentManager().getComponent( this, type );
	}

	/**
	 * Returns a bag of all components this entity has. You need to reset the
	 * bag yourself if you intend to fill it more than once.
	 * 
	 * @param fillBag
	 *            the bag to put the components into.
	 * @return the fillBag with the components in.
	 */
	public Bag<Component> getComponents ( final Bag<Component> fillBag )
	{
		return world.getComponentManager().getComponentsFor( this, fillBag );
	}

	/**
	 * Refresh all changes to components for this entity. After adding or
	 * removing components, you must call this method. It will update all
	 * relevant systems. It is typical to call this after adding components to a
	 * newly created entity.
	 */
	public void addToWorld ()
	{
		world.addEntity( this );
	}

	/**
	 * This entity has changed, a component added or deleted.
	 */
	public void changedInWorld ()
	{
		world.changedEntity( this );
	}

	/**
	 * Delete this entity from the world.
	 */
	public void deleteFromWorld ()
	{
		world.deleteEntity( this );
	}

	/**
	 * (Re)enable the entity in the world, after it having being disabled. Won't
	 * do anything unless it was already disabled.
	 */
	public void enable ()
	{
		world.enable( this );
	}

	/**
	 * Disable the entity from being processed. Won't delete it, it will
	 * continue to exist but won't get processed.
	 */
	public void disable ()
	{
		world.disable( this );
	}

	/**
	 * Returns the world this entity belongs to.
	 * 
	 * @return world of entity.
	 */
	public World getWorld ()
	{
		return world;
	}

}
