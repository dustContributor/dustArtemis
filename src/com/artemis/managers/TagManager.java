package com.artemis.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.artemis.Entity;
import com.artemis.Manager;
import com.artemis.utils.ImmutableBag;

/**
 * If you need to tag any entity, use this. A typical usage would be to tag
 * entities such as "PLAYER", "BOSS" or something that is very unique.
 * 
 * @author Arni Arent
 *
 */
public class TagManager extends Manager
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
	public void deleted ( ImmutableBag<Entity> entities )
	{
		for ( int i = entities.size(); i-- > 0; )
		{
			String removedTag = tagsByEntity.remove( entities.getUnsafe( i ) );
			
			if ( removedTag != null )
			{
				entitiesByTag.remove( removedTag );
			}
		}
	}

//	@Override
//	public void deleted ( final Entity e )
//	{
//		final String removedTag = tagsByEntity.remove( e );
//		if ( removedTag != null )
//		{
//			entitiesByTag.remove( removedTag );
//		}
//	}

	@Override
	protected void initialize ()
	{
		// Empty method.
	}

}
