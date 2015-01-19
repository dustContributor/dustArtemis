package com.artemis.systems;

import com.artemis.Aspect;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableIntBag;

/**
 * This system has an empty aspect so it processes no entities, but it still
 * gets invoked. You can use this system if you need to execute some game logic
 * and not have to concern yourself about aspects or entities.
 * 
 * @author Arni Arent
 * 
 */
public abstract class VoidEntitySystem extends EntitySystem
{

	public VoidEntitySystem ()
	{
		super( Aspect.EMPTY_ASPECT );
	}

	@Override
	protected final void processEntities ( final ImmutableIntBag entities )
	{
		processSystem();
	}

	protected abstract void processSystem ();

}
