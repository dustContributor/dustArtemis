package com.artemis;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.lucene.util.BitUtil;
import org.apache.lucene.util.FixedBitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.ClassIndexer;
import com.artemis.utils.FixedBitIterator;
import com.artemis.utils.ImmutableBag;
import com.artemis.utils.SimplePool;

/**
 * @author Arni Arent
 */
final class ComponentManager
{
	/** Mutable iterator for component bits. */
	private final FixedBitIterator bitIterator = new FixedBitIterator();
	/** Bits holding which type of components are pooled. */
	private final FixedBitSet pooledComponentBits = FixedBitSet.newBitSetByWords( DAConstants.COMPONENT_BITS_WORD_COUNT );
	/** Temp bit set to do some operations. */
	private final FixedBitSet tmpBits = FixedBitSet.newBitSetByWords( DAConstants.COMPONENT_BITS_WORD_COUNT );
	/** Component bags by type index. */
	private ComponentMapper<Component>[] componentsByType;
	/** Component pools by type index. */
	private SimplePool<Component>[] poolsByType;

	@SuppressWarnings ( { "unchecked" } )
	ComponentManager ()
	{
		// Fetch component types.
		int size = DAConstants.APPROX_COMPONENT_TYPES;
		// Init all type bags.
		componentsByType = new ComponentMapper[size];
		poolsByType = new SimplePool[size];
	}

	void addComponent ( final Entity e, final Component component )
	{
		final Class<? extends Component> type = component.getClass();
		final int cmpIndex = indexFor( type );

		initIfAbsent( cmpIndex, type ).set( e.id, component );

		e.componentBits.set( cmpIndex );
	}

	void addPooledComponent ( final Entity e, final Class<? extends Component> type )
	{
		final int cmpIndex = indexFor( type );
		final Component cmp = poolsByType[cmpIndex].get();

		initIfAbsent( cmpIndex, type ).set( e.id, cmp );

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

		if ( cmpIndex >= poolsByType.length )
		{
			final int newSize = BitUtil.nextHighestPowerOfTwo( cmpIndex + 1 );
			poolsByType = Arrays.copyOf( poolsByType, newSize );
		}

		poolsByType[cmpIndex] = new SimplePool( type, supplier, resetter );
		pooledComponentBits.set( cmpIndex );
	}

	void removeComponent ( final Entity e, final Class<? extends Component> type )
	{
		final int cmpIndex = indexFor( type );
		final FixedBitSet componentBits = e.componentBits;

		if ( componentBits.get( cmpIndex ) )
		{
			componentsByType[cmpIndex].removeUnsafe( e.id );
			componentBits.clear( cmpIndex );
		}
	}

	void removePooledComponent ( final Entity e, final Class<? extends Component> type )
	{
		final int cmpIndex = indexFor( type );
		final FixedBitSet componentBits = e.componentBits;

		if ( componentBits.get( cmpIndex ) )
		{
			final Component c = componentsByType[cmpIndex].removeUnsafe( e.id );
			componentBits.clear( cmpIndex );
			poolsByType[cmpIndex].store( c );
		}
	}

	Component getComponent ( final Entity e, final Class<? extends Component> type )
	{
		return initIfAbsent( indexFor( type ), type ).get( e.id );
	}

	Bag<Component> getComponentsFor ( final Entity e, final Bag<Component> fillBag )
	{
		final ComponentMapper<Component>[] cmpBags = componentsByType;
		final FixedBitIterator mbi = bitIterator;
		final int eid = e.id;

		mbi.setBits( e.componentBits );

		for ( int i = mbi.nextSetBit(); i >= 0; i = mbi.nextSetBit() )
		{
			fillBag.add( cmpBags[i].getUnsafe( eid ) );
		}

		return fillBag;
	}

	@SuppressWarnings ( "unchecked" )
	<T extends Component> ComponentMapper<T> getMapperFor ( final Class<T> type )
	{
		return (ComponentMapper<T>) initIfAbsent( indexFor( type ), type );
	}

	void clean ( final ImmutableBag<Entity> deleted )
	{
		final Entity[] ents = ((Bag<Entity>) deleted).data();
		final int size = deleted.size();

		clearPooledComponents( ents, size );
		// Now clear all components that aren't pooled.
		clearComponents( ents, size );
		// Clear all bits from entities.
		clearComponentBits( ents, size );
	}

	private final void clearPooledComponents ( final Entity[] ents, final int size )
	{
		final ComponentMapper<Component>[] cmpBags = componentsByType;
		final SimplePool<Component>[] pools = poolsByType;
		final FixedBitIterator mbi = bitIterator;
		final FixedBitSet tmp = tmpBits;

		for ( int i = size; i-- > 0; )
		{
			final FixedBitSet cmpBits = ents[i].componentBits;
			final int eid = ents[i].id;

			tmp.clear();
			tmp.or( pooledComponentBits );
			tmp.and( cmpBits );
			mbi.setBits( tmp );

			for ( int j = mbi.nextSetBit(); j >= 0; j = mbi.nextSetBit() )
			{
				pools[j].store( cmpBags[j].removeUnsafe( eid ) );
			}

			cmpBits.andNot( tmp );
		}
	}

	private final void clearComponents ( final Entity[] ents, final int size )
	{
		final ComponentMapper<Component>[] cmpBags = componentsByType;
		final FixedBitIterator mbi = bitIterator;

		for ( int i = size; i-- > 0; )
		{
			final FixedBitSet cmpBits = ents[i].componentBits;
			final int eid = ents[i].id;

			mbi.setBits( cmpBits );

			for ( int j = mbi.nextSetBit(); j >= 0; j = mbi.nextSetBit() )
			{
				cmpBags[j].removeUnsafe( eid );
			}
		}
	}

	private static final void clearComponentBits ( final Entity[] ents, final int size )
	{
		for ( int i = size; i-- > 0; )
		{
			ents[i].componentBits.clear();
		}
	}

	/**
	 * If the component index passed is too high for the
	 * {@link #componentsByType} to hold, it resizes it.
	 * 
	 * It also initializes the mapper if it isn't present.
	 * 
	 * @param cmpIndex component index to check if it has a component bag
	 *            initialized.
	 * @return Component bag for the given component index.
	 */
	@SuppressWarnings ( { "rawtypes", "unchecked" } )
	private final ComponentMapper<Component> initIfAbsent (
		final int cmpIndex,
		final Class<? extends Component> type )
	{
		// If type bag can't hold this component type.
		if ( cmpIndex >= componentsByType.length )
		{
			final int newLen = BitUtil.nextHighestPowerOfTwo( cmpIndex + 1 );
			componentsByType = Arrays.copyOf( componentsByType, newLen );
		}

		if ( componentsByType[cmpIndex] == null )
		{
			componentsByType[cmpIndex] = new ComponentMapper( type );
		}

		return componentsByType[cmpIndex];
	}

	private static final int indexFor ( final Class<? extends Component> type )
	{
		return ClassIndexer.getIndexFor( type, Component.class );
	}

}
