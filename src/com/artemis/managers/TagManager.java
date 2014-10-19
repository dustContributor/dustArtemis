package com.artemis.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.artemis.Entity;
import com.artemis.EntityObserver;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

/**
 * If you need to tag any entity, use this. A typical usage would be to tag
 * entities such as "PLAYER", "BOSS" or something that is very unique.
 * 
 * @author Arni Arent
 *
 */
public class TagManager  extends EntityObserver
{
	private final Map<String, Entity> entitiesByTag;
	private final Map<Entity, String> tagsByEntity;

	public TagManager ()
	{
		entitiesByTag = new HashMap<>();
		tagsByEntity = new HashMap<>();
	}

	public void register ( final String tag, final Entity e )
	{
		entitiesByTag.put( tag, e );
		tagsByEntity.put( e, tag );
	}

	public void unregister ( final String tag )
	{
		tagsByEntity.remove( entitiesByTag.remove( tag ) );
	}

	public boolean isRegistered ( final String tag )
	{
		return entitiesByTag.containsKey( tag );
	}

	public Entity getEntity ( final String tag )
	{
		return entitiesByTag.get( tag );
	}

	public Collection<String> getRegisteredTags ()
	{
		return tagsByEntity.values();
	}
	
	@Override
	public void deleted ( final ImmutableBag<Entity> entities )
	{
		final Entity[] array = ((Bag<Entity>) entities).data();
		final int size = entities.size();
		
		for ( int i = 0; i < size; ++i )
		{
			final String removedTag = tagsByEntity.remove( array[i] );
			
			if ( removedTag != null )
			{
				entitiesByTag.remove( removedTag );
			}
		}
	}

}
