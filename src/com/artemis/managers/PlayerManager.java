package com.artemis.managers;

import java.util.HashMap;

import com.artemis.EntityObserver;
import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;

/**
 * You may sometimes want to specify to which player an entity belongs to.
 * 
 * An entity can only belong to a single player at a time.
 * 
 * @author Arni Arent
 *
 */
public class PlayerManager extends EntityObserver
{
	private final HashMap<Integer, String> playerByEntity;
	private final HashMap<String, IntBag> entitiesByPlayer;

	public PlayerManager ()
	{
		playerByEntity = new HashMap<>();
		entitiesByPlayer = new HashMap<>();
	}

	public void setPlayer ( final int eid, final String player )
	{
		playerByEntity.put( Integer.valueOf( eid ), player );
		IntBag entities = entitiesByPlayer.get( player );
		if ( entities == null )
		{
			entities = new IntBag();
			entitiesByPlayer.put( player, entities );
		}
		entities.add( eid );
	}

	public ImmutableIntBag getEntitiesOfPlayer ( final String player )
	{
		IntBag entities = entitiesByPlayer.get( player );
		if ( entities == null )
		{
			entities = new IntBag();
		}
		return entities;
	}

	public void removeFromPlayer ( final int eid )
	{
		final String player = playerByEntity.get( Integer.valueOf( eid ) );
		if ( player != null )
		{
			final IntBag entities = entitiesByPlayer.get( player );
			if ( entities != null )
			{
				entities.remove( eid );
			}
		}
	}

	public String getPlayer ( final int eid )
	{
		return playerByEntity.get( Integer.valueOf( eid ) );
	}

	@Override
	public void deleted ( final ImmutableIntBag entities )
	{
		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			removeFromPlayer( array[i] );
		}
	}

}
