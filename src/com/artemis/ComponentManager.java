package com.artemis;

import java.util.Arrays;

import org.apache.lucene.util.BitUtil;
import org.apache.lucene.util.FixedBitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.ClassIndexer;
import com.artemis.utils.FixedBitIterator;
import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;

/**
 * @author Arni Arent
 * @author dustContributor
 */
public final class ComponentManager
{
	/** Mutable iterator for component bits. */
	private final FixedBitIterator bitIterator = new FixedBitIterator();
	/** Component bags by type index. */
	private ComponentHandler<Component>[] componentHandlers;
	/** Component bits for all entities. */
	private FixedBitSet[] componentBits;

	@SuppressWarnings( { "unchecked" } )
	ComponentManager ()
	{
		// Init all type bags.
		componentHandlers = new ComponentHandler[DAConstants.COMPONENT_TYPES_COUNT];
		componentBits = new FixedBitSet[DAConstants.APPROX_LIVE_ENTITIES];

		// Init bitsets for all entities.
		final int wCnt = DAConstants.COMPONENT_BITS_WORD_COUNT;
		Arrays.setAll( componentBits, ( i ) -> FixedBitSet.newBitSetByWords( wCnt ) );
	}

	/**
	 * @param id of the entity to fetch all its components from.
	 * @param dest bag to store all the components.
	 * @return bag passed as 'dest' parameter.
	 */
	public final Bag<Component> getComponentsFor ( final int id, final Bag<Component> dest )
	{
		final ComponentHandler<Component>[] cmpBags = componentHandlers;
		final FixedBitIterator mbi = bitIterator;

		mbi.setBits( componentBits[id] );

		for ( int i = mbi.nextSetBit(); i >= 0; i = mbi.nextSetBit() )
		{
			dest.add( cmpBags[i].get( id ) );
		}

		return dest;
	}

	/**
	 * Either creates or retrieves a component handler for the passed component
	 * type.
	 * 
	 * @param type of the component to create or retrieve a handler from.
	 * @return handler of the component type.
	 */
	@SuppressWarnings( "unchecked" )
	public final <T extends Component> ComponentHandler<T> getHandlerFor ( final Class<T> type )
	{
		return (ComponentHandler<T>) initIfAbsent( indexFor( type ), type );
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

	final FixedBitSet[] componentBits ()
	{
		return componentBits;
	}

	final void clean ( final ImmutableIntBag deleted )
	{
		// Clear all components.
		clearComponents( ((IntBag) deleted).data(), deleted.size() );
	}

	private final void clearComponents ( final int[] ents, final int size )
	{
		final ComponentHandler<Component>[] cmpBags = componentHandlers;
		final FixedBitSet[] bits = componentBits;
		final FixedBitIterator mbi = bitIterator;

		for ( int i = size; i-- > 0; )
		{
			final int eid = ents[i];
			final FixedBitSet ebits = bits[eid];

			// Now iterate over normal component bits and remove them.
			mbi.setBits( ebits );

			for ( int j = mbi.nextSetBit(); j >= 0; j = mbi.nextSetBit() )
			{
				cmpBags[j].data()[eid] = null;
			}

			// Now clear all component bits from the entity.
			ebits.clear();
		}
	}

	/**
	 * If the component index passed is too high for the
	 * {@link #componentHandlers} to hold, it resizes it.
	 * 
	 * It also initializes the handler if it isn't present.
	 * 
	 * @param typeIndex component type index to check if it has a component bag
	 *          initialized.
	 * @return Component bag for the given component index.
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	private final ComponentHandler<Component> initIfAbsent (
			final int typeIndex,
			final Class<? extends Component> type )
	{
		// If type bag can't hold this component type.
		if ( typeIndex >= componentHandlers.length )
		{
			final int newLen = BitUtil.nextHighestPowerOfTwo( typeIndex + 1 );
			componentHandlers = Arrays.copyOf( componentHandlers, newLen );
		}

		final ComponentHandler<Component>[] handlers = componentHandlers;

		if ( handlers[typeIndex] == null )
		{
			final int cap = DAConstants.APPROX_LIVE_ENTITIES / 2;
			handlers[typeIndex] = new ComponentHandler( type, this, typeIndex, cap );
		}

		return handlers[typeIndex];
	}

	private static final int indexFor ( final Class<? extends Component> type )
	{
		return ClassIndexer.getIndexFor( type, Component.class );
	}

}
