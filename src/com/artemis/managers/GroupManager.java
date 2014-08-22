package com.artemis.managers;

import java.util.HashMap;

import com.artemis.Entity;
import com.artemis.Manager;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

/**
 * If you need to group your entities together, e.g. tanks going into "units"
 * group or explosions into "effects", then use this manager. You must retrieve
 * it using world instance.
 * 
 * A entity can be assigned to more than one group.
 * 
 * @author Arni Arent
 * 
 */
public class GroupManager extends Manager
{
	private final HashMap<String, Bag<Entity>> entitiesByGroup;
	private final HashMap<Entity, Bag<String>> groupsByEntity;

	public GroupManager ()
	{
		entitiesByGroup = new HashMap<>();
		groupsByEntity = new HashMap<>();
	}

	/**
	 * Set the group of the entity.
	 * 
	 * @param group
	 *            group to add the entity into.
	 * @param e
	 *            entity to add into the group.
	 */
	public void add ( final Entity e, final String group )
	{
		Bag<Entity> entities = entitiesByGroup.get( group );
		if ( entities == null )
		{
			entities = new Bag<>( Entity.class );
			entitiesByGroup.put( group, entities );
		}
		entities.add( e );

		Bag<String> groups = groupsByEntity.get( e );
		if ( groups == null )
		{
			groups = new Bag<>( String.class );
			groupsByEntity.put( e, groups );
		}
		groups.add( group );
	}

	/**
	 * Remove the entity from the specified group.
	 * 
	 * @param e entity to be removed.
	 * @param group where the entity will be removed from.
	 */
	public void remove ( final Entity e, final String group )
	{
		final Bag<Entity> entities = entitiesByGroup.get( group );
		if ( entities != null )
		{
			entities.remove( e );
		}

		final Bag<String> groups = groupsByEntity.get( e );
		if ( groups != null )
		{
			groups.remove( group );
			// Artemis-ODB guys did this down here, commented out.
			// if (groups.size() == 0) groupsByEntity.remove(e);
		}
	}

	public void removeFromAllGroups ( final Entity e )
	{
		final Bag<String> groups = groupsByEntity.get( e );

		if ( groups != null )
		{
			final String[] grpArray = groups.data();
			final int size = groups.size();

			for ( int i = 0; i < size; ++i )
			{
				final Bag<Entity> entities = entitiesByGroup.get( grpArray[i] );

				if ( entities != null )
				{
					entities.remove( e );
				}
			}

			groupsByEntity.remove( e );
		}
	}

	/**
	 * Get all entities that belong to the provided group.
	 * 
	 * @param group
	 *            name of the group.
	 * @return read-only bag of entities belonging to the group.
	 */
	public ImmutableBag<Entity> getEntities ( final String group )
	{
		Bag<Entity> entities = entitiesByGroup.get( group );
		if ( entities == null )
		{
			entities = new Bag<>( Entity.class );
			entitiesByGroup.put( group, entities );
		}
		return entities;
	}

	/**
	 * @param e
	 *            entity
	 * @return the groups the entity belongs to, null if none.
	 */
	public ImmutableBag<String> getGroups ( final Entity e )
	{
		return groupsByEntity.get( e );
	}

	/**
	 * Checks if the entity belongs to any group.
	 * 
	 * @param e
	 *            the entity to check.
	 * @return true if it is in any group, false if none.
	 */
	public boolean isInAnyGroup ( final Entity e )
	{
		return getGroups( e ) != null;
	}

	/**
	 * Check if the entity is in the supplied group.
	 * 
	 * @param group
	 *            the group to check in.
	 * @param e
	 *            the entity to check for.
	 * @return true if the entity is in the supplied group, false if not.
	 */
	public boolean isInGroup ( final Entity e, final String group )
	{
		final Bag<String> groups = groupsByEntity.get( e );

		if ( groups != null )
		{
			final String[] grpArray = groups.data();
			final int size = groups.size();

			for ( int i = 0; i < size; ++i )
			{
				if ( group.equals( grpArray[i] ) )
				{
					return true;
				}
			}
		}

		return false;
	}
	
	@Override
	public void deleted ( final ImmutableBag<Entity> entities )
	{
		final Entity[] array = ((Bag<Entity>) entities).data();
		final int size = entities.size();
		
		for ( int i = 0; i < size; ++i )
		{
			removeFromAllGroups( array[i] );
		}
	}

}
