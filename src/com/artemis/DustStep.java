package com.artemis;

/**
 * This class represents a step in the {@link DustContext} that owns it. It is a
 * singular piece of logic that may or may not process entities.
 * 
 * @author dustContributor
 */
public abstract class DustStep
{
	/** Active by default. */
	private boolean active = true;

	private DustContext context;

	protected void init ()
	{
		// Empty by default.
	}

	protected void run ()
	{
		// Empty by default.
	}

	protected void dispose ()
	{
		// Empty by default.
	}

	/**
	 * @return the {@link DustContext} instance that owns this {@link DustStep}.
	 */
	protected final DustContext context ()
	{
		return this.context;
	}

	final void context ( final DustContext context )
	{
		this.context = context;
	}

	/**
	 * A convenience method that delegates to {@link DustContext#data()}.
	 *
	 * @see DustContext#data()
	 * @return arbitrary data object retrieved from the world instance.
	 */
	protected final <T> T data ()
	{
		return this.context.data();
	}

	/**
	 * Returns the state of this {@link DustStep}
	 *
	 * @return {@code true} if its active and will get processed, {@code false}
	 *         otherwise.
	 */
	public final boolean isActive ()
	{
		return active;
	}

	/**
	 * Sets the state of this {@link DustStep}.
	 *
	 * @param active {@code true} if its active and will get processed,
	 *          {@code false} otherwise.
	 */
	public final void active ( final boolean active )
	{
		this.active = active;
	}
}
