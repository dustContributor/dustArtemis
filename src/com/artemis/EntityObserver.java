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

	/**
	 * This will be called with the entity groups present in the world. You can
	 * ask it for entity groups matching a certain filter.
	 *
	 * <p>
	 * <b>NOTE:</b> 'world' instance isn't present at this point in the observer
	 * yet.
	 * </p>
	 *
	 * @param groups to fetch matching entities from.
	 */
	protected void groups ( final EntityGroups groups )
	{
		// Empty by default.
	}

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
