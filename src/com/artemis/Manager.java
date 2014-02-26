package com.artemis;

/**
 * Manager.
 * 
 * @author Arni Arent
 * 
 */
public abstract class Manager implements EntityObserver
{
	protected World world;

	protected abstract void initialize ();

	@Override
	public void added ( Entity e )
	{
		// Empty method.
	}

	@Override
	public void changed ( Entity e )
	{
		// Empty method.
	}

	@Override
	public void deleted ( Entity e )
	{
		// Empty method.
	}

	@Override
	public void disabled ( Entity e )
	{
		// Empty method.
	}

	@Override
	public void enabled ( Entity e )
	{
		// Empty method.
	}
}
