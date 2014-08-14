package com.artemis;

import java.util.BitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.BoundedBag;
import com.artemis.utils.ClassIndexer;
import com.artemis.utils.ImmutableBag;

/**
 * @author Arni Arent
 */
public class ComponentManager extends Manager
{
	private final Bag<BoundedBag<Component>> componentsByType;
	private final Bag<Entity> deleted;

	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	public ComponentManager ()
	{
		componentsByType = new Bag( BoundedBag.class, 8 );
		deleted = new Bag<>( Entity.class, 8 );
		// Init all type bags.
		for ( int i = componentsByType.capacity(); i-- > 0; )
		{
			componentsByType.setUnsafe( i, new BoundedBag<>( Component.class, 4 ) );
		}
	}

	@Override
	protected void initialize ()
	{
		// Empty method.
	}

	private void removeComponentsOfEntity ( final Entity e )
	{
		final BitSet componentBits = e.componentBits;
		
		for ( int i = componentBits.nextSetBit( 0 ); i >= 0; i = componentBits.nextSetBit( i + 1 ) )
		{
			componentsByType.getUnsafe( i ).removeUnsafe( e.id );
		}
		
		componentBits.clear();
	}

	protected void addComponent ( final Entity e, final Component component )
	{
		final int cmpIndex = ClassIndexer.getIndexFor( component.getClass(), Component.class );
		
		initIfAbsent( cmpIndex ).add( e.id, component );

		e.componentBits.set( cmpIndex );
	}

	protected void removeComponent ( final Entity e, final Class<? extends Component> type )
	{
		final int cmpIndex = ClassIndexer.getIndexFor( type, Component.class );
		final BitSet componentBits = e.componentBits;
		
		if ( componentBits.get( cmpIndex ) )
		{
			componentsByType.getUnsafe( cmpIndex ).removeUnsafe( e.id );
			componentBits.clear( cmpIndex );
		}
	}

	protected ImmutableBag<Component> getComponentsByType ( final Class<? extends Component> type )
	{
		final int cmpIndex = ClassIndexer.getIndexFor( type, Component.class );
		
		return initIfAbsent( cmpIndex );
	}

	protected Component getComponent ( final Entity e, final Class<? extends Component> type )
	{
		return getComponentsByType( type ).get( e.id );
	}

	public Bag<Component> getComponentsFor ( final Entity e, final Bag<Component> fillBag )
	{
		final BitSet componentBits = e.componentBits;

		for ( int i = componentBits.nextSetBit( 0 ); i >= 0; i = componentBits.nextSetBit( i + 1 ) )
		{
			fillBag.add( componentsByType.getUnsafe( i ).getUnsafe( e.id ) );
		}

		return fillBag;
	}
	
	@Override
	public void deleted ( ImmutableBag<Entity> entities )
	{
		deleted.addAll( entities );
	}

//	@Override
//	public void deleted ( final Entity e )
//	{
//		deleted.add( e );
//	}

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

	/**
	 * If the component index passed is too high for the
	 * {@link #componentsByType} to hold, it resizes it and initializes all the
	 * component bags between the old {@link #componentsByType} capacity and new
	 * one. This way, {@link #componentsByType} will never have a
	 * <code>null</code> value.
	 * 
	 * @param cmpIndex
	 *            component index to check if it has a component bag
	 *            initialized.
	 * @return Component bag for the given component index.
	 */
	private final BoundedBag<Component> initIfAbsent ( final int cmpIndex )
	{
		final int prevCap = componentsByType.capacity();
		// If type bag can't hold this component type.
		if ( cmpIndex >= prevCap )
		{
			componentsByType.ensureCapacity( cmpIndex );
			// Init all the missing bags.
			for ( int i = componentsByType.capacity(); i-- > prevCap; )
			{
				componentsByType.setUnsafe( i, new BoundedBag<>( Component.class, 4 ) );
			}
		}
		
		return componentsByType.getUnsafe( cmpIndex );
	}

}
