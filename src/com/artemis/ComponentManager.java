package com.artemis;

import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.BoundedBag;
import com.artemis.utils.ClassIndexer;
import com.artemis.utils.ImmutableBag;

/**
 * @author Arni Arent
 */
public final class ComponentManager
{
	private final Bag<BoundedBag<Component>> componentsByType;

	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	ComponentManager ()
	{
		componentsByType = new Bag( BoundedBag.class, 8 );
		// Init all type bags.
		for ( int i = componentsByType.capacity(); i-- > 0; )
		{
			componentsByType.setUnsafe( i, new BoundedBag<>( Component.class, 4 ) );
		}
	}

	void addComponent ( final Entity e, final Component component )
	{
		final int cmpIndex = ClassIndexer.getIndexFor( component.getClass(), Component.class );
		
		initIfAbsent( cmpIndex ).add( e.id, component );

		e.componentBits.set( cmpIndex );
	}

	void removeComponent ( final Entity e, final Class<? extends Component> type )
	{
		final int cmpIndex = ClassIndexer.getIndexFor( type, Component.class );
		final OpenBitSet componentBits = e.componentBits;
		
		if ( componentBits.get( cmpIndex ) )
		{
			componentsByType.getUnsafe( cmpIndex ).removeUnsafe( e.id );
			componentBits.clear( cmpIndex );
		}
	}

	ImmutableBag<Component> getComponentsByType ( final Class<? extends Component> type )
	{
		final int cmpIndex = ClassIndexer.getIndexFor( type, Component.class );
		
		return initIfAbsent( cmpIndex );
	}

	Component getComponent ( final Entity e, final Class<? extends Component> type )
	{
		return getComponentsByType( type ).get( e.id );
	}

	Bag<Component> getComponentsFor ( final Entity e, final Bag<Component> fillBag )
	{
		final BoundedBag<Component>[] cmpBags = componentsByType.data();
		final OpenBitSet componentBits = e.componentBits;

		for ( int i = componentBits.nextSetBit( 0 ); i >= 0; i = componentBits.nextSetBit( i + 1 ) )
		{
			fillBag.add( cmpBags[i].getUnsafe( e.id ) );
		}

		return fillBag;
	}

	void clean ( final ImmutableBag<Entity> deleted )
	{
		final BoundedBag<Component>[] cmpBags = componentsByType.data();

		final Entity[] delarray = ((Bag<Entity>) deleted).data();
		final int delsize = deleted.size();

		for ( int i = 0; i < delsize; ++i )
		{
			final Entity e = delarray[i];
			final OpenBitSet cmpBits = e.componentBits;
			final int eid = e.id;

			for ( int j = cmpBits.length(); (j = cmpBits.prevSetBit( j - 1 )) >= 0; )
			{
				cmpBags[j].removeUnsafe( eid );
			}

			cmpBits.clear();
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
