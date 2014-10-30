package com.artemis;

import java.util.Arrays;
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
final class ComponentManager
{
	/** Mutable iterator for component bits. */
	private final MutableBitIterator bitIterator = new MutableBitIterator();
	/** Bits holding which type of components are pooled. */
	private final OpenBitSet pooledComponentBits = new OpenBitSet();
	/** Temp bit set to do some operations. */
	private final OpenBitSet tmpBits = new OpenBitSet();
	/** Component bags by type index. */
	private BoundedBag<Component>[] componentsByType;
	/** Component pools by type index. */
	private SimplePool<Component>[] poolsByType;

	@SuppressWarnings ( { "unchecked" } )
	ComponentManager ()
	{
		// Fetch component types.
		int size = DAConstants.APPROX_COMPONENT_TYPES;
		// Init all type bags.
		componentsByType = initFrom( new BoundedBag[size], 0 );
		poolsByType = new SimplePool[size];
	}

	void addComponent ( final Entity e, final Component component )
	{
		final int cmpIndex = indexFor( component.getClass() );

		initIfAbsent( cmpIndex ).set( e.id, component );

		e.componentBits.set( cmpIndex );
	}

	void addPooledComponent ( final Entity e, final Class<? extends Component> type )
	{
		final int cmpIndex = indexFor( type );
		final Component cmp = poolsByType[cmpIndex].get();

		initIfAbsent( cmpIndex ).set( e.id, cmp );

		e.componentBits.set( cmpIndex );
	}

	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	<T extends Component> void registerPoolable (
		final Class<T> type,
		final Supplier<T> supplier,
		Consumer<T> resetter )
	{
		resetter = (resetter != null) ? resetter : ( a ) -> {
		};

		final int cmpIndex = indexFor( type );

		if ( cmpIndex <= poolsByType.length )
		{
			poolsByType = Arrays.copyOf( poolsByType, cmpIndex + 1 );
		}

		poolsByType[cmpIndex] = new SimplePool( type, supplier, resetter );
		pooledComponentBits.set( cmpIndex );
	}

	void removeComponent ( final Entity e, final Class<? extends Component> type )
	{
		final int cmpIndex = indexFor( type );
		final OpenBitSet componentBits = e.componentBits;

		if ( componentBits.get( cmpIndex ) )
		{
			componentsByType[cmpIndex].removeUnsafe( e.id );
			componentBits.fastClear( cmpIndex );
		}
	}

	ImmutableBag<Component> getComponentsByType ( final Class<? extends Component> type )
	{
		final int cmpIndex = indexFor( type );

		return initIfAbsent( cmpIndex );
	}

	Component getComponent ( final Entity e, final Class<? extends Component> type )
	{
		return getComponentsByType( type ).get( e.id );
	}

	Bag<Component> getComponentsFor ( final Entity e, final Bag<Component> fillBag )
	{
		final BoundedBag<Component>[] cmpBags = componentsByType;
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
		final BoundedBag<Component>[] cmpBags = componentsByType;
		final MutableBitIterator mbi = bitIterator;

		final Entity[] delarray = ((Bag<Entity>) deleted).data();
		final int delsize = deleted.size();

		for ( int i = 0; i < delsize; ++i )
		{
			final OpenBitSet cmpBits = delarray[i].componentBits;
			final int eid = delarray[i].id;

			clearPooledComponents( cmpBits, eid );

			// Now clear all components that aren't pooled.
			mbi.setBits( cmpBits.getBits() );

			for ( int j = mbi.nextSetBit(); j >= 0; j = mbi.nextSetBit() )
			{
				cmpBags[j].removeUnsafe( eid );
			}
		}

		clearComponentBits( delarray, delsize );
	}
	
	void clearPooledComponents ( final OpenBitSet cmpBits, final int eid )
	{
//		debugPools();
		
		final BoundedBag<Component>[] cmpBags = componentsByType;
		final SimplePool<Component>[] pools = poolsByType;
		final MutableBitIterator mbi = bitIterator;
		final OpenBitSet tmp = tmpBits;

		tmp.clear();
		tmp.or( pooledComponentBits );
		tmp.and( cmpBits );
		mbi.setBits( tmp.getBits() );

		for ( int j = mbi.nextSetBit(); j >= 0; j = mbi.nextSetBit() )
		{
			pools[j].store( cmpBags[j].removeUnsafe( eid ) );
		}

		cmpBits.andNot( tmp );
	}

//	private long start = System.nanoTime();
//	private long end = 0L;
//	private long diff = 0L;
//	
//	private void debugPools ()
//	{
//		end = System.nanoTime();
//		diff += ( end - start );
//		start = System.nanoTime();
//		
//		if ( diff > 1000000000L )
//		{
//			diff = 0L;
//			StringBuilder tmp = new StringBuilder( 40 );
//			for ( int i = poolsByType.length; i-- > 0; )
//			{
//				if ( poolsByType[i] != null )
//				{
//					String name = poolsByType[i].type.getName();
//					name = name.substring( name.lastIndexOf( '.' ) + 1 );
//					tmp.append( "POOL OF: " );
//					tmp.append( name );
//					for ( int j = 12 - name.length(); j-- > 0; )
//					{
//						tmp.append( ' ' );
//					}
//					tmp.append( "- SIZE: " );
//					tmp.append( poolsByType[i].store.size() );
//					System.out.println(tmp.toString());
//					tmp.delete( 0, tmp.length() );
//				}
//			}
//		}
//	}
	
	private static final void clearComponentBits ( final Entity[] ents, final int size )
	{
		for ( int i = size; i-- > 0; )
		{
			ents[i].componentBits.clear();
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
	private final BoundedBag<Component> initIfAbsent ( final int cmpIndex )
	{
		final int prevCap = componentsByType.length;
		// If type bag can't hold this component type.
		if ( cmpIndex >= prevCap )
		{
			// Expand and init bag array.
			componentsByType = initFrom( Arrays.copyOf( componentsByType, cmpIndex + 1 ), prevCap );
		}

		return componentsByType[cmpIndex];
	}

	private static final int indexFor ( final Class<? extends Component> type )
	{
		return ClassIndexer.getIndexFor( type, Component.class );
	}

	private static final BoundedBag<Component>[] initFrom (
		final BoundedBag<Component>[] bags,
		final int position )
	{
		for ( int i = bags.length; i-- > position; )
		{
			bags[i] = new BoundedBag<>( Component.class );
		}

		return bags;
	}

}
