package com.artemis;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.BoundedBag;
import com.artemis.utils.ClassIndexer;
import com.artemis.utils.ImmutableBag;
import com.artemis.utils.MutableBitIterator;
import com.artemis.utils.SimplePool;

/**
 * @author Arni Arent
 */
final class PooledComponentManager
{
	/** Mutable iterator for component bits. */
	private final MutableBitIterator bitIterator = new MutableBitIterator();
	private final OpenBitSet poolableBits = new OpenBitSet();

	private final Bag<BoundedBag<PooledComponent>> componentsByType;
	@SuppressWarnings ( "rawtypes" )
	private final Bag<SimplePool> poolsByType;

	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	PooledComponentManager ()
	{
		// Fetch component types.
		int size = DAConstants.APPROX_COMPONENT_TYPES;

		componentsByType = new Bag( BoundedBag.class, size );
		poolsByType = new Bag<>( SimplePool.class, size );
		// Init all type bags.
		for ( int i = componentsByType.capacity(); i-- > 0; )
		{
			componentsByType.setUnsafe( i, new BoundedBag<>( PooledComponent.class, 4 ) );
		}
	}

	void addComponent ( final Entity e, final Class<? extends PooledComponent> type )
	{
		final int cmpIndex = indexFor( type );
		final SimplePool<PooledComponent> factory = poolsByType.getUnsafe( cmpIndex );

		initIfAbsent( cmpIndex ).set( e.id, factory.get() );

		e.componentBits.set( cmpIndex );
	}

	<T extends PooledComponent> void registerPoolable (
		final Class<T> type,
		final Supplier<T> supplier,
		final Consumer<T> resetter )
	{
		final int cmpIndex = indexFor( type );

		poolsByType.set( cmpIndex, new SimplePool<>( type, supplier, resetter ) );
		poolableBits.set( cmpIndex );
	}

	void removeComponent ( final Entity e, final Class<? extends PooledComponent> type )
	{
		final int cmpIndex = indexFor( type );
		final OpenBitSet componentBits = e.componentBits;

		if ( componentBits.get( cmpIndex ) )
		{
			componentsByType.getUnsafe( cmpIndex ).removeUnsafe( e.id );
			componentBits.fastClear( cmpIndex );
		}
	}

	ImmutableBag<PooledComponent> getComponentsByType ( final Class<? extends PooledComponent> type )
	{
		return initIfAbsent( indexFor( type ) );
	}

	PooledComponent getComponent ( final Entity e, final Class<? extends PooledComponent> type )
	{
		return getComponentsByType( type ).get( e.id );
	}

	Bag<PooledComponent> getComponentsFor ( final Entity e, final Bag<PooledComponent> fillBag )
	{
		final BoundedBag<PooledComponent>[] cmpBags = componentsByType.data();
		final MutableBitIterator mbi = bitIterator;
		final int eid = e.id;

		mbi.setBits( e.componentBits.getBits() );

		for ( int i = mbi.nextSetBit(); i >= 0; i = mbi.nextSetBit() )
		{
			fillBag.add( cmpBags[i].getUnsafe( eid ) );
		}

		return fillBag;
	}

	void clean ( final ImmutableBag<Entity> deleted )
	{
		final BoundedBag<PooledComponent>[] cmpBags = componentsByType.data();
		final SimplePool<PooledComponent>[] facs = poolsByType.data();

		final MutableBitIterator mbi = bitIterator;
		final OpenBitSet pbits = poolableBits;

		final Entity[] delents = ((Bag<Entity>) deleted).data();
		final int delsize = deleted.size();

		for ( int i = 0; i < delsize; ++i )
		{
			final int eid = delents[i].id;

			mbi.setBits( delents[i].componentBits.getBits() );
			pbits.ensureCapacityWords( delents[i].componentBits.getBits().length );

			for ( int j = mbi.nextSetBit(); j >= 0; j = mbi.nextSetBit() )
			{
				if ( poolableBits.fastGet( j ) )
				{
					facs[j].store( cmpBags[j].removeUnsafe( eid ) );
				}
			}
		}
	}

	/**
	 * If the component index passed is too high for the
	 * {@link #componentsByType} to hold, it resizes it and initializes all the
	 * component bags between the old {@link #componentsByType} capacity and new
	 * one. This way, {@link #componentsByType} will never have a
	 * <code>null</code> value.
	 * 
	 * @param cmpIndex component index to check if it has a component bag
	 *            initialized.
	 * @return Component bag for the given component index.
	 */
	private final BoundedBag<PooledComponent> initIfAbsent ( final int cmpIndex )
	{
		final int prevCap = componentsByType.capacity();
		// If type bag can't hold this component type.
		if ( cmpIndex >= prevCap )
		{
			componentsByType.ensureCapacity( cmpIndex );
			// Init all the missing bags.
			for ( int i = componentsByType.capacity(); i-- > prevCap; )
			{
				componentsByType.setUnsafe( i, new BoundedBag<>( PooledComponent.class, 4 ) );
			}
		}

		return componentsByType.getUnsafe( cmpIndex );
	}

	private static final int indexFor ( final Class<? extends Component> type )
	{
		return ClassIndexer.getIndexFor( type, Component.class );
	}

}
