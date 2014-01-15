package com.artemis;

import com.artemis.utils.Bag;

/**
 * High performance component retrieval from entities. Use this wherever you
 * need to retrieve components from entities often and fast.
 * 
 * @author Arni Arent
 * 
 * @param <T>
 *            the class type of the component
 */
public class ComponentMapper<T extends Component>
{
	private final Bag<Component> components;

	private ComponentMapper ( Class<T> type, World world )
	{
		components = world.getComponentManager().getComponentsByType( ComponentType.getTypeFor( type ) );
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
	@SuppressWarnings("unchecked")
	public T get ( Entity e )
	{
		return (T) components.get( e.getId() );
	}

	/**
	 * Fast and safe retrieval of a component for this entity. If the entity
	 * does not have this component then null is returned.
	 * 
	 * @param e
	 *            the entity that should possess the component
	 * @return the instance of the component
	 */
	@SuppressWarnings("unchecked")
	public T getSafe ( Entity e )
	{
		if ( components.isIndexWithinBounds( e.getId() ) )
		{
			return (T) components.get( e.getId() );
		}
		
		return null;
	}

	/**
	 * Checks if the entity has this type of component.
	 * 
	 * @param e
	 *            the entity to check
	 * @return true if the entity has this component type, false if it doesn't.
	 */
	public boolean has ( Entity e )
	{
		return getSafe( e ) != null;
	}

	/**
	 * Returns a component mapper for this type of components.
	 * 
	 * @param type
	 *            the type of components this mapper uses.
	 * @param world
	 *            the world that this component mapper should use.
	 * @return a new mapper.
	 */
	public static final <T extends Component> ComponentMapper<T> getFor ( Class<T> type, World world )
	{
		return new ComponentMapper<>( type, world );
	}

}
