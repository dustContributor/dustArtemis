package com.artemis.managers;

import java.util.HashMap;

import com.artemis.EntityObserver;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;
import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;

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
public class GroupManager extends EntityObserver
{
	private final HashMap<String, IntBag> entitiesByGroup;
	private final HashMap<Integer, Bag<String>> groupsByEntity;

	public GroupManager ()
	{
		entitiesByGroup = new HashMap<>();
		groupsByEntity = new HashMap<>();
	}

	/**
	 * Set the group of the entity.
	 * 
	 * @param group group to add the entity into.
	 * @param eid entity to add into the group.
	 */
	public void add ( final int eid, final String group )
	{
		IntBag entities = entitiesByGroup.get( group );
		if ( entities == null )
		{
			entities = new IntBag();
			entitiesByGroup.put( group, entities );
		}
		entities.add( eid );

		Bag<String> groups = groupsByEntity.get( Integer.valueOf( eid ) );
		if ( groups == null )
		{
			groups = new Bag<>( String.class );
			groupsByEntity.put( Integer.valueOf( eid ), groups );
		}
		groups.add( group );
	}

	/**
	 * Remove the entity from the specified group.
	 * 
	 * @param eid entity to be removed.
	 * @param group where the entity will be removed from.
	 */
	public void remove ( final int eid, final String group )
	{
		final IntBag entities = entitiesByGroup.get( group );
		if ( entities != null )
		{
			entities.remove( eid );
		}

		final Bag<String> groups = groupsByEntity.get( Integer.valueOf( eid ) );
		if ( groups != null )
		{
			groups.remove( group );
			// Artemis-ODB guys did this down here, commented out.
			// if (groups.size() == 0) groupsByEntity.remove(e);
		}
	}

	public void removeFromAllGroups ( final int eid )
	{
		final Bag<String> groups = groupsByEntity.get( Integer.valueOf( eid ) );

		if ( groups != null )
		{
			final String[] grpArray = groups.data();
			final int size = groups.size();

			for ( int i = 0; i < size; ++i )
			{
				final IntBag entities = entitiesByGroup.get( grpArray[i] );

				if ( entities != null )
				{
					entities.remove( eid );
				}
			}

			groupsByEntity.remove( Integer.valueOf( eid ) );
		}
	}

	/**
	 * Get all entities that belong to the provided group.
	 * 
	 * @param group name of the group.
	 * @return read-only bag of entities belonging to the group.
	 */
	public ImmutableIntBag getEntities ( final String group )
	{
		IntBag entities = entitiesByGroup.get( group );
		if ( entities == null )
		{
			entities = new IntBag();
			entitiesByGroup.put( group, entities );
		}
		return entities;
	}

	/**
	 * @param eid entity
	 * @return the groups the entity belongs to, null if none.
	 */
	public ImmutableBag<String> getGroups ( final int eid )
	{
		return groupsByEntity.get( Integer.valueOf( eid ) );
	}

	/**
	 * Checks if the entity belongs to any group.
	 * 
	 * @param eid the entity to check.
	 * @return true if it is in any group, false if none.
	 */
	public boolean isInAnyGroup ( final int eid )
	{
		return getGroups( eid ) != null;
	}

	/**
	 * Check if the entity is in the supplied group.
	 * 
	 * @param group the group to check in.
	 * @param eid the entity to check for.
	 * @return true if the entity is in the supplied group, false if not.
	 */
	public boolean isInGroup ( final int eid, final String group )
	{
		final Bag<String> groups = groupsByEntity.get( Integer.valueOf( eid ) );

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
	public void deleted ( final ImmutableIntBag entities )
	{
		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			removeFromAllGroups( array[i] );
		}
	}

}
