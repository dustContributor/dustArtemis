package com.artemis;

/**
 * @author Arni Arent
 * @author dustContributor
 */
public abstract class EntityObserver
{
	/** Active by default. */
	private boolean active = true;

	private World world;

	protected void init ()
	{
		// Empty by default.
	}

	protected void process ()
	{
		// Empty by default.
	}

	protected void dispose ()
	{
		// Empty by default.
	}

	/**
	 * @return the {@link World} instance that owns this observer.
	 */
	protected final World world ()
	{
		return this.world;
	}

	final void world ( final World world )
	{
		this.world = world;
	}

	/**
	 * A convenience method that delegates to {@link World#data()}.
	 *
	 * @see World#data()
	 * @return arbitrary data object retrieved from the world instance.
	 */
	protected final <T> T data ()
	{
		return this.world.data();
	}

	/**
	 * Returns the state of this observer
	 *
	 * @return {@code true} if its active and will get processed, {@code false}
	 *         otherwise.
	 */
	public final boolean isActive ()
	{
		return active;
	}

	/**
	 * Sets the state of this observer.
	 *
	 * @param active {@code true} if its active and will get processed,
	 *          {@code false} otherwise.
	 */
	public final void active ( final boolean active )
	{
		this.active = active;
	}
}
