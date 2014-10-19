package com.artemis;

import com.artemis.utils.ImmutableBag;

/**
 * @author Arni Arent
 */
@SuppressWarnings ( "unused" )
public abstract class EntityObserver
{
	private boolean active;

	protected World world;

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
	 * Returns the state of this observer
	 * 
	 * @return {@code true} if its active and will get processed, {@code false}
	 *         otherwise.
	 */
	public boolean isActive ()
	{
		return active;
	}

	/**
	 * Sets the state of this observer.
	 * 
	 * @param active {@code true} if its active and will get processed,
	 *            {@code false} otherwise.
	 */
	public void setActive ( final boolean active )
	{
		this.active = active;
	}
}
