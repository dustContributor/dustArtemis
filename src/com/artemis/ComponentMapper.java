package com.artemis;

import com.artemis.utils.ImmutableBag;

/**
 * High performance component retrieval from entities. Use this wherever you
 * need to retrieve components from entities often and fast.
 * 
 * @author Arni Arent
 * 
 * @param <T>
 *            the class type of the component
 */
public final class ComponentMapper<T extends Component>
{
	private final ImmutableBag<T> components;

	/**
	 * Creates a component mapper for this type of components.
	 * 
	 * @param type
	 *            the type of components this mapper uses.
	 * @param world
	 *            the world that this component mapper should use.
	 */
	@SuppressWarnings ( "unchecked" )
	public ComponentMapper ( final Class<T> type, final World world )
	{
		components = (ImmutableBag<T>) world.getComponentManager().getComponentsByType( type );
	}

	/**
	 * Fast but unsafe retrieval of a component for this entity. No bounding
	 * checks, so this could throw an ArrayIndexOutOfBoundsExeption, however in
	 * most scenarios you already know the entity possesses this component.
	 * 
	 * @param e
	 *            the entity that should possess the component
	 * @return the instance of the component
	 */
	public final T get ( final Entity e )
	{
		return components.getUnsafe( e.id );
	}

	/**
	 * Fast and safe retrieval of a component for this entity. If the entity
	 * does not have this component then null is returned.
	 * 
	 * @param e
	 *            the entity that should possess the component
	 * @return the instance of the component
	 */
	public final T getSafe ( final Entity e )
	{
		return components.get( e.id );
	}

	/**
	 * Checks if the entity has this type of component.
	 * 
	 * @param e
	 *            the entity to check
	 * @return true if the entity has this component type, false if it doesn't.
	 */
	public final boolean has ( final Entity e )
	{
		return components.get( e.id ) != null;
	}

}
