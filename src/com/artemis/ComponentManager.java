package com.artemis;

import java.util.BitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.ClassIndexer;

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
			componentsByType.get( i ).set( e.id, null );
		}
		
		componentBits.clear();
	}

	protected void addComponent ( final Entity e, final Component component )
	{
		final int cmpIndex = ClassIndexer.getIndexFor( component.getClass(), Component.class );
		
		componentsByType.ensureCapacity( cmpIndex );

		Bag<Component> components = componentsByType.get( cmpIndex );
		
		if ( components == null )
		{
			components = new Bag<>( Component.class );
			componentsByType.set( cmpIndex, components );
		}

		components.set( e.id, component );

		e.getComponentBits().set( cmpIndex );
	}

	protected void removeComponent ( final Entity e, final Class<? extends Component> type )
	{
		final int cmpIndex = ClassIndexer.getIndexFor( type, Component.class );
		
		if ( e.getComponentBits().get( cmpIndex ) )
		{
			componentsByType.get( cmpIndex ).set( e.id, null );
			e.getComponentBits().clear( cmpIndex );
		}
	}

	protected Bag<Component> getComponentsByType ( final Class<? extends Component> type )
	{
		final int cmpIndex = ClassIndexer.getIndexFor( type, Component.class );
		
		Bag<Component> components = componentsByType.get( cmpIndex );
		
		if ( components == null )
		{
			components = new Bag<>( Component.class );
			componentsByType.set( cmpIndex, components );
		}
		
		return components;
	}

	protected Component getComponent ( final Entity e, final Class<? extends Component> type )
	{
		final int cmpIndex = ClassIndexer.getIndexFor( type, Component.class );
		
		final Bag<Component> components = componentsByType.get( cmpIndex );
		
		if ( components != null )
		{
			return components.get( e.id );
		}
		
		return null;
	}

	public Bag<Component> getComponentsFor ( final Entity e, final Bag<Component> fillBag )
	{
		final BitSet componentBits = e.getComponentBits();

		for ( int i = componentBits.nextSetBit( 0 ); i >= 0; i = componentBits.nextSetBit( i + 1 ) )
		{
			fillBag.add( componentsByType.get( i ).get( e.id ) );
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
		final int size = deleted.size();
		
		if ( size > 0 )
		{
			final Entity[] eArray = deleted.data();

			for ( int i = 0; i < size; ++i )
			{
				removeComponentsOfEntity( eArray[i] );
			}

			deleted.clear();
		}
	}

}
