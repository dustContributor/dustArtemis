package com.artemis;

import com.artemis.utils.ImmutableIntBag;

/**
 * @author Arni Arent
 * @author dustContributor
 */
@SuppressWarnings( "unused" )
public abstract class EntityObserver
{
	/** Active by default. */
	private boolean active = true;

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

	public void added ( ImmutableIntBag entities )
	{
		// Empty by default.
	}

	public void changed ( ImmutableIntBag entities )
	{
		// Empty by default.
	}

	public void deleted ( ImmutableIntBag entities )
	{
		// Empty by default.
	}

	public void enabled ( ImmutableIntBag entities )
	{
		// Empty by default.
	}

	public void disabled ( ImmutableIntBag entities )
	{
		// Empty by default.
	}

	/**
	 * Processes all entities that were inserted or removed fin this particular
	 * observer.
	 */
	void processModifiedEntities ()
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
	 *          {@code false} otherwise.
	 */
	public void setActive ( final boolean active )
	{
		this.active = active;
	}
}
