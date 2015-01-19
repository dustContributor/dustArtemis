package com.artemis.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.artemis.EntityObserver;
import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;

/**
 * If you need to tag any entity, use this. A typical usage would be to tag
 * entities such as "PLAYER", "BOSS" or something that is very unique.
 * 
 * @author Arni Arent
 *
 */
public class TagManager extends EntityObserver
{
	private final Map<String, Integer> entitiesByTag;
	private final Map<Integer, String> tagsByEntity;

	public TagManager ()
	{
		entitiesByTag = new HashMap<>();
		tagsByEntity = new HashMap<>();
	}

	public void register ( final String tag, final int eid )
	{
		entitiesByTag.put( tag, Integer.valueOf( eid ) );
		tagsByEntity.put( Integer.valueOf( eid ), tag );
	}

	public void unregister ( final String tag )
	{
		tagsByEntity.remove( entitiesByTag.remove( tag ) );
	}

	public boolean isRegistered ( final String tag )
	{
		return entitiesByTag.containsKey( tag );
	}

	public int getEntity ( final String tag )
	{
		return entitiesByTag.get( tag ).intValue();
	}

	public Collection<String> getRegisteredTags ()
	{
		return tagsByEntity.values();
	}

	@Override
	public void deleted ( final ImmutableIntBag entities )
	{
		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final String removedTag = tagsByEntity.remove( Integer.valueOf( array[i] ) );

			if ( removedTag != null )
			{
				entitiesByTag.remove( removedTag );
			}
		}
	}

}
