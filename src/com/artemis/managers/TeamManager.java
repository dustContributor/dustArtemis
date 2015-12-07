package com.artemis.managers;

import java.util.HashMap;

import com.artemis.EntityObserver;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

/**
 * Use this class together with PlayerManager.
 * 
 * You may sometimes want to create teams in your game, so that some players are
 * team mates.
 * 
 * A player can only belong to a single team.
 * 
 * @author Arni Arent
 * 
 */
public class TeamManager extends EntityObserver
{
	private final HashMap<String, Bag<String>> playersByTeam;
	private final HashMap<String, String> teamByPlayer;

	public TeamManager ()
	{
		playersByTeam = new HashMap<>();
		teamByPlayer = new HashMap<>();
	}

	public String getTeam ( final String player )
	{
		return teamByPlayer.get( player );
	}

	public void setTeam ( final String player, final String team )
	{
		removeFromTeam( player );

		teamByPlayer.put( player, team );

		Bag<String> players = playersByTeam.get( team );

		if ( players == null )
		{
			players = new Bag<>( new String[ImmutableBag.DEFAULT_CAPACITY] );
			playersByTeam.put( team, players );
		}

		players.add( player );
	}

	public ImmutableBag<String> getPlayers ( final String team )
	{
		return playersByTeam.get( team );
	}

	public void removeFromTeam ( final String player )
	{
		final String team = teamByPlayer.remove( player );

		if ( team != null )
		{
			final Bag<String> players = playersByTeam.get( team );

			if ( players != null )
			{
				players.remove( player );
			}
		}
	}

}
