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
 * @author dustContributor
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
	private FixedBitSet[] componentBits;

	@SuppressWarnings( { "unchecked" } )
	ComponentManager ()
	{
		// Init all type bags.
		componentsByType = new ComponentMapper[DAConstants.COMPONENT_TYPES_COUNT];
		poolsByType = new SimplePool[DAConstants.COMPONENT_TYPES_COUNT];
		componentBits = new FixedBitSet[DAConstants.APPROX_LIVE_ENTITIES];

		// Init bitsets for all entities.
		final int wCnt = DAConstants.COMPONENT_BITS_WORD_COUNT;
		Arrays.setAll( componentBits, ( i ) -> FixedBitSet.newBitSetByWords( wCnt ) );
	}

	/**
	 * Add a component to an entity.
	 * 
	 * @param eid id of the entity to add the component to.
	 * @param component to add to the entity.
	 */
	public final void addComponent ( final int eid, final Component component )
	{
		final Class<? extends Component> type = component.getClass();
		final int cmpIndex = indexFor( type );
		final ComponentMapper<Component> cm = initIfAbsent( cmpIndex, type );
		cm.ensureCapacity( eid );
		cm.setUnsafe( eid, component );

		componentBits[eid].set( cmpIndex );
	}

	/**
	 * Faster way to add a pooled component to an entity if you already have the
	 * related mapper at hand.
	 * 
	 * It adds the passed component instance to the entity.
	 * 
	 * @param eid id of the entity to add the component to.
	 * @param component component to add to the entity.
	 * @param mapper of the type of the component that will be added to the
	 *          entity.
	 */
	public final <T extends Component> void addComponent (
			final int eid,
			final T component,
			final ComponentMapper<T> mapper )
	{
		final int cmpIndex = mapper.typeIndex;
		mapper.ensureCapacity( eid );
		mapper.setUnsafe( eid, component );
		componentBits[eid].set( cmpIndex );
	}

	/**
	 * Add a pooled component to an entity.
	 * 
	 * @param eid id of the entity to add the component to.
	 * @param type of component to add to the entity
	 */
	public final void addPooledComponent ( final int eid, final Class<? extends Component> type )
	{
		final int cmpIndex = indexFor( type );
		final ComponentMapper<Component> cm = initIfAbsent( cmpIndex, type );
		cm.ensureCapacity( eid );
		cm.setUnsafe( eid, poolsByType[cmpIndex].get() );

		componentBits[eid].set( cmpIndex );
	}

	/**
	 * Faster way to add a pooled component to an entity if you already have the
	 * related mapper at hand.
	 * 
	 * It retrieves an existing component of a pool and adds it to an entity.
	 * 
	 * @param eid id of the entity to add the component to.
	 * @param mapper of the type of the component that will be added to the
	 *          entity.
	 */
	@SuppressWarnings( "unchecked" )
	public final <T extends Component> void addPooledComponent (
			final int eid,
			final ComponentMapper<T> mapper )
	{
		final int cmpIndex = mapper.typeIndex;
		mapper.ensureCapacity( eid );
		mapper.setUnsafe( eid, (T) poolsByType[cmpIndex].get() );
		componentBits[eid].set( cmpIndex );
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	final <T extends Component> void registerPoolable (
			final Class<T> type,
			final Supplier<T> supplier,
			final Consumer<T> resetter )
	{
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
	public final void removeComponent ( final int eid, final Class<? extends Component> type )
	{
		final int cmpIndex = indexFor( type );
		final FixedBitSet bits = componentBits[eid];

		if ( bits.get( cmpIndex ) )
		{
			componentsByType[cmpIndex].removeUnsafe( eid );
			bits.clear( cmpIndex );
		}
	}

	/**
	 * Faster way to remove a component of an entity if you already have the
	 * related mapper at hand.
	 * 
	 * Remove component by its type.
	 * 
	 * @param eid id of the entity to remove the component from.
	 * @param mapper related to the type of component that will be removed of the
	 *          entity.
	 */
	public final void removeComponent ( final int eid, final ComponentMapper<? extends Component> mapper )
	{
		final int cmpIndex = mapper.typeIndex;
		final FixedBitSet bits = componentBits[eid];

		if ( bits.get( cmpIndex ) )
		{
			mapper.removeUnsafe( eid );
			bits.clear( cmpIndex );
		}
	}

	/**
	 * Remove pooled component by its type.
	 * 
	 * @param eid id of the entity to remove the component from.
	 * @param type of the component to be removed.
	 */
	public final void removePooledComponent ( final int eid, final Class<? extends Component> type )
	{
		final int cmpIndex = indexFor( type );
		final FixedBitSet bits = componentBits[eid];

		if ( bits.get( cmpIndex ) )
		{
			final Component c = componentsByType[cmpIndex].removeUnsafe( eid );
			bits.clear( cmpIndex );
			poolsByType[cmpIndex].store( c );
		}
	}

	/**
	 * Faster way to remove a pooled component of an entity if you already have
	 * the related mapper at hand. It stores the component back into the pool.
	 * 
	 * Remove component by its type.
	 * 
	 * @param eid id of the entity to remove the component from.
	 * @param mapper related to the type of component that will be removed of the
	 *          entity.
	 */
	public final void removePooledComponent (
			final int eid,
			final ComponentMapper<? extends Component> mapper )
	{
		final int cmpIndex = mapper.typeIndex;
		final FixedBitSet bits = componentBits[eid];

		if ( bits.get( cmpIndex ) )
		{
			final Component c = mapper.removeUnsafe( eid );
			bits.clear( cmpIndex );
			poolsByType[cmpIndex].store( c );
		}
	}

	/**
	 * Slower retrieval of components from an entity. The recommended way to
	 * retrieve components from an entity is using the ComponentMapper. Is fine to
	 * use e.g. when creating new entities and setting data in components.
	 * 
	 * @param <T> the expected component type.
	 * @param eid id of the entity to retrieve the component from.
	 * @param type the expected return component type.
	 * @return component that matches, or null if none is found.
	 */
	@SuppressWarnings( "unchecked" )
	public final <T extends Component> T getComponent ( final int eid, final Class<T> type )
	{
		return (T) initIfAbsent( indexFor( type ), type ).get( eid );
	}

	public final Bag<Component> getComponentsFor ( final int eid, final Bag<Component> fillBag )
	{
		final ComponentMapper<Component>[] cmpBags = componentsByType;
		final FixedBitIterator mbi = bitIterator;

		mbi.setBits( componentBits[eid] );

		for ( int i = mbi.nextSetBit(); i >= 0; i = mbi.nextSetBit() )
		{
			fillBag.add( cmpBags[i].getUnsafe( eid ) );
		}

		return fillBag;
	}

	@SuppressWarnings( "unchecked" )
	final <T extends Component> ComponentMapper<T> getMapperFor ( final Class<T> type )
	{
		return (ComponentMapper<T>) initIfAbsent( indexFor( type ), type );
	}

	final void clean ( final ImmutableIntBag deleted )
	{
		// Clear all components.
		clearComponents( ((IntBag) deleted).data(), deleted.size() );
	}

	final void registerEntity ( final int eid )
	{
		if ( eid >= componentBits.length )
		{
			final int newLen = BitUtil.nextHighestPowerOfTwo( eid + 1 );
			componentBits = Arrays.copyOf( componentBits, newLen );
		}

		final FixedBitSet[] bits = componentBits;

		if ( bits[eid] == null )
		{
			final int wCnt = DAConstants.COMPONENT_BITS_WORD_COUNT;
			bits[eid] = FixedBitSet.newBitSetByWords( wCnt );
		}
	}

	private final void clearComponents ( final int[] ents, final int size )
	{
		final ComponentMapper<Component>[] cmpBags = componentsByType;
		final SimplePool<Component>[] pools = poolsByType;
		final FixedBitSet[] bits = componentBits;
		final FixedBitIterator mbi = bitIterator;
		final FixedBitSet tmp = tmpBits;
		final FixedBitSet pcb = pooledComponentBits;

		for ( int i = size; i-- > 0; )
		{
			final int eid = ents[i];
			final FixedBitSet ebits = bits[eid];

			// Copy pooled component bits.
			tmp.copyFrom( pcb );
			// Keep only the ones the entity has.
			tmp.and( ebits );

			// Now iterate over them and put them in the pools.
			mbi.setBits( tmp );

			for ( int j = mbi.nextSetBit(); j >= 0; j = mbi.nextSetBit() )
			{
				pools[j].store( cmpBags[j].removeUnsafe( eid ) );
			}

			// Remove pooled bits from entity.
			ebits.andNot( pcb );

			// Now iterate over normal component bits and remove them.
			mbi.setBits( ebits );

			for ( int j = mbi.nextSetBit(); j >= 0; j = mbi.nextSetBit() )
			{
				cmpBags[j].removeUnsafe( eid );
			}

			// Now clear all component bits from the entity.
			ebits.clear();
		}
	}

	final FixedBitSet[] componentBits ()
	{
		return componentBits;
	}

	/**
	 * If the component index passed is too high for the {@link #componentsByType}
	 * to hold, it resizes it.
	 * 
	 * It also initializes the mapper if it isn't present.
	 * 
	 * @param typeIndex component type index to check if it has a component bag
	 *          initialized.
	 * @return Component bag for the given component index.
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	private final ComponentMapper<Component> initIfAbsent (
			final int typeIndex,
			final Class<? extends Component> type )
	{
		// If type bag can't hold this component type.
		if ( typeIndex >= componentsByType.length )
		{
			final int newLen = BitUtil.nextHighestPowerOfTwo( typeIndex + 1 );
			componentsByType = Arrays.copyOf( componentsByType, newLen );
		}

		final ComponentMapper<Component>[] mappers = componentsByType;

		if ( mappers[typeIndex] == null )
		{
			final int cap = DAConstants.APPROX_LIVE_ENTITIES / 2;
			mappers[typeIndex] = new ComponentMapper( type, typeIndex, cap );
		}

		return mappers[typeIndex];
	}

	private static final int indexFor ( final Class<? extends Component> type )
	{
		return ClassIndexer.getIndexFor( type, Component.class );
	}

}
