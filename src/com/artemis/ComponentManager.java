package com.artemis;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.lucene.util.BitUtil;
import org.apache.lucene.util.FixedBitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.ClassIndexer;
import com.artemis.utils.FixedBitIterator;
import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;
import com.artemis.utils.SimplePool;

/**
 * @author Arni Arent
 */
public final class ComponentManager
{
	/** Mutable iterator for component bits. */
	private final FixedBitIterator bitIterator = new FixedBitIterator();
	/** Bits holding which type of components are pooled. */
	private final FixedBitSet pooledComponentBits = FixedBitSet
			.newBitSetByWords( DAConstants.COMPONENT_BITS_WORD_COUNT );
	/** Temp bit set to do some operations. */
	private final FixedBitSet tmpBits = FixedBitSet
			.newBitSetByWords( DAConstants.COMPONENT_BITS_WORD_COUNT );
	/** Component bags by type index. */
	private ComponentMapper<Component>[] componentsByType;
	/** Component pools by type index. */
	private SimplePool<Component>[] poolsByType;
	/** Component bits for all entities. */
	private final Bag<FixedBitSet> componentBits;

	@SuppressWarnings ( { "unchecked" } )
	ComponentManager ()
	{
		// Fetch component types.
		int size = DAConstants.COMPONENT_TYPES_COUNT;
		// Init all type bags.
		componentsByType = new ComponentMapper[size];
		poolsByType = new SimplePool[size];
		componentBits = new Bag<>( FixedBitSet.class, DAConstants.APPROX_LIVE_ENTITIES );

		final int wCnt = DAConstants.COMPONENT_BITS_WORD_COUNT;
		Arrays.setAll( componentBits.data(), ( i ) -> FixedBitSet.newBitSetByWords( wCnt ) );
	}

	/**
	 * Add a component to an entity.
	 * 
	 * @param eid id of the entity to add the component to.
	 * @param component to add to the entity
	 */
	public void addComponent ( final int eid, final Component component )
	{
		final Class<? extends Component> type = component.getClass();
		final int cmpIndex = indexFor( type );
		addComponent( eid, component, type, cmpIndex );
	}

	/**
	 * Add a pooled component to an entity.
	 * 
	 * @param eid id of the entity to add the component to.
	 * @param type of component to add to the entity
	 */
	public void addPooledComponent ( final int eid, final Class<? extends Component> type )
	{
		final int cmpIndex = indexFor( type );
		addComponent( eid, poolsByType[cmpIndex].get(), type, cmpIndex );
	}

	private final void addComponent (
		final int eid,
		final Component component,
		final Class<? extends Component> type,
		final int cmpIndex )
	{
		final ComponentMapper<Component> cm = initIfAbsent( cmpIndex, type );
		cm.ensureCapacity( eid );
		cm.setUnsafe( eid, component );

		componentBits.getUnsafe( eid ).set( cmpIndex );
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

	/**
	 * Remove component by its type.
	 * 
	 * @param eid id of the entity to remove the component from.
	 * @param type of the component to be removed.
	 */
	public void removeComponent ( final int eid, final Class<? extends Component> type )
	{
		final int cmpIndex = indexFor( type );
		final FixedBitSet bits = componentBits.getUnsafe( eid );

		if ( bits.get( cmpIndex ) )
		{
			componentsByType[cmpIndex].removeUnsafe( eid );
			bits.clear( cmpIndex );
		}
	}

	/**
	 * Remove pooled component by its type.
	 * 
	 * @param eid id of the entity to remove the component from.
	 * @param type of the component to be removed.
	 */
	public void removePooledComponent ( final int eid, final Class<? extends Component> type )
	{
		final int cmpIndex = indexFor( type );
		final FixedBitSet bits = componentBits.getUnsafe( eid );

		if ( bits.get( cmpIndex ) )
		{
			final Component c = componentsByType[cmpIndex].removeUnsafe( eid );
			bits.clear( cmpIndex );
			poolsByType[cmpIndex].store( c );
		}
	}

	/**
	 * Slower retrieval of components from an entity. The recommended way to
	 * retrieve components from an entity is using the ComponentMapper. Is fine
	 * to use e.g. when creating new entities and setting data in components.
	 * 
	 * @param <T> the expected component type.
	 * @param eid id of the entity to retrieve the component from.
	 * @param type the expected return component type.
	 * @return component that matches, or null if none is found.
	 */
	@SuppressWarnings ( "unchecked" )
	public <T extends Component> T getComponent ( final int eid, final Class<T> type )
	{
		return (T) initIfAbsent( indexFor( type ), type ).get( eid );
	}

	public Bag<Component> getComponentsFor ( final int eid, final Bag<Component> fillBag )
	{
		final ComponentMapper<Component>[] cmpBags = componentsByType;
		final FixedBitIterator mbi = bitIterator;

		mbi.setBits( componentBits.getUnsafe( eid ) );

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

	void clean ( final ImmutableIntBag deleted )
	{
		final int[] ents = ((IntBag) deleted).data();
		final int size = deleted.size();

		clearPooledComponents( ents, size );
		// Now clear all components that aren't pooled.
		clearComponents( ents, size );
		// Clear all bits from entities.
		clearComponentBits( ents, size );
	}

	void initBitsIfAbsent ( final int eid )
	{
		componentBits.ensureCapacity( eid );

		if ( componentBits.getUnsafe( eid ) == null )
		{
			final int wCnt = DAConstants.COMPONENT_BITS_WORD_COUNT;
			componentBits.setUnsafe( eid, FixedBitSet.newBitSetByWords( wCnt ) );
		}
	}

	private final void clearPooledComponents ( final int[] ents, final int size )
	{
		final ComponentMapper<Component>[] cmpBags = componentsByType;
		final SimplePool<Component>[] pools = poolsByType;
		final FixedBitSet[] bits = componentBits.data();
		final FixedBitIterator mbi = bitIterator;
		final FixedBitSet tmp = tmpBits;
		final FixedBitSet pcb = pooledComponentBits;

		mbi.setBits( tmp );

		for ( int i = size; i-- > 0; )
		{
			final int eid = ents[i];

			tmp.copyFrom( pcb );
			tmp.and( bits[eid] );
			mbi.reset();

			for ( int j = mbi.nextSetBit(); j >= 0; j = mbi.nextSetBit() )
			{
				pools[j].store( cmpBags[j].removeUnsafe( eid ) );
			}
		}

		for ( int i = size; i-- > 0; )
		{
			bits[ents[i]].andNot( pcb );
		}
	}

	private final void clearComponents ( final int[] ents, final int size )
	{
		final ComponentMapper<Component>[] cmpBags = componentsByType;
		final FixedBitSet[] bits = componentBits.data();
		final FixedBitIterator mbi = bitIterator;

		for ( int i = size; i-- > 0; )
		{
			final int eid = ents[i];

			mbi.setBits( bits[eid] );

			for ( int j = mbi.nextSetBit(); j >= 0; j = mbi.nextSetBit() )
			{
				cmpBags[j].removeUnsafe( eid );
			}
		}
	}

	private final void clearComponentBits ( final int[] ents, final int size )
	{
		final FixedBitSet[] bits = componentBits.data();
		
		for ( int i = size; i-- > 0; )
		{
			bits[ents[i]].clear();
		}
	}

	final Bag<FixedBitSet> getComponentBits ()
	{
		return componentBits;
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
