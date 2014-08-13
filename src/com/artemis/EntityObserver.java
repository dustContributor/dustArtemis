package com.artemis;

import com.artemis.utils.ImmutableBag;

/**
 * @author Arni Arent
 */
@SuppressWarnings ( "unused" )
public abstract class EntityObserver
{
	public void added ( ImmutableBag<Entity> entities )
	{
		// Empty by default.
	}

	public void changed ( ImmutableBag<Entity> entities )
	{
		// Empty by default.
	}

	public void deleted ( ImmutableBag<Entity> entities )
	{
		// Empty by default.
	}

	public void enabled ( ImmutableBag<Entity> entities )
	{
		// Empty by default.
	}

	public void disabled ( ImmutableBag<Entity> entities )
	{
		// Empty by default.
	}
}
