package com.artemis.systems;

import com.artemis.Aspect;
import com.artemis.EntitySystem;

/**
 * A system that processes entities at a interval in milliseconds. A typical
 * usage would be a collision system or physics system.
 * 
 * @author Arni Arent
 * 
 */
public abstract class IntervalEntitySystem extends EntitySystem
{
	private float interval;
	private boolean intervalPassed;
	
	private float acc;

	public IntervalEntitySystem ( Aspect aspect, float interval )
	{
		super( aspect );
		this.interval = interval;
	}
	
	@Override
	protected void begin ()
	{
		acc += world.getDelta();
		intervalPassed = acc >= interval;
		
		if ( intervalPassed )
		{
			acc -= interval;
		}
	}
	
	protected boolean hasIntervalPassed ()
	{
		return intervalPassed;
	}
	
	public void setInterval ( float interval )
	{
		this.interval = interval;
	}
	
	public float getInterval ()
	{
		return interval;
	}

}
