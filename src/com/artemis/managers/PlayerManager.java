package com.artemis.managers;

import java.util.HashMap;

import com.artemis.Entity;
import com.artemis.Manager;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

/**
 * You may sometimes want to specify to which player an entity belongs to.
 * 
 * An entity can only belong to a single player at a time.
 * 
 * @author Arni Arent
 *
 */
public class PlayerManager extends Manager
{
	private final HashMap<Entity, String> playerByEntity;
	private final HashMap<String, Bag<Entity>> entitiesByPlayer;

	public PlayerManager ()
	{
		playerByEntity = new HashMap<>();
		entitiesByPlayer = new HashMap<>();
	}

	public void setPlayer ( final Entity e, final String player )
	{
		playerByEntity.put( e, player );
		Bag<Entity> entities = entitiesByPlayer.get( player );
		if ( entities == null )
		{
			entities = new Bag<>( Entity.class );
			entitiesByPlayer.put( player, entities );
		}
		entities.add( e );
	}

	public ImmutableBag<Entity> getEntitiesOfPlayer ( final String player )
	{
		Bag<Entity> entities = entitiesByPlayer.get( player );
		if ( entities == null )
		{
			entities = new Bag<>( Entity.class );
		}
		return entities;
	}

	public void removeFromPlayer ( final Entity e )
	{
		final String player = playerByEntity.get( e );
		if ( player != null )
		{
			final Bag<Entity> entities = entitiesByPlayer.get( player );
			if ( entities != null )
			{
				entities.remove( e );
			}
		}
	}

	public String getPlayer ( final Entity e )
	{
		return playerByEntity.get( e );
	}

	@Override
	protected void initialize ()
	{
		// Empty method.
	}

	@Override
	public void deleted ( ImmutableBag<Entity> entities )
	{
		entities.forEach( this::removeFromPlayer );
	}
	
//	@Override
//	public void deleted ( final Entity e )
//	{
//		removeFromPlayer( e );
//	}

}
