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
	ComponentMapper ( final Class<T> type, final World world )
	{
		components = (ImmutableBag<T>) world.getComponentManager().getComponentsByType( type );
	}

	/**
	 * Fast but unsafe retrieval of a component for this entity. No bounding
	 * checks, so this could throw an ArrayIndexOutOfBoundsExeption, however in
	 * most scenarios you already know the entity possesses this component.
	 * 
	 * @param entity that should possess the component
	 * @return the instance of the component
	 */
	public final T get ( final Entity entity )
	{
		return get( entity.id );
	}
	
	/**
	 * Fast but unsafe retrieval of a component for this entity. No bounding
	 * checks, so this could throw an ArrayIndexOutOfBoundsExeption, however in
	 * most scenarios you already know the entity possesses this component.
	 * 
	 * @param entityID of the entity that should have the component.
	 * @return the instance of the component
	 */
	public final T get ( final int entityID )
	{
		return components.getUnsafe( entityID );
	}

	/**
	 * Fast and safe retrieval of a component for this entity. If the entity
	 * does not have this component then null is returned.
	 * 
	 * @param entity that could have the component
	 * @return the instance of the component
	 */
	public final T getSafe ( final Entity entity )
	{
		return getSafe( entity.id );
	}
	
	/**
	 * Fast and safe retrieval of a component for this entity. If the entity
	 * does not have this component then null is returned.
	 * 
	 * @param entityID of the entity that could have the component.
	 * @return the instance of the component
	 */
	public final T getSafe ( final int entityID )
	{
		return components.get( entityID );
	}

	/**
	 * Checks if the entity has this type of component.
	 * 
	 * @param entity that could have the component.
	 * @return true if the entity has this component type, false if it doesn't.
	 */
	public final boolean has ( final Entity entity )
	{
		return has( entity.id );
	}
	
	/**
	 * Checks if the entity has this type of component.
	 * 
	 * @param entityID of the entity that could have the component.
	 * @return true if the entity has this component type, false if it doesn't.
	 */
	public final boolean has ( final int entityID )
	{
		return components.get( entityID ) != null;
	}

}
