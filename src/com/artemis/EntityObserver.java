package com.artemis;

import java.io.Closeable;
import java.io.IOException;

import com.artemis.utils.ImmutableBag;

/**
 * @author Arni Arent
 */
@SuppressWarnings ( "unused" )
public abstract class EntityObserver
{
	protected World world;
	protected boolean active;

	public void init ()
	{
		// Empty by default.
	}

	public void process ()
	{
		// Empty by default.
	}

	public void dispose ()
	{
		// Empty by default.
	}
	
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

	/**
	 * 
	 * @return true if the observer should be processed, false if not.
	 */
	public boolean isActive ()
	{
		return active;
	}

	public void setActive ( final boolean active )
	{
		this.active = active;
	}
}
