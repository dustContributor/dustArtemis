package com.artemis;

/**
 * Manager.
 * 
 * @author Arni Arent
 * 
 */
public abstract class Manager extends EntityObserver
{
	protected World world;

	protected void initialize ()
	{
		// Empty by default.
	}
}
