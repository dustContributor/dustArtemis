package com.artemis;

import java.util.BitSet;

import com.artemis.utils.Bag;

public class ComponentManager extends Manager
{
	private final Bag<Bag<Component>> componentsByType;
	private final Bag<Entity> deleted;

	public ComponentManager ()
	{
		componentsByType = new Bag<>();
		deleted = new Bag<>( Entity.class );
	}

	@Override
	protected void initialize ()
	{
		// Empty method.
	}

	private void removeComponentsOfEntity ( final Entity e )
	{
		final BitSet componentBits = e.getComponentBits();
		
		for ( int i = componentBits.nextSetBit( 0 ); i >= 0; i = componentBits.nextSetBit( i + 1 ) )
		{
			componentsByType.get( i ).set( e.getId(), null );
		}
		
		componentBits.clear();
	}

	protected void addComponent ( final Entity e, final ComponentType type, final Component component )
	{
		componentsByType.ensureCapacity( type.getIndex() );

		Bag<Component> components = componentsByType.get( type.getIndex() );
		
		if ( components == null )
		{
			components = new Bag<>( Component.class );
			componentsByType.set( type.getIndex(), components );
		}

		components.set( e.getId(), component );

		e.getComponentBits().set( type.getIndex() );
	}

	protected void removeComponent ( final Entity e, final ComponentType type )
	{
		if ( e.getComponentBits().get( type.getIndex() ) )
		{
			componentsByType.get( type.getIndex() ).set( e.getId(), null );
			e.getComponentBits().clear( type.getIndex() );
		}
	}

	protected Bag<Component> getComponentsByType ( final ComponentType type )
	{
		Bag<Component> components = componentsByType.get( type.getIndex() );
		
		if ( components == null )
		{
			components = new Bag<>( Component.class );
			componentsByType.set( type.getIndex(), components );
		}
		
		return components;
	}

	protected Component getComponent ( final Entity e, final ComponentType type )
	{
		final Bag<Component> components = componentsByType.get( type.getIndex() );
		
		if ( components != null )
		{
			return components.get( e.getId() );
		}
		
		return null;
	}

	public Bag<Component> getComponentsFor ( final Entity e, final Bag<Component> fillBag )
	{
		final BitSet componentBits = e.getComponentBits();

		for ( int i = componentBits.nextSetBit( 0 ); i >= 0; i = componentBits.nextSetBit( i + 1 ) )
		{
			fillBag.add( componentsByType.get( i ).get( e.getId() ) );
		}

		return fillBag;
	}

	@Override
	public void deleted ( final Entity e )
	{
		deleted.add( e );
	}

	protected void clean ()
	{
		if ( deleted.size() > 0 )
		{
			for ( int i = 0; i < deleted.size(); ++i )
			{
				removeComponentsOfEntity( deleted.get( i ) );
			}
			
			deleted.clear();
		}
	}

}
